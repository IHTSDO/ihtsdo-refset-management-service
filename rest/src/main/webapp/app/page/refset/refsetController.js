// Refset controller
tsApp
  .controller(
    'RefsetCtrl',
    [
      '$scope',
      '$http',
      '$rootScope',
      'tabService',
      'securityService',
      'projectService',
      'refsetService',
      'workflowService',
      function($scope, $http, $rootScope, tabService, securityService, projectService,
        refsetService, workflowService) {
        console.debug('configure RefsetCtrl');

        // Handle resetting tabs on "back" button
        if (tabService.selectedTab.label != 'Refset') {
          tabService.setSelectedTabByLabel('Refset');
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

        // Metadata for refsets, projects, etc.
        $scope.metadata = {
          refsetTypes : [],
          terminologies : [],
          versions : {},
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

        // Get $scope.refsetTypes - for picklist
        $scope.getRefsetTypes = function() {
          refsetService.getRefsetTypes().then(function(data) {
            $scope.metadata.refsetTypes = data.strings;
          })
        };

        // Get $scope.metadata.terminologies, also loads
        // versions for the first edition in the list
        $scope.getTerminologyEditions = function() {
          projectService.getTerminologyEditions().then(function(data) {
            $scope.metadata.terminologies = data.strings;
            // Look up all versions
            for (var i = 0; i < data.strings.length; i++) {
              $scope.getTerminologyVersions(data.strings[i]);
            }
          })

        };

        // Get $scope.metadata.versions
        $scope.getTerminologyVersions = function(terminology) {
          projectService.getTerminologyVersions(terminology).then(function(data) {
            $scope.metadata.versions[terminology] = [];
            for (var i = 0; i < data.translations.length; i++) {
              $scope.metadata.versions[terminology].push(data.translations[i].version);
              //.replace(/-/gi, ""));
            }
          })
        };

        // Get $scope.metadata.{import,export}Handlers
        $scope.getIOHandlers = function() {
          refsetService.getImportRefsetHandlers().then(function(data) {
            $scope.metadata.importHandlers = data.handlers;
          });
          refsetService.getExportRefsetHandlers().then(function(data) {
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
        $scope.getRefsetTypes();
        $scope.getTerminologyEditions();
        $scope.getIOHandlers();
        $scope.getWorkflowPaths();
      }

    ]);
