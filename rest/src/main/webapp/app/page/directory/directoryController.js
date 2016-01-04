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

    // Test for empty accordion state
    $scope.isAccordionStateEmpty = function() {
      for (key in $scope.accordionState) {
        if ($scope.accordionState.hasOwnProperty(key))
          return false;
      }
      return true;
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
      $scope.user.userPreferences.lastDirectoryAccordion = data;
      securityService.updateUserPreferences($scope.user.userPreferences);
    };

    // Configure tab and accordion
    $scope.configureTab = function() {
      $scope.user.userPreferences.lastTab = '/directory';
      if ($scope.user.userPreferences.lastDirectoryAccordion) {
        $scope.accordionState[$scope.user.userPreferences.lastDirectoryAccordion] = true;
      } else {
        // default is published if nothing set
        $scope.accordionState['PUBLISHED'] = true;
      }
      securityService.updateUserPreferences($scope.user.userPreferences);
    }

    // Initialize
    $scope.getRefsetTypes();
    $scope.getProjects();
    $scope.getTerminologyEditions();
    $scope.getWorkflowPaths();
    // Handle users with user preferences
    if ($scope.user.userPreferences) {
      $scope.configureTab();
    }

    // end
  } ]);
