// Concept Info directive
// e.g. <div concept-info data="data" paging="paging" ></div>
tsApp.directive('conceptInfo',
  [
    'utilService',
    'projectService',
    function(utilService, projectService) {
      console.debug('configure conceptInfo directive');
      return {
        restrict : 'A',
        scope : {
          data : '='
        },
        templateUrl : 'app/component/conceptInfo/conceptInfo.html',
        controller : [
          '$scope',
          function($scope) {

            // Paging parameters
            $scope.pageSize = 5;
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
            $scope.$watch('data.concept', function() {
              // When used by translation table, data.concept is null until selected
              // Clear error
              $scope.error = null;
              if ($scope.data.concept) {
                $scope.getConceptParents($scope.data.concept);
                $scope.getFullConcept($scope.data.concept);
                $scope.getConceptChildren($scope.data.concept);
              }
            });

            // When children are reloaded, redo paging
            $scope.$watch('children', function() {
              $scope.getPagedChildren();
            });

            // Clear error
            $scope.clearError = function() {
              $scope.error = null;
            }

            // Force a change to $scope.data.concept to reload the data
            $scope.getConceptById = function(terminologyId) {
              var copy = JSON.parse(JSON.stringify($scope.data.concept));
              copy.terminologyId = terminologyId
              $scope.data.concept = copy;
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

            // Table sorting mechanism
            $scope.setSortField = function(table, field) {
              utilService.setSortField(table, field, $scope.paging);
              // re-page the children
              if (table == 'children') {
                $scope.getPagedChildren();
              }

            };

            // Return up or down sort chars if sorted
            $scope.getSortIndicator = function(table, field) {
              return utilService.getSortIndicator(table, field, $scope.paging);
            };

            // Get paged children (assume all are loaded)
            $scope.getPagedChildren = function() {
              $scope.pagedChildren = $scope.getPagedArray($scope.children,
                $scope.paging['children']);
            }

            // Helper to get a paged array with show/hide flags
            // and filtered by query string
            $scope.getPagedArray = function(array, paging) {
              console.debug("getPagedArray");
              return utilService.getPagedArray(array, paging, $scope.pageSize);
            }

            // end

          } ]
      }
    } ]);
