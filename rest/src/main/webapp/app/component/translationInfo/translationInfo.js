// Translation Info directive
// e.g. <div translation-info translation="translation" form="long" ></div>
tsApp.directive('translationInfo', [ '$uibModal', 'utilService', function($uibModal, utilService) {
  console.debug('configure conceptInfo directive');
  return {
    restrict : 'A',
    scope : {
      translation : '=',
      form : '@'
    },
    templateUrl : 'app/component/translationInfo/translationInfo.html',
    controller : [ '$scope', function($scope) {

      // Convert date to a string
      $scope.toShortDate = function(lastModified) {
        return utilService.toShortDate(lastModified);

      };

      // end
    } ]
  }

} ]);
