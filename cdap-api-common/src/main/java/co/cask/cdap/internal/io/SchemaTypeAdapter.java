/*
 * Copyright © 2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.internal.io;

import co.cask.cdap.api.data.schema.Schema;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for serialize/deserialize Schema object to/from json through {@link com.google.gson.Gson Gson}.
 * <p>
 *  Expected usage:
 *
 *  <pre>
 *    Schema schema = ...;
 *    Gson gson = new GsonBuilder()
 *                  .registerTypeAdapter(Schema.class, new SchemaTypeAdapter())
 *                  .create();
 *    String json = gson.toJson(schema);
 *
 *    Schema newSchema = gson.fromJson(json, Schema.class);
 *  </pre>
 * </p>
 */
public final class SchemaTypeAdapter extends TypeAdapter<Schema> {

  @Override
  public void write(JsonWriter writer, Schema schema) throws IOException {
    if (schema == null) {
      writer.nullValue();
      return;
    }
    Set<String> knownRecords = new HashSet<>();
    write(writer, schema, knownRecords);
  }

  @Override
  public Schema read(JsonReader reader) throws IOException {
    return read(reader, new HashMap<String, Schema>());
  }

  /**
   * Reads json value and convert it into {@link Schema} object.
   *
   * @param reader Source of json
   * @param knownRecords Set of record name already encountered during the reading.
   * @return A {@link Schema} reflecting the json.
   * @throws IOException Any error during reading.
   */
  private Schema read(JsonReader reader, Map<String, Schema> knownRecords) throws IOException {
    JsonToken token = reader.peek();
    switch (token) {
      case NULL:
        return null;
      case STRING: {
        // Simple type or know record type
        String name = reader.nextString();
        if (knownRecords.containsKey(name)) {
          Schema schema = knownRecords.get(name);
          /*
             schema is null and in the map if this is a recursive reference. For example,
             if we're looking at the inner 'node' record in the example below:
             {
               "type": "record",
               "name": "node",
               "fields": [{
                 "name": "children",
                 "type": [{
                   "type": "array",
                   "items": ["node", "null"]
                 }, "null"]
               }, {
                 "name": "data",
                 "type": "int"
               }]
             }
           */
          return schema == null ? Schema.recordOf(name) : schema;
        }
        return Schema.of(Schema.Type.valueOf(name.toUpperCase()));
      }
      case BEGIN_ARRAY:
        // Union type
        return readUnion(reader, knownRecords);
      case BEGIN_OBJECT: {
        reader.beginObject();
        String name = reader.nextName();
        if (!"type".equals(name)) {
          throw new IOException("Property \"type\" missing.");
        }
        Schema.Type schemaType = Schema.Type.valueOf(reader.nextString().toUpperCase());

        Schema schema;
        switch (schemaType) {
          case ENUM:
            schema = readEnum(reader);
            break;
          case ARRAY:
            schema = readArray(reader, knownRecords);
            break;
          case MAP:
            schema = readMap(reader, knownRecords);
            break;
          case RECORD:
            schema = readRecord(reader, knownRecords);
            break;
          default:
            schema = Schema.of(schemaType);
        }
        reader.endObject();
        return schema;
      }
    }
    throw new IOException("Malformed schema input.");
  }

  /**
   * Constructs {@link Schema.Type#UNION UNION} type schema from the json input.
   *
   * @param reader The {@link JsonReader} for streaming json input tokens.
   * @param knownRecords Set of record name already encountered during the reading.
   * @return A {@link Schema} of type {@link Schema.Type#UNION UNION}.
   * @throws IOException When fails to construct a valid schema from the input.
   */
  private Schema readUnion(JsonReader reader, Map<String, Schema> knownRecords) throws IOException {
    List<Schema> unionSchemas = new ArrayList<>();
    reader.beginArray();
    while (reader.peek() != JsonToken.END_ARRAY) {
      unionSchemas.add(read(reader, knownRecords));
    }
    reader.endArray();
    return Schema.unionOf(unionSchemas);
  }

  /**
   * Constructs {@link Schema.Type#ENUM ENUM} type schema from the json input.
   *
   * @param reader The {@link JsonReader} for streaming json input tokens.
   * @return A {@link Schema} of type {@link Schema.Type#ENUM ENUM}.
   * @throws IOException When fails to construct a valid schema from the input.
   */
  private Schema readEnum(JsonReader reader) throws IOException {
    if (!"symbols".equals(reader.nextName())) {
      throw new IOException("Property \"symbols\" missing for enum.");
    }
    List<String> enumValues = new ArrayList<>();
    reader.beginArray();
    while (reader.peek() != JsonToken.END_ARRAY) {
      enumValues.add(reader.nextString());
    }
    reader.endArray();
    return Schema.enumWith(enumValues);
  }

  /**
   * Constructs {@link Schema.Type#ARRAY ARRAY} type schema from the json input.
   *
   * @param reader The {@link JsonReader} for streaming json input tokens.
   * @param knownRecords Set of record name already encountered during the reading.
   * @return A {@link Schema} of type {@link Schema.Type#ARRAY ARRAY}.
   * @throws IOException When fails to construct a valid schema from the input.
   */
  private Schema readArray(JsonReader reader, Map<String, Schema> knownRecords) throws IOException {
    return Schema.arrayOf(readInnerSchema(reader, "items", knownRecords));
  }

