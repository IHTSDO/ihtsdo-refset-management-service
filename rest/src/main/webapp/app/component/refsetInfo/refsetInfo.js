// Refset Info directive
// e.g. <div refset-info refset='refset' form='long' ></div>
tsApp.directive('refsetInfo', [ '$uibModal', 'utilService', 'projectService',
  function($uibModal, utilService, projectService) {
    console.debug('configure refsetInfo directive');
    return {
      restrict : 'A',
      scope : {
        refset : '=',
        metadata : '=',
        form : '@'
      },
      templateUrl : 'app/component/refsetInfo/refsetInfo.html',
      controller : [ '$scope', function($scope) {

        $scope.metadata = {
          terminologyNames : {}
        };

        // Convert date to a string
        $scope.toShortDate = function(lastModified) {
          return utilService.toShortDate(lastModified);

        };

        // Return the name for a terminology
        $scope.getTerminologyName = function(terminology) {
          return $scope.metadata.terminologyNames[terminology];
        };

        // end
      } ]
    };

  } ]);
