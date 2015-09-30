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
      function($scope, $http, $modal, $location, $anchorScroll, $timeout, gpService,
        utilService, tabService, securityService, projectService) {
        console.debug('configure AdminCtrl');

        // Handle resetting tabs on "back" button
        if (tabService.selectedTab.label != 'Admin') {
          tabService.setSelectedTabByLabel('Admin');
        }

        //
        // Scope Variables
        //

        // Scope variables initialized from services
        $scope.user = securityService.getUser();

        // remove a project, a user
        $scope.remove = function(type, object, objArray) {
          if (!confirm("Are you sure you want to remove the " + type + " ("
            + object.name + ")?")) {
            return;
          }
          if (type == 'project') {
            projectService.removeProject(object).then(function() {
              $scope.getProjects();
            });
          }
          if (type == 'user') {
            securityService.removeUser(object).then(function() {
              $scope.getUsers();
            });
          }
        };

        // get projects
        $scope.getProjects = function() {
          projectService.getProjects().then(function(data) {
            for (var i = 0; i < data.projects.length; i++) {
              data.projects[i].isExpanded = false;
            }
            $scope.projects = data.projects;
          })
        };

        // get users
        $scope.getUsers = function() {
          securityService.getUsers().then(function(data) {
            $scope.users = data.users;
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
          projectService.getProjectRoles().then(function(data) {
            $scope.projectRoles = data.strings;
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
        }

        // add user to project
        $scope.assignUserToProject = function(projectId, userName, projectRole) {
          projectService.assignUserToProject(projectId, userName, projectRole).then(
            function(data) {
              $scope.getProjects();
              $scope.selectedProject = data;
            })
        };

        // remove user from project
        $scope.unassignUserFromProject = function(projectId, userName) {
          projectService.unassignUserFromProject(projectId, userName).then(
            function(data) {

              $scope.getProjects();
              $scope.selectedProject = data;
            })
        };

        // call these during initialization
        $scope.getProjects();
        $scope.getUsers();
        $scope.getApplicationRoles();
        $scope.getProjectRoles();

        //
        // Modals
        //

        // modal for creating a new project
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
              }
            }
          });
        };

        var NewProjectModalCtrl = function($scope, $modalInstance, project,
          projects) {

          console.debug("Entered new project modal control");

          $scope.project = project;

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

        // modal for creating a new user
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

        // modal for editing a project
        $scope.openEditProjectModal = function(lproject) {

          console.debug("openEditProjectModal ");

          var modalInstance = $modal.open({
            templateUrl : 'app/page/admin/editProject.html',
            controller : EditProjectModalCtrl,
            resolve : {
              project : function() {
                return lproject;
              }
            }
          });
        };

        var EditProjectModalCtrl = function($scope, $modalInstance, project) {

          console.debug("Entered edit project modal control");

          $scope.project = project;

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
              // TODO get this working $scope.projects.push(data);
              $modalInstance.close();
            }, function(data) {
              $modalInstance.close();
            })

          };

          $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
          };

        };

        // modal for editing a user
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
              // TODO get this working $scope.users.push(data);
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
