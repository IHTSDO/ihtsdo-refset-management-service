// Directory controller
tsApp
  .controller(
    'DirectoryCtrl',
    [
      '$scope',
      '$http',
      '$modal',
      '$location',
      '$anchorScroll',
      'gpService',
      'utilService',
      'tabService',
      'securityService',
      'translationService',
      'refsetService',
      'directoryService',
      function($scope, $http, $modal, $location, $anchorScroll, gpService,
        utilService, tabService, securityService, translationService,
        refsetService, directoryService) {
        console.debug('configure DirectoryCtrl');

        // Handle resetting tabs on "back" button
        if (tabService.selectedTab.label != 'Directory') {
          tabService.setSelectedTabByLabel('Directory');
        }

        //
        // Scope Variables
        //

        // Scope variables initialized from services
        $scope.translation = translationService.getModel();
        $scope.user = securityService.getUser();
        $scope.component = directoryService.getModel();
        $scope.pageSizes = directoryService.getPageSizes();

        // Search parameters
        $scope.searchParams = directoryService.getSearchParams();
        $scope.searchResults = directoryService.getSearchResults();

      }
 

    ]);
