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
          value : '@'
        },
        templateUrl : 'app/component/refsetTable/refsetTable.html',
        controller : [
          '$scope',
          function($scope) {
            // Variable
            $scope.iconConfig = projectService.getIconConfig();
            $scope.user = securityService.getUser();
            $scope.refset = null;
            $scope.refsets = null;
            $scope.pageSize = 10;
            $scope.memberTypes = [ "Member", "Exclusion", "Inclusion",
              "Inactive Member", "Inactive Inclusion" ];
            $scope.selectedProject = null;
            $scope.refsetIdToAuthorsMap = {};
            $scope.refsetIdToReviewersMap = {};

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
                $scope.findAssignedUsersForProject();
              })
            };
            
            // get refsets
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
            
            // get assigned users - this is the list of users that are
            // already
            // assigned to the selected project
            $scope.findAssignedUsersForProject = function() {

              var pfs = {
                startIndex : 0,
                maxResults : 100,
                sortField : 'userName',
                queryRestriction : null
              };
              projectService.findAssignedUsersForProject($scope.selectedProject.id,
                "", pfs).then(function(data) {
                $scope.assignedUsers = data.users;
                for (var i = 0; i< $scope.assignedUsers.length; i++) {
                  if ($scope.assignedUsers[i].userName == $scope.user.userName) {
                    $scope.user = $scope.assignedUsers[i];
                 
                  }
                }
                $scope.role = $scope.user.projectRoleMap[$scope.selectedProject.id];
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
                if ($scope.value == 'ASSIGNED' && $scope.role == 'ADMIN') {
                  $scope.findAllAssignedRefsets();
                }
              })

            };
            
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
            
            $scope.findAllAvailableRefsets = function() {
              var pfs = {
                startIndex : 0,
                maxResults : 100,
                sortField : null,
                queryRestriction : null
              };

              workflowService.findAllAvailableRefsets($scope.selectedProject.id, $scope.user.userName, pfs).then(function(data) {
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
            
            // get members
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

            // get current refset release info
            $scope.getCurrentRefsetReleaseInfo = function(refset) {
              releaseService.getCurrentReleaseInfoForRefset(refset.id).then(
                function(data) {
                  refset.releaseInfo = data;
                })
            };
            
            // begin redefinition (or first definition)
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

            // sort mechanism
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

            $scope.selectRefset = function(refset) {
              $scope.refset = refset;
              $scope.getCurrentRefsetReleaseInfo(refset);
              $scope.getMembers(refset);
            };

            $scope.isDisabled = function(member) {
              return member.memberType == 'INACTIVE_MEMBER'
                || member.memberType == 'INACTIVE_INCLUSION'
                || member.memberType == 'EXCLUSION';
            }

            $scope.getMemberStyle = function(member) {
              if (member.memberType == 'MEMBER') {
                return "";
              }
              return member.memberType.replace('_', ' ').toLowerCase();
            }
            
            
            // get refset types
            $scope.getRefsetTypes = function() {
              console.debug("getRefsetTypes");
              refsetService.getRefsetTypes().then(function(data) {
                $scope.refsetTypes = data.strings;
              })
            };
            
            // remove a refset
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
                refsetService.removeRefset(object.id).then(function() {
                  $scope.getRefsets();
                });
              }
              if (type == 'member') {
            
                refsetService.removeRefsetMember(object.id).then(function() {
                  //$scope.getRefsets();
                  objArray.splice(objArray.indexOf(object), 1);
                });
              }
            };
            
            // add a refset exclusion
            $scope.exclude = function(refset, conceptId) {
               
                refsetService.addRefsetExclusion(refset.id, conceptId).then(function() {
                  $scope.getMembers(refset);
                });

            };
            
            $scope.performWorkflowAction = function(refset, action, userName) {
              
              workflowService.performWorkflowAction($scope.selectedProject.id, refset.id,
                userName, action).then(function(data) {
                //$scope.trackingRecord = data.trackingRecord;
                //$scope.initializeProjectAndRefsets($scope.project);

                console.log("rootScope.broadcast", $scope.value);  
                $rootScope.$broadcast('refsetTable:initialize', $scope.selectedProject.id);
                 })
            };
            
            // get terminology Editions
            $scope.getTerminologyEditions = function() {
              console.debug("getTerminologyEditions");
              projectService.getTerminologyEditions().then(function(data) {
                $scope.terminologyEditions = data.strings;

                $scope.getTerminologyVersions($scope.terminologyEditions[0]);
              })
            };
            
            // get terminology versions
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
            
            $scope.getIOHandlers = function() {
              refsetService.getExportRefsetHandlers().then(function(data) {
                $scope.ioHandlers = data.handlers;
              });
            }
            
            $scope.getAuthorsForRefsetId = function(refsetId) {
              return $scope.refsetIdToAuthorsMap[refsetId];
            }
            $scope.getReviewersForRefsetId = function(refsetId) {
              return $scope.refsetIdToReviewersMap[refsetId];
            }
            
            $scope.exportReleaseArtifact = function(artifact) {
              releaseService.exportReleaseArtifact(artifact);
            }
            
            // Initialize
            if ($scope.value == 'PREVIEW' || $scope.value == 'PUBLISHED') {
              $scope.getRefsets();
            }
            $scope.getIOHandlers();
            // this is no longer needed because broadcast of project, causes this to occur
            /*if ($scope.value == 'AVAILABLE' || $scope.value == 'ASSIGNED' 
              || $scope.value == 'ASSIGNED_ALL') {
              $scope.initializeProjectAndRefsets($scope.project);
              $scope.getRefsetTypes();
              $scope.getTerminologyEditions();
            }*/
            

            
            
            //
            // Modals
            //
            // modal for exporting refset, definition and/or members
            $scope.openExportModal = function(lrefset) {

              console.debug("exportModal ", lrefset);

              var modalInstance = $modal.open({
                templateUrl : 'app/page/refset/export.html',
                controller : ExportModalCtrl,
                resolve : {
                  refset : function() {
                    return lrefset;
                  },
                  ioHandlers : function() {
                    return $scope.ioHandlers;
                  }
                }
              });

            };

            var ExportModalCtrl = function($scope, $modalInstance, refset, ioHandlers) {

              console.debug("Entered export modal control", refset.id, ioHandlers);

              $scope.refset = refset;
              $scope.ioHandlers = ioHandlers;
              $scope.selectedIOHandler = $scope.ioHandlers[0];
              $scope.content = ['Full Refset', 'Refset Members'];
              $scope.selectedContent = 'Refset Members';
              
              if ($scope.refset.type == 'INTENSIONAL') {
                $scope.content.push('Definition');
              }

              $scope.export = function() {
                console.debug("export", refset.id);

                if ($scope.selectedIOHandler == null 
                  || $scope.selectedIOHandler == undefined) {
                  window.alert("The I/O handler must be selected. ");
                  return;
                }

                if ($scope.selectedContent == 'Definition') {
                  refsetService.exportDefinition(refset, $scope.selectedIOHandler.id);
                }
                if ($scope.selectedContent == 'Refset Members') {
                  refsetService.exportMembers(refset, $scope.selectedIOHandler.id);
                }
                $modalInstance.close();
              };

              $scope.cancel = function() {
                $modalInstance.dismiss('cancel');
              };

            };
            
            // modal for choosing a user for refset assignment
            $scope.openChooseUserModal = function(lrefset) {

              console.debug("openChooseUserModal ", lrefset);

              var modalInstance = $modal.open({
                templateUrl : 'app/page/refset/chooseUser.html',
                controller : ChooseUserModalCtrl,
                resolve : {
                  refset : function() {
                    return lrefset;
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

            var ChooseUserModalCtrl = function($scope, $modalInstance, refset,
              assignedUsers, selectedProject, $rootScope) {

              console.debug("Entered choose user modal control", assignedUsers, selectedProject.id);

              $scope.refset = refset;
              $scope.selectedProject = selectedProject;
              $scope.assignedUserNames = [];
              
              for (var i = 0; i < assignedUsers.length; i++) {
                $scope.assignedUserNames.push(assignedUsers[i].userName);
              }

              $scope.submitChosenUser = function(userName) {
                console.debug("Submitting chosen user", userName);

                if (userName == null 
                  || userName == undefined) {
                  window.alert("The user must be selected. ");
                  return;
                }

                $scope.selectedUserName = userName;

                workflowService.performWorkflowAction($scope.selectedProject.id, refset.id,
                  userName, "ASSIGN").then(function(data) {
                  $rootScope.$broadcast('refsetTable:initialize', $scope.selectedProject.id);
                    
                  $modalInstance.close();
                }, function(data) {
                  $modalInstance.close();
                })
              };

              $scope.cancel = function() {
                $modalInstance.dismiss('cancel');
              };

            };
            
            
            // modal for creating a new refset 
            $scope.openNewRefsetModal = function(lrefset) {

              console.debug("openNewRefsetModal ", lrefset);

              var modalInstance = $modal.open({
                templateUrl : 'app/page/refset/newRefset.html',
                controller : NewRefsetModalCtrl,
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
                refset.workflowPath = 'DEFAULT';
                // TODO replace with conversion from 20150131 format
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
            // this
            $scope.openEditRefsetModal = function(lrefset) {

              console.debug("openEditRefsetModal ");

              var modalInstance = $modal.open({
                templateUrl : 'app/page/refset/editRefset.html',
                controller : EditRefsetModalCtrl,
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
              project, terminologyEditions, terminologyVersions) {

              console.debug("Entered edit refset modal control");

              $scope.refset = refset;
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

                refsetService.updateRefset(refset).then(function(data) {
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

                  // if search term is an id, simply look up the id
                  if (/^\d+$/.test(search)) {

                    projectService.getConceptWithDescriptions(search,
                      refset.terminology, refset.version, pfs).then(
                      function(data) {
                        $scope.searchResults = data;
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
                  // TODO: add back
                  //$scope.getConceptWithDescriptions(concept.terminologyId);
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
