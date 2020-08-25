// Translation Info directive
// e.g. <div translation-info translation='translation' form='long' ></div>
tsApp.directive('translationInfo', [ '$uibModal', 'utilService', 'projectService','translationService',
  function($uibModal, utilService, projectService, translationService) {
    console.debug('configure translationInfo directive');
    return {
      restrict : 'A',
      scope : {
        translation : '=',
        metadata : '=',
        form : '@'
      },
      templateUrl : 'app/component/translationInfo/translationInfo.html',
      controller : [ '$scope', function($scope) {

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

// Language list directive
// e.g. <div languages></div>
tsApp.directive('languages', [ 'utilService','translationService', function(utilService, translationService) {
  console.debug('configure langauges directive');
  return {
    restrict : 'A',
    scope : {
      translation : '='
    },
    templateUrl : 'app/component/translationInfo/languages.html',
    controller : [ '$scope', function($scope) {

     $scope.languages = [];
      
     translationService.getLanguageRefsetDialectInfo('ALL').then(
        // Success
        function(data) {
          $scope.languages = {};
          
          for (var i = 0; i < data.keyValuePairs.length; i++) {
            $scope.languages[i] = {
              value : data.keyValuePairs[i].key,
              name : data.keyValuePairs[i].value.substring(0, data.keyValuePairs[i].value.indexOf('|'))
            }
          }
 
        }); 
      
      // end
    } ]
  };

} ]);

 

