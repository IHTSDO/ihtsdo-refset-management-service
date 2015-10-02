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
        $scope.user = securityService.getUser();
        
        $scope.pageSize = 4;
        
        $scope.pagedProjects = null;
        $scope.pagedUsers = null;

        $scope.projectPaging = {
          page : 1,
          filter : "",
          sortField : 'lastModified',
          ascending : []
        }
        
        $scope.userPaging = {
          page : 1,
          filter : "",
          sortField : 'email',
          ascending : []
        }        
        
        // Search parameters
        //$scope.searchParams = securityService.getSearchParams();



        // get paged projects
        $scope.retrievePagedProjects = function() {
          
          var pfs = {
            startIndex : ($scope.projectPaging.page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.projectPaging.sortField,
            ascending : $scope.projectPaging.ascending.indexOf($scope.projectPaging.sortField) != -1 ? true : false,
            queryRestriction : 'projectRoleMap:' + $scope.user.userName + 'ADMIN'
          };

          // clear queryRestriction for application admins
          if ($scope.user.applicationRole == 'ADMIN') {
            pfs.queryRestriction = null;
          }
          
          projectService.findProjectsAsList($scope.projectPaging.filter, pfs).then(function(data) {
            $scope.pagedProjects = data.projects;
            $scope.pagedProjects.totalCount = data.totalCount;
          })
          
        };
        

        // get paged users
        $scope.retrievePagedUsers = function() {

          var pfs = {
            startIndex : ($scope.userPaging.page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.userPaging.sortField,
            ascending : $scope.userPaging.ascending.indexOf($scope.userPaging.sortField) != -1 ? true : false,  
            queryRestriction : null
          };
         
          securityService.findUsersAsList($scope.userPaging.filter, pfs).then(function(data) {
            $scope.pagedUsers = data.users;
            $scope.pagedUsers.totalCount = data.totalCount;
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
        
        // remove a project, a user
        $scope.remove = function(type, object, objArray) {
          if (!confirm("Are you sure you want to remove the " + type + " ("
            + object.name + ")?")) {
            return;
          }
          if (type == 'project') {
            projectService.removeProject(object).then(function() {
              $scope.retrievePagedProjects();
            });
          }
          if (type == 'user') {
            securityService.removeUser(object).then(function() {
              $scope.retrievePagedUsers();
            });
          }
        };

        // sort mechanism for project table
        $scope.setProjectSortField = function(field) {
          $scope.projectPaging.sortField = field;
          var fieldIndex = $scope.projectPaging.ascending.indexOf(field);
          if (fieldIndex != -1) {
            $scope.projectPaging.ascending.splice(fieldIndex, 1);
          } else {
            $scope.projectPaging.ascending.push(field);
          }
          $scope.retrievePagedProjects();
        }
        
        // sort mechanism for user table
        $scope.setUserSortField = function(field) {
          $scope.userPaging.sortField = field;
          var fieldIndex = $scope.userPaging.ascending.indexOf(field);
          if (fieldIndex != -1) {
            $scope.userPaging.ascending.splice(fieldIndex, 1);
          } else {
            $scope.userPaging.ascending.push(field);
          }
          $scope.retrievePagedUsers();
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
              $scope.retrievePagedProjects();
              $scope.selectedProject = data;
              for (var i = $scope.userResults.length - 1; i >= 0; i--) {
                if (userName == $scope.userResults[i].userName) {
                  $scope.userResults.splice(i, 1);
                }
              }
            })
        };

        // remove user from project
        $scope.unassignUserFromProject = function(projectId, userName) {
          projectService.unassignUserFromProject(projectId, userName).then(
            function(data) {

              $scope.retrievePagedProjects();
              $scope.selectedProject = data;
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
        
        // get project role for user
        function getProjectRoleForUser(project, user) {
          for (var i = 0; i < project.projectRoleMap.entry.length; i++) {
            if (project.projectRoleMap.entry[i].key.userName == user.userName)
              return project.projectRoleMap.entry[i].value;
          }
          return null;
        };
        
        //
        // call these during initialization
        //
        
        $scope.retrievePagedProjects();
        $scope.retrievePagedUsers();
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
                return $scope.pagedProjects;
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
                return $scope.pagedUsers;
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
