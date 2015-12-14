// Translation Table directive
// e.g. <div translation-table value="PUBLISHED"></div>
tsApp
  .directive(
    'translationTable',
    [
      '$uibModal',
      '$rootScope',
      '$sce',
      'utilService',
      'securityService',
      'projectService',
      'translationService',
      'refsetService',
      'releaseService',
      'workflowService',
      function($uibModal, $rootScope, $sce, utilService, securityService, projectService,
        translationService, refsetService, releaseService, workflowService) {
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
              $scope.showLatest = true;

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
                // This also reselects the translation
                $scope.getTranslations();
              });

              // Concept Changed handler
              $scope.$on('refset:conceptChanged', function(event, data) {
                console.debug('on refset:conceptChanged', data, $scope.selected.translation);
                // If the translation is set, refresh available/assigned lists
                if ($scope.selected.translation) {
                  $scope.selectTranslation($scope.selected.translation);
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
                  pfs.latestOnly = $scope.showLatest;  
                  translationService.findTranslationsForQuery($scope.paging["translation"].filter,
                    pfs).then(function(data) {
                    $scope.translations = data.translations;
                    $scope.translations.totalCount = data.totalCount;
                    $scope.reselect();
                  })
                }

                if ($scope.value == 'EDITING'
                  && ($scope.projects.role == 'AUTHOR' || $scope.projects.role == 'REVIEWER')) {
                  workflowService.findNonReleaseProcessTranslations($scope.project.id, pfs).then(
                    function(data) {
                      $scope.translations = data.translations;
                      $scope.translations.totalCount = data.totalCount;
                      $scope.reselect();
                    })
                }

                if ($scope.value == 'EDITING_ALL') {
                  workflowService.findNonReleaseProcessTranslations($scope.project.id, pfs).then(
                    function(data) {
                      $scope.translations = data.translations;
                      $scope.translations.totalCount = data.totalCount;
                      $scope.reselect();
                    })
                }

                if ($scope.value == 'RELEASE') {
                  pfs.queryRestriction = "(workflowStatus:READY_FOR_PUBLICATION OR workflowStatus:PREVIEW  OR workflowStatus:PUBLISHED)";
                  pfs.latestOnly = $scope.showLatest;  
                  translationService.findTranslationsForQuery($scope.project.id, pfs).then(
                    function(data) {
                      $scope.translations = data.translations;
                      $scope.translations.totalCount = data.totalCount;
                      $scope.reselect();
                    })
                }

              };

              // Reselect selected translation to refresh it
              $scope.reselect = function() {
                // Bail if no selection
                if (!$scope.selected.translation) {
                  return;
                }
                // If $scope.selected.translation is in the list, select it, if not clear $scope.selected.translation
                var found = false;
                if ($scope.selected.translation) {
                  for (var i = 0; i < $scope.translations.length; i++) {
                    if ($scope.selected.translation.id == $scope.translations[i].id) {
                      $scope.selectTranslation($scope.translations[i]);
                      found = true;
                      break;
                    }
                  }
                }
                if (!found) {
                  $scope.selected.translation = null;
                }
              }
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

                // For editing pane, restrict to READY_FOR_PUBLICATION only
                if ($scope.value == 'EDITING') {
                  pfs.queryRestriction = "workflowStatus:READY_FOR_PUBLICATION";
                }
                if ($scope.value == 'RELEASE') {
                  // may not need a restriction here
                }

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
                    translation.assigned = data.records;
                    translation.assigned.totalCount = data.totalCount;
                  });
                } else if ($scope.projects.role == 'REVIEWER') {
                  workflowService.findAssignedReviewConcepts($scope.project.id, translation.id,
                    $scope.user.userName, pfs).then(
                  // Success
                  function(data) {
                    translation.assigned = data.records;
                    translation.assigned.totalCount = data.totalCount;
                  });
                } else if ($scope.projects.role == 'ADMIN') {
                  workflowService.findAllAssignedConcepts($scope.project.id, translation.id,
                    $scope.user.userName, pfs).then(
                  // Success
                  function(data) {
                    translation.assigned = data.records;
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
                if (table === 'translation') {
                  $scope.getTranslations();
                }
                if (table === 'concept') {
                  $scope.getConcepts(object);
                }
                if (table === 'available') {
                  $scope.getAvailableConcepts(object);
                }
                if (table === 'assigned') {
                  $scope.getAssignedConcepts(object);
                }
              };

              // Return up or down sort chars if sorted
              $scope.getSortIndicator = function(table, field) {
                return utilService.getSortIndicator(table, field, $scope.paging);
              };

              // Selects a translation (setting $scope.selected.translation).
              // Looks up current release info and concepts.
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

              // Unassign the specified concept
              $scope.unassign = function(concept) {
                // Confirm action
                if (!confirm("Are you sure you want to unassign this concept ("
                  + concept.terminologyId + ")")) {
                  return;
                }
                if (!concept) {
                  return;
                }

                workflowService.performTranslationWorkflowAction($scope.project.id,
                  $scope.selected.translation.id, $scope.user.userName, $scope.projects.role,
                  "UNASSIGN", concept).then(
                // Success
                function(data) {
                  translationService.fireTranslationChanged($scope.selected.translation);
                });

              }

              // Unassign all concepts assigned to this user
              $scope.unassignAll = function() {
                // Confirm action
                if (!confirm("Are you sure you want to unassign all concepts?")) {
                  return;
                }

                // load all concepts assigned to the user
                var pfs = {
                  startIndex : -1,
                  maxResults : 10,
                  sortField : null,
                  ascending : null,
                  queryRestriction : null
                };

                workflowService.findAssignedEditingConcepts($scope.project.id,
                  $scope.selected.translation.id, $scope.user.userName, pfs).then(
                  // Success
                  function(data) {

                    // Extract concepts from records
                    var list = [];
                    for (var i = 0; i < data.records.length; i++) {
                      list.push(data.records[i].concept);
                    }

                    // Make parameter
                    var conceptList = {
                      concepts : list
                    };

                    // Unassign all concepts
                    workflowService.performBatchTranslationWorkflowAction($scope.project.id,
                      $scope.selected.translation.id, $scope.user.userName, $scope.projects.role,
                      "UNASSIGN", conceptList).then(
                    // Success
                    function(data) {
                      translationService.fireTranslationChanged($scope.selected.translation);
                    }
                    // Error is already handled by service
                    );

                  }
                // Error is already handled by service
                );

              }

              // Performs a workflow action
              $scope.performWorkflowAction = function(concept, action) {

                workflowService.performTranslationWorkflowAction($scope.project.id,
                  $scope.selected.translation.id, $scope.user.userName, $scope.projects.role,
                  action, concept).then(
                // Success
                function(data) {
                  translationService.fireConceptChanged(concept);
                })
              };

              // Get the most recent note for display
              $scope.getLatestNote = function(translation) {
                if (translation && translation.notes && translation.notes.length > 0) {
                  return $sce.trustAsHtml(translation.notes
                    .sort(utilService.sort_by('lastModified', -1))[0].value);
                }
                return $sce.trustAsHtml("");
              }

              // Save user preferences // TODO: should use security service
              $scope.saveUserPreferences = function() {
                console.debug("save user prefs", $scope.user.userPreferences);
                window.alert("need to implement save user prefs");
                // Saves $scope.user.userPreferences, probably just saves the user
              }

              // Clear spelling dictionary
              $scope.clearSpellingDictionary = function(translation) {
                translationService.clearSpellingDictionary(translation.id).then(function(data) {
                  translationService.fireTranslationChanged(translation);
                });

              }

              // Clear phrase memory 
              $scope.clearPhraseMemory = function(translation) {
                translationService.clearPhraseMemory(translation.id).then(function(data) {
                  translationService.fireConceptChanged(data);
                });

              }

              // Initialize if project setting isn't used
              if ($scope.value == 'PREVIEW' || $scope.value == 'PUBLISHED') {
                $scope.getTranslations();
              }

              // 
              // MODALS
              //

              // Notes modal
              $scope.openNotesModal = function(lobject, ltype) {
                console.debug("openNotesModal ", lobject, ltype);

                var modalInstance = $uibModal.open({
                  // Reuse refset URL - TODO: should make this a reusable component
                  templateUrl : 'app/component/refsetTable/notes.html',
                  controller : NotesModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    object : function() {
                      return lobject;
                    },
                    type : function() {
                      return ltype;
                    },
                    tinymceOptions : function() {
                      return utilService.getTinymceOptions();
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.selectTranslation($scope.selected.translation);
                });

              };

              // Notes controller
              var NotesModalCtrl = function($scope, $uibModalInstance, $sce, object, type,
                tinymceOptions) {
                console.debug("Entered notes modal control", object, type);

                $scope.errors = [];

                $scope.object = object;
                $scope.type = type;
                $scope.tinymceOptions = tinymceOptions;
                $scope.newNote = null;

                // Paging parameters
                $scope.pageSize = 5;
                $scope.pagedNotes = [];
                $scope.paging = {};
                $scope.paging["notes"] = {
                  page : 1,
                  filter : "",
                  typeFilter : "",
                  sortField : "lastModified",
                  ascending : true
                }

                // Get paged notes (assume all are loaded)
                $scope.getPagedNotes = function() {
                  $scope.pagedNotes = utilService.getPagedArray($scope.object.notes,
                    $scope.paging['notes'], $scope.pageSize);
                }

                $scope.getNoteValue = function(note) {
                  return $sce.trustAsHtml(note.value);
                }

                // remove note
                $scope.removeNote = function(object, note) {
                  console.debug("remove note", object.id, note.value);
                  if ($scope.type == 'Translation') {
                    translationService.removeTranslationNote(object.id, note.id).then(
                    // Success - add refset
                    function(data) {
                      $scope.newNote = null;
                      translationService.getTranslation(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add refset
                      function(data) {
                        $scope.errors[0] = data;
                        utilService.clearError();
                      })
                    },
                    // Error - add refset
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })
                  } else if ($scope.type == 'Concept') {
                    translationService.removeTranslationConceptNote(
                      $scope.selected.translationn.id, object.refsetId, note.id).then(
                    // Success - add refset
                    function(data) {
                      $scope.newNote = null;
                      translationService.getConcept(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add refset
                      function(data) {
                        $scope.errors[0] = data;
                        utilService.clearError();
                      })
                    },
                    // Error - add refset
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })
                  }
                }

                $scope.submitNote = function(object, text) {
                  console.debug("submit note", object.id, text);

                  if ($scope.type == 'Translation') {
                    translationService.addTranslationNote(object.id, text).then(
                    // Success - add translation
                    function(data) {
                      $scope.newNote = null;
                      translationService.getTranslation(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add translation
                      function(data) {
                        $scope.errors[0] = data;
                        utilService.clearError();
                      })
                    },
                    // Error - add translation
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })
                  } else if ($scope.type == 'Concept') {
                    translationService.addTranslationConceptNote(object.translationId, object.id,
                      text).then(
                    // Success - add translation
                    function(data) {
                      $scope.newNote = null;

                      translationService.getConcept(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add translation
                      function(data) {
                        $scope.errors[0] = data;
                        utilService.clearError();
                      })
                    },
                    // Error - add translation
                    function(data) {
                      $scope.errors[0] = data;
                      utilService.clearError();
                    })
                  }
                };

                // Convert date to a string
                $scope.toDate = function(lastModified) {
                  return utilService.toDate(lastModified);
                };

                // close the modal
                $scope.cancel = function() {
                  $uibModalInstance.close();
                }

                // initialize modal
                $scope.getPagedNotes();
              };

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
                $scope.refsets = refsets;
                $scope.project = project;
                $scope.translation = translation;

                // find refset translation is attached to (by terminology id)
                for (var i = 0; i < refsets.length; i++) {
                  if ($scope.translation.refsetId == refsets[i].id) {
                    $scope.refset = refsets[i];
                    break;
                  }
                }

                $scope.submitTranslation = function(translation) {
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
              $scope.openAssignConceptModal = function(ltranslation, lconcept, luserName) {
                console.debug("openAssignConceptModal ", ltranslation, lconcept, luserName);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/translationTable/assignConcept.html',
                  controller : AssignConceptModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    concept : function() {
                      return lconcept;
                    },
                    translation : function() {
                      return ltranslation;
                    },
                    currentUserName : function() {
                      return luserName;
                    },
                    assignedUsers : function() {
                      return $scope.projects.assignedUsers;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    role : function() {
                      return $scope.projects.role;
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
              var AssignConceptModalCtrl = function($scope, $uibModalInstance, $sce, concept,
                translation, currentUserName, assignedUsers, project, role) {

                console.debug("Entered assign concept modal control", assignedUsers, project.id);

                $scope.note;
                $scope.concept = concept;
                $scope.project = project;
                $scope.role = role;
                $scope.assignedUserNames = [];
                $scope.selectedUserName = currentUserName;
                $scope.errors = [];

                // Prep userNames picklist
                for (var i = 0; i < assignedUsers.length; i++) {
                  $scope.assignedUserNames.push(assignedUsers[i].userName);
                }
                $scope.assignedUserNames = $scope.assignedUserNames.sort();

                // Handle assigning a concept
                $scope.assignConcept = function(userName) {
                  if (!userName) {
                    $scope.errors[0] = "The user must be selected. ";
                    return;
                  }

                  $scope.selectedUserName = userName;

                  workflowService.performTranslationWorkflowAction($scope.project.id,
                    translation.id, userName, $scope.role, "ASSIGN", $scope.concept).then(
                  // Success
                  function(data) {
                    // close modal
                    $uibModalInstance.close(translation);
                  },
                  // Error
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
                  })

                };

                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Assign batch concept modal
              $scope.openBatchAssignConceptModal = function(ltranslation, luserName) {
                console.debug("openBatchAssignConceptModal ", ltranslation, luserName);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/translationTable/assignBatch.html',
                  controller : BatchAssignConceptModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    translation : function() {
                      return ltranslation;
                    },
                    currentUserName : function() {
                      return luserName;
                    },
                    assignedUsers : function() {
                      return $scope.projects.assignedUsers;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    role : function() {
                      return $scope.projects.role;
                    },
                    paging : function() {
                      return $scope.paging
                    }
                  }

                });

                modalInstance.result.then(
                // Success
                function(data) {
                  translationService.fireConceptChanged(data);
                });
              };

              // Batch assign concept controller
              var BatchAssignConceptModalCtrl = function($scope, $uibModalInstance, $sce,
                translation, currentUserName, assignedUsers, project, role, paging) {

                console.debug("Entered assign concept modal control", assignedUsers, project.id);

                $scope.batchSize = 10;
                $scope.project = project;
                $scope.role = role;
                $scope.assignedUserNames = [];
                $scope.selectedUserName = currentUserName;
                $scope.errors = [];
                $scope.note = "";

                // Prep userNames picklist
                for (var i = 0; i < assignedUsers.length; i++) {
                  $scope.assignedUserNames.push(assignedUsers[i].userName);
                }
                $scope.assignedUserNames = $scope.assignedUserNames.sort();

                // Handle assigning the batch
                $scope.assignBatch = function(userName) {
                  if (!userName) {
                    $scope.errors[0] = "The user must be selected. ";
                    return;
                  }
                  $scope.selectedUserName = userName;

                  // Perform the same search as the current concept id list
                  // and make sure it still matches (otherwise someone else
                  // may have assigned off this list first).  If successful, send the request
                  var pfs = {
                    startIndex : (paging["concept"].page - 1) * $scope.batchSize,
                    maxResults : $scope.batchSize,
                    sortField : paging["concept"].sortField,
                    ascending : paging["concept"].ascending == null ? true
                      : paging["concept"].ascending,
                    queryRestriction : null
                  };

                  workflowService
                    .findAvailableEditingConcepts(project.id, translation.id, userName, pfs)
                    .then(
                      // Success
                      function(data) {

                        // The first X entries of data.concepts should match translation.available
                        var match = true;
                        console.debug('look for match', data.concepts, translation.available);
                        for (var i = 0; i < Math.min(data.concepts.length,
                          translation.available.length); i++) {
                          if (data.concepts[i].id !== translation.available[i].id) {
                            match = false;
                            break
                          }
                        }
                        if (!match) {
                          $scope.errors[0] = "Some available concepts have been assigned, please refresh the list and try again.";
                          return;
                        }

                        // Make parameter
                        var conceptList = {
                          concepts : data.concepts
                        };

                        workflowService.performBatchTranslationWorkflowAction($scope.project.id,
                          translation.id, userName, $scope.role, "ASSIGN", conceptList).then(
                        // Success
                        function(data) {
                          // close modal
                          $uibModalInstance.close(translation);
                        },
                        // Error
                        function(data) {
                          $scope.errors[0] = data;
                          utilService.clearError();
                        })

                      },
                      // Error
                      function(data) {
                        $scope.errors[0] = data;
                        utilService.clearError();
                      })

                };

                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Edit concept modal
              $scope.openEditConceptModal = function(lconcept) {

                console.debug("openEditConceptModal ");

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/translationTable/editConcept.html',
                  controller : EditConceptModalCtrl,
                  backdrop : 'static',
                  size : 'lg',
                  resolve : {
                    concept : function() {
                      return lconcept;
                    },
                    translation : function() {
                      return $scope.selected.translation;
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

              // Edit concept controller
              var EditConceptModalCtrl = function($scope, $uibModalInstance, concept, translation,
                project) {

                console.debug("Entered edit concept modal control");

                $scope.pageSize = 5;
                $scope.paging = {};
                $scope.paging["descriptions"] = {
                  page : 1,
                  filter : ""
                }
                $scope.pagedDescriptions;
                $scope.errors = [];
                $scope.warnings = [];
                $scope.data = {
                  concept : null
                }
                $scope.translation = translation;
                // Convert case sensitive types to an array
                $scope.caseSensitiveTypes = [];
                for ( var type in $scope.translation.caseSensitiveTypes) {
                  $scope.caseSensitiveTypes.push({
                    key : type,
                    value : $scope.translation.caseSensitiveTypes[type]
                  })
                }
                $scope.conceptTranslated = JSON.parse(JSON.stringify(concept));
                $scope.conceptTranslated.descriptions = [];
                $scope.conceptTranslated.relationships = null;
                $scope.newDescription = null;
                //TODO: wire to user prefs
                $scope.enableSpelling = true;
                $scope.selectedWord = "_ALL_";
                $scope.enableMemory = true;
                $scope.selectedEntry = null;

                $scope.project = project;

                //Clear errors
                $scope.clearError = function() {
                  $scope.errors = [];
                }

                $scope.getDescriptionWords = function() {
                  // look through concdeptTranslated.descriptions and parse out words
                  return [ "todo", "fix", "this" ];
                }

                $scope.removeSpellingEntry = function(word) {
                  // If none chosen, return
                  if (!word) {
                    return;
                  }
                  // If all chosen, confirm
                  if (word == '_ALL_') {
                    if (!confirm("Are you sure you want to remove these spelling dictionary entries?")) {
                      return;
                    }
                    // Make a call to remove all of these entries
                  }

                  // Make a call to translation sevice to remove the entry from spelling
                }
                $scope.addSpellingEntry = function(word) {
                  // Make a call to translation sevice to remove the entry from spelling
                  // dictionary
                }

                $scope.getMemoryEntries = function() {
                  // These are whatever memory entries have bene retrieved for the 
                  // current set of descriptions
                  return [];
                }

                $scope.removeMemoryEntry = function(name, translatedName) {
                  // Make a call to translation sevice to remove the entry from memory
                }
                $scope.addMemoryEntry = function(name, translatedName) {
                  // Make a call to translation sevice to add entry to memory
                }

                // Apply the type parameters to the description
                $scope.applyDescriptionType = function(description, type) {

                  // assume at most one language entry
                  if (description.languages && description.languages.length > 1) {
                    alert('Unexpected number of language entries for description.');
                  }

                  //TODO: fix this                  description.typeId = type.type;
                  description.languages[0].descriptionId = description.terminologyId;
                  description.languages[0].acceptabilityId = type.acceptability;
                }

                // Get paged descriptions (assume all are loaded)
                $scope.getPagedDescriptions = function() {
                  $scope.pagedDescriptions = utilService.getPagedArray(
                    $scope.conceptTranslated.descriptions, $scope.paging['descriptions'],
                    $scope.pageSize);
                  console.debug("pagedDescriptions", $scope.pagedDescriptions);
                }

                // Add a new blank description entry
                $scope.newDescription = function() {
                  var description = {};
                  description.name = "";
                  description.languages = [ {} ];
                  description.caseSignificanceId = $scope.caseSensitiveTypes[0].key;
                  // TODO: decide which one to pick (maybe just look for "Synonym"
                  $scope
                    .applyDescriptionType(
                      description,
                      $scope.translation.descriptionTypes[$scope.translation.descriptionTypes.length - 1]);

                  $scope.conceptTranslated.descriptions.push(description);
                  $scope.getPagedDescriptions();
                }

                // Remove description at specified index
                $scope.removeDescription = function(index) {
                  $scope.conceptTranslated.descriptions.splice(index, 1);
                  $scope.getPagedDescriptions();
                }

                $scope.submitConcept = function(concept) {

                  // TODO: validation/errors

                  translationService.updateConcept(concept).then(
                  // Success - update concept
                  function(data) {

                    // TODO: workflow action to "SAVE"

                    $uibModalInstance.close(concept);
                  },
                  // Error - update concept
                  function(data) {
                    $scope.errors[0] = data;
                    utilService.clearError();
                  })

                };

                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

                // Initialize
                $scope.data.concept = concept;
                // If editing from scratch, start with one description
                if ($scope.conceptTranslated.descriptions.length == 0) {
                  $scope.newDescription();
                }

              };

              // Import/Export modal
              $scope.openImportExportModal = function(ltranslation, loperation, ltype) {
                console.debug("exportModal ", ltranslation);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/translationTable/importExport.html',
                  controller : ImportExportModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    translation : function() {
                      return ltranslation;
                    },
                    operation : function() {
                      return loperation;
                    },
                    type : function() {
                      return ltype;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  translationService.fireTranslationChanged(data);
                });
              };

              // Import/Export controller
              var ImportExportModalCtrl = function($scope, $uibModalInstance, translation,
                operation, type) {
                console.debug("Entered import export modal control");

                $scope.translation = translation;
                $scope.type = type;
                $scope.operation = operation;
                $scope.errors = [];

                // Handle export
                $scope.export = function() {
                  console.debug("export", $scope.translation.id);
                  if (type == 'Spelling Dictionary') {
                    translationService.exportSpellingDictionary($scope.translation.id);
                  }
                  if (type == 'Phrase Memory') {
                    translationService.exportPhraseMemory($scope.translation.id);
                  }
                  $uibModalInstance.close();
                };

                // Handle import
                $scope.import = function(file) {
                  console.debug("import", $scope.translation.id, file);

                  if (type == 'Spelling Dictionary') {
                    translationService.importSpellingDictionary($scope.translation.id, file).then(
                      // Success
                      function(data) {
                        $uibModalInstance.close($scope.translation);
                      },
                      // Error 
                      function(data) {
                        $scope.errors[0] = data;
                        utilService.clearError();
                      });
                  }
                  if (type == 'Phrase Memory') {
                    translationService.importPhraseMemory($scope.translation.id, file).then(
                      // Success
                      function(data) {
                        $uibModalInstance.close($scope.translation);
                      },
                      // Error 
                      function(data) {
                        $scope.errors[0] = data;
                        utilService.clearError();
                      });
                  }

                };

                $scope.cancel = function() {
                  // dismiss the dialog
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Copy modal
              $scope.openCopyModal = function(ltranslation, ltype) {
                console.debug("copyModal ", ltranslation, ltype);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/translationTable/copy.html',
                  controller : CopyModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    toTranslation : function() {
                      return ltranslation;
                    },
                    type : function() {
                      return ltype;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  translationService.fireTranslationChanged(data);
                });
              };

              // Copy controller
              var CopyModalCtrl = function($scope, $uibModalInstance, toTranslation, type) {
                console.debug("Entered copymodal control");

                $scope.translations = [];
                $scope.toTranslation = toTranslation;
                $scope.translation = null;
                $scope.type = type;
                $scope.errors = [];

                // Initialize by looking up all translations
                translationService.findTranslationsForQuery("", {}).then(
                // Success
                function(data) {
                  var list = [];
                  for (var t in data.translations) {
                    if (type == 'Spelling Dictionary' && t.spellingDictionarySize > 0) {
                      list.push(t);
                    }
                    if (type == 'Phrase Memory' && t.phraseMemorySize > 0) {
                      list.push(t);
                    }
                  }
                  $scope.translations = list.sort(utilService.sort_by('name'));
                  if ($scope.translations.length == 0) {
                    $scope.errors[0] = "No translations with " + type + " entries were found.";
                  }
                },
                // Error
                function(data) {
                  $scope.errors[0] = data;
                  utilService.clearError();
                });

                // Select translation
                $scope.selectTranslation = function(translation) {
                  $scope.translation = translation;
                }

                // Handle export
                $scope.copy = function() {
                  if (type == 'Spelling Dictionary') {
                    translationService.copySpellingDictionary($scope.translation.id,
                      $scope.toTranslation.id);
                  }
                  if (type == 'Phrase Memory') {
                    translationService.copyPhraseMemory($scope.translation.id,
                      $scope.toTranslation.id);
                  }
                  $uibModalInstance.close(refset);
                };

                $scope.cancel = function() {
                  // dismiss the dialog
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // end

            } ]
        }
      } ]);
