// Member Info directive
// e.g. <div member-info data="data" ></div>
tsApp.directive('memberInfo', [
  'utilService',
  'projectService',
  function(utilService, projectService) {
    console.debug('configure memberInfo directive');
    return {
      restrict : 'A',
      scope : {
        data : '='
      },
      templateUrl : 'app/component/memberInfo/memberInfo.html',
      controller : [
        '$scope',
        function($scope) {

          // Paging parameters
          $scope.pageSize = 10;
          $scope.pagedChildren = [];
          $scope.paging = {};
          $scope.paging["children"] = {
            page : 1,
            filter : "",
            typeFilter : "",
            sortField : 'name',
            ascending : null
          }
          $scope.concept = null;
          $scope.children = [];
          $scope.parents = [];
          $scope.parents = [];
          $scope.error = null;

          // When concept changes, redo paging
          $scope.$watch('data.member', function() {
            // When used by translation table, data.concept is null until selected
            // Clear error
            $scope.error = null;
            if ($scope.data.member) {
              console.debug("DO STUFF HERE");
            }
          });

          // When children are reloaded, redo paging
          $scope.$watch('children', function() {
            // TBD
          });

          // Clear error
          $scope.clearError = function() {
            $scope.error = null;
          }

          // get concept parents
          $scope.getConceptParents = function(concept) {
            console.debug("Getting concept parents", concept);
            if (!concept) {
              return;
            }
            projectService.getConceptParents(concept.terminologyId, concept.terminology,
              concept.version).then(
            // Success
            function(data) {
              $scope.parents = data.concepts;
            },
            // Error 
            function(data) {
              $scope.error = data;
              utilService.clearError();
            })

          };

          // get concept children
          $scope.getConceptChildren = function(concept) {
            console.debug("Getting concept children", concept);

            // No PFS, get all children - term server doesn't handle paging of children
            projectService.getConceptChildren(concept.terminologyId, concept.terminology,
              concept.version, {}).then(
            // Success
            function(data) {
              $scope.children = data.concepts;
              $scope.children.totalCount = data.totalCount;
            },
            // Error 
            function(data) {
              $scope.error = data;
              utilService.clearError();
            })

          };

          // get concept with descriptions
          $scope.getFullConcept = function(concept) {
            console.debug("Getting concept with descriptions", concept);
            projectService.getFullConcept(concept.terminologyId, concept.terminology,
              concept.version).then(
            // Success
            function(data) {
              $scope.concept = data;
            },
            // Error
            function(data) {
              $scope.error = data;
              utilService.clearError();
            });

          };

          // end

        } ]
    }
  } ]);
