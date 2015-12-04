// Translation Table directive
// e.g. <div translation-table value="PUBLISHED"></div>
tsApp.directive('translationTable', [
  '$uibModal',
  '$rootScope',
  'utilService',
  'securityService',
  'projectService',
  'translationService',
  'refsetService',
  'releaseService',
  'workflowService',
  function($uibModal, $rootScope, utilService, securityService, projectService, translationService,
    refsetService, releaseService, workflowService) {
    console.debug('configure translationTable directive');
    return {
      restrict : 'A',
      scope : {
        // Legal "value" settings include
        // For directory tab: PUBLISHED, PREVIEW
        // For refset tab: EDITING, EDITING_ALL, RELEASE
        value : '@',
        projects : '=',
        metadata : '='
      },
      templateUrl : 'app/component/translationTable/translationTable.html',
      controller : [
        '$scope',
        function($scope) {

          // Variables
          $scope.user = securityService.getUser();
          $scope.selected = {
            concept : null,
            translation : null
          };
          $scope.translations = null;
          $scope.translationReleaseInfo = null;
          $scope.project = null;
          $scope.refsets = [];

          // Used for project admin to know what users are assigned to something.
          $scope.conceptIdToAuthorsMap = {};
          $scope.conceptIdToReviewersMap = {};

          // Paging variables
          $scope.pageSize = 10;
          $scope.paging = {};
          $scope.paging["translation"] = {
            page : 1,
            filter : "",
            sortField : 'name',
            ascending : null
          };
          $scope.paging["concept"] = {
            page : 1,
            filter : "",
            sortField : 'lastModified',
            ascending : null
          };
          $scope.paging["available"] = {
            page : 1,
            filter : "",
            sortField : 'lastModified',
            ascending : null
          };
          $scope.paging["assigned"] = {
            page : 1,
            filter : "",
            sortField : 'lastModified',
            ascending : null
          };

          $scope.ioImportHandlers = [];
          $scope.ioExportHandlers = [];

          // Translation Changed handler
          $scope.$on('refset:translationChanged', function(event, data) {
            console.debug('on refset:translationChanged', data);
            // If the translation is set, refresh translation list
            $scope.getTranslations();
          });

          // Concept Changed handler
          $scope.$on('refset:conceptChanged', function(event, data) {
            console.debug('on refset:conceptChanged', data, $scope.selected.translation);
            // If the translation is set, refresh available/assigned lists
            if ($scope.selected.translation) {
              $scope.selectTranslation($scope.selected.translation);
              $scope.getAvailableConcepts($scope.selected.translation);
              $scope.getAssignedConcepts($scope.selected.translation);
            }
          });

          // Project Changed Handler
          $scope.$on('refset:projectChanged', function(event, data) {
            console.debug('on refset:projectChanged', data);
            // Set project, refresh translation list
            $scope.setProject(data);
          });

          // Set $scope.project and reload
          // $scope.refsets
          $scope.setProject = function(project) {
            $scope.project = project;
            $scope.getRefsets();
            $scope.getTranslations();
          };

          // Get $scope.refsets
          $scope.getRefsets = function() {
            var pfs = {
              startIndex : -1,
              maxResults : 100,
              sortField : null,
              queryRestriction : null
            };
            // Get refsets for project
            refsetService.findRefsetsForQuery("projectId:" + $scope.project.id, pfs).then(
              function(data) {
                $scope.refsets = data.refsets;
              });
          }

          // Get $scope.translations
          $scope.getTranslations = function() {
            var pfs = {
              startIndex : ($scope.paging["translation"].page - 1) * $scope.pageSize,
              maxResults : $scope.pageSize,
              sortField : $scope.paging["translation"].sortField,
              ascending : $scope.paging["translation"].ascending == null ? true
                : $scope.paging["translation"].ascending,
              queryRestriction : null
            };

            if ($scope.value == 'PUBLISHED' || $scope.value == 'PREVIEW') {
              pfs.queryRestriction = 'workflowStatus:' + $scope.value;
              translationService.findTranslationsForQuery($scope.paging["translation"].filter, pfs)
                .then(function(data) {
                  $scope.translations = data.translations;
                  $scope.translations.totalCount = data.totalCount;
                })
            }

            if ($scope.value == 'EDITING'
              && ($scope.projects.role == 'AUTHOR' || $scope.projects.role == 'REVIEWER')) {
              workflowService.findNonReleaseProcessTranslations($scope.project.id, pfs).then(
                function(data) {
                  $scope.translations = data.translations;
                  $scope.translations.totalCount = data.totalCount;
                  // Refresh the translation if it is selected
                  if ($scope.selected.translation) {
                    $scope.selectTranslation($scope.selected.translation);
                  }
                })
            }

            if ($scope.value == 'EDITING_ALL') {
              workflowService.findNonReleaseProcessTranslations($scope.project.id, pfs).then(
                function(data) {
                  $scope.translations = data.translations;
                  $scope.translations.totalCount = data.totalCount;
                  // Refresh the translation if it is selected
                  if ($scope.selected.translation) {
                    $scope.selectTranslation($scope.selected.translation);
                  }
                })
            }

            if ($scope.value == 'RELEASE') {
              workflowService.findReleaseProcessTranslations($scope.project.id, pfs).then(
                function(data) {
                  $scope.translations = data.translations;
                  $scope.translations.totalCount = data.totalCount;
                })
            }

            // If $scope.selected.translation is in the list, select it, if not clear $scope.selected.translation
            var found = false;
            if ($scope.selected.translation) {
              for (var i = 0; i < $scope.translations.length; i++) {
                if ($scope.selected.translation.id == $scope.translations[i].id) {
                  found = true;
                  break;
                }
              }
            }
            if (found) {
              $scope.getConcepts($scope.selected.translation);
            } else {
              $scope.selected.translation = null;
            }
          };

          // Get $scope.selected.translation.concepts
          $scope.getConcepts = function(translation) {

            var pfs = {
              startIndex : ($scope.paging["concept"].page - 1) * $scope.pageSize,
              maxResults : $scope.pageSize,
              sortField : $scope.paging["concept"].sortField,
              ascending : $scope.paging["concept"].ascending == null ? true
                : $scope.paging["concept"].ascending,
              queryRestriction : null
            };

            translationService.findTranslationConceptsForQuery(translation.id,
              $scope.paging["concept"].filter, pfs).then(function(data) {
              translation.concepts = data.concepts;
              translation.concepts.totalCount = data.totalCount;
            })

          };

          // Get $scope.selected.translation.available
          $scope.getAvailableConcepts = function(translation) {
            if (!$scope.projects.role) {
              return;
            }
            var pfs = {
              startIndex : ($scope.paging["available"].page - 1) * $scope.pageSize,
              maxResults : $scope.pageSize,
              sortField : $scope.paging["available"].sortField,
              ascending : $scope.paging["available"].ascending == null ? true
                : $scope.paging["available"].ascending,
              queryRestriction : null
            };

            if ($scope.projects.role == 'AUTHOR') {
              workflowService.findAvailableEditingConcepts($scope.project.id, translation.id,
                $scope.user.userName, pfs).then(
              // Success
              function(data) {
                translation.available = data.concepts;
                translation.available.totalCount = data.totalCount;
              });
            } else if ($scope.projects.role == 'REVIEWER') {
              workflowService.findAvailableReviewConcepts($scope.project.id, translation.id,
                $scope.user.userName, pfs).then(
              // Success
              function(data) {
                translation.available = data.concepts;
                translation.available.totalCount = data.totalCount;
              });
            } else if ($scope.projects.role == 'ADMIN') {
              workflowService.findAllAvailableConcepts($scope.project.id, translation.id,
                $scope.user.userName, pfs).then(
              // Success
              function(data) {
                translation.available = data.concepts;
                translation.available.totalCount = data.totalCount;
              });

            } else {
              window.alert("Unexpected role attempting to get available concepts");
            }

          };

          // Get $scope.selected.translation.assigned
          $scope.getAssignedConcepts = function(translation) {
            if (!$scope.projects.role) {
              return;
            }

            var pfs = {
              startIndex : ($scope.paging["assigned"].page - 1) * $scope.pageSize,
              maxResults : $scope.pageSize,
              sortField : $scope.paging["assigned"].sortField,
              ascending : $scope.paging["assigned"].ascending == null ? true
                : $scope.paging["assigned"].ascending,
              queryRestriction : null
            };

            if ($scope.projects.role == 'AUTHOR') {
              workflowService.findAssignedEditingConcepts($scope.project.id, translation.id,
                $scope.user.userName, pfs).then(
              // Success
              function(data) {
                translation.assigned = data.concepts;
                translation.assigned.totalCount = data.totalCount;
              });
            } else if ($scope.projects.role == 'REVIEWER') {
              workflowService.findAssignedReviewConcepts($scope.project.id, translation.id,
                $scope.user.userName, pfs).then(
              // Success
              function(data) {
                translation.assigned = data.concepts;
                translation.assigned.totalCount = data.totalCount;
              });
            } else if ($scope.projects.role == 'ADMIN') {
              workflowService.findAllAssignedConcepts($scope.project.id, translation.id,
                $scope.user.userName, pfs).then(
              // Success
              function(data) {
                translation.assigned = data.concepts;
                translation.assigned.totalCount = data.totalCount;
              });

            } else {
              window.alert("Unexpected role attempting to get available concepts");
            }

          };
          // get translation release info
          $scope.getTranslationReleaseInfo = function(translation) {
            releaseService.getCurrentTranslationRelease(translation.id).then(function(data) {
              $scope.translationReleaseInfo = data;
            })
          };

          // export release artifact
          $scope.exportReleaseArtifact = function(artifact) {
            releaseService.exportReleaseArtifact(artifact);
          };

          // Convert date to a string
          $scope.toDate = function(lastModified) {
            return utilService.toDate(lastModified);

          };

          // Convert date to a string
          $scope.toShortDate = function(lastModified) {
            return utilService.toShortDate(lastModified);

          };

          // Table sorting mechanism
          $scope.setSortField = function(table, field, object) {
            utilService.setSortField(table, field, $scope.paging);

            // retrieve the correct table
            if (table === 'refset') {
              $scope.getRefsets();
            }
            if (table === 'member') {
              $scope.getMembers(object);
            }
          };

          // Return up or down sort chars if sorted
          $scope.getSortIndicator = function(table, field) {
            return utilService.getSortIndicator(table, field, $scope.paging);
          };

          // Selects a translation (setting $scope.selected.translation).
          // Looks up current release info and members.
          $scope.selectTranslation = function(translation) {
            $scope.selected.translation = translation;
            $scope.getTranslationReleaseInfo(translation);
            $scope.getConcepts(translation);
            $scope.getAvailableConcepts(translation);
            $scope.getAssignedConcepts(translation);
          };

          // Selects a concepts (setting $scope.concept)
          $scope.selectConcept = function(concept) {
            $scope.selected.concept = concept;
            // Look up details of concept
          };

          // Initialize if project setting isn't used
          if ($scope.value == 'PREVIEW' || $scope.value == 'PUBLISHED') {
            $scope.getTranslations();
          }

          // Remove a translation
          $scope.removeTranslation = function(translation) {
            // Confirm action
            if (!confirm("Are you sure you want to remove the translation (" + translation.name
              + ")?")) {
              return;
            }

            // TODO need to handle where concepts isn't loaded
            if (translation.concepts != null) {
              if (!confirm("The translation has concepts that will also be deleted.")) {
                return;
              }
            }
            translationService.removeTranslation(translation.id).then(
            // Success
            function(data) {
              $scope.selected.translation = null;
              translationService.fireTranslationChanged();
            });

          };

          // 
          // MODALS
          //

          // Add translation modal
          $scope.openAddTranslationModal = function() {
            console.debug("openAddTranslationModal ");

            var modalInstance = $uibModal.open({
              templateUrl : 'app/component/translationTable/editTranslation.html',
              controller : AddTranslationModalCtrl,
              backdrop : 'static',
              resolve : {
                metadata : function() {
                  return $scope.metadata;
                },
                refsets : function() {
                  return $scope.refsets;
                },
                project : function() {
                  return $scope.project;
                }
              }
            });

            modalInstance.result.then(
            // Success
            function(data) {
              translationService.fireTranslationChanged(data);
            });
          };

          // Add translation controller
          var AddTranslationModalCtrl = function($scope, $uibModalInstance, metadata, refsets,
            project) {

            console.debug("Entered add translation modal control", metadata);

            $scope.action = 'Add';
            $scope.errors = [];
            $scope.metadata = metadata;
            $scope.project = project;
            $scope.refsets = refsets;
            $scope.translation = {
              workflowPath : metadata.workflowPaths[0]
            };

            $scope.selectRefset = function(refset) {
              $scope.translation.moduleId = refset.moduleId;
              $scope.translation.terminology = refset.terminology;
              $scope.translation.version = refset.version;
              $scope.translation.terminologyId = refset.terminologyId;
              $scope.translation.refsetId = refset.id;
            };

            $scope.submitTranslation = function(translation) {
              if (!translation || !translation.name || !translation.description) {
                $scope.errors[0] = "The name and description fields cannot be blank. ";
                return;
              }

              translation.projectId = project.id;
              translationService.addTranslation(translation).then(
              // Success - add translation
              function(data) {
                $uibModalInstance.close(data);
              },
              // Error - add refset
              function(data) {
                $scope.errors[0] = data;
                utilService.clearError();
              })
            };

            $scope.cancel = function() {
              $uibModalInstance.dismiss('cancel');
            };

          };

          $scope.openEditTranslationModal = function(ltranslation) {
            console.debug("openEditTranslationModal ");

            var modalInstance = $uibModal.open({
              templateUrl : 'app/component/translationTable/editTranslation.html',
              controller : EditTranslationModalCtrl,
              backdrop : 'static',
              resolve : {
                metadata : function() {
                  return $scope.metadata;
                },
                refsets : function() {
                  return $scope.refsets;
                },
                project : function() {
                  return $scope.project;
                },
                translation : function() {
                  return ltranslation;
                }
              }
            });

            modalInstance.result.then(
            // Success
            function(data) {
              translationService.fireTranslationChanged(data);
            });
          };

          // Edit translation controller
          var EditTranslationModalCtrl = function($scope, $uibModalInstance, metadata, refsets,
            project, translation) {

            console.debug("Entered edit translation modal control", metadata, translation);

            $scope.action = 'Edit';
            $scope.errors = [];
            $scope.metadata = metadata;
            $scope.project = project;
            $scope.translation = translation;

            // find refset translation is attached to (by terminology id)
            console.debug("refset id", translation.refsetId);
            console.debug("refset tid", translation.terminologyId);
            for (var i = 0; i < refsets.length; i++) {
              console.debug("  refset = ", refsets[i]);
              if ($scope.translation.refsetId == refsets[i].id) {
                $scope.refset = refsets[i];
                break;
              }
            }

            $scope.submitTranslation = function(translation) {
              console.debug("Submitting edit translation", translation);

              if (!translation || !translation.name || !translation.description) {
                $scope.error = "The name, description, and terminology fields cannot be blank. ";
                return;
              }
              translationService.updateTranslation(translation).then(
              // Success - update translation
              function(data) {
                $uibModalInstance.close(data);
              },
              // Error - update translation
              function(data) {
                $scope.errors[0] = data;
                utilService.clearError();
              })
            };

            $scope.cancel = function() {
              $uibModalInstance.dismiss('cancel');
            };

          };

          // Assign concept modal
          $scope.openAssignConceptModal = function(ltranslation, lconcept, laction, luserName) {
            console.debug("openAssignConceptModal ", ltranslation, lconcept, laction, luserName);

            var modalInstance = $uibModal.open({
              templateUrl : 'app/component/translationTable/assignConcept.html',
              controller : AssignConceptModalCtrl,
              backdrop : 'static',
              resolve : {
                concept : function() {
                  return lconcept;
                },
                translation : function() {
                  return translation;
                },
                action : function() {
                  return laction;
                },
                currentUserName : function() {
                  return luserName;
                },
                assignedUsers : function() {
                  return $scope.projects.assignedUsers;
                },
                project : function() {
                  return $scope.project;
                }
              }

            });

            modalInstance.result.then(
            // Success
            function(data) {
              translationService.fireConceptChanged(data);
            });
          };

          // Assign concept controller
          var AssignConceptModalCtrl = function($scope, $uibModalInstance, concept, action,
            currentUserName, assignedUsers, project, $rootScope) {

            console.debug("Entered assign concept modal control", assignedUsers, project.id);

            $scope.concept = concept;
            $scope.project = project;
            $scope.assignedUserNames = [];
            $scope.selectedUserName = currentUserName;
            $scope.errors = [];

            // Prep userNames picklist
            for (var i = 0; i < assignedUsers.length; i++) {
              $scope.assignedUserNames.push(assignedUsers[i].userName);
            }
            $scope.assignedUserNames = $scope.assignedUserNames.sort();

            $scope.assignConcept = function(userName) {
              if (!userName) {
                $scope.errors[0] = "The user must be selected. ";
                return;
              }

              $scope.selectedUserName = userName;

              if (action == 'ASSIGN') {
                workflowService.performTranslationWorkflowAction($scope.project.id, translation.id,
                  userName, "ASSIGN", concept).then(
                // Success
                function(data) {
                  $uibModalInstance.close(concept);
                },
                // Error
                function(data) {
                  $scope.errors[0] = data;
                  utilService.clearError();
                })
              }
            };

            $scope.cancel = function() {
              $uibModalInstance.dismiss('cancel');
            };

          };

          // end

        } ]
    }
  } ]);