  /**
   * Constructs {@link Schema.Type#MAP MAP} type schema from the json input.
   *
   * @param reader The {@link JsonReader} for streaming json input tokens.
   * @param knownRecords Set of record name already encountered during the reading.
   * @return A {@link Schema} of type {@link Schema.Type#MAP MAP}.
   * @throws IOException When fails to construct a valid schema from the input.
   */
  private Schema readMap(JsonReader reader, Map<String, Schema> knownRecords) throws IOException {
    return Schema.mapOf(readInnerSchema(reader, "keys", knownRecords),
                        readInnerSchema(reader, "values", knownRecords));
  }

  /**
   * Constructs {@link Schema.Type#RECORD RECORD} type schema from the json input.
   *
   * @param reader The {@link JsonReader} for streaming json input tokens.
   * @param knownRecords Set of record name already encountered during the reading.
   * @return A {@link Schema} of type {@link Schema.Type#RECORD RECORD}.
   * @throws IOException When fails to construct a valid schema from the input.
   */
  private Schema readRecord(JsonReader reader, Map<String, Schema> knownRecords) throws IOException {
    if (!"name".equals(reader.nextName())) {
      throw new IOException("Property \"name\" missing for record.");
    }

    String recordName = reader.nextString();

    // Read in fields schemas
    if (!"fields".equals(reader.nextName())) {
      throw new IOException("Property \"fields\" missing for record.");
    }

    /*
      put a null schema schema is null and in the map if this is a recursive reference.
      for example, if we are looking at the outer 'node' reference in the example below,
      when we get to the inner 'node' reference, we need some way to know that its a record type
      and not a Schema.Type.
      {
        "type": "record",
        "name": "node",
        "fields": [{
          "name": "children",
          "type": [{
            "type": "array",
            "items": ["node", "null"]
          }, "null"]
        }, {
          "name": "data",
          "type": "int"
        }]
      }
      the full schema will be put in at the end of this method
    */
    knownRecords.put(recordName, null);

    List<Schema.Field> fieldBuilder = new ArrayList<>();
    reader.beginArray();
    while (reader.peek() != JsonToken.END_ARRAY) {
      reader.beginObject();
      if (!"name".equals(reader.nextName())) {
        throw new IOException("Property \"name\" missing for record field.");
      }
      String fieldName = reader.nextString();
      fieldBuilder.add(Schema.Field.of(fieldName, readInnerSchema(reader, "type", knownRecords)));
      reader.endObject();
    }
    reader.endArray();
    Schema schema = Schema.recordOf(recordName, fieldBuilder);
    knownRecords.put(recordName, schema);
    return schema;
  }

  /**
   * Constructs a {@link Schema} from the "key":"schema" pair.
   *
   * @param reader The {@link JsonReader} for streaming json input tokens.
   * @param key The json property name that need to match.
   * @param knownRecords Set of record name already encountered during the reading.
   * @return A {@link Schema} object representing the schema of the json input.
   * @throws IOException When fails to construct a valid schema from the input.
   */
  private Schema readInnerSchema(JsonReader reader, String key, Map<String, Schema> knownRecords) throws IOException {
    if (!key.equals(reader.nextName())) {
      throw new IOException("Property \"" + key + "\" missing.");
    }
    return read(reader, knownRecords);
  }

  /**
   * Writes the given {@link Schema} into json.
   *
   * @param writer A {@link JsonWriter} for emitting json.
   * @param schema The {@link Schema} object to encode to json.
   * @param knownRecords Set of record names that has already been encoded.
   * @return The same {@link JsonWriter} as the one passed in.
   * @throws IOException When fails to encode the schema into json.
   */
  private JsonWriter write(JsonWriter writer, Schema schema, Set<String> knownRecords) throws IOException {
    // Simple type, just emit the type name as a string
    if (schema.getType().isSimpleType()) {
      return writer.value(schema.getType().name().toLowerCase());
    }

    // Union type is an array of schemas
    if (schema.getType() == Schema.Type.UNION) {
      writer.beginArray();
      for (Schema unionSchema : schema.getUnionSchemas()) {
        write(writer, unionSchema, knownRecords);
      }
      return writer.endArray();
    }

    // If it is a record that refers to a previously defined record, just emit the name of it
    if (schema.getType() == Schema.Type.RECORD && knownRecords.contains(schema.getRecordName())) {
      return writer.value(schema.getRecordName());
    }
    // Complex types, represented as an object with "type" property carrying the type name
    writer.beginObject().name("type").value(schema.getType().name().toLowerCase());
    switch (schema.getType()) {
      case ENUM:
        // Emits all enum values as an array, keyed by "symbols"
        writer.name("symbols").beginArray();
        for (String enumValue : schema.getEnumValues()) {
          writer.value(enumValue);
        }
        writer.endArray();
        break;

      case ARRAY:
        // Emits the schema of the array component type, keyed by "items"
        write(writer.name("items"), schema.getComponentSchema(), knownRecords);
        break;

      case MAP:
        // Emits schema of both key and value types, keyed by "keys" and "values" respectively
        Map.Entry<Schema, Schema> mapSchema = schema.getMapSchema();
        write(writer.name("keys"), mapSchema.getKey(), knownRecords);
        write(writer.name("values"), mapSchema.getValue(), knownRecords);
        break;

      case RECORD:
        // Emits the name of record, keyed by "name"
        knownRecords.add(schema.getRecordName());
        writer.name("name").value(schema.getRecordName())
              .name("fields").beginArray();
        // Each field is an object, with field name keyed by "name" and field schema keyed by "type"
        for (Schema.Field field : schema.getFields()) {
          writer.beginObject().name("name").value(field.getName());
          write(writer.name("type"), field.getSchema(), knownRecords);
          writer.endObject();
        }
        writer.endArray();
        break;
    }
    writer.endObject();

    return writer;
  }
}
