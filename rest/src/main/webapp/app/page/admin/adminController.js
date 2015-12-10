// Administration controller
tsApp
  .controller(
    'AdminCtrl',
    [
      '$scope',
      '$http',
      '$uibModal',
      '$location',
      '$anchorScroll',
      '$timeout',
      'gpService',
      'utilService',
      'tabService',
      'securityService',
      'projectService',
      'validationService',
      function($scope, $http, $uibModal, $location, $anchorScroll, $timeout, gpService,
        utilService, tabService, securityService, projectService, validationService) {
        console.debug('configure AdminCtrl');

        // Handle resetting tabs on "back" button
        if (tabService.selectedTab.label != 'Admin') {
          tabService.setSelectedTabByLabel('Admin');
        }

        //
        // Scope Variables
        //
        $scope.user = securityService.getUser();
        $scope.selectedProject = null;
        $scope.projectRoles = [];

        // Model variables
        $scope.projects = null;
        $scope.candiateProjects = null;
        $scope.users = null;
        $scope.assignedUsers = null;
        $scope.unassignedUsers = null;

        // Paging variables
        $scope.pageSize = 10;
        $scope.paging = {};
        $scope.paging["project"] = {
          page : 1,
          filter : "",
          sortField : 'lastModified',
          ascending : null
        }
        $scope.paging["candidateProject"] = {
          page : 1,
          filter : "",
          sortField : 'lastModified',
          ascending : null
        }
        $scope.paging["user"] = {
          page : 1,
          filter : "",
          sortField : 'userName',
          ascending : null
        }
        $scope.paging["assignedUser"] = {
          page : 1,
          filter : "",
          sortField : 'userName',
          ascending : null
        }
        $scope.paging["candidateUser"] = {
          page : 1,
          filter : "",
          sortField : 'userName',
          ascending : null
        }

        // Get $scope.projects
        $scope.getProjects = function() {

          var pfs = {
            startIndex : ($scope.paging["project"].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging["project"].sortField,
            ascending : $scope.paging["project"].ascending == null ? true
              : $scope.paging["project"].ascending,
            queryRestriction : null
          };

          projectService.findProjectsAsList($scope.paging["project"].filter, pfs).then(
            function(data) {
              $scope.projects = data.projects;
              $scope.projects.totalCount = data.totalCount;
            })

        };

        // Get $scope.candidateProjects
        // one of these projects can be selected for user and role
        // assignment
        $scope.getCandidateProjects = function() {

          var pfs = {
            startIndex : ($scope.paging["candidateProject"].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging["candidateProject"].sortField,
            ascending : $scope.paging["candidateProject"].ascending == null ? true
              : $scope.paging["candidateProject"].ascending,
            queryRestriction : 'userRoleMap:' + $scope.user.userName + 'ADMIN'
          };
          // clear queryRestriction for application admins
          if ($scope.user.applicationRole == 'ADMIN') {
            pfs.queryRestriction = null;
          }

          projectService.findProjectsAsList($scope.paging["candidateProject"].filter, pfs).then(
            function(data) {
              $scope.candidateProjects = data.projects;
              $scope.candidateProjects.totalCount = data.totalCount;
            })

        };

        // Get $scope.users
        $scope.getUsers = function() {

          var pfs = {
            startIndex : ($scope.paging["user"].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging["user"].sortField,
            ascending : $scope.paging["user"].ascending,
            ascending : $scope.paging["user"].ascending == null ? true
              : $scope.paging["user"].ascending,
            queryRestriction : null
          };

          securityService.findUsersAsList($scope.paging["user"].filter, pfs).then(function(data) {
            $scope.users = data.users;
            $scope.users.totalCount = data.totalCount;
          })

        };

        // Get $scope.unassignedUsers
        // this is the list of users that are not  yet
        // assigned to the selected project
        $scope.getUnassignedUsers = function() {
          var pfs = {
            startIndex : ($scope.paging["candidateUser"].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging["candidateUser"].sortField,
            ascending : $scope.paging["candidateUser"].ascending == null ? true
              : $scope.paging["candidateUser"].ascending,
            queryRestriction : null
          };

          projectService.findUnassignedUsersForProject($scope.selectedProject.id,
            $scope.paging["candidateUser"].filter, pfs).then(function(data) {
            $scope.unassignedUsers = data.users;
            $scope.unassignedUsers.totalCount = data.totalCount;
          })
        };

        // Get $scope.assignedUsers
        // this is the list of users that are already
        // assigned to the selected project
        $scope.getAssignedUsers = function() {

          var pfs = {
            startIndex : ($scope.paging["assignedUser"].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging["assignedUser"].sortField,
            ascending : $scope.paging["assignedUser"].ascending == null ? true
              : $scope.paging["assignedUser"].ascending,
            queryRestriction : null
          };
          projectService.findAssignedUsersForProject($scope.selectedProject.id,
            $scope.paging["assignedUser"].filter, pfs).then(function(data) {
            $scope.assignedUsers = data.users;
            $scope.assignedUsers.totalCount = data.totalCount;
          })

        };

        // Get $scope.applicationRoles
        $scope.getApplicationRoles = function() {
          securityService.getApplicationRoles().then(function(data) {
            $scope.applicationRoles = data.strings;
          })
        };

        // Get $scope.projectRoles
        $scope.getProjectRoles = function() {
          projectService.getProjectRoles().then(function(data) {
            $scope.projectRoles = data.strings;
          })
        };

        // Get $scope.terminologies
        $scope.getTerminologies = function() {
          projectService.getTerminologyEditions().then(function(data) {
            $scope.terminologies = data.strings;
          })
        };

        // Sets the selected project
        $scope.setProject = function(project) {
          if (!project) {
            return;
          }
          // Don't re-select
          if ($scope.selectedProject && project.id == $scope.selectedProject.id) {
            return;
          }

          $scope.selectedProject = project;
          $scope.getUnassignedUsers();
          $scope.getAssignedUsers();

          resetPaging();
        }

        // Removes a project
        $scope.removeProject = function(project) {
          if (!confirm("Are you sure you want to remove the project (" + project.name + ")?")) {
            return;
          }
          if (object.userRoleMap != null && object.userRoleMap != undefined
            && Object.keys(object.userRoleMap).length > 0) {
            window
              .alert("You can not delete a project that has users assigned to it. Remove the assigned users before deleting the project.");
            return;
          }
          projectService.removeProject(object).then(function() {
            // Refresh projects
            $scope.getProjects();
            $scope.getCandidateProjects();
          });
        };

        // Removes a user
        $scope.remove = function(user) {
          if (!confirm("Are you sure you want to remove the user (" + user.userName + ")?")) {
            return;
          }
          if (user.projectRoleMap && Object.keys(user.projectRoleMap).length > 0) {
            window.alert("You can not delete a user that is assigned to a project."
              + "Remove this user from all projects before deleting it.");
            return;
          }
          securityService.removeUser(object).then(function() {
            // Refresh users
            $scope.getUsers();
            if ($scope.selectedProject != null) {
              $scope.getUnassignedUsers();
              $scope.getAssignedUsers();
            }
          });

        };

        // sort mechanism 
        $scope.setSortField = function(table, field) {
          utilService.setSortField(table, field, $scope.paging);

          // retrieve the correct table
          if (table === 'candidateProject') {
            $scope.getCandidateProjects();
          } else if (table === 'project') {
            $scope.getProjects();
          } else if (table === 'user') {
            $scope.getUsers();
          } else if (table === 'assignedUser') {
            $scope.getAssignedUsers();
          } else if (table === 'candidateUser') {
            $scope.getUnassignedUsers();
          }

        }

        // Return up or down sort chars if sorted
        $scope.getSortIndicator = function(table, field) {
          return utilService.getSortIndicator(table, field, $scope.paging);
        }

        // assign user to project
        $scope.assignUserToProject = function(projectId, userName, projectRole) {
          if (projectId == null || projectId == undefined) {
            window.alert("Select a project before assigning a user! ");
            return;
          }

          projectService.assignUserToProject(projectId, userName, projectRole).then(function(data) {
            $scope.getProjects();
            $scope.selectedProject = data;
            $scope.getAssignedUsers();
            $scope.getUnassignedUsers();
          })
        };

        // remove user from project
        $scope.unassignUserFromProject = function(projectId, userName) {
          projectService.unassignUserFromProject(projectId, userName).then(function(data) {
            $scope.getProjects();
            $scope.selectedProject = data;
            $scope.getAssignedUsers();
            $scope.getUnassignedUsers();
          })
        };

        // reset paging for all tables to page 1
        var resetPaging = function() {
          for ( var key in $scope.paging) {
            if ($scope.paging.hasOwnProperty(key)) {
              $scope.paging[key].page = 1;
            }
          }
          $scope.getAssignedUsers();
          $scope.getUnassignedUsers();
        }

        $scope.getValidationChecks = function() {
          validationService.getValidationCheckNames().then(
          // Success 
          function(data) {
            $scope.validationChecks = data.keyValuePairs;
          },
          // Error - update refset
          function(data) {
            $scope.errors[0] = data;
            utilService.clearError();
          })
        }

        //
        // Initialize
        //
        $scope.getProjects();
        $scope.getUsers();
        $scope.getCandidateProjects();
        $scope.getApplicationRoles();
        $scope.getProjectRoles();
        $scope.getTerminologies();
        $scope.getValidationChecks();

        //
        // MODALS
        //

        // Add project modal
        $scope.openAddProjectModal = function() {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/admin/editProject.html',
            controller : AddProjectModalCtrl,
            resolve : {
              terminologies : function() {
                return $scope.terminologies;
              }
            }
          });

          modalInstance.result.then(
          // Success
          function() {
            projectService.fireProjectChanged(project);
            $scope.getCandidateProjects();
          });
        };

        // Add project controller
        var AddProjectModalCtrl = function($scope, $uibModalInstance, terminologies) {

          $scope.action = 'Add';
          $scope.terminologies = terminologies;
          $scope.project = {
            terminology : terminologies[0]
          };
          $scope.error = null;

          $scope.submitProject = function(project) {

            if (!project || !project.name || !project.description) {
              window.alert("The name and description fields cannot be blank. ");
              return;
            }
            // Add project
            projectService.addProject(project).then(
            // Success
            function(data) {
              $uibModalInstance.close(data);
            },
            // Error
            function(data) {
              $scope.error = data;
              utilService.clearError();
            })
          };

          $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
          };

        };

        // modal for editing a project - only application admins can do
        // this
        $scope.openEditProjectModal = function(lproject) {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/admin/editProject.html',
            controller : EditProjectModalCtrl,
            resolve : {
              project : function() {
                return lproject;
              },
              terminologies : function() {
                return $scope.terminologies;
              },
              validationChecks : function() {
                return $scope.validationChecks;
              }
            }
          });

          modalInstance.result.then(
          // Success
          function() {
            $scope.getCandidateProjects();
          });
        };

        var EditProjectModalCtrl = function($scope, $uibModalInstance, project, terminologies,
          validationChecks) {

          $scope.action = 'Edit';
          $scope.project = project;
          $scope.terminologies = terminologies;
          $scope.validationChecks = validationChecks;
          $scope.availableChecks = [];
          $scope.selectedChecks = [];
          $scope.error = null;

          for (var i = 0; i < $scope.validationChecks.length; i++) {
            if (project.validationChecks.indexOf($scope.validationChecks[i].key) > -1) {
              $scope.selectedChecks.push($scope.validationChecks[i].value);
            } else {
              $scope.availableChecks.push($scope.validationChecks[i].value);
            }
          }

          $scope.selectValidationCheck = function(check) {
            $scope.selectedChecks.push(check);
            var index = $scope.availableChecks.indexOf(check);
            $scope.availableChecks.splice(index, 1);
          }

          $scope.removeValidationCheck = function(check) {
            $scope.availableChecks.push(check);
            var index = $scope.selectedChecks.indexOf(check);
            $scope.selectedChecks.splice(index, 1);
          }

          $scope.submitProject = function(project) {
            if (!project || !project.name || !project.description || !project.terminology) {
              window.alert("The name, description, and terminology fields cannot be blank. ");
              return;
            }

            project.validationChecks = [];
            for (var i = 0; i < $scope.validationChecks.length; i++) {
              if ($scope.selectedChecks.indexOf($scope.validationChecks[i].value) != -1) {
                project.validationChecks.push($scope.validationChecks[i].key);
              }
            }
            /*
                         * project.validationChecks = $scope.validationChecks; for (var i =
                         * $scope.validationChecks.length -1; i >= 0; i--) { var index =
                         * $scope.selectedChecks.indexOf($scope.validationChecks[i].value); if
                         * (index == -1) { project.validationChecks.splice(index, 1); } }
                         */

            projectService.updateProject(project).then(
            // Success
            function(data) {
              $uibModalInstance.close();
            },
            // Error
            function(data) {
              $scope.error = data;
              utilService.clearError();
            })
          };

          $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
          };

        };

        // Add user modal
        $scope.openAddUserModal = function(luser) {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/admin/editUser.html',
            controller : AddUserModalCtrl,
            resolve : {
              user : function() {
                return luser;
              },
              applicationRoles : function() {
                return $scope.applicationRoles;
              }
            }
          });

          modalInstance.result.then(
          // Success
          function() {
            $scope.getUnassignedUsers();
            $scope.getAssignedUsers();
          });
        };

        // Add user controller
        var AddUserModalCtrl = function($scope, $uibModalInstance, user, applicationRoles) {
          $scope.action = 'Add';
          $scope.user = user;
          $scope.applicationRoles = applicationRoles
          $scope.error = null;

          $scope.submitUser = function(user) {
            if (!user || !user.name || !user.userName || !user.applicationRole) {
              window.alert("The name, user name, and application role fields cannot be blank. ");
              return;
            }
            securityService.addUser(user).then(
            // Success
            function(data) {
              $uibModalInstance.close();
            },
            // Error
            function(data) {
              $scope.error = data;
              utilService.clearError();
            })

          };

          $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
          };

        };

        // modal for editing a user - only application admins can do
        // this
        $scope.openEditUserModal = function(luser) {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/admin/editUser.html',
            controller : EditUserModalCtrl,
            resolve : {
              user : function() {
                return luser;
              },
              applicationRoles : function() {
                return $scope.applicationRoles;
              }
            }
          });

          modalInstance.result.then(
          // Success
          function() {
            $scope.getUnassignedUsers();
            $scope.getAssignedUsers();
          });
        };

        var EditUserModalCtrl = function($scope, $uibModalInstance, user, applicationRoles) {

          $scope.action = 'Edit';
          $scope.user = user;
          $scope.applicationRoles = applicationRoles;
          $scope.error = null;

          $scope.submitUser = function(user) {

            if (!user || !user.name || !user.userName || !user.applicationRole) {
              window.alert("The name, user name, and application role fields cannot be blank. ");
              return;
            }

            securityService.updateUser(user).then(
            // Success
            function(data) {
              $uibModalInstance.close();
            },
            // Error
            function(data) {
              $scope.error = data;
              utilService.clearError();
            })
          };

          $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
          };

        };
      }

    ]);
