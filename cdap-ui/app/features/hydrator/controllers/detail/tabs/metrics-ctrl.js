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

angular.module(PKG.name + '.feature.hydrator')
  .controller('HydratorDetailMetricsController', function(DetailRunsStore, MetricsStore, PipelineDetailMetricsActionFactory, $scope, NodesStore) {

    this.setState = function() {
      this.state = {
        metrics: MetricsStore.getMetrics(),
        nodes: NodesStore.getNodesAsObjects()
      };
    };

    this.setState();

    MetricsStore.registerOnChangeListener(this.setState.bind(this));
  });
