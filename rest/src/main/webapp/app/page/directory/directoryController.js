// Directory controller
tsApp.controller('DirectoryCtrl', [
  '$scope',
  '$http',
  'tabService',
  'utilService',
  'securityService',
  'projectService',
  'refsetService',
  'workflowService',
  function($scope, $http, tabService, utilService, securityService, projectService, refsetService,
    workflowService) {
    console.debug('configure DirectoryCtrl');

    // Clear error
    utilService.clearError();

    // Handle resetting tabs on 'back' button
    if (tabService.selectedTab.label != 'Directory') {
      tabService.setSelectedTabByLabel('Directory');
    }

    // Initialize
    $scope.user = securityService.getUser();
    projectService.getUserHasAnyRole();
    $scope.userProjectsInfo = projectService.getUserProjectsInfo();
    projectService.prepareIconConfig();

    $scope.accordionState = {};

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
      projectService.findProjectsAsList('', pfs).then(
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
    };

    // Set the current accordion
    $scope.setAccordion = function(data) {
      utilService.clearError();

      // skip guest user
      if ($http.defaults.headers.common.Authorization == 'guest') {
        return;
      }

      if ($scope.user.userPreferences) {
        $scope.user.userPreferences.lastDirectoryAccordion = data;
        securityService.updateUserPreferences($scope.user.userPreferences);
      }
    };

    // Configure tab and accordion
    $scope.configureTab = function() {
      // skip guest user
      if ($http.defaults.headers.common.Authorization == 'guest') {
        $scope.accordionState['PUBLISHED'] = true;
        return;
      }
      $scope.user.userPreferences.lastTab = '/directory';
      if ($scope.user.userPreferences.lastDirectoryAccordion) {
        $scope.accordionState[$scope.user.userPreferences.lastDirectoryAccordion] = true;
      } else {
        // default is published if nothing set
        $scope.accordionState['PUBLISHED'] = true;
      }
      securityService.updateUserPreferences($scope.user.userPreferences);
    }

    // Get $scope.metadata.{import,export}Handlers
    $scope.getIOHandlers = function() {
      refsetService.getImportRefsetHandlers().then(function(data) {
        $scope.metadata.importHandlers = data.handlers;
      });
      refsetService.getExportRefsetHandlers().then(function(data) {
        $scope.metadata.exportHandlers = data.handlers;
      });
    }

    // Initialize
    $scope.getRefsetTypes();
    $scope.getProjects();
    $scope.getTerminologyEditions();
    $scope.getWorkflowPaths();
    $scope.getIOHandlers();
    // Handle users with user preferences
    if ($scope.user.userPreferences) {
      $scope.configureTab();
    }

    // end
  } ]);
