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

package co.cask.cdap.etl.batch;

import co.cask.cdap.api.metrics.Metrics;
import co.cask.cdap.etl.api.StageLifecycle;
import co.cask.cdap.etl.api.Transformation;
import co.cask.cdap.etl.api.batch.BatchRuntimeContext;
import co.cask.cdap.etl.common.DefaultStageMetrics;
import co.cask.cdap.etl.common.PipelinePhase;
import co.cask.cdap.etl.common.TransformDetail;
import co.cask.cdap.etl.common.TransformExecutor;
import co.cask.cdap.etl.planner.StageInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Helps create {@link TransformExecutor TransformExecutors}.
 *
 * @param <T> the type of input for the created transform executors
 */
public abstract class TransformExecutorFactory<T> {
  protected final PipelinePluginInstantiator pluginInstantiator;
  protected final Metrics metrics;

  public TransformExecutorFactory(PipelinePluginInstantiator pluginInstantiator, Metrics metrics) {
    this.pluginInstantiator = pluginInstantiator;
    this.metrics = metrics;
  }

  protected abstract BatchRuntimeContext createRuntimeContext(String stageName);

  protected Transformation getTransformation(String pluginType, String stageName) throws Exception {
    return getInitializedTransformation(stageName);
  }

  /**
   * Create a transform executor for the specified pipeline. Will instantiate and initialize all sources,
   * transforms, and sinks in the pipeline.
   *
   * @param pipeline the pipeline to create a transform executor for
   * @return executor for the pipeline
   * @throws InstantiationException if there was an error instantiating a plugin
   * @throws Exception if there was an error initializing a plugin
   */
  public TransformExecutor<T> create(PipelinePhase pipeline) throws Exception {
    Map<String, TransformDetail> transformations = new HashMap<>();
    for (String pluginType : pipeline.getPluginTypes()) {
      for (StageInfo stageInfo : pipeline.getStagesOfType(pluginType)) {
        String stageName = stageInfo.getName();
        transformations.put(stageName,
                            new TransformDetail(getTransformation(pluginType, stageName),
                                                new DefaultStageMetrics(metrics, stageName),
                                                pipeline.getStageOutputs(stageName)));
      }
    }

    return new TransformExecutor<>(transformations, pipeline.getSources());
  }

  /**
   * Instantiates and initializes the plugin for the stage.
   *
   * @param stageName the stage name.
   * @return the initialized Transformation
   * @throws InstantiationException if the plugin for the stage could not be instantiated
   * @throws Exception if there was a problem initializing the plugin
   */
  protected <T extends Transformation & StageLifecycle<BatchRuntimeContext>> Transformation
  getInitializedTransformation(String stageName) throws Exception {
    BatchRuntimeContext runtimeContext = createRuntimeContext(stageName);
    T plugin = pluginInstantiator.newPluginInstance(stageName);
    plugin.initialize(runtimeContext);
    return plugin;
  }

}
