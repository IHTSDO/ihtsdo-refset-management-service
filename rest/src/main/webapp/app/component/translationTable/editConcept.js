// Edit concept modal controller
tsApp.controller('EditConceptModalCtrl', [
  '$scope',
  '$uibModalInstance',
  '$uibModal',
  'projectService',
  'utilService',
  'translationService',
  'validationService',
  'workflowService',
  'securityService',
  'concept',
  'translation',
  'project',
  'user',
  'role',
  function($scope, $uibModalInstance, $uibModal, projectService, utilService, translationService,
    validationService, workflowService, securityService, concept, translation, project, user, role) {
    console.debug('Entered edit concept modal control');
    
    $scope.displaySuggest = true;
    // Paging params
    $scope.pageSize = 4;
    $scope.paging = {};
    $scope.paging['descriptions'] = {
      page : 1,
      filter : '',
      sortField : 'lastModified',
      ascending : null
    };
    $scope.pagedDescriptions;

    // make the callback function actually a scope function (and a regular
    // function), then ditch the "abc" button, and instaed
    // just call it on blur, change or debounce.
    // tinymce config
    $scope.tinymceOptions = {
      oninit : "setPlainText",
      resize : false,
      max_height : 80,
      height : 80,
      plugins : 'spellchecker paste',
      menubar : false,
      statusbar : false,
      toolbar : 'spellchecker',
      format : 'text',
      spellchecker_languages : translation.language + '=' + translation.language,
      spellchecker_language : translation.language,
      spellchecker_wordchar_pattern : /[^\s,\.]+/g,
      spellchecker_callback : function(method, text, success, failure) {
        // method == spellcheck
        if (method == 'spellcheck' && text) {
          // NOTE: may not need to actually call this, probably can
          // just look up
          // words from the description
          translationService.suggestBatchSpelling(translation.id,
            text.match(this.getWordCharPattern())).then(
          // Success
          function(data) {
            $scope.suggestions = {};
            for ( var entry in data.map) {
              $scope.suggestions[entry] = data.map[entry].strings;
            }
            var result = {
              'dictionary' : 'true',
              'words' : $scope.suggestions
            };
            success(result);
          },
          // Error
          function(data) {
            handleError($scope.errors, data);
            $scope.suggestions = {};
            failure(data);
          });

        }

        // method == addToDictionary
        if (method == 'addToDictionary') {
          translationService.addSpellingDictionaryEntry(translation.id, text).then(
          // Success
          function(data) {
            // Recompute suggestions
            $scope.getSuggestions();
            success(data);
            if ($scope.translation.spellingDictionaryEmpty) {
              $scope.translation.spellingDictionaryEmpty = false;
            }
          },
          // Error
          function(data) {
            handleError($scope.errors, data);
            failure(data);
          });

        }
      }
    };

    // Validation
    $scope.errors = [];
    $scope.warnings = [];

    // data structure for report - setting this causes the frame to
    // load
    $scope.data = {
      concept : null,
      descriptionTypes : translation.descriptionTypes,
      translation : translation,
      terminology : translation.terminology,
      version : translation.version
    };

    // scope variables
    $scope.translation = translation;
    $scope.conceptTranslated = JSON.parse(JSON.stringify(concept));
    $scope.conceptTranslated.relationships = null;
    $scope.newDescription = null;
    $scope.project = project;
    $scope.user = user;
    $scope.role = role;
    $scope.authorNames = [];
    $scope.descriptionAuthor = {};
        
    if (translation.assigned) {
      $scope.editTranslation = translation.assigned.find(
        et => et.concept.terminologyId === concept.terminologyId);
      $scope.editTranslation.concept.descriptions.forEach(function(description) {
        securityService.getUserByUsername(description.lastModifiedBy).then(
          function(data) {
            //If no user found, add username as-is
            var username;
            if(data == ""){
              username = description.lastModifiedBy;
            }
            else{
              username = data.name;
            }
            $scope.descriptionAuthor[description.id] = username;
            
            if(!$scope.authorNames.includes(username, 0)){
           $scope.authorNames.push(username);
            }
          });
      });
    }
    
    // Save this so we can set the workflow status and it shows up
    // immediately
    $scope.concept = concept;
    $scope.autoResult = null;
    $scope.translationSuggestions = [];
    $scope.displayControls = false;

    // Data structure for case significance - we just need the
    // id/name
    $scope.caseSignificanceTypes = [];
    for ( var type in $scope.translation.caseSensitiveTypes) {
      $scope.caseSignificanceTypes.push({
        key : type,
        value : $scope.translation.caseSensitiveTypes[type]
      });
    }

    // spelling/memory scope vars
    $scope.selectedWord = null;
    $scope.allUniqueWordsNoSuggestions = [];
    // Result of gathered suggestions - {'words' : {'word' :
    // ['suggestion1', 'suggestion2'] }}
    $scope.suggestions = {};
    $scope.memoryEntries = [];
    $scope.memoryEntriesMap = {};
    $scope.allUniquePhrasesNoSuggestions = [];
    $scope.selectedEntry = null;
    $scope.selectedName = null;
    $scope.translatedName = null;
    // When descriptions are ready, load phrases
    $scope.$watch('data.descriptions', function() {
      if (!$scope.data.descriptions) {
        return;
      }
      $scope.getMemoryEntries();

      var translateTerm = '';
      // translate the first 'en' PT in the descriptions
      for (var i = 0; i < $scope.data.descriptions.length; i++) {
        if ($scope.getDescriptionType($scope.data.descriptions[i]) == 'PT'
          && $scope.data.descriptions[i].languageCode == 'en') {
          translateTerm = $scope.data.descriptions[i].term;
          break;
        }
      }
      // translate term
      if (translateTerm) {
        projectService.translate($scope.project.id, translateTerm, $scope.translation.language)
          .then(function(data) {
            $scope.autoResult = data;
          });
                
        translationService.findTranslationSuggestionsForConcept(
          $scope.translation.refsetId, $scope.concept.terminologyId)
          .then(function(data) {
            $scope.translationSuggestions = data;
        });
      }

    });
    // Clear errors
    $scope.clearError = function() {
      $scope.errors = [];
    };

    // Return the description type name
    $scope.getDescriptionType = function(description) {
      for (var i = 0; i < $scope.data.descriptionTypes.length; i++) {
        var type = $scope.data.descriptionTypes[i];
        if (description.typeId == type.typeId && description.languages
          && description.languages[0].acceptabilityId == type.acceptabilityId) {
          return type.name;
        }
      }
      return 'UNKNOWN';
    };

    // link to error handling
    function handleError(errors, error) {
      utilService.handleDialogError(errors, error);
    }

    // toggles display of memory and spelling controls
    $scope.toggleDisplayControls = function() {
      $scope.displayControls = !$scope.displayControls;
    };

    // Need both a $scope version and a non one for modals.
    $scope.updateConceptName = function(translation, conceptId) {
      updateConceptName(translation, conceptId);
    };

    // Update concept name from term server
    function updateConceptName(translation, conceptId) {
      console.debug("update concept name");
      translationService.updateConceptName(translation.id, conceptId).then(
      // Success
      function(data) {
    	  $scope.conceptTranslated.name= data.name;
      });
    }
    
    // Table sorting

    $scope.setSortField = function(field, object) {
      utilService.setSortField('descriptions', field, $scope.paging);
      $scope.getPagedDescriptions();
      $scope.$broadcast('$tinymce:refresh');
    };
    $scope.getSortIndicator = function(field) {
      return utilService.getSortIndicator('descriptions', field, $scope.paging);
    };

    // Spelling Correction

    // Populate $scope.suggestions (outside of spelling correction
    // run)
    $scope.getSuggestions = function() {

      $scope.suggestions = {};
      if (!$scope.user.userPreferences.spellingEnabled) {
        return;
      }

      translationService.suggestBatchSpelling(translation.id, $scope.getAllUniqueWords()).then(
      // Success
      function(data) {
        for ( var entry in data.map) {
          $scope.suggestions[entry] = data.map[entry].strings;
        }
        // compute all unique words without suggestions
        $scope.getAllUniqueWordsNoSuggestions();
      },
      // Error
      function(data) {
        handleError($scope.errors, data);
        $scope.suggestions = {};
      });
    };

    // Determine if a description has any suggestion words (e.g.
    // should spelling correction be run)
    $scope.hasSuggestions = function(description) {
      var words = utilService.getWords(description.term);
      if (words && words.length > 0) {
        for (var i = 0; i < words.length; i++) {
          if ($scope.suggestions[words[i]]) {
            return true;
          }
        }
      }
      return false;
    };

    // Get unique words from all descriptions
    $scope.getAllUniqueWords = function() {
      var all = {};
      for (var i = 0; i < $scope.conceptTranslated.descriptions.length; i++) {
        var words = utilService.getWords($scope.conceptTranslated.descriptions[i].term);
        if (words && words.length > 0) {
          for (var j = 0; j < words.length; j++) {
            all[words[j]] = 1;
          }
        }
      }
      var retval = new Array();
      for ( var word in all) {
        retval.push(word);
      }
      return retval.sort();
    };

    // Sets $scope.allUniqueWordsNoSuggestions
    $scope.getAllUniqueWordsNoSuggestions = function() {
      if (!$scope.user.userPreferences.spellingEnabled) {
        $scope.allUniqueWordsNoSuggestions = [];
        return;
      }
      var words = $scope.getAllUniqueWords();
      var retval = new Array();
      for (var i = 0; i < words.length; i++) {
        if (words[i] && !$scope.suggestions[words[i]]) {
          retval.push(words[i]);
        }
      }
      $scope.allUniqueWordsNoSuggestions = retval.sort();
      if (retval.length > 0) {
        $scope.selectedWord = retval[0];
      }
    };

    // Remove a spelling entry
    $scope.removeSpellingEntry = function(word) {
      // If none chosen, return
      if (!word) {
        return;
      }
      translationService.removeSpellingDictionaryEntry(translation.id, word).then(
      // Success
      function(data) {
        $scope.getSuggestions();
      },
      // Error
      function(data) {
        handleError($scope.errors, data);
      });

    };

    // Add a spelling entry
    $scope.addSpellingEntry = function(word) {
      // If none chosen, return
      if (!word) {
        return;
      }
      translationService.removeSpellingDictionaryEntry(translation.id, word).then(
      // Success
      function(data) {
        $scope.getSuggestions();
      },
      // Error
      function(data) {
        handleError($scope.errors, data);
      });
    };

    // Add a spelling entry
    $scope.addAllSpellingEntries = function(description) {
      var words = utilService.getWords(description.term);
      var map = {};
      if (words && words.length > 0) {
        for (var i = 0; i < words.length; i++) {
          if ($scope.suggestions[words[i]]) {
            map[words[i]] = 1;
          }
        }
      }
      var entries = new Array();
      for ( var key in map) {
        entries.push(key);
      }
      translationService.addBatchSpellingDictionaryEntries(translation.id, entries).then(
      // Success
      function(data) {
        $scope.getSuggestions();
        if ($scope.translation.spellingDictionaryEmpty) {
          $scope.translation.spellingDictionaryEmpty = false;
        }
      },
      // Error
      function(data) {
        handleError($scope.errors, data);
      });
    };

    // Translation memory

    // Get unique phrases from all English descriptions
    $scope.getAllUniquePhrases = function() {
      // bail if no descriptions
      if (!$scope.data.descriptions) {
        return [];
      }
      var all = {};
      for (var i = 0; i < $scope.data.descriptions.length; i++) {
        var desc = $scope.data.descriptions[i];
        // Skip non-English phrases
        // NOTE: ideally this should be metadata driven
        if (desc.languageCode != 'en') {
          continue;
        }
        var phrases = utilService.getPhrases(desc.term);
        if (phrases && phrases.length > 0) {
          for (var j = 0; j < phrases.length; j++) {
            all[phrases[j]] = 1;
          }
        }
      }
      var retval = new Array();
      for ( var phrase in all) {
        retval.push(phrase);
      }
      return retval.sort();
    };

    // Sets $scope.allUniquePhraseNoSuggestions
    $scope.getAllUniquePhrasesNoSuggestions = function() {
      if (!$scope.user.userPreferences.memoryEnabled) {
        $scope.allUniquePhrasesNoSuggestions = [];
        return;
      }
      var phrases = $scope.getAllUniquePhrases();
      var retval = new Array();
      for (var i = 0; i < phrases.length; i++) {
        if (phrases[i] && !$scope.memoryEntriesMap[phrases[i]]) {
          retval.push(phrases[i]);
        }
      }
      $scope.allUniquePhrasesNoSuggestions = retval.sort();
      if (retval.length > 0) {
        $scope.selectedName = retval[0];
      }
    };

    // Displayable value for an entry
    $scope.getEntryInfo = function(entry) {
      return entry.name + ' => ' + entry.translatedName;
    };
    // Populates $scope.memoryEntries
    $scope.getMemoryEntries = function() {
      $scope.memoryEntries = [];
      $scope.memoryEntriesMap = {};
      if (!$scope.user.userPreferences.memoryEnabled) {
        return;
      }

      translationService.suggestBatchTranslation(translation.id, $scope.getAllUniquePhrases())
        .then(
        // Success
        function(data) {
          for ( var entry in data.map) {
            for (var i = 0; i < data.map[entry].strings.length; i++) {
              $scope.memoryEntriesMap[entry] = 1;
              $scope.memoryEntries.push({
                name : entry,
                translatedName : data.map[entry].strings[i]
              });
            }
            if ($scope.memoryEntries.length > 0) {
              $scope.selectedEntry = $scope.memoryEntries[0];

            }
          }
          // compute all unique phrases without suggestions
          $scope.getAllUniquePhrasesNoSuggestions();
        },
        // Error
        function(data) {
          handleError($scope.errors, data);
        });
    };

    // Apply memory entry to the 'current' description
    $scope.applyMemoryEntry = function(translatedName) {
      // Find the first empty description and put the translated
      // name there
      var found = false;
      for (var i = 0; i < $scope.pagedDescriptions.length; i++) {
        var desc = $scope.pagedDescriptions[i];
        if (!desc.term) {
          desc.term = translatedName;
          found = true;
        }
      }
      // If not found, just add to the end of the first description
      if (!found && $scope.pagedDescriptions.length > 0) {
        if ($scope.pagedDescriptions[0].term) {
          $scope.pagedDescriptions[0].term += ' ' + translatedName;
        } else {
          $scope.pagedDescriptions[0].term = translatedName;
        }
      }
    };

    // Remove a memory entry
    $scope.removeMemoryEntry = function(name, translatedName) {
      translationService.removePhraseMemoryEntry(translation.id, name, translatedName).then(
      // Success
      function(data) {
        $scope.getMemoryEntries();
      },
      // Error
      function(data) {
        handleError($scope.errors, data);
      });
    };

    // Add a memory entry
    $scope.addMemoryEntry = function(name, translatedName) {
      translationService.addPhraseMemoryEntry(translation.id, name, translatedName).then(
      // Success
      function(data) {
        // clear selected ata model
        $scope.selectedName = null;
        $scope.translatedName = null;
        $scope.getMemoryEntries();
      },
      // Error
      function(data) {
        handleError($scope.errors, data);
      });
    };

    // Description stuff

    // Get description types
    $scope.getDescriptionTypes = function() {
      return $scope.translation.descriptionTypes.sort(utilService.sortBy('name'));
    };
    // Get paged descriptions (assume all are loaded)
    $scope.getPagedDescriptions = function() {
      // special handling for "typeId"
      if ($scope.paging['descriptions'].sortField == 'typeId') {
        var paging = angular.copy($scope.paging['descriptions']);
        paging.sortField = '';
        var arr = $scope.conceptTranslated.descriptions.sort(function(a, b) {
          var retval = 0;
          if (a.type.name < b.type.name)
            retval = paging.ascending ? -1 : 1;
          if (a.type.name > b.type.name)
            retval = paging.ascending ? 1 : -1;
          return retval;
        });
        $scope.pagedDescriptions = utilService.getPagedArray(arr, paging, $scope.pageSize);
      } else {
        $scope.pagedDescriptions = utilService.getPagedArray($scope.conceptTranslated.descriptions,
          $scope.paging['descriptions'], $scope.pageSize);
      }
    };

    $scope.addAutoDescription = function(term) {
      if ($scope.conceptTranslated.descriptions.length == 1
        && $scope.conceptTranslated.descriptions[0].term == '') {
        $scope.conceptTranslated.descriptions[0].term = term;
      } else {
        $scope.addDescription(term);
      }
    }

    // Add a new empty description entry
    $scope.addDescription = function(term) {
      var description = {};
      if (term) {
        description.term = term;
      } else {
        description.term = '';
      }
      description.caseSignificanceId = $scope.caseSignificanceTypes[0].key;
      // Pick PT or SY by default depending on how many descriptions there are
      var types = $scope.getDescriptionTypes();
      description.type = types.filter(function(item) {
        return $scope.conceptTranslated.descriptions.length == 0 ? item.name == 'PT'
          : item.name == 'SY';
      })[0];
      $scope.conceptTranslated.descriptions.push(description);
      if ($scope.conceptTranslated.descriptions.length > $scope.pageSize) {
        $scope.paging['descriptions'].page = 
          parseInt($scope.conceptTranslated.descriptions.length / $scope.pageSize) + 1;
      }
      $scope.getPagedDescriptions();
    };

    // Remove description at specified index
    $scope.removeDescription = function(index, page) {
      page = (page == null ) ? 0 : page-1;
      var itemToRemove = index + (page*$scope.pageSize);
      $scope.conceptTranslated.descriptions.splice(itemToRemove, 1);
      $scope.getPagedDescriptions();
    };

    // Concept stuff

    // Save concept
    $scope.saveConcept = function(concept, closeFlag) {
      $scope.saveOrFinishConcept(concept, 'SAVE', closeFlag);
    };

    // Finish concept
    $scope.finishConcept = function(concept, closeFlag) {
      $scope.saveOrFinishConcept(concept, 'FINISH', closeFlag);
    };

    // Handle both save and finish - different workflow
    // action is used after validate
    $scope.saveOrFinishConcept = function(concept, action, closeFlag) {
      // Iterate through concept, set description types and
      // languages
      var spliceIndexes = new Array();
      var copy = JSON.parse(JSON.stringify(concept));
      for (var i = 0; i < copy.descriptions.length; i++) {
        var desc = copy.descriptions[i];
        desc.typeId = desc.type.typeId;
        desc.languages = [ {} ];
        desc.languages[0].descriptionId = desc.terminologyId;
        desc.languages[0].acceptabilityId = desc.type.acceptabilityId;
        desc.type = undefined;
        if (!desc.term) {
          spliceIndexes.push(i);
        }
      }
      // Remove empty descriptions
      for (var i = 0; i < spliceIndexes.length; i++) {
        copy.descriptions.splice(spliceIndexes[i], 1);
      }

      if (copy.descriptions.length == 0) {
        $scope.errors = [];
        $scope.errors[0] = 'Enter at least one description';
        return;
      }

      $scope.validateConcept(copy, action, closeFlag);
    };

    // Validate the concept
    $scope.validateConcept = function(concept, action, closeFlag) {
      // Validate the concept
      validationService.validateConcept(concept, $scope.project.id).then(
      // Success
      function(data) {
        // If there are errors, make them available and stop.
        if (data.errors && data.errors.length > 0) {
          $scope.errors = data.errors;
          return;
        } else {
          $scope.errors = [];
        }

        // if $scope.warnings is empty, and data.warnings is not,
        // show warnings and stop
        if ($scope.warnings.length == 0 && data.warnings && data.warnings.length > 0) {
          $scope.warnings = data.warnings;
          return;
        } else {
          $scope.warnings = [];
        }

        // Otherwise, there are no errors and either no warnings
        // or the user has clicked through warnings. Proceed
        $scope.submitConceptHelper(concept, action, closeFlag);

      },
      // Error
      function(data) {
        handleError($scope.errors, data);
      });
    };

    // Helper (so there's not so much nesting
    $scope.submitConceptHelper = function(concept, action, closeFlag) {

      translationService.updateTranslationConcept(concept).then(
        // Success - update concept
        function(data) {
          // pick up the latest concept
          concept = data;
          // Perform a workflow 'save' operation
          workflowService.performTranslationWorkflowAction($scope.project.id,
            $scope.translation.id, $scope.user.userName, $scope.role, 'SAVE', concept).then(
            // Success
            function(data) {
              // Set the workflow status in the assigned concepts
              // list
              $scope.concept.workflowStatus = data.concept.workflowStatus;
              // Special case:
              // If "FINISH", mark again as 'finish'
              if (action == 'FINISH') {
                workflowService.performTranslationWorkflowAction($scope.project.id,
                  $scope.translation.id, $scope.user.userName, $scope.role, action, concept).then(
                // Success
                function(data) {
                  // Set the workflow status in the assigned
                  // concepts list
                  $scope.concept.workflowStatus = data.concept.workflowStatus;
                  $uibModalInstance.close({
                    action : action,
                    closeFlag : closeFlag
                  });
                },
                // Error
                function(data) {
                  handleError($scope.errors, data);
                });

              } else {
                $uibModalInstance.close({
                  action : action,
                  closeFlag : closeFlag
                });
              }
            },
            // Error
            function(data) {
              handleError($scope.errors, data);
            });
        },
        // Error - update concept
        function(data) {
          handleError($scope.errors, data);
        });

    };

    // Close modal
    $scope.close = function() {
      $uibModalInstance.close({
        action : 'CLOSE',
        closeFlag : true
      });
    };

    // Initialize
    $scope.data.concept = concept;
    // If editing from scratch, start with one description
    if ($scope.conceptTranslated.descriptions.length == 0) {
      $scope.addDescription();
    }

    // otherwise, set terms
    else {
      for (var i = 0; i < $scope.conceptTranslated.descriptions.length; i++) {
        var desc = $scope.conceptTranslated.descriptions[i];
        for (var j = 0; j < $scope.translation.descriptionTypes.length; j++) {
          var type = $scope.translation.descriptionTypes[j];
          if (desc.typeId == type.typeId
            && desc.languages[0].acceptabilityId == type.acceptabilityId) {
            desc.type = type;
          }
        }
      }
      $scope.getPagedDescriptions();
      $scope.getSuggestions();
    }

    //
    // MODALS
    //

    // Log modal
    $scope.openLogModal = function(concept) {
      console.debug('openLogModal ');

      var modalInstance = $uibModal.open({
        templateUrl : 'app/component/refsetTable/log.html',
        controller : LogModalCtrl,
        backdrop : 'static',
        size : 'lg',
        resolve : {
          translation : function() {
            return $scope.translation;
          },
          project : function() {
            return $scope.project;
          },
          concept : function() {
            return concept;
          },
        }
      });

      // NO need for a result function
      // modalInstance.result.then(
      // // Success
      // function(data) {
      // });
    };

    // Log controller
    var LogModalCtrl = function($scope, $uibModalInstance, translation, project, concept) {
      console.debug('Entered log modal control', translation, project, concept);

      $scope.filter = '';
      $scope.errors = [];
      $scope.warnings = [];

      // Get log to display
      $scope.getLog = function() {
        var objectId = translation.id
        if (concept) {
          objectId = concept.id;
        }
        projectService.getLog(project.id, objectId, $scope.filter).then(
        // Success
        function(data) {
          $scope.log = data;
        },
        // Error
        function(data) {
          handleError($scope.errors, data);
        });

      };

      // close modal
      $scope.close = function() {
        // nothing changed, don't pass a value
        $uibModalInstance.close();
      };

      // initialize
      $scope.getLog();
    };

    $scope.openNotesModal = function(lobject, ltype) {
      console.debug('openNotesModal ', lobject, ltype);

      var modalInstance = $uibModal.open({
        // Reuse refset URL
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
            return utilService.tinymceOptions;
          },
          translation : function() {
            return $scope.translation;
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
    var NotesModalCtrl = function($scope, $uibModalInstance, $sce, object, type, tinymceOptions,
      translation) {
      console.debug('Entered notes modal control', object, type, translation.id);

      $scope.errors = [];

      $scope.object = object;
      $scope.type = type;
      $scope.tinymceOptions = tinymceOptions;
      $scope.newNote = null;

      // Paging parameters
      $scope.pageSize = 5;
      $scope.pagedNotes = [];
      $scope.paging = {};
      $scope.paging['notes'] = {
        page : 1,
        filter : '',
        typeFilter : '',
        sortField : 'lastModified',
        ascending : true
      };

      // Get paged notes (assume all are loaded)
      $scope.getPagedNotes = function() {
        $scope.pagedNotes = utilService.getPagedArray($scope.object.notes, $scope.paging['notes'],
          $scope.pageSize);
      };

      $scope.getNoteValue = function(note) {
        return $sce.trustAsHtml(note.value);
      };

      // remove note
      $scope.removeNote = function(object, note) {
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
              handleError($scope.errors, data);
            });
          },
          // Error - add refset
          function(data) {
            handleError($scope.errors, data);
          });
        } else if ($scope.type == 'Concept') {
          translationService.removeTranslationConceptNote(object.id, note.id).then(
          // Success - add refset
          function(data) {
            $scope.newNote = null;
            translationService.getConcept(object.id).then(function(data) {
              object.notes = data.notes;
              $scope.getPagedNotes();
            },
            // Error - add refset
            function(data) {
              handleError($scope.errors, data);
            });
          },
          // Error - add refset
          function(data) {
            handleError($scope.errors, data);
          });
        }
      };

      $scope.submitNote = function(object, text) {

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
              handleError($scope.errors, data);
            });
          },
          // Error - add translation
          function(data) {
            handleError($scope.errors, data);
          });
        } else if ($scope.type == 'Concept') {
          translationService.addTranslationConceptNote(translation.id, object.id, text).then(
          // Success - add translation
          function(data) {
            $scope.newNote = null;

            translationService.getConcept(object.id).then(function(data) {
              object.notes = data.notes;
              $scope.getPagedNotes();
            },
            // Error - add translation
            function(data) {
              handleError($scope.errors, data);
            });
          },
          // Error - add translation
          function(data) {
            handleError($scope.errors, data);
          });
        }
      };

      // Convert date to a string
      $scope.toDate = function(lastModified) {
        return utilService.toDate(lastModified);
      };

      // Dismiss modal
      $scope.cancel = function() {
        $uibModalInstance.dismiss('cancel');
      };

      // initialize modal
      $scope.getPagedNotes();
    };
  } ]);