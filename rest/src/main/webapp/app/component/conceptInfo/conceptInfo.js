// Concept Info directive
// e.g. <div concept-info data="data" paging="paging" ></div>
tsApp.directive('conceptInfo', [
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
            utilService.setSortField(table,field,$scope.paging);
            // re-page the children
            if (table == 'children') {
              $scope.getPagedChildren();
            }

          };

          // Return up or down sort chars if sorted
          $scope.getSortIndicator = function(table, field) {
            return utilService.getSortIndicator(table,field,$scope.paging);
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
            var newArray = new Array();

            // if array blank or not an array, return blank list
            if (array == null || array == undefined || !Array.isArray(array)) {
              return newArray;
            }

            newArray = array;

            // apply sort if specified
            if (paging.sortField) {
              // if ascending specified, use that value, otherwise use false
              newArray.sort($scope.sort_by(paging.sortField, paging.ascending))
            }

            // apply filter
            if (paging.filter) {
              newArray = getArrayByFilter(newArray, paging.filter);
            }

            // get the page indices
            var fromIndex = (paging.page - 1) * $scope.pageSize;
            var toIndex = Math.min(fromIndex + $scope.pageSize, array.length);

            // slice the array
            var results = newArray.slice(fromIndex, toIndex);

            // add the total count before slicing
            results.totalCount = newArray.length;

            return results;
          }

          // function for sorting an array by (string) field and direction
          $scope.sort_by = function(field, reverse) {

            // key: function to return field value from object
            var key = function(x) {
              return x[field]
            };

            // convert reverse to integer (1 = ascending, -1 =
            // descending)
            reverse = !reverse ? 1 : -1;

            return function(a, b) {
              return a = key(a), b = key(b), reverse * ((a > b) - (b > a));
            }
          }

          // Get array by filter text matching terminologyId or name
          function getArrayByFilter(array, filter) {
            var newArray = [];

            for ( var object in array) {

              if (objectContainsFilterText(array[object], filter)) {
                newArray.push(array[object]);
              }
            }
            return newArray;
          }

          // Returns true if any field on object contains filter text
          function objectContainsFilterText(object, filter) {

            if (!filter || !object)
              return false;

            for ( var prop in object) {
              var value = object[prop];
              // check property for string, note this will cover child elements
              if (value && value.toString().toLowerCase().indexOf(filter.toLowerCase()) != -1) {
                return true;
              }
            }

            return false;
          }

          // end

        } ]
    }
  } ]);
