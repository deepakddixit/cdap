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

function NavbarController ($scope, $state, myNamespace, EventPipe, MYAUTH_EVENT, myAuth, MY_CONFIG) {
  'ngInject';

  let vm = this;

  function findActiveProduct() {
    if ($state.includes('hydrator.**') || $state.includes('hydratorplusplus.**')) {
      return 'hydrator';
    } else if ($state.includes('tracker.**') || $state.is('tracker-home') || $state.is('tracker-integrations')) {
      return 'tracker';
    } else {
      return 'cdap';
    }
  }
  vm.showSidebar = false;
  vm.toggleSidebar = () => {
    vm.showSidebar = !vm.showSidebar;
  };
  vm.securityEnabled = MY_CONFIG.securityEnabled;

  $scope.$on('$stateChangeSuccess', function(event, toState) {
    vm.highlightTab = toState.data && toState.data.highlightTab;
    vm.activeProduct = findActiveProduct();
    vm.showSidebar = false;
  });


  // NAMESPACE
  vm.namespaces = [];
  function updateNamespaceList() {
    myNamespace.getList()
      .then(function(list) {
        vm.namespaces = list;
      });
  }
  // Listening for event from namespace create or namespace delete
  EventPipe.on('namespace.update', updateNamespaceList);

  vm.currentUser = myAuth.isAuthenticated();
  $scope.$on (MYAUTH_EVENT.loginSuccess, () => {
    vm.currentUser = myAuth.isAuthenticated();
    updateNamespaceList();
  });
  $scope.$on (MYAUTH_EVENT.logoutSuccess, () => {
    vm.currentUser = myAuth.isAuthenticated();
    vm.namespaces = [];
  });

  vm.logout = myAuth.logout.bind(myAuth);
  vm.changeNamespace = (ns) => {
    if ($state.params.namespace === ns.name) { return; }
    if ($state.includes('hydrator.**')) {
      $state.go('hydrator.list', { namespace: ns.name });
    } else if ($state.includes('tracker.**') || $state.is('tracker-home') || $state.is('tracker-integrations')) {
      $state.go('tracker-home', { namespace: ns.name });
    } else if ($state.includes('dashboard.**')){
      $state.go('dashboard.standard.cdap', { namespace: ns.name });
    } else {
      $state.go('overview', { namespace: ns.name });
    }
  };

}


angular.module(PKG.name+'.commons')
  .directive('myGlobalNavbar', () => {
    return {
      restrict: 'E',
      templateUrl: 'my-global-navbar/my-global-navbar.html',
      controller: NavbarController,
      controllerAs: 'Navbar'
    };
  });
