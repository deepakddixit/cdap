/*
 * Copyright © 2016 Cask Data, Inc.
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

package co.cask.cdap.etl.spec;

import co.cask.cdap.api.artifact.ArtifactId;
import co.cask.cdap.api.artifact.ArtifactScope;
import co.cask.cdap.api.artifact.ArtifactVersion;
import co.cask.cdap.api.data.schema.Schema;
import co.cask.cdap.api.dataset.DatasetProperties;
import co.cask.cdap.api.dataset.lib.FileSet;
import co.cask.cdap.etl.api.PipelineConfigurable;
import co.cask.cdap.etl.api.PipelineConfigurer;
import co.cask.cdap.etl.api.Transform;
import co.cask.cdap.etl.api.batch.BatchSink;
import co.cask.cdap.etl.api.batch.BatchSource;
import co.cask.cdap.etl.common.MockPluginConfigurer;
import co.cask.cdap.etl.proto.v2.ETLBatchConfig;
import co.cask.cdap.etl.proto.v2.ETLConfig;
import co.cask.cdap.etl.proto.v2.ETLPlugin;
import co.cask.cdap.etl.proto.v2.ETLStage;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

/**
 * Tests for converting a {@link ETLConfig} into a {@link PipelineSpec}.
 */
public class PipelineSpecGeneratorTest {
  private static final Schema SCHEMA_A = Schema.recordOf("a", Schema.Field.of("a", Schema.of(Schema.Type.STRING)));
  private static final Schema SCHEMA_B = Schema.recordOf("b", Schema.Field.of("b", Schema.of(Schema.Type.STRING)));
  private static final ETLPlugin MOCK_SOURCE =
    new ETLPlugin("mocksource", BatchSource.PLUGIN_TYPE, ImmutableMap.<String, String>of(), null);
  private static final ETLPlugin MOCK_TRANSFORM_A =
    new ETLPlugin("mockA", Transform.PLUGIN_TYPE, ImmutableMap.<String, String>of(), null);
  private static final ETLPlugin MOCK_TRANSFORM_B =
    new ETLPlugin("mockB", Transform.PLUGIN_TYPE, ImmutableMap.<String, String>of(), null);
  private static final ETLPlugin MOCK_SINK =
    new ETLPlugin("mocksink", BatchSink.PLUGIN_TYPE, ImmutableMap.<String, String>of(), null);
  private static final ArtifactId ARTIFACT_ID =
    new ArtifactId("plugins", new ArtifactVersion("1.0.0"), ArtifactScope.USER);
  private static PipelineSpecGenerator specGenerator;

  @BeforeClass
  public static void setupTests() {
    // populate some mock plugins.
    MockPluginConfigurer pluginConfigurer = new MockPluginConfigurer();
    Set<ArtifactId> artifactIds = ImmutableSet.of(ARTIFACT_ID);
    pluginConfigurer.addMockPlugin(BatchSource.PLUGIN_TYPE, "mocksource", new MockPlugin(SCHEMA_A), artifactIds);
    pluginConfigurer.addMockPlugin(Transform.PLUGIN_TYPE, "mockA", new MockPlugin(SCHEMA_A), artifactIds);
    pluginConfigurer.addMockPlugin(Transform.PLUGIN_TYPE, "mockB", new MockPlugin(SCHEMA_B), artifactIds);
    pluginConfigurer.addMockPlugin(BatchSink.PLUGIN_TYPE, "mocksink", new MockPlugin(), artifactIds);

    specGenerator = new PipelineSpecGenerator(pluginConfigurer,
                                              ImmutableSet.of(BatchSource.PLUGIN_TYPE),
                                              ImmutableSet.of(BatchSink.PLUGIN_TYPE),
                                              FileSet.class, DatasetProperties.EMPTY);
  }


