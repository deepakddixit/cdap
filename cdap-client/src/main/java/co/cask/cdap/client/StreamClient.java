/*
 * Copyright © 2014-2015 Cask Data, Inc.
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

package co.cask.cdap.client;

import co.cask.cdap.api.annotation.Beta;
import co.cask.cdap.api.data.schema.Schema;
import co.cask.cdap.api.flow.flowlet.StreamEvent;
import co.cask.cdap.client.config.ClientConfig;
import co.cask.cdap.client.util.RESTClient;
import co.cask.cdap.common.BadRequestException;
import co.cask.cdap.common.StreamNotFoundException;
import co.cask.cdap.common.UnauthenticatedException;
import co.cask.cdap.common.stream.StreamEventTypeAdapter;
import co.cask.cdap.common.utils.TimeMathParser;
import co.cask.cdap.internal.io.SchemaTypeAdapter;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.StreamDetail;
import co.cask.cdap.proto.StreamProperties;
import co.cask.cdap.proto.StreamRecord;
import co.cask.cdap.security.authentication.client.AccessToken;
import co.cask.common.http.HttpMethod;
import co.cask.common.http.HttpRequest;
import co.cask.common.http.HttpRequests;
import co.cask.common.http.HttpResponse;
import co.cask.common.http.ObjectResponse;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.core.HttpHeaders;

/**
 * Provides ways to interact with CDAP Streams.
 */
@Beta
public class StreamClient {

  private static final Gson GSON = StreamEventTypeAdapter.register(
    new GsonBuilder().registerTypeAdapter(Schema.class, new SchemaTypeAdapter())).create();

  private final RESTClient restClient;
  private final ClientConfig config;

  @Inject
  public StreamClient(ClientConfig config, RESTClient restClient) {
    this.config = config;
    this.restClient = restClient;
  }

  public StreamClient(ClientConfig config) {
    this(config, new RESTClient(config));
  }

