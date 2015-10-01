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
        $scope.users = null;
        

        $scope.pageSize = 5;
        $scope.projectsPageSize = 1;
        
        $scope.pagedProjects = null;

        $scope.projectPaging = {
          page : 1,
          filter : "description"
        }
        
        // Search parameters
        $scope.searchParams = securityService.getSearchParams();
        //$scope.searchResults = securityService.getSearchResults();

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

        // get paged projects
        $scope.retrievePagedProjects = function() {

          var pfs = {
            startIndex : ($scope.projectPaging.page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : 'lastModified',
            queryRestriction : null
          };

          $scope.projectsPageSize = $scope.pageSize;
          
          projectService.findProjectsAsList($scope.projectPaging.filter, pfs).then(function(data) {
            $scope.pagedProjects = data.projects;
            $scope.pagedProjects.totalCount = data.totalCount;
          })
          
        };
        
        // get users
        $scope.getUsers = function() {
          securityService.getUsers().then(function(data) {
            $scope.users = data.users;
          })
        };
        
        // find users
        $scope.findUsers = function() {
          securityService
          .findUsersAsList($scope.searchParams.query, $scope.searchParams.page)
          .then(
            function(data) {
              $scope.userResults = data.users;
            });
        }

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
          var projectRole = getProjectRoleForUser(project, $scope.selectedUser);
          if (projectRole == null || !projectRole == 'ADMIN') {
            window.alert("User must have administrator privledges on this project in order to manage users on this project.");
          }
            
          
          $scope.selectedProject = project;
        }

        // assign user to project
        $scope.assignUserToProject = function(projectId, userName, projectRole) {
          if (projectId == null || projectId == undefined) {
            window.alert("Select a project before assigning a user! ");
            return;
          }
          
          if (isUserAssignedToSelectedProject(userName)) {
            window.alert("The " + userName + " user has already been assigned to the project!");
            return;
          }
          
          projectService.assignUserToProject(projectId, userName, projectRole).then(
            function(data) {
              $scope.getProjects();
              $scope.selectedProject = data;
              for (var i = $scope.userResults.length - 1; i >= 0; i--) {
                if (userName == $scope.userResults[i].userName) {
                  $scope.userResults.splice(i, 1);
                }
              }
            })
        };
        
        // find out if user is already assigned to selected project
        function isUserAssignedToSelectedProject(userName) {
          for (var i = 0; i < $scope.selectedProject.projectRoleMap.entry.length; i++) {
            if ($scope.selectedProject.projectRoleMap.entry[i].key.userName == userName)
              return true;
          }
          return false;
        };
        
        function getProjectRoleForUser(project, user) {
          for (var i = 0; i < project.projectRoleMap.entry.length; i++) {
            if (project.projectRoleMap.entry[i].key.userName == user.userName)
              return project.projectRoleMap.entry[i].value;
          }
          return null;
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

        $scope.retrievePagedProjects();
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
