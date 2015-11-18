// Refset Table directive
// e.g. <div refset-table value="PUBLISHED" />
tsApp.directive('refsetTable',
  [
    'utilService',
    'projectService',
    'refsetService',
    'releaseService',
    'workflowService',
    'securityService',
    '$modal',
    '$rootScope',
    function(utilService, projectService, refsetService, releaseService, workflowService,
      securityService, $modal, $rootScope) {
      console.debug('configure refsetTable directive');
      return {
        restrict : 'A',
        scope : {
          // Legal "value" settings include
          // For directory tab: PUBLISHED, PREVIEW
          // For refset tab: AVAILABLE, ASSIGNED, ASSIGNED_ALL, RELEASE
          value : '@'
        },
        templateUrl : 'app/component/refsetTable/refsetTable.html',
        controller : [
          '$scope',
          function($scope) {

            // Variables
            $scope.iconConfig = projectService.getIconConfig();
            $scope.user = securityService.getUser();
            $scope.role = 'VIEWER';
            $scope.refset = null;
            $scope.refsets = null;

            $scope.memberTypes = [ "Member", "Exclusion", "Inclusion",
              "Inactive Member", "Inactive Inclusion" ];
            $scope.selectedProject = null;

            // TODO: remove as I believe these are unused
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
            $scope.paging["children"] = {
              page : 1,
              filter : "",
              typeFilter : "",
              sortField : 'name',
              ascending : null
            }

            $scope.ioImportHandlers = [];
            $scope.ioExportHandlers = [];
            
            // TODO: need to fix these according to the wiki (e.g. "projectChanged")
            $scope.$on('refsetTable:initialize', function (event, data) {
              console.log('on refsetTable:initialize', data, $scope.value);  
              $scope.initializeProjectAndRefsets(data);
            });
            
            $scope.$on('refset:project', function (event, data) {
              console.log('on refset:project', data);  
              $scope.selectedProject = data;
              if ($scope.selectedProject != undefined && $scope.selectedProject != null) {
                $scope.initializeProjectAndRefsets($scope.selectedProject.id);
                $scope.getRefsetTypes();
                $scope.getTerminologyEditions();
              }
            });
            
            // get all projects where user has a role
            $scope.initializeProjectAndRefsets = function(projectId) {
              projectService.getProject(projectId).then(function(data) {
                $scope.selectedProject = data;
                console.debug("value: ", $scope.value);
                $scope.initializeUsersAndRefsets();
              })
            };
            
            // Get $scope.refsets
            // TODO: collapse ALL functionality for collecting $scope.refsets into this
            // method . At the moment there are many functions that can populate this
            // variable.  There should be only one.
            $scope.getRefsets = function() {

              var pfs = {
                startIndex : ($scope.paging["refset"].page - 1)
                  * $scope.pageSize,
                maxResults : $scope.pageSize,
                sortField : $scope.paging["refset"].sortField,
                ascending : $scope.paging["refset"].ascending == null ? true
                  : $scope.paging["refset"].ascending,
                queryRestriction : 'workflowStatus:' + $scope.value
              };

              refsetService.findRefsetsForQuery($scope.paging["refset"].filter,
                pfs).then(function(data) {
                $scope.refsets = data.refsets;
                $scope.refsets.totalCount = data.totalCount;
              })
            };
            
            // Get users with role on project and determine which refsets to load
            // TODO: this method should ALWAYS be used, not onoly for "refset" tab
            // e.g. consolidate the logic and choose a more generic function name
            $scope.initializeUsersAndRefsets = function() {

              var pfs = {
                startIndex : 0,
                maxResults : 100, // TODO: what if there are > 100 users?  Make start index -1
                sortField : 'userName',
                queryRestriction : null
              };
              
              // Find users with roles on this project
              projectService.findAssignedUsersForProject($scope.selectedProject.id,
                "", pfs).then(function(data) {
                $scope.assignedUsers = data.users;
                for (var i = 0; i< $scope.assignedUsers.length; i++) {
                  if ($scope.assignedUsers[i].userName == $scope.user.userName) {
                    var projectRoleMap = $scope.assignedUsers[i].projectRoleMap;
                    if (projectRoleMap) {
                      $scope.role = projectRoleMap[$scope.selectedProject.id];                 
                      break;
                    }
                  }
                }
                // TODO: move this logic into getRefsets which will become
                // aware of $scope.value and project role (which can be a parameter)
                // e.g. $scope.getRefsets(role);  The $scope.value is already
                // a top-level variable and so does not need to be a parameter
                console.debug("project role: ", $scope.role);
                if ($scope.value == 'AVAILABLE' && $scope.role == 'AUTHOR') {
                  $scope.findAvailableEditingRefsets();
                }
                if ($scope.value == 'AVAILABLE' && $scope.role == 'REVIEWER') {
                  $scope.findAvailableReviewRefsets();
                }
                if ($scope.value == 'AVAILABLE' && $scope.role == 'ADMIN') {
                  $scope.findAllAvailableRefsets();
                }
                if ($scope.value == 'ASSIGNED_ALL' && $scope.role == 'ADMIN') {
                  $scope.findAllAssignedRefsets();
                }
                if ($scope.value == 'ASSIGNED' && $scope.role == 'AUTHOR') {
                  $scope.findAssignedEditingRefsets();
                }
                if ($scope.value == 'ASSIGNED' && $scope.role == 'REVIEWER') {
                  $scope.findAssignedReviewRefsets();
                }
                if ($scope.value == 'RELEASE') {
                  $scope.findReleaseRefsets();
                }
              })

            };
            
            // TODO: Logic of all these methods can be folded into getRefsets
            $scope.findAvailableEditingRefsets = function() {
              var pfs = {
                startIndex : 0,
                maxResults : 100,
                sortField : null,
                queryRestriction : null
              };

              workflowService.findAvailableEditingRefsets($scope.selectedProject.id, $scope.user.userName, pfs).then(function(data) {
                $scope.refsets = data.refsets;
                $scope.refsets.totalCount = data.totalCount;
              })
            };
                        
            $scope.findAvailableReviewRefsets = function() {
              var pfs = {
                startIndex : 0,
                maxResults : 100,
                sortField : null,
                queryRestriction : null
              };

              workflowService.findAvailableReviewRefsets($scope.selectedProject.id, $scope.user.userName, pfs).then(function(data) {
                $scope.refsets = data.refsets;
                $scope.refsets.totalCount = data.totalCount;
              })
            };
            

            $scope.findAssignedEditingRefsets = function() {
              var pfs = {
                startIndex : 0,
                maxResults : 100,
                sortField : null,
                queryRestriction : null
              };

              workflowService.findAssignedEditingRefsets($scope.selectedProject.id, $scope.user.userName, pfs).then(function(data) {
                $scope.refsets = data.refsets;
                $scope.refsets.totalCount = data.totalCount;
              })
            };
            
            $scope.findAllAvailableRefsets = function() {
              var pfs = {
                startIndex : 0,
                maxResults : 100,
                sortField : null,
                queryRestriction : null
              };

              workflowService.findAllAvailableRefsets($scope.selectedProject.id, '', pfs).then(function(data) {
                $scope.refsets = data.refsets;
                $scope.refsets.totalCount = data.totalCount;
              })
            };

            $scope.findAllAssignedRefsets = function() {
              var pfs = {
                startIndex : 0,
                maxResults : 100,
                sortField : null,
                queryRestriction : null
              };

              workflowService.findAllAssignedRefsets($scope.selectedProject.id, '', pfs).then(function(data) {
                $scope.refsets = data.refsets;
                $scope.refsets.totalCount = data.totalCount;
                
                // get refset tracking records in order to get refset authors
                for (var i = 0; i<$scope.refsets.length; i++) {
                  workflowService.getTrackingRecordForRefset($scope.refsets[i].id).then(function(data) {
                    $scope.refsetIdToAuthorsMap[data.refsetId] = data.authors;
                    $scope.refsetIdToReviewersMap[data.refsetId] = data.reviewers;
                  });
                }
              })
            };

            $scope.findAssignedReviewRefsets = function() {
              var pfs = {
                startIndex : 0,
                maxResults : 100,
                sortField : null,
                queryRestriction : null
              };

              workflowService.findAssignedReviewRefsets($scope.selectedProject.id, $scope.user.userName, pfs).then(function(data) {
                $scope.refsets = data.refsets;
                $scope.refsets.totalCount = data.totalCount;
              })
            };
            
            
            $scope.findReleaseRefsets = function() {
              var pfs = {
                startIndex : 0,
                maxResults : 100,
                sortField : null,
                queryRestriction : null
              };

              workflowService.findReleaseProcessRefsets($scope.selectedProject.id, $scope.user.userName, pfs).then(function(data) {
                $scope.refsets = data.refsets;
                $scope.refsets.totalCount = data.totalCount;
              })
            };
            
            // Get $scope.members
            $scope.getMembers = function(refset) {

              var pfs = {
                startIndex : ($scope.paging["member"].page - 1)
                  * $scope.pageSize,
                maxResults : $scope.pageSize,
                sortField : $scope.paging["member"].sortField,
                ascending : $scope.paging["member"].ascending == null ? true
                  : $scope.paging["member"].ascending,
                queryRestriction : ""
              };
              
              if ($scope.paging["member"].typeFilter) {
                var value = $scope.paging["member"].typeFilter;
                value = value.replace(" ", "_").toUpperCase();
                pfs.queryRestriction += "memberType:" + value;
              }

              refsetService.findRefsetMembersForQuery(refset.id,
                $scope.paging["member"].filter, pfs).then(function(data) {
                refset.members = data.members;
                refset.members.totalCount = data.totalCount;
              })
            };

            // Get current refset release info
            // TODO: may be better to not attach this to the refset itself
            // but instead to have a $scope.currentReleaseInfo variable
            $scope.getCurrentRefsetReleaseInfo = function(refset) {
              releaseService.getCurrentReleaseInfoForRefset(refset.id).then(
                function(data) {
                  refset.releaseInfo = data;
                })
            };
            
            // Begin redefinition (or first definition)
            $scope.beginRedefinition = function(refsetId, definition) {
              refsetService.beginRedefinition(refset.id, definition).then(
                function(data) {
                  console.debug("data", data);
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
              if ($scope.paging[table].sortField == field
                && $scope.paging[table].ascending) {
                return "▴";
              }
              if ($scope.paging[table].sortField == field
                && !$scope.paging[table].ascending) {
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
                || member.memberType == 'INACTIVE_INCLUSION'
                || member.memberType == 'EXCLUSION';
            }

            // Used for styling - coordinated with css file
            // TODO: this can be better
            $scope.getMemberStyle = function(member) {
              if (member.memberType == 'MEMBER') {
                return "";
              }
              return member.memberType.replace('_', ' ').toLowerCase();
            }
            
            
            // Get $scope.refsetTypes - for picklist
            $scope.getRefsetTypes = function() {
              console.debug("getRefsetTypes");
              refsetService.getRefsetTypes().then(function(data) {
                $scope.refsetTypes = data.strings;
              })
            };
            
            // Remove a refset  or a refset member
            // TODO: why are these not separate methods?!
            $scope.remove = function(type, object, objArray) {
              if (!confirm("Are you sure you want to remove the " + type + " ("
                + object.name + ")?")) {
                return;
              }
              if (type == 'refset') {
                if (object.userRoleMap != null && object.userRoleMap != undefined
                  && Object.keys(object.userRoleMap).length > 0) {
                  window
                    .alert("You can not delete a project that has users assigned to it. Remove the assigned users before deleting the project.");
                  return;
                }
                if (object.members != null) {
                  if (!confirm("The refset has members that will also be deleted.")) {
                    return;
                  }
                }
                refsetService.removeRefset(object.id).then(function() {
                  $scope.getRefsets();
                  $scope.refset = null;
                });
              }
              if (type == 'member') {
            
                refsetService.removeRefsetMember(object.id).then(function() {
                  //$scope.getRefsets();
                  objArray.splice(objArray.indexOf(object), 1);
                });
              }
            };
            
            // Adds a refset exclusion and refreshes member list with current PFS settings
            $scope.exclude = function(refset, conceptId) {
               
                refsetService.addRefsetExclusion(refset.id, conceptId).then(function() {
                  $scope.getMembers(refset);
                });

            };

            // Performs a workflow action
            // TODO: should fire "refsetChanged"
            $scope.performWorkflowAction = function(refset, action, userName) {
              
              workflowService.performWorkflowAction($scope.selectedProject.id, refset.id,
                userName, action).then(function(data) {
                console.log("rootScope.broadcast", $scope.value);  
                $rootScope.$broadcast('refsetTable:initialize', $scope.selectedProject.id);
                 })
            };
            
            // Get $scope.terminologyEditions, also loads versions for the first edition in the list
            $scope.getTerminologyEditions = function() {
              console.debug("getTerminologyEditions");
              projectService.getTerminologyEditions().then(function(data) {
                $scope.terminologyEditions = data.strings;
                $scope.getTerminologyVersions($scope.terminologyEditions[0]);
              })
            };
            
            // Get $scope.terminologyVersions
            $scope.getTerminologyVersions = function(terminology) {
              console.debug("getTerminologyVersions");
              projectService.getTerminologyVersions(terminology).then(function(data) {
                $scope.terminologyVersions = {};
                $scope.terminologyVersions[terminology] = [];
                for (var i = 0; i < data.translations.length; i++) {
                  $scope.terminologyVersions[terminology].push(
                    data.translations[i].version.replace(/-/gi, ""));
                }
              })
            };

            // Get $scope.io{Import,Export}Handlers
            $scope.getIOHandlers = function() {
              refsetService.getImportRefsetHandlers().then(function(data) {
                $scope.ioImportHandlers = data.handlers;
              });
              refsetService.getExportRefsetHandlers().then(function(data) {
                $scope.ioExportHandlers = data.handlers;
              });
            }
            
            // TODO: remove as I believe these are unused
            $scope.getAuthorsForRefsetId = function(refsetId) {
              return $scope.refsetIdToAuthorsMap[refsetId];
            }
            $scope.getReviewersForRefsetId = function(refsetId) {
              return $scope.refsetIdToReviewersMap[refsetId];
            }

            // Exports a release artifact (and begins the download)
            $scope.exportReleaseArtifact = function(artifact) {
              releaseService.exportReleaseArtifact(artifact);
            }
            
            // get all projects where user has a role
            // TODO: this scope variable should be defined at the top of the controller
            // TODO: this should probably be "projects" instead of "candidateProjects"
            $scope.retrieveCandidateProjects = function() {

              var pfs = {
                startIndex : 0,
                maxResults : 100,
                sortField : 'name',
                queryRestriction : 'userAnyRole:' + $scope.user.userName
              };
              // clear queryRestriction for application admins
              if ($scope.user.applicationRole == 'ADMIN') {
                pfs.queryRestriction = null;
              }

              projectService.findProjectsAsList("", pfs).then(function(data) {
                $scope.candidateProjects = data.projects;
                $scope.candidateProjects.totalCount = data.totalCount;
              })

            };
            
            // Initialization routine
            // TODO: this should go at the END of the controller
            // i.e. declare everything, then perform init steps.
            if ($scope.value == 'PREVIEW' || $scope.value == 'PUBLISHED') {
              $scope.getRefsets();
            }
            $scope.getIOHandlers();
            $scope.retrieveCandidateProjects();

           
            //
            // Modals:
            // TODO: put partials pages with the directive and not with the controller
            //  

            // modal for creating a clone of a refset
            $scope.openCloneRefsetModal = function(lrefset) {

              console.debug("cloneRefsetModal ", lrefset);

              var modalInstance = $modal.open({
                templateUrl : 'app/page/refset/clone.html',
                controller : CloneRefsetModalCtrl,
                backdrop : 'static',
                resolve : {
                  refset : function() {
                    return lrefset;
                  },                  
                  candidateProjects : function() {
                    return $scope.candidateProjects;
                  }
                }
              });
              
              modalInstance.result.then(
                // Success
                // TODO: this should fire a "refsetChanged" event instead.
                function() {
                  $scope.findAvailableEditingRefsets();               
                });

            };

            var CloneRefsetModalCtrl = function($scope, $modalInstance, refset,
              candidateProjects) {

              console.debug("Entered clone refset modal control", refset.id);

              $scope.candidateProjects = candidateProjects;
              $scope.refset = refset;
              $scope.refset.releaseInfo = undefined;
              
              $scope.newRefset = {};
              $scope.newRefset.terminologyId = null;
              

              $scope.clone = function() {
                console.debug("clone refset", refset.id);


                refsetService.cloneRefset($scope.refset, $scope.newRefset.project.id,
                  $scope.newRefset.terminologyId);

                $modalInstance.close();
              };

              $scope.cancel = function() {
                $modalInstance.dismiss('cancel');
              };

            };            
            
            
            // modal for exporting refset, definition and/or members
            $scope.openImportExportModal = function(lrefset, ldir, lcontentType) {

              console.debug("exportModal ", lrefset);

              var modalInstance = $modal.open({
                templateUrl : 'app/page/refset/importExport.html',
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
                      return $scope.ioImportHandlers;
                    } else {
                      return $scope.ioExportHandlers;
                    }
                  }
                }
              });
            };

            var ImportExportModalCtrl = function($scope, $modalInstance, refset, dir, contentType, ioHandlers) {

              console.debug("Entered import export modal control", refset.id, ioHandlers, dir, contentType);

              $scope.refset = refset;
              $scope.ioHandlers = ioHandlers;
              $scope.selectedIoHandler = $scope.ioHandlers[0];
              $scope.selectedContent = contentType;
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
                  refsetService.beginImportMembers($scope.refset.id, $scope.selectedIoHandler.id).then(

                    // Success
                    function(data) {
                      console.debug("begin import members, valdiation = ",data);
                      // data is a validation result, check for errors
                      if (data.errors.length > 0) {
                        $scope.errors = data.errors;
                      } else {
                      
                        // If there are no errors, finish import
                        refsetService.finishImportMembers($scope.refset.id, $scope.selectedIoHandler.id,file).then(
                          // Success
                          function(data) {
                            $modalInstance.close();
                          },
                          // Failure - close dialog so error messages shows up
                          function(data) {
                            $modalInstance.close();
                          }
                        );
                      }
                    },
                    
                    // Failure - close dialog so error messages shows up
                    function(data) {
                      $modalInstance.close();
                    }
                  );
                }
              };
              
              // Handle continue import
              $scope.continueImport = function(file) {
                console.debug("continue import", $scope.refset.id, file);
                
                if (contentType == 'Refset Members') {
                  refsetService.finishImportMembers($scope.refset.id, $scope.selectedIoHandler.id,file).then(
                    // Success
                    function(data) {
                      $modalInstance.close();
                    },
                    // Failure - close dialog so error messages shows up
                    function(data) {
                      $modalInstance.close();
                    }
                  );
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
            
            
            // modal for exporting refset, definition and/or members
            $scope.openReleaseProcessModal = function(lrefset, lEffectiveTime) {

              console.debug("releaseProcessModal ", lrefset);

              var modalInstance = $modal.open({
                templateUrl : 'app/page/refset/release.html',
                controller : ReleaseProcessModalCtrl,
                backdrop : 'static',
                resolve : {
                  refset : function() {
                    return lrefset;
                  },
                  ioHandlers : function() {
                    return $scope.ioHandlers;
                  },
                  effectiveTime : function() {
                    return lEffectiveTime
                  }
                }
              });
            };

            var ReleaseProcessModalCtrl = function($scope, $modalInstance, refset, ioHandlers, effectiveTime) {

              console.debug("Entered release process modal", refset.id, ioHandlers);

              $scope.refset = refset;
              $scope.ioHandlers = ioHandlers;
              $scope.selectedIoHandler = $scope.ioHandlers[0];
              

              $scope.release = function() {
                console.debug("export", refset.id);


                releaseService.beginRefsetRelease(refset.id, effectiveTime).then(function(data) {
                  releaseService.previewRefsetRelease(refset.id, $scope.selectedIoHandler.id);
                }, function(data){});

                $modalInstance.close();
              };

              $scope.cancel = function() {
                $modalInstance.dismiss('cancel');
              };

            };
            
            
            // modal for choosing a user for refset assignment
            $scope.openChooseUserModal = function(lrefset, laction, luserName) {
              console.debug("openChooseUserModal ", lrefset, laction, luserName);

              var modalInstance = $modal.open({
                templateUrl : 'app/page/refset/chooseUser.html',
                controller : ChooseUserModalCtrl,
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
                    return $scope.assignedUsers;
                  },
                  selectedProject : function() {
                    return $scope.selectedProject;
                  }
                }
              });

            };

            var ChooseUserModalCtrl = function($scope, $modalInstance, refset, action,
              currentUserName, assignedUsers, selectedProject, $rootScope) {

              console.debug("Entered choose user modal control", assignedUsers, selectedProject.id);

              $scope.refset = refset;
              $scope.selectedProject = selectedProject;
              $scope.assignedUserNames = [];
              
              for (var i = 0; i < assignedUsers.length; i++) {
                $scope.assignedUserNames.push(assignedUsers[i].userName);
              }

              $scope.submitChosenUser = function(newUserName) {
                console.debug("Submitting chosen user", newUserName);

                if (newUserName == null 
                  || newUserName == undefined) {
                  window.alert("The user must be selected. ");
                  return;
                }

                $scope.selectedUserName = newUserName;
                if (action == 'ASSIGN') {
                  workflowService.performWorkflowAction($scope.selectedProject.id, refset.id,
                    newUserName, "ASSIGN").then(function(data) {
                    $rootScope.$broadcast('refsetTable:initialize', $scope.selectedProject.id);
                    
                    $modalInstance.close();
                  }, function(data) {
                    $modalInstance.close();
                  })
                } else if (action == 'REASSIGN') {
                  // first unassign something in review, then reassign to an author
                  workflowService.performWorkflowAction($scope.selectedProject.id, refset.id,
                      currentUserName, 'UNASSIGN').then(function(data) {
                        workflowService.performWorkflowAction($scope.selectedProject.id, refset.id, newUserName, 'REASSIGN').then(function(data) {
                          $modalInstance.close();
                        }, function(data) {
                          $modalInstance.close();
                        })
                      })
                  
                }
              };

              $scope.cancel = function() {
                $modalInstance.dismiss('cancel');
              };

            };
            
            
            // modal for creating a new refset 
            // TODO: consolidate "new" and "edit" functionality into same modal and partials page
            // 
            $scope.openNewRefsetModal = function(lrefset) {

              console.debug("openNewRefsetModal ", lrefset);

              var modalInstance = $modal.open({
                templateUrl : 'app/page/refset/newRefset.html',
                controller : NewRefsetModalCtrl,
                backdrop : 'static',
                resolve : {
                  refset : function() {
                    return lrefset;
                  },
                  refsets : function() {
                    return $scope.refsets;
                  },
                  refsetTypes : function() {
                    return $scope.refsetTypes;
                  },
                  project : function() {
                    return $scope.selectedProject;
                  },
                  terminologyEditions : function() {
                    return $scope.terminologyEditions;
                  },
                  terminologyVersions : function() {
                    return $scope.terminologyVersions;
                  }
                }
              });

              modalInstance.result.then(
              // Success
              function() {
                $scope.findAvailableEditingRefsets();               
              });
            };

            var NewRefsetModalCtrl = function($scope, $modalInstance, refset,
              refsets, refsetTypes, project, terminologyEditions, terminologyVersions) {

              console.debug("Entered new refset modal control", refsetTypes);

              $scope.refset = refset;
              $scope.refsetTypes = refsetTypes;
              $scope.terminologyEditions = terminologyEditions;
              $scope.project = project;
              
              
              
              $scope.terminologySelected = function(terminology) {
                $scope.terminologyVersions = terminologyVersions[terminology].sort();
              };


              $scope.submitNewRefset = function(refset) {
                console.debug("Submitting new refset", refset);

                if (refset == null || refset.name == null
                  || refset.name == undefined || refset.description == null
                  || refset.description == undefined) {
                  window.alert("The name and description fields cannot be blank. ");
                  return;
                }

                refset.projectId = project.id;
                // TODO:!! this is hardcoded and almost certainly should not be
                refset.workflowPath = 'DEFAULT';
                // TODO replace with conversion from 20150131 format
                // TODO:!! this is hardcoded and almost certainly should not be
                refset.version = '2015-01-31';
                refsetService.addRefset(refset).then(function(data) {
                  var newRefset = data;
                  refsets.push(newRefset);
                  
                  if (newRefset.type == 'INTENSIONAL') {
                    refsetService.beginRedefinition(newRefset.id, newRefset.definition)
                    .then(function(data) {
                      
                      refsetService.finishRedefinition(newRefset.id)
                      .then(function(data) {  

                        $modalInstance.close();
                      }, function(data) {
                      })
                                           
                    }, function(data) {
                    })
                  } else {
                    $modalInstance.close();
                  }
                }, function(data) {
                  $modalInstance.close();
                })

              };

              $scope.cancel = function() {
                $modalInstance.dismiss('cancel');
              };

            };

            // modal for editing a refset
            $scope.openEditRefsetModal = function(lrefset) {

              console.debug("openEditRefsetModal ");

              var modalInstance = $modal.open({
                templateUrl : 'app/page/refset/editRefset.html',
                controller : EditRefsetModalCtrl,
                backdrop : 'static',
                resolve : {
                  refset : function() {
                    return lrefset;
                  },
                  refsetTypes : function() {
                    return $scope.refsetTypes;
                  },
                  project : function() {
                    return $scope.selectedProject;
                  },
                  terminologyEditions : function() {
                    return $scope.terminologyEditions;
                  },
                  terminologyVersions : function() {
                    return $scope.terminologyVersions;
                  }
                }
              });

              modalInstance.result.then(
              // Success
              function() {
                $scope.getRefsets();
              });
            };

            var EditRefsetModalCtrl = function($scope, $modalInstance, refset, refsetTypes,
              project, terminologyEditions, terminologyVersions, $rootScope) {

              console.debug("Entered edit refset modal control");

              $scope.refset = refset;
              $scope.originalDefinition = $scope.refset.definition;
              $scope.refsetTypes = refsetTypes;
              $scope.terminologyEditions = terminologyEditions;
              $scope.terminologyVersions = terminologyVersions;
              
              $scope.terminologySelected = function(terminology) {
                $scope.terminologyVersions = terminologyVersions[terminology].sort();
              };
              

              $scope.submitEditRefset = function(refset) {
                console.debug("Submitting edit refset", refset);

                if (refset == null || refset.name == null
                  || refset.name == undefined || refset.description == null
                  || refset.description == undefined) {
                  window
                    .alert("The name, description, and terminology fields cannot be blank. ");
                  return;
                }
                refset.releaseInfo = undefined;
                
                refsetService.updateRefset(refset).then(function(data) {
                  if (refset.definition != $scope.originalDefinition) {
                    console.log("need to run redefinition");
                    // TODO: we can't actually do this in the long run
                    // we need to have a "staged" operation where the user can review
                    // the definition change and then cancel back out of it.
                    // a special dialog will be required, etc.  It's fine for "new" but not for "Edit"
                    refsetService.beginRedefinition(refset.id, refset.definition)
                    .then(function(data) {
                      
                      refsetService.finishRedefinition(refset.id)
                      .then(function(data) {  
                        $rootScope.$broadcast('refsetTable:initialize', $scope.selectedProject.id);
                        
                        $modalInstance.close();
                      }, function(data) {
                      })
                                           
                    }, function(data) {
                    })
                  }
                  $modalInstance.close();
                }, function(data) {
                  $modalInstance.close();
                })

              };

              $scope.cancel = function() {
                $modalInstance.dismiss('cancel');
              };

            };


            // modal for creating a new refset member 
              $scope.openNewMemberModal = function(lmember, lrefset) {

                console.debug("openNewMemberModal ", lrefset);

                var modalInstance = $modal.open({
                  templateUrl : 'app/page/refset/newMember.html',
                  controller : NewMemberModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    member : function() {
                      return lmember;
                    },
                    refset : function() {
                      return lrefset;
                    },
                    
                    project : function() {
                      return $scope.selectedProject;
                    },
                    paging : function() {
                      return $scope.paging;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function() {
                  //$scope.findAssignedEditingRefsets();
                });
              };

              var NewMemberModalCtrl = function($scope, $modalInstance, member,
                refset, project, paging) {

                console.debug("Entered new member modal control");
                $scope.pageSize = 10;
                $scope.paging = paging;
                if (refset.type == 'EXTENSIONAL') {
                  $scope.memberType = 'MEMBER';
                }
                if (refset.type == 'INTENSIONAL') {
                  $scope.memberType = 'INCLUSION';
                }

                $scope.submitNewMember = function(concept) {
                  console.debug("Submitting new member", concept);


                  var member = {
                    conceptId : concept.terminologyId,
                    conceptName : concept.name,
                    memberType : $scope.memberType,
                    terminology : refset.terminology,
                    version : refset.version,
                    moduleId : refset.moduleId,
                    terminologyId : concept.terminologyId,
                    lastModifiedBy : concept.lastModifiedBy
                  };

                  member.refsetId = refset.id;

                  if (member.memberType == 'MEMBER') {
                  refsetService.addRefsetMember(member).then(function(data) {
                    if (refset.members == undefined) {
                      refset.members = [];
                    }
                    refset.members.push(data);
                    $modalInstance.close();
                  }, function(data) {
                    $modalInstance.close();
                  })
                  }
                  
                  if (member.memberType == 'INCLUSION') {
                    refsetService.addRefsetInclusion(member.refsetId, member.conceptId).then(function(data) {
                      if (refset.members == undefined) {
                        refset.members = [];
                      }
                      refset.members.push(data);
                      $modalInstance.close();
                    }, function(data) {
                      $modalInstance.close();
                    })
                  }
                  


                };

                // get search results
                $scope.getSearchResults = function(search) {
                  console.debug("Getting search results", search);

                  if (search == null || search == undefined) {
                    window.alert("The search field cannot be blank. ");
                    return;
                  }

                  $scope.searchResults = [];
                  $scope.parents = [];
                  $scope.children = [];
                  $scope.concept = null;
                  
                  // if search term is an id, simply look up the id
                  if (/^\d+$/.test(search)) {

                    projectService.getConceptWithDescriptions(search,
                      refset.terminology, refset.version, pfs).then(
                      function(data) {
                        $scope.searchResults[0] = data;
                        $scope.selectConcept($scope.searchResults[0]);
                      }, function(data) {
                    })                    
                  
                  } else {
                    var pfs = {
                      startIndex : 0,
                      maxResults : 10,
                      sortField : null,
                      queryRestriction : null
                    };

                    projectService.findConceptsForQuery(search,
                      refset.terminology, refset.version, pfs).then(
                      function(data) {
                        $scope.searchResults = data.concepts;
                      }, function(data) {
                      })

                  }
                };

                // select concept and get concept data
                $scope.selectConcept = function(concept) {
                  $scope.selectedConcept = concept;
                  $scope.getConceptParents(concept.terminologyId);
                  $scope.getConceptChildren(concept);
                  $scope.getConceptWithDescriptions(concept.terminologyId);
                };

                
                // get concept parents
                $scope.getConceptParents = function(concept) {
                  console.debug("Getting concept parents", concept);

                  projectService.getConceptParents(concept,
                    concept.terminology, concept.version).then(
                    function(data) {
                      $scope.parents = data.concepts;
                    }, function(data) {
                    })
                };
                
                // get concept children
                $scope.getConceptChildren = function(concept) {
                  console.debug("Getting concept children", concept);

                  var pfs = {
                    startIndex : ($scope.paging["children"].page - 1)
                      * $scope.pageSize,
                    maxResults : $scope.pageSize,
                    sortField : null,
                    queryRestriction : $scope.paging["children"].filter != undefined ?
                      $scope.paging["children"].filter : null
                  };
                  
                  projectService.getConceptChildren(concept.terminologyId,
                    concept.terminology, concept.version, pfs).then(
                    function(data) {
                      $scope.children = data.concepts;
                      $scope.children.totalCount = data.totalCount;
                    }, function(data) {
                    })
                };

                // get concept with descriptions
                $scope.getConceptWithDescriptions = function(concept) {
                  console.debug("Getting concept with descriptions", concept);

                  projectService.getConceptWithDescriptions(concept,
                    concept.terminology, concept.version).then(
                    function(data) {
                      $scope.concept = data;
                    }, function(data) {
                    })
                };

                
                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };

              };

            
          } ]
      }
    } ]);
