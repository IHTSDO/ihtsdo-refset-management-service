// Refset Table directive
// e.g. <div refset-table value='PUBLISHED' />
tsApp
  .directive(
    'refsetTable',
    [
      '$uibModal',
      '$window',
      '$sce',
      '$interval',
      'utilService',
      'securityService',
      'projectService',
      'refsetService',
      'releaseService',
      'workflowService',
      'validationService',
      function($uibModal, $window, $sce, $interval, utilService, securityService, projectService,
        refsetService, releaseService, workflowService, validationService) {
        console.debug('configure refsetTable directive');
        return {
          restrict : 'A',
          scope : {
            // Legal 'value' settings include
            // For directory tab: PUBLISHED, BETA
            // For refset tab: AVAILABLE, ASSIGNED
            // RELEASE
            value : '@',
            projects : '=',
            metadata : '='
          },
          templateUrl : 'app/component/refsetTable/refsetTable.html',
          controller : [
            '$scope',
            function($scope) {

              // Variables
              $scope.user = securityService.getUser();
              $scope.userProjectsInfo = projectService.getUserProjectsInfo();
              $scope.selected = {
                refset : null,
                member : null,
                concept : null,
                terminology : null,
                version : null
              };
              $scope.refsetReleaseInfo = null;
              $scope.refsets = null;
              $scope.refsetLookupProgress = {};
              $scope.lookupInterval = null;
              $scope.project = null;
              $scope.cancelling = null;
              $scope.showLatest = true;

              // Page metadata
              $scope.memberTypes = [ 'Member', 'Exclusion', 'Inclusion', 'Active', 'Retired' ];

              // Used for project admin to know what users are assigned to
              // something.
              $scope.refsetAuthorsMap = {};
              $scope.refsetReviewersMap = {};

              // Paging variables
              $scope.pageSize = 10;
              $scope.paging = {};
              $scope.paging['refset'] = {
                page : 1,
                filter : '',
                sortField : $scope.value == 'ASSIGNED' ? 'refsetName' : 'name',
                ascending : null
              };
              $scope.paging['member'] = {
                page : 1,
                filter : '',
                typeFilter : '',
                sortField : $scope.value == 'PUBLISHED' || $scope.value == 'BETA' ? 'conceptName' : 'lastModified',
                ascending : true
              };
              $scope.paging['membersInCommon'] = {
                page : 1,
                filter : '',
                typeFilter : '',
                sortField : 'name',
                ascending : null
              };
              $scope.paging['oldRegularMembers'] = {
                page : 1,
                filter : '',
                typeFilter : '',
                sortField : 'name',
                ascending : null
              };
              $scope.paging['newRegularMembers'] = {
                page : 1,
                filter : '',
                typeFilter : '',
                sortField : 'name',
                ascending : null
              };

              $scope.ioImportHandlers = [];
              $scope.ioExportHandlers = [];

              // Refset Changed handler
              $scope.$on('refset:refsetChanged', function(event, data) {
                console.debug('on refset:refsetChanged', data);
                $scope.getRefsets();
              });

              // Project Changed Handler
              $scope.$on('refset:projectChanged', function(event, data) {
                console.debug('on refset:projectChanged', data);
                // Set project, refresh refset list
                $scope.setProject(data);
              });

              // link to error handling
              function handleError(errors, error) {
                utilService.handleDialogError(errors, error);
              }

              // Set $scope.project and reload
              // $scope.refsets
              $scope.setProject = function(project) {
                $scope.project = project;
                $scope.getRefsets();
                // $scope.projects.role already updated
              };

              // Get $scope.refsets
              // Logic for this depends on the $scope.value and
              // $scope.projects.role
              $scope.getRefsets = function() {
                var pfs = {
                  startIndex : ($scope.paging['refset'].page - 1) * $scope.pageSize,
                  maxResults : $scope.pageSize,
                  sortField : $scope.paging['refset'].sortField,
                  ascending : $scope.paging['refset'].ascending == null ? true
                    : $scope.paging['refset'].ascending,
                  queryRestriction : null,
                  latestOnly : false
                };

                if ($scope.value == 'PUBLISHED' || $scope.value == 'BETA') {
                  pfs.queryRestriction = 'workflowStatus:' + $scope.value;
                  pfs.latestOnly = $scope.showLatest;
                  refsetService.findRefsetsForQuery($scope.paging['refset'].filter, pfs).then(
                    function(data) {
                      $scope.refsets = data.refsets;
                      $scope.refsets.totalCount = data.totalCount;
                      $scope.reselect();
                    });
                }

                if ($scope.value == 'AVAILABLE' && $scope.projects.role == 'AUTHOR') {
                  pfs.queryRestriction = $scope.paging['refset'].filter;
                  workflowService.findAvailableEditingRefsets($scope.project.id,
                    $scope.user.userName, pfs).then(function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                    $scope.reselect();
                  });
                }
                if ($scope.value == 'AVAILABLE' && $scope.projects.role == 'REVIEWER') {
                  pfs.queryRestriction = $scope.paging['refset'].filter;
                  workflowService.findAvailableReviewRefsets($scope.project.id,
                    $scope.user.userName, pfs).then(function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                    $scope.reselect();
                  });
                }
                if ($scope.value == 'AVAILABLE' && $scope.projects.role == 'ADMIN') {
                  pfs.queryRestriction = $scope.paging['refset'].filter;
                  workflowService.findAllAvailableRefsets($scope.project.id, pfs).then(
                    function(data) {
                      $scope.refsets = data.refsets;
                      $scope.refsets.totalCount = data.totalCount;
                      $scope.reselect();
                    });
                }
                if ($scope.value == 'ASSIGNED' && $scope.projects.role == 'ADMIN') {
                  pfs.queryRestriction = $scope.paging['refset'].filter;
                  workflowService
                    .findAllAssignedRefsets($scope.project.id, pfs)
                    .then(
                      // Success
                      function(data) {
                        console.debug("X", data);
                        $scope.refsets = $scope.getRefsetsFromRecords(data.records);
                        $scope.refsets.totalCount = data.totalCount;
                        // get refset tracking records in order to get refset
                        // authors
                        for (var i = 0; i < data.records.length; i++) {
                          console.debug("Y", data.records[i]);
                          if (data.records[i].authors.length > 0) {
                            console.debug("Z", data.records[i].authors);
                            $scope.refsetAuthorsMap[data.records[i].refset.id] = data.records[i].authors;
                          }
                          if (data.records[i].reviewers.length > 0) {
                            console.debug("Z", data.records[i].reviewers);
                            $scope.refsetReviewersMap[data.records[i].refset.id] = data.records[i].reviewers;
                          }
                        }
                        $scope.reselect();
                      });
                }
                if ($scope.value == 'ASSIGNED' && $scope.projects.role == 'AUTHOR') {
                  pfs.queryRestriction = $scope.paging['refset'].filter;
                  workflowService.findAssignedEditingRefsets($scope.project.id,
                    $scope.user.userName, pfs).then(
                  // Success
                  function(data) {
                    $scope.refsets = $scope.getRefsetsFromRecords(data.records);
                    $scope.refsets.totalCount = data.totalCount;
                    $scope.reselect();
                  });
                }
                if ($scope.value == 'ASSIGNED' && $scope.projects.role == 'REVIEWER') {
                  pfs.queryRestriction = $scope.paging['refset'].filter;
                  workflowService.findAssignedReviewRefsets($scope.project.id,
                    $scope.user.userName, pfs).then(
                  // Success
                  function(data) {
                    $scope.refsets = $scope.getRefsetsFromRecords(data.records);
                    $scope.refsets.totalCount = data.totalCount;
                    $scope.reselect();
                  });
                }
                if ($scope.value == 'RELEASE') {
                  pfs.queryRestriction = 'projectId:'
                    + $scope.project.id
                    + ' AND revision:false AND (workflowStatus:READY_FOR_PUBLICATION OR workflowStatus:BETA  OR workflowStatus:PUBLISHED)';
                  pfs.latestOnly = $scope.showLatest;
                  refsetService.findRefsetsForQuery($scope.paging['refset'].filter, pfs).then(
                    function(data) {
                      $scope.refsets = data.refsets;
                      $scope.refsets.totalCount = data.totalCount;
                      $scope.reselect();
                    });
                }
              };

              // Convert an array of tracking records to an array of refsets.
              $scope.getRefsetsFromRecords = function(records) {
                var refsets = new Array();
                for (var i = 0; i < records.length; i++) {
                  refsets.push(records[i].refset);
                }
                return refsets;
              };

              // Reselect selected refset to refresh
              $scope.reselect = function() {
                // if there is a selection...
                // Bail if nothing selected
                if ($scope.selected.refset) {
                  // If $scope.selected.refset is in the list, select it, if not
                  // clear $scope.selected.refset
                  var found = false;
                  if ($scope.selected.refset) {
                    for (var i = 0; i < $scope.refsets.length; i++) {
                      if ($scope.selected.refset.id == $scope.refsets[i].id) {
                        $scope.selectRefset($scope.refsets[i]);
                        found = true;
                        break;
                      }
                    }
                  }
                  if (!found) {
                    $scope.selected.refset = null;
                    $scope.selected.concept = null;
                  }
                }

                // If 'lookup in progress' is set, get progress
                for (var i = 0; i < $scope.refsets.length; i++) {
                  if ($scope.refsets[i].lookupInProgress) {
                    $scope.refreshLookupProgress($scope.refsets[i]);
                  }
                }
              };

              // Get $scope.metadata.descriptionTypes
              $scope.getStandardDescriptionTypes = function(terminology, version) {
                projectService.getStandardDescriptionTypes(terminology, version).then(
                // Success
                function(data) {
                  // Populate 'selected' for refsetTable.html
                  // and metadata for addMember.html
                  $scope.selected.descriptionTypes = data.types;
                  $scope.metadata.descriptionTypes = data.types;
                });
              };

              // Get $scope.members
              $scope.getMembers = function(refset) {

                var pfs = {
                  startIndex : ($scope.paging['member'].page - 1) * $scope.pageSize,
                  maxResults : $scope.pageSize,
                  sortField : $scope.paging['member'].sortField,
                  ascending : $scope.paging['member'].ascending == null ? false
                    : $scope.paging['member'].ascending,
                  queryRestriction : null
                };

                if ($scope.paging['member'].typeFilter) {
                  var value = $scope.paging['member'].typeFilter;

                  // Handle inactive
                  if (value == 'Retired') {
                    pfs.queryRestriction = 'conceptActive:false';
                  } else if (value == 'Active') {
                    pfs.queryRestriction = 'conceptActive:true';
                  }

                  else {
                    // Handle member type

                    value = value.replace(' ', '_').toUpperCase();
                    pfs.queryRestriction = 'memberType:' + value;
                  }
                }

                refsetService.findRefsetMembersForQuery(refset.id, $scope.paging['member'].filter,
                  pfs).then(
                // Success
                function(data) {
                  refset.members = data.members;
                  refset.members.totalCount = data.totalCount;
                });

              };

              // Get $scope.refsetReleaseInfo
              $scope.getRefsetReleaseInfo = function(refset) {
                $scope.refsetReleaseInfo = null;
                var pfs = {
                  startIndex : -1,
                  maxResults : 10,
                  sortField : null,
                  ascending : null,
                  queryRestriction : null
                };
                releaseService.findRefsetReleasesForQuery(refset.id, null, pfs).then(
                  function(data) {
                    $scope.refsetReleaseInfo = data.releaseInfos[0];
                  });
              };

              // optimizes the definition
              $scope.optimizeDefinition = function(refset) {
                refsetService.optimizeDefinition(refset.id).then(function() {
                  refsetService.fireRefsetChanged(refset);
                });
              };

              // Convert date to a string
              $scope.toDate = function(lastModified) {
                return utilService.toDate(lastModified);

              };

              // Convert date to a string
              $scope.toShortDate = function(lastModified) {
                return utilService.toShortDate(lastModified);

              };

              // Table sorting mechanism
              $scope.setSortField = function(table, field, object) {

                // handle 'ASSIGNED' vs 'AVAILABLE' fields
                // refsetTable.html expresses the fields in terms of available
                var lfield = field;
                if (table == 'refset' && ($scope.value == 'ASSIGNED')) {
                  if (field == 'terminologyId') {
                    lfield = 'refsetId';
                  } else if (field == 'lastModified') {
                    lfield = 'lastModified';
                  } else {
                    // uppercase and prepend refset in all other cases
                    lfield = 'refset' + field.charAt(0).toUpperCase() + field.slice(1);
                  }
                }

                utilService.setSortField(table, lfield, $scope.paging);
                // retrieve the correct table
                if (table === 'refset') {
                  $scope.getRefsets();
                }
                if (table === 'member') {
                  $scope.getMembers(object);
                }
              };

              // Return up or down sort chars if sorted
              $scope.getSortIndicator = function(table, field) {
                var lfield = field;
                if (table == 'refset' && ($scope.value == 'ASSIGNED')) {
                  if (field == 'terminologyId') {
                    lfield = 'refsetId';
                  } else if (field == 'lastModified') {
                    lfield = 'lastModified';
                  } else {
                    // uppercase and prepend refset in all other cases
                    lfield = 'refset' + field.charAt(0).toUpperCase() + field.slice(1);
                  }
                }
                return utilService.getSortIndicator(table, lfield, $scope.paging);
              };

              // Selects a refset (setting $scope.selected.refset).
              // Looks up current release info and members.
              $scope.selectRefset = function(refset) {
                $scope.selected.refset = refset;
                $scope.selected.terminology = refset.terminology;
                $scope.selected.version = refset.version;
                $scope.getRefsetReleaseInfo(refset);
                $scope.getMembers(refset);
                $scope.getStandardDescriptionTypes(refset.terminology, refset.version);
              };

              // Selects a member (setting $scope.selected.member)
              $scope.selectMember = function(member) {
                $scope.selected.member = member;
                // Set the concept for display in concept-info
                $scope.selected.concept = {
                  terminologyId : member.conceptId,
                  terminology : member.terminology,
                  version : member.version
                };

              };

              // Member type style
              $scope.getMemberStyle = function(member) {
                if (member.memberType == 'MEMBER') {
                  return '';
                }
                return member.memberType.replace('_STAGED', '');
              };

              // Remove a refset
              $scope.removeRefset = function(refset) {
                workflowService.findAllAssignedRefsets($scope.project.id, {
                  startIndex : 0,
                  maxResults : 1,
                  queryRestriction : 'refsetId:' + refset.id
                }).then(
                  // Success
                  function(data) {
                    if (data.records.length > 0
                      && !$window
                        .confirm('The refset is assigned, are you sure you want to proceed?')) {
                      return;
                    }
                    $scope.removeRefsetHelper(refset);
                  });
              };

              // Helper for removing a refest
              $scope.removeRefsetHelper = function(refset) {

                refsetService.findRefsetMembersForQuery(refset.id, '', {
                  startIndex : 0,
                  maxResults : 1
                }).then(
                  function(data) {
                    if (data.members.length == 1) {
                      if (!$window
                        .confirm('The refset has members, are you sure you want to proceed.')) {
                        return;
                      }
                    }
                    refsetService.removeRefset(refset.id).then(function() {
                      $scope.selected.refset = null;
                      refsetService.fireRefsetChanged();
                    });
                  });
              };

              // Remove refset member
              $scope.removeRefsetMember = function(refset, member) {

                refsetService.removeRefsetMember(member.id).then(
                // Success
                function() {
                  $scope.selected.concept = null;
                  $scope.handleWorkflow(refset);
                });
              };
              // Remove refset inclusion
              $scope.removeRefsetInclusion = function(refset, member) {

                refsetService.removeRefsetMember(member.id).then(
                // Success
                function() {
                  $scope.handleWorkflow(refset);
                });
              };

              // Adds a refset exclusion and refreshes member
              // list with current PFS settings
              $scope.addRefsetExclusion = function(refset, member) {
                refsetService.addRefsetExclusion(refset, member.conceptId, false).then(function() {
                  $scope.handleWorkflow(refset);
                });

              };

              // Remove refset exclusion and refreshes members
              $scope.removeRefsetExclusion = function(refset, member) {
                refsetService.removeRefsetExclusion(member.id).then(function() {
                  $scope.handleWorkflow(refset);
                });

              };

              // Unassign refset from user
              $scope.unassign = function(refset, userName) {
                $scope.performWorkflowAction(refset, 'UNASSIGN', userName);
              };

              // handle workflow advancement
              $scope.handleWorkflow = function(refset) {
                if ($scope.value == 'ASSIGNED'
                  && refset
                  && (refset.workflowStatus == 'NEW' || refset.workflowStatus == 'READY_FOR_PUBLICATION')) {
                  $scope.performWorkflowAction(refset, 'SAVE', $scope.user.userName);
                } else {
                  refsetService.fireRefsetChanged(refset);
                }
              };

              // Performs a workflow action
              $scope.performWorkflowAction = function(refset, action, userName) {

                workflowService.performWorkflowAction($scope.project.id, refset.id, userName,
                  $scope.projects.role, action).then(function(data) {
                  refsetService.fireRefsetChanged(data);
                });
              };

              // Removes all refset members
              $scope.removeAllRefsetMembers = function(refset) {
                refsetService.removeAllRefsetMembers(refset.id).then(function(data) {
                  refsetService.fireRefsetChanged(refset);
                });
              };

              // Exports a release artifact (and begins the
              // download)
              $scope.exportReleaseArtifact = function(artifact) {
                releaseService.exportReleaseArtifact(artifact);
              };

              // Directive scoped method for cancelling an import/migration
              $scope.cancelAction = function(refset) {
                $scope.cancelling = true;
                if (refset.stagingType == 'IMPORT') {
                  refsetService.cancelImportMembers(refset.id).then(
                  // Success
                  function() {
                    $scope.cancelling = false;
                    refsetService.fireRefsetChanged(refset);
                  },
                  // Error
                  function() {
                    $scope.cancelling = false;
                  });
                }
                if (refset.stagingType == 'MIGRATION') {

                  refsetService.cancelMigration(refset.id).then(
                  // Success
                  function(data) {
                    // Some local management of refset state to avoid
                    // a million callbacks to the server while
                    // startLookup is running
                    refset.staged = false;
                    refset.stagingType = null;
                    // If INTENSIONAL, we need to re-look up old/not/new members
                    if (refset.type == 'INTENSIONAL') {
                      refset.lookupInProgress = true;
                      startLookup(refset);
                    }
                    $scope.cancelling = false;
                    // refsetService.fireRefsetChanged($scope.refset);
                  },
                  // Error
                  function() {
                    $scope.cancelling = false;
                  });

                }
                if (refset.stagingType == 'BETA') {
                  releaseService.cancelRefsetRelease($scope.refset.id).then(
                  // Success
                  function() {
                    $scope.cancelling = false;
                    refsetService.fireRefsetChanged($scope.refset);
                  },
                  // Error
                  function() {
                    $scope.cancelling = false;
                  });
                }
              };

              // cancelling a release given the staged refset
              $scope.cancelActionForStaged = function(refest) {
                if (stagedRefset.workflowStatus == 'BETA') {
                  refsetService.getOriginForStagedRefsetId(refset.id).then(
                  // Success
                  function(data) {
                    refsetService.getRefset(data).then(
                    // Success
                    function(data) {
                      $scope.cancelAction(data);
                    });
                  });
                }
              };

              // Start lookup again - not $scope because modal must access it
              function startLookup(refset) {
                refsetService.startLookup(refset.id).then(
                // Success
                function(data) {
                  $scope.refsetLookupProgress[refset.id] = 1;
                  // Start if not already running
                  if (!$scope.lookupInterval) {
                    $scope.lookupInterval = $interval(function() {
                      $scope.refreshLookupProgress(refset);
                    }, 2000);
                  }
                });
              }

              // Refresh lookup progress
              $scope.refreshLookupProgress = function(refset) {
                refsetService.getLookupProgress(refset.id).then(
                // Success
                function(data) {
                  if (data === "100" || data == 100) {
                    refset.lookupInProgress = false;
                  }
                  $scope.refsetLookupProgress[refset.id] = data;
                  // If all lookups in progress are at 100%, stop interval
                  var found = true;
                  for ( var key in $scope.refsetLookupProgress) {
                    if ($scope.refsetLookupProgress[key] < 100) {
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
              $scope.getLatestNote = function(refset) {
                if (refset && refset.notes && refset.notes.length > 0) {
                  return $sce.trustAsHtml(refset.notes
                    .sort(utilService.sort_by('lastModified', -1))[0].value);
                }
                return $sce.trustAsHtml('');
              };

              // Initialize if project setting isn't used
              if ($scope.value == 'BETA' || $scope.value == 'PUBLISHED') {
                $scope.getRefsets();
              }

              //
              // MODALS
              //

              // Definition clauses modal
              $scope.openDefinitionClausesModal = function(lrefset, lvalue) {
                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/definitionClauses.html',
                  controller : DefinitionClausesModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
                    },
                    value : function() {
                      return lvalue;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.handleWorkflow(data);
                });

              };

              // Definition clauses controller
              var DefinitionClausesModalCtrl = function($scope, $uibModalInstance, refset, value) {
                console.debug('Entered definition clauses modal control', refset, value);

                $scope.refset = refset;
                $scope.value = value;
                $scope.newClause = null;

                // Paging parameters
                $scope.newClauses = angular.copy($scope.refset.definitionClauses);
                $scope.pageSize = 5;
                $scope.pagedClauses = [];
                $scope.paging = {};
                $scope.paging['clauses'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.errors = [];

                // Get paged clauses (assume all are loaded)
                $scope.getPagedClauses = function() {
                  $scope.pagedClauses = utilService.getPagedArray($scope.newClauses,
                    $scope.paging['clauses'], $scope.pageSize);
                };

                // identify whether defintion has changed
                $scope.isDefinitionDirty = function() {
                  if ($scope.newClauses.length != $scope.refset.definitionClauses.length) {
                    return true;
                  }

                  // Compare scope.refset.definitionClauses to newClauses
                  for (var i = 0; i < $scope.newClauses.length; i++) {
                    if ($scope.newClauses[i].value != $scope.refset.definitionClauses[i].value) {
                      return true;
                    }
                    if ($scope.newClauses[i].negated != $scope.refset.definitionClauses[i].negated) {
                      return true;
                    }
                  }
                  return false;
                };

                // remove clause
                $scope.removeClause = function(refset, clause) {
                  for (var i = 0; i < $scope.newClauses.length; i++) {
                    var index = $scope.newClauses.indexOf(clause);
                    if (index != -1) {
                      $scope.newClauses.splice(index, 1);
                    }
                  }
                  $scope.getPagedClauses();
                };

                // add new clause
                $scope.addClause = function(refset, clause) {
                  $scope.errors = [];

                  // Confirm clauses are unique, skip if not
                  for (var i = 0; i < $scope.newClauses.length; i++) {
                    if ($scope.newClauses[i].value == clause.value) {
                      $scope.errors[0] = 'Duplicate definition clause';
                      return;
                    }
                  }
                  refsetService.isExpressionValid(clause.value, refset.terminology, refset.version)
                    .then(
                    // Success - add refset
                    function(data) {
                      if (data == 'true') {
                        $scope.newClauses.push(clause);
                        $scope.getPagedClauses();
                        $scope.newClause = null;
                      } else {
                        $scope.errors[0] = 'Submitted definition clause is invalid';
                        return;
                      }
                    },
                    // Error - add refset
                    function(data) {
                      handleError($scope.errors, data);
                    });
                };

                // Save refset
                $scope.save = function(refset) {
                  refset.definitionClauses = $scope.newClauses;
                  refsetService.updateRefset(refset).then(
                  // Success - add refset
                  function(data) {
                    $uibModalInstance.close(refset);
                  },
                  // Error - add refset
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

                // initialize modal
                $scope.getPagedClauses();
              };

              // Notes modal
              $scope.openNotesModal = function(lobject, ltype) {
                console.debug('openNotesModal ', lobject, ltype);

                var modalInstance = $uibModal.open({
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
                  $scope.handleWorkflow(data);
                });

              };

              // Notes controller
              var NotesModalCtrl = function($scope, $uibModalInstance, $sce, object, type,
                tinymceOptions) {
                console.debug('Entered notes modal control', object, type);
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
                $scope.errors = [];

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

                  if ($scope.type == 'Refset') {
                    refsetService.removeRefsetNote(object.id, note.id).then(
                    // Success - add refset
                    function(data) {
                      $scope.newNote = null;
                      refsetService.getRefset(object.id).then(function(data) {
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
                  } else if ($scope.type == 'Member') {
                    refsetService.removeRefsetMemberNote(object.id, note.id).then(
                    // Success - add refset
                    function(data) {
                      $scope.newNote = null;
                      refsetService.getMember(object.id).then(function(data) {
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

                // add new note
                $scope.submitNote = function(object, text) {

                  if ($scope.type == 'Refset') {
                    refsetService.addRefsetNote(object.id, text).then(
                    // Success - add refset
                    function(data) {
                      $scope.newNote = null;
                      refsetService.getRefset(object.id).then(function(data) {
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
                  } else if ($scope.type == 'Member') {
                    refsetService.addRefsetMemberNote(object.refsetId, object.id, text).then(
                    // Success - add refset
                    function(data) {
                      $scope.newNote = null;

                      refsetService.getMember(object.id).then(function(data) {
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

              // Clone Refset modal
              $scope.openCloneRefsetModal = function(lrefset) {
                console.debug('cloneRefsetModal ', lrefset);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/editRefset.html',
                  controller : CloneRefsetModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
                    },
                    metadata : function() {
                      return $scope.metadata;
                    },
                    projects : function() {
                      return $scope.projects;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.handleWorkflow(data);
                });

              };

              // Clone Refset controller
              var CloneRefsetModalCtrl = function($scope, $uibModalInstance, refset, metadata,
                projects) {
                console.debug('Entered clone refset modal control', refset, projects);

                $scope.action = 'Clone';
                $scope.projects = projects;
                $scope.metadata = metadata;
                $scope.versions = metadata.versions[metadata.terminologies[0]].sort().reverse();
                // Copy refset and clear terminology id
                $scope.refset = JSON.parse(JSON.stringify(refset));
                $scope.refset.terminologyId = null;
                $scope.errors = [];

                $scope.submitRefset = function(refset) {

                  if (!refset.project) {
                    $scope.errors[0] = 'A project must be chosen from the picklist.';
                    return;
                  }
                  // validate refset before cloning it
                  validationService.validateRefset(refset).then(
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
                      if (data.warnings && data.warnings.length > 0
                        && $scope.warnings.join() !== data.warnings.join()) {
                        $scope.warnings = data.warnings;
                        return;
                      } else {
                        $scope.warnings = [];
                      }

                      refsetService.cloneRefset(refset.project.id, refset).then(
                      // Success - clone refset
                      function(data) {
                        var newRefset = data;
                        $uibModalInstance.close(newRefset);
                      },
                      // Error - clone refset
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - validate
                    function(data) {
                      handleError($scope.errors, data);
                    });
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Import/Export modal
              $scope.openImportExportModal = function(lrefset, loperation, ltype) {
                console.debug('exportModal ', lrefset);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/importExport.html',
                  controller : ImportExportModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
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
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  if (loperation == 'Import') {
                    $scope.handleWorkflow(data);
                  } else {
                    refsetService.fireRefsetChanged(data);
                  }
                });
              };

              // Import/Export controller
              var ImportExportModalCtrl = function($scope, $uibModalInstance, refset, operation,
                type, ioHandlers) {
                console.debug('Entered import export modal control', refset.id, ioHandlers,
                  operation, type);

                $scope.refset = refset;
                $scope.ioHandlers = ioHandlers;
                $scope.selectedIoHandler = null;
                for (var i = 0; i < ioHandlers.length; i++) {
                  // Choose first one if only one
                  if ($scope.selectedIoHandler == null) {
                    $scope.selectedIoHandler = ioHandlers[i];
                  }
                  // choose 'rf2' as default otherwise
                  if (ioHandlers[i].name.endsWith('RF2')) {
                    $scope.selectedIoHandler = ioHandlers[i];
                  }
                }
                $scope.type = type;
                $scope.operation = operation;
                $scope.comments = [];
                $scope.warnings = [];
                $scope.errors = [];

                // Handle export
                $scope.export = function(file) {
                  if (type == 'Definition') {
                    refsetService.exportDefinition($scope.refset, $scope.selectedIoHandler.id,
                      $scope.selectedIoHandler.fileTypeFilter);
                  }
                  if (type == 'Refset Members') {
                    refsetService.exportMembers($scope.refset, $scope.selectedIoHandler.id,
                      $scope.selectedIoHandler.fileTypeFilter);
                  }
                  $uibModalInstance.close(refset);
                };

                // Handle import
                $scope.import = function(file) {

                  if (type == 'Definition') {
                    refsetService.importDefinition($scope.refset.id, $scope.selectedIoHandler.id,
                      file).then(
                    // Success - close dialog
                    function(data) {
                      $uibModalInstance.close(refset);
                    },
                    // Failure - show error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }

                  if (type == 'Refset Members') {
                    refsetService.beginImportMembers($scope.refset.id, $scope.selectedIoHandler.id)
                      .then(

                        // Success
                        function(data) {
                          // data is a validation result, check for errors
                          if (data.errors.length > 0) {
                            $scope.errors = data.errors;
                          } else {

                            // If there are no errors, finish import
                            refsetService.finishImportMembers($scope.refset.id,
                              $scope.selectedIoHandler.id, file).then(
                            // Success - close dialog
                            function(data) {
                              $scope.comments = data.comments;
                              $scope.warnings = data.warnings;
                              $scope.errors = data.errors;
                              startLookup(refset);
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

                  if (type == 'Refset Members') {
                    refsetService.finishImportMembers($scope.refset.id,
                      $scope.selectedIoHandler.id, file).then(
                    // Success - close dialog
                    function(data) {
                      $scope.comments = data.comments;
                      $scope.warnings = data.warnings;
                      $scope.errors = data.errors;
                      startLookup(refset);
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
                  if ($scope.errors.length > 0 && type == 'Refset Members') {
                    refsetService.cancelImportMembers($scope.refset.id);
                  }
                  // close the dialog and reload refsets
                  $uibModalInstance.close();
                };

                $scope.close = function() {
                  // close the dialog and reload refsets
                  $uibModalInstance.close();
                };
              };

              // Release Process modal
              $scope.openReleaseProcessModal = function(lrefset) {
                console.debug('releaseProcessModal ', lrefset);
                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/release.html',
                  controller : ReleaseProcessModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
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
                  refsetService.fireRefsetChanged(data);
                });
              };

              // Open release process modal given staged refset
              $scope.openReleaseProcessModalForStaged = function(stagedRefset) {

                refsetService.getOriginForStagedRefsetId(stagedRefset.id).then(
                // Success
                function(data) {
                  refsetService.getRefset(data).then(
                  // Success
                  function(data) {
                    $scope.openReleaseProcessModal(data);
                  });
                });
              };

              // Release Process controller
              var ReleaseProcessModalCtrl = function($scope, $uibModalInstance, refset, ioHandlers,
                utilService) {
                console.debug('Entered release process modal', refset.id, ioHandlers);

                $scope.refset = refset;
                $scope.ioHandlers = ioHandlers;
                $scope.selectedIoHandler = $scope.ioHandlers[0];
                $scope.releaseInfo = [];
                $scope.validationResult = null;
                $scope.format = 'yyyyMMdd';
                $scope.releaseDate = utilService.toSimpleDate($scope.refset.effectiveTime);
                $scope.status = {
                  opened : false
                };
                $scope.errors = [];

                if (refset.stagingType == 'BETA') {
                  releaseService.resumeRelease(refset.id).then(
                  // Success
                  function(data) {
                    $scope.stagedRefset = data;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                }

                $scope.beginRefsetRelease = function(refset) {

                  releaseService.beginRefsetRelease(refset.id,
                    utilService.toSimpleDate(refset.effectiveTime)).then(
                  // Success
                  function(data) {
                    $scope.releaseInfo = data;
                    $scope.refset.inPublicationProcess = true;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                $scope.validateRefsetRelease = function(refset) {

                  releaseService.validateRefsetRelease(refset.id).then(
                  // Success
                  function(data) {
                    $scope.validationResult = data;
                    refsetService.fireRefsetChanged(refset);
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                $scope.betaRefsetRelease = function(refset) {

                  releaseService.betaRefsetRelease(refset.id, $scope.selectedIoHandler.id).then(
                  // Success
                  function(data) {
                    $scope.stagedRefset = data;
                    $uibModalInstance.close($scope.stagedRefset);
                    alert('The BETA refset has been added .');
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                $scope.finishRefsetRelease = function(refset) {

                  releaseService.finishRefsetRelease(refset.id, $scope.selectedIoHandler.id).then(
                  // Success
                  function(data) {
                    $uibModalInstance.close(refset);
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Cancel release process and dismiss modal
                $scope.cancel = function() {
                  releaseService.cancelRefsetRelease($scope.refset.id);
                  $uibModalInstance.dismiss('cancel');

                };

                // Close the window - to return later
                $scope.close = function() {
                  $uibModalInstance.close();
                };

                $scope.open = function($event) {
                  $scope.status.opened = true;
                };

                $scope.format = 'yyyyMMdd';
              };

              // Assign refset modal
              $scope.openAssignRefsetModal = function(lrefset, laction, lrole) {
                console.debug('openAssignRefsetModal ', lrefset, laction);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/assignRefset.html',
                  controller : AssignRefsetModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
                    },
                    action : function() {
                      return laction;
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
                      if (lrole) {
                        return lrole;
                      } else {
                        return $scope.projects.role;
                      }
                    },
                    tinymceOptions : function() {
                      return utilService.tinymceOptions;
                    }
                  }

                });

                modalInstance.result.then(
                // Success
                function(data) {
                  refsetService.fireRefsetChanged(data);
                });
              };

              // Assign refset controller
              var AssignRefsetModalCtrl = function($scope, $uibModalInstance, $sce, refset, action,
                currentUser, assignedUsers, project, role, tinymceOptions) {
                console.debug('Entered assign refset modal control', assignedUsers, project.id);
                $scope.refset = refset;
                $scope.action = action;
                $scope.project = project;
                $scope.role = role;
                $scope.tinymceOptions = tinymceOptions;
                $scope.assignedUsers = [];
                $scope.user = utilService.findBy(assignedUsers, currentUser, 'userName');
                $scope.note;
                $scope.errors = [];

                // Sort users by name and role restricts
                var sortedUsers = assignedUsers.sort(utilService.sort_by('name'));
                for (var i = 0; i < sortedUsers.length; i++) {
                  if ($scope.role == 'AUTHOR'
                    || $scope.project.userRoleMap[sortedUsers[i].userName] == 'REVIEWER'
                    || $scope.project.userRoleMap[sortedUsers[i].userName] == 'ADMIN') {
                    $scope.assignedUsers.push(sortedUsers[i]);
                  }
                }

                // Assign (or reassign)
                $scope.assignRefset = function() {
                  if (!$scope.user) {
                    $scope.errors[0] = 'The user must be selected. ';
                    return;
                  }

                  if (action == 'ASSIGN') {
                    workflowService.performWorkflowAction($scope.project.id, refset.id,
                      $scope.user.userName, $scope.role, 'ASSIGN').then(
                    // Success
                    function(data) {

                      // Add a note as well
                      if ($scope.note) {
                        refsetService.addRefsetNote(refset.id, $scope.note).then(
                        // Success
                        function(data) {
                          $uibModalInstance.close(refset);
                        },
                        // Error
                        function(data) {
                          handleError($scope.errors, data);
                        });
                      }
                      // close dialog if no note
                      else {
                        $uibModalInstance.close(refset);
                      }

                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }

                  // else
                  if (action == 'REASSIGN') {
                    workflowService.performWorkflowAction($scope.project.id, refset.id,
                      $scope.user.userName, $scope.role, 'UNASSIGN').then(
                      // Success - unassign
                      function(data) {
                        // The username doesn't matter - it'll go back to the
                        // author
                        workflowService.performWorkflowAction($scope.project.id, refset.id,
                          $scope.user.userName, 'AUTHOR', 'REASSIGN').then(
                        // success - reassign
                        function(data) {
                          // Add a note as well
                          if ($scope.note) {
                            refsetService.addRefsetNote(refset.id, $scope.note).then(
                            // Success - add note
                            function(data) {
                              $uibModalInstance.close(refset);
                            },
                            // Error - remove note
                            function(data) {
                              handleError($scope.errors, data);
                            });
                          }
                          // close dialog if no note
                          else {
                            $uibModalInstance.close(refset);
                          }
                        },
                        // Error - reassign
                        function(data) {
                          handleError($scope.errors, data);
                        });
                      },
                      // Error - unassign
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

              // Log modal
              $scope.openLogModal = function() {
                console.debug('openLogModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/log.html',
                  controller : LogModalCtrl,
                  backdrop : 'static',
                  size : 'lg',
                  resolve : {
                    refset : function() {
                      return $scope.selected.refset;
                    },
                    project : function() {
                      return $scope.project;
                    }
                  }
                });

                // NO need for result function - no action on close
                // modalInstance.result.then(function(data) {});
              };

              // Log controller
              var LogModalCtrl = function($scope, $uibModalInstance, refset, project) {
                console.debug('Entered log modal control', refset, project);

                $scope.errors = [];
                $scope.warnings = [];

                // Get log to display
                $scope.getLog = function() {
                  projectService.getLog(project.id, refset.id).then(
                  // Success
                  function(data) {
                    $scope.log = data;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                // Dismiss modal
                $scope.close = function() {
                  // nothing changed, don't pass a refset
                  $uibModalInstance.close();
                };

                // initialize
                $scope.getLog();
              };

              // Add Refset Member List modal
              $scope.openAddRefsetMemberListModal = function() {
                console.debug('openAddRefsetMemberListModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/addMemberList.html',
                  controller : AddRefsetMemberListModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return $scope.selected.refset;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.handleWorkflow(data);
                });
              };

              // Add Refset Member List controller
              var AddRefsetMemberListModalCtrl = function($scope, $uibModalInstance, refset) {
                console.debug('Entered add refset member list modal control', refset);

                $scope.refsetMemberList = '';
                $scope.errors = [];
                $scope.warnings = [];
                $scope.comments = [];
                $scope.memberIdList = '';
                $scope.ids = [];
                $scope.added = [];
                $scope.exists = [];
                $scope.removed = [];
                $scope.notExists = [];

                // Used for enabling/disabling in UI
                $scope.hasResults = function() {
                  return $scope.added.length > 0 || $scope.removed.length > 0
                    || $scope.exists.length > 0 || $scope.notExists.length > 0;
                };

                // Add members in the list
                $scope.includeMembers = function() {
                  $scope.errors = [];
                  $scope.ids = getIds($scope.memberIdList);
                  for (var i = 0; i < $scope.ids.length; i++) {
                    var conceptId = $scope.ids[i];
                    includeMember(refset, conceptId);
                  }
                };

                // find member and add if not exists
                function includeMember(refset, conceptId) {
                  refsetService.findRefsetMembersForQuery(refset.id, 'conceptId:' + conceptId, {
                    startIndex : 0,
                    maxResults : 1
                  }).then(
                  // Success
                  function(data) {
                    if (data.members.length > 0) {
                      $scope.exists.push(conceptId);
                    } else {
                      var member = {
                        active : true,
                        conceptId : conceptId,
                        memberType : 'MEMBER',
                        moduleId : refset.moduleId,
                        refsetId : refset.id
                      };
                      refsetService.addRefsetMember(member).then(
                      // Success
                      function(data) {
                        $scope.added.push(conceptId);
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    }
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                }

                // Exclude members in list
                $scope.excludeMembers = function() {
                  $scope.errors = [];
                  $scope.ids = getIds($scope.memberIdList);
                  var notExists = new Array();
                  var removed = new Array();
                  for (var i = 0; i < $scope.ids.length; i++) {
                    var conceptId = $scope.ids[i];
                    removeMember(refset, conceptId);
                  }
                };

                // validation
                function removeMember(refset, conceptId) {
                  refsetService.findRefsetMembersForQuery(refset.id, 'conceptId:' + conceptId, {
                    startIndex : 0,
                    maxResults : 1
                  }).then(
                  // Success
                  function(data) {

                    if (data.members.length == 0) {
                      $scope.notExists.push(conceptId);
                    } else {

                      var memberId = data.members[0].id;
                      refsetService.removeRefsetMember(memberId).then(
                      // Success
                      function(data) {
                        $scope.removed.push(conceptId);
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    }
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                }

                // Get the ids from the value
                function getIds(value) {
                  // Split on punctuation
                  var list = $scope.memberIdList.split(/[\s;,\.]/);

                  var result = new Array();
                  // remove empty stuff
                  for (var i = 0; i < list.length; i++) {
                    if (list[i]) {
                      result.push(list[i]);
                    }
                  }
                  return result;
                }

                // Dismiss modal
                $scope.close = function() {
                  $uibModalInstance.close(refset);
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Add Refset modal
              $scope.openAddRefsetModal = function() {
                console.debug('openAddRefsetModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/editRefset.html',
                  controller : AddRefsetModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    metadata : function() {
                      return $scope.metadata;
                    },
                    project : function() {
                      return $scope.project;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  refsetService.fireRefsetChanged(data);
                });
              };

              // Add Refset controller
              var AddRefsetModalCtrl = function($scope, $uibModalInstance, metadata, project) {
                console.debug('Entered add refset modal control', metadata);

                $scope.action = 'Add';
                $scope.definition = null;
                $scope.metadata = metadata;
                $scope.project = project;
                $scope.versions = metadata.versions[metadata.terminologies[0]].sort().reverse();
                $scope.clause = {
                  value : null
                };
                $scope.refset = {
                  workflowPath : metadata.workflowPaths[0],
                  terminology : metadata.terminologies[0],
                  version : $scope.versions[0],
                  namespace : $scope.project.namespace,
                  moduleId : $scope.project.moduleId,
                  organization : $scope.project.organization,
                  terminology : $scope.project.terminology,
                  feedbackEmail : $scope.project.feedbackEmail,
                  type : metadata.refsetTypes[0],
                  definitionClauses : []
                };
                $scope.errors = [];
                $scope.warnings = [];

                // lookup versions
                $scope.terminologySelected = function(terminology) {
                  $scope.versions = metadata.versions[terminology].sort().reverse();
                };

                $scope.submitRefset = function(refset) {

                  refset.projectId = project.id;
                  // Setup definition if configured
                  if (refset.type == 'EXTENSIONAL') {
                    $scope.clause = null;
                  }
                  if ($scope.clause && $scope.clause.value) {
                    refset.definitionClauses = [ {
                      value : $scope.clause.value,
                      negated : false
                    } ];
                  }

                  // validate refset before adding it
                  validationService
                    .validateRefset(refset)
                    .then(
                      function(data) {

                        // If there are errors, make them available and stop.
                        if (data.errors && data.errors.length > 0) {
                          $scope.errors = data.errors;
                          return;
                        } else {
                          $scope.errors = [];
                        }

                        // if $scope.warnings is empty, and data.warnings is
                        // not,
                        // show warnings and stop
                        if (data.warnings && data.warnings.length > 0
                          && $scope.warnings.join() !== data.warnings.join()) {
                          $scope.warnings = data.warnings;
                          return;
                        } else {
                          $scope.warnings = [];
                        }

                        if (!refset.name || !refset.description || !refset.moduleId) {
                          $scope.errors[0] = 'Refset name, description and moduleId must not be empty.';
                          return;
                        }

                        // Success - validate refset
                        refsetService.addRefset(refset).then(
                        // Success - add refset
                        function(data) {
                          var newRefset = data;
                          $uibModalInstance.close(newRefset);
                        },
                        // Error - add refset
                        function(data) {
                          handleError($scope.errors, data);
                        });

                      },
                      // Error - validate refset
                      function(data) {
                        handleError($scope.errors, data);
                      });
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Edit refset modal
              $scope.openEditRefsetModal = function(lrefset) {
                console.debug('openEditRefsetModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/editRefset.html',
                  controller : EditRefsetModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
                    },
                    metadata : function() {
                      return $scope.metadata;
                    },
                    project : function() {
                      return $scope.project;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  // handle workflow advancement
                  $scope.handleWorkflow(data);
                });
              };

              // Edit refset controller
              var EditRefsetModalCtrl = function($scope, $uibModalInstance, refset, metadata,
                project) {
                console.debug('Entered edit refset modal control');

                $scope.action = 'Edit';
                $scope.refset = refset;
                $scope.project = project;
                $scope.metadata = metadata;
                $scope.versions = $scope.metadata.versions[refset.terminology].sort().reverse();
                $scope.errors = [];

                $scope.terminologySelected = function(terminology) {
                  $scope.versions = $scope.metadata.versions[terminology].sort().reverse();
                };

                $scope.submitRefset = function(refset) {

                  // Validate refset
                  validationService.validateRefset(refset).then(
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
                      if (data.warnings && data.warnings.length > 0
                        && $scope.warnings.join() !== data.warnings.join()) {
                        $scope.warnings = data.warnings;
                        return;
                      } else {
                        $scope.warnings = [];
                      }

                      // Success - validate refset
                      refsetService.updateRefset(refset).then(
                      // Success - update refset
                      function(data) {
                        $uibModalInstance.close(refset);
                      },
                      // Error - update refset
                      function(data) {
                        handleError($scope.errors, data);
                      });

                    },
                    // Error - validate refset
                    function(data) {
                      handleError($scope.errors, data);
                    });

                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              $scope.openAddMemberModal = function(lrefset) {
                console.debug('openAddMemberModal ', lrefset);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/addMember.html',
                  controller : AddMemberModalCtrl,
                  backdrop : 'static',
                  size : 'lg',
                  resolve : {
                    metadata : function() {
                      return $scope.metadata;
                    },
                    refset : function() {
                      return lrefset;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    value : function() {
                      return $scope.value;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.handleWorkflow(data);
                });
              };

              // Add member controller
              var AddMemberModalCtrl = function($scope, $uibModalInstance, metadata, refset,
                project, value) {
                console.debug('Entered add member modal control');
                $scope.value = value;
                $scope.pageSize = 10;
                $scope.searchResults = null;
                $scope.data = {
                  concept : null,
                  descriptionTypes : metadata.descriptionTypes,
                  terminology : refset.terminology,
                  version : refset.version,
                  refset : refset,
                  memberTypes : {}
                };
                $scope.pageSize = 10;
                $scope.paging = {};
                $scope.paging['search'] = {
                  page : 1,
                  filter : '',
                  sortField : null,
                  ascending : null
                };
                $scope.errors = [];

                if (refset.type == 'EXTENSIONAL') {
                  $scope.memberType = 'MEMBER';
                }
                if (refset.type == 'INTENSIONAL') {
                  $scope.memberType = 'INCLUSION';
                }

                $scope.addMember = function(concept) {

                  var member = {
                    active : true,
                    conceptId : concept.terminologyId,
                    conceptName : concept.name,
                    conceptActive : concept.active,
                    memberType : $scope.memberType,
                    moduleId : refset.moduleId,
                  };
                  member.refsetId = refset.id;

                  // validate member before adding it
                  validationService.validateMember(member, project.id).then(
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

                      // Success - validate refset
                      if (member.memberType == 'MEMBER') {

                        refsetService.addRefsetMember(member).then(
                        // Success
                        function(data) {
                          $scope.data.memberTypes[concept.terminologyId] = member;
                        },
                        // Error
                        function(data) {
                          handleError($scope.errors, data);
                        });
                      }

                      if (member.memberType == 'INCLUSION') {
                        refsetService.addRefsetInclusion(member, false).then(
                        // Success
                        function(data) {
                          $scope.data.memberTypes[concept.terminologyId] = member;
                        },
                        // Error
                        function(data) {
                          handleError($scope.errors, data);
                        });

                      }
                    },
                    // Error - validate refset
                    function(data) {
                      handleError($scope.errors, data);
                    });

                };

                // get search results
                $scope.getSearchResults = function(search, clearPaging) {

                  if (clearPaging) {
                    $scope.paging['search'].page = 1;
                  }

                  // skip search if blank
                  if (!search) {
                    return;
                  }
                  // clear data structures
                  $scope.errors = [];

                  var pfs = {
                    startIndex : ($scope.paging['search'].page - 1) * $scope.pageSize,
                    maxResults : $scope.pageSize,
                    sortField : null,
                    ascending : null,
                    queryRestriction : null
                  };

                  projectService.findConceptsForQuery(search, refset.terminology, refset.version,
                    pfs).then(
                  // Success
                  function(data) {
                    $scope.searchResults = data.concepts;
                    $scope.searchResults.totalCount = data.totalCount;
                    $scope.getMemberTypes();
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                // select concept and get concept data
                $scope.selectConcept = function(concept) {
                  $scope.data.concept = concept;
                };

                // Gets $scope.data.memberTypes
                $scope.getMemberTypes = function() {
                  var concepts = new Array();
                  for (var i = 0; i < $scope.searchResults.length; i++) {
                    concepts.push($scope.searchResults[i].terminologyId);
                  }
                  var query = concepts[0];
                  for (var i = 1; i < concepts.length; i++) {
                    if (!$scope.data.memberTypes[concepts[i]]) {
                      query += ' OR ';
                      query += concepts[i];
                      // put a placeholder entry for the cases when it isn't a
                      // member of the refset
                      $scope.data.memberTypes[concepts[i]] = {
                        conceptId : concepts[i]
                      };
                    }
                  }
                  var pfs = {
                    startIndex : -1
                  };
                  query = '(' + query + ')';
                  refsetService.findRefsetMembersForQuery($scope.data.refset.id, query, pfs).then(
                  // Success
                  function(data) {
                    for (var i = 0; i < data.members.length; i++) {
                      $scope.data.memberTypes[data.members[i].conceptId] = data.members[i];
                    }
                  });
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.close(refset);
                };

              };

              // Migration modal
              $scope.openMigrationModal = function(lrefset) {
                console.debug('openMigrationModal ', lrefset);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/migration.html',
                  controller : MigrationModalCtrl,
                  backdrop : 'static',
                  size : 'lg',
                  resolve : {

                    refset : function() {
                      return lrefset;
                    },
                    paging : function() {
                      return $scope.paging;
                    },
                    metadata : function() {
                      return $scope.metadata;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  // handle workflow advancement
                  $scope.handleWorkflow(data);
                });
              };

              // Migration modal controller
              var MigrationModalCtrl = function($scope, $uibModalInstance, $interval, gpService,
                refset, paging, metadata) {
                console.debug('Entered migration modal control');

                // set up variables
                $scope.refset = refset;
                $scope.newTerminology = refset.terminology;
                $scope.newVersion = null;
                $scope.membersInCommon = null;
                $scope.pageSize = 5;
                $scope.paging = paging;
                $scope.metadata = metadata;
                $scope.versions = metadata.versions[metadata.terminologies[0]].sort().reverse();
                $scope.errors = [];
                $scope.statusTypes = [ 'Active', 'Retired' ];
                $scope.pagedStagedInclusions = [];
                $scope.paging['newRegularMembers'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.paging['oldRegularMembers'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.paging['membersInCommon'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.paging['stagedInclusions'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.pagedValidInclusions = [];
                $scope.paging['validInclusions'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.pagedInvalidInclusions = [];
                $scope.paging['invalidInclusions'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.pagedStagedExclusions = [];
                $scope.paging['stagedExclusions'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.pagedValidExclusions = [];
                $scope.paging['validExclusions'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.pagedInvalidExclusions = [];
                $scope.paging['invalidExclusions'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.lookupInterval = null;

                // Initialize
                if ($scope.refset.stagingType == 'MIGRATION') {
                  refsetService.resumeMigration($scope.refset.id).then(
                  // Success
                  function(data) {
                    $scope.stagedRefset = data;
                    $scope.newTerminology = $scope.stagedRefset.terminology;
                    $scope.newVersion = $scope.stagedRefset.version;
                    refsetService.compareRefsets($scope.refset.id, $scope.stagedRefset.id).then(
                    // Success
                    function(data) {
                      $scope.reportToken = data;
                      $scope.getDiffReport();
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

                // Table sorting mechanism
                $scope.setSortField = function(table, field, object) {
                  console.debug("sort field", table, field, $scope.paging);
                  utilService.setSortField(table, field, $scope.paging);
                  console.debug("  paging = ", $scope.paging[table]);
                  // retrieve the correct table
                  if (table == 'membersInCommon') {
                    $scope.findMembersInCommon();
                  } else if (table == 'oldRegularMembers') {
                    $scope.getOldRegularMembers();
                  } else if (table == 'newRegularMembers') {
                    $scope.getNewRegularMembers();
                  } else if (table == 'stagedInclusions') {
                    $scope.getPagedStagedInclusions();
                  } else if (table == 'validInclusions') {
                    $scope.getPagedValidInclusions();
                  } else if (table == 'invalidInclusions') {
                    $scope.getPagedInvalidInclusions();
                  } else if (table == 'stagedExclusions') {
                    $scope.getPagedStagedExclusions();
                  } else if (table == 'validExclusions') {
                    $scope.getPagedValidExclusions();
                  } else if (table == 'invalidExclusions') {
                    $scope.getPagedInvalidExclusions();
                  }
                };

                // Return up or down sort chars if sorted
                $scope.getSortIndicator = function(table, field) {
                  return utilService.getSortIndicator(table, field, $scope.paging);
                };

                // get diff report
                $scope.getDiffReport = function() {
                  refsetService.getDiffReport($scope.reportToken).then(
                  // Success
                  function(data) {
                    $scope.diffReport = data;
                    $scope.validInclusions = data.validInclusions;
                    $scope.validExclusions = data.validExclusions;
                    $scope.invalidInclusions = data.invalidInclusions;
                    $scope.invalidExclusions = data.invalidExclusions;
                    $scope.stagedInclusions = data.stagedInclusions;
                    $scope.stagedExclusions = data.stagedExclusions;
                    $scope.findMembersInCommon();
                    $scope.getNewRegularMembers();
                    $scope.getOldRegularMembers();
                    $scope.getPagedStagedInclusions();
                    $scope.getPagedValidInclusions();
                    $scope.getPagedInvalidInclusions();
                    $scope.getPagedStagedExclusions();
                    $scope.getPagedValidExclusions();
                    $scope.getPagedInvalidExclusions();
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Load 'old regular members' with paging
                $scope.getOldRegularMembers = function() {
                  var pfs = {
                    startIndex : ($scope.paging['oldRegularMembers'].page - 1) * $scope.pageSize,
                    maxResults : $scope.pageSize,
                    sortField : $scope.paging['oldRegularMembers'].sortField,
                    ascending : $scope.paging['oldRegularMembers'].ascending,
                    queryRestriction : $scope.paging['oldRegularMembers'].filter != undefined ? $scope.paging['oldRegularMembers'].filter
                      : null
                  };

                  var conceptActive;
                  if ($scope.paging['oldRegularMembers'].typeFilter == 'Active') {
                    conceptActive = true;
                  } else if ($scope.paging['oldRegularMembers'].typeFilter == 'Retired') {
                    conceptActive = false;
                  } else {
                    conceptActive = null;
                  }

                  refsetService.getOldRegularMembers($scope.reportToken, null, pfs, conceptActive)
                    .then(
                    // Success
                    function(data) {
                      $scope.oldRegularMembers = data.members;
                      $scope.oldRegularMembers.totalCount = data.totalCount;
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                };

                // Load 'new regular members' with paging
                $scope.getNewRegularMembers = function() {
                  var pfs = {
                    startIndex : ($scope.paging['newRegularMembers'].page - 1) * $scope.pageSize,
                    maxResults : $scope.pageSize,
                    sortField : $scope.paging['newRegularMembers'].sortField,
                    ascending : $scope.paging['newRegularMembers'].ascending,
                    queryRestriction : $scope.paging['newRegularMembers'].filter != undefined ? $scope.paging['newRegularMembers'].filter
                      : null
                  };
                  refsetService.getNewRegularMembers($scope.reportToken, null, pfs, null).then(
                  // Success
                  function(data) {
                    $scope.newRegularMembers = data.members;
                    $scope.newRegularMembers.totalCount = data.totalCount;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Load 'members in common' with paging
                $scope.findMembersInCommon = function() {
                  var pfs = {
                    startIndex : ($scope.paging['membersInCommon'].page - 1) * $scope.pageSize,
                    maxResults : $scope.pageSize + 2,
                    sortField : $scope.paging['membersInCommon'].sortField,
                    ascending : $scope.paging['membersInCommon'].ascending,
                    queryRestriction : $scope.paging['membersInCommon'].filter != undefined ? $scope.paging['membersInCommon'].filter
                      : null
                  };

                  var conceptActive;
                  if ($scope.paging['membersInCommon'].typeFilter == 'Active') {
                    conceptActive = true;
                  } else if ($scope.paging['membersInCommon'].typeFilter == 'Retired') {
                    conceptActive = false;
                  } else {
                    conceptActive = null;
                  }

                  refsetService.findMembersInCommon($scope.reportToken, null, pfs, conceptActive)
                    .then(
                    // Succcess
                    function(data) {
                      $scope.membersInCommon = data.members;
                      $scope.membersInCommon.totalCount = data.totalCount;
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                };

                // Get paged staged inclusions (assume all are loaded)
                $scope.getPagedStagedInclusions = function() {
                  $scope.pagedStagedInclusions = utilService.getPagedArray($scope.stagedInclusions,
                    $scope.paging['stagedInclusions'], $scope.pageSize);
                };

                // Get paged valid inclusions (assume all are loaded)
                $scope.getPagedValidInclusions = function() {
                  $scope.pagedValidInclusions = utilService.getPagedArray($scope.validInclusions,
                    $scope.paging['validInclusions'], $scope.pageSize);
                };

                // Get paged invalid inclusions (assume all are loaded)
                $scope.getPagedInvalidInclusions = function() {
                  $scope.pagedInvalidInclusions = utilService.getPagedArray(
                    $scope.invalidInclusions, $scope.paging['invalidInclusions'], $scope.pageSize);
                };

                // Get paged staged exclusions (assume all are loaded)
                $scope.getPagedStagedExclusions = function() {
                  $scope.pagedStagedExclusions = utilService.getPagedArray($scope.stagedExclusions,
                    $scope.paging['stagedExclusions'], $scope.pageSize);
                };

                // Get paged valid exclusions (assume all are loaded)
                $scope.getPagedValidExclusions = function() {
                  $scope.pagedValidExclusions = utilService.getPagedArray($scope.validExclusions,
                    $scope.paging['validExclusions'], $scope.pageSize);
                };

                // Get paged invalid exclusions (assume all are loaded)
                $scope.getPagedInvalidExclusions = function() {
                  $scope.pagedInvalidExclusions = utilService.getPagedArray(
                    $scope.invalidExclusions, $scope.paging['invalidExclusions'], $scope.pageSize);
                };

                // Cancel migration and close dialog
                $scope.cancel = function(refset) {
                  refsetService.cancelMigration(refset.id).then(
                  // Success
                  function(data) {
                    // mark as cancelled
                    refset.staged = false;
                    refset.stagingType = null;
                    $scope.stagedRefset = null;
                    // If INTENSIONAL, we need to re-look up old/not/new members
                    if (refset.type == 'INTENSIONAL') {
                      startLookup(refset);
                    }
                    $uibModalInstance.close(refset);
                  },
                  // Error - cancel migration
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                // Refresh lookup progress until ready
                // then compare refsets
                $scope.refreshLookupProgress = function(refset) {
                  refsetService.getLookupProgress(refset.id).then(
                  // Success
                  function(data) {
                    $scope.lookupProgress = data;
                    if (data >= 100) {
                      gpService.decrement();
                      $scope.stagedRefset.lookupInProgress = false;
                      $scope.refset.lookupInProgress = false;

                      // Cancel interval
                      $interval.cancel($scope.lookupInterval);
                      $scope.lookupInterval = null;

                      // Compare refsets original and staged, regardless of the
                      // parameter
                      refsetService.compareRefsets($scope.refset.id, $scope.stagedRefset.id).then(
                      // Success
                      function(data) {
                        $scope.reportToken = data;
                        $scope.getDiffReport();
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    }
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Begin migration and compare refsets and get diff report
                $scope.beginMigration = function(newTerminology, newVersion) {
                  $scope.errors = [];
                  if (newTerminology == $scope.refset.terminology
                    && newVersion == $scope.refset.version) {
                    $scope.errors[0] = 'New terminology and version cannot match existing values';
                    return;
                  }
                  if (newTerminology == $scope.refset.terminology
                    && newVersion < $scope.refset.version) {
                    $scope.errors[0] = 'New version must be greater than existing version';
                    return;
                  }
                  if (!newVersion) {
                    $scope.errors[0] = 'New version must not be blank';
                    return;
                  }

                  refsetService.beginMigration(refset.id, newTerminology, newVersion).then(
                  // Success
                  function(data) {
                    $scope.stagedRefset = data;
                    // manage local state witout re-reading refset
                    $scope.refset.stagingType = 'MIGRATION';
                    $scope.refset.staged = true;

                    // What to do next depends on type
                    var lookupRefset = null;
                    $scope.lookupProgress = 1;
                    if (refset.type == 'EXTENSIONAL') {
                      lookupRefset = $scope.stagedRefset;
                    } else {
                      lookupRefset = refset;
                    }

                    gpService.increment();
                    lookupRefset.lookupInProgress = true;
                    $scope.lookupInterval = $interval(function() {
                      $scope.refreshLookupProgress(data);
                    }, 2000);
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Finish migration
                $scope.finish = function(refset) {

                  refsetService.finishMigration(refset.id).then(
                  // Success
                  function(data) {
                    $uibModalInstance.close(refset);
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                $scope.removeMember = function(member) {

                  refsetService.removeRefsetMember(member.id).then(
                  // Success
                  function(data) {
                    refsetService.compareRefsets($scope.refset.id, $scope.stagedRefset.id).then(
                    // Success
                    function(data) {
                      $scope.reportToken = data;
                      $scope.getDiffReport();
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

                };

                // Save for later, allow state to be resumed
                $scope.saveForLater = function(refset) {
                  // updates refset on close
                  $uibModalInstance.close(refset);
                };

                // add exclusion
                $scope.exclude = function(refset, member, staged) {
                  refsetService.addRefsetExclusion($scope.stagedRefset, member.conceptId, staged)
                    .then(
                      // Success
                      function() {
                        refsetService.releaseReportToken($scope.reportToken).then(
                          // Success
                          function() {
                            refsetService.compareRefsets($scope.refset.id, $scope.stagedRefset.id)
                              .then(
                              // Success
                              function(data) {
                                $scope.reportToken = data;
                                $scope.getDiffReport();
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
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                };  
                
                // add inclusion
                $scope.include = function(refset, member, staged) {
                  // if retired, find if there are replacement concepts
                  if (!member.conceptActive) {
                    projectService.getReplacementConcepts(member.conceptId, refset.terminology,
                      refset.version).then(
                      // Success
                      function(data) {
                        $scope.replacementConcepts = data.concepts;

                        // if no replacements, just add the inclusion
                        if ($scope.replacementConcepts.length == 0) {
                          $scope.addRefsetInclusion(refset, member, staged);
                        } else {
                          $scope.openReplacementConceptsModal(refset, member, staged,
                            $scope.replacementConcepts, $scope.reportToken);
                        }
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                  } else {
                    $scope.addRefsetInclusion(refset, member, staged);
                  }
                }
                
                $scope.addRefsetInclusion = function(refset, member, staged) {
                  member.refsetId = refset.id;
                  refsetService.addRefsetInclusion(member, staged).then(
                  // Success
                  function(data) {
                    refsetService.releaseReportToken($scope.reportToken).then(
                    // Success
                    function(data) {
                      refsetService.compareRefsets($scope.refset.id, $scope.stagedRefset.id).then(
                      // Success
                      function(data) {
                        $scope.reportToken = data;
                        $scope.getDiffReport();
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
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // add all inclusions
                $scope.includeAll = function(refset, list, staged, active) {
                  for (var i = 0; i < list.length; i++) {
                    $scope.include(refset, list[i]);
                  }
                };

                // exclude all
                $scope.excludeAll = function(refset, list, staged, active) {
                  for (var i = 0; i < list.length; i++) {
                    $scope.exclude(refset, list[i]);
                  }
                };

                // revert all inclusions or exclusions
                $scope.revertAll = function(refset, list) {
                  for (var i = 0; i < list.length; i++) {
                    $scope.revert(refset, list[i]);
                  }
                };

                // revert inclusions and exclusions
                $scope.revert = function(refset, member) {
                  if (member.memberType == 'INCLUSION' || member.memberType == 'INCLUSION_STAGED') {
                    refsetService.removeRefsetMember(member.id).then(
                      // Success - remove refse member
                      function() {
                        refsetService.releaseReportToken($scope.reportToken).then(
                          // Success - release report token
                          function() {
                            refsetService.compareRefsets($scope.refset.id, $scope.stagedRefset.id)
                              .then(
                              // Success - compare refsets
                              function(data) {
                                $scope.reportToken = data;
                                $scope.getDiffReport();
                              },
                              // Error - compare refsets
                              function(data) {
                                handleError($scope.errors, data);
                              });
                          },
                          // Error - release report token
                          function(data) {
                            handleError($scope.errors, data);
                          });
                      },
                      // Error - remove refset member
                      function(data) {
                        handleError($scope.errors, data);
                      });
                  } else if (member.memberType == 'EXCLUSION'
                    || member.memberType == 'EXCLUSION_STAGED') {
                    refsetService.removeRefsetExclusion(member.id).then(
                      // Success
                      function() {
                        refsetService.releaseReportToken($scope.reportToken).then(
                          // Success - release report token
                          function() {
                            refsetService.compareRefsets($scope.refset.id, $scope.stagedRefset.id)
                              .then(
                              // Success - compare refsets
                              function(data) {
                                $scope.reportToken = data;
                                $scope.getDiffReport();
                              },
                              // Error - compare refsets
                              function(data) {
                                handleError($scope.errors, data);
                              });
                          },
                          // Error - release report token
                          function(data) {
                            handleError($scope.errors, data);
                          });
                      },
                      // Error - remove refset exclusion
                      function(data) {
                        handleError($scope.errors, data);
                      });
                  }
                };

                // Used for styling - coordinated with css file
                $scope.getMemberStyle = function(member) {
                  if (member.memberType == 'MEMBER') {
                    return '';
                  }
                  return member.memberType.replace('_STAGED', '');
                };
                // Close modal
                $scope.close = function() {
                  $uibModalInstance.close();
                };

                
                // Add modal
                $scope.openReplacementConceptsModal = function(lrefset, lmember, lstaged, lreplacementConcepts, lreportToken) {
                  console.debug('openReplacementConceptsModal ', lrefset, lmember, lstaged, lreplacementConcepts, lreportToken);

                  var modalInstance = $uibModal.open({
                    templateUrl : 'app/component/refsetTable/replacements.html',
                    controller : ReplacementConceptsModalCtrl,
                    backdrop : 'static',
                    resolve : {
                      refset : function() {
                        return lrefset;
                      },
                      member : function() {
                        return lmember;
                      },
                      staged : function() {
                        return lstaged;
                      },
                      replacementConcepts : function() {
                        return lreplacementConcepts;
                      },
                      reportToken : function() {
                        return lreportToken;
                      }
                    }
                  });

                  modalInstance.result.then(
                  // Success
                  function(data) {
                    
                      refsetService.releaseReportToken($scope.reportToken).then(
                      // Success
                      function() {
                        refsetService.compareRefsets($scope.refset.id, $scope.stagedRefset.id).then(
                        // Success
                        function(data) {
                          $scope.reportToken = data;
                          $scope.getDiffReport();
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
              
                    
                  });

                };

                // Add modal controller
                var ReplacementConceptsModalCtrl = function($scope, $uibModalInstance, refset, 
                  member, staged, replacementConcepts, reportToken) {
                  console.debug('Entered replacement concepts modal control', 
                    refset, member, staged, replacementConcepts, reportToken);

                  $scope.errors = [];
                  $scope.refset = refset;
                  $scope.member = member;
                  $scope.staged = staged;
                  $scope.replacementConcepts = replacementConcepts;
                  $scope.reportToken = reportToken;
                  $scope.selection = {
                    ids: {"test": true}
                  };
                  //$scope.invalidIds = new Array();
                  $scope.invalid = {
                    ids: {"test": true}
                  };
                  $scope.expectedCt = 0;
                  
                  // initialize
                  var pfs = {
                    startIndex : 0,
                    maxResults : 1,
                    sortField : null,
                    ascending : null,
                    queryRestriction : null
                  };
                  
                  // check if replacements are already members
                  for (var i=0; i<$scope.replacementConcepts.length; i++) {
                    var query = '(' + $scope.replacementConcepts[i].terminologyId + ')';
                    refsetService.getNewRegularMembers($scope.reportToken, query, pfs, null).then(
                      // Success
                      function(data) {
                        if (data.members.length != 0) {
                          $scope.invalid.ids[data.members[0].terminologyId] = true;
                        }
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      }); 
                    refsetService.findMembersInCommon($scope.reportToken, query, pfs, null).then(
                      // Success
                      function(data) {
                        if (data.members.length != 0) {
                          $scope.invalid.ids[data.members[0].terminologyId] = true;
                        }
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      }); 
                    
                  }      
                  refsetService.getDiffReport($scope.reportToken).then(
                    // Success
                    function(data) {
                      $scope.stagedInclusions = data.stagedInclusions;
                      for (var i=0; i<$scope.replacementConcepts.length; i++) {
                        for (var j=0; j<$scope.stagedInclusions.length; j++) {
                          if ($scope.stagedInclusions[j].conceptId == 
                            $scope.replacementConcepts[i].terminologyId) {
                            $scope.invalid.ids[$scope.stagedInclusions[j].conceptId] = true;
                          }
                        }
                      }
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    }); 
                  
                  // Add button
                  $scope.submitAdd = function() {
                    // calculate total number of replacement options
                    for (var i=0; i<replacementConcepts.length; i++) {
                      if ($scope.selection.ids[replacementConcepts[i].terminologyId]) {
                        $scope.expectedCt++;
                      }
                    }
                    // if intensional, check if retired concept itself should be included
                    if (refset.type == 'INTENSIONAL') {            
                      if ($scope.selection.ids[$scope.member.conceptId]) {
                        $scope.expectedCt++;
                        $scope.addRefsetInclusionOrMember($scope.refset, $scope.member, $scope.staged);
                      }
                    }   
                    // if a concept is selected, add it as an inclusion or member
                    for (var i=0; i<replacementConcepts.length; i++) {
                      if ($scope.selection.ids[replacementConcepts[i].terminologyId]) {
                        var member = {
                          active : true,
                          conceptId : replacementConcepts[i].terminologyId,
                          conceptName : replacementConcepts[i].name,
                          conceptActive : replacementConcepts[i].active,
                          memberType : (refset.type == 'INTENSIONAL' ? 'INCLUSION' : 'MEMBER'),
                          moduleId : refset.moduleId,
                          refsetId : $scope.refset.id
                        };
                        $scope.addRefsetInclusionOrMember($scope.refset, member, $scope.staged);
                      }
                    }
                  };

                  $scope.addRefsetInclusionOrMember = function(refset, member, staged) {
                    member.refsetId = refset.id;
                    if (refset.type == 'INTENSIONAL') {
                      refsetService.addRefsetInclusion(member, staged).then(
                      // Success
                      function(data) {
                        $scope.expectedCt--;
                        if ($scope.expectedCt == 0) {
                          $uibModalInstance.close();
                        }
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    } else if (refset.type == 'EXTENSIONAL') {
                      refsetService.addRefsetMember(member).then(
                        // Success
                        function(data) {
                          $scope.expectedCt--;
                          if ($scope.expectedCt == 0) {
                            $uibModalInstance.close();
                          }
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

                  $scope.isInvalid = function(id) {
                    return  true;
                  };
                };
                
              };
              
              // Feedback modal
              $scope.openFeedbackModal = function(lrefset) {
                console.debug('Open feedbackModal ', lrefset);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/feedback.html',
                  controller : FeedbackModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
                    },
                    tinymceOptions : function() {
                      return utilService.tinymceOptions;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  refsetService.fireRefsetChanged(data);
                });

              };

              // Feedback controller
              var FeedbackModalCtrl = function($scope, $uibModalInstance, refset, tinymceOptions) {
                console.debug('Entered feedback modal control', refset);

                $scope.refset = JSON.parse(JSON.stringify(refset));
                $scope.tinymceOptions = tinymceOptions;
                $scope.errors = [];

                // Add feedback
                $scope.addFeedback = function(refset, name, email, message) {

                  if (!message) {
                    window.alert('The message cannot be empty');
                    return;
                  }
                  if (!name) {
                    window.alert('Name cannot be empty');
                    return;
                  }
                  if (!validateEmail(email)) {
                    window
                      .alert('Invalid email address provided (e.g. should be like someone@example.com)');
                    return;
                  }
                  workflowService.addFeedback(refset, name, email, message).then(
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

              // end

            } ]
        };
      } ]);
