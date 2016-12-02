// Refset controller
tsApp
  .controller(
    'RefsetCtrl',
    [
      '$scope',
      '$http',
      '$location',
      'tabService',
      'utilService',
      'securityService',
      'projectService',
      'refsetService',
      'workflowService',
      function($scope, $http, $location, tabService, utilService, securityService, projectService,
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
          // Only save lastProjectRole if lastProject is the same
          if ($scope.user.userPreferences.lastProjectId != $scope.project.id) {
            $scope.user.userPreferences.lastProjectRole = null;
            $scope.user.userPreferences.lastRefsetId = null;
            $scope.user.userPreferences.lastTranslationId = null;
          }
          $scope.user.userPreferences.lastProjectId = $scope.project.id;
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
                    if ($scope.projects.role == 'ADMIN') {
                      $scope.roleOptions = [ 'ADMIN', 'REVIEWER', 'AUTHOR' ];
                    } else if ($scope.projects.role == 'REVIEWER') {
                      $scope.roleOptions = [ 'REVIEWER', 'AUTHOR' ];
                    } else if ($scope.projects.role == 'AUTHOR') {
                      $scope.roleOptions = [ 'AUTHOR' ];
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
                    break;
                  }
                }
                $scope.setRole($scope.projects.role);
              });
        };

        $scope.setRole = function() {
          if ($scope.user.userPreferences.lastProjectRole != $scope.projects.role) {
            $scope.user.userPreferences.lastProjectRole = $scope.projects.role;
            securityService.updateUserPreferences($scope.user.userPreferences);
          }
          projectService.fireProjectChanged($scope.project);
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
            $scope.metadata.importHandlers = data.handlers;
          });
          refsetService.getExportRefsetHandlers().then(function(data) {
            $scope.metadata.exportHandlers = data.handlers;
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
            securityService.updateUserPreferences($scope.user.userPreferences);
          }
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
