// Refset Table directive
// e.g. <div refset-table value="PUBLISHED" />
<<<<<<< .mine
tsApp
  .directive(
    'refsetTable',
    [
      'utilService',
      'projectService',
      'refsetService',
      'releaseService',
      'workflowService',
      'securityService',
      '$modal',
      '$rootScope',
      function(utilService, projectService, refsetService, releaseService,
        workflowService, securityService, $modal, $rootScope) {
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
              $scope.selectedRefset = null;
              $scope.refsetIdToAuthorsMap = {};
              $scope.refsetIdToReviewersMap = {};
=======
tsApp
  .directive(
    'refsetTable',
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
            // For refset tab: AVAILABLE, ASSIGNED, ASSIGNED_ALL,
            // RELEASE
            value : '@',
            projects : '='
          },
          templateUrl : 'app/component/refsetTable/refsetTable.html',
          controller : [
            '$scope',
            function($scope) {







>>>>>>> .theirs

<<<<<<< .mine
              $scope.paging = {};
              $scope.paging["refset"] = {
                page : 1,
                filter : "",
                sortField : 'name',
                ascending : null



















=======
              // Variables
              $scope.iconConfig = projectService.getIconConfig();
              $scope.user = securityService.getUser();
              $scope.refset = null;
              $scope.refsetReleaseInfo = null;
              $scope.refsets = null;
              $scope.project = null;

              // Page metadata
              $scope.memberTypes = [ "Member", "Exclusion", "Inclusion", "Inactive Member",
                "Inactive Inclusion" ];
              $scope.refsetTypes = null;

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
>>>>>>> .theirs
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

<<<<<<< .mine
              $scope.$on('refsetChanged', function(event, data) {
                console.log('on refsetTable:initialize', data, $scope.value);
                $scope.initializeProjectAndRefsets(data);
              });

              $scope.$on('projectChanged',
                function(event, data) {
                  console.log('on refset:project', data);
                  $scope.selectedProject = data;
                  if ($scope.selectedProject != undefined
                    && $scope.selectedProject != null) {
                    $scope
                      .initializeProjectAndRefsets($scope.selectedProject.id);
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
=======
              $scope.ioImportHandlers = [];
              $scope.ioExportHandlers = [];
























>>>>>>> .theirs

<<<<<<< .mine
              // get refsets
              $scope.getRefsets = function() {






=======
              // Refset Changed handler
              $scope.$on('refset:refsetChanged', function(event, data) {
                console.log('on refset:refsetChanged', data);
                // If the refset is set, refresh refsets list
                if (data) {
                  $scope.getRefsets();
                }
              });
>>>>>>> .theirs

<<<<<<< .mine
                var pfs = {
                  startIndex : ($scope.paging["refset"].page - 1)
                    * $scope.pageSize,
                  maxResults : $scope.pageSize,
                  sortField : $scope.paging["refset"].sortField,
                  ascending : $scope.paging["refset"].ascending == null ? true
                    : $scope.paging["refset"].ascending,
                  queryRestriction : 'workflowStatus:' + $scope.value
                };

                refsetService.findRefsetsForQuery(
                  $scope.paging["refset"].filter, pfs).then(function(data) {
                  $scope.refsets = data.refsets;
                  $scope.refsets.totalCount = data.totalCount;
                })








=======
              // Project Changed Handler
              $scope.$on('refset:projectChanged', function(event, data) {
                console.log('on refset:projectChanged', data);
                $scope.project = data;
                if (!$scope.project) {
                  return;
                }

                $scope.getProject($scope.project.id);

              });

              // Tests that the key has an icon
              $scope.hasIcon = function(key) {
                return projectService.hasIcon(key);
              }

              // Returns the icon path for the key (moduleId or namespaceId)
              $scope.getIcon = function(key) {
                return projectService.getIcon(key);
              }

              // Get $scope.project and reload
              // $scope.refsets
              $scope.getProject = function(projectId) {
                console.debug("getProject", projectId);
                projectService.getProject(projectId).then(function(data) {
                  console.debug("  project = ", data);
                  $scope.project = data;

                  // Refresh refsets
                  $scope.getRefsets();
                })
>>>>>>> .theirs
              };
<<<<<<< .mine











































































































=======

              // Get $scope.refsets
              $scope.getRefsets = function() {
                var pfs = {
                  startIndex : ($scope.paging["refset"].page - 1) * $scope.pageSize,
                  maxResults : $scope.pageSize,
                  sortField : $scope.paging["refset"].sortField,
                  ascending : $scope.paging["refset"].ascending == null ? true
                    : $scope.paging["refset"].ascending,
                  queryRestriction : null
                };
                console.debug("PFS=", pfs);
                console.debug("paging=", $scope.paging["refset"]);
                if ($scope.value == 'PUBLISHED' || $scope.value == 'PREVIEW') {
                  pfs.queryRestriction = 'workflowStatus:' + $scope.value;
                  refsetService.findRefsetsForQuery($scope.paging["refset"].filter, pfs).then(
                    function(data) {
                      $scope.refsets = data.refsets;
                      $scope.refsets.totalCount = data.totalCount;
                      // Refresh the refset if it is
                      // selected
                      if ($scope.refset) {
                        $scope.selectRefset(refset);
                      }
                    })
                }

                if ($scope.value == 'AVAILABLE' && $scope.user.role == 'AUTHOR') {
                  workflowService.findAvailableEditingRefsets($scope.project.id,
                    $scope.user.userName, pfs).then(function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                  });
                }
                if ($scope.value == 'AVAILABLE' && $scope.user.role == 'REVIEWER') {
                  workflowService.findAvailableReviewRefsets($scope.project.id,
                    $scope.user.userName, pfs).then(function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                  })
                }
                if ($scope.value == 'AVAILABLE' && $scope.user.role == 'ADMIN') {
                  workflowService.findAllAvailableRefsets($scope.project.id, pfs).then(
                    function(data) {
                      $scope.refsets = data.refsets;
                      $scope.refsets.totalCount = data.totalCount;
                    })
                }
                if ($scope.value == 'ASSIGNED_ALL' && $scope.user.role == 'ADMIN') {
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
                if ($scope.value == 'ASSIGNED' && $scope.user.role == 'AUTHOR') {
                  workflowService.findAssignedEditingRefsets($scope.project.id,
                    $scope.user.userName, pfs).then(function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                  })
                }
                if ($scope.value == 'ASSIGNED' && $scope.user.role == 'REVIEWER') {
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
                  pfs.queryRestriction += "memberType:" + value;
                }
>>>>>>> .theirs

<<<<<<< .mine
              // get assigned users - this is the list of users that are
              // already
              // assigned to the selected project
              $scope.initializeUsersAndRefsets = function() {

                var pfs = {
                  startIndex : 0,
                  maxResults : 100,
                  sortField : 'userName',
                  queryRestriction : null
                };
                projectService
                  .findAssignedUsersForProject($scope.selectedProject.id, "",
                    pfs)
                  .then(
                    function(data) {
                      $scope.assignedUsers = data.users;
                      for (var i = 0; i < $scope.assignedUsers.length; i++) {
                        if ($scope.assignedUsers[i].userName == $scope.user.userName) {
                          $scope.user = $scope.assignedUsers[i];

                        }
                      }
                      $scope.role = $scope.user.projectRoleMap[$scope.selectedProject.id];
                      console.debug("project role: ", $scope.role);

                      if ($scope.value == 'AVAILABLE'
                        && $scope.role == 'AUTHOR') {
                        $scope.findAvailableEditingRefsets();
                      }
                      if ($scope.value == 'AVAILABLE'
                        && $scope.role == 'REVIEWER') {
                        $scope.findAvailableReviewRefsets();
                      }
                      if ($scope.value == 'AVAILABLE' && $scope.role == 'ADMIN') {
                        $scope.findAllAvailableRefsets();
                      }
                      if ($scope.value == 'ASSIGNED_ALL'
                        && $scope.role == 'ADMIN') {
                        $scope.findAllAssignedRefsets();
                      }
                      if ($scope.value == 'ASSIGNED' && $scope.role == 'AUTHOR') {
                        $scope.findAssignedEditingRefsets();
                      }
                      if ($scope.value == 'ASSIGNED'
                        && $scope.role == 'REVIEWER') {
                        $scope.findAssignedReviewRefsets();
                      }
                      if ($scope.value == 'ASSIGNED' && $scope.role == 'ADMIN') {
                        $scope.findAllAssignedRefsets();
                      }
                      if ($scope.value == 'RELEASE'
                        && ($scope.role == 'REVIEWER' || $scope.role == 'ADMIN')) {
                        $scope.findReleaseRefsets();
                      }
                      
                      if ($scope.refset != null) {
                        $scope.getMembers($scope.refset);
                      }
                    })

=======
                refsetService.findRefsetMembersForQuery(refset.id, $scope.paging["member"].filter,
                  pfs).then(function(data) {
                  refset.members = data.members;
                  refset.members.totalCount = data.totalCount;
                })
























































>>>>>>> .theirs
              };

<<<<<<< .mine
              $scope.findAvailableEditingRefsets = function() {
                var pfs = {
                  startIndex : 0,
                  maxResults : 100,
                  sortField : null,
                  queryRestriction : null
                };

                workflowService.findAvailableEditingRefsets(
                  $scope.selectedProject.id, $scope.user.userName, pfs).then(
                  function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                  })
=======
              // Get $scope.refsetReleaseInfo
              $scope.getCurrentRefsetReleaseInfo = function(refset) {
                $scope.refsetReleaseInfo = null;
                releaseService.getCurrentReleaseInfoForRefset(refset.id).then(function(data) {
                  $scope.refsetReleaseInfo = data;
                })








>>>>>>> .theirs
              };

<<<<<<< .mine
              $scope.findAvailableReviewRefsets = function() {
                var pfs = {
                  startIndex : 0,
                  maxResults : 100,
                  sortField : null,
                  queryRestriction : null
                };

                workflowService.findAvailableReviewRefsets(
                  $scope.selectedProject.id, $scope.user.userName, pfs).then(
                  function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                  })
=======
              // Begin redefinition (or first definition)
              $scope.beginRedefinition = function(refsetId, definition) {
                refsetService.beginRedefinition(refset.id, definition).then(function(data) {
                  console.debug("data", data);
                })









>>>>>>> .theirs
              };

<<<<<<< .mine
              $scope.findAllAvailableRefsets = function() {
                var pfs = {
                  startIndex : 0,
                  maxResults : 100,
                  sortField : null,
                  queryRestriction : null
                };

                workflowService.findAllAvailableRefsets(
                  $scope.selectedProject.id, $scope.user.userName, pfs).then(
                  function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                  })
=======
              // Convert date to a string
              $scope.toDate = function(lastModified) {
                return utilService.toDate(lastModified);











>>>>>>> .theirs
              };

<<<<<<< .mine
              $scope.findAssignedEditingRefsets = function() {
                var pfs = {
                  startIndex : 0,
                  maxResults : 100,
                  sortField : null,
                  queryRestriction : null
                };

                workflowService.findAssignedEditingRefsets(
                  $scope.selectedProject.id, $scope.user.userName, pfs).then(
                  function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                  })
=======
              // Convert date to a string
              $scope.toShortDate = function(lastModified) {
                return utilService.toShortDate(lastModified);











>>>>>>> .theirs
              };

<<<<<<< .mine
              $scope.findAllAssignedRefsets = function() {
                var pfs = {
                  startIndex : 0,
                  maxResults : 100,
                  sortField : null,
                  queryRestriction : null
                };



















=======
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
>>>>>>> .theirs

<<<<<<< .mine
                workflowService
                  .findAllAssignedRefsets($scope.selectedProject.id, '', pfs)
                  .then(
                    function(data) {
                      $scope.refsets = data.refsets;
                      $scope.refsets.totalCount = data.totalCount;

                      // get refset tracking records in order to get refset
                      // authors
                      for (var i = 0; i < $scope.refsets.length; i++) {
                        workflowService
                          .getTrackingRecordForRefset($scope.refsets[i].id)
                          .then(
                            function(data) {
                              $scope.refsetIdToAuthorsMap[data.refsetId] = data.authors;
                              $scope.refsetIdToReviewersMap[data.refsetId] = data.reviewers;
                            });
                      }
                    })
=======
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








>>>>>>> .theirs
              };

<<<<<<< .mine
              $scope.findAssignedReviewRefsets = function() {
                var pfs = {
                  startIndex : 0,
                  maxResults : 100,
                  sortField : null,
                  queryRestriction : null
                };

                workflowService.findAssignedReviewRefsets(
                  $scope.selectedProject.id, $scope.user.userName, pfs).then(
                  function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                  })
=======
              // Selects a refset (setting $scope.refset).
              // Looks up current release info and members.
              $scope.selectRefset = function(refset) {
                $scope.refset = refset;
                $scope.getCurrentRefsetReleaseInfo(refset);
                $scope.getMembers(refset);








>>>>>>> .theirs
              };

<<<<<<< .mine
              $scope.findReleaseRefsets = function() {
                var pfs = {
                  startIndex : 0,
                  maxResults : 100,
                  sortField : null,
                  queryRestriction : null
                };

                workflowService.findReleaseProcessRefsets(
                  $scope.selectedProject.id, $scope.user.userName, pfs).then(
                  function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                  })
              };
=======
              // Used for styling inactive/disabled
              $scope.isDisabled = function(member) {
                return member.memberType == 'INACTIVE_MEMBER'
                  || member.memberType == 'INACTIVE_INCLUSION' || member.memberType == 'EXCLUSION';
              }










>>>>>>> .theirs

<<<<<<< .mine
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
=======
              // Used for styling - coordinated with css file
              // TODO: this can be better
              $scope.getMemberStyle = function(member) {
                if (member.memberType == 'MEMBER') {
                  return "";
























































>>>>>>> .theirs
                }
<<<<<<< .mine
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
=======
                return member.memberType.replace('_', ' ').toLowerCase();








































>>>>>>> .theirs
              }

<<<<<<< .mine
              $scope.getMemberStyle = function(member) {
                if (member.memberType == 'MEMBER') {
                  return "";
                }
                return member.memberType.replace('_', ' ').toLowerCase();
              }

=======
              // Get $scope.refsetTypes - for picklist
              $scope.getRefsetTypes = function() {
                console.debug("getRefsetTypes");
                refsetService.getRefsetTypes().then(function(data) {
                  $scope.refsetTypes = data.strings;
                })
              };
>>>>>>> .theirs

<<<<<<< .mine
              // get refset types
              $scope.getRefsetTypes = function() {
                console.debug("getRefsetTypes");
                refsetService.getRefsetTypes().then(function(data) {
                  $scope.refsetTypes = data.strings;
                })
              };

              // remove a refset
              $scope.remove = function(type, object, objArray) {
                if (!confirm("Are you sure you want to remove the " + type
                  + " (" + object.name + ")?")) {
=======
              // Remove a refset or a refset member
              $scope.remove = function(type, object, objArray) {
                if (!confirm("Are you sure you want to remove the " + type + " (" + object.name
                  + ")?")) {








>>>>>>> .theirs
                  return;
                }
<<<<<<< .mine
                if (type == 'refset') {
                  if (object.userRoleMap != null
                    && object.userRoleMap != undefined
                    && Object.keys(object.userRoleMap).length > 0) {
                    window
                      .alert("You can not delete a project that has users assigned to it. Remove the assigned users before deleting the project.");
=======
                if (type == 'refset') {
                  if (object.userRoleMap != null && object.userRoleMap != undefined
                    && Object.keys(object.userRoleMap).length > 0) {
                    window
                      .alert("You can not delete a project that has users assigned to it. Remove the assigned users before deleting the project.");

>>>>>>> .theirs
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
<<<<<<< .mine
                if (type == 'member') {

                  refsetService.removeRefsetMember(object.id).then(function() {
                    // $scope.getRefsets();
                    objArray.splice(objArray.indexOf(object), 1);
                  });
                }
              };

              // add a refset exclusion
              $scope.exclude = function(refset, conceptId) {

                refsetService.addRefsetExclusion(refset.id, conceptId).then(
                  function() {
                    $scope.getMembers(refset);
                  });

              };

              $scope.performWorkflowAction = function(refset, action, userName) {

                workflowService.performWorkflowAction(
                  $scope.selectedProject.id, refset.id, userName, action).then(
                  function(data) {
                    refsetService.fireRefsetChanged($scope.selectedProject.id);
                  })
              };
              
              $scope.removeAllRefsetMembers = function(refset) {
                if (!confirm("Are you sure you want to remove all the members of the refset" 
                  + " (" + refset.name + ")?")) {
                  return;
                }
                refsetService.removeAllRefsetMembers(refset.id).then(
                  function(data) {                    
                    refsetService.fireRefsetChanged($scope.selectedProject.id)
                        $scope.selectRefset(refset);
                  })
              };

              // get terminology editions
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
                projectService.getTerminologyVersions(terminology).then(
                  function(data) {
                    $scope.terminologyVersions = {};
                    $scope.terminologyVersions[terminology] = [];
                    for (var i = 0; i < data.translations.length; i++) {
                      $scope.terminologyVersions[terminology]
                        .push(data.translations[i].version.replace(/-/gi, ""));
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
=======
                if (type == 'member') {

                  refsetService.removeRefsetMember(object.id).then(function() {
                    // $scope.getRefsets();
                    objArray.splice(objArray.indexOf(object), 1);
                  });
                }
              };

              // Adds a refset exclusion and refreshes member
              // list
              // with current PFS settings
              $scope.exclude = function(refset, conceptId) {

                refsetService.addRefsetExclusion(refset.id, conceptId).then(function() {
                  $scope.getMembers(refset);
                });



























































>>>>>>> .theirs

<<<<<<< .mine
              $scope.exportReleaseArtifact = function(artifact) {
                releaseService.exportReleaseArtifact(artifact);
              }
=======
              };


>>>>>>> .theirs

<<<<<<< .mine
              // get all projects where user has a role
              $scope.retrieveCandidateProjects = function() {
=======
              // Performs a workflow action
              $scope.performWorkflowAction = function(refset, action, userName) {
>>>>>>> .theirs

<<<<<<< .mine
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
=======
                workflowService.performWorkflowAction($scope.project.id, refset.id, userName,
                  action).then(function(data) {
                  refsetService.fireRefsetChanged(refset);
                })
              };





>>>>>>> .theirs

<<<<<<< .mine
                projectService.findProjectsAsList("", pfs).then(function(data) {
                  $scope.candidateProjects = data.projects;
                  $scope.candidateProjects.totalCount = data.totalCount;
                  // $scope.initializeUsersAndRefsets();
                })



=======
              // Get $scope.terminologyEditions, also loads
              // versions for the first edition in the list
              $scope.getTerminologyEditions = function() {
                console.debug("getTerminologyEditions");
                projectService.getTerminologyEditions().then(function(data) {
                  $scope.terminologyEditions = data.strings;
                  $scope.getTerminologyVersions($scope.terminologyEditions[0]);
                })
>>>>>>> .theirs
              };
<<<<<<< .mine
























=======

              // Get $scope.terminologyVersions
              $scope.getTerminologyVersions = function(terminology) {
                console.debug("getTerminologyVersions");
                projectService.getTerminologyVersions(terminology).then(
                  function(data) {
                    $scope.terminologyVersions = {};
                    $scope.terminologyVersions[terminology] = [];
                    for (var i = 0; i < data.translations.length; i++) {
                      $scope.terminologyVersions[terminology].push(data.translations[i].version
                        .replace(/-/gi, ""));
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
>>>>>>> .theirs

<<<<<<< .mine
              // reassign to author refset that is in review process
              $scope.performReassign = function(refset) {
                // first unassign, then assign to author who worked on it
                workflowService.performWorkflowAction(
                  $scope.selectedProject.id, refset.id, $scope.user.userName,
                  'UNASSIGN').then(
                  function(data) {
                    // refsetService.fireRefsetChanged($scope.selectedProject.id).then(function(data)
                    // {
                    // newUserName = $scope.getAuthorsForRefsetId(refset.id)[0];
                    workflowService.performWorkflowAction(
                      $scope.selectedProject.id, refset.id,
                      $scope.user.userName, 'REASSIGN').then(
                      function(data) {
                        refsetService
                          .fireRefsetChanged($scope.selectedProject.id);
                      }, function(data) {
                      })
                    // })








=======
              // TODO: remove as I believe these are unused
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
                    }, function(data) {
                    })
>>>>>>> .theirs
                  })
              };

<<<<<<< .mine
              // Initialize
              if ($scope.value == 'PREVIEW' || $scope.value == 'PUBLISHED') {
                $scope.getRefsets();
              }
              $scope.getIOHandlers();
              $scope.retrieveCandidateProjects();
              // this is no longer needed because broadcast of project, causes
              // this to occur
              /*
               * if ($scope.value == 'AVAILABLE' || $scope.value == 'ASSIGNED' ||
               * $scope.value == 'ASSIGNED_ALL') {
               * $scope.initializeProjectAndRefsets($scope.project);
               * $scope.getRefsetTypes(); $scope.getTerminologyEditions(); }
               */
=======
              // Initialize
              if ($scope.value == 'PREVIEW' || $scope.value == 'PUBLISHED') {
                $scope.getRefsets();
              }
              // Initialize some metadata first time
              $scope.getRefsetTypes();
              $scope.getTerminologyEditions();
              $scope.getIOHandlers();






>>>>>>> .theirs

<<<<<<< .mine
              //
              // Modals
              //  
              // modal for creating a clone of a refset
              $scope.openCloneRefsetModal = function(lrefset) {
=======
              //
              // Modals:
              //


>>>>>>> .theirs

<<<<<<< .mine
                console.debug("cloneRefsetModal ", lrefset);


=======
              // Clone Refset modal
              $scope.openCloneRefsetModal = function(lrefset) {
                console.debug("cloneRefsetModal ", lrefset);
>>>>>>> .theirs

<<<<<<< .mine
                var modalInstance = $modal.open({
                  templateUrl : 'app/page/refset/clone.html',
                  controller : CloneRefsetModalCtrl,
                  resolve : {
                    refset : function() {
                      return lrefset;
                    },
                    ioHandlers : function() {
                      return $scope.ioHandlers;
                    },
                    candidateProjects : function() {
                      return $scope.candidateProjects;
                    }
=======
                var modalInstance = $modal.open({
                  templateUrl : 'app/page/refset/clone.html',
                  controller : CloneRefsetModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
                    },
                    projects : function() {
                      return $scope.projects;
                    }


>>>>>>> .theirs
                  }
                });

                modalInstance.result.then(
                // Success
                function() {
<<<<<<< .mine
                  $scope.findAvailableEditingRefsets();
=======
                  refsetService.fireRefsetChanged(lrefset);
>>>>>>> .theirs
                });

              };

<<<<<<< .mine
              var CloneRefsetModalCtrl = function($scope, $modalInstance,
                refset, ioHandlers, candidateProjects) {

=======
              // Clone Refset controller
              var CloneRefsetModalCtrl = function($scope, $modalInstance, refset, projects) {
                console.debug("Entered clone refset modal control", refset.id);
>>>>>>> .theirs

<<<<<<< .mine
                console.debug("Entered clone refset modal control", refset.id);





=======
                $scope.projects = projects;
                $scope.refset = refset;
                $scope.refset.releaseInfo = undefined;
                $scope.newRefset = {
                  terminologyId : null
                };
>>>>>>> .theirs

<<<<<<< .mine
                $scope.candidateProjects = candidateProjects;
                $scope.refset = refset;
                $scope.refset.releaseInfo = undefined;



=======
                $scope.clone = function() {
                  console.debug("clone refset", refset.id);
                  refsetService.cloneRefset($scope.refset, $scope.newRefset.project.id,
                    $scope.newRefset.terminologyId);
                  $modalInstance.close();
                };
>>>>>>> .theirs

<<<<<<< .mine
                $scope.newRefset = {};
                $scope.newRefset.terminologyId = null;

=======
                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };
>>>>>>> .theirs

<<<<<<< .mine
                $scope.clone = function() {
                  console.debug("clone refset", refset.id);
=======
              };

>>>>>>> .theirs

<<<<<<< .mine
                  refsetService
                    .cloneRefset($scope.refset, $scope.newRefset.project.id,
                      $scope.newRefset.terminologyId);
=======
              // Import/Export modal
              $scope.openImportExportModal = function(lrefset, ldir, lcontentType) {
                console.debug("exportModal ", lrefset);
>>>>>>> .theirs

<<<<<<< .mine
                  $modalInstance.close();
                };

                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };

















=======
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
>>>>>>> .theirs
              };

<<<<<<< .mine
              // modal for exporting refset, definition and/or members
              $scope.openImportExportModal = function(lrefset, ldir,
                lcontentType) {

                console.debug("exportModal ", lrefset);

                var modalInstance = $modal.open({
                  templateUrl : 'app/page/refset/export.html',
                  controller : ImportExportModalCtrl,
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
                      return $scope.ioHandlers;
                    }
                  }
                });
              };
=======
              // Import/Export controller
              var ImportExportModalCtrl = function($scope, $modalInstance, refset, dir,
                contentType, ioHandlers) {
                console.debug("Entered import export modal control", refset.id, ioHandlers, dir,
                  contentType);




















>>>>>>> .theirs

<<<<<<< .mine
              var ImportExportModalCtrl = function($scope, $modalInstance,
                refset, dir, contentType, ioHandlers) {




=======
                $scope.refset = refset;
                $scope.ioHandlers = ioHandlers;
                $scope.selectedIoHandler = $scope.ioHandlers[0];
                $scope.selectedContent = contentType;
                $scope.dir = dir;
                $scope.errors = [];
>>>>>>> .theirs

<<<<<<< .mine
                console.debug("Entered import export modal control", refset.id,
                  ioHandlers, dir, contentType);

=======
                // Handle export
                $scope.export = function(file) {
                  console.debug("export", $scope.refset.id, file);
>>>>>>> .theirs

<<<<<<< .mine
                $scope.refset = refset;
                $scope.ioHandlers = ioHandlers;
                $scope.selectedIOHandler = $scope.ioHandlers[0];
                $scope.selectedContent = contentType;
                $scope.dir = dir;

                $scope.export = function() {
                  console.debug("export", refset.id);

                  if (contentType == 'Definition') {
                    refsetService.exportDefinition(refset.id,
                      $scope.selectedIOHandler.id,
                      $scope.selectedIOHandler.fileTypeFilter);
=======
                  if (contentType == 'Definition') {
                    refsetService.exportDefinition($scope.refset.id, $scope.selectedIoHandler.id,
                      $scope.selectedIoHandler.fileTypeFilter);










>>>>>>> .theirs
                  }
<<<<<<< .mine
                  if (contentType == 'Refset Members') {
                    refsetService.exportMembers(refset.id,
                      $scope.selectedIOHandler.id,
                      $scope.selectedIOHandler.fileTypeFilter);
                  }
                  $modalInstance.close();
                };
=======
                  if (contentType == 'Refset Members') {
                    refsetService.exportMembers($scope.refset.id, $scope.selectedIoHandler.id,
                      $scope.selectedIoHandler.fileTypeFilter);
                  }
                  $modalInstance.close();
                };

>>>>>>> .theirs

<<<<<<< .mine
                $scope.import = function() {
                  console.debug("import", refset.id);

=======
                // Handle import
                $scope.import = function(file) {
                  console.debug("import", $scope.refset.id, file);
>>>>>>> .theirs

<<<<<<< .mine
                  if (contentType == 'Definition') {
                    refsetService.importDefinition(refset.id,
                      $scope.selectedIOHandler.id,
                      $scope.selectedIOHandler.fileTypeFilter);
                  }
                  if (contentType == 'Refset Members') {
                    refsetService.importMembers(refset.id,
                      $scope.selectedIOHandler.id,
                      $scope.selectedIOHandler.fileTypeFilter);
                  }
                  $modalInstance.close();
                };
=======
                  if (contentType == 'Definition') {
                    refsetService.importDefinition($scope.refset.id, $scope.selectedIoHandler.id,
                      $scope.selectedIoHandler.fileTypeFilter);
                  }








>>>>>>> .theirs

<<<<<<< .mine
                $scope.onFileSelect = function($files) {
                  console.debut("onFileSelect");
                  // $files: an array of files selected, each file
                  // has name, size, and type.
                  /*
                   * for (var i = 0; i < $files.length; i++) { var $file =
                   * $files[i]; $rootScope.glassPane++; $upload .upload({ url :
                   * refsetUrl + "import/definition?handlerId=" + handlerId +
                   * "&refsetId=" + refsetId, file : $file, progress :
                   * function(e) { } }) .error(function(data, status, headers,
                   * config) { // file is not uploaded // successfully // TODO..
                   * handle error }) .success( function(data) { // file is
                   * uploaded // successfully, "data" contains the filename }); }
                   */
                };
=======
                  if (contentType == 'Refset Members') {
                    refsetService.beginImportMembers($scope.refset.id, $scope.selectedIoHandler.id)
                      .then(












>>>>>>> .theirs

<<<<<<< .mine
                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };




=======
                        // Success
                        function(data) {
                          console.debug("begin import members, valdiation = ", data);
                          // data is a validation result, check for errors
                          if (data.errors.length > 0) {
                            $scope.errors = data.errors;
                          } else {
>>>>>>> .theirs

<<<<<<< .mine
              };














=======
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
>>>>>>> .theirs

<<<<<<< .mine
              // modal for exporting refset, definition and/or members
              $scope.openReleaseProcessModal = function(lrefset, lEffectiveTime) {






































=======
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
>>>>>>> .theirs

<<<<<<< .mine
<<<<<<< .mine
                console.debug("releaseProcessModal ", lrefset);

=======




=======
              // Directive scoped method for cancelling an import
              $scope.cancelImport = function(refset) {
                $scope.refset = refset;
                refsetService.cancelImportMembers($scope.refset.id).then(new function() {
                  refsetService.fireRefsetChanged($scope.refset);
                });
              };

>>>>>>> .theirs
              // Release Process modal
              $scope.openReleaseProcessModal = function(lrefset, lEffectiveTime) {
>>>>>>> .theirs

<<<<<<< .mine
                var modalInstance = $modal.open({
                  templateUrl : 'app/page/refset/release.html',
                  controller : ReleaseProcessModalCtrl,
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



=======
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
>>>>>>> .theirs
                  }
                });
              };

<<<<<<< .mine
              var ReleaseProcessModalCtrl = function($scope, $modalInstance,
                refset, ioHandlers, effectiveTime) {

=======
              // Release Process controller
              var ReleaseProcessModalCtrl = function($scope, $modalInstance, refset, ioHandlers,
                effectiveTime) {
>>>>>>> .theirs

<<<<<<< .mine
                console.debug("Entered release process modal", refset.id,
                  ioHandlers);
=======
                console.debug("Entered release process modal", refset.id, ioHandlers);

>>>>>>> .theirs

<<<<<<< .mine
                $scope.refset = refset;
                $scope.ioHandlers = ioHandlers;
                $scope.selectedIOHandler = $scope.ioHandlers[0];
=======
                $scope.refset = refset;
                $scope.ioHandlers = ioHandlers;
                $scope.selectedIoHandler = $scope.ioHandlers[0];
>>>>>>> .theirs

                $scope.release = function() {
                  console.debug("export", refset.id);

<<<<<<< .mine
                  releaseService.beginRefsetRelease(refset.id, effectiveTime)
                    .then(
                      function(data) {
                        releaseService.previewRefsetRelease(refset.id,
                          $scope.selectedIOHandler.id);
                      }, function(data) {
                      });
=======
                  releaseService.beginRefsetRelease(refset.id, effectiveTime).then(function(data) {
                    releaseService.previewRefsetRelease(refset.id, $scope.selectedIoHandler.id);
                  }, function(data) {
                  });



>>>>>>> .theirs

                  $modalInstance.close();
                };

<<<<<<< .mine
                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };

              };
=======
                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };


>>>>>>> .theirs

<<<<<<< .mine
              // modal for choosing a user for refset assignment
              $scope.openChooseUserModal = function(lrefset, laction, luserName) {
                console.debug("openChooseUserModal ", lrefset, laction,
                  luserName);

                var modalInstance = $modal.open({
                  templateUrl : 'app/page/refset/chooseUser.html',
                  controller : ChooseUserModalCtrl,
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

=======



























>>>>>>> .theirs
              };

<<<<<<< .mine
              var ChooseUserModalCtrl = function($scope, $modalInstance,
                refset, action, currentUserName, assignedUsers,
                selectedProject, $rootScope) {
=======
              // Choose User modal
              $scope.openChooseUserModal = function(lrefset, laction, luserName) {
                console.debug("openChooseUserModal ", lrefset, laction, luserName);
>>>>>>> .theirs

<<<<<<< .mine
                console.debug("Entered choose user modal control",
                  assignedUsers, selectedProject.id);

                $scope.refset = refset;
                $scope.selectedProject = selectedProject;
                $scope.assignedUserNames = [];

                for (var i = 0; i < assignedUsers.length; i++) {
                  $scope.assignedUserNames.push(assignedUsers[i].userName);












=======
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
                    project : function() {
                      return $scope.project;
                    }
                  }
>>>>>>> .theirs
<<<<<<< .mine
                }
=======
                });
>>>>>>> .theirs

<<<<<<< .mine
                $scope.submitChosenUser = function(newUserName) {
                  console.debug("Submitting chosen user", newUserName);
=======
              };

>>>>>>> .theirs

<<<<<<< .mine
                  if (newUserName == null || newUserName == undefined) {
                    window.alert("The user must be selected. ");
                    return;
                  }
=======
              // Choose user controller
              var ChooseUserModalCtrl = function($scope, $modalInstance, refset, action,
                currentUserName, assignedUsers, project, $rootScope) {

>>>>>>> .theirs

<<<<<<< .mine
                  $scope.selectedUserName = newUserName;
=======
                console.debug("Entered choose user modal control", assignedUsers, project.id);
>>>>>>> .theirs

<<<<<<< .mine
                  if (action == 'ASSIGN') {
                    workflowService.performWorkflowAction(
                      $scope.selectedProject.id, refset.id, newUserName,
                      "ASSIGN").then(
                      function(data) {
                        refsetService
                          .fireRefsetChanged($scope.selectedProject.id);
=======
                $scope.refset = refset;
                $scope.project = project;
                $scope.assignedUserNames = [];




>>>>>>> .theirs

<<<<<<< .mine
                        $modalInstance.close();
                      }, function(data) {
                        $modalInstance.close();
                      })
                  }
                };

                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };
=======
                for (var i = 0; i < assignedUsers.length; i++) {
                  $scope.assignedUserNames.push(assignedUsers[i].userName);
                }







>>>>>>> .theirs

<<<<<<< .mine
              };

=======
                $scope.submitChosenUser = function(newUserName) {
                  console.debug("Submitting chosen user", newUserName);
>>>>>>> .theirs

<<<<<<< .mine
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
=======
                  if (newUserName == null || newUserName == undefined) {
                    window.alert("The user must be selected. ");
                    return;
                  }
































>>>>>>> .theirs

<<<<<<< .mine
              var NewRefsetModalCtrl = function($scope, $modalInstance, refset,
                refsets, refsetTypes, project, terminologyEditions,
                terminologyVersions) {
=======
                  $scope.selectedUserName = newUserName;


>>>>>>> .theirs

<<<<<<< .mine
                console.debug("Entered new refset modal control", refsetTypes);



=======
                  if (action == 'ASSIGN') {
                    workflowService.performWorkflowAction($scope.project.id, refset.id,
                      newUserName, "ASSIGN").then(function(data) {
                      refsetService.fireRefsetChanged(refset);
>>>>>>> .theirs

<<<<<<< .mine
                $scope.refset = refset;
                $scope.refsetTypes = refsetTypes;
                $scope.terminologyEditions = terminologyEditions;
                $scope.project = project;


=======
                      $modalInstance.close();
                    }, function(data) {
                      $modalInstance.close();
                    })
                  }
                };
>>>>>>> .theirs

<<<<<<< .mine
                $scope.terminologySelected = function(terminology) {
                  $scope.terminologyVersions = terminologyVersions[terminology]
                    .sort();
                };
=======
                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };

>>>>>>> .theirs

<<<<<<< .mine
                $scope.submitNewRefset = function(refset) {
                  console.debug("Submitting new refset", refset);
=======
              };

>>>>>>> .theirs

<<<<<<< .mine
                  if (refset == null || refset.name == null
                    || refset.name == undefined || refset.description == null
                    || refset.description == undefined) {
                    window
                      .alert("The name and description fields cannot be blank. ");
                    return;
                  }
=======
              // Add Refset modal
              $scope.openNewRefsetModal = function(lrefset) {
                console.debug("openNewRefsetModal ", lrefset);




>>>>>>> .theirs

<<<<<<< .mine
                  refset.projectId = project.id;
                  refset.workflowPath = 'DEFAULT';
                  // TODO replace with conversion from 20150131 format
                  refset.version = '2015-01-31';
                  refsetService.addRefset(refset).then(
                    function(data) {
                      var newRefset = data;
                      refsets.push(newRefset);
























=======
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
                      return $scope.project;
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
                // Success - reload
                function() {
                  $scope.getRefsets();
                });
              };
>>>>>>> .theirs

<<<<<<< .mine
                      if (newRefset.type == 'INTENSIONAL') {
                        refsetService.beginRedefinition(newRefset.id,
                          newRefset.definition).then(
                          function(data) {
=======
              // Add Refset controller
              var NewRefsetModalCtrl = function($scope, $modalInstance, refset, refsets,
                refsetTypes, project, terminologyEditions, terminologyVersions) {

>>>>>>> .theirs

<<<<<<< .mine
                            refsetService.finishRedefinition(newRefset.id)
                              .then(function(data) {
=======
                console.debug("Entered new refset modal control", refsetTypes, terminologyVersions);

>>>>>>> .theirs

<<<<<<< .mine
                                $modalInstance.close();
                              }, function(data) {
                              })

=======
                $scope.refset = refset;
                $scope.refsetTypes = refsetTypes;
                $scope.terminologyEditions = terminologyEditions;
                $scope.project = project;
>>>>>>> .theirs

<<<<<<< .mine
                          }, function(data) {
                          })
                      } else {

=======
                $scope.terminologySelected = function(terminology) {
                  $scope.terminologyVersions = terminologyVersions[terminology].sort().reverse();
                };

>>>>>>> .theirs
                $scope.submitNewRefset = function(refset) {
                  console.debug("Submitting new refset", refset);

                  if (refset == null || refset.name == null || refset.name == undefined
                    || refset.description == null || refset.description == undefined) {
                    window.alert("The name and description fields cannot be blank. ");
                    return;
                  }

                  refset.projectId = project.id;
                  // TODO:!! this is hardcoded and almost
                  // certainly should not be - have a project
                  // workflow path setting
                  refset.workflowPath = 'DEFAULT';
                  // TODO:!! this is hardcoded and almost
                  // certainly should not be, this should be
                  // chosen in the dialog from a list
                  refset.version = '2015-01-31';
                  refsetService.addRefset(refset).then(
                    function(data) {
                      var newRefset = data;
                      refsets.push(newRefset);

                      if (newRefset.type == 'INTENSIONAL') {
                        refsetService.beginRedefinition(newRefset.id, newRefset.definition).then(
                          function(data) {

                            refsetService.finishRedefinition(newRefset.id).then(function(data) {

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

<<<<<<< .mine
              // modal for editing a refset
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

=======
              // Edit refset modal
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
                      return $scope.project;
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
>>>>>>> .theirs
              };

<<<<<<< .mine
              var EditRefsetModalCtrl = function($scope, $modalInstance,
                refset, refsetTypes, project, terminologyEditions,
                terminologyVersions, $rootScope) {
=======
              // Edit refset controller
              var EditRefsetModalCtrl = function($scope, $modalInstance, refset, refsetTypes,
                project, terminologyEditions, terminologyVersions, $rootScope) {
>>>>>>> .theirs

                console.debug("Entered edit refset modal control");

                $scope.refset = refset;
                $scope.originalDefinition = $scope.refset.definition;
                $scope.refsetTypes = refsetTypes;
                $scope.terminologyEditions = terminologyEditions;
                $scope.terminologyVersions = terminologyVersions;

<<<<<<< .mine
                $scope.terminologySelected = function(terminology) {
                  $scope.terminologyVersions = terminologyVersions[terminology]
                    .sort();
                };
=======
                $scope.terminologySelected = function(terminology) {
                  $scope.terminologyVersions = terminologyVersions[terminology].sort();
                };

>>>>>>> .theirs

                $scope.submitEditRefset = function(refset) {
                  console.debug("Submitting edit refset", refset);

<<<<<<< .mine
                  if (refset == null || refset.name == null
                    || refset.name == undefined || refset.description == null
                    || refset.description == undefined) {
                    window
                      .alert("The name, description, and terminology fields cannot be blank. ");
                    return;
=======
                  if (refset == null || refset.name == null || refset.name == undefined
                    || refset.description == null || refset.description == undefined) {
                    window.alert("The name, description, and terminology fields cannot be blank. ");
                    return;


>>>>>>> .theirs
                  }
                  refset.releaseInfo = undefined;

<<<<<<< .mine
                  refsetService
                    .updateRefset(refset)
                    .then(
                      function(data) {
                        if (refset.definition != $scope.originalDefinition) {
                          console.log("need to run redefinition");
                          refsetService
                            .beginRedefinition(refset.id, refset.definition)
                            .then(
                              function(data) {











=======
                  refsetService.updateRefset(refset).then(
                    function(data) {
                      if (refset.definition != $scope.originalDefinition) {
                        console.log("need to run redefinition");
                        // TODO: we can't actually
                        // do
                        // this in the long run
                        // we need to have a
                        // "staged"
                        // operation where the user
                        // can
                        // review
                        // the definition change and
                        // then cancel back out of
                        // it.
                        // a special dialog will be
                        // required, etc. It's fine
                        // for
                        // "new" but not for "Edit"
                        refsetService.beginRedefinition(refset.id, refset.definition).then(
                          function(data) {
>>>>>>> .theirs

<<<<<<< .mine
                                refsetService
                                  .finishRedefinition(refset.id)
                                  .then(
                                    function(data) {
                                      refsetService
                                        .fireRefsetChanged($scope.selectedProject.id);
=======
                            refsetService.finishRedefinition(refset.id).then(function(data) {
                              refsetService.fireRefsetChanged(refset);




>>>>>>> .theirs

<<<<<<< .mine
                                      $modalInstance.close();
                                    }, function(data) {
                                    })
=======
                              $modalInstance.close();
                            }, function(data) {
                            })
>>>>>>> .theirs

<<<<<<< .mine
                              }, function(data) {
                              })
                        }
                        $modalInstance.close();
                      }, function(data) {
                        $modalInstance.close();
                      })
=======
                          }, function(data) {
                          })
                      }
                      $modalInstance.close();
                    }, function(data) {
                      $modalInstance.close();
                    })
>>>>>>> .theirs

                };

                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };

              };

<<<<<<< .mine
              // modal for creating a new refset member
=======
              // New member modal
>>>>>>> .theirs
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
<<<<<<< .mine

=======

>>>>>>> .theirs
                    project : function() {
                      return $scope.project;
                    },
                    paging : function() {
                      return $scope.paging;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function() {
                  // $scope.findAssignedEditingRefsets();
                });
              };

              // New member controller
              var NewMemberModalCtrl = function($scope, $modalInstance, member, refset, project,
                paging) {

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
<<<<<<< .mine
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
                    refsetService.addRefsetInclusion(member.refsetId,
                      member.conceptId).then(function(data) {
=======
                    refsetService.addRefsetMember(member).then(function(data) {













>>>>>>> .theirs
                      if (refset.members == undefined) {
                        refset.members = [];
                      }
                      refset.members.push(data);
                      $modalInstance.close();
                    }, function(data) {
                      $modalInstance.close();
                    })
                  }

<<<<<<< .mine













=======
                  if (member.memberType == 'INCLUSION') {
                    refsetService.addRefsetInclusion(member.refsetId, member.conceptId).then(
                      function(data) {
                        if (refset.members == undefined) {
                          refset.members = [];
                        }
                        refset.members.push(data);
                        $modalInstance.close();
                      }, function(data) {
                        $modalInstance.close();
                      })
                  }

>>>>>>> .theirs
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
<<<<<<< .mine

                  // if search term is an id, simply look up the id

=======

                  // if search term is an id, simply look up
                  // the id
>>>>>>> .theirs
                  if (/^\d+$/.test(search)) {
                    projectService.getConceptWithDescriptions(search, refset.terminology,
                      refset.version, pfs).then(function(data) {
                      $scope.searchResults[0] = data;
                      $scope.selectConcept($scope.searchResults[0]);
                    }, function(data) {
                    })

<<<<<<< .mine
                    projectService.getConceptWithDescriptions(search,
                      refset.terminology, refset.version, pfs).then(
                      function(data) {
                        $scope.searchResults[0] = data;
                        $scope.selectConcept($scope.searchResults[0]);
                      }, function(data) {
                      })

=======








>>>>>>> .theirs
                  } else {
                    // TODO: manage paging of results
                    var pfs = {
                      startIndex : 0,
                      maxResults : 10,
                      sortField : null,
                      queryRestriction : null
                    };

                    projectService.findConceptsForQuery(search, refset.terminology, refset.version,
                      pfs).then(function(data) {
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

<<<<<<< .mine
                  projectService.getConceptParents(concept,
                    concept.terminology, concept.version).then(function(data) {
                    $scope.parents = data.concepts;
                  }, function(data) {
                  })
=======
                  projectService.getConceptParents(concept, concept.terminology, concept.version)
                    .then(function(data) {
                      $scope.parents = data.concepts;
                    }, function(data) {
                    })
>>>>>>> .theirs
                };

                // get concept children
                $scope.getConceptChildren = function(concept) {
                  console.debug("Getting concept children", concept);

                  var pfs = {
                    startIndex : ($scope.paging["children"].page - 1) * $scope.pageSize,
                    maxResults : $scope.pageSize,
                    sortField : null,
                    queryRestriction : $scope.paging["children"].filter != undefined ? $scope.paging["children"].filter
                      : null
                  };
<<<<<<< .mine

                  projectService.getConceptChildren(concept.terminologyId,
                    concept.terminology, concept.version, pfs).then(
                    function(data) {
                      $scope.children = data.concepts;
                      $scope.children.totalCount = data.totalCount;
                    }, function(data) {
                    })
=======

                  projectService.getConceptChildren(concept.terminologyId, concept.terminology,
                    concept.version, pfs).then(function(data) {
                    $scope.children = data.concepts;
                    $scope.children.totalCount = data.totalCount;
                  }, function(data) {
                  })

>>>>>>> .theirs
                };

                // get concept with descriptions
                $scope.getConceptWithDescriptions = function(concept) {
                  console.debug("Getting concept with descriptions", concept);

<<<<<<< .mine
                  projectService.getConceptWithDescriptions(concept,
                    concept.terminology, concept.version).then(function(data) {
                    $scope.concept = data;
                  }, function(data) {
                  })


=======
                  // projectService.getConceptWithDescriptions(concept,
                  // concept.terminology,
                  // concept.version).then(function(data)
                  // {
                  // $scope.concept = data;
                  // }, function(data) {
                  // })
>>>>>>> .theirs
                };

                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };

              };

<<<<<<< .mine
              // modal for resolving redefinition issues
              $scope.openRedefinitionModal = function(lrefset, ldefinition) {

                console.debug("openRedefinitionModal ", lrefset, ldefinition);

                var modalInstance = $modal.open({
                  templateUrl : 'app/page/refset/redefinition.html',
                  controller : RedefinitionModalCtrl,
                  resolve : {

                    refset : function() {
                      return lrefset;
                    },
                    definition : function() {
                      return ldefinition;
                    },
                    paging : function() {
                      return $scope.paging;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function() {
                });
              };

              var RedefinitionModalCtrl = function($scope, $modalInstance,
                refset, definition, paging) {

                console.debug("Entered redefinition modal control");

                $scope.beginRedefinition(refset.id, definition).then(
                  function(data) {
                    $scope.stagedRefset = data;
                  })

                $scope.cancel = function() {
                  $modalInstance.dismiss('cancel');
                };

              };

            } ]
        }
      } ]);
=======
            } ]
        }
      } ]);












































>>>>>>> .theirs
