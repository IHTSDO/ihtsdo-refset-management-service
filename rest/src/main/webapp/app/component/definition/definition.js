// Definition directive
// e.g. <div definition clause="clause"  ></div>
tsApp.directive('definition',
  [
    'utilService',
    'projectService',
    function(utilService, projectService) {
      console.debug('configure definition directive');
      return {
        restrict : 'A',
        scope : {
          clause : '='
        },
        templateUrl : 'app/component/definition/definition.html',
        controller : [
          '$scope',
          function($scope) {

            

            // end

          } ]
      }
    } ]);
