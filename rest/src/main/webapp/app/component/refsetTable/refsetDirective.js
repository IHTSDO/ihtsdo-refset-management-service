// Refset Table directive
// e.g. <div refset-table value="PUBLISHED" />
tsApp
  .directive(
    'refsetTable',
    [
      '$modal',
      '$rootScope',
      'utilService',
      'securityService',
      'projectService',
      'refsetService',
      'releaseService',
      'workflowService',
      function($modal, $rootScope, utilService, securityService, projectService, refsetService,
        releaseService, workflowService) {
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
              $scope.refset = null;
              $scope.refsetReleaseInfo = null;
              $scope.refsets = null;
              $scope.project = null;

              // Page metadata
              $scope.memberTypes = [ "Member", "Exclusion", "Inclusion", "Inactive Member",
                "Inactive Inclusion" ];

              // Used for project admin to know what users are assigned to something.
              $scope.refsetIdToAuthorsMap = {};
              $scope.refsetIdToReviewersMap = {};

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
                sortField : 'memberType',
                ascending : null
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
                console.debug("setProject", $scope.projects.role, project);
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
                  queryRestriction : null
                };

                if ($scope.value == 'PUBLISHED' || $scope.value == 'PREVIEW') {
                  pfs.queryRestriction = 'workflowStatus:' + $scope.value;
                  refsetService.findRefsetsForQuery($scope.paging["refset"].filter, pfs).then(
                    function(data) {
                      $scope.refsets = data.refsets;
                      $scope.refsets.totalCount = data.totalCount;
                    })
                }

                if ($scope.value == 'AVAILABLE' && $scope.projects.role == 'AUTHOR') {
                  workflowService.findAvailableEditingRefsets($scope.project.id,
                    $scope.user.userName, pfs).then(function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                  });
                }
                if ($scope.value == 'AVAILABLE' && $scope.projects.role == 'REVIEWER') {
                  workflowService.findAvailableReviewRefsets($scope.project.id,
                    $scope.user.userName, pfs).then(function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                  })
                }
                if ($scope.value == 'AVAILABLE' && $scope.projects.role == 'ADMIN') {
                  workflowService.findAllAvailableRefsets($scope.project.id, pfs).then(
                    function(data) {
                      $scope.refsets = data.refsets;
                      $scope.refsets.totalCount = data.totalCount;
                    })
                }
                if ($scope.value == 'ASSIGNED_ALL' && $scope.projects.role == 'ADMIN') {
                  workflowService.findAllAssignedRefsets($scope.project.id, pfs).then(
                    function(data) {
                      $scope.refsets = data.refsets;
                      $scope.refsets.totalCount = data.totalCount;

                      // get refset tracking records
                      // in
                      // order to get refset authors
                      for (var i = 0; i < $scope.refsets.length; i++) {
                        workflowService.getTrackingRecordForRefset($scope.refsets[i].id).then(
                          function(data) {
                            $scope.refsetIdToAuthorsMap[data.refsetId] = data.authors;
                            $scope.refsetIdToReviewersMap[data.refsetId] = data.reviewers;
                          });
                      }
                    })
                }
                if ($scope.value == 'ASSIGNED' && $scope.projects.role == 'AUTHOR') {
                  workflowService.findAssignedEditingRefsets($scope.project.id,
                    $scope.user.userName, pfs).then(function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                  })
                }
                if ($scope.value == 'ASSIGNED' && $scope.projects.role == 'REVIEWER') {
                  workflowService.findAssignedReviewRefsets($scope.project.id,
                    $scope.user.userName, pfs).then(function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                  })
                }
                if ($scope.value == 'RELEASE') {
                  workflowService.findReleaseProcessRefsets($scope.project.id, pfs).then(
                    function(data) {
                      $scope.refsets = data.refsets;
                      $scope.refsets.totalCount = data.totalCount;
                    })
                }

                // If $scope.refset is in the list, select it, if not clear $scope.refset
                var found = false;
                if ($scope.refset) {
                  for (var i = 0; i < $scope.refsets.length; i++) {
                    if ($scope.refset.id == $scope.refsets[i].id) {
                      found = true;
                      break;
                    }
                  }
                }
                if (found) {
                  $scope.getMembers($scope.refset);
                } else {
                  $scope.refset = null;
                }
              };

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
                  value = value.replace(" ", "_").toUpperCase();
                  pfs.queryRestriction = "memberType:" + value;
                }

                refsetService.findRefsetMembersForQuery(refset.id, $scope.paging["member"].filter,
                  pfs).then(function(data) {
                  refset.members = data.members;
                  refset.members.totalCount = data.totalCount;
                })

              };

              // Get $scope.refsetReleaseInfo
              $scope.getCurrentRefsetReleaseInfo = function(refset) {
                $scope.refsetReleaseInfo = null;
                releaseService.getCurrentReleaseInfoForRefset(refset.id).then(function(data) {
                  $scope.refsetReleaseInfo = data;
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
                $scope.paging[table].sortField = field;
                // reset page number too
                $scope.paging[table].page = 1;
                // handles null case also
                if (!$scope.paging[table].ascending) {
                  $scope.paging[table].ascending = true;
                } else {
                  $scope.paging[table].ascending = false;
                }
                // reset the paging for the correct table
                for ( var key in $scope.paging) {
                  if ($scope.paging.hasOwnProperty(key)) {
                    if (key == table)
                      $scope.paging[key].page = 1;
                  }
                }
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
                if ($scope.paging[table].ascending == null) {
                  return "";
                }
                if ($scope.paging[table].sortField == field && $scope.paging[table].ascending) {
                  return "▴";
                }
                if ($scope.paging[table].sortField == field && !$scope.paging[table].ascending) {
                  return "▾";
                }

              };

              // Selects a refset (setting $scope.refset).
              // Looks up current release info and members.
              $scope.selectRefset = function(refset) {
                $scope.refset = refset;
                $scope.getCurrentRefsetReleaseInfo(refset);
                $scope.getMembers(refset);

              };

              // Used for styling inactive/disabled
              $scope.isDisabled = function(member) {
                return member.memberType == 'INACTIVE_MEMBER'
                  || member.memberType == 'INACTIVE_INCLUSION' || member.memberType == 'EXCLUSION';
              }

              // Used for styling - coordinated with css file
              // TODO: this can be better
              $scope.getMemberStyle = function(member) {
                if (member.memberType == 'MEMBER') {
                  return "";

                }
                return member.memberType.replace('_', ' ').toLowerCase();
              }

              // Remove a refset
              $scope.removeRefset = function(refset) {
                if (!confirm("Are you sure you want to remove the refset (" + refset.name + ")?")) {
                  return;
                }

                if (refset.members != null) {
                  if (!confirm("The refset has members that will also be deleted.")) {
                    return;
                  }
                }
                refsetService.removeRefset(object.id).then(function() {
                  $scope.refset = null;
                  refsetService.fireRefsetChanged();
                });

              };

              // Remove refset member
              $scope.removeMember = function(member) {
                if (!confirm("Are you sure you want to remove the member (" + member.conceptName
                  + ")?")) {
                  return;
                }

                refsetService.removeRefsetMember(member.id).then(
                // Success 
                function() {
                  $scope.getMembers();
                });
              };

              // Adds a refset exclusion and refreshes member
              // list with current PFS settings
              $scope.exclude = function(refset, conceptId) {

                refsetService.addRefsetExclusion(refset.id, conceptId, false, true).then(function() {
                  $scope.getMembers(refset);
                });

              };

              // Performs a workflow action
              $scope.performWorkflowAction = function(refset, action, userName) {

                workflowService.performWorkflowAction($scope.project.id, refset.id, userName,
                  action).then(function(data) {
                  refsetService.fireRefsetChanged(refset);
                })
              };

              $scope.removeAllRefsetMembers = function(refset) {
                if (!confirm("Are you sure you want to remove all the members of the refset" + " ("
                  + refset.name + ")?")) {
                  return;
                }
                refsetService.removeAllRefsetMembers(refset.id).then(function(data) {
                  refsetService.fireRefsetChanged($scope.project.id)
                  $scope.selectRefset(refset);
                })
              };

              // Used for ASSIGNED_ALL to know who refsets are assigned to
              $scope.getAuthorsForRefsetId = function(refsetId) {
                return $scope.refsetIdToAuthorsMap[refsetId];
              }
              $scope.getReviewersForRefsetId = function(refsetId) {
                return $scope.refsetIdToReviewersMap[refsetId];
              }

              // Exports a release artifact (and begins the
              // download)
              $scope.exportReleaseArtifact = function(artifact) {
                releaseService.exportReleaseArtifact(artifact);
              }

              // reassign to author refset that is in review
              // process
              $scope.performReassign = function(refset) {
                // first unassign, then assign to author who
                // worked on it
                workflowService.performWorkflowAction($scope.project.id, refset.id,
                  $scope.user.userName, 'UNASSIGN').then(
                  function(data) {
                    workflowService.performWorkflowAction($scope.project.id, refset.id,
                      $scope.user.userName, 'REASSIGN').then(function(data) {
                      refsetService.fireRefsetChanged(refset);
                    })
                  })
              };

              // Initialize if project setting isn't used
              if ($scope.value == 'PREVIEW' || $scope.value == 'PUBLISHED') {
                $scope.getRefsets();
              }

              //
              // Modals:
              //

              // Clone Refset modal
              $scope.openCloneRefsetModal = function(lrefset) {
                console.debug("cloneRefsetModal ", lrefset);

                var modalInstance = $modal.open({
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
                    project : function() {
                      return $scope.project;
                    },
                    projects : function() {
                      return $scope.projects;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function() {
                  refsetService.fireRefsetChanged(lrefset);
                });

              };

              // Clone Refset controller
              var CloneRefsetModalCtrl = function($scope, $modalInstance, refset, metadata,
                project, projects) {
                console.debug("Entered clone refset modal control", refset, projects);

                $scope.action = 'Clone';
                $scope.errors = [];
                $scope.project = project;
                $scope.projects = projects;
                $scope.metadata = metadata;
                $scope.versions = metadata.versions[metadata.terminologies[0]].sort().reverse();
                // Copy refset and clear terminology id
                $scope.refset = JSON.parse(JSON.stringify(refset));
                $scope.refset.terminologyId = null;
                $scope.originRefsetId = refset.id;

                $scope.submitRefset = function(refset) {
                  console.debug("clone refset", refset.id);
                  // Make sure refset id is null
                  refset.id = null;
                  refsetService.cloneRefset($scope.project.id, $scope.originRefsetId, refset).then(
                  // Success - add refset
                  function(data) {
                    var newRefset = data;
                    // If intensional, apply the definition
                    if (newRefset.type == 'INTENSIONAL') {
                      refsetService.beginRedefinition(newRefset.id, newRefset.definition).then(
                      // Success - begin redefinition
                      function(data) {

                        refsetService.finishRedefinition(newRefset.id).then(
                        // Success - finish redefinition
                        function(data) {
                          $modalInstance.close();
                        },
                        // Error - finish redefinition
                        function(data) {
                          $scope.errors[0] = data;
                          utilService.clearError();
                        })
                      },
                      // Error - begin redefinition
                      function(data) {
                        $scope.errors[0] = data;
                        utilService.clearError();
                      })
                    } else {
                      $modalInstance.close();
                    }
                  },
                  // Error - add refset
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
                  })
                };

                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };

              };

              // Import/Export modal
              $scope.openImportExportModal = function(lrefset, ldir, lcontentType) {
                console.debug("exportModal ", lrefset);

                var modalInstance = $modal.open({
                  templateUrl : 'app/component/refsetTable/importExport.html',
                  controller : ImportExportModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
                    },
                    dir : function() {
                      return ldir;
                    },
                    contentType : function() {
                      return lcontentType;
                    },
                    ioHandlers : function() {
                      if (ldir == 'Import') {
                        return $scope.metadata.importHandlers;
                      } else {
                        return $scope.metadata.exportHandlers;
                      }
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function() {
                  refsetService.fireRefsetChanged(lrefset);
                });
              };

              // Import/Export controller
              var ImportExportModalCtrl = function($scope, $modalInstance, refset, dir,
                contentType, ioHandlers) {
                console.debug("Entered import export modal control", refset.id, ioHandlers, dir,
                  contentType);

                $scope.refset = refset;
                $scope.ioHandlers = ioHandlers;
                $scope.selectedIoHandler = $scope.ioHandlers[0];
                $scope.contentType = contentType;
                $scope.dir = dir;
                $scope.errors = [];

                // Handle export
                $scope.export = function(file) {
                  console.debug("export", $scope.refset.id, file);

                  if (contentType == 'Definition') {
                    refsetService.exportDefinition($scope.refset.id, $scope.selectedIoHandler.id,
                      $scope.selectedIoHandler.fileTypeFilter);

                  }

                  if (contentType == 'Refset Members') {
                    refsetService.exportMembers($scope.refset.id, $scope.selectedIoHandler.id,
                      $scope.selectedIoHandler.fileTypeFilter);
                  }
                  $modalInstance.close();
                };

                // Handle import
                $scope.import = function(file) {
                  console.debug("import", $scope.refset.id, file);

                  if (contentType == 'Definition') {
                    refsetService.importDefinition($scope.refset.id, $scope.selectedIoHandler.id,
                      $scope.selectedIoHandler.fileTypeFilter);
                  }

                  if (contentType == 'Refset Members') {
                    refsetService.beginImportMembers($scope.refset.id, $scope.selectedIoHandler.id)
                      .then(

                        // Success
                        function(data) {
                          console.debug("begin import members, valdiation = ", data);
                          // data is a validation result, check for errors
                          if (data.errors.length > 0) {
                            $scope.errors = data.errors;
                          } else {

                            // If there are no errors, finish import
                            refsetService.finishImportMembers($scope.refset.id,
                              $scope.selectedIoHandler.id, file).then(
                            // Success - close dialog
                            function(data) {
                              $modalInstance.close();
                            },
                            // Failure - show error
                            function(data) {
                              $scope.errors[0] = data;
                              utilService.clearError();
                              // $modalInstance.close();
                            });
                          }
                        },

                        // Failure - show error, clear global error
                        function(data) {
                          $scope.errors[0] = data;
                          utilService.clearError();
                          // $modalInstance.close();
                        });
                  }
                };

                // Handle continue import
                $scope.continueImport = function(file) {
                  console.debug("continue import", $scope.refset.id, file);

                  if (contentType == 'Refset Members') {
                    refsetService.finishImportMembers($scope.refset.id,
                      $scope.selectedIoHandler.id, file).then(
                    // Success - close dialog
                    function(data) {
                      $modalInstance.close();
                    },
                    // Failure - show error
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                      // $modalInstance.close();
                    });
                  }
                };

                $scope.cancel = function() {
                  // If there are lingering errors, cancel the import
                  if ($scope.errors.length > 0) {
                    refsetService.cancelImportMembers($scope.refset.id);
                  }
                  // dismiss the dialog
                  $modalInstance.dismiss('cancel');

                };

              };

              // Directive scoped method for cancelling an import
              $scope.cancelImport = function(refset) {
                $scope.refset = refset;
                refsetService.cancelImportMembers($scope.refset.id).then(
                // Success
                function() {
                  refsetService.fireRefsetChanged($scope.refset);
                });
              };

              // Release Process modal
              $scope.openReleaseProcessModal = function(lrefset, lEffectiveTime) {

                console.debug("releaseProcessModal ", lrefset);

                var modalInstance = $modal.open({
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
                    effectiveTime : function() {
                      return lEffectiveTime
                    }

                  }
                });

                modalInstance.result.then(
                // Success
                function() {
                  refsetService.fireRefsetChanged(lrefset);
                });
              };

              // Release Process controller
              var ReleaseProcessModalCtrl = function($scope, $modalInstance, refset, ioHandlers,
                effectiveTime) {

                console.debug("Entered release process modal", refset.id, ioHandlers);

                $scope.errors = [];
                $scope.refset = refset;
                $scope.ioHandlers = ioHandlers;
                $scope.selectedIoHandler = $scope.ioHandlers[0];

                $scope.release = function() {
                  console.debug("export", refset.id);

                  releaseService.beginRefsetRelease(refset.id, effectiveTime).then(
                  // Success
                  function(data) {
                    releaseService.previewRefsetRelease(refset.id, $scope.selectedIoHandler.id);
                  },
                  // Error
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
                  });

                  $modalInstance.close();
                };

                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };

              };

              // Assign User modal
              $scope.openAssignUserModal = function(lrefset, laction, luserName) {
                console.debug("openAssignUserModal ", lrefset, laction, luserName);

                var modalInstance = $modal.open({
                  templateUrl : 'app/component/refsetTable/assignUser.html',
                  controller : AssignUserModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
                    },
                    action : function() {
                      return laction;
                    },
                    currentUserName : function() {
                      return luserName;
                    },
                    assignedUsers : function() {
                      return $scope.projects.assignedUsers;
                    },
                    project : function() {
                      return $scope.project;
                    }
                  }

                });

                modalInstance.result.then(
                // Success
                function() {
                  refsetService.fireRefsetChanged(lrefset);
                });
              };

              // Assign user controller
              var AssignUserModalCtrl = function($scope, $modalInstance, refset, action,
                currentUserName, assignedUsers, project, $rootScope) {

                console.debug("Entered assign user modal control", assignedUsers, project.id);

                $scope.refset = refset;
                $scope.project = project;
                $scope.assignedUserNames = [];
                $scope.selectedUserName = currentUserName;
                $scope.errors = [];

                // Prep userNames picklist
                for (var i = 0; i < assignedUsers.length; i++) {
                  $scope.assignedUserNames.push(assignedUsers[i].userName);
                }
                $scope.assignedUserNames = $scope.assignedUserNames.sort();

                $scope.assignUser = function(userName) {
                  console.debug("Submitting chosen user", userName);

                  if (!userName) {
                    $scope.errors[0] = "The user must be selected. ";
                    return;
                  }

                  $scope.selectedUserName = userName;

                  if (action == 'ASSIGN') {
                    workflowService.performWorkflowAction($scope.project.id, refset.id, userName,
                      "ASSIGN").then(
                    // Success
                    function(data) {
                      $modalInstance.close();
                    },
                    // Error
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })
                  }
                };

                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };

              };

              // Add Refset modal
              $scope.openAddRefsetModal = function() {
                console.debug("openAddRefsetModal ");

                var modalInstance = $modal.open({
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
              var AddRefsetModalCtrl = function($scope, $modalInstance, metadata, project) {

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
                  moduleId : $scope.project.projectId,
                  organization : $scope.project.organization,
                  terminology : $scope.project.terminology,
                  version : $scope.project.version,
                  type : metadata.refsetTypes[0],
                };

                $scope.terminologySelected = function(terminology) {
                  $scope.versions = metadata.versions[terminology].sort().reverse();
                };

                $scope.submitRefset = function(refset) {
                  console.debug("Submitting add refset", refset);

                  if (!refset || !refset.name || !refset.description) {
                    $scope.errors[0] = "The name and description fields cannot be blank. ";
                    return;
                  }

                  refset.projectId = project.id;
                  refsetService.addRefset(refset).then(
                  // Success - add refset
                  function(data) {
                    var newRefset = data;
                    // IF intensional, apply the definition
                    if (newRefset.type == 'INTENSIONAL') {
                      refsetService.beginRedefinition(newRefset.id, newRefset.definition).then(
                      // Success - begin redefinition
                      function(data) {

                        refsetService.finishRedefinition(newRefset.id).then(
                        // Success - finish redefinition
                        function(data) {
                          $modalInstance.close(newRefset);
                        },
                        // Error - finish redefinition
                        function(data) {
                          $scope.errors[0] = data;
                          utilService.clearError();
                        })
                      },
                      // Error - begin redefinition
                      function(data) {
                        $scope.errors[0] = data;
                        utilService.clearError();
                      })
                    } else {
                      $modalInstance.close(newRefset);
                    }
                  },
                  // Error - add refset
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
                  })

                };

                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };

              };

              // Edit refset modal
              $scope.openEditRefsetModal = function(lrefset) {

                console.debug("openEditRefsetModal ");

                var modalInstance = $modal.open({
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
                function() {
                  refsetService.fireRefsetChanged(lrefset);
                });
              };

              // Edit refset controller
              var EditRefsetModalCtrl = function($scope, $modalInstance, refset, metadata, project) {

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
                  console.debug("Submitting edit refset", refset);

                  if (!refset || !refset.name || !refset.description) {
                    $scope.error = "The name, description, and terminology fields cannot be blank. ";
                    return;
                  }
                  refsetService.updateRefset(refset).then(
                  // Success - update refset
                  function(data) {
                    if (refset.definition != $scope.originalDefinition) {
                      $scope.error = "Definition is not allowed to change with refset edit.";
                    } else {
                      $modalInstance.close();
                    }
                  },
                  // Error - update refset
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
                  })

                };

                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };

              };

              // Add member modal

              $scope.openAddMemberModal = function(lmember, lrefset) {

                console.debug("openAddMemberModal ", lrefset);

                var modalInstance = $modal.open({
                  templateUrl : 'app/component/refsetTable/addMember.html',
                  controller : AddMemberModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    member : function() {
                      return lmember;
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
                function() {
                  refsetService.fireRefsetChanged(lrefset);
                });
              };

              // Add member controller
              var AddMemberModalCtrl = function($scope, $modalInstance, member, refset, project) {

                console.debug("Entered add member modal control");
                $scope.pageSize = 10;
                $scope.errors = [];
                $scope.searchResults = null;
                $scope.data = {
                  concept : null
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
                  console.debug("add member", concept);

                  var member = {
                    conceptId : concept.terminologyId,
                    conceptName : concept.name,
                    memberType : $scope.memberType,
                    terminology : refset.terminology,
                    version : refset.version,
                    moduleId : refset.moduleId,
                  };
                  member.refsetId = refset.id;

                  if (member.memberType == 'MEMBER') {

                    refsetService.addRefsetMember(member).then(
                    // Success
                    function(data) {
                      $modalInstance.close();
                    },
                    // Error
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })
                  }

                  if (member.memberType == 'INCLUSION') {
                    refsetService.addRefsetInclusion(member.refsetId, member.conceptId, false, true).then(
                    // Success
                    function(data) {
                      $modalInstance.close();
                    },
                    // Error
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })
                  }

                };
                
                $scope.getPreviousPage = function() {
                  $scope.paging['search'].page--;
                  $scope.getSearchResults($scope.search);
                }
                $scope.getNextPage = function() {
                  $scope.paging['search'].page++;
                  $scope.getSearchResults($scope.search);
                }

                // get search results
                $scope.getSearchResults = function(search) {
                  console.debug("Getting search results", search);

                  if (!search) {
                    $scope.errors[0] = "The search field cannot be blank. ";
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
                  $modalInstance.dismiss('cancel');
                };

              };

              // modal for resolving redefinition issues
              $scope.openRedefinitionModal = function(lrefset) {

                console.debug("openRedefinitionModal ", lrefset);

                var modalInstance = $modal.open({
                  templateUrl : 'app/component/refsetTable/redefinition.html',
                  controller : RedefinitionModalCtrl,
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
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function() {
                  refsetService.fireRefsetChanged(lrefset);
                });
              };

              var RedefinitionModalCtrl = function($scope, $modalInstance, refset, definition,
                paging) {

                console.debug("Entered redefinition modal control");
                $scope.refset = refset;
                $scope.membersInCommon = null;
                $scope.pageSize = 10;
                $scope.paging = paging;

                $scope.getDiffReport = function() {
                  refsetService.getDiffReport($scope.reportToken).then(function(data) {
                    console.debug("diffReport", data);
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
                  });
                };
 
                $scope.getOldRegularMembers = function() {
                  var pfs = {
                    startIndex : ($scope.paging["oldRegularMembers"].page - 1) * $scope.pageSize,
                    maxResults : $scope.pageSize,
                    sortField : null,
                    queryRestriction : $scope.paging["oldRegularMembers"].filter != undefined ? $scope.paging["oldRegularMembers"].filter
                      : null
                  };
                  refsetService.getOldRegularMembers($scope.reportToken, null, pfs).then(function(data) {
                    console.debug("oldRegularMembers", data);
                    $scope.oldRegularMembers = data.members;
                    $scope.oldRegularMembers.totalCount = data.totalCount;
                  })
                };
                
                $scope.getNewRegularMembers = function() {
                  var pfs = {
                    startIndex : ($scope.paging["newRegularMembers"].page - 1) * $scope.pageSize,
                    maxResults : $scope.pageSize,
                    sortField : null,
                    queryRestriction : $scope.paging["newRegularMembers"].filter != undefined ? $scope.paging["newRegularMembers"].filter
                      : null
                  };
                  refsetService.getNewRegularMembers($scope.reportToken, null, pfs).then(function(data) {
                    console.debug("newRegularMembers", data);
                    $scope.newRegularMembers = data.members;
                    $scope.newRegularMembers.totalCount = data.totalCount;
                  })
                };
                
                $scope.findMembersInCommon = function() {
                  var pfs = {
                    startIndex : ($scope.paging["membersInCommon"].page - 1) * $scope.pageSize,
                    maxResults : $scope.pageSize,
                    sortField : null,
                    queryRestriction : $scope.paging["membersInCommon"].filter != undefined ? $scope.paging["membersInCommon"].filter
                      : null
                  };
                  refsetService.findMembersInCommon($scope.reportToken, null, pfs).then(
                    function(data) {
                      console.debug("membersInCommon", data);
                      $scope.membersInCommon = data.members;
                      $scope.membersInCommon.totalCount = data.totalCount;
                    })
                };

                $scope.redefine = function(newDefinition) {
                  console.debug("Begin redefinition", newDefinition);

                  refsetService.beginRedefinition(refset.id, newDefinition).then(function(data) {
                    console.debug("stagedRefset", data);
                    $scope.stagedRefset = data;
                    $scope.refset.stagingType = 'DEFINITION';
                    refsetService.compareRefsets(refset.id, data.id).then(function(data) {
                      console.debug("reportToken", data);
                      $scope.reportToken = data;
                      $scope.getDiffReport();
                    })
                  })
                };
                $scope.finish = function(refset) {
                  console.debug("Finish redefinition", refset.id);

                  refsetService.finishRedefinition(refset.id).then(function(data) {
                    console.debug("data", data);
                    $modalInstance.close();
                  })
                };
                
                $scope.saveForLater = function(refset) {
                  console.debug("Save for later redefinition", refset.id);
                  // updates refset on close
                  // TODO: need resume redefinition alert button  disable icon
                  $modalInstance.close();
                };
                
                // add exclusion
                $scope.exclude = function(refset, concept, staged, active) {
                  refsetService.addRefsetExclusion($scope.stagedRefset, concept.conceptId, staged, active).then(function() {
                    refsetService.releaseReportToken($scope.reportToken).then(function() {
                      console.debug("Released report token");
                      refsetService.compareRefsets(refset.id, $scope.stagedRefset.id).then(function(data) {
                        console.debug("reportToken", data);
                        $scope.reportToken = data;
                        $scope.getDiffReport();
                      });  
                    });
                  });
                }

                // add inclusion
                $scope.include = function(refset, concept, staged, active) {
                  refsetService.addRefsetInclusion($scope.stagedRefset, concept.conceptId, staged, active).then(function() {
                    refsetService.releaseReportToken($scope.reportToken).then(function() {
                        console.debug("Released report token");
                        refsetService.compareRefsets(refset.id, $scope.stagedRefset.id).then(function(data) {
                          console.debug("reportToken", data);
                          $scope.reportToken = data;
                          $scope.getDiffReport();
                        });  
                    });                   
                  });
                }
                
                // revert inclusions and exclusions
                $scope.revert = function(refset, concept) {
                  if (concept.memberType == 'INCLUSION' ||
                    concept.memberType == 'INCLUSION_STAGED') {
                    refsetService.removeRefsetMember(concept.id).then(function() {
                      refsetService.releaseReportToken($scope.reportToken).then(function() {
                        console.debug("Released report token");
                        refsetService.compareRefsets(refset.id, $scope.stagedRefset.id).then(function(data) {
                          console.debug("reportToken", data);
                          $scope.reportToken = data;
                          $scope.getDiffReport();
                        });  
                      });                   
                    });
                  } else if (concept.memberType == 'EXCLUSION' ||
                    concept.memberType == 'EXCLUSION_STAGED') {
                    refsetService.removeRefsetExclusion(concept.id).then(function() {
                      refsetService.releaseReportToken($scope.reportToken).then(function() {
                        console.debug("Released report token");
                        refsetService.compareRefsets(refset.id, $scope.stagedRefset.id).then(function(data) {
                          console.debug("reportToken", data);
                          $scope.reportToken = data;
                          $scope.getDiffReport();
                        });  
                      });                   
                    });
                  }
                }
                
                // Used for styling - coordinated with css file
                // TODO: this can be better
                $scope.getMemberStyle = function(member) {
                  if (member.memberType == 'MEMBER') {
                    return "";
                  }
                  return member.memberType.replace('_', ' ').toLowerCase();
                }
                
                $scope.cancel = function(refset) {
                  console.debug("Cancel redefinition", refset.id);
                  $modalInstance.dismiss('cancel');
                  refsetService.cancelRedefinition(refset.id).then(function(data) {
                    console.debug("data", data);
                  })
                };

                $scope.close = function() {
                  $modalInstance.close();
                }
              }

            } ]
        }
      } ]);
