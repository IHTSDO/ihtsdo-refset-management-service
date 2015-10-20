// Directory controller
tsApp.controller('DirectoryCtrl', [ '$scope', '$http', 'tabService',
  function($scope, $http, tabService) {
    console.debug('configure DirectoryCtrl');

    // Handle resetting tabs on "back" button
    if (tabService.selectedTab.label != 'Directory') {
      tabService.setSelectedTabByLabel('Directory');
    }

  }

]);
