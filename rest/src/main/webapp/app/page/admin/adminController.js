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
        $scope.pagedCandidateProjects = null;
        $scope.pagedUsers = null;
        $scope.pagedAssignedUsers = null;
        $scope.pagedCandidateUsers = null;

        $scope.projectPaging = {
          page : 1,
          filter : "",
          sortField : 'lastModified',
          ascending : []
        }
        
        $scope.candidateProjectPaging = {
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
        
        $scope.assignedUserPaging = {
          page : 1,
          filter : "",
          sortField : 'email',
          ascending : []
        }        
        
        $scope.candidateUserPaging = {
          page : 1,
          filter : "",
          sortField : 'email',
          ascending : []
        }        

        // get projects
        $scope.retrievePagedProjects = function() {
          
          var pfs = {
            startIndex : ($scope.projectPaging.page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.projectPaging.sortField,
            ascending : $scope.projectPaging.ascending.indexOf($scope.projectPaging.sortField) != -1 ? true : false,
            queryRestriction : null
          };
       
          projectService.findProjectsAsList($scope.projectPaging.filter, pfs).then(function(data) {
            $scope.pagedProjects = data.projects;
            $scope.pagedProjects.totalCount = data.totalCount;
          })
          
        };
        
        // get candidate projects
        // one of these projects can be selected for user and role assignment
        $scope.retrieveCandidateProjects = function() {
          
          var pfs = {
            startIndex : ($scope.candidateProjectPaging.page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.candidateProjectPaging.sortField,
            ascending : $scope.candidateProjectPaging.ascending.indexOf($scope.candidateProjectPaging.sortField) != -1 ? true : false,
            queryRestriction : 'userRoleMap:' + $scope.user.userName + 'ADMIN'
          };

          // clear queryRestriction for application admins
          if ($scope.user.applicationRole == 'ADMIN') {
            pfs.queryRestriction = null;
          }
          
          projectService.findProjectsAsList($scope.candidateProjectPaging.filter, pfs).then(function(data) {
            $scope.pagedCandidateProjects = data.projects;
            $scope.pagedCandidateProjects.totalCount = data.totalCount;
          })
          
        };
        
        // get users
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
        
        // get candidate users - this is the list of users that are not yet
        // assigned to the selected project
        $scope.retrievePagedCandidateUsers = function() {
          var pfs = {
            startIndex : ($scope.candidateUserPaging.page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.candidateUserPaging.sortField,
            ascending : $scope.candidateUserPaging.ascending.indexOf($scope.candidateUserPaging.sortField) != -1 ? true : false,  
            queryRestriction : null
          };
         
          projectService.findCandidateUsersForProject($scope.selectedProject.id, $scope.candidateUserPaging.filter, pfs).then(function(data) {
            $scope.pagedCandidateUsers = data.users;
            $scope.pagedCandidateUsers.totalCount = data.totalCount;
          }) 
        };
        
        // get assigned users - this is the list of users that are already
        // assigned to the selected project
        $scope.retrievePagedAssignedUsers = function() {

          var pfs = {
            startIndex : ($scope.assignedUserPaging.page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.assignedUserPaging.sortField,
            ascending : $scope.assignedUserPaging.ascending.indexOf($scope.assignedUserPaging.sortField) != -1 ? true : false,  
            queryRestriction : null
          };
          projectService.findUsersForProject($scope.selectedProject.id, $scope.assignedUserPaging.filter, pfs).then(function(data) {
            $scope.pagedAssignedUsers = data.users;
            $scope.pagedAssignedUsers.totalCount = data.totalCount;
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

          $scope.retrievePagedCandidateUsers();
          $scope.retrievePagedAssignedUsers();
        }
        
        // remove a project, a user - only application admins can do this
        $scope.remove = function(type, object, objArray) {
          if (!confirm("Are you sure you want to remove the " + type + " ("
            + object.name + ")?")) {
            return;
          }
          if (type == 'project') {
            if (object.userRoleMap != null && object.userRoleMap != undefined 
              && Object.keys(object.userRoleMap).length > 0) {
            window.alert("You can not delete a project that has users assigned to it. Remove the assigned users before deleting the project.");
            return;
          }
            projectService.removeProject(object).then(function() {
              $scope.retrievePagedProjects();
              $scope.retrieveCandidateProjects();
            });
          }
          if (type == 'user') {
            if (object.projectRoleMap != null && object.projectRoleMap != undefined 
                && Object.keys(object.projectRoleMap).length > 0) {
              window.alert("You can not delete a user that is assigned to a project. Remove this user from all projects before deleting it.");
              return;
            }
              securityService.removeUser(object).then(function() {
              $scope.retrievePagedUsers();
              if ($scope.selectedProject != null) {
                $scope.retrievePagedCandidateUsers();
                $scope.retrievePagedAssignedUsers();
              }
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
        
        // sort mechanism for candidate project table
        $scope.setCandidateProjectSortField = function(field) {
          console.debug("setCandidateProjectSortField " + field);
          $scope.candidateProjectPaging.sortField = field;
          var fieldIndex = $scope.candidateProjectPaging.ascending.indexOf(field);
          if (fieldIndex != -1) {
            $scope.candidateProjectPaging.ascending.splice(fieldIndex, 1);
          } else {
            $scope.candidateProjectPaging.ascending.push(field);
          }
          $scope.retrieveCandidateProjects();
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
        
        // sort mechanism for candidate user table
        $scope.setCandidateUserSortField = function(field) {
          $scope.candidateUserPaging.sortField = field;
          var fieldIndex = $scope.candidateUserPaging.ascending.indexOf(field);
          if (fieldIndex != -1) {
            $scope.candidateUserPaging.ascending.splice(fieldIndex, 1);
          } else {
            $scope.candidateUserPaging.ascending.push(field);
          }
          $scope.retrievePagedCandidateUsers();
        }
        
        // sort mechanism for assigned user table
        $scope.setAssignedUserSortField = function(field) {
          $scope.assignedUserPaging.sortField = field;
          var fieldIndex = $scope.assignedUserPaging.ascending.indexOf(field);
          if (fieldIndex != -1) {
            $scope.assignedUserPaging.ascending.splice(fieldIndex, 1);
          } else {
            $scope.assignedUserPaging.ascending.push(field);
          }
          $scope.retrievePagedAssignedUsers();
        }
        
        // assign user to project
        $scope.assignUserToProject = function(projectId, userName, projectRole) {
          if (projectId == null || projectId == undefined) {
            window.alert("Select a project before assigning a user! ");
            return;
          }
          
          projectService.assignUserToProject(projectId, userName, projectRole).then(
            function(data) {
              $scope.retrievePagedProjects();
              $scope.selectedProject = data;
              $scope.retrievePagedAssignedUsers();
              $scope.retrievePagedCandidateUsers();
            })
        };

        // remove user from project
        $scope.unassignUserFromProject = function(projectId, userName) {
          projectService.unassignUserFromProject(projectId, userName).then(
            function(data) {

              $scope.retrievePagedProjects();
              $scope.selectedProject = data;
              $scope.retrievePagedAssignedUsers();
              $scope.retrievePagedCandidateUsers();
            })
        };

        
        //
        // call these during initialization
        //
        
        $scope.retrievePagedProjects();
        $scope.retrievePagedUsers();
        $scope.retrieveCandidateProjects();
        $scope.getApplicationRoles();
        $scope.getProjectRoles();

        //
        // Modals
        //

        // modal for creating a new project - only application admins can do this
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
          
          modalInstance.result.finally(function() {
            $scope.retrieveCandidateProjects();
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

        // modal for creating a new user- only application admins can do this
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
          
          modalInstance.result.finally(function() {
            $scope.retrievePagedCandidateUsers();
            $scope.retrievePagedAssignedUsers();
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

        // modal for editing a project - only application admins can do this
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
          
          modalInstance.result.finally(function() {
            $scope.retrieveCandidateProjects();
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

        // modal for editing a user - only application admins can do this
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
          
          modalInstance.result.finally(function() {
            $scope.retrievePagedCandidateUsers();
            $scope.retrievePagedAssignedUsers();
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