  @Test(expected = IllegalArgumentException.class)
  public void testUniqueStageNames() {
    ETLBatchConfig etlConfig = ETLBatchConfig.builder("* * * * *")
      .addStage(new ETLStage("source", MOCK_SOURCE))
      .addStage(new ETLStage("t1", MOCK_TRANSFORM_A))
      .addStage(new ETLStage("t1", MOCK_TRANSFORM_B))
      .addStage(new ETLStage("sink", MOCK_SINK))
      .addConnection("source", "sink")
      .build();
    specGenerator.generateSpec(etlConfig);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConnectionWithMissingStage() {
    ETLBatchConfig etlConfig = ETLBatchConfig.builder("* * * * *")
      .addStage(new ETLStage("source", MOCK_SOURCE))
      .addStage(new ETLStage("sink", MOCK_SINK))
      .addConnection("source", "sink")
      .addConnection("source", "stage2")
      .build();
    specGenerator.generateSpec(etlConfig);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConnectionIntoSource() {
    ETLBatchConfig etlConfig = ETLBatchConfig.builder("* * * * *")
      .addStage(new ETLStage("source", MOCK_SOURCE))
      .addStage(new ETLStage("sink", MOCK_SINK))
      .addStage(new ETLStage("transform", MOCK_TRANSFORM_A))
      .addConnection("source", "sink")
      .addConnection("transform", "source")
      .build();
    specGenerator.generateSpec(etlConfig);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConnectionOutOfSink() {
    ETLBatchConfig etlConfig = ETLBatchConfig.builder("* * * * *")
      .addStage(new ETLStage("source", MOCK_SOURCE))
      .addStage(new ETLStage("sink", MOCK_SINK))
      .addStage(new ETLStage("transform", MOCK_TRANSFORM_A))
      .addConnection("source", "sink")
      .addConnection("sink", "transform")
      .build();
    specGenerator.generateSpec(etlConfig);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnreachableStage() {
    ETLBatchConfig etlConfig = ETLBatchConfig.builder("* * * * *")
      .addStage(new ETLStage("source", MOCK_SOURCE))
      .addStage(new ETLStage("sink", MOCK_SINK))
      .addStage(new ETLStage("transform", MOCK_TRANSFORM_A))
      .addConnection("source", "sink")
      .addConnection("transform", "sink")
      .build();
    specGenerator.generateSpec(etlConfig);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeadEndStage() {
    ETLBatchConfig etlConfig = ETLBatchConfig.builder("* * * * *")
      .addStage(new ETLStage("source", MOCK_SOURCE))
      .addStage(new ETLStage("sink", MOCK_SINK))
      .addStage(new ETLStage("transform", MOCK_TRANSFORM_A))
      .addConnection("source", "sink")
      .addConnection("source", "transform")
      .build();
    specGenerator.generateSpec(etlConfig);
  }

  @Test(expected = IllegalStateException.class)
  public void testCycle() {
    ETLBatchConfig etlConfig = ETLBatchConfig.builder("* * * * *")
      .addStage(new ETLStage("source", MOCK_SOURCE))
      .addStage(new ETLStage("sink", MOCK_SINK))
      .addStage(new ETLStage("t1", MOCK_TRANSFORM_A))
      .addStage(new ETLStage("t2", MOCK_TRANSFORM_A))
      .addConnection("source", "t1")
      .addConnection("t1", "t2")
      .addConnection("t2", "t1")
      .addConnection("t2", "sink")
      .build();
    specGenerator.generateSpec(etlConfig);
  }

  @Test
  public void testGenerateSpec() {
    /*
     *           ---- t1 ------------
     *           |            |      |
     * source ---             |      |--- t3 --- sink1
     *           |            |      |
     *           ------------ t2 --------------- sink2
     *           |                        |
     *           |                        |
     *           -------------------------
     */
    ETLBatchConfig etlConfig = ETLBatchConfig.builder("* * * * *")
      .addStage(new ETLStage("source", MOCK_SOURCE))
      .addStage(new ETLStage("sink1", MOCK_SINK))
      .addStage(new ETLStage("sink2", MOCK_SINK))
      .addStage(new ETLStage("t1", MOCK_TRANSFORM_A))
      .addStage(new ETLStage("t2", MOCK_TRANSFORM_A))
      .addStage(new ETLStage("t3", MOCK_TRANSFORM_B))
      .addConnection("source", "t1")
      .addConnection("source", "t2")
      .addConnection("source", "sink2")
      .addConnection("t1", "t2")
      .addConnection("t1", "t3")
      .addConnection("t1", "sink2")
      .addConnection("t2", "sink2")
      .addConnection("t2", "t3")
      .addConnection("t3", "sink1")
      .build();
    // test the spec generated is correct, with the right input and output schemas and artifact information.
    PipelineSpec actual = specGenerator.generateSpec(etlConfig);
    Map<String, String> emptyMap = ImmutableMap.of();
    Set<StageSpec> expectedStages = ImmutableSet.of(
      StageSpec.builder("source", new PluginSpec(BatchSource.PLUGIN_TYPE, "mocksource", emptyMap, ARTIFACT_ID))
        .setOutputSchema(SCHEMA_A)
        .addOutputs("t1", "t2", "sink2")
        .build(),
      StageSpec.builder("sink1", new PluginSpec(BatchSink.PLUGIN_TYPE, "mocksink", emptyMap, ARTIFACT_ID))
        .setInputSchema(SCHEMA_B)
        .addInputs("t3")
        .build(),
      StageSpec.builder("sink2", new PluginSpec(BatchSink.PLUGIN_TYPE, "mocksink", emptyMap, ARTIFACT_ID))
        .setInputSchema(SCHEMA_A)
        .addInputs("t1", "t2", "source")
        .build(),
      StageSpec.builder("t1", new PluginSpec(Transform.PLUGIN_TYPE, "mockA", emptyMap, ARTIFACT_ID))
        .setInputSchema(SCHEMA_A)
        .setOutputSchema(SCHEMA_A)
        .addInputs("source")
        .addOutputs("t2", "t3", "sink2")
        .build(),
      StageSpec.builder("t2", new PluginSpec(Transform.PLUGIN_TYPE, "mockA", emptyMap, ARTIFACT_ID))
        .setInputSchema(SCHEMA_A)
        .setOutputSchema(SCHEMA_A)
        .addInputs("source", "t1")
        .addOutputs("t3", "sink2")
        .build(),
      StageSpec.builder("t3", new PluginSpec(Transform.PLUGIN_TYPE, "mockB", emptyMap, ARTIFACT_ID))
        .setInputSchema(SCHEMA_A)
        .setOutputSchema(SCHEMA_B)
        .addInputs("t1", "t2")
        .addOutputs("sink1")
        .build()
    );
    PipelineSpec expected = new PipelineSpec(expectedStages, ImmutableSet.copyOf(etlConfig.getConnections()),
                                             etlConfig.getResources(),
                                             etlConfig.isStageLoggingEnabled());
    Assert.assertEquals(expected, actual);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConflictingInputSchemas() {
    /*
     *           ---- transformA ----
     *           |                  |
     * source ---                   |--- sink
     *           |                  |
     *           ---- transformB ----
     *
     * sink gets schema A and schema B as input, should fail
     */
    ETLBatchConfig etlConfig = ETLBatchConfig.builder("* * * * *")
      .addStage(new ETLStage("source", MOCK_SOURCE))
      .addStage(new ETLStage("sink", MOCK_SINK))
      .addStage(new ETLStage("tA", MOCK_TRANSFORM_A))
      .addStage(new ETLStage("tB", MOCK_TRANSFORM_B))
      .addConnection("source", "tA")
      .addConnection("source", "tB")
      .addConnection("tA", "sink")
      .addConnection("tB", "sink")
      .build();
    specGenerator.generateSpec(etlConfig);
  }

  private static class MockPlugin implements PipelineConfigurable {
    private final Schema schema;

    public MockPlugin() {
      this.schema = null;
    }

    public MockPlugin(Schema schema) {
      this.schema = schema;
    }

    @Override
    public void configurePipeline(PipelineConfigurer pipelineConfigurer) throws IllegalArgumentException {
      if (schema != null) {
        pipelineConfigurer.getStageConfigurer().setOutputSchema(schema);
      }
    }
  }
}
