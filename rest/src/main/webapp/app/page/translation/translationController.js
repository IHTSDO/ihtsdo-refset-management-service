// Translation controller
tsApp
  .controller(
    'TranslationCtrl',
    [
      '$scope',
      '$http',
      'tabService',
      'securityService',
      'projectService',
      'translationService',
      'workflowService',
      function($scope, $http, tabService, securityService, projectService, translationService,
        workflowService) {
        console.debug('configure TranslationCtrl');

        // Handle resetting tabs on "back" button
        if (tabService.selectedTab.label != 'Translation') {
          tabService.setSelectedTabByLabel('Translation');
        }

        // Initialize
        $scope.user = securityService.getUser();
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

        // Metadata for translations, projects, etc.
        $scope.metadata = {
          importHandlers : [],
          exportHandlers : [],
          workflowPaths : []
        }

        // Test for empty accordion state
        $scope.isAccordionStateEmpty = function() {
          for (key in $scope.accordionState) {
            if ($scope.accordionState.hasOwnProperty(key))
              return false;
          }
          return true;
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
          projectService.findProjectsAsList("", pfs).then(function(data) {
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
          })

        };

        // Fire a "projectChanged" event after looking up role
        $scope.setProject = function(project) {
          $scope.project = project;
          if (!$scope.project) {
            return;
          }
          $scope.user.userPreferences.lastProjectId = $scope.project.id;
          securityService.updateUserPreferences($scope.user.userPreferences);
          // Empty PFS
          var pfs = {};
          // Find role
          projectService
            .findAssignedUsersForProject($scope.project.id, "", pfs)
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
                    if ($scope.user.userPreferences.lastProjectRole) {
                      $scope.projects.role = $scope.user.userPreferences.lastProjectRole;
                    }
                    break;
                  }
                }

                projectService.fireProjectChanged($scope.project);
              })
        }

        $scope.updateRole = function() {
          $scope.user.userPreferences.lastProjectRole = $scope.projects.role;
          securityService.updateUserPreferences($scope.user.userPreferences);
          projectService.fireProjectChanged($scope.project);
        }

        // Determine whether the user is a project admin
        $scope.isProjectAdmin = function() {
          return $scope.projects.role == 'ADMIN';
        }

        // Get $scope.metadata.{import,export}Handlers
        $scope.getIOHandlers = function() {
          translationService.getImportTranslationHandlers().then(function(data) {
            $scope.metadata.importHandlers = data.handlers;
          });
          translationService.getExportTranslationHandlers().then(function(data) {
            $scope.metadata.exportHandlers = data.handlers;
          });
        }

        // Get $scope.metadata.workflowPaths
        $scope.getWorkflowPaths = function() {
          workflowService.getWorkflowPaths().then(function(data) {
            $scope.metadata.workflowPaths = data.strings;
          });
        }

        // Set the current accordion
        $scope.setAccordion = function(data) {
          $scope.user.userPreferences.lastTranslationAccordion = data;
          securityService.updateUserPreferences($scope.user.userPreferences);
        }

        // Configure tab and accordion
        $scope.configureTab = function() {
          $scope.user.userPreferences.lastTab = '/translation';
          if ($scope.user.userPreferences.lastTranslationAccordion) {
            $scope.accordionState[$scope.user.userPreferences.lastTranslationAccordion] = true;
          } else {
            // default is published if nothing set
            $scope.accordionState['EDITING'] = true;
          }
          securityService.updateUserPreferences($scope.user.userPreferences);
        }

        // Initialize
        $scope.getProjects();
        // Initialize some metadata first time
        $scope.getIOHandlers();
        $scope.getWorkflowPaths();

        // Handle users with user preferences
        if ($scope.user.userPreferences) {
          $scope.configureTab();
        }

      }

    ]);
