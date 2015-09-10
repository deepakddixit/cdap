/*
 * Copyright © 2015 Cask Data, Inc.
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

package co.cask.cdap.template.etl.batch;

import co.cask.cdap.api.data.batch.OutputFormatProvider;
import co.cask.cdap.api.dataset.Dataset;
import co.cask.cdap.api.mapreduce.MapReduceContext;
import co.cask.cdap.api.metrics.Metrics;
import co.cask.cdap.template.etl.api.batch.BatchSinkContext;

import java.util.Map;

/**
 * MapReduce Sink Context.
 */
public class MapReduceSinkContext extends MapReduceBatchContext implements BatchSinkContext {

  public MapReduceSinkContext(MapReduceContext context, Metrics metrics, String prefixId) {
    super(context, metrics, prefixId);
  }

  @Override
  public void addOutput(String datasetName) {
    mrContext.addOutput(datasetName);
  }

  @Override
  public void addOutput(String datasetName, Map<String, String> arguments) {
    mrContext.addOutput(datasetName, arguments);
  }

  @Override
  public void addOutput(String outputName, OutputFormatProvider outputFormatProvider) {
    mrContext.addOutput(outputName, outputFormatProvider);
  }
}