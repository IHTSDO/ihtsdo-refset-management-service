// Translation controller
tsApp
  .controller(
    'TranslationCtrl',
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
      'refsetService',
      'directoryService',
      'translationService',
      function($scope, $http, $modal, $location, $anchorScroll, gpService,
        utilService, tabService, securityService, refsetService,
        directoryService, translationService) {
        console.debug('configure TranslationCtrl');

        // Handle resetting tabs on "back" button
        if (tabService.selectedTab.label != 'Translation') {
          tabService.setSelectedTabByLabel('Translation');
        }

        //
        // Scope Variables
        //

        // Scope variables initialized from services
        $scope.translation = translationService.getModel();
        $scope.user = securityService.getUser();
        $scope.component = translationService.getModel();
        $scope.pageSizes = translationService.getPageSizes();

        // Search parameters
        $scope.searchParams = translationService.getSearchParams();
        $scope.searchResults = translationService.getSearchResults();

      }
 

    ]);
