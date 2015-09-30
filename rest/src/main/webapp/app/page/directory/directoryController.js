// Directory controller
tsApp.controller('DirectoryCtrl', [
  '$scope',
  '$http',
  '$modal',
  '$location',
  '$anchorScroll',
  'gpService',
  'utilService',
  'tabService',
  'securityService',
  'refsetService',
  function($scope, $http, $modal, $location, $anchorScroll, gpService,
    utilService, tabService, securityService, refsetService) {
    console.debug('configure DirectoryCtrl');

    // Handle resetting tabs on "back" button
    if (tabService.selectedTab.label != 'Directory') {
      tabService.setSelectedTabByLabel('Directory');
    }

    // eventually need to make some kind of call so that a failed call can redirect 
    // to login page

  }

]);
