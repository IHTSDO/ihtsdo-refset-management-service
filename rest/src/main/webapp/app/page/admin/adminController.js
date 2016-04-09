// Administration controller
tsApp
  .controller(
    'AdminCtrl',
    [
      '$scope',
      '$http',
      '$location',
      '$uibModal',
      'gpService',
      'utilService',
      'tabService',
      'securityService',
      'projectService',
      'validationService',
      'refsetService',
      'translationService',
      function($scope, $http, $location, $uibModal, gpService, utilService, tabService,
        securityService, projectService, validationService, refsetService, translationService) {
        console.debug('configure AdminCtrl');

        // Clear error
        utilService.clearError();

        // Handle resetting tabs on 'back' button
        if (tabService.selectedTab.label != 'Admin') {
          tabService.setSelectedTabByLabel('Admin');
        }

        //
        // Scope Variables
        //
        $scope.user = securityService.getUser();
        // If not logged in, redirect
        if ($http.defaults.headers.common.Authorization == 'guest') {
          $location.path('/');
          return;
        }

        projectService.getUserHasAnyRole();

        $scope.selectedProject = null;
        $scope.projectRoles = [];

        // Model variables
        $scope.projects = null;
        $scope.candiateProjects = null;
        $scope.users = null;
        $scope.assignedUsers = null;
        $scope.unassignedUsers = null;
        $scope.languageDescriptionTypes = [];
        $scope.pagedAvailableLdt = [];

        // Metadata for refsets, projects, etc.
        $scope.metadata = {
          terminologies : []
        };
        
        $scope.userPreferences = {
          moduleId : $scope.user.userPreferences.moduleId,
          namespace : $scope.user.userPreferences.namespace,
          organization : $scope.user.userPreferences.organization,
          exclusionClause : $scope.user.userPreferences.exclusionClause,
          feedbackEmail : $scope.user.userPreferences.feedbackEmail
        }
        $scope.moduleIdChanged = false;
        $scope.namespaceChanged = false;
        $scope.organizationChanged = false;
        $scope.exclusionClauseChanged = false;
        $scope.feedbackEmailChanged = false;

        // Paging variables
        $scope.pageSize = 10;
        $scope.paging = {};
        $scope.paging['project'] = {
          page : 1,
          filter : '',
          sortField : 'lastModified',
          ascending : null
        };
        $scope.paging['candidateProject'] = {
          page : 1,
          filter : '',
          sortField : 'lastModified',
          ascending : null
        };
        $scope.paging['user'] = {
          page : 1,
          filter : '',
          sortField : 'userName',
          ascending : null
        };
        $scope.paging['assignedUser'] = {
          page : 1,
          filter : '',
          sortField : 'userName',
          ascending : null
        };
        $scope.paging['candidateUser'] = {
          page : 1,
          filter : '',
          sortField : 'userName',
          ascending : null
        };
        $scope.paging['lang'] = {
          page : 1,
          filter : '',
          typeFilter : '',
          sortField : 'refsetId',
          ascending : true
        };

        // Get $scope.projects
        $scope.getProjects = function() {

          var pfs = {
            startIndex : ($scope.paging['project'].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging['project'].sortField,
            ascending : $scope.paging['project'].ascending == null ? true
              : $scope.paging['project'].ascending,
            queryRestriction : 'userRoleMap:' + $scope.user.userName + 'ADMIN'
          };
          // clear queryRestriction for application admins
          if ($scope.user.applicationRole == 'ADMIN') {
            pfs.queryRestriction = null;
          }
          projectService.findProjectsAsList($scope.paging['project'].filter, pfs).then(
            function(data) {
              $scope.projects = data.projects;
              $scope.projects.totalCount = data.totalCount;

            });

        };

        // Get $scope.candidateProjects
        // one of these projects can be selected for user and role
        // assignment
        $scope.getCandidateProjects = function() {

          var pfs = {
            startIndex : ($scope.paging['candidateProject'].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging['candidateProject'].sortField,
            ascending : $scope.paging['candidateProject'].ascending == null ? true
              : $scope.paging['candidateProject'].ascending,
            queryRestriction : 'userRoleMap:' + $scope.user.userName + 'ADMIN'
          };
          // clear queryRestriction for application admins
          if ($scope.user.applicationRole == 'ADMIN') {
            pfs.queryRestriction = null;
          }

          projectService.findProjectsAsList($scope.paging['candidateProject'].filter, pfs).then(
            function(data) {
              $scope.candidateProjects = data.projects;
              $scope.candidateProjects.totalCount = data.totalCount;
            });

        };

        // Get $scope.users
        $scope.getUsers = function() {

          var pfs = {
            startIndex : ($scope.paging['user'].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging['user'].sortField,
            ascending : $scope.paging['user'].ascending,
            ascending : $scope.paging['user'].ascending == null ? true
              : $scope.paging['user'].ascending,
            queryRestriction : null
          };

          securityService.findUsersAsList($scope.paging['user'].filter, pfs).then(function(data) {
            $scope.users = data.users;
            $scope.users.totalCount = data.totalCount;
          });

        };

        // Get $scope.unassignedUsers
        // this is the list of users that are not yet
        // assigned to the selected project
        $scope.getUnassignedUsers = function() {
          var pfs = {
            startIndex : ($scope.paging['candidateUser'].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging['candidateUser'].sortField,
            ascending : $scope.paging['candidateUser'].ascending == null ? true
              : $scope.paging['candidateUser'].ascending,
            queryRestriction : '(applicationRole:USER OR applicationRole:ADMIN)'
          };

          projectService.findUnassignedUsersForProject($scope.selectedProject.id,
            $scope.paging['candidateUser'].filter, pfs).then(function(data) {
            $scope.unassignedUsers = data.users;
            $scope.unassignedUsers.totalCount = data.totalCount;
          });
        };

        // Get $scope.assignedUsers
        // this is the list of users that are already
        // assigned to the selected project
        $scope.getAssignedUsers = function() {

          var pfs = {
            startIndex : ($scope.paging['assignedUser'].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging['assignedUser'].sortField,
            ascending : $scope.paging['assignedUser'].ascending == null ? true
              : $scope.paging['assignedUser'].ascending,
            queryRestriction : null
          };
          projectService.findAssignedUsersForProject($scope.selectedProject.id,
            $scope.paging['assignedUser'].filter, pfs).then(function(data) {
            $scope.assignedUsers = data.users;
            $scope.assignedUsers.totalCount = data.totalCount;
          });

        };

        // Get $scope.applicationRoles
        $scope.getApplicationRoles = function() {
          securityService.getApplicationRoles().then(function(data) {
            $scope.applicationRoles = data.strings;
          });
        };

        // Get $scope.projectRoles
        $scope.getProjectRoles = function() {
          projectService.getProjectRoles().then(function(data) {
            $scope.projectRoles = data.strings;
          });
        };

        // Get $scope.metadata.terminologies
        $scope.getTerminologyEditions = function() {
          projectService.getTerminologyEditions().then(function(data) {
            $scope.metadata.terminologies = data.terminologies;
          });

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
        };

        // Removes a project
        $scope.removeProject = function(project) {
          // check for users
          if (project.userRoleMap != null && project.userRoleMap != undefined
            && Object.keys(project.userRoleMap).length > 0) {
            if (!confirm('The project has users assigned to it.  Are you sure you want to remove the project ('
              + project.name + ') and unassign all of its users?')) {
              return;
            }
          }

          // Check for refsets
          refsetService
            .findRefsetsForQuery("projectId:" + project.id, {
              startIndex : 0,
              maxResults : 1
            })
            .then(
              // Successs
              function(data) {
                console.debug("DATA", data);
                // if refsets, stop
                if (data.refsets.length > 0) {
                  alert('The project has refsets attached. The refsets for this project must be removed'
                    + ' before the project itself can be removed');
                  return;
                }
                // Otherwise, remove project
                projectService.removeProject(project).then(
                // Success
                function() {
                  // Refresh projects
                  $scope.getProjects();
                  $scope.getCandidateProjects();
                });
              } // no need for error function
            );

        };

        // Removes a user
        $scope.removeUser = function(user) {
          if (user.projectRoleMap && Object.keys(user.projectRoleMap).length > 0) {
            window.alert('You can not delete a user that is assigned to a project -'
              + 'Remove this user from all projects before deleting it');
            return;
          }
          securityService.removeUser(user).then(function() {
            // Refresh users
            $scope.getUsers();
            if ($scope.selectedProject != null) {
              $scope.getUnassignedUsers();
              $scope.getAssignedUsers();
            }
          });

        };

        // update a specific user preference
        $scope.saveUserPreference = function(item, value) {
          if (item == 'moduleId') {
            $scope.user.userPreferences.moduleId = value;
            $scope.moduleIdChanged = false;
          } else if (item == 'namespace') {
            $scope.user.userPreferences.namespace = value;
            $scope.namespaceChanged = false;
          } else if (item == 'organization') {
            $scope.user.userPreferences.organization = value;
            $scope.organizationChanged = false;
          } else if (item == 'exclusionClause') {
            $scope.user.userPreferences.exclusionClause = value;
            $scope.exclusionClauseChanged = false;
          } else if (item == 'feedbackEmail') {
            $scope.user.userPreferences.feedbackEmail = value;
            $scope.feedbackEmailChanged = false;
          }
          
          $scope.saveUserPreferences();
        };
        
        // Save the user preferences
        $scope.saveUserPreferences = function() {
          securityService.updateUserPreferences($scope.user.userPreferences).then(
          // Success
          function(data) {
            $scope.user.userPreferences = data;
            $scope.getPagedAvailableLdt();
          });
        };
        
        // indicate that a user preference value has changed
        $scope.setChanged = function(item) {
          if (item == 'moduleId') {
            $scope.moduleIdChanged = true;
          } else if (item == 'namespace') {
            $scope.namespaceChanged = true;
          } else if (item == 'organization') {
            $scope.organizationChanged = true;
          } else if (item == 'exclusionClause') {
            $scope.exclusionClauseChanged = true;
          } else if (item == 'feedbackEmail') {
            $scope.feedbackEmailChanged = true; 
          }
        }

        // Get $scope.languageDescriptionTypes
        $scope.getLanguageDescriptionTypes = function() {
          translationService.getLanguageDescriptionTypes().then(
          // Success
          function(data) {
            $scope.languageDescriptionTypes = data.types;
            $scope.languageDescriptionTypes.totalCount = data.totalCount;
            $scope.getPagedAvailableLdt();
          });
        };

        // Get paged available language description types not already assigned
        $scope.getPagedAvailableLdt = function() {
          var available = [];
          for (var i = 0; i < $scope.languageDescriptionTypes.length; i++) {
            var found = false;
            for (var j = 0; j < $scope.user.userPreferences.languageDescriptionTypes.length; j++) {
              if ($scope.isEquivalent($scope.languageDescriptionTypes[i],
                $scope.user.userPreferences.languageDescriptionTypes[j])) {
                found = true;
                break;
              }
            }
            if (!found) {
              available.push($scope.languageDescriptionTypes[i]);
            }
          }
          $scope.pagedAvailableLdt = utilService.getPagedArray(available, $scope.paging['lang'],
            $scope.pageSize);
        };

        // equivalent test for language description types
        $scope.isEquivalent = function(ldt1, ldt2) {
          return ldt1.refsetId == ldt2.refsetId
            && ldt1.descriptionType.typeId == ldt2.descriptionType.typeId
            && ldt1.descriptionType.acceptabilityId == ldt2.descriptionType.acceptabilityId;
        };

        // Add an LDT to user prefs
        $scope.addLanguageDescriptionType = function(ldt) {
          $scope.user.userPreferences.languageDescriptionTypes.push(ldt);
          $scope.saveUserPreferences();
        };

        // Remove an LDT from user prefs
        $scope.removeLanguageDescriptionType = function(ldt) {
          for (var i = 0; i < $scope.user.userPreferences.languageDescriptionTypes.length; i++) {
            if ($scope.isEquivalent(ldt, $scope.user.userPreferences.languageDescriptionTypes[i])) {
              $scope.user.userPreferences.languageDescriptionTypes.splice(i, 1);
            }
          }
          $scope.saveUserPreferences();
        };

        // Move an LDT up in user prefs
        $scope.moveLanguageDescriptionTypeUp = function(ldt) {
          // Start at index 1 because we can't move the top one up
          for (var i = 1; i < $scope.user.userPreferences.languageDescriptionTypes.length; i++) {
            if ($scope.isEquivalent(ldt, $scope.user.userPreferences.languageDescriptionTypes[i])) {
              $scope.user.userPreferences.languageDescriptionTypes.splice(i, 1);
              $scope.user.userPreferences.languageDescriptionTypes.splice(i - 1, 0, ldt);
            }
          }
          $scope.saveUserPreferences();
        };

        // Move an LDT down in user prefs
        $scope.moveLanguageDescriptionTypeDown = function(ldt) {
          // end at index -11 because we can't move the last one down
          for (var i = 0; i < $scope.user.userPreferences.languageDescriptionTypes.length - 1; i++) {
            if ($scope.isEquivalent(ldt, $scope.user.userPreferences.languageDescriptionTypes[i])) {
              console.debug(' ldt 1 = ', $scope.user.userPreferences.languageDescriptionTypes);
              $scope.user.userPreferences.languageDescriptionTypes.splice(i, 2,
                $scope.user.userPreferences.languageDescriptionTypes[i + 1], ldt);
              console.debug(' ldt 2 = ', $scope.user.userPreferences.languageDescriptionTypes);
              break;
            }
          }
          $scope.saveUserPreferences();
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
          } else if (table === 'lang') {
            $scope.getPagedAvailableLdt();
          }
        };

        // Return up or down sort chars if sorted
        $scope.getSortIndicator = function(table, field) {
          return utilService.getSortIndicator(table, field, $scope.paging);
        };

        // assign user to project
        $scope.assignUserToProject = function(projectId, userName, projectRole) {
          if (projectId == null || projectId == undefined) {
            window.alert('Select a project before assigning a user! ');
            return;
          }
          // call service
          projectService.assignUserToProject(projectId, userName, projectRole).then(function(data) {
            // Update 'anyrole'
            projectService.getUserHasAnyRole();
            $scope.getProjects();
            $scope.selectedProject = data;
            $scope.getAssignedUsers();
            $scope.getUnassignedUsers();
          });
        };

        // remove user from project
        $scope.unassignUserFromProject = function(projectId, userName) {
          projectService.unassignUserFromProject(projectId, userName).then(function(data) {
            // Update 'anyrole' in case user removed themselves from the project
            projectService.getUserHasAnyRole();
            $scope.getProjects();
            $scope.selectedProject = data;
            $scope.getAssignedUsers();
            $scope.getUnassignedUsers();
          });
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
        };

        $scope.getValidationChecks = function() {
          validationService.getValidationCheckNames().then(
          // Success
          function(data) {
            $scope.validationChecks = data.keyValuePairs;
          });
        };

        //
        // MODALS
        //

        // Add project modal
        $scope.openAddProjectModal = function() {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/admin/editProject.html',
            backdrop : 'static',
            controller : AddProjectModalCtrl,
            resolve : {
              metadata : function() {
                return $scope.metadata;
              },
              user : function() {
                return $scope.user;
              },
              validationChecks : function() {
                return $scope.validationChecks;
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(project) {
            projectService.fireProjectChanged(project);
            $scope.getProjects();
            $scope.getCandidateProjects();

          });
        };

        // Add project controller
        var AddProjectModalCtrl = function($scope, $uibModalInstance, metadata, user,
          validationChecks) {

          $scope.action = 'Add';
          $scope.project = {
            terminology : metadata.terminologies[0].terminology,
            moduleId : user.userPreferences.moduleId,
            namespace : user.userPreferences.namespace,
            organization : user.userPreferences.organization,
            exclusionClause : user.userPreferences.exclusionClause,
            feedbackEmail : user.userPreferences.feedbackEmail
          };
          $scope.clause = {
            value : null
          };
          $scope.terminologies = metadata.terminologies;
          $scope.metadata = metadata;
          $scope.user = user;
          $scope.validationChecks = validationChecks;
          $scope.availableChecks = [];
          $scope.selectedChecks = [];
          $scope.errors = [];

          // Wire default validation check 'on' by default
          for (var i = 0; i < $scope.validationChecks.length; i++) {
            if ($scope.validationChecks[i].value == 'Default validation check') {
              $scope.selectedChecks.push($scope.validationChecks[i].value);
            } else {
              $scope.availableChecks.push($scope.validationChecks[i].value);
            }
          }

          // move a check from unselected to selected
          $scope.selectValidationCheck = function(check) {
            $scope.selectedChecks.push(check);
            var index = $scope.availableChecks.indexOf(check);
            $scope.availableChecks.splice(index, 1);
          };

          // move a check from selected to unselected
          $scope.removeValidationCheck = function(check) {
            $scope.availableChecks.push(check);
            var index = $scope.selectedChecks.indexOf(check);
            $scope.selectedChecks.splice(index, 1);
          };

          // Add the project
          $scope.submitProject = function(project) {
            if (!project || !project.name || !project.description || !project.terminology) {
              window.alert('The name, description, and terminology fields cannot be blank. ');
              return;
            }
            // Connect validation checks
            project.validationChecks = [];
            for (var i = 0; i < $scope.validationChecks.length; i++) {
              if ($scope.selectedChecks.indexOf($scope.validationChecks[i].value) != -1) {
                project.validationChecks.push($scope.validationChecks[i].key);
              }
            }

            // copy clause - don't allow negation - it's implicitly negated
            project.exclusionClause = $scope.clause == null ? null : $scope.clause.value;

            // Add project - this will validate the expression
            projectService.addProject(project).then(
              // Success
              function(data) {
                // if not an admin, add user as a project admin
                if ($scope.user.applicationRole != 'ADMIN') {
                  var projectId = data.id;
                  projectService.assignUserToProject(data.id, $scope.user.userName, 'ADMIN').then(
                    function(data) {
                      // Update 'anyrole'
                      projectService.getUserHasAnyRole();

                      // Set the "last project" setting to this project
                      $scope.user.userPreferences.lastProjectId = projectId;
                      securityService.updateUserPreferences($scope.user.userPreferences);
                      $uibModalInstance.close(data);
                    },
                    // Error
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    });
                } else {
                  $uibModalInstance.close(data);
                }
              },
              // Error
              function(data) {
                $scope.errors[0] = data;
                utilService.clearError();
              });
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
            backdrop : 'static',
            controller : EditProjectModalCtrl,
            resolve : {
              project : function() {
                return lproject;
              },
              metadata : function() {
                return $scope.metadata;
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

        var EditProjectModalCtrl = function($scope, $uibModalInstance, project, metadata,
          validationChecks) {

          $scope.action = 'Edit';
          $scope.clause = {
            value : project.exclusionClause
          };
          $scope.project = project;
          $scope.metadata = metadata;
          $scope.validationChecks = validationChecks;
          $scope.availableChecks = [];
          $scope.selectedChecks = [];
          $scope.errors = [];

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
          };

          $scope.removeValidationCheck = function(check) {
            $scope.availableChecks.push(check);
            var index = $scope.selectedChecks.indexOf(check);
            $scope.selectedChecks.splice(index, 1);
          };

          $scope.submitProject = function(project) {
            if (!project || !project.name || !project.description || !project.terminology) {
              window.alert('The name, description, and terminology fields cannot be blank. ');
              return;
            }

            project.validationChecks = [];
            for (var i = 0; i < $scope.validationChecks.length; i++) {
              if ($scope.selectedChecks.indexOf($scope.validationChecks[i].value) != -1) {
                project.validationChecks.push($scope.validationChecks[i].key);
              }
            }

            // copy clause - don't allow negation - it's implicitly negated
            project.exclusionClause = $scope.clause == null ? null : $scope.clause.value;
            // Update project - this will validate the expression
            projectService.updateProject(project).then(
            // Success
            function(data) {
              $uibModalInstance.close();
            },
            // Error
            function(data) {
              $scope.errors[0] = data;
              utilService.clearError();
            });
          };

          $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
          };

        };

        // Add user modal
        $scope.openAddUserModal = function(luser) {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/admin/editUser.html',
            backdrop : 'static',
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
          $scope.applicationRoles = applicationRoles;
          $scope.errors = [];

          $scope.submitUser = function(user) {
            if (!user || !user.name || !user.userName || !user.applicationRole) {
              window.alert('The name, user name, and application role fields cannot be blank. ');
              return;
            }
            securityService.addUser(user).then(
            // Success
            function(data) {
              $uibModalInstance.close();
            },
            // Error
            function(data) {
              $scope.errors[0] = data;
              utilService.clearError();
            });

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
            backdrop : 'static',
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
          // copy data structure so it will be fresh each time modal is opened
          $scope.applicationRoles = JSON.parse(JSON.stringify(applicationRoles));
          $scope.errors = [];

          // those without application admin roles, can't give themselves admin
          // roles
          if (user.applicationRole != 'ADMIN') {
            var index = $scope.applicationRoles.indexOf('ADMIN');
            $scope.applicationRoles.splice(index, 1);
          }

          $scope.submitUser = function(user) {

            if (!user || !user.name || !user.userName || !user.applicationRole) {
              window.alert('The name, user name, and application role fields cannot be blank. ');
              return;
            }

            securityService.updateUser(user).then(
            // Success
            function(data) {
              $uibModalInstance.close();
            },
            // Error
            function(data) {
              $scope.error[0] = data;
              utilService.clearError();
            });
          };

          $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
          };

        };

        // Configure the tab
        $scope.configureTab = function() {
          $scope.user.userPreferences.lastTab = '/admin';
          securityService.updateUserPreferences($scope.user.userPreferences);
        };

        //
        // Initialize
        //
        $scope.getProjects();
        $scope.getUsers();
        $scope.getCandidateProjects();
        $scope.getApplicationRoles();
        $scope.getProjectRoles();
        $scope.getTerminologyEditions();
        $scope.getValidationChecks();
        $scope.getLanguageDescriptionTypes();

        // Handle users with user preferences
        if ($scope.user.userPreferences) {
          $scope.configureTab();
        }

        // end

      }

    ]);
