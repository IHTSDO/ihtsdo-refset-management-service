// Refset controller
tsApp
  .controller(
    'RefsetCtrl',
    [
      '$scope',
      '$http',
      '$location',
      '$window',
      'tabService',
      'utilService',
      'securityService',
      'projectService',
      'refsetService',
      'workflowService',
      function($scope, $http, $location, $window, tabService, utilService, securityService, projectService,
        refsetService, workflowService) {
        console.debug('configure RefsetCtrl');

        // Handle resetting tabs on 'back' button
        if (tabService.selectedTab.label != 'Refset') {
          tabService.setSelectedTabByLabel('Refset');
        }

        // Initialize
        $scope.user = securityService.getUser();
        // If not logged in, redirect
        if ($http.defaults.headers.common.Authorization == 'guest') {
          $location.path('/');
          return;
        }

        projectService.getUserHasAnyRole();
        projectService.prepareIconConfig();
        $scope.accordionState = {};
        // Wrap in a json object so we can pass to the directive effectively
        $scope.projects = {
          data : [],
          totalCount : 0,
          assignedUsers : [],
          role : null
        };
        $scope.project = null;

        // Metadata for refsets, projects, etc.
        $scope.metadata = {
          refsetTypes : [],
          importHandlers : [],
          exportHandlers : [],
          workflowPaths : [],
          terminologies : [],
          versions : {},
          terminologyNames : {}
        };

        // Stats containers for refset-table sections
        $scope.available = {
          count : 0
        };
        $scope.assigned = {
          count : 0
        };
        $scope.released = {
          count : 0
        };

        // Get $scope.projects
        $scope.getProjects = function() {

          // Get all projects for this user
          var pfs = {
            startIndex : -1,
            maxResults : 10,
            sortField : 'name',
            queryRestriction : 'userAnyRole:' + $scope.user.userName
          };
          projectService.findProjectsAsList('', pfs).then(function(data) {
            $scope.projects.data = data.projects;
            $scope.projects.totalCount = data.totalCount;
            if ($scope.user.userPreferences.lastProjectId) {
              var found = false;
              for (var i = 0; i < data.projects.length; i++) {
                if (data.projects[i].id == $scope.user.userPreferences.lastProjectId) {
                  $scope.setProject(data.projects[i]);
                  found = true;
                  break;
                }
              }
              if (!found) {
                $scope.setProject(data.projects[0]);
              }
            } else {
              $scope.setProject(data.projects[0]);
            }
          });

        };

        // Fire a 'projectChanged' event after looking up role
        $scope.setProject = function(project) {
          $scope.project = project;
          if (!$scope.project) {
            return;
          }

          // Lookup terminology info for this project
          $scope.getTerminologyMetadata(project);

          // Only save lastProjectRole if lastProject is the same
          if ($scope.user.userPreferences.lastProjectId != $scope.project.id) {
            $scope.user.userPreferences.lastProjectRole = null;
            $scope.user.userPreferences.lastRefsetId = null;
            $scope.user.userPreferences.lastTranslationId = null;
          }
          $scope.user.userPreferences.lastProjectId = $scope.project.id;

          // Lookup workflow config for this project
          workflowService
            .getWorkflowConfig($scope.project.id)
            .then(
              // Success
              function(data) {
                $scope.metadata.workflowConfig = data;

                // Empty PFS
                var pfs = {};
                // Find role
                projectService
                  .findAssignedUsersForProject($scope.project.id, '', pfs)
                  .then(
                    // Success
                    function(data) {
                      $scope.projects.assignedUsers = data.users;
                      for (var i = 0; i < $scope.projects.assignedUsers.length; i++) {
                        if ($scope.projects.assignedUsers[i].userName == $scope.user.userName) {
                          $scope.projects.role = $scope.projects.assignedUsers[i].projectRoleMap[$scope.project.id];
                          var availableRoles = $scope.metadata.workflowConfig.refsetAvailableRoles.strings;

                          // determine role options for each project given the user's project role
                          // make all roles equal or lower to user's project role available
                          $scope.roleOptions = [];
                          for (var j = 0; j < availableRoles.length; j++) {
                            if ($scope.projects.role == availableRoles[j]) {
                              $scope.roleOptions.unshift(availableRoles[j]);
                              break;
                            } else {
                              $scope.roleOptions.unshift(availableRoles[j]);
                            }
                          }

                          // Force the initial choice to be "AUTHOR" instead of
                          // "ADMIN"
                          if ($scope.projects.role == 'ADMIN'
                            && !$scope.user.userPreferences.lastProjectRole) {
                            $scope.projects.role = 'AUTHOR';
                          }
                          if ($scope.user.userPreferences.lastProjectRole) {
                            $scope.projects.role = $scope.user.userPreferences.lastProjectRole;
                          }

                          // ensure that user's role is allowed on refset tab - if not, assign user to be AUTHOR
                          var found = false;
                          for (var j = 0; j < availableRoles.length; j++) {
                            if ($scope.projects.role == availableRoles[j]) {
                              found = true;
                              break;
                            }
                          }
                          if (!found) {
                            $scope.projects.role = 'AUTHOR';
                          }
                          break;
                        }
                      }
                      $scope.setRole($scope.projects.role);
                    });

              });

        };

        $scope.setRole = function() {
          if ($scope.user.userPreferences.lastProjectRole != $scope.projects.role) {
            $scope.user.userPreferences.lastProjectRole = $scope.projects.role;
            securityService.updateUserPreferences($scope.user.userPreferences);
          }
          projectService.fireProjectChanged($scope.project);
        };

        $scope.getInactiveConcepts = function() {
          if ($window
            .confirm('Checking for inactive concepts may take a couple of minutes.  Are you sure you want to proceed?')) {
            refsetService.getInactiveConcepts($scope.project.id).then(// Success
            function(data) {  
               var msg = '';
               for (var i = 0; i < data.totalCount; i++) {
                 msg += data.strings[i];
                 msg += '\n';
               }
               projectService.fireProjectChanged($scope.project);
               $scope.getProjects();
               if (data.totalCount > 0) {
                 $window.alert('The following refsets have inactive concepts:\n ' + msg);
               } else {
                 $window.alert('None of the refsets have inactive concepts.  No further action is required.');
               }
            });
          }
        }

        $scope.showInactiveLookupButton = function() {
          if (!$scope.project) {
            return false;
          }
          $scope.inactiveDate  = utilService.toDate($scope.project.inactiveLastModified);
          return ($scope.project.terminologyHandlerKey == 'MANAGED-SERVICE');           
        }
        
        // Lookup terminologies, names, and versions
        $scope.getTerminologyMetadata = function(project) {
          projectService.getTerminologyEditions(project).then(
          // Success
          function(data) {
            $scope.metadata.terminologies = data.terminologies;
            $scope.metadata.versions = {};
            // Look up all versions
            for (var i = 0; i < data.terminologies.length; i++) {
              var terminology = data.terminologies[i];
              $scope.metadata.terminologyNames[terminology.terminology] = terminology.name
              $scope.getTerminologyVersions(project, terminology.terminology);
            }
            console.debug('metadata.terminologyNames ', $scope.metadata.terminologyNames);
          });
        };
        $scope.getTerminologyVersions = function(project, terminology) {
          projectService.getTerminologyVersions(project, terminology).then(function(data) {
            $scope.metadata.versions[terminology] = [];
            for (var i = 0; i < data.terminologies.length; i++) {
              $scope.metadata.versions[terminology].push(data.terminologies[i].version);
            }
          });
        };

        // Determine whether the user is a project admin
        $scope.isProjectAdmin = function() {
          return $scope.projects.role == 'ADMIN';
        };

        // Get $scope.refsetTypes - for picklist
        $scope.getRefsetTypes = function() {
          refsetService.getRefsetTypes().then(function(data) {
            $scope.metadata.refsetTypes = data.strings;
          });
        };

        // Get $scope.metadata.{import,export}Handlers
        $scope.getIOHandlers = function() {
          refsetService.getImportRefsetHandlers().then(function(data) {
            $scope.metadata.refsetImportHandlers = data.handlers;
          });
          refsetService.getExportRefsetHandlers().then(function(data) {
            $scope.metadata.refsetExportHandlers = data.handlers;
          });
        };

        // Get $scope.metadata.workflowPaths
        $scope.getWorkflowPaths = function() {
          workflowService.getWorkflowPaths().then(function(data) {
            $scope.metadata.workflowPaths = data.strings;
          });
        };

        // Set the current accordion
        $scope.setAccordion = function(data) {
          utilService.clearError();
          if ($scope.user.userPreferences
            && $scope.user.userPreferences.lastRefsetAccordion != data) {
            $scope.user.userPreferences.lastRefsetAccordion = data;
            $scope.user.userPreferences.lastRefsetId = null;
            securityService.updateUserPreferences($scope.user.userPreferences);
          }
        };

        // Configure tab and accordion
        $scope.configureTab = function() {
          $scope.user.userPreferences.lastTab = '/refset';
          if ($scope.user.userPreferences.lastRefsetAccordion) {
            $scope.accordionState[$scope.user.userPreferences.lastRefsetAccordion] = true;
          } else {
            // default is available if nothing set
            $scope.accordionState['AVAILABLE'] = true;
            $scope.user.userPreferences.lastRefsetAccordion = 'AVAILABLE';
          }
          securityService.updateUserPreferences($scope.user.userPreferences);
        };

        // Initialize some metadata first time
        $scope.getProjects();
        $scope.getRefsetTypes();
        $scope.getIOHandlers();
        $scope.getWorkflowPaths();

        // Handle users with user preferences
        if ($scope.user.userPreferences) {
          $scope.configureTab();
        }

      }

    ]);
