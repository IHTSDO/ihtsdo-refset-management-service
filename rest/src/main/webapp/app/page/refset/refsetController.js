// Refset controller
tsApp
  .controller(
    'RefsetCtrl',
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
      'directoryService',
      'refsetService',
      function($scope, $http, $modal, $location, $anchorScroll, gpService,
        utilService, tabService, securityService, translationService,
        directoryService, refsetService) {
        console.debug('configure RefsetCtrl');

        // Handle resetting tabs on "back" button
        if (tabService.selectedTab.label != 'Refset') {
          tabService.setSelectedTabByLabel('Refset');
        }

        //
        // Scope Variables
        //

        // Scope variables initialized from services
        $scope.translation = translationService.getModel();
        $scope.user = securityService.getUser();
        $scope.component = refsetService.getModel();
        $scope.pageSizes = refsetService.getPageSizes();

        // Search parameters
        $scope.searchParams = refsetService.getSearchParams();
        $scope.searchResults = refsetService.getSearchResults();

      }
 

    ]);
