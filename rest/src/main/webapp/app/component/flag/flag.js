// Translation Table directive
// e.g. <div flag-icon obj="refset"></div>
tsApp.directive('flagIcon', [ 'projectService', function(projectService) {
  console.debug('configure flagIcon directive');
  return {
    restrict : 'A',
    scope : {
      obj : '='
    },
    templateUrl : 'app/component/flag/flag.html',
    controller : [ '$scope', function($scope) {

      // Tests that the key has an icon
      $scope.hasIcon = function(key) {
        return projectService.hasIcon(key);
      }

      // Returns the icon path for the key (moduleId or namespaceId)
      $scope.getIcon = function(key) {
        return projectService.getIcon(key);
      }

    } ]
  }
} ]);
