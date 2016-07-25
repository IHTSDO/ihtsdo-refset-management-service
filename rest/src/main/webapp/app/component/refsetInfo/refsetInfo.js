// Refset Info directive
// e.g. <div refset-info refset='refset' form='long' ></div>
tsApp
  .directive(
    'refsetInfo',
    [
      '$uibModal',
      'utilService', 'projectService',
      function($uibModal, utilService, projectService) {
        console.debug('configure refsetInfo directive');
        return {
          restrict : 'A',
          scope : {
            refset : '=',
            form : '@'
          },
          templateUrl : 'app/component/refsetInfo/refsetInfo.html',
          controller : [
            '$scope',
            function($scope) {

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

              // Get $scope.metadata.terminologies, also loads
              // versions for the first edition in the list
              $scope.getTerminologyEditions = function() {
                projectService
                  .getTerminologyEditions()
                  .then(
                    function(data) {
                      $scope.metadata.terminologies = data.terminologies;
                      // Look up all versions
                      for (var i = 0; i < data.terminologies.length; i++) {
                        $scope.metadata.terminologyNames[data.terminologies[i].terminology] = data.terminologies[i].name;
                      }
                    });

              };

              // Initialize
              $scope.getTerminologyEditions();

              // end
            } ]
        };

      } ]);