  /**
   * Gets the configuration of a stream.
   *
   * @param stream ID of the stream
   * @throws IOException if a network error occurred
   * @throws StreamNotFoundException if the stream was not found
   */
  public StreamProperties getConfig(Id.Stream stream)
    throws IOException, StreamNotFoundException, UnauthenticatedException {

    URL url = config.resolveNamespacedURLV3(stream.getNamespace(), String.format("streams/%s", stream.getId()));
    HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken(),
                                               HttpURLConnection.HTTP_NOT_FOUND);
    if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new StreamNotFoundException(stream);
    }
    return GSON.fromJson(response.getResponseBodyAsString(Charsets.UTF_8), StreamProperties.class);
  }

  /**
   * Sets properties of a stream.
   *
   * @param stream ID of the stream
   * @param properties properties to set
   * @throws IOException if a network error occurred
   * @throws UnauthenticatedException if the client is unauthorized
   * @throws BadRequestException if the request is bad
   * @throws StreamNotFoundException if the stream was not found
   */
  public void setStreamProperties(Id.Stream stream, StreamProperties properties)
    throws IOException, UnauthenticatedException, BadRequestException, StreamNotFoundException {

    URL url = config.resolveNamespacedURLV3(stream.getNamespace(),
                                            String.format("streams/%s/properties", stream.getId()));

    HttpRequest request = HttpRequest.put(url).withBody(GSON.toJson(properties)).build();
    HttpResponse response = restClient.execute(request, config.getAccessToken(),
                                               HttpURLConnection.HTTP_NOT_FOUND,
                                               HttpURLConnection.HTTP_BAD_REQUEST);
    if (response.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
      throw new BadRequestException("Bad request: " + response.getResponseBodyAsString());
    }
    if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new StreamNotFoundException(stream);
    }
  }

  /**
   * Creates a stream.
   *
   * @param newStreamId ID of the new stream to create
   * @throws IOException if a network error occurred
   * @throws BadRequestException if the provided stream ID was invalid
   */
  public void create(Id.Stream newStreamId)
    throws IOException, BadRequestException, UnauthenticatedException {

    URL url = config.resolveNamespacedURLV3(newStreamId.getNamespace(),
                                            String.format("streams/%s", newStreamId.getId()));
    HttpResponse response = restClient.execute(HttpMethod.PUT, url, config.getAccessToken(),
                                               HttpURLConnection.HTTP_BAD_REQUEST);
    if (response.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
      throw new BadRequestException("Bad request: " + response.getResponseBodyAsString());
    }
  }

  /**
   * Sends an event to a stream.
   *
   * @param stream ID of the stream
   * @param event event to send to the stream
   * @throws IOException if a network error occurred
   * @throws StreamNotFoundException if the stream with the specified ID was not found
   */
  public void sendEvent(Id.Stream stream, String event) throws IOException,
                                                              StreamNotFoundException, UnauthenticatedException {
    URL url = config.resolveNamespacedURLV3(stream.getNamespace(), String.format("streams/%s", stream.getId()));
    writeEvent(url, stream, event);
  }

  /**
   * Sends an event to a stream. The write is asynchronous, meaning when this method returns, it only guarantees
   * the event has been received by the server, but may not get persisted.
   *
   * @param stream ID of the stream
   * @param event event to send to the stream
   * @throws IOException if a network error occurred
   * @throws StreamNotFoundException if the stream with the specified ID was not found
   */
  public void asyncSendEvent(Id.Stream stream, String event) throws IOException,
                                                                   StreamNotFoundException, UnauthenticatedException {
    URL url = config.resolveNamespacedURLV3(stream.getNamespace(), String.format("streams/%s/async", stream.getId()));
    writeEvent(url, stream, event);
  }

  /**
   * Sends a file of the given content type to a stream batch endpoint.
   *
   * @param stream ID of the stream
   * @param contentType content type of the file
   * @param file the file to upload
   * @throws IOException if a network error occurred
   * @throws StreamNotFoundException if the stream with the specified ID was not found
   */
  public void sendFile(Id.Stream stream, String contentType,
                       File file) throws IOException, StreamNotFoundException, UnauthenticatedException {
    sendBatch(stream, contentType, Files.newInputStreamSupplier(file));
  }

  /**
   * Sends a batch request to a stream batch endpoint.
   *
   * @param stream ID of the stream
   * @param contentType content type of the data
   * @param inputSupplier provides content for the batch request
   * @throws IOException if a network error occurred
   * @throws StreamNotFoundException if the stream with the specified ID was not found
   */
  public void sendBatch(Id.Stream stream, String contentType,
                        InputSupplier<? extends InputStream> inputSupplier)
    throws IOException, StreamNotFoundException, UnauthenticatedException {

    URL url = config.resolveNamespacedURLV3(stream.getNamespace(), String.format("streams/%s/batch", stream.getId()));
    Map<String, String> headers = ImmutableMap.of("Content-type", contentType);
    HttpRequest request = HttpRequest.post(url).addHeaders(headers).withBody(inputSupplier).build();

    HttpResponse response = restClient.upload(request, config.getAccessToken(), HttpURLConnection.HTTP_NOT_FOUND);
    if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new StreamNotFoundException(stream);
    }
  }

  /**
   * Truncates a stream, deleting all stream events belonging to the stream.
   *
   * @param stream ID of the stream to truncate
   * @throws IOException if a network error occurred
   * @throws StreamNotFoundException if the stream with the specified name was not found
   */
  public void truncate(Id.Stream stream) throws IOException, StreamNotFoundException, UnauthenticatedException {
    URL url = config.resolveNamespacedURLV3(stream.getNamespace(),
                                            String.format("streams/%s/truncate", stream.getId()));
    HttpResponse response = restClient.execute(HttpMethod.POST, url, config.getAccessToken(),
                                               HttpURLConnection.HTTP_NOT_FOUND);
    if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new StreamNotFoundException(stream);
    }
  }

  /**
   * Deletes a stream.
   *
   * @param stream ID of the stream to truncate
   * @throws IOException if a network error occurred
   * @throws StreamNotFoundException if the stream with the specified name was not found
   */
  public void delete(Id.Stream stream) throws IOException, StreamNotFoundException, UnauthenticatedException {
    URL url = config.resolveNamespacedURLV3(stream.getNamespace(), String.format("streams/%s", stream.getId()));
    HttpResponse response = restClient.execute(HttpMethod.DELETE, url, config.getAccessToken(),
                                               HttpURLConnection.HTTP_NOT_FOUND);
    if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new StreamNotFoundException(stream);
    }
  }

  /**
   * Sets the Time-to-Live (TTL) of a stream. TTL governs how long stream events are readable.
   *
   * @param stream ID of the stream
   * @param ttlInSeconds desired TTL, in seconds
   * @throws IOException if a network error occurred
   * @throws StreamNotFoundException if the stream with the specified name was not found
   */
  public void setTTL(Id.Stream stream, long ttlInSeconds)
    throws IOException, StreamNotFoundException, UnauthenticatedException {

    URL url = config.resolveNamespacedURLV3(stream.getNamespace(),
                                            String.format("streams/%s/properties", stream.getId()));
    HttpRequest request = HttpRequest.put(url).withBody(GSON.toJson(ImmutableMap.of("ttl", ttlInSeconds))).build();

    HttpResponse response = restClient.execute(request, config.getAccessToken(), HttpURLConnection.HTTP_NOT_FOUND);
    if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new StreamNotFoundException(stream);
    }
  }

  /**
   * Lists all streams.
   *
   * @return list of {@link StreamRecord}s
   * @throws IOException if a network error occurred
   */
  public List<StreamDetail> list(Id.Namespace namespace) throws IOException, UnauthenticatedException {
    URL url = config.resolveNamespacedURLV3(namespace, "streams");
    HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken());
    return ObjectResponse.fromJsonBody(response, new TypeToken<List<StreamDetail>>() {
    }).getResponseObject();
  }
  /**
   * Reads events from a stream
   *
   * @param streamId ID of the stream
   * @param startTime Timestamp in milliseconds to start reading event from (inclusive)
   * @param endTime Timestamp in milliseconds for the last event to read (exclusive)
   * @param limit Maximum number of events to read
   * @param results Collection for storing the resulting stream events
   * @param <T> Type of the collection for storing results
   * @return The same collection object as passed in the {@code results} parameter
   * @throws IOException If fails to read from stream
   * @throws StreamNotFoundException If the given stream does not exists
   */
  public <T extends Collection<? super StreamEvent>> T getEvents(Id.Stream streamId, String startTime,
                                                                 String endTime, int limit, final T results)
    throws IOException, StreamNotFoundException, UnauthenticatedException {
    getEvents(streamId, startTime, endTime, limit, new Function<StreamEvent, Boolean>() {
      @Override
      public Boolean apply(StreamEvent input) {
        results.add(input);
        return true;
      }
    });
    return results;
  }

  /**
   * Reads events from a stream
   *
   * @param streamId ID of the stream
   * @param startTime Timestamp in milliseconds to start reading event from (inclusive)
   * @param endTime Timestamp in milliseconds for the last event to read (exclusive)
   * @param limit Maximum number of events to read
   * @param results Collection for storing the resulting stream events
   * @param <T> Type of the collection for storing results
   * @return The same collection object as passed in the {@code results} parameter
   * @throws IOException If fails to read from stream
   * @throws StreamNotFoundException If the given stream does not exists
   */
  public <T extends Collection<? super StreamEvent>> T getEvents(Id.Stream streamId, long startTime,
                                                                 long endTime, int limit, final T results)
    throws IOException, StreamNotFoundException, UnauthenticatedException {
    return getEvents(streamId, String.valueOf(startTime), String.valueOf(endTime), limit, results);
  }

  /**
   * Reads events from a stream
   *
   * @param streamId ID of the stream
   * @param startTime Timestamp in milliseconds to start reading event from (inclusive)
   * @param endTime Timestamp in milliseconds for the last event to read (exclusive)
   * @param limit Maximum number of events to read
   * @param callback Callback to invoke for each stream event read. If the callback function returns {@code false}
   *                  upon invocation, it will stops the reading
   * @throws IOException If fails to read from stream
   * @throws StreamNotFoundException If the given stream does not exists
   */
  public void getEvents(Id.Stream streamId, long startTime, long endTime, int limit,
                        Function<? super StreamEvent, Boolean> callback)
    throws IOException, StreamNotFoundException, UnauthenticatedException {

    getEvents(streamId, String.valueOf(startTime), String.valueOf(endTime), limit, callback);
  }

  /**
   * Reads events from a stream
   *
   * @param streamId ID of the stream
   * @param start Timestamp in milliseconds or now-xs format to start reading event from (inclusive)
   * @param end Timestamp in milliseconds or now-xs format for the last event to read (exclusive)
   * @param limit Maximum number of events to read
   * @param callback Callback to invoke for each stream event read. If the callback function returns {@code false}
   *                 upon invocation, it will stops the reading
   * @throws IOException If fails to read from stream
   * @throws StreamNotFoundException If the given stream does not exists
   */
  public void getEvents(Id.Stream streamId, String start, String end, int limit,
                        Function<? super StreamEvent, Boolean> callback)
    throws IOException, StreamNotFoundException, UnauthenticatedException {

    long startTime = TimeMathParser.parseTime(start, TimeUnit.MILLISECONDS);
    long endTime = TimeMathParser.parseTime(end, TimeUnit.MILLISECONDS);

    URL url = config.resolveNamespacedURLV3(streamId.getNamespace(),
                                            String.format("streams/%s/events?start=%d&end=%d&limit=%d",
                                                          streamId.getId(), startTime, endTime, limit));
    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
    AccessToken accessToken = config.getAccessToken();
    if (accessToken != null) {
      urlConn.setRequestProperty(HttpHeaders.AUTHORIZATION, accessToken.getTokenType() + " " + accessToken.getValue());
    }

    if (urlConn instanceof HttpsURLConnection && !config.isVerifySSLCert()) {
      try {
        HttpRequests.disableCertCheck((HttpsURLConnection) urlConn);
      } catch (Exception e) {
        // TODO: Log "Got exception while disabling SSL certificate check for request.getURL()"
      }
    }

    try {
      if (urlConn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
        throw new UnauthenticatedException("Unauthorized status code received from the server.");
      }
      if (urlConn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
        throw new StreamNotFoundException(streamId);
      }
      if (urlConn.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
        return;
      }

      // The response is an array of stream event object
      InputStream inputStream = urlConn.getInputStream();
      JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream, Charsets.UTF_8));
      jsonReader.beginArray();
      while (jsonReader.peek() != JsonToken.END_ARRAY) {
        Boolean result = callback.apply(GSON.<StreamEvent>fromJson(jsonReader, StreamEvent.class));
        if (result == null || !result) {
          break;
        }
      }
      drain(inputStream);
      // No need to close reader, the urlConn.disconnect in finally will close all underlying streams
    } finally {
      urlConn.disconnect();
    }
  }

  /**
   * Writes stream event using the given URL. The write maybe sync or async, depending on the URL.
   */
  private void writeEvent(URL url, Id.Stream stream, String event)
    throws IOException, StreamNotFoundException, UnauthenticatedException {

    HttpRequest request = HttpRequest.post(url).withBody(event).build();
    HttpResponse response = restClient.execute(request, config.getAccessToken(), HttpURLConnection.HTTP_NOT_FOUND);
    if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new StreamNotFoundException(stream);
    }
  }

  @SuppressWarnings("StatementWithEmptyBody")
  private void drain(InputStream input) throws IOException {
    while (input.skip(Long.MAX_VALUE) > 0) {
      // empty
    }
  }
}
