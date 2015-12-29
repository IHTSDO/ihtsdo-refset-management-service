// Concept Info directive
// e.g. <div concept-info data="data" paging="paging" ></div>
tsApp.directive('conceptInfo', [
  '$uibModal',
  'utilService',
  'projectService',
  'refsetService',
  'validationService',
  function($uibModal, utilService, projectService, refsetService, validationService) {
    console.debug('configure conceptInfo directive');
    return {
      restrict : 'A',
      scope : {
        data : '=',
        value : '='
      },
      templateUrl : 'app/component/conceptInfo/conceptInfo.html',
      controller : [
        '$scope',
        function($scope) {
          // Paging parameters
          $scope.pageSize = 10;
          $scope.paging = {};
          $scope.paging["children"] = {
            page : 1,
            filter : "",
            typeFilter : "",
            sortField : 'name',
            ascending : null
          }
          $scope.concept = null;
          $scope.orderedDescriptions = null;
          $scope.children = [];
          $scope.parents = [];
          $scope.error = null;

          // tracks member types by concept id
          $scope.disableMemberTypes = false;
          $scope.memberTypes = {};

          // When concept changes, redo paging
          $scope.$watch('data.concept', function() {
            // When used by translation table, data.concept is null until selected
            // Clear error
            console.debug("data.concept");
            $scope.error = null;
            if ($scope.data.concept) {
              $scope.getFullConcept($scope.data.concept);
              $scope.getConceptParents($scope.data.concept);
              $scope.getConceptChildren($scope.data.concept);
              $scope.memberTypes = {};
            }
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
              concept.version, ($scope.data.translation ? $scope.data.translation.id : null)).then(
            // Success
            function(data) {
              $scope.parents = data.concepts;
              $scope.getMemberTypes();
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
              concept.version, ($scope.data.translation ? $scope.data.translation.id : null), {})
              .then(
              // Success
              function(data) {
                $scope.children = data.concepts;
                $scope.children.totalCount = data.totalCount;
                $scope.getMemberTypes();
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
              concept.version, ($scope.data.translation ? $scope.data.translation.id : null)).then(
              // Success
              function(data) {
                $scope.concept = data;
                $scope.orderedDescriptions = [];
                if ($scope.concept && $scope.concept.descriptions.length > 0) {
                  $scope.orderedDescriptions = concept.descriptions
                    .sort($scope.sortByDescriptionType);
                }
                return [];
              },
              // Error
              function(data) {
                $scope.error = data;
                utilService.clearError();
              });

          };

          // function for sorting an array by (string) field and direction
          $scope.sortByDescriptionType = function(a, b) {
            // TODO: sort by user preferences languageDescripionTypes
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

          // Order by group.
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
              ($scope.data.translation ? $scope.data.translation.id : null), {
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

              $scope.getMemberTypes();

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
              ($scope.data.translation ? $scope.data.translation.id : null)).then(function(data) {

              // cycle over parents, and construct tree nodes
              for (var i = 0; i < data.concepts.length; i++) {
                tree.inner.push(data.concepts[i]);
              }

              $scope.getMemberTypes();

            });
          }

          // Function to find all concepts in the graph
          $scope.getAllConcepts = function() {
            var concepts = [ $scope.data.concept.terminologyId ];
            for (var i = 0; i < $scope.children.length; i++) {
              concepts = concepts.concat($scope.getAllConceptsHelper($scope.children[i]));
            }
            for (var i = 0; i < $scope.parents.length; i++) {
              concepts = concepts.concat($scope.getAllConceptsHelper($scope.parents[i]));
            }
            return concepts;
          }

          // Helper
          $scope.getAllConceptsHelper = function(tree) {
            var concepts = [ tree.terminologyId ];
            if (tree.inner) {
              for (var i = 0; i < tree.inner.length; i++) {
                concepts = concepts.concat($scope.getAllConceptsHelper(tree.inner[i]));
              }
            }
            return concepts;
          }

          // Gets $scope.memberTypes
          // Only operates if $scope.data.refset exists
          $scope.getMemberTypes = function() {
            // skip if refset not set
            if (!$scope.data.refset) {
              $scope.disableMemberTypes = true;
              return;
            }

            // only show for "ASSIGNED" refsets
            if ($scope.value != 'ASSIGNED') {
              $scope.disableMemberTypes = true;
              return;
            }

            $scope.disableMemberTypes = false;

            var concepts = $scope.getAllConcepts();
            var query = concepts[0];
            for (var i = 1; i < concepts.length; i++) {
              if (!$scope.memberTypes[concepts[i]]) {
                query += " OR ";
                query += concepts[i];
                // put a placeholder entry for the cases when it isn't a member of the refset
                $scope.memberTypes[concepts[i]] = {
                  conceptId : concepts[i].terminologyId
                };
              }
            }
            var pfs = {
              startIndex : -1
            };
            query = "(" + query + ")";
            refsetService.findRefsetMembersForQuery($scope.data.refset.id, query, pfs).then(
            // Success
            function(data) {
              for (var i = 0; i < data.members.length; i++) {
                $scope.memberTypes[data.members[i].conceptId] = data.members[i];
              }
            })
          }

          // Remove refset inclusion
          $scope.removeRefsetInclusion = function(refset, member) {
            if (!confirm("Are you sure you want to remove the inclusion (" + member.conceptName
              + ")?")) {
              return;
            }
            refsetService.removeRefsetMember(member.id).then(
            // Success 
            function() {
              refsetService.fireRefsetChanged(refset);
              $scope.memberTypes = {};
              $scope.getMemberTypes();
            });
          };

          // Remove refset exclusion and refreshes members
          $scope.removeRefsetExclusion = function(refset, member) {
            refsetService.removeRefsetExclusion(member.id).then(
            // Success
            function() {
              refsetService.fireRefsetChanged(refset);
              $scope.memberTypes = {};
              $scope.getMemberTypes();
            });
          };

          // Modals
          // Add modal
          $scope.openAddModal = function(lrefset, lmember) {
            console.debug("openAddModal ", lrefset, lmember);

            var modalInstance = $uibModal.open({
              templateUrl : 'app/component/conceptInfo/add.html',
              controller : AddModalCtrl,
              backdrop : 'static',
              resolve : {
                refset : function() {
                  return lrefset;
                },
                member : function() {
                  return lmember;
                }
              }
            });

            modalInstance.result.then(
            // Success
            function(data) {
              refsetService.fireRefsetChanged(data);
              $scope.memberTypes = {};
              $scope.getMemberTypes();
            });

          };

          // Add modal controller
          var AddModalCtrl = function($scope, $uibModalInstance, refset, member) {
            console.debug("Entered add modal control", refset, member);

            $scope.errors = [];

            $scope.refset = refset;
            $scope.member = member;
            $scope.selfAndDescendants = '<<';
            $scope.descendants = '<';
            $scope.includeClause = "";

            // Add button
            $scope.submitAdd = function(refset, concept, value) {
              var definitionClause = value.indexOf('<') != -1;
              // if intensional and clause defined, add clause and update refset
              if (refset.type == 'INTENSIONAL' && definitionClause) {
                var clause = {
                  value : value,
                  negated : false
                }
                refset.definitionClauses.push(clause);
                refsetService.updateRefset(refset).then(
                // Success - update refset
                function(data) {
                  $uibModalInstance.close(refset);
                },
                // Error - add refset
                function(data) {
                  $scope.errors[0] = data;
                  utilService.clearError();
                })
              }
              // if extensional and clause defined, call addRefsetMembersForExpression
              else if (refset.type == 'EXTENSIONAL' && definitionClause) {
                refsetService.addRefsetMembersForExpression(refset, value).then(
                // Success - update refset
                function(data) {
                  $uibModalInstance.close(refset);
                },
                // Error - add refset
                function(data) {
                  $scope.errors[0] = data;
                  utilService.clearError();
                })
              }
              // if intensional and clause undefined, call add inclusion
              else if (refset.type == 'INTENSIONAL' && !definitionClause) {
                $scope.addMember(concept, 'INCLUSION');
              }
              // if extensional and clause undefined, call add member
              else if (refset.type == 'EXTENSIONAL' && !definitionClause) {
                $scope.addMember(concept, 'MEMBER');
              }
            }

            // close the modal
            $scope.close = function() {
              $uibModalInstance.close();
            }

            // Add the member
            $scope.addMember = function(concept, memberType) {

              var member = {
                active : true,
                conceptId : concept.terminologyId,
                conceptName : concept.name,
                conceptActive : concept.active,
                memberType : memberType,
                terminology : refset.terminology,
                version : refset.version,
                moduleId : refset.moduleId,
              };
              member.refsetId = refset.id;

              // validate member before adding it
              validationService.validateMember(member, $scope.refset.projectId).then(
                // Success - validate refset
                function(data) {
                  $scope.validationResult = data;

                  // If there are errors, make them available and stop.
                  if (data.errors && data.errors.length > 0) {
                    $scope.errors = data.errors;
                    return;
                  } else {
                    $scope.errors = [];
                  }

                  // if $scope.warnings is empty, and data.warnings is not, show warnings and stop
                  if (data.warnings && data.warnings.length > 0
                    && $scope.warnings !== data.warnings) {
                    $scope.warnings = data.warnings;
                    return;
                  } else {
                    $scope.warnings = [];
                  }

                  // Handle regular member type
                  if (member.memberType == 'MEMBER') {

                    refsetService.addRefsetMember(member).then(
                    // Success
                    function(data) {
                      $uibModalInstance.close(refset);
                    },
                    // Error
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })
                  }

                  // Handle inclusion
                  if (member.memberType == 'INCLUSION') {
                    refsetService.addRefsetInclusion(member, false).then(
                    // Success
                    function(data) {
                      $uibModalInstance.close(refset);
                    },
                    // Error
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })
                  }
                },
                // Error - validate refset
                function(data) {
                  $scope.errors[0] = data;
                  utilService.clearError();
                })
            };
          };

          // Remove modal
          $scope.openRemoveModal = function(lrefset, lmember) {
            console.debug("openRemoveModal ", lrefset, lmember);

            var modalInstance = $uibModal.open({
              templateUrl : 'app/component/conceptInfo/remove.html',
              controller : RemoveModalCtrl,
              backdrop : 'static',
              resolve : {
                refset : function() {
                  return lrefset;
                },
                member : function() {
                  return lmember;
                }
              }
            });

            modalInstance.result.then(
            // Success
            function(data) {
              refsetService.fireRefsetChanged(data);
              $scope.memberTypes = {};
              $scope.getMemberTypes();
            });

          };

          // Remove modal controller
          var RemoveModalCtrl = function($scope, $uibModalInstance, refset, member) {
            console.debug("Entered remove modal control", refset, member);

            $scope.errors = [];

            $scope.refset = refset;
            $scope.member = member;
            $scope.selfAndDescendants = '<<';
            $scope.descendants = '<';
            $scope.removeClause = "";

            // Handles removing a member or clause
            $scope.submitRemove = function(refset, concept, value) {
              var definitionClause = value.indexOf('<') != -1;

              // if intensional and clause defined, add clause and update refset
              if (refset.type == 'INTENSIONAL' && definitionClause) {
                var clause = {
                  value : value,
                  negated : true
                }
                // This updates refset model
                refset.definitionClauses.push(clause);
                refsetService.updateRefset(refset).then(
                // Success - update refset
                function(data) {
                  $uibModalInstance.close(refset);
                },
                // Error - update refset
                function(data) {
                  $scope.errors[0] = data;
                  utilService.clearError();
                })
              }

              // if extensional and clause defined, call removeRefsetMembersForExpression
              else if (refset.type == 'EXTENSIONAL' && definitionClause) {
                refsetService.removeRefsetMembersForExpression(refset, value).then(
                // Success - add members for expression
                function(data) {
                  $uibModalInstance.close(refset);
                },
                // Error - add members for expression
                function(data) {
                  $scope.errors[0] = data;
                  utilService.clearError();
                })
              }

              // if intensional and clause undefined, call add exclusion
              else if (refset.type == 'INTENSIONAL' && !definitionClause) {
                refsetService.addRefsetExclusion(refset, member.conceptId, false).then(
                // Success - add exclusion
                function() {
                  $uibModalInstance.close(refset);
                },
                // Error - add exclusion
                function(data) {
                  $scope.errors[0] = data;
                  utilService.clearError();
                });
              }

              // if extensional and clause undefined, call remove member
              else if (refset.type == 'EXTENSIONAL' && !definitionClause) {

                refsetService.removeRefsetMember(member.id).then(
                // Success - remove member
                function(data) {
                  $uibModalInstance.close(refset);
                },
                // Error - remove member
                function(data) {
                  $scope.errors[0] = data;
                  utilService.clearError();
                })
              }
            }

            // close the modal
            $scope.close = function() {
              $uibModalInstance.close();
            }
          };
          // end
        } ]
    }

  } ]);
