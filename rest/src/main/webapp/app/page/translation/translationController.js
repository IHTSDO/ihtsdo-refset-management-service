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
      'translationService',
      function($scope, $http, $modal, $location, $anchorScroll, gpService,
        utilService, tabService, securityService, refsetService,
        translationService) {
        console.debug('configure TranslationCtrl');

        // Handle resetting tabs on "back" button
        if (tabService.selectedTab.label != 'Translation') {
          tabService.setSelectedTabByLabel('Translation');
        }

        // eventually need to make some kind of call so that a failed call can redirect 
        // to login page

        // TODO
      }
 

    ]);
