// Directory controller
tsApp.controller('DirectoryCtrl', [ '$scope', '$http', 'tabService','projectService',
  function($scope, $http, tabService, projectService) {
    console.debug('configure DirectoryCtrl');

    // Handle resetting tabs on "back" button
    if (tabService.selectedTab.label != 'Directory') {
      tabService.setSelectedTabByLabel('Directory');
    }
    
    // Initialize
    projectService.prepareIconConfig();

  }

]);
