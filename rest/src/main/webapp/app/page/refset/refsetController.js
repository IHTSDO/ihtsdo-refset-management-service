// Refset controller
tsApp.controller('RefsetCtrl', [
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
    console.debug('configure RefsetCtrl');

    // Handle resetting tabs on "back" button
    if (tabService.selectedTab.label != 'Refset') {
      tabService.setSelectedTabByLabel('Refset');
    }
    
    // eventually need to make some kind of call so that a failed call can redirect 
    // to login page

    // TODO
  }

]);
