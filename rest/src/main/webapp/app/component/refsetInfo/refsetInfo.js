// Refset Info directive
// e.g. <div refset-info refset="refset" form="long" ></div>
tsApp.directive('refsetInfo', [ '$uibModal', 'utilService', function($uibModal, utilService) {
  console.debug('configure conceptInfo directive');
  return {
    restrict : 'A',
    scope : {
      refset : '=',
      form : '@'
    },
    templateUrl : 'app/component/refsetInfo/refsetInfo.html',
    controller : [ '$scope', function($scope) {

      // Convert date to a string
      $scope.toShortDate = function(lastModified) {
        return utilService.toShortDate(lastModified);

      };

      // end
    } ]
  }

} ]);
