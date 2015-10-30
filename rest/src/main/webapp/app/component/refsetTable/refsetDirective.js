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
    function(utilService, projectService, refsetService, releaseService, workflowService,
      securityService, $modal) {
      console.debug('configure refsetTable directive');
      return {
        restrict : 'A',
        scope : {
          value : '@',
          project : '@'
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
              ascending : null
            }


            // get all projects where user has a role
            $scope.initializeProjectAndRefsets = function(projectId) {
              projectService.getProject(projectId).then(function(data) {
                $scope.selectedProject = data;
                if ($scope.value == 'AVAILABLE') {
                  $scope.findAvailableEditingRefsets();
                }
                if ($scope.value == 'ASSIGNED') {
                  $scope.findAssignedEditingRefsets();
                }
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
                /*for (var i = 0; i < $scope.refsets.length; i++) {
                  $scope.refsets[i].isExpanded = false;
                }*/
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
              releaseService.getCurrentRefsetRelease(refset.id).then(
                function(data) {
                  refset.releaseInfo = data;
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
            
            $scope.performWorkflowAction = function(action) {

              workflowService.performWorkflowAction($scope.selectedProject.id, $scope.selectedRefset.id,
                $scope.user.userName, action).then(function(data) {
                $scope.trackingRecord = data.trackingRecord;
              })
            };
            
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
                  objArray.splice(objArray.indexOf(object));
                });
              }
            };
            
            $scope.performWorkflowAction = function(refset, action) {
              
              workflowService.performWorkflowAction($scope.selectedProject.id, refset.id,
                $scope.user.userName, action).then(function(data) {
                $scope.trackingRecord = data.trackingRecord;
                $scope.initializeProjectAndRefsets($scope.project);
              })
            };
            
            
            // Initialize
            if ($scope.value == 'PREVIEW' || $scope.value == 'PUBLISHED') {
              $scope.getRefsets();
            }
            if ($scope.value == 'AVAILABLE' || $scope.value == 'ASSIGNED') {
              $scope.initializeProjectAndRefsets($scope.project);
              $scope.getRefsetTypes();
            }
            

            
            
            //
            // Modals
            //

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
              refsets, refsetTypes, project) {

              console.debug("Entered new refset modal control", refsetTypes);

              $scope.refset = refset;
              $scope.refsetTypes = refsetTypes;

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
                refsetService.addRefset(refset).then(function(data) {
                  refsets.push(data);
                  $modalInstance.close();
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
                  }
                }
              });

              modalInstance.result.then(
              // Success
              function() {
                $scope.getRefsets();
              });
            };

            var EditRefsetModalCtrl = function($scope, $modalInstance, refset, refsetTypes, project) {

              console.debug("Entered edit refset modal control");

              $scope.refset = refset;
              $scope.refsetTypes = refsetTypes;

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
                    memberTypes : function() {
                      return $scope.memberTypes;
                    },
                    project : function() {
                      return $scope.selectedProject;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function() {
                  $scope.findAssignedEditingRefsets();
                });
              };

              var NewMemberModalCtrl = function($scope, $modalInstance, member,
                refset, memberTypes, project) {

                console.debug("Entered new member modal control");

                $scope.submitNewMember = function(concept) {
                  console.debug("Submitting new member", concept);

                  var member = {
                    conceptId : concept.terminologyId,
                    conceptName : concept.name,
                    memberType : 'MEMBER',
                    terminology : refset.terminology,
                    version : refset.version,
                    moduleId : refset.moduleId,
                    terminologyId : concept.terminologyId,
                    lastModifiedBy : concept.lastModifiedBy
                  };

                  member.refsetId = refset.id;
                  
                  refsetService.addRefsetMember(member).then(function(data) {
                    refset.members.push(data);
                    $modalInstance.close();
                  }, function(data) {
                    $modalInstance.close();
                  })

                };

                $scope.getSearchResults = function(search) {
                  console.debug("Getting search results", search);

                  if (search == null || search == undefined) {
                    window.alert("The search field cannot be blank. ");
                    return;
                  }

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

                };

                $scope.selectConcept = function(concept) {
                  $scope.selectedConcept = concept;
                };

                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };

              };

            
          } ]
      }
    } ]);
