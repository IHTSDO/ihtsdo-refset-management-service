// Administration controller
tsApp
  .controller(
    'AdminCtrl',
    [
      '$scope',
      '$http',
      '$modal',
      '$location',
      '$anchorScroll',
      '$timeout',
      'gpService',
      'utilService',
      'tabService',
      'securityService',
      'projectService',
      function($scope, $http, $modal, $location, $anchorScroll, $timeout,
        gpService, utilService, tabService, securityService, projectService) {
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
        $scope.candidateProjects = null;
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

        // get projects
        $scope.retrieveProjects = function() {

          var pfs = {
            startIndex : ($scope.paging["project"].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging["project"].sortField,
            ascending : $scope.paging["project"].ascending == null ? true
              : $scope.paging["project"].ascending,
            queryRestriction : null
          };

          projectService.findProjectsAsList($scope.paging["project"].filter,
            pfs).then(function(data) {
            $scope.projects = data.projects;
            $scope.projects.totalCount = data.totalCount;
          })

        };

        // get candidate projects
        // one of these projects can be selected for user and role
        // assignment
        $scope.retrieveCandidateProjects = function() {

          var pfs = {
            startIndex : ($scope.paging["candidateProject"].page - 1)
              * $scope.pageSize,
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

          projectService.findProjectsAsList(
            $scope.paging["candidateProject"].filter, pfs).then(function(data) {
            $scope.candidateProjects = data.projects;
            $scope.candidateProjects.totalCount = data.totalCount;
          })

        };

        // get users
        $scope.retrieveUsers = function() {

          var pfs = {
            startIndex : ($scope.paging["user"].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging["user"].sortField,
            ascending : $scope.paging["user"].ascending,
            ascending : $scope.paging["user"].ascending == null ? true
              : $scope.paging["user"].ascending,
            queryRestriction : null
          };

          securityService.findUsersAsList($scope.paging["user"].filter, pfs)
            .then(function(data) {
              $scope.users = data.users;
              $scope.users.totalCount = data.totalCount;
            })

        };

        // get unassigned users - this is the list of users that are not
        // yet
        // assigned to the selected project
        $scope.retrieveUnassignedUsers = function() {
          var pfs = {
            startIndex : ($scope.paging["candidateUser"].page - 1)
              * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging["candidateUser"].sortField,
            ascending : $scope.paging["candidateUser"].ascending == null ? true
              : $scope.paging["candidateUser"].ascending,
            queryRestriction : null
          };

          projectService.findUnassignedUsersForProject(
            $scope.selectedProject.id, $scope.paging["candidateUser"].filter,
            pfs).then(function(data) {
            $scope.unassignedUsers = data.users;
            $scope.unassignedUsers.totalCount = data.totalCount;
          })
        };

        // get assigned users - this is the list of users that are
        // already
        // assigned to the selected project
        $scope.retrieveAssignedUsers = function() {

          var pfs = {
            startIndex : ($scope.paging["assignedUser"].page - 1)
              * $scope.pageSize,
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

        // get application roles
        $scope.getApplicationRoles = function() {
          securityService.getApplicationRoles().then(function(data) {
            $scope.applicationRoles = data.strings;
          })
        };

        // get project roles
        $scope.getProjectRoles = function() {
          console.debug("getProjectRoles");
          projectService.getProjectRoles().then(function(data) {
            console.debug("adminController.roles =",data.strings);
            $scope.projectRoles = data.strings;
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

        // Sets the selected project
        $scope.setProject = function(project) {
          if (typeof project === undefined) {
            return;
          }
          if ($scope.selectedProject
            && project.id === $scope.selectedProject.id) {
            return;
          }

          $scope.selectedProject = project;
          $scope.retrieveUnassignedUsers();
          $scope.retrieveAssignedUsers();

          resetPaging();
        }

        // remove a project, a user - only application admins can do
        // this
        $scope.remove = function(type, object, objArray) {
          if (!confirm("Are you sure you want to remove the " + type + " ("
            + object.name + ")?")) {
            return;
          }
          if (type == 'project') {
            if (object.userRoleMap != null && object.userRoleMap != undefined
              && Object.keys(object.userRoleMap).length > 0) {
              window
                .alert("You can not delete a project that has users assigned to it. Remove the assigned users before deleting the project.");
              return;
            }
            projectService.removeProject(object).then(function() {
              $scope.retrieveProjects();
              $scope.retrieveCandidateProjects();
            });
          }
          if (type == 'user') {
            if (object.projectRoleMap != null
              && object.projectRoleMap != undefined
              && Object.keys(object.projectRoleMap).length > 0) {
              window
                .alert("You can not delete a user that is assigned to a project. Remove this user from all projects before deleting it.");
              return;
            }
            securityService.removeUser(object).then(function() {
              $scope.retrieveUsers();
              if ($scope.selectedProject != null) {
                $scope.retrieveUnassignedUsers();
                $scope.retrieveAssignedUsers();
              }
            });
          }
        };

        // sort mechanism 
        $scope.setSortField = function(table, field) {
          console.debug("set " + table + " sortField " + field);
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
          for (var key in $scope.paging) {
            if ($scope.paging.hasOwnProperty(key)) {
              //console.debug(key + " -> " + $scope.paging[key].page);
              if (key == table)
                $scope.paging[key].page = 1;
            }
          }
          // retrieve the correct table
          if (table === 'candidateProject') {
            $scope.retrieveCandidateProjects();
          } else if (table === 'project') {
            $scope.retrieveProjects();
          } else if (table === 'user') {
            $scope.retrieveUsers();
          } else if (table === 'assignedUser') {
            $scope.retrieveAssignedUsers();
          } else if (table === 'candidateUser') {
            $scope.retrieveUnassignedUsers();
          }
          
        }

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
        }

        // assign user to project
        $scope.assignUserToProject = function(projectId, userName, projectRole) {
          if (projectId == null || projectId == undefined) {
            window.alert("Select a project before assigning a user! ");
            return;
          }

          projectService.assignUserToProject(projectId, userName, projectRole)
            .then(function(data) {
              $scope.retrieveProjects();
              $scope.selectedProject = data;
              $scope.retrieveAssignedUsers();
              $scope.retrieveUnassignedUsers();
            })
        };

        // remove user from project
        $scope.unassignUserFromProject = function(projectId, userName) {
          projectService.unassignUserFromProject(projectId, userName).then(
            function(data) {
              $scope.retrieveProjects();
              $scope.selectedProject = data;
              $scope.retrieveAssignedUsers();
              $scope.retrieveUnassignedUsers();
            })
        };

        // reset paging for all tables to page 1
        var resetPaging = function() {
          for (var key in $scope.paging) {
            if ($scope.paging.hasOwnProperty(key)) {
              //console.debug(key + " -> " + $scope.paging[key].page);
              $scope.paging[key].page = 1;
            }
          }
          $scope.retrieveAssignedUsers();
          $scope.retrieveUnassignedUsers();
        }
        
        
        //
        // call these during initialization
        //

        $scope.retrieveProjects();
        $scope.retrieveUsers();
        $scope.retrieveCandidateProjects();
        $scope.getApplicationRoles();
        $scope.getProjectRoles();
        $scope.getTerminologyEditions();

        //
        // Modals
        //

        // modal for creating a new project - only application admins
        // can do this
        $scope.openNewProjectModal = function(lproject) {

          console.debug("openNewProjectModal ");

          var modalInstance = $modal.open({
            templateUrl : 'app/page/admin/newProject.html',
            controller : NewProjectModalCtrl,
            resolve : {
              project : function() {
                return lproject;
              },
              projects : function() {
                return $scope.projects;
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
            $scope.retrieveCandidateProjects();
          });
        };

        var NewProjectModalCtrl = function($scope, $modalInstance, project,
          projects, terminologyEditions, terminologyVersions) {

          console.debug("Entered new project modal control");

          $scope.project = project;
          $scope.terminologyEditions = terminologyEditions;
          
          $scope.terminologySelected = function(terminology) {
            $scope.terminologyVersions = terminologyVersions[terminology].sort();
          };

          $scope.submitNewProject = function(project) {
            console.debug("Submitting new project", project);

            
            if (project == null || project.name == null
              || project.name == undefined || project.description == null
              || project.description == undefined) {
              window.alert("The name and description fields cannot be blank. ");
              return;
            }

            projectService.addProject(project).then(function(data) {
              projects.push(data);
              $modalInstance.close();
            }, function(data) {
              $modalInstance.close();
            })

          };

          $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
          };

        };

        // modal for creating a new user- only application admins can do
        // this
        $scope.openNewUserModal = function(luser) {

          console.debug("openNewUserModal ");

          var modalInstance = $modal.open({
            templateUrl : 'app/page/admin/newUser.html',
            controller : NewUserModalCtrl,
            resolve : {
              user : function() {
                return luser;
              },
              users : function() {
                return $scope.users;
              },
              applicationRoles : function() {
                return $scope.applicationRoles;
              }
            }
          });

          modalInstance.result.then(
          // Success
          function() {
            $scope.retrieveUnassignedUsers();
            $scope.retrieveAssignedUsers();
          });
        };

        var NewUserModalCtrl = function($scope, $modalInstance, user, users,
          applicationRoles) {

          console.debug("Entered new user modal control");

          $scope.user = user;
          $scope.applicationRoles = applicationRoles

          $scope.submitNewUser = function(user) {
            console.debug("Submitting new user", user);

            if (user == null || user.name == null || user.name == undefined
              || user.userName == null || user.userName == undefined
              || user.applicationRole == null
              || user.applicationRole == undefined) {
              window
                .alert("The name, user name, and application role fields cannot be blank. ");
              return;
            }

            securityService.addUser(user).then(function(data) {
              users.push(data);
              $modalInstance.close();
            }, function(data) {
              $modalInstance.close();
            })

          };

          $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
          };

        };

        // modal for editing a project - only application admins can do
        // this
        $scope.openEditProjectModal = function(lproject) {

          console.debug("openEditProjectModal ");

          var modalInstance = $modal.open({
            templateUrl : 'app/page/admin/editProject.html',
            controller : EditProjectModalCtrl,
            resolve : {
              project : function() {
                return lproject;
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
            $scope.retrieveCandidateProjects();
          });
        };

        var EditProjectModalCtrl = function($scope, $modalInstance, project, 
          terminologyEditions, terminologyVersions) {

          console.debug("Entered edit project modal control");
          console.debug("versions:", terminologyVersions);

          $scope.project = project;
          $scope.terminologyEditions = terminologyEditions;
          $scope.terminologyVersions = terminologyVersions[project.terminology].sort();

          $scope.submitEditProject = function(project) {
            console.debug("Submitting edit project", project);

            if (project == null || project.name == null
              || project.name == undefined || project.description == null
              || project.description == undefined
              || project.terminology == null
              || project.terminology == undefined) {
              window
                .alert("The name, description, and terminology fields cannot be blank. ");
              return;
            }

            projectService.updateProject(project).then(function(data) {
              $modalInstance.close();
            }, function(data) {
              $modalInstance.close();
            })

          };

          $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
          };

        };

        // modal for editing a user - only application admins can do
        // this
        $scope.openEditUserModal = function(luser) {

          console.debug("openEditUserModal");

          var modalInstance = $modal.open({
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
            $scope.retrieveUnassignedUsers();
            $scope.retrieveAssignedUsers();
          });
        };

        var EditUserModalCtrl = function($scope, $modalInstance, user,
          applicationRoles) {

          console.debug("Entered edit user modal control");

          $scope.user = user;
          $scope.applicationRoles = applicationRoles;

          $scope.submitEditUser = function(user) {
            console.debug("Submitting edit user ", user);

            if (user == null || user.name == null || user.name == undefined
              || user.userName == null || user.userName == undefined
              || user.applicationRole == null
              || user.applicationRole == undefined) {
              window
                .alert("The name, user name, and application role fields cannot be blank. ");
              return;
            }

            securityService.updateUser(user).then(function(data) {
              $modalInstance.close();
            }, function(data) {
              $modalInstance.close();
            })

          };

          $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
          };

        };
      }

    ]);
