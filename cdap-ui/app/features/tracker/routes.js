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

angular.module(PKG.name + '.feature.tracker')
  .config(function($stateProvider, MYAUTH_ROLE) {

    $stateProvider
      .state('tracker-home', {
        url: '/tracker/home',
        parent: 'ns',
        data: {
          authorizedRoles: MYAUTH_ROLE.all,
          highlightTab: 'search'
        },
        templateUrl: '/assets/features/tracker/templates/main.html',
        controller: 'TrackerMainController',
        controllerAs: 'MainController'
      })

      .state('tracker-integrations', {
        url: '/tracker/integrations',
        parent: 'ns',
        templateUrl: '/assets/features/tracker/templates/integrations.html',
        controller: 'TrackerIntegrationsController',
        controllerAs: 'IntegrationsController',
        data: {
          authorizedRoles: MYAUTH_ROLE.all,
          highlightTab: 'integrations'
        }
      })

      .state('tracker', {
        url: '/tracker',
        parent: 'ns',
        data: {
          authorizedRoles: MYAUTH_ROLE.all,
          highlightTab: 'search'
        },
        templateUrl: '/assets/features/tracker/templates/container.html',
        controller: 'TrackerContainerController',
        controllerAs: 'ContainerController'
      })

        .state('tracker.result', {
          url: '/search/:searchQuery/result',
          templateUrl: '/assets/features/tracker/templates/results.html',
          controller: 'TrackerResultsController',
          controllerAs: 'ResultsController',
          data: {
            authorizedRoles: MYAUTH_ROLE.all,
            highlightTab: 'search'
          }
        })

        .state('tracker.entity', {
          url: '/entity/:entityType/:entityId?searchTerm',
          templateUrl: '/assets/features/tracker/templates/entity.html',
          controller: 'TrackerEntityController',
          controllerAs: 'EntityController',
          data: {
            authorizedRoles: MYAUTH_ROLE.all,
            highlightTab: 'search'
          },
          resolve: {
            rDatasetType: function ($q, myTrackerApi, $stateParams) {
              if ($stateParams.entityType !== 'datasets') {
                return null;
              }

              let defer = $q.defer();

              let params = {
                namespace: $stateParams.namespace,
                entityType: $stateParams.entityType,
                entityId: $stateParams.entityId
              };
              myTrackerApi.getDatasetSystemProperties(params)
                .$promise
                .then( (res) => {
                  defer.resolve(res.type);
                }, () => {
                  defer.reject();
                });

              return defer.promise;
            }
          }
        })
          .state('tracker.entity.metadata', {
            url: '/metadata',
            templateUrl: '/assets/features/tracker/templates/metadata.html',
            controller: 'TrackerMetadataController',
            controllerAs: 'MetadataController',
            data: {
              authorizedRoles: MYAUTH_ROLE.all,
              highlightTab: 'search'
            }
          })
          .state('tracker.entity.lineage', {
            url: '/lineage?start&end&method',
            templateUrl: '/assets/features/tracker/templates/lineage.html',
            controller: 'TrackerLineageController',
            controllerAs: 'LineageController',
            data: {
              authorizedRoles: MYAUTH_ROLE.all,
              highlightTab: 'search'
            }
          })
          .state('tracker.entity.audit', {
            url: '/audit?start&end',
            templateUrl: '/assets/features/tracker/templates/audit.html',
            controller: 'TrackerAuditController',
            controllerAs: 'AuditController',
            data: {
              authorizedRoles: MYAUTH_ROLE.all,
              highlightTab: 'search'
            }
          });
  });
