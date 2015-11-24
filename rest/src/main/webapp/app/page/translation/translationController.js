// Translation controller
tsApp
  .controller(
    'TranslationCtrl',
    [
      '$scope',
      '$http',
      '$rootScope',
      'tabService',
      'securityService',
      'projectService',
      'translationService',
      'workflowService',
      function($scope, $http, $rootScope, tabService, securityService, projectService,
        translationService, workflowService) {
        console.debug('configure TranslationCtrl');

        // Handle resetting tabs on "back" button
        if (tabService.selectedTab.label != 'Translation') {
          tabService.setSelectedTabByLabel('Translation');
        }

        // Initialize
        projectService.prepareIconConfig();
        $scope.user = securityService.getUser();
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
            $scope.setProject(data.projects[0]);
          })

        };

        // Fire a "projectChanged" event after looking up role
        $scope.setProject = function(project) {
          $scope.project = project;
          if (!$scope.project) {
            return;
          }

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
                    break;
                  }
                }

                projectService.fireProjectChanged($scope.project);
              })
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

        // Initialize
        $scope.getProjects();
        // Initialize some metadata first time
        $scope.getIOHandlers();
        $scope.getWorkflowPaths();
      }

    ]);
