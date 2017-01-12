// Translation Table directive
// e.g. <div translation-table value='PUBLISHED'></div>
tsApp
  .directive(
    'translationTable',
    [
      '$uibModal',
      '$window',
      '$route',
      '$routeParams',
      '$sce',
      '$interval',
      'utilService',
      'securityService',
      'projectService',
      'translationService',
      'refsetService',
      'releaseService',
      'workflowService',
      'validationService',
      function($uibModal, $window, $route, $routeParams, $sce, $interval, utilService,
        securityService, projectService, translationService, refsetService, releaseService,
        workflowService, validationService) {
        console.debug('configure translationTable directive');
        return {
          restrict : 'A',
          scope : {
            // Legal 'value' settings include
            // For directory tab: PUBLISHED, BETA
            // For refset tab: EDITING, RELEASE
            value : '@',
            projects : '=',
            metadata : '=',
            stats : '='
          },
          templateUrl : 'app/component/translationTable/translationTable.html',
          controller : [
            '$scope',
            function($scope) {

              // Variables
              $scope.user = securityService.getUser();
              $scope.userProjectsInfo = projectService.getUserProjectsInfo();
              $scope.selected = {
                concept : null,
                translation : null,
                descriptionTypes : [],
                terminology : null,
                version : null,
              };
              $scope.translations = null;
              $scope.translationLookupProgress = {};
              $scope.lookupInterval = null;
              $scope.translationReleaseInfo = null;
              $scope.project = null;
              $scope.refsets = [];
              $scope.filters = [];
              $scope.showLatest = true;
              $scope.withNotesOnly = false;

              // Used for project admin to know what users are assigned to
              // something.
              $scope.conceptIdToAuthorsMap = {};
              $scope.conceptIdToReviewersMap = {};

              // Paging variables
              $scope.paging = {};
              $scope.paging['translation'] = {
                page : 1,
                filter : $routeParams.translationId ? 'id:' + $routeParams.translationId : '',
                sortField : 'name',
                ascending : null,
                pageSize : 10
              };
              $scope.paging['concept'] = {
                page : 1,
                filter : '',
                sortField : $scope.value == 'PUBLISHED' || $scope.value == 'BETA' ? 'name'
                  : 'lastModified',
                ascending : null,
                pageSize : 10

              };
              $scope.paging['available'] = {
                page : 1,
                filter : '',
                sortField : 'lastModified',
                ascending : null,
                pageSize : 10

              };
              $scope.paging['assigned'] = {
                page : 1,
                filter : '',
                sortField : 'conceptTerminologyId',
                ascending : null,
                pageSize : 10

              };

              $scope.ioImportHandlers = [];
              $scope.ioExportHandlers = [];

              // Translation Changed handler
              $scope.$on('refset:translationChanged', function(event, data) {
                console.debug('on refset:translationChanged', data);
                // If the translation is set, refresh translation list
                // This also reselects the translation
                $scope.getTranslations();
              });

              // Concept Changed handler
              $scope.$on('refset:conceptChanged', function(event, data) {
                console.debug('on refset:conceptChanged', data, $scope.selected.translation);
                // If the translation is set, refresh available/assigned lists
                if ($scope.selected.translation) {
                  $scope.selectTranslation($scope.selected.translation);
                }
              });

              // Project Changed Handler
              $scope.$on('refset:projectChanged', function(event, data) {
                console.debug('on refset:projectChanged', data);
                // Set project, refresh translation list
                $scope.setProject(data);
                $scope.getFilters();
              });

              // link to error handling
              function handleError(errors, error) {
                utilService.handleDialogError(errors, error);
              }

              // Indicates whether we are in a directory page section
              var valueFlag = ($scope.value == 'PUBLISHED' || $scope.value == 'BETA');
              $scope.isDirectory = function() {
                return valueFlag;
              };

              // Set $scope.project and reload
              // $scope.refsets
              $scope.setProject = function(project) {
                $scope.project = project;
                $scope.getRefsets();
                $scope.getTranslations();
              };

              // Get $scope.refsets
              $scope.getRefsets = function() {
                var pfs = {
                  startIndex : -1,
                  maxResults : 10,
                  sortField : null,
                  queryRestriction : null
                };
                // Get refsets for project - but not published or beta
                var query = ' AND NOT workflowStatus:PUBLISHED AND NOT workflowStatus:BETA';
                if ($scope.isDirectory()) {
                  query = ' AND (workflowStatus:PUBLISHED OR workflowStatus:BETA)';
                }

                refsetService.findRefsetsForQuery('projectId:' + $scope.project.id + query, pfs)
                  .then(function(data) {
                    $scope.refsets = data.refsets;
                  });
              };

              // Get $scope.translations
              $scope.getTranslations = function() {
                var pfs = {
                  startIndex : ($scope.paging['translation'].page - 1)
                    * $scope.paging['translation'].pageSize,
                  maxResults : $scope.paging['translation'].pageSize,
                  sortField : $scope.paging['translation'].sortField,
                  ascending : $scope.paging['translation'].ascending == null ? true
                    : $scope.paging['translation'].ascending,
                  queryRestriction : null
                };

                if ($scope.value == 'PUBLISHED' || $scope.value == 'BETA') {
                  pfs.queryRestriction = 'workflowStatus:' + $scope.value;
                  pfs.latestOnly = $scope.showLatest;
                  translationService.findTranslationsForQuery($scope.paging['translation'].filter,
                    pfs).then(function(data) {
                    $scope.translations = data.translations;
                    $scope.translations.totalCount = data.totalCount;
                    $scope.stats.count = $scope.translations.totalCount;
                    $scope.reselect();
                  });
                }

                if ($scope.value == 'EDITING') {
                  pfs.queryRestriction = $scope.paging['translation'].filter;
                  workflowService.findNonReleaseProcessTranslations($scope.project.id, pfs).then(
                    function(data) {
                      $scope.translations = data.translations;
                      $scope.translations.totalCount = data.totalCount;
                      $scope.stats.count = $scope.translations.totalCount;
                      $scope.reselect();
                    });
                }

                if ($scope.value == 'RELEASE') {
                  pfs.queryRestriction = 'projectId: '
                    + $scope.project.id
                    + ' AND (workflowStatus:READY_FOR_PUBLICATION OR workflowStatus:BETA OR workflowStatus:PUBLISHED)';
                  pfs.latestOnly = $scope.showLatest;
                  translationService.findTranslationsForQuery($scope.paging['translation'].filter,
                    pfs).then(function(data) {
                    $scope.translations = data.translations;
                    $scope.translations.totalCount = data.totalCount;
                    $scope.stats.count = $scope.translations.totalCount;
                    $scope.reselect();
                  });
                }

              };

              // Set the scope link
              $scope.getLink = function(translation) {
                $scope.link = utilService.composeUrl('/directory?translationId=' + translation.id)
              }

              // Clear the url params when "clear" gets clicked
              $scope.clearUrlParams = function() {
                if ($scope.value == 'PUBLISHED' && $scope.link) {
                  var index = $scope.link.indexOf("?");
                  if (index != -1) {
                    $scope.link = $scope.link.substring(0, index);
                    $window.location.href = $scope.link;
                    $route.reload();
                  }
                }
              }

              // Reselect selected translation to refresh it
              $scope.reselect = function() {
                // If no selected translation, use route params
                if (!$scope.selected.translation && $routeParams.translationId
                  && $scope.value == 'PUBLISHED') {
                  $scope.selected.translation = {
                    id : $routeParams.translationId
                  };
                }

                // If no selected translation, use user preferences
                if (!$scope.selected.translation && $scope.user.userPreferences.lastTranslationId
                  && $scope.value == $scope.user.userPreferences.lastTranslationAccordion) {
                  $scope.selected.translation = {
                    id : $scope.user.userPreferences.lastTranslationId
                  };
                }
                // if there is a selection...
                if ($scope.selected.translation) {
                  // If $scope.selected.translation is in the list, select it,
                  // if not clear $scope.selected.translation
                  var found = false;
                  for (var i = 0; i < $scope.translations.length; i++) {
                    if ($scope.selected.translation.id == $scope.translations[i].id) {
                      $scope.selectTranslation($scope.translations[i]);
                      found = true;
                      break;
                    }
                  }

                  if (!found) {
                    $scope.selected.translation = null;
                    $scope.selected.concept = null;
                    $scope.clearLastTranslationId();
                  }
                }

                // If still no selection, clear lastTranslationId
                else {
                  $scope.clearLastTranslationId();
                }

                // If 'lookup in progress' is set, get progress
                for (var i = 0; i < $scope.translations.length; i++) {
                  var translation = $scope.translations[i];
                  if (translation.lookupInProgress) {
                    $scope.refreshLookupProgress(translation);
                  }
                }
              };

              // clear the last translation id
              $scope.clearLastTranslationId = function() {
                if ($scope.user.userPreferences.lastTranslationId
                  && $scope.value == $scope.user.userPreferences.lastTranslationAccordion) {
                  $scope.user.userPreferences.lastTranslationId = null;
                  securityService.updateUserPreferences($scope.user.userPreferences);
                }
              }

              // Get $scope.selected.translation.concepts
              $scope.getConcepts = function(translation) {

                var pfs = prepPfs();

                translationService.findTranslationConceptsForQuery(translation.id,
                  $scope.paging['concept'].filter, pfs).then(function(data) {
                  translation.concepts = data.concepts;
                  translation.concepts.totalCount = data.totalCount;
                });

              };

              // Prepare PFS for searches and for export
              function prepPfs() {
                var pfs = {
                  startIndex : ($scope.paging['concept'].page - 1)
                    * $scope.paging['concept'].pageSize,
                  maxResults : $scope.paging['concept'].pageSize,
                  sortField : $scope.paging['concept'].sortField,
                  ascending : $scope.paging['concept'].ascending == null ? true
                    : $scope.paging['concept'].ascending,
                  queryRestriction : null
                };

                // For editing pane, restrict to READY_FOR_PUBLICATION only
                if ($scope.value == 'EDITING') {
                  pfs.queryRestriction = 'workflowStatus:READY_FOR_PUBLICATION AND revision:false';
                  if ($scope.withNotesOnly) {
                    pfs.queryRestriction += ' AND notes.value:[* TO *]';
                  }
                }
                if ($scope.value == 'RELEASE') {
                  if ($scope.withNotesOnly) {
                    pfs.queryRestriction = 'notes.value:[* TO *]';
                  }
                  // may not need a restriction here
                }
                return pfs;
              }
              // Get $scope.selected.translation.available
              $scope.getAvailableConcepts = function(translation) {
                if (!$scope.projects.role) {
                  return;
                }
                var pfs = {
                  startIndex : ($scope.paging['available'].page - 1)
                    * $scope.paging['available'].pageSize,
                  maxResults : $scope.paging['available'].pageSize,
                  sortField : $scope.paging['available'].sortField,
                  ascending : $scope.paging['available'].ascending == null ? true
                    : $scope.paging['available'].ascending,
                  queryRestriction : null
                };

                pfs.queryRestriction = $scope.paging['available'].filter;
                workflowService.findAvailableConcepts($scope.projects.role, $scope.project.id,
                  translation.id, $scope.user.userName, pfs).then(
                // Success
                function(data) {
                  translation.available = data.concepts;
                  translation.available.totalCount = data.totalCount;
                });

              };

              // Get $scope.selected.translation.assigned
              // If nextIndex is set, we need to open the edit concept modal for
              // that entry
              $scope.getAssignedConcepts = function(translation, nextIndex) {
                if (!$scope.projects.role) {
                  return;
                }

                var pfs = {
                  startIndex : ($scope.paging['assigned'].page - 1)
                    * $scope.paging['assigned'].pageSize,
                  maxResults : $scope.paging['assigned'].pageSize,
                  sortField : $scope.paging['assigned'].sortField,
                  ascending : $scope.paging['assigned'].ascending == null ? false
                    : $scope.paging['assigned'].ascending,
                  queryRestriction : null
                };

                if ($scope.projects.role != 'ADMIN') {
                  pfs.queryRestriction = $scope.paging['assigned'].filter;
                  workflowService.findAssignedConcepts($scope.projects.role, $scope.project.id,
                    translation.id, $scope.user.userName, pfs).then(
                    // Success
                    function(data) {
                      translation.assigned = data.records;
                      translation.assigned.totalCount = data.totalCount;
                      if (nextIndex == 0 || nextIndex) {
                        $scope.openEditConceptModal(translation.assigned[nextIndex].concept,
                          nextIndex);
                      }
                    });
                } else if ($scope.projects.role == 'ADMIN') {
                  pfs.queryRestriction = $scope.paging['assigned'].filter;
                  workflowService.findAssignedConcepts('ADMIN', $scope.project.id, translation.id,
                    $scope.user.userName, pfs).then(
                  // Success
                  function(data) {
                    translation.assigned = data.records;
                    translation.assigned.totalCount = data.totalCount;
                  });

                }

              };
              // get translation release info
              $scope.getTranslationReleaseInfo = function(translation) {
                var pfs = {
                  startIndex : -1,
                  maxResults : 10,
                  sortField : null,
                  ascending : null,
                  queryRestriction : null
                };
                releaseService.findTranslationReleasesForQuery(translation.id, null, pfs).then(
                  function(data) {
                    $scope.translationReleaseInfo = data.releaseInfos[0];
                  });

              };

              // Get $scope.filters
              $scope.getFilters = function() {
                var projectId = $scope.project ? $scope.project.id : null;
                var workflowStatus = null;
                if ($scope.value == 'PUBLISHED' || $scope.value == 'BETA') {
                  workflowStatus = $scope.value;
                }
                translationService.getFilters(projectId, workflowStatus).then(
                // Success
                function(data) {
                  $scope.filters = data.keyValuePairs;
                });
              };

              // Save user preferences
              $scope.saveUserPreferences = function() {
                securityService.updateUserPreferences($scope.user.userPreferences).then(
                // Success
                function(data) {
                  $scope.user.userPreferences = data;
                });
              };

              // export release artifact
              $scope.exportReleaseArtifact = function(artifact) {
                releaseService.exportReleaseArtifact(artifact);
              };

              // Removes all translation concepts
              $scope.removeAllTranslationConcepts = function(translation) {
                translationService.removeAllTranslationConcepts(translation.id).then(
                  function(data) {
                    translationService.fireTranslationChanged(translation);
                  });
              };

              // Convert date to a string
              $scope.toSimpleDate = function(lastModified) {
                return utilService.toSimpleDate(lastModified);

              };

              // Convert date to a string
              $scope.toDate = function(lastModified) {
                return utilService.toDate(lastModified);

              };

              // Convert date to a string
              $scope.toShortDate = function(lastModified) {
                return utilService.toShortDate(lastModified);

              };

              // Return the name for a terminology
              $scope.getTerminologyName = function(terminology) {
                if ($scope.metadata && $scope.metadata.terminologyNames) {
                  return $scope.metadata.terminologyNames[terminology];
                }
              };

              // Table sorting mechanism
              $scope.setSortField = function(table, field, object) {
                utilService.setSortField(table, field, $scope.paging);

                // retrieve the correct table
                if (table === 'translation') {
                  $scope.getTranslations();
                }
                if (table === 'concept') {
                  $scope.getConcepts(object);
                }
                if (table === 'available') {
                  $scope.getAvailableConcepts(object);
                }
                if (table === 'assigned') {
                  $scope.getAssignedConcepts(object);
                }
              };

              // Return up or down sort chars if sorted
              $scope.getSortIndicator = function(table, field) {
                return utilService.getSortIndicator(table, field, $scope.paging);
              };

              // Selects a translation (setting $scope.selected.translation).
              // Looks up current release info and concepts.
              $scope.selectTranslation = function(translation) {
                $scope.selected.translation = translation;
                $scope.selected.descriptionTypes = translation.descriptionTypes;
                $scope.selected.terminology = translation.terminology;
                $scope.selected.version = translation.version;
                $scope.getTranslationReleaseInfo(translation);
                $scope.getConcepts(translation);
                $scope.getAvailableConcepts(translation);
                $scope.getAssignedConcepts(translation);
                $scope.selected.concept = null;
                if (translation.id != $scope.user.userPreferences.lastTranslationId) {
                  $scope.user.userPreferences.lastTranslationId = translation.id;
                  securityService.updateUserPreferences($scope.user.userPreferences);
                }
                $scope.getLink(translation);
              };

              // Selects a concepts (setting $scope.concept)
              $scope.selectConcept = function(concept) {
                $scope.selected.concept = concept;
                // Look up details of concept
              };

              // Look through refsets to get the translation organization
              $scope.getTranslationOrganization = function(translation) {
                for (var i = 0; i < $scope.refsets.length; i++) {
                  if (translation.refsetId == $scope.refsets[i].id) {
                    return $scope.refsets[i].organization;
                  }
                }
              };

              // Remove a translation
              $scope.removeTranslation = function(translation) {

                workflowService
                  .findAssignedConcepts('ADMIN', $scope.project.id, translation.id,
                    $scope.user.userName, {
                      startIndex : 0,
                      maxResults : 1
                    })
                  .then(
                    // Success
                    function(data) {
                      if (data.records.length > 0
                        && !$window
                          .confirm('The translation has assigned concepts, are you sure you want to proceed?')) {
                        return;
                      }

                      // Now check for available concepts
                      workflowService
                        .findAvailableConcepts('ADMIN', $scope.project.id, translation.id,
                          $scope.user.userName, {
                            startIndex : 0,
                            maxResults : 1
                          })
                        .then(
                          // Success
                          function(data) {
                            if (data.concepts.length > 0
                              && !$window
                                .confirm('The translation has available concepts, are you sure you want to proceed?')) {
                              return;
                            }

                            $scope.removeTranslationHelper(translation);
                          });

                    });

              };
              $scope.removeTranslationHelper = function(translation) {
                console.debug("remove translation", translation);

                // Test for concept
                translationService
                  .findTranslationConceptsForQuery(translation.id, '', {
                    startIndex : 0,
                    maxResults : 1
                  })
                  .then(
                    function(data) {
                      if (data.concepts.length == 1) {
                        if (!$window
                          .confirm('The translation has concepts, are you sure you want to proceed?')) {
                          return;
                        }
                      }
                      translationService.removeTranslation(translation.id).then(
                      // Success
                      function(data) {
                        $scope.selected.translation = null;
                        translationService.fireTranslationChanged();
                      });

                    });

              };

              // Remove concept
              $scope.removeConcept = function(translation, concept) {
                translationService.removeTranslationConcept(concept.id).then(
                // Success
                function() {
                  $scope.getConcepts(translation);
                });
              };

              // Unassign the specified concept
              $scope.unassign = function(concept, userName) {
                if (!concept) {
                  return;
                }

                workflowService.performTranslationWorkflowAction($scope.project.id,
                  $scope.selected.translation.id, userName, getRole('UNASSIGN', concept),
                  'UNASSIGN', concept).then(
                // Success
                function(data) {
                  translationService.fireTranslationChanged($scope.selected.translation);
                });

              };

              // Unassign all concepts assigned to this user
              $scope.unassignAll = function(userName) {

                // load all concepts assigned to the user
                var pfs = {
                  startIndex : ($scope.paging['assigned'].page - 1)
                    * $scope.paging['assigned'].pageSize,
                  maxResults : $scope.paging['assigned'].pageSize,
                  sortField : $scope.paging['assigned'].sortField,
                  ascending : $scope.paging['assigned'].ascending == null ? false
                    : $scope.paging['assigned'].ascending,
                  queryRestriction : $scope.paging['assigned'].filter
                };

                workflowService.findAssignedConcepts($scope.projects.role, $scope.project.id,
                  $scope.selected.translation.id, userName, pfs).then(
                  // Success
                  function(data) {

                    // Extract concepts from records
                    var list = new Array();
                    for (var i = 0; i < data.records.length; i++) {
                      list.push(data.records[i].concept);
                    }

                    // Make parameter
                    var conceptList = {
                      concepts : list
                    };

                    // Unassign all concepts
                    workflowService.performBatchTranslationWorkflowAction($scope.project.id,
                      $scope.selected.translation.id, $scope.user.userName, $scope.projects.role,
                      'UNASSIGN', conceptList).then(
                    // Success
                    function(data) {
                      translationService.fireTranslationChanged($scope.selected.translation);
                    }
                    // Error is already handled by service
                    );

                  }
                // Error is already handled by service
                );

              };

              // Finish all concepts assigned to this user
              $scope.finishAll = function(userName) {

                // load all concepts assigned to the user
                var pfs = {
                  startIndex : ($scope.paging['assigned'].page - 1)
                    * $scope.paging['assigned'].pageSize,
                  maxResults : $scope.paging['assigned'].pageSize,
                  sortField : $scope.paging['assigned'].sortField,
                  ascending : $scope.paging['assigned'].ascending == null ? false
                    : $scope.paging['assigned'].ascending,
                  queryRestriction : $scope.paging['assigned'].filter
                };

                if ($scope.projects.role != 'ADMIN') {
                  workflowService.findAssignedConcepts($scope.projects.role, $scope.project.id,
                    $scope.selected.translation.id, userName, pfs).then(
                    // Success
                    function(data) {

                      // Extract concepts from records
                      var list = new Array();
                      for (var i = 0; i < data.records.length; i++) {
                        if (data.records[i].concept.workflowStatus.indexOf('IN_PROGRESS') != -1
                          || data.records[i].concept.workflowStatus == 'REVIEW_NEW') {
                          list.push(data.records[i].concept);
                        }
                      }
                      if (list.length == 0) {
                        alert('There are no concepts ready to be finished.')
                      }

                      // Make parameter
                      var conceptList = {
                        concepts : list
                      };

                      // Finish all concepts
                      workflowService.performBatchTranslationWorkflowAction($scope.project.id,
                        $scope.selected.translation.id, $scope.user.userName, $scope.projects.role,
                        'FINISH', conceptList).then(
                      // Success
                      function(data) {
                        translationService.fireTranslationChanged($scope.selected.translation);
                      }
                      // Error is already handled by service
                      );

                    }
                  // Error is already handled by service
                  );
                } else {
                  alert("Finish is only available for AUTHOR or REVIEWER roles.");
                }

              };

              // Publish all concepts assigned to this user
              $scope.publishAll = function(userName) {

                // load all concepts assigned to the user
                var pfs = {
                  startIndex : ($scope.paging['assigned'].page - 1)
                    * $scope.paging['assigned'].pageSize,
                  maxResults : $scope.paging['assigned'].pageSize,
                  sortField : $scope.paging['assigned'].sortField,
                  ascending : $scope.paging['assigned'].ascending == null ? false
                    : $scope.paging['assigned'].ascending,
                  queryRestriction : $scope.paging['assigned'].filter
                };

                workflowService.findAssignedConcepts($scope.projects.role, $scope.project.id,
                  $scope.selected.translation.id, $scope.user.userName, pfs).then(
                  // Success
                  function(data) {

                    // Extract concepts from records
                    var list = new Array();
                    for (var i = 0; i < data.records.length; i++) {
                      if (data.records[i].concept.workflowStatus == 'REVIEW_DONE') {
                        list.push(data.records[i].concept);
                      }
                    }
                    if (list.length == 0) {
                      alert('There are no concepts ready to be published.')
                    }

                    // Make parameter
                    var conceptList = {
                      concepts : list
                    };

                    // Publish all concepts
                    workflowService.performBatchTranslationWorkflowAction($scope.project.id,
                      $scope.selected.translation.id, $scope.user.userName, $scope.projects.role,
                      'PREPARE_FOR_PUBLICATION', conceptList).then(
                    // Success
                    function(data) {
                      translationService.fireTranslationChanged($scope.selected.translation);
                    }
                    // Error is already handled by service
                    );

                  }
                // Error is already handled by service
                );

              };
              // Performs a workflow action
              $scope.performWorkflowAction = function(concept, action) {

                workflowService.performTranslationWorkflowAction($scope.project.id,
                  $scope.selected.translation.id, $scope.user.userName, getRole(action, concept),
                  action, concept).then(
                // Success
                function(data) {
                  translationService.fireConceptChanged(concept);
                });
              };

              // Need both a $scope version and a non one for modals.
              $scope.startLookup = function(translation) {
                startLookup(translation);
              };

              // Start lookup again
              function startLookup(translation) {
                console.debug("start lookup", $scope.lookupInterval);
                translationService.startLookup(translation.id).then(
                // Success
                function(data) {
                  $scope.translationLookupProgress[translation.id] = 1;
                  if (!$scope.lookupInterval) {
                    $scope.lookupInterval = $interval(function() {
                      $scope.refreshLookupProgress(translation);
                    }, 2000);
                  }
                });
              }

              // Refresh lookup progress
              $scope.refreshLookupProgress = function(translation) {
                console.debug("Refresh lookup progress", $scope.translationLookupProgress);
                translationService.getLookupProgress(translation.id).then(
                // Success
                function(data) {
                  $scope.translationLookupProgress[translation.id] = data;

                  // If all lookups in progress are at 100%, stop interval
                  var found = true;
                  for ( var key in $scope.translationLookupProgress) {
                    if ($scope.translationLookupProgress[key] < 100) {
                      found = false;
                      break;
                    }
                  }
                  if (found) {
                    $interval.cancel($scope.lookupInterval);
                    $scope.lookupInterval = null;
                  }

                },
                // Error
                function(data) {
                  // Cancel automated lookup on error
                  $interval.cancel($scope.lookupInterval);
                });
              };

              // Get the most recent note for display
              $scope.getLatestNote = function(translation) {
                if (translation && translation.notes && translation.notes.length > 0) {
                  return $sce.trustAsHtml(translation.notes.sort(utilService.sortBy('lastModified',
                    -1))[0].value);
                }
                return $sce.trustAsHtml('');
              };

              // Save user preferences
              $scope.saveUserPreferences = function() {
                securityService.updateUserPreferences($scope.user.userPreferences).then(
                // Success
                function(data) {
                  $scope.user.userPreferences = data;
                });
              };

              // Clear spelling dictionary
              $scope.clearSpellingDictionary = function(translation) {
                translationService.clearSpellingDictionary(translation.id).then(function(data) {
                  translationService.fireTranslationChanged(translation);
                });

              };

              // Clear phrase memory
              $scope.clearPhraseMemory = function(translation) {
                translationService.clearPhraseMemory(translation.id).then(function(data) {
                  translationService.fireTranslationChanged(translation);
                });

              };

              // Directive scoped method for cancelling a release
              $scope.cancelAction = function(translation) {
                $scope.translation = translation;
                if (translation.stagingType == 'IMPORT') {
                  translationService.cancelImportConcepts($scope.translation.id).then(
                  // Success
                  function() {
                    translationService.fireTranslationChanged($scope.translation);
                  });
                }
                if (translation.stagingType == 'BETA' || translation.inPublicationProcess) {
                  releaseService.cancelTranslationRelease($scope.translation.id).then(
                  // Success
                  function() {
                    translationService.fireTranslationChanged($scope.translation);
                  });
                }
              };

              // cancelling a release given the staged translation
              $scope.cancelActionForStaged = function(translation) {
                if (translation.workflowStatus == 'BETA') {
                  translationService.getOriginForStagedTranslation(translation.id).then(
                  // Success
                  function(data) {
                    $scope.originId = data;
                    translationService.getTranslation(data).then(
                    // Success
                    function(data) {
                      $scope.cancelAction(data);
                    });
                  });
                }
              };

              $scope.isAllowed = function(action, concept) {
                if ($scope.value == 'PUBLISHED' || $scope.value == 'BETA') {
                  return false;
                }
                return workflowService.translationIsAllowed(action, $scope.projects.role,
                  concept.workflowStatus, $scope.metadata.workflowConfig);
              }

              $scope.getRole = function(action, concept) {
                getRole(action, concept);
              };

              // Start lookup again
              function getRole(action, concept) {
                if ($scope.value == 'PUBLISHED' || $scope.value == 'BETA') {
                  return $scope.projects.role;
                }
                return workflowService.translationGetRole(action, $scope.projects.role,
                  concept.workflowStatus, $scope.metadata.workflowConfig);
              }

              // 
              // MODALS
              //

              // Notes modal
              $scope.openNotesModal = function(lobject, ltype) {
                console.debug('openNotesModal ', lobject, ltype);

                var modalInstance = $uibModal.open({
                  // Reuse refset URL
                  templateUrl : 'app/component/refsetTable/notes.html',
                  controller : NotesModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    object : function() {
                      return lobject;
                    },
                    type : function() {
                      return ltype;
                    },
                    tinymceOptions : function() {
                      return utilService.tinymceOptions;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.selectTranslation($scope.selected.translation);
                });

              };

              // Notes controller
              var NotesModalCtrl = function($scope, $uibModalInstance, $sce, object, type,
                tinymceOptions) {
                console.debug('Entered notes modal control', object, type);

                $scope.errors = [];

                $scope.object = object;
                $scope.type = type;
                $scope.tinymceOptions = tinymceOptions;
                $scope.newNote = null;

                // Paging parameters
                $scope.pageSize = 5;
                $scope.pagedNotes = [];
                $scope.paging = {};
                $scope.paging['notes'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };

                // Get paged notes (assume all are loaded)
                $scope.getPagedNotes = function() {
                  $scope.pagedNotes = utilService.getPagedArray($scope.object.notes,
                    $scope.paging['notes'], $scope.pageSize);
                };

                $scope.getNoteValue = function(note) {
                  return $sce.trustAsHtml(note.value);
                };

                // remove note
                $scope.removeNote = function(object, note) {
                  if ($scope.type == 'Translation') {
                    translationService.removeTranslationNote(object.id, note.id).then(
                    // Success - add refset
                    function(data) {
                      $scope.newNote = null;
                      translationService.getTranslation(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add refset
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - add refset
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  } else if ($scope.type == 'Concept') {
                    translationService.removeTranslationConceptNote(object.id, note.id).then(
                    // Success - add refset
                    function(data) {
                      $scope.newNote = null;
                      translationService.getConcept(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add refset
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - add refset
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }
                };

                $scope.submitNote = function(object, text) {

                  if ($scope.type == 'Translation') {
                    translationService.addTranslationNote(object.id, text).then(
                    // Success - add translation
                    function(data) {
                      $scope.newNote = null;
                      translationService.getTranslation(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add translation
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - add translation
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  } else if ($scope.type == 'Concept') {
                    translationService.addTranslationConceptNote(object.translationId, object.id,
                      text).then(
                    // Success - add translation
                    function(data) {
                      $scope.newNote = null;

                      translationService.getConcept(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add translation
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - add translation
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }
                };

                // Convert date to a string
                $scope.toDate = function(lastModified) {
                  return utilService.toDate(lastModified);
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

                // initialize modal
                $scope.getPagedNotes();
              };

              // Add translation modal
              $scope.openAddTranslationModal = function() {
                console.debug('openAddTranslationModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/translationTable/editTranslation.html',
                  controller : AddTranslationModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    metadata : function() {
                      return $scope.metadata;
                    },
                    refsets : function() {
                      return $scope.refsets.sort(utilService.sortBy('name'));
                    },
                    project : function() {
                      return $scope.project;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.selected.translation = data;
                  translationService.fireTranslationChanged(data);
                });
              };

              // Add translation controller
              var AddTranslationModalCtrl = function($scope, $uibModalInstance, metadata, refsets,
                project) {

                console.debug('Entered add translation modal control', metadata);

                $scope.action = 'Add';
                $scope.errors = [];
                $scope.warnings = [];
                $scope.metadata = metadata;
                $scope.project = project;
                $scope.refsets = refsets;
                $scope.translation = {
                  workflowPath : metadata.workflowPaths[0],
                  projectId : project.id
                };

                // Return the name for a terminology
                $scope.getTerminologyName = function(terminology) {
                  return $scope.metadata.terminologyNames[terminology];
                };

                $scope.selectRefset = function(refset) {
                  $scope.translation.moduleId = refset.moduleId;
                  $scope.translation.terminology = refset.terminology;
                  $scope.translation.version = refset.version;
                  $scope.translation.terminologyId = refset.terminologyId;
                  $scope.translation.refsetId = refset.id;
                };

                // Update translation
                $scope.submitTranslation = function(translation) {

                  // Validate the translation
                  validationService
                    .validateTranslation(translation, $scope.project.id)
                    .then(
                      // Success
                      function(data) {
                        // If there are errors, make them available and stop.
                        if (data.errors && data.errors.length > 0) {
                          $scope.errors = data.errors;
                          return;
                        } else {
                          $scope.errors = [];
                        }

                        // if data.warnings is set and doesn't match
                        // $scope.warnings
                        if (data.warnings && data.warnings.length > 0
                          && $scope.warnings.join() !== data.warnings.join()) {
                          $scope.warnings = data.warnings;
                          return;
                        } else {
                          $scope.warnings = [];
                        }

                        if (!translation.name || !translation.description || !translation.language) {
                          $scope.errors[0] = "Translation name, description, and language must not be empty.";
                          return;
                        }
                        if (!translation.refsetId) {
                          $scope.errors[0] = "A refset must be selected";
                          return;
                        }
                        translationService.addTranslation(translation).then(
                        // Success - update translation
                        function(data) {
                          $uibModalInstance.close(data);
                        },
                        // Error - update translation
                        function(data) {
                          handleError($scope.errors, data);
                        });

                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });

                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              $scope.openEditTranslationModal = function(ltranslation) {
                console.debug('openEditTranslationModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/translationTable/editTranslation.html',
                  controller : EditTranslationModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    metadata : function() {
                      return $scope.metadata;
                    },
                    refsets : function() {
                      return $scope.refsets;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    translation : function() {
                      return ltranslation;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  translationService.fireTranslationChanged(data);
                });
              };

              // Edit translation controller
              var EditTranslationModalCtrl = function($scope, $uibModalInstance, metadata, refsets,
                project, translation) {

                console.debug('Entered edit translation modal control', metadata, translation);

                $scope.action = 'Edit';
                $scope.errors = [];
                $scope.warnings = [];
                $scope.metadata = metadata;
                $scope.refsets = refsets;
                $scope.project = project;
                $scope.translation = translation;

                // find refset translation is attached to (by terminology id)
                for (var i = 0; i < refsets.length; i++) {
                  if ($scope.translation.refsetId == refsets[i].id) {
                    $scope.refset = refsets[i];
                    break;
                  }
                }

                // Return the name for a terminology
                $scope.getTerminologyName = function(terminology) {
                  return $scope.metadata.terminologyNames[terminology];
                };

                // Update translation
                $scope.submitTranslation = function(translation) {

                  // Validate the translation
                  validationService.validateTranslation(translation, $scope.project.id).then(
                  // Success
                  function(data) {
                    // If there are errors, make them available and stop.
                    if (data.errors && data.errors.length > 0) {
                      $scope.errors = data.errors;
                      return;
                    } else {
                      $scope.errors = [];
                    }

                    // if $scope.warnings is empty, and data.warnings is not,
                    // show warnings and stop
                    if ($scope.warnings.length == 0 && data.warnings && data.warnings.length > 0) {
                      $scope.warnings = data.warnings;
                      return;
                    } else {
                      $scope.warnings = [];
                    }

                    translationService.updateTranslation(translation).then(
                    // Success - update translation
                    function(data) {
                      $uibModalInstance.close(data);
                    },
                    // Error - update translation
                    function(data) {
                      handleError($scope.errors, data);
                    });

                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Assign concept modal
              $scope.openAssignConceptModal = function(lconcept, laction) {
                console.debug('openAssignConceptModal ', lconcept, laction);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/translationTable/assignConcept.html',
                  controller : AssignConceptModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    concept : function() {
                      return lconcept;
                    },
                    metadata : function() {
                      return $scope.metadata;
                    },
                    action : function() {
                      return laction;
                    },
                    translation : function() {
                      return $scope.selected.translation;
                    },
                    currentUser : function() {
                      return $scope.user;
                    },
                    assignedUsers : function() {
                      return $scope.projects.assignedUsers;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    role : function() {
                      return $scope.projects.role;
                    },
                    tinymceOptions : function() {
                      return utilService.tinymceOptions;
                    }
                  }

                });

                modalInstance.result.then(
                // Success
                function(data) {
                  translationService.fireConceptChanged(data);
                });
              };

              // Assign concept controller
              var AssignConceptModalCtrl = function($scope, $uibModalInstance, concept, metadata,
                action, translation, currentUser, assignedUsers, project, role, tinymceOptions) {

                console.debug('Entered assign concept modal control', concept);

                $scope.concept = concept;
                $scope.metadata = metadata;
                $scope.workflowConfig = metadata.workflowConfig;
                $scope.note;
                $scope.translation = translation;
                $scope.action = action;
                $scope.project = project;
                $scope.role = workflowService.translationGetRole(action, role,
                  concept.workflowStatus, $scope.workflowConfig);
                $scope.assignedUsers = [];
                $scope.user = utilService.findBy(assignedUsers, currentUser, 'userName');
                $scope.tinymceOptions = tinymceOptions;
                $scope.note;
                $scope.errors = [];

                // Sort users by name and role restrict
                var sortedUsers = assignedUsers.sort(utilService.sortBy('name'));
                for (var i = 0; i < sortedUsers.length; i++) {
                  if ($scope.role == 'AUTHOR'
                    || $scope.project.userRoleMap[sortedUsers[i].userName] == 'REVIEWER'
                    || $scope.project.userRoleMap[sortedUsers[i].userName] == 'ADMIN') {
                    $scope.assignedUsers.push(sortedUsers[i]);
                  }
                }

                // Assign
                $scope.assignConcept = function() {
                  if (!$scope.user) {
                    $scope.errors[0] = 'The user must be selected. ';
                    return;
                  }

                  if (action == 'ASSIGN') {
                    workflowService.performTranslationWorkflowAction($scope.project.id,
                      translation.id, $scope.user.userName, getRole('ASSIGN', concept), 'ASSIGN',
                      $scope.concept).then(
                      // Success
                      function(data) {

                        // Add a note as well
                        if ($scope.note) {
                          translationService.addTranslationConceptNote(translation.id, concept.id,
                            $scope.note).then(
                          // Success
                          function(data) {
                            $uibModalInstance.close(translation);
                          },
                          // Error
                          function(data) {
                            handleError($scope.errors, data);
                          });
                        }
                        // close dialog if no note
                        else {
                          $uibModalInstance.close(translation);
                        }

                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                  }
                }

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Assign batch concept modal
              $scope.openBatchAssignConceptModal = function(type) {
                console.debug('openBatchAssignConceptModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/translationTable/assignBatch.html',
                  controller : BatchAssignConceptModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    translation : function() {
                      return $scope.selected.translation;
                    },
                    metadata : function() {
                      return $scope.metadata;
                    },
                    currentUser : function() {
                      return $scope.user;
                    },
                    assignedUsers : function() {
                      return $scope.projects.assignedUsers;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    role : function() {
                      return $scope.projects.role;
                    },
                    paging : function() {
                      return $scope.paging;
                    },
                    type : function() {
                      return type;
                    }
                  }

                });

                modalInstance.result.then(
                // Success
                function(data) {
                  translationService.fireConceptChanged(data);
                });
              };

              // Batch assign concept controller
              var BatchAssignConceptModalCtrl = function($scope, $uibModalInstance, translation,
                metadata, currentUser, assignedUsers, project, role, paging, type) {
                console.debug('Entered assign concept modal control', assignedUsers, project.id);

                $scope.translation = translation;
                $scope.metadata = metadata;
                $scope.batchSize = 10;
                $scope.batchSizes = [ 10, 50, 100, 200 ];
                $scope.project = project;
                $scope.role = role;
                $scope.assignedUsers = [];
                $scope.user = utilService.findBy(assignedUsers, currentUser, 'userName');
                $scope.errors = [];
                $scope.note = '';

                // Sort users by name and role restrict
                var sortedUsers = assignedUsers.sort(utilService.sortBy('name'));
                for (var i = 0; i < sortedUsers.length; i++) {
                  if ($scope.role == 'AUTHOR'
                    || $scope.project.userRoleMap[sortedUsers[i].userName] == 'REVIEWER'
                    || $scope.project.userRoleMap[sortedUsers[i].userName] == 'ADMIN') {
                    $scope.assignedUsers.push(sortedUsers[i]);
                  }
                }

                // Assign the batch
                $scope.assignBatch = function() {
                  if (!$scope.user) {
                    $scope.errors[0] = 'The user must be selected. ';
                    return;
                  }

                  // Perform the same search as the current concept id list
                  // and make sure it still matches (otherwise someone else
                  // may have assigned off this list first). If successful, send
                  // the request
                  var pfs = null;
                  if (type == 'Available') {
                    pfs = {
                      startIndex : (paging['available'].page - 1) * $scope.batchSize,
                      maxResults : $scope.batchSize,
                      sortField : paging['available'].sortField,
                      ascending : paging['available'].ascending == null ? true
                        : paging['available'].ascending,
                      queryRestriction : paging['available'].filter
                    };
                  } else {
                    pfs = {
                      startIndex : (paging['concept'].page - 1) * $scope.batchSize,
                      maxResults : $scope.batchSize,
                      sortField : paging['concept'].sortField,
                      ascending : paging['concept'].ascending == null ? true
                        : paging['concept'].ascending,
                      queryRestriction : null
                    }
                    pfs.queryRestriction = 'workflowStatus:READY_FOR_PUBLICATION AND revision:false';
                    if ($scope.withNotesOnly) {
                      pfs.queryRestriction += ' AND notes.value:[* TO *]';
                    }

                  }

                  if ($scope.role == 'AUTHOR' && type == 'Available') {

                    workflowService
                      .findAvailableConcepts('AUTHOR', project.id, translation.id,
                        $scope.user.userName, pfs)
                      .then(
                        // Success
                        function(data) {

                          // The first X entries of data.concepts should match
                          // translation.available
                          var match = true;
                          for (var i = 0; i < Math.min(data.concepts.length,
                            translation.available.length); i++) {
                            if (data.concepts[i].id !== translation.available[i].id) {
                              match = false;
                              break;
                            }
                          }
                          if (!match) {
                            $scope.errors[0] = 'Some available concepts have been assigned, please refresh the list and try again.';
                            return;
                          }

                          // Make parameter
                          var conceptList = {
                            concepts : data.concepts
                          };

                          workflowService.performBatchTranslationWorkflowAction($scope.project.id,
                            translation.id, $scope.user.userName, $scope.role, 'ASSIGN',
                            conceptList).then(
                          // Success
                          function(data) {
                            // close modal
                            $uibModalInstance.close(translation);
                          },
                          // Error
                          function(data) {
                            handleError($scope.errors, data);
                          });

                        },
                        // Error
                        function(data) {
                          handleError($scope.errors, data);
                        });
                  }

                  else if ($scope.role == 'REVIEWER' && type == 'Available') {
                    workflowService
                      .findAvailableConcepts('REVIEWER', project.id, translation.id,
                        $scope.user.userName, pfs)
                      .then(
                        // Success
                        function(data) {

                          // The first X entries of data.concepts should match
                          // translation.available
                          var match = true;
                          for (var i = 0; i < Math.min(data.concepts.length,
                            translation.available.length); i++) {
                            if (data.concepts[i].id !== translation.available[i].id) {
                              match = false;
                              break;
                            }
                          }
                          if (!match) {
                            $scope.errors[0] = 'Some available concepts have been assigned, please refresh the list and try again.';
                            return;
                          }

                          // Make parameter
                          var conceptList = {
                            concepts : data.concepts
                          };

                          workflowService.performBatchTranslationWorkflowAction($scope.project.id,
                            translation.id, $scope.user.userName, $scope.role, 'ASSIGN',
                            conceptList).then(
                          // Success
                          function(data) {
                            // close modal
                            $uibModalInstance.close(translation);
                          },
                          // Error
                          function(data) {
                            handleError($scope.errors, data);
                          });

                        },
                        // Error
                        function(data) {
                          handleError($scope.errors, data);
                        });
                  } else if (type == 'Finished') {
                    translationService
                      .findTranslationConceptsForQuery(translation.id, paging['concept'].filter,
                        pfs)
                      .then(
                        // Success
                        function(data) {

                          // The first X entries of data.concepts should match
                          // translation.available
                          var match = true;
                          for (var i = 0; i < Math.min(data.concepts.length,
                            translation.concepts.length); i++) {
                            if (data.concepts[i].id !== translation.concepts[i].id) {
                              match = false;
                              break;
                            }
                          }
                          if (!match) {
                            $scope.errors[0] = 'Some translation concepts have been assigned, please refresh the list and try again.';
                            return;
                          }

                          // Make parameter
                          var conceptList = {
                            concepts : data.concepts
                          };

                          workflowService.performBatchTranslationWorkflowAction($scope.project.id,
                            translation.id, $scope.user.userName, 'AUTHOR', 'ASSIGN', conceptList)
                            .then(
                            // Success
                            function(data) {
                              // close modal
                              $uibModalInstance.close(translation);
                            },
                            // Error
                            function(data) {
                              handleError($scope.errors, data);
                            });

                        },
                        // Error
                        function(data) {
                          handleError($scope.errors, data);
                        });
                  }

                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Edit concept modal (pass in the index in the current assigned
              // work search results)
              $scope.openEditConceptModal = function(lconcept, index) {

                console.debug('openEditConceptModal', lconcept, index);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/translationTable/editConcept.html',
                  controller : 'EditConceptModalCtrl',
                  backdrop : 'static',
                  size : 'lg',
                  resolve : {
                    concept : function() {
                      return lconcept;
                    },
                    translation : function() {
                      return $scope.selected.translation;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    user : function() {
                      return $scope.user;
                    },
                    role : function() {
                      return $scope.projects.role;
                    }
                  }
                });

                modalInstance.result
                  .then(
                  // Success
                  function(data) {
                    // data is an action:
                    // CLOSE => fire concept changed with the concept
                    if (data === 'CLOSE') {
                      translationService.fireConceptChanged(lconcept);
                      return;
                    }

                    // If the index is at the end of the total count of assigned
                    // concepts, then we are finished. simulate "close" but
                    // with a window confirm.
                    if ($scope.selected.translation.assigned.length == $scope.selected.translation.assigned.totalCount
                      && (index + 1) == $scope.selected.translation.assigned.totalCount) {
                      window.confirm("The end of the list of concepts to edit has been reached.");
                      translationService.fireConceptChanged(lconcept);
                      return;
                    }

                    // SAVE, FINISIH => compute next index, re-search, reopen
                    // modal
                    // Search results in $scope.selected.translation.assigned
                    var searchAgain = false;
                    var nextIndex = index;
                    if (data === 'FINISH') {
                      // search again if there are more past the current index
                      if ((index + 1) < $scope.selected.translation.assigned.totalCount) {
                        searchAgain = true;
                      }
                      // nextIndex remains unchanged
                    } else if (data === 'SAVE') {
                      // search again if we are at the end of the current search
                      // results, but not at the end of the total search results
                      if ((index + 1) == $scope.selected.translation.assigned.length
                        && (index + 1) < $scope.selected.translation.assigned.totalCount) {
                        searchAgain = true;
                        nextIndex = 0;
                        $scope.paging['assigned'].page++;
                      } else {
                        nextIndex++;
                      }

                    } else {
                      alert('SHOULD NEVER HAPPEN: ' + data);
                    }

                    // Search, then open the concept modal
                    if (searchAgain) {
                      $scope.getAssignedConcepts($scope.selected.translation, nextIndex);
                    }

                    // Otherwise, just open the modal for the next concept
                    else {
                      $scope.openEditConceptModal(
                        $scope.selected.translation.assigned[nextIndex].concept, nextIndex);
                    }

                  });
              };

              // Import/Export modal
              $scope.openImportExportModal = function(ltranslation, loperation, ltype) {
                console.debug('exportModal ', ltranslation);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/translationTable/importExport.html',
                  controller : ImportExportModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    translation : function() {
                      return ltranslation;
                    },
                    metadata : function() {
                      return $scope.metadata;
                    },
                    operation : function() {
                      return loperation;
                    },
                    type : function() {
                      return ltype;
                    },
                    ioHandlers : function() {
                      if (loperation == 'Import') {
                        return $scope.metadata.importHandlers;
                      } else {
                        return $scope.metadata.exportHandlers;
                      }
                    },
                    query : function() {
                      return $scope.paging['concept'].filter;
                    },
                    pfs : function() {
                      var pfs = prepPfs();
                      pfs.startIndex = -1;
                      return pfs;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  translationService.fireTranslationChanged(data);
                });
              };

              // Import/Export controller
              var ImportExportModalCtrl = function($scope, $uibModalInstance, translation,
                metadata, operation, type, ioHandlers, query, pfs) {
                console.debug('Entered import export modal control');

                $scope.translation = translation;
                $scope.metadata = metadata;
                $scope.query = query;
                $scope.pfs = pfs;
                $scope.ioHandlers = ioHandlers;
                $scope.selectedIoHandler = null;
                for (var i = 0; i < ioHandlers.length; i++) {
                  // Choose first one if only one
                  if ($scope.selectedIoHandler == null) {
                    $scope.selectedIoHandler = ioHandlers[i];
                  }
                  // choose "rf2" as default otherwise
                  if (ioHandlers[i].name.endsWith("RF2")) {
                    $scope.selectedIoHandler = ioHandlers[i];
                  }
                }
                $scope.type = type;
                $scope.operation = operation;
                $scope.errors = [];
                $scope.warnings = [];
                $scope.comments = [];
                $scope.importStarted = false;
                $scope.importFinished = false;
                if (type == 'Translation' && ($scope.query || $scope.pfs)) {
                  $scope.warnings
                    .push("Export is based on current search criteria and may not include all concepts.");
                }

                // Handle export
                $scope.export = function() {
                  if (type == 'Spelling Dictionary') {
                    translationService.exportSpellingDictionary($scope.translation);
                  }
                  if (type == 'Phrase Memory') {
                    translationService.exportPhraseMemory($scope.translation);
                  }
                  if (type == 'Translation') {
                    translationService.exportConcepts($scope.translation, $scope.selectedIoHandler,
                      $scope.query, $scope.pfs);
                  }
                  $uibModalInstance.close($scope.translation);
                };

                // Handle import
                $scope.import = function(file) {
                  if (type == 'Spelling Dictionary') {
                    translationService.importSpellingDictionary($scope.translation.id, file).then(
                    // Success
                    function(data) {
                      $uibModalInstance.close($scope.translation);
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }

                  if (type == 'Phrase Memory') {
                    translationService.importPhraseMemory($scope.translation.id, file).then(
                    // Success
                    function(data) {
                      $uibModalInstance.close($scope.translation);
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }

                  if (type == 'Translation') {
                    translationService.beginImportConcepts($scope.translation.id,
                      $scope.selectedIoHandler.id).then(

                      // Success
                      function(data) {
                        $scope.importStarted = true;
                        // data is a validation result, check for errors
                        if (data.errors.length > 0) {
                          $scope.errors = data.errors;
                        } else {

                          // If there are no errors, finish import
                          translationService.finishImportConcepts($scope.translation.id,
                            $scope.selectedIoHandler.id, file).then(
                          // Success - close dialog
                          function(data) {
                            $scope.importFinished = true;
                            $scope.errors = data.errors;
                            $scope.warnings = data.warnings;
                            $scope.comments = data.comments;
                            startLookup(translation);
                          },
                          // Failure - show error
                          function(data) {
                            handleError($scope.errors, data);
                          });
                        }
                      },

                      // Failure - show error, clear global error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                  }
                };

                // Handle continue import
                $scope.continueImport = function(file) {

                  if (type == 'Translation') {
                    translationService.finishImportConcepts($scope.translation.id,
                      $scope.selectedIoHandler.id, file).then(
                    // Success - close dialog
                    function(data) {
                      $scope.importFinished = true;
                      $scope.errors = data.errors;
                      $scope.warnings = data.warnings;
                      $scope.comments = data.comments;
                      startLookup(translation);
                    },
                    // Failure - show error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }
                };

                // Dismiss modal
                $scope.cancel = function() {
                  // If there are lingering errors, cancel the import
                  if (type == 'Translation' && $scope.errors.length > 0) {
                    translationService.cancelImportConcepts($scope.translation.id);
                  }
                  $uibModalInstance.close();
                };

                // Close modal
                $scope.close = function() {
                  $uibModalInstance.close();
                };

              };

              // Copy modal
              $scope.openCopyModal = function(ltranslation, ltype) {
                console.debug('openCopyModal ', ltranslation, ltype);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/translationTable/copy.html',
                  controller : CopyModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    toTranslation : function() {
                      return ltranslation;
                    },
                    type : function() {
                      return ltype;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  translationService.fireTranslationChanged(data);
                });
              };

              // Copy controller
              var CopyModalCtrl = function($scope, $uibModalInstance, toTranslation, type) {
                console.debug('Entered copymodal control');

                $scope.translations = [];
                $scope.toTranslation = toTranslation;
                $scope.translation = null;
                $scope.type = type;
                $scope.errors = [];

                // Initialize by looking up all translations
                translationService.findTranslationsForQuery('', {}).then(
                  // Success
                  function(data) {
                    var list = new Array();
                    for (var i = 0; i < data.translations.length; i++) {
                      // Skip this one
                      if (data.translations[i].id == $scope.toTranslation.id) {
                        continue;
                      }
                      if (type == 'Spelling Dictionary'
                        && !data.translations[i].spellingDictionaryEmpty) {
                        list.push(data.translations[i]);
                      }
                      if (type == 'Phrase Memory' && !data.translations[i].phraseMemoryEmpty) {
                        list.push(data.translations[i]);
                      }
                    }
                    $scope.translations = list.sort(utilService.sortBy('name'));
                    if ($scope.translations.length == 0) {
                      $scope.errors[0] = 'No translations with ' + type + ' entries were found.';
                    }
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                // Handle export
                $scope.copy = function() {
                  if (type == 'Spelling Dictionary') {
                    translationService.copySpellingDictionary($scope.translation.id,
                      $scope.toTranslation.id).then(function(data) {
                      $uibModalInstance.close($scope.toTranslation);
                    });
                  }
                  if (type == 'Phrase Memory') {
                    translationService.copyPhraseMemory($scope.translation.id,
                      $scope.toTranslation.id).then(function(data) {
                      $uibModalInstance.close($scope.toTranslation);
                    });
                  }
                };

                // Dismiss modal
                $scope.cancel = function() {
                  // dismiss the dialog
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Release Process modal
              $scope.openReleaseProcessModal = function(ltranslation) {

                console.debug('openReleaseProcessModal ', ltranslation);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/translationTable/release.html',
                  controller : ReleaseProcessModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    translation : function() {
                      return ltranslation;
                    },
                    ioHandlers : function() {
                      return $scope.metadata.exportHandlers;
                    },
                    utilService : function() {
                      return utilService;
                    }

                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  translationService.fireTranslationChanged(data);
                  $scope.selectTranslation(data);
                });
              };

              // Open release process modal given staged translation
              $scope.openReleaseProcessModalForStaged = function(translation) {

                translationService.getOriginForStagedTranslation(translation.id).then(
                // Success
                function(data) {
                  $scope.originId = data;
                  translationService.getTranslation(data).then(
                  // Success
                  function(data) {
                    $scope.openReleaseProcessModal(data);
                  });
                });
              };

              // Release Process controller
              var ReleaseProcessModalCtrl = function($scope, $uibModalInstance, translation,
                ioHandlers, utilService) {

                console.debug('Entered release process modal', translation.id, ioHandlers);

                $scope.errors = [];
                $scope.translation = translation;
                $scope.ioHandlers = ioHandlers;
                $scope.selectedIoHandler = $scope.ioHandlers[0];
                $scope.releaseInfo = [];
                $scope.validationResult = null;
                $scope.format = 'yyyyMMdd';
                $scope.releaseDate = utilService.toSimpleDate($scope.translation.effectiveTime);
                $scope.status = {
                  opened : false
                };

                if (translation.stagingType == 'BETA') {
                  releaseService.resumeRelease(translation.id).then(
                  // Success
                  function(data) {
                    $scope.stagedTranslation = data;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                }

                // Begin release
                $scope.beginTranslationRelease = function(translation) {

                  releaseService.beginTranslationRelease(translation.id,
                    utilService.toSimpleDate(translation.effectiveTime)).then(
                  // Success
                  function(data) {
                    $scope.releaseInfo = data;
                    $scope.translation.inPublicationProcess = true;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                // Validate release
                $scope.validateTranslationRelease = function(translation) {

                  releaseService.validateTranslationRelease(translation.id).then(
                  // Success
                  function(data) {
                    $scope.validationResult = data;
                    translationService.fireTranslationChanged(translation);
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Initiate BETA
                $scope.betaTranslationRelease = function(translation) {
                  $scope.validationResult = null;

                  releaseService
                    .betaTranslationRelease(translation.id, $scope.selectedIoHandler.id).then(
                    // Success
                    function(data) {
                      $scope.stagedTranslation = data;
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                };

                // Finish release
                $scope.finishTranslationRelease = function(translation) {

                  releaseService.finishTranslationRelease(translation.id,
                    $scope.selectedIoHandler.id).then(
                  // Success
                  function(data) {
                    $uibModalInstance.close(translation);
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // cancel operation, close modal
                $scope.cancel = function() {
                  releaseService.cancelTranslationRelease($scope.translation.id).then(
                  // Success
                  function() {
                    $uibModalInstance.close($scope.translation);
                  });
                };

                // Close modal
                $scope.close = function() {
                  $uibModalInstance.close($scope.translation);
                };

                $scope.open = function($event) {
                  $scope.status.opened = true;
                };

                $scope.format = 'yyyyMMdd';
              };

              // Log modal
              $scope.openLogModal = function(concept) {
                console.debug('openLogModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/log.html',
                  controller : LogModalCtrl,
                  backdrop : 'static',
                  size : 'lg',
                  resolve : {
                    translation : function() {
                      return $scope.selected.translation;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    concept : function() {
                      return concept;
                    },
                  }
                });

                // NO need for a result function
                // modalInstance.result.then(
                // // Success
                // function(data) {
                // });
              };

              // Log controller
              var LogModalCtrl = function($scope, $uibModalInstance, translation, project, concept) {
                console.debug('Entered log modal control', translation, project, concept);

                $scope.filter = '';
                $scope.errors = [];
                $scope.warnings = [];

                // Get log to display
                $scope.getLog = function() {
                  var objectId = translation.id
                  if (concept) {
                    objectId = concept.id;
                  }
                  projectService.getLog(project.id, objectId, $scope.filter).then(
                  // Success
                  function(data) {
                    $scope.log = data;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                // close modal
                $scope.close = function() {
                  // nothing changed, don't pass a value
                  $uibModalInstance.close();
                };

                // initialize
                $scope.getLog();
              };

              // Feedback modal
              $scope.openFeedbackModal = function(ltranslation) {
                console.debug('Open feedbackModal ', ltranslation);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/translationTable/feedback.html',
                  controller : FeedbackModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    translation : function() {
                      return ltranslation;
                    },
                    metadata : function() {
                      return $scope.metadata;
                    },
                    tinymceOptions : function() {
                      return utilService.tinymceOptions;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  translationService.fireTranslationChanged(data);
                });

              };

              // Feedback controller
              var FeedbackModalCtrl = function($scope, $uibModalInstance, translation, metadata,
                tinymceOptions) {
                console.debug('Entered feedback modal control', translation);

                $scope.translation = JSON.parse(JSON.stringify(translation));
                $scope.metadata = metadata;
                $scope.errors = [];
                $scope.tinymceOptions = tinymceOptions;

                $scope.addFeedback = function(translation, name, email, message) {

                  if (message == null || message == undefined || message === '') {
                    window.alert('The message cannot be empty. ');
                    return;
                  }

                  if (name == null || name == undefined || name === '' || email == null
                    || email == undefined || email === '') {
                    window.alert('Name and email must be provided.');
                    return;
                  }

                  if (!validateEmail(email)) {
                    window
                      .alert('Invalid email address provided (e.g. should be like someone@example.com)');
                    return;
                  }

                  workflowService.addFeedback(translation, name, email, message).then(
                  // Success
                  function(data) {
                    $uibModalInstance.dismiss('cancel');
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

                // email validation via regex
                function validateEmail(email) {
                  var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
                  return re.test(email);
                }

              };

              //
              // Initialize if project setting isn't used
              //
              if ($scope.value == 'BETA' || $scope.value == 'PUBLISHED') {
                $scope.getTranslations();
              }
              $scope.getFilters();

              // end

            } ]
        };
      } ]);
