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
  .controller('HydratorDetailHistoryController', function(DetailRunsStore) {
    var vm = this;

    vm.history = [];

    vm.setState = function() {
      vm.history = DetailRunsStore.getRuns();
      var params = DetailRunsStore.getParams();
      vm.programType = params.programType.toUpperCase();
      vm.programId = params.programName;
      vm.appId = params.app;
    };
    vm.setState();
    DetailRunsStore.registerOnChangeListener(vm.setState);
  });
