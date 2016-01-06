// Refset Table directive
// e.g. <div refset-table value="PUBLISHED" />
tsApp
  .directive(
    'refsetTable',
    [
      '$uibModal',
      '$rootScope',
      '$sce',
      'utilService',
      'securityService',
      'projectService',
      'refsetService',
      'releaseService',
      'workflowService',
      'validationService',
      function($uibModal, $rootScope, $sce, utilService, securityService, projectService,
        refsetService, releaseService, workflowService, validationService) {
        console.debug('configure refsetTable directive');
        return {
          restrict : 'A',
          scope : {
            // Legal "value" settings include
            // For directory tab: PUBLISHED, PREVIEW
            // For refset tab: AVAILABLE, ASSIGNED, ASSIGNED_ALL,
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
                concept : null
              };
              $scope.refsetReleaseInfo = null;
              $scope.refsets = null;
              $scope.refsetLookupProgress = {};
              $scope.project = null;

              $scope.showLatest = true;

              // Page metadata
              $scope.memberTypes = [ "Member", "Exclusion", "Inclusion" ];

              // Used for project admin to know what users are assigned to something.
              $scope.refsetAuthorsMap = {};
              $scope.refsetReviewersMap = {};

              // Paging variables
              $scope.pageSize = 10;
              $scope.paging = {};
              $scope.paging["refset"] = {
                page : 1,
                filter : "",
                sortField : 'name',
                ascending : null
              }
              $scope.paging["member"] = {
                page : 1,
                filter : "",
                typeFilter : "",
                sortField : 'lastModified',
                ascending : true
              }
              $scope.paging["membersInCommon"] = {
                page : 1,
                filter : "",
                typeFilter : "",
                sortField : 'name',
                ascending : null
              }
              $scope.paging["oldRegularMembers"] = {
                page : 1,
                filter : "",
                typeFilter : "",
                sortField : 'name',
                ascending : null
              }
              $scope.paging["newRegularMembers"] = {
                page : 1,
                filter : "",
                typeFilter : "",
                sortField : 'name',
                ascending : null
              }

              $scope.ioImportHandlers = [];
              $scope.ioExportHandlers = [];

              // Refset Changed handler
              $scope.$on('refset:refsetChanged', function(event, data) {
                console.debug('on refset:refsetChanged', data);
                // If the refset is set, refresh refsets list
                if (data) {
                  $scope.getRefsets();
                }
              });

              // Project Changed Handler
              $scope.$on('refset:projectChanged', function(event, data) {
                console.debug('on refset:projectChanged', data);
                // Set project, refresh refset list
                $scope.setProject(data);
              });

              // Set $scope.project and reload
              // $scope.refsets
              $scope.setProject = function(project) {
                $scope.project = project;
                $scope.getRefsets();
                // $scope.projects.role already updated
              };

              // Get $scope.refsets
              // Logic for this depends on the $scope.value and $scope.projects.role
              $scope.getRefsets = function() {
                var pfs = {
                  startIndex : ($scope.paging["refset"].page - 1) * $scope.pageSize,
                  maxResults : $scope.pageSize,
                  sortField : $scope.paging["refset"].sortField,
                  ascending : $scope.paging["refset"].ascending == null ? true
                    : $scope.paging["refset"].ascending,
                  queryRestriction : null,
                  latestOnly : false
                };

                if ($scope.value == 'PUBLISHED' || $scope.value == 'PREVIEW') {
                  pfs.queryRestriction = 'workflowStatus:' + $scope.value;
                  pfs.latestOnly = $scope.showLatest;
                  refsetService.findRefsetsForQuery($scope.paging["refset"].filter, pfs).then(
                    function(data) {
                      $scope.refsets = data.refsets;
                      $scope.refsets.totalCount = data.totalCount;
                      $scope.reselect();
                    })
                }

                if ($scope.value == 'AVAILABLE' && $scope.projects.role == 'AUTHOR') {
                  workflowService.findAvailableEditingRefsets($scope.project.id,
                    $scope.user.userName, pfs).then(function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                    $scope.reselect();
                  });
                }
                if ($scope.value == 'AVAILABLE' && $scope.projects.role == 'REVIEWER') {
                  workflowService.findAvailableReviewRefsets($scope.project.id,
                    $scope.user.userName, pfs).then(function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                    $scope.reselect();
                  })
                }
                if ($scope.value == 'AVAILABLE' && $scope.projects.role == 'ADMIN') {
                  workflowService.findAllAvailableRefsets($scope.project.id, pfs).then(
                    function(data) {
                      $scope.refsets = data.refsets;
                      $scope.refsets.totalCount = data.totalCount;
                      $scope.reselect();
                    })
                }
                if ($scope.value == 'ASSIGNED_ALL' && $scope.projects.role == 'ADMIN') {
                  workflowService.findAllAssignedRefsets($scope.project.id, pfs).then(
                    function(data) {
                      $scope.refsets = data.refsets;
                      $scope.refsets.totalCount = data.totalCount;

                      // get refset tracking records in order to get refset authors
                      for (var i = 0; i < $scope.refsets.length; i++) {
                        workflowService.getTrackingRecordForRefset($scope.refsets[i].id).then(
                          function(data) {
                            if (data.authors.length > 0) {
                              $scope.refsetAuthorsMap[data.refsetId] = data.authors;
                            }
                            if (data.reviewers.length > 0) {
                              $scope.refsetReviewersMap[data.refsetId] = data.reviewers;
                            }
                          });
                      }
                      $scope.reselect();
                    })
                }
                if ($scope.value == 'ASSIGNED' && $scope.projects.role == 'AUTHOR') {
                  workflowService.findAssignedEditingRefsets($scope.project.id,
                    $scope.user.userName, pfs).then(function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                    $scope.reselect();
                  })
                }
                if ($scope.value == 'ASSIGNED' && $scope.projects.role == 'REVIEWER') {
                  workflowService.findAssignedReviewRefsets($scope.project.id,
                    $scope.user.userName, pfs).then(function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                    $scope.reselect();
                  })
                }
                if ($scope.value == 'RELEASE') {
                  pfs.queryRestriction = "(workflowStatus:READY_FOR_PUBLICATION OR workflowStatus:PREVIEW  OR workflowStatus:PUBLISHED)";
                  pfs.latestOnly = $scope.showLatest;
                  refsetService.findRefsetsForQuery($scope.paging["refset"].filter, pfs).then(
                    function(data) {
                      $scope.refsets = data.refsets;
                      $scope.refsets.totalCount = data.totalCount;
                      $scope.reselect();
                    })
                }
              };

              // Reselect selected refset to refresh
              $scope.reselect = function() {
                // if there is a selection...
                // Bail if nothing selected
                if ($scope.selected.refset) {
                  // If $scope.selected.refset is in the list, select it, if not clear $scope.selected.refset
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
                  }
                }

                // If "lookup in progress" is set, get progress
                for (var i = 0; i < $scope.refsets.length; i++) {
                  if ($scope.refsets[i].lookupInProgress) {
                    $scope.refreshLookupProgress($scope.refsets[i]);
                  }
                }
              }

              // Get $scope.metadata.descriptionTypes
              $scope.getStandardDescriptionTypes = function(terminology, version) {
                projectService.getStandardDescriptionTypes(terminology, version).then(
                // Success
                function(data) {
                  // Populate "selected" for refsetTable.html
                  // and metadata for addMember.html
                  $scope.selected.descriptionTypes = data.types;
                  $scope.metadata.descriptionTypes = data.types;
                });
              }

              // Get $scope.members
              $scope.getMembers = function(refset) {

                var pfs = {
                  startIndex : ($scope.paging["member"].page - 1) * $scope.pageSize,
                  maxResults : $scope.pageSize,
                  sortField : $scope.paging["member"].sortField,
                  ascending : $scope.paging["member"].ascending == null ? true
                    : $scope.paging["member"].ascending,
                  queryRestriction : null
                };

                if ($scope.paging["member"].typeFilter) {
                  var value = $scope.paging["member"].typeFilter;
                  // Handle inactive
                  value = value.replace(" ", "_").toUpperCase();
                  pfs.queryRestriction = "memberType:" + value;
                }

                refsetService.findRefsetMembersForQuery(refset.id, $scope.paging["member"].filter,
                  pfs).then(
                // Success
                function(data) {
                  refset.members = data.members;
                  refset.members.totalCount = data.totalCount;
                })

              };

              // Get $scope.refsetReleaseInfo
              $scope.getCurrentRefsetReleaseInfo = function(refset) {
                $scope.refsetReleaseInfo = null;
                releaseService.getCurrentRefsetReleaseInfo(refset.id).then(function(data) {
                  $scope.refsetReleaseInfo = data;
                })
              };

              // optimizes the definition
              $scope.optimizeDefinition = function(refset) {
                refsetService.optimizeDefinition(refset.id).then(function() {
                  refsetService.fireRefsetChanged(refset);
                })
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
                utilService.setSortField(table, field, $scope.paging);
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
                return utilService.getSortIndicator(table, field, $scope.paging);
              };

              // Selects a refset (setting $scope.selected.refset).
              // Looks up current release info and members.
              $scope.selectRefset = function(refset) {
                $scope.selected.refset = refset;
                $scope.getCurrentRefsetReleaseInfo(refset);
                $scope.getMembers(refset);
                $scope.getStandardDescriptionTypes(refset.terminology, refset.version);
              };

              // Selects a member (setting $scope.selected.member)
              $scope.selectMember = function(member) {
                $scope.selected.member = member
                // Set the concept for display in concept-info
                $scope.selected.concept = {
                  terminologyId : member.conceptId,
                  terminology : member.terminology,
                  version : member.version
                }

              };

              // Member type style
              $scope.getMemberStyle = function(member) {
                if (member.memberType == 'MEMBER') {
                  return "";
                }
                return member.memberType.replace('_STAGED', '');
              }

              // Remove a refset
              $scope.removeRefset = function(refset) {

                if (refset.members != null) {
                  if (!confirm("The refset has members that will also be deleted.")) {
                    return;
                  }
                }
                refsetService.removeRefset(refset.id).then(function() {
                  $scope.selected.refset = null;
                  refsetService.fireRefsetChanged(refset);
                });

              };

              // Remove refset member
              $scope.removeRefsetMember = function(refset, member) {

                refsetService.removeRefsetMember(member.id).then(
                // Success 
                function() {
                  $scope.getMembers(refset);
                });
              };
              // Remove refset inclusion
              $scope.removeRefsetInclusion = function(refset, member) {

                refsetService.removeRefsetMember(member.id).then(
                // Success 
                function() {
                  $scope.getMembers(refset);
                });
              };

              // Adds a refset exclusion and refreshes member
              // list with current PFS settings
              $scope.addRefsetExclusion = function(refset, member) {
                refsetService.addRefsetExclusion(refset, member.conceptId, false).then(function() {
                  $scope.getMembers(refset);
                });

              };

              // Remove refset exclusion and refreshes members
              $scope.removeRefsetExclusion = function(refset, member) {
                refsetService.removeRefsetExclusion(member.id).then(function() {
                  $scope.getMembers(refset);
                });

              };

              // Unassign refset from user
              $scope.unassign = function(refset, userName) {
                $scope.performWorkflowAction(refset, "UNASSIGN", userName);
              }

              // Performs a workflow action
              $scope.performWorkflowAction = function(refset, action, userName) {

                workflowService.performWorkflowAction($scope.project.id, refset.id, userName,
                  $scope.projects.role, action).then(function(data) {
                  refsetService.fireRefsetChanged(refset);
                })
              };

              // Removes all refset members
              $scope.removeAllRefsetMembers = function(refset) {
                refsetService.removeAllRefsetMembers(refset.id).then(function(data) {
                  refsetService.fireRefsetChanged($scope.project.id)
                  $scope.selectRefset(refset);
                })
              };

              // Exports a release artifact (and begins the
              // download)
              $scope.exportReleaseArtifact = function(artifact) {
                releaseService.exportReleaseArtifact(artifact);
              }

              // Cancel a staging operation
              $scope.cancelAction = function(refset) {
                $scope.refset = refset;
                if (refset.stagingType == 'IMPORT') {
                  refsetService.cancelImportMembers($scope.refset.id).then(
                  // Success
                  function() {
                    refsetService.fireRefsetChanged($scope.refset);
                  });
                }
                if (refset.stagingType == 'MIGRATION') {
                  refsetService.cancelMigration($scope.refset.id).then(
                  // Success
                  function() {
                    refsetService.fireRefsetChanged($scope.refset);
                  });
                }
              };

              // Start lookup again
              $scope.startLookup = function(refset) {
                refsetService.startLookup(refset.id).then(
                // Success
                function(data) {
                  $scope.refsetLookupProgress[refset.id] = 1;
                });
              }

              // Refresh lookup progress
              $scope.refreshLookupProgress = function(refset) {
                refsetService.getLookupProgress(refset.id).then(
                // Success
                function(data) {
                  if (data > 0 && data < 101) {
                    window.alert("Progress is " + data + " % complete.");
                  }
                  $scope.refsetLookupProgress[refset.id] = data;
                });
              }

              // Get the most recent note for display
              $scope.getLatestNote = function(refset) {
                if (refset && refset.notes && refset.notes.length > 0) {
                  return $sce.trustAsHtml(refset.notes
                    .sort(utilService.sort_by('lastModified', -1))[0].value);
                }
                return $sce.trustAsHtml("");
              }

              // Initialize if project setting isn't used
              if ($scope.value == 'PREVIEW' || $scope.value == 'PUBLISHED') {
                $scope.getRefsets();
              }

              //
              // MODALS
              //

              // Definition clauses modal
              $scope.openDefinitionClausesModal = function(lrefset, lvalue) {
                console.debug("openDefinitionClausesModal ", lrefset, lvalue);

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
                  refsetService.fireRefsetChanged(data);
                });

              };

              // Definition clauses controller
              var DefinitionClausesModalCtrl = function($scope, $uibModalInstance, $sce, refset,
                value) {
                console.debug("Entered definition clauses modal control", refset, value);

                $scope.errors = [];

                $scope.refset = refset;
                $scope.value = value;
                $scope.newClause = null;

                // Paging parameters
                $scope.pageSize = 5;
                $scope.pagedClauses = [];
                $scope.paging = {};
                $scope.paging["clauses"] = {
                  page : 1,
                  filter : "",
                  typeFilter : "",
                  sortField : "lastModified",
                  ascending : true
                }

                // Get paged clauses (assume all are loaded)
                $scope.getPagedClauses = function() {
                  $scope.pagedClauses = utilService.getPagedArray($scope.refset.definitionClauses,
                    $scope.paging['clauses'], $scope.pageSize);
                }

                $scope.getClauseValue = function(clause) {
                  return $sce.trustAsHtml(clause.value);
                }

                // remove clause
                $scope.removeClause = function(refset, clause) {
                  for (var i = 0; i < refset.definitionClauses.length; i++) {
                    var index = refset.definitionClauses.indexOf(clause);
                    if (index != -1) {
                      refset.definitionClauses.splice(index, 1);
                    }
                  }
                  $scope.getPagedClauses();
                };

                // add new clause
                $scope.submitClause = function(refset, clause) {

                  // Confirm clauses are unique, skip if not
                  for (var i = 0; i < refset.definitionClauses.length; i++) {
                    if (refset.definitionClauses[i] == clause) {
                      return;
                    }
                  }
                  refset.definitionClauses.push(clause);
                  $scope.getPagedClauses();
                  $scope.newClause = null;
                };

                $scope.save = function(refset) {
                  refsetService.updateRefset(refset).then(
                  // Success - add refset
                  function(data) {
                    $uibModalInstance.close(refset);
                  },
                  // Error - add refset
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
                  })
                }

                // close the modal
                $scope.cancel = function(refset) {
                  $uibModalInstance.close(refset);
                }

                // initialize modal
                $scope.getPagedClauses();
              };

              // Notes modal
              $scope.openNotesModal = function(lobject, ltype) {
                console.debug("openNotesModal ", lobject, ltype);

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
                  refsetService.fireRefsetChanged(data);
                });

              };

              // Notes controller
              var NotesModalCtrl = function($scope, $uibModalInstance, $sce, object, type,
                tinymceOptions) {
                console.debug("Entered notes modal control", object, type);

                $scope.errors = [];

                $scope.object = object;
                $scope.type = type;
                $scope.tinymceOptions = tinymceOptions;
                $scope.newNote = null;

                // Paging parameters
                $scope.pageSize = 5;
                $scope.pagedNotes = [];
                $scope.paging = {};
                $scope.paging["notes"] = {
                  page : 1,
                  filter : "",
                  typeFilter : "",
                  sortField : "lastModified",
                  ascending : true
                }

                // Get paged notes (assume all are loaded)
                $scope.getPagedNotes = function() {
                  $scope.pagedNotes = utilService.getPagedArray($scope.object.notes,
                    $scope.paging['notes'], $scope.pageSize);
                }

                $scope.getNoteValue = function(note) {
                  return $sce.trustAsHtml(note.value);
                }

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
                        $scope.errors[0] = data;
                        utilService.clearError();
                      })
                    },
                    // Error - add refset
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })
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
                        $scope.errors[0] = data;
                        utilService.clearError();
                      })
                    },
                    // Error - add refset
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })
                  }
                }

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
                        $scope.errors[0] = data;
                        utilService.clearError();
                      })
                    },
                    // Error - add refset
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })
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
                        $scope.errors[0] = data;
                        utilService.clearError();
                      })
                    },
                    // Error - add refset
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })
                  }
                };

                // Convert date to a string
                $scope.toDate = function(lastModified) {
                  return utilService.toDate(lastModified);
                };

                // close the modal
                $scope.cancel = function() {
                  $uibModalInstance.close();
                }

                // initialize modal
                $scope.getPagedNotes();
              };

              // Clone Refset modal
              $scope.openCloneRefsetModal = function(lrefset) {
                console.debug("cloneRefsetModal ", lrefset);

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
                  refsetService.fireRefsetChanged(data);
                });

              };

              // Clone Refset controller
              var CloneRefsetModalCtrl = function($scope, $uibModalInstance, refset, metadata,
                projects) {
                console.debug("Entered clone refset modal control", refset, projects);

                $scope.action = 'Clone';
                $scope.errors = [];
                $scope.projects = projects;
                $scope.metadata = metadata;
                $scope.versions = metadata.versions[metadata.terminologies[0]].sort().reverse();
                // Copy refset and clear terminology id
                $scope.refset = JSON.parse(JSON.stringify(refset));
                $scope.refset.terminologyId = null;

                $scope.submitRefset = function(refset) {

                  if (!refset.project) {
                    $scope.errors[0] = "A project must be chosen from the picklist.";
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

                      // if $scope.warnings is empty, and data.warnings is not, show warnings and stop
                      if (data.warnings && data.warnings.length > 0
                        && $scope.warnings !== data.warnings) {
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
                        $scope.errors[0] = data;
                        utilService.clearError();
                      })
                    },
                    // Error - validate
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    });
                };

                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Import/Export modal
              $scope.openImportExportModal = function(lrefset, loperation, ltype) {
                console.debug("exportModal ", lrefset);

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
                  refsetService.fireRefsetChanged(data);
                });
              };

              // Import/Export controller
              var ImportExportModalCtrl = function($scope, $uibModalInstance, refset, operation,
                type, ioHandlers) {
                console.debug("Entered import export modal control", refset.id, ioHandlers,
                  operation, type);

                $scope.refset = refset;
                $scope.ioHandlers = ioHandlers;
                $scope.selectedIoHandler = $scope.ioHandlers[0];
                $scope.type = type;
                $scope.operation = operation;
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
                      $scope.errors[0] = data;
                      utilService.clearError();
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
                              $uibModalInstance.close(refset);
                            },
                            // Failure - show error
                            function(data) {
                              $scope.errors[0] = data;
                              utilService.clearError();
                            });
                          }
                        },

                        // Failure - show error, clear global error
                        function(data) {
                          $scope.errors[0] = data;
                          utilService.clearError();
                          // $uibModalInstance.close();
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
                      $uibModalInstance.close(refset);
                    },
                    // Failure - show error
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                      // $uibModalInstance.close();
                    });
                  }
                };

                $scope.cancel = function() {
                  // If there are lingering errors, cancel the import
                  if ($scope.errors.length > 0) {
                    refsetService.cancelImportMembers($scope.refset.id);
                  }
                  // dismiss the dialog
                  $uibModalInstance.dismiss('cancel');

                };

              };

              // Directive scoped method for cancelling an import/migration
              $scope.cancelAction = function(refset) {
                $scope.refset = refset;
                if (refset.stagingType == 'IMPORT') {
                  refsetService.cancelImportMembers($scope.refset.id).then(
                  // Success
                  function() {
                    refsetService.fireRefsetChanged($scope.refset);
                  });
                }
                if (refset.stagingType == 'MIGRATION') {
                  refsetService.cancelMigration($scope.refset.id).then(
                  // Success
                  function() {
                    refsetService.fireRefsetChanged($scope.refset);
                  });
                }
                if (refset.stagingType == 'PREVIEW') {
                  releaseService.cancelRefsetRelease($scope.refset.id).then(
                  // Success
                  function() {
                    refsetService.fireRefsetChanged($scope.refset);
                  });
                }
              };

              // Release Process modal
              $scope.openReleaseProcessModal = function(lrefset) {

                console.debug("releaseProcessModal ", lrefset);

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

              // Release Process controller
              var ReleaseProcessModalCtrl = function($scope, $uibModalInstance, refset, ioHandlers,
                utilService) {

                console.debug("Entered release process modal", refset.id, ioHandlers);

                $scope.errors = [];
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

                if (refset.stagingType == 'PREVIEW') {
                  releaseService.resumeRelease(refset.id).then(
                  // Success
                  function(data) {
                    $scope.stagedRefset = data;
                  },
                  // Error
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
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
                    $scope.errors[0] = data;
                    utilService.clearError();
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
                    $scope.errors[0] = data;
                    utilService.clearError();
                  });
                };

                $scope.previewRefsetRelease = function(refset) {

                  releaseService.previewRefsetRelease(refset.id, $scope.selectedIoHandler.id).then(
                  // Success
                  function(data) {
                    $scope.stagedRefset = data;
                    $uibModalInstance.close($scope.stagedRefset);
                    alert("The PREVIEW refset has been added .");
                  },
                  // Error
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
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
                    $scope.errors[0] = data;
                    utilService.clearError();
                  });
                };

                // Cancel release process
                $scope.cancel = function(refset) {

                  $uibModalInstance.dismiss('cancel');
                  releaseService.cancelRefsetRelease(refset.id).then(
                  // Success
                  function(data) {
                    $uibModalInstance.close(refset);
                  },
                  // Error
                  function(data) {
                    $uibModalInstance.close();
                  });
                };

                // Close the window - to return later
                $scope.close = function() {
                  $uibModalInstance.close(refset);
                };

                $scope.open = function($event) {
                  $scope.status.opened = true;
                };

                $scope.format = 'yyyyMMdd';
              }

              // Assign refset modal
              $scope.openAssignRefsetModal = function(lrefset, laction) {
                console.debug("openAssignRefsetModal ", lrefset, laction);

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
                    currentUserName : function() {
                      return $scope.user.userName;
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
                      return utilService.inymceOptions;
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
                currentUserName, assignedUsers, project, role, tinymceOptions) {

                console.debug("Entered assign refset modal control", assignedUsers, project.id);

                $scope.refset = refset;
                $scope.action = action;
                $scope.project = project;
                $scope.role = role;
                $scope.tinymceOptions = tinymceOptions;
                $scope.assignedUserNames = [];
                $scope.userName = currentUserName;
                $scope.note;
                $scope.errors = [];

                // Prep userNames picklist
                for (var i = 0; i < assignedUsers.length; i++) {
                  $scope.assignedUserNames.push(assignedUsers[i].userName);
                }
                $scope.assignedUserNames = $scope.assignedUserNames.sort();

                $scope.assignRefset = function() {
                  if (!$scope.userName) {
                    $scope.errors[0] = "The user must be selected. ";
                    return;
                  }

                  if (action == 'ASSIGN') {
                    workflowService.performWorkflowAction($scope.project.id, refset.id,
                      $scope.userName, $scope.role, 'ASSIGN').then(
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
                          $scope.errors[0] = data;
                          utilService.clearError();
                        });
                      }
                      // close dialog if no note
                      else {
                        $uibModalInstance.close(refset);
                      }

                    },
                    // Error
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })
                  }

                  // else
                  if (action == 'REASSIGN') {
                    workflowService.performWorkflowAction($scope.project.id, refset.id,
                      $scope.userName, $scope.role, 'UNASSIGN').then(
                      // Success - unassign
                      function(data) {
                        // The username doesn't matter - it'll go back to the author
                        workflowService.performWorkflowAction($scope.project.id, refset.id,
                          $scope.userName, 'AUTHOR', 'REASSIGN').then(
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
                              $scope.errors[0] = data;
                              utilService.clearError();
                            });
                          }
                          // close dialog if no note
                          else {
                            $uibModalInstance.close(refset);
                          }
                        },
                        // Error - reassign
                        function(data) {
                          $scope.errors[0] = data;
                          utilService.clearError();
                        })
                      },
                      // Error - unassign
                      function(data) {
                        $scope.errors[0] = data;
                        utilService.clearError();
                      });
                  }
                };

                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Add Refset modal
              $scope.openAddRefsetModal = function() {
                console.debug("openAddRefsetModal ");

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

                console.debug("Entered add refset modal control", metadata);

                $scope.action = 'Add';
                $scope.errors = [];
                $scope.metadata = metadata;
                $scope.project = project;
                $scope.versions = metadata.versions[metadata.terminologies[0]].sort().reverse();
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
                };

                $scope.terminologySelected = function(terminology) {
                  $scope.versions = metadata.versions[terminology].sort().reverse();
                };

                $scope.submitRefset = function(refset) {

                  refset.projectId = project.id;

                  // validate refset before adding it
                  validationService.validateRefset(refset).then(
                    function(data) {

                      // If there are errors, make them available and stop.
                      if (data.errors && data.errors.length > 0) {
                        $scope.errors = data.errors;
                        return;
                      } else {
                        $scope.errors = [];
                      }

                      // if $scope.warnings is empty, and data.warnings is not, show warnings and stop
                      if (data.warnings && data.warnings.length > 0
                        && $scope.warnings !== data.warnings) {
                        $scope.warnings = data.warnings;
                        return;
                      } else {
                        $scope.warnings = [];
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
                        $scope.errors[0] = data;
                        utilService.clearError();
                      })

                    },
                    // Error - validate refset
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })
                };

                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Edit refset modal
              $scope.openEditRefsetModal = function(lrefset) {

                console.debug("openEditRefsetModal ");

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
                  refsetService.fireRefsetChanged(data);
                });
              };

              // Edit refset controller
              var EditRefsetModalCtrl = function($scope, $uibModalInstance, refset, metadata,
                project) {

                console.debug("Entered edit refset modal control");

                $scope.action = 'Edit';
                $scope.errors = [];
                $scope.refset = refset;
                $scope.project = project;
                $scope.originalDefinition = $scope.refset.definition;
                $scope.metadata = metadata;
                $scope.versions = $scope.metadata.versions[refset.terminology].sort().reverse();

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

                      // if $scope.warnings is empty, and data.warnings is not, show warnings and stop
                      if (data.warnings && data.warnings.length > 0
                        && $scope.warnings !== data.warnings) {
                        $scope.warnings = data.warnings;
                        return;
                      } else {
                        $scope.warnings = [];
                      }

                      // Success - validate refset
                      refsetService.updateRefset(refset).then(
                      // Success - update refset
                      function(data) {
                        if (refset.definition != $scope.originalDefinition) {
                          $scope.error = "Definition is not allowed to change with refset edit.";
                        } else {
                          $uibModalInstance.close(refset);
                        }
                      },
                      // Error - update refset
                      function(data) {
                        $scope.errors[0] = data;
                        utilService.clearError();
                      })

                    },
                    // Error - validate refset
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })

                };

                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              $scope.openAddMemberModal = function(lrefset) {

                console.debug("openAddMemberModal ", lrefset);

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
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  refsetService.fireRefsetChanged(data);
                });
              };

              // Add member controller
              var AddMemberModalCtrl = function($scope, $uibModalInstance, metadata, refset,
                project) {

                console.debug("Entered add member modal control");
                $scope.pageSize = 10;
                $scope.errors = [];
                $scope.searchResults = null;
                $scope.data = {
                  concept : null,
                  descriptionTypes : metadata.descriptionTypes
                };
                $scope.pageSize = 10;
                $scope.paging = {};
                $scope.paging["search"] = {
                  page : 1,
                  filter : "",
                  sortField : null,
                  ascending : null
                }

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
                    terminology : refset.terminology,
                    version : refset.version,
                    moduleId : refset.moduleId,
                  };
                  member.refsetId = refset.id;

                  // validate member before adding it
                  validationService.validateMember(member, project.id).then(
                    function(data) {
                      $scope.validationResult = data;
                      if ($scope.validationResult.errors.length > 0) {
                        $scope.errors = $scope.validationResult.errors;
                      } else {
                        $scope.errors = null;
                      }
                      if ($scope.validationResult.warnings.length > 0) {
                        $scope.previousWarnings = $scope.warnings;
                        $scope.warnings = $scope.validationResult.warnings;
                      } else {
                        $scope.warnings = null;
                      }
                      // perform the edit if there are no errors or if there are only warnings
                      // and the user clicks through the warnings
                      if ($scope.errors == null
                        && ($scope.warnings == null || (JSON.stringify($scope.warnings) == JSON
                          .stringify($scope.previousWarnings)))) {
                        $scope.warnings = null;
                        // Success - validate refset

                        if (member.memberType == 'MEMBER') {

                          refsetService.addRefsetMember(member).then(
                          // Success
                          function(data) {
                            $uibModalInstance.close(refset);
                          },
                          // Error
                          function(data) {
                            $scope.errors[0] = data;
                            utilService.clearError();
                          })
                        }

                        if (member.memberType == 'INCLUSION') {
                          refsetService.addRefsetInclusion(member, false).then(
                          // Success
                          function(data) {
                            $uibModalInstance.close(refset);
                          },
                          // Error
                          function(data) {
                            $scope.errors[0] = data;
                            utilService.clearError();
                          })
                        }
                      }
                    },
                    // Error - validate refset
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })

                };

                // get search results
                $scope.getSearchResults = function(search, clearPaging) {

                  if (clearPaging) {
                    $scope.paging["search"].page = 1;
                  }

                  if (!search) {
                    $scope.errors[0] = "The search field cannot be empty. ";
                    return;
                  }
                  // clear data structures
                  $scope.errors = [];

                  var pfs = {
                    startIndex : ($scope.paging["search"].page - 1) * $scope.pageSize,
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
                  },
                  // Error
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
                  });

                };

                // Clear errors
                $scope.clearError = function() {
                  $scope.errors = [];
                }

                // select concept and get concept data
                $scope.selectConcept = function(concept) {
                  $scope.data.concept = concept;
                };

                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Migration modal
              $scope.openMigrationModal = function(lrefset) {

                console.debug("openMigrationModal ", lrefset);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/migration.html',
                  controller : MigrationModalCtrl,
                  backdrop : 'static',
                  size : 'lg',
                  resolve : {

                    refset : function() {
                      return lrefset;
                    },
                    definition : function() {
                      return lrefset.definition;
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
                  refsetService.fireRefsetChanged(data);
                });
              };

              // Migration modal controller
              var MigrationModalCtrl = function($scope, $uibModalInstance, $interval, refset, definition,
                paging, metadata) {

                console.debug("Entered migration modal control");

                // set up variables
                $scope.refset = refset;
                $scope.membersInCommon = null;
                $scope.pageSize = 10;
                $scope.paging = paging;
                $scope.metadata = metadata;
                $scope.versions = metadata.versions[metadata.terminologies[0]].sort().reverse();
                $scope.errors = [];
                var stop;
                
                // Initialize
                if ($scope.refset.stagingType == 'MIGRATION') {
                  refsetService.resumeMigration($scope.refset.id).then(
                  // Success
                  function(data) {
                    $scope.stagedRefset = data;
                    $scope.refset.newTerminology = $scope.stagedRefset.terminology;
                    $scope.refset.newVersion = $scope.stagedRefset.version;
                    refsetService.compareRefsets($scope.refset.id, data.id).then(
                    // Success
                    function(data) {
                      $scope.reportToken = data;
                      $scope.getDiffReport();
                    },
                    // Error
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    });
                  },
                  // Error
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
                  });
                }

                //
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
                    $scope.getOldRegularMembers();
                    $scope.getNewRegularMembers();
                  },
                  // Error
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
                  });
                };

                // Load "old regular members" with paging
                $scope.getOldRegularMembers = function() {
                  var pfs = {
                    startIndex : ($scope.paging["oldRegularMembers"].page - 1) * $scope.pageSize,
                    maxResults : $scope.pageSize,
                    sortField : null,
                    queryRestriction : $scope.paging["oldRegularMembers"].filter != undefined ? $scope.paging["oldRegularMembers"].filter
                      : null
                  };
                  refsetService.getOldRegularMembers($scope.reportToken, null, pfs).then(
                  // Success
                  function(data) {
                    $scope.oldRegularMembers = data.members;
                    $scope.oldRegularMembers.totalCount = data.totalCount;
                  },
                  // Error
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
                  });
                };

                // Load "new regular members" with paging
                $scope.getNewRegularMembers = function() {
                  var pfs = {
                    startIndex : ($scope.paging["newRegularMembers"].page - 1) * $scope.pageSize,
                    maxResults : $scope.pageSize,
                    sortField : null,
                    queryRestriction : $scope.paging["newRegularMembers"].filter != undefined ? $scope.paging["newRegularMembers"].filter
                      : null
                  };
                  refsetService.getNewRegularMembers($scope.reportToken, null, pfs).then(
                  // Success
                  function(data) {
                    $scope.newRegularMembers = data.members;
                    $scope.newRegularMembers.totalCount = data.totalCount;
                  },
                  // Error
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
                  });
                };

                // Load "members in common" with paging
                $scope.findMembersInCommon = function() {
                  var pfs = {
                    startIndex : ($scope.paging["membersInCommon"].page - 1) * $scope.pageSize,
                    maxResults : $scope.pageSize,
                    sortField : null,
                    queryRestriction : $scope.paging["membersInCommon"].filter != undefined ? $scope.paging["membersInCommon"].filter
                      : null
                  };
                  refsetService.findMembersInCommon($scope.reportToken, null, pfs).then(
                  // Succcess
                  function(data) {
                    $scope.membersInCommon = data.members;
                    $scope.membersInCommon.totalCount = data.totalCount;
                  },
                  // Error
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
                  });
                };

                $scope.refreshLookupProgress = function() {
                  refsetService.getLookupProgress($scope.stagedRefset.id).then(
                    // Success
                    function(data) {
                      $scope.lookupProgress = data;
                      if (data >= 100) {
                        $scope.stopRefreshLookupProgress();
                        $scope.stagedRefset.lookupInProgress = false;
                        
                          refsetService.compareRefsets(refset.id, $scope.stagedRefset.id).then(
                            // Success
                            function(data) {
                              $scope.reportToken = data;
                              $scope.getDiffReport();
                            },
                            // Error
                            function(data) {
                              $scope.errors[0] = data;
                              utilService.clearError();
                            });
                      } /*else if (data < 100) {
                        window.alert("Progress is " + data + " % complete.");
                      }*/
                    },
                    // Error
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    });
                }
                
                $scope.stopRefreshLookupProgress = function() {
                  if (angular.isDefined(stop)) {
                    $interval.cancel(stop);
                    stop = undefined;
                  }
                };
                
                // Begin migration and compare refsets and get diff report
                $scope.beginMigration = function(newTerminology, newVersion) {

                  refsetService.beginMigration(refset.id, newTerminology, newVersion).then(
                  // Success
                  function(data) {
                    $scope.stagedRefset = data;
                    $scope.stagedRefset.lookupInProgress = true;
                    // TODO read this from the refset using a service
                    $scope.refset.stagingType = 'MIGRATION';
                    $scope.lookupProgress = 0;
                    
                    stop = $interval(function() {
                      $scope.refreshLookupProgress();
                    }, 2000);
                  },
                  // Error
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
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
                    $scope.errors[0] = data;
                    utilService.clearError();
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
                        refsetService.compareRefsets(refset.id, $scope.stagedRefset.id).then(
                        // Success
                        function(data) {
                          $scope.reportToken = data;
                          $scope.getDiffReport();
                        },
                        // Error
                        function(data) {
                          $scope.errors[0] = data;
                          utilService.clearError();
                        });
                      },
                      // Error
                      function(data) {
                        $scope.errors[0] = data;
                        utilService.clearError();
                      });
                    },
                    // Error
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    });
                }

                // add inclusion
                $scope.include = function(member, staged) {
                  refsetService.addRefsetInclusion(member, staged).then(
                  // Success
                  function() {
                    refsetService.releaseReportToken($scope.reportToken).then(
                    // Success
                    function() {
                      refsetService.compareRefsets(refset.id, $scope.stagedRefset.id).then(
                      // Success
                      function(data) {
                        $scope.reportToken = data;
                        $scope.getDiffReport();
                      },
                      // Error
                      function(data) {
                        $scope.errors[0] = data;
                        utilService.clearError();
                      });
                    },
                    // Error
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    });
                  },
                  // Error
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
                  });
                }

                // add all inclusions
                $scope.includeAll = function(refset, list, staged, active) {
                  for (var i = 0; i < list.length; i++) {
                    $scope.include(refset, list[i]);
                  }
                }

                // exclude all
                $scope.excludeAll = function(refset, list, staged, active) {
                  for (var i = 0; i < list.length; i++) {
                    $scope.exclude(refset, list[i]);
                  }
                }

                // revert all inclusions or exclusions
                $scope.revertAll = function(refset, list) {
                  for (var i = 0; i < list.length; i++) {
                    $scope.revert(refset, list[i]);
                  }
                }

                // revert inclusions and exclusions
                $scope.revert = function(refset, member) {
                  if (member.memberType == 'INCLUSION' || member.memberType == 'INCLUSION_STAGED') {
                    refsetService.removeRefsetMember(member.id).then(
                    // Success - remove refse member
                    function() {
                      refsetService.releaseReportToken($scope.reportToken).then(
                      // Success - release report token
                      function() {
                        refsetService.compareRefsets(refset.id, $scope.stagedRefset.id).then(
                        // Success - compare refsets
                        function(data) {
                          $scope.reportToken = data;
                          $scope.getDiffReport();
                        },
                        // Error - compare refsets
                        function(data) {
                          $scope.errors[0] = data;
                          utilService.clearError();
                        });
                      },
                      // Error - release report token
                      function(data) {
                        $scope.errors[0] = data;
                        utilService.clearError();
                      });
                    },
                    // Error - remove refset member
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    });
                  } else if (member.memberType == 'EXCLUSION'
                    || member.memberType == 'EXCLUSION_STAGED') {
                    refsetService.removeRefsetExclusion(member.id).then(
                    // Success
                    function() {
                      refsetService.releaseReportToken($scope.reportToken).then(
                      // Success - release report token
                      function() {
                        refsetService.compareRefsets(refset.id, $scope.stagedRefset.id).then(
                        // Success - compare refsets
                        function(data) {
                          $scope.reportToken = data;
                          $scope.getDiffReport();
                        },
                        // Error - compare refsets
                        function(data) {
                          $scope.errors[0] = data;
                          utilService.clearError();
                        });
                      },
                      // Error - release report token
                      function(data) {
                        $scope.errors[0] = data;
                        utilService.clearError();
                      });
                    },
                    // Error - remove refset exclusion
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    });
                  }
                }

                // Used for styling - coordinated with css file
                $scope.getMemberStyle = function(member) {
                  if (member.memberType == 'MEMBER') {
                    return "";
                  }
                  return member.memberType.replace('_STAGED', '');
                }

                // Cancel migration
                $scope.cancel = function(refset) {
                  /*if (!confirm("Are you sure you want to cancel the migration?")) {
                    return;
                  }*/
                  refsetService.cancelMigration(refset.id).then(
                  // Success
                  function(data) {
                    $scope.stagedRefset = null;
                  },
                  // Error
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
                  });

                  $uibModalInstance.dismiss('cancel');
                };

                $scope.close = function(refset) {
                  $uibModalInstance.close(refset);
                }

              }

              // Feedback modal
              $scope.openFeedbackModal = function(lrefset) {
                console.debug("Open feedbackModal ", lrefset);

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
                console.debug("Entered feedback modal control", refset);

                $scope.errors = [];
                $scope.refset = JSON.parse(JSON.stringify(refset));
                $scope.tinymceOptions = tinymceOptions;

                $scope.addFeedback = function(refset, name, email, message) {

                  if (message == null || message == undefined || message === '') {
                    window.alert("The message cannot be empty. ");
                    return;
                  }

                  if (name == null || name == undefined || name === '' || email == null
                    || email == undefined || email === '') {
                    window.alert("Name and email must be provided.");
                    return;
                  }

                  if (!validateEmail(email)) {
                    window
                      .alert("Invalid email address provided (e.g. should be like someone@example.com)");
                    return;
                  }

                  workflowService.addFeedback(refset, name, email, message).then(
                  // Success
                  function(data) {
                    $uibModalInstance.dismiss('cancel');
                  },
                  // Error
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
                  })
                };

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
        }
      } ]);
