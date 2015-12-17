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

            // Order by preference, active, etc.
            $scope.getOrderedDescriptions = function(concept) {
              if (concept && concept.descriptions.length > 0) {
                return concept.descriptions.sort($scope.sortByDescriptionType);
              }
              return [];
            }

            // function for sorting an array by (string) field and direction
            $scope.sortByDescriptionType = function(a, b) {
              return a = $scope.getDescriptionType(a), b = $scope.getDescriptionType(b), (a > b)
                - (b > a);
            }

            // Return the description type name
            $scope.getDescriptionType = function(description) {
              for (var i = 0; i < $scope.data.descriptionTypes.length; i++) {
                var type = $scope.data.descriptionTypes[i];
                if (description.typeId == type.typeId && description.languages
                  && description.languages[0].acceptabilityId == type.acceptabilityId) {
                  return type.name;
                }
              }
              return "UNKNOWN";
            }

            // Order by group. TODO:
            $scope.getOrderedRelationships = function(concept) {
              if (concept && concept.relationships.length > 0) {
                return concept.relationships.sort($scope.sortByRelationshipGroup);
              }
              return [];
            }

            // function for sorting a relationship array
            $scope.sortByRelationshipGroup = function(a, b) {
              var t1 = a.relationshipGroup ? a.relationshipGroup : 9999;
              var t2 = b.relationshipGroup ? b.relationshipGroup : 9999;
              return (t1 > t2) - (t2 > t1);
            }

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
              return utilService.getPagedArray(array, paging, $scope.pageSize);
            }

            // 
            // Supporting trees
            // 

            /** Fake "enum" for clarity. Could use freeze, but meh */
            var TreeNodeExpansionState = {
              'Undefined' : -1,
              'Unloaded' : 0,
              'Loaded' : 3
            };

            // Helper function to determine what icon to show for a tree
            // node Case
            // 1: Has children, none loaded -> use right chevron Case 2: Has
            // children, incompletely loaded (below pagesize) -> expandable
            // (plus)
            // Case 3: Has children, completely loaded (or above pagesize)
            // -> not
            // expandable (right/down))
            $scope.getTreeNodeExpansionState = function(tree) {

              if (!tree) {
                return null;
              }

              if (!tree.inner) {
                tree.inner = [];
              }

              // case 1: no children loaded, but children exist
              if (!tree.leaf && tree.inner.length == 0) {
                return TreeNodeExpansionState.Unloaded;
              }
              // case 4: all children loaded
              else if (!tree.leaf && tree.inner.length > 0) {
                return TreeNodeExpansionState.Loaded;
              }

              else {
                return TreeNodeExpansionState.Undefined;
              }

            }

            // Determine the icon to show (plus, right, down, or blank)
            $scope.getTreeNodeIcon = function(tree, collapsed) {
              // if childCt is zero, return leaf
              if (tree.childCt == 0)
                return 'glyphicon-leaf';

              // otherwise, switch on expansion state
              switch ($scope.getTreeNodeExpansionState(tree)) {
              case TreeNodeExpansionState.Unloaded:
                return 'glyphicon-chevron-right';
              case TreeNodeExpansionState.Loaded:
                if (collapsed)
                  return 'glyphicon-chevron-right';
                else
                  return 'glyphicon-chevron-down';
              default:
                return 'glyphicon-question-sign';
              }
            }

            // Helper function to determine whether to toggle children
            // and/or retrieve children if necessary
            $scope.getChildTrees = function(tree, treeHandleScope) {

              switch ($scope.getTreeNodeExpansionState(tree)) {

              // if fully loaded or expandable from list, simply toggle
              case TreeNodeExpansionState.Loaded:
                treeHandleScope.toggle();
                return;

              default:
                $scope.getAndSetChildTrees(tree, 0);
              }
            }

            // Get a tree node's children and add to the parent
            $scope.getAndSetChildTrees = function(tree, startIndex) {
              if (!tree) {
                return;
              }

              // Get child trees
              projectService.getConceptChildren(tree.terminologyId, tree.terminology, tree.version,
                {
                  startIndex : -1
                }).then(function(data) {

                // cycle over children, and construct tree nodes
                for (var i = 0; i < data.concepts.length; i++) {

                  // check that child is not already present (don't override present data)
                  var childPresent = false;
                  for (var j = 0; j < tree.inner.length; j++) {
                    if (tree.inner[j].terminologyId == data.concepts[i].terminologyId) {
                      childPresent = true;
                      break;
                    }
                  }

                  // if not present, add
                  if (!childPresent) {
                    tree.inner.push(data.concepts[i]);
                  }
                }

              });

            }

            // Helper function to determine whether to toggle parents
            // and/or retrieve parents if necessary
            $scope.getParentTrees = function(tree, treeHandleScope) {

              switch ($scope.getTreeNodeExpansionState(tree)) {

              // if fully loaded or expandable from list, simply toggle
              case TreeNodeExpansionState.Loaded:
                treeHandleScope.toggle();
                return;

              default:
                $scope.getAndSetParentTrees(tree, 0);
              }
            }

            // Get a tree node's parents and add them
            $scope.getAndSetParentTrees = function(tree, startIndex) {
              if (!tree) {
                return;
              }

              // Get parent trees
              projectService.getConceptParents(tree.terminologyId, tree.terminology, tree.version,
                {
                  startIndex : -1
                }).then(function(data) {

                // cycle over parents, and construct tree nodes
                for (var i = 0; i < data.concepts.length; i++) {
                  tree.inner.push(data.concepts[i]);
                }

              });

            }
            // end

          } ]
      }
    } ]);
