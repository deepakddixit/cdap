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

package co.cask.cdap.etl.batch.spark;

import co.cask.cdap.api.spark.SparkClientContext;
import co.cask.cdap.etl.api.LookupProvider;
import co.cask.cdap.etl.api.batch.SparkPluginContext;
import co.cask.cdap.etl.batch.AbstractBatchContext;
import org.apache.spark.SparkConf;

/**
 * Implementation of SparkPluginContext that delegates to a SparkContext.
 */
public class BasicSparkPluginContext extends AbstractBatchContext implements SparkPluginContext {

  private final SparkClientContext sparkContext;

  public BasicSparkPluginContext(SparkClientContext sparkContext, LookupProvider lookupProvider, String stageId) {
    super(sparkContext.getPluginContext(), sparkContext, sparkContext.getMetrics(), lookupProvider, stageId,
          sparkContext.getLogicalStartTime(), sparkContext.getRuntimeArguments());
    this.sparkContext = sparkContext;
  }

  @Override
  public void setSparkConf(SparkConf sparkConf) {
    sparkContext.setSparkConf(sparkConf);
  }

  @Override
  public <T> T getHadoopJob() {
    throw new UnsupportedOperationException("Hadoop Job is not available in Spark");
  }
}
