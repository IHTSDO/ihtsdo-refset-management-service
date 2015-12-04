// Directory controller
tsApp.controller('DirectoryCtrl', [
  '$scope',
  '$http',
  'tabService',
  'securityService',
  'projectService',
  'refsetService',
  'workflowService',
  function($scope, $http, tabService, securityService, projectService, refsetService,
    workflowService) {
    console.debug('configure DirectoryCtrl');

    // Handle resetting tabs on "back" button
    if (tabService.selectedTab.label != 'Directory') {
      tabService.setSelectedTabByLabel('Directory');
    }

    // Initialize
    projectService.prepareIconConfig();

    $scope.user = securityService.getUser();
    $scope.userProjectsInfo = projectService.getUserProjectsInfo();

    // Wrap in a json object so we can pass to the directive effectively
    $scope.projects = {
      data : [],
      totalCount : 0,
      assignedUsers : [],
      role : null
    };

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
      projectService.findProjectsAsList("", pfs).then(
      // Success
      function(data) {
        $scope.projects.data = data.projects;
        $scope.projects.totalCount = data.totalCount;
      })

    };

    // Get $scope.refsetTypes - for picklist
    $scope.getRefsetTypes = function() {
      refsetService.getRefsetTypes().then(function(data) {
        $scope.metadata.refsetTypes = data.strings;
      })
    };
    // Get $scope.metadata.terminologies, also loads
    // versions for the first edition in the list
    $scope.getTerminologyEditions = function() {
      projectService.getTerminologyEditions().then(
      // Success
      function(data) {
        $scope.metadata.terminologies = data.strings;
        // Look up all versions
        for (var i = 0; i < data.strings.length; i++) {
          $scope.getTerminologyVersions(data.strings[i]);
        }
      })

    };

    // Get $scope.metadata.versions
    $scope.getTerminologyVersions = function(terminology) {
      projectService.getTerminologyVersions(terminology).then(
      // Success
      function(data) {
        $scope.metadata.versions[terminology] = [];
        for (var i = 0; i < data.translations.length; i++) {
          $scope.metadata.versions[terminology].push(data.translations[i].version);
        }
      })
    };

    // Get $scope.metadata.workflowPaths
    $scope.getWorkflowPaths = function() {
      workflowService.getWorkflowPaths().then(function(data) {
        $scope.metadata.workflowPaths = data.strings;
      });
    }

    // Initialize
    $scope.getRefsetTypes();
    $scope.getProjects();
    $scope.getTerminologyEditions();
    $scope.getWorkflowPaths();

    // end
  }

]);
