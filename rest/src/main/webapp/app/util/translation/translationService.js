// Translation Service
tsApp
  .service('translationService',
    [
      '$http',
      '$q',
      '$rootScope',
      'Upload',
      'gpService',
      'utilService',
      function($http, $q, $rootScope, Upload, gpService, utilService) {
        console.debug('configure translationService');

        // broadcasts a translation change
        this.fireTranslationChanged = function(translation) {
          $rootScope.$broadcast('refset:translationChanged', translation);
        }

        // broadcasts a concept change
        this.fireConceptChanged = function(concept) {
          $rootScope.$broadcast('refset:conceptChanged', concept);
        }

        // Get translation for id and date
        this.getTranslationRevision = function(translationId, date) {
          console.debug('getTranslationRevision');
          var deferred = $q.defer();

          // Get translation for id and date
          gpService.increment();
          $http.get(translationUrl + translationId + '/' + date).then(
          // success
          function(response) {
            console.debug('  translation ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Finds concepts for translation revision
        this.findTranslationRevisionConceptsForQuery = function(translationId,
          date, pfs) {
          console.debug('findTranslationRevisionConceptsForQuery');
          var deferred = $q.defer();

          // Finds concepts for translation revision
          gpService.increment();
          $http.post(translationUrl + translationId + '/' + date + '/concepts',
            utilService.prepPfs(pfs)).then(
          // success
          function(response) {
            console.debug('  concepts ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Get translation for id
        this.getTranslation = function(translationId) {
          console.debug('getTranslation');
          var deferred = $q.defer();

          // Get translation for id
          gpService.increment();
          $http.get(translationUrl + translationId).then(
          // success
          function(response) {
            console.debug('  translation ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // get translation concept for id
        this.getConcept = function(conceptId) {
          console.debug('getConcept');
          var deferred = $q.defer();

          gpService.increment();
          $http.get(translationUrl + 'concept/' + conceptId).then(
          // success
          function(response) {
            console.debug('  concept = ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }
        // Get translation for refset
        this.getTranslationsForRefset = function(refsetId) {
          console.debug('getTranslationsForRefset');
          var deferred = $q.defer();

          // Get translation for id
          gpService.increment();
          $http.get(translationUrl + 'translations/' + refsetId).then(
          // success
          function(response) {
            console.debug('  translations ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Finds translations
        this.findTranslationsForQuery = function(query, pfs) {
          console.debug('findTranslationsForQuery');
          var deferred = $q.defer();

          // Finds translations
          gpService.increment();
          $http.post(
            translationUrl + 'translations?query='
              + utilService.prepQuery(query), utilService.prepPfs(pfs)).then(
          // success
          function(response) {
            console.debug('  translations ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Add new translation
        this.addTranslation = function(translation) {
          console.debug('addTranslation', translation);
          var deferred = $q.defer();

          // Add new translation
          gpService.increment();
          $http.put(translationUrl + 'add', translation).then(
          // success
          function(response) {
            console.debug('  translation ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Update translation
        this.updateTranslation = function(translation) {
          console.debug('updateTranslation');
          var deferred = $q.defer();

          // Update translation
          gpService.increment();
          $http.post(translationUrl + 'update', translation).then(
          // success
          function(response) {
            console.debug('  translation ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Remove translation
        this.removeTranslation = function(translationId) {
          console.debug('removeTranslation');
          var deferred = $q.defer();

          // Remove translation
          gpService.increment();
          $http['delete'](
            translationUrl + 'remove/' + translationId + '?cascade=true').then(
          // success
          function(response) {
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // remove translation concepts
        this.removeAllTranslationConcepts = function(translationId) {
          console.debug('removeAllTranslationConcepts');
          var deferred = $q.defer();

          // remove translation concepts
          gpService.increment()
          $http['delete'](
            translationUrl + 'concept/remove/all/' + translationId).then(
          // success
          function(response) {
            console.debug('  remove translation concepts = ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // find concepts for translation and query
        this.findTranslationConceptsForQuery = function(translationId, query,
          pfs) {
          console.debug('findTranslationConceptsForQuery');
          var deferred = $q.defer();

          // find concepts
          gpService.increment();
          $http.post(
            translationUrl + 'concepts?query=' + utilService.prepQuery(query)
              + '&translationId=' + translationId, utilService.prepPfs(pfs))
            .then(
            // success
            function(response) {
              console.debug('  concepts = ', response.data);
              gpService.decrement();
              deferred.resolve(response.data);
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        }

        // Get import translation handlers
        this.getImportTranslationHandlers = function() {
          console.debug('getImportTranslationHandlers');
          var deferred = $q.defer();

          gpService.increment();
          $http.get(translationUrl + 'import/handlers').then(
          // success
          function(response) {
            console.debug('  handlers ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Get export translation handlers
        this.getExportTranslationHandlers = function() {
          console.debug('getExportTranslationHandlers');
          var deferred = $q.defer();

          gpService.increment();
          $http.get(translationUrl + 'export/handlers').then(
          // success
          function(response) {
            console.debug('  handlers ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Add new translation concept
        this.addTranslationConcept = function(concept) {
          console.debug('addTranslationConcept');
          var deferred = $q.defer();

          gpService.increment();
          $http.put(translationUrl + 'concept/add', concept).then(
          // success
          function(response) {
            console.debug('  concept ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Update translation concept
        this.updateTranslationConcept = function(concept) {
          console.debug('updateTranslationConcept');
          var deferred = $q.defer();

          // Update concept
          gpService.increment();
          $http.post(translationUrl + 'concept/update', concept).then(
          // success
          function(response) {
            console.debug('  concept ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Remove translation concept
        this.removeTranslationConcept = function(conceptId) {
          console.debug('removeTranslationConcept');
          var deferred = $q.defer();

          gpService.increment();
          $http['delete'](translationUrl + 'concept/remove/' + conceptId).then(
          // success
          function(response) {
            console.debug('  concept = ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Get translations with spelling dictionary
        this.getTranslationsWithSpellingDictionary = function() {
          console.debug('getTranslationsWithSpellingDictionary');
          var deferred = $q.defer();

          gpService.increment();
          $http.get(translationUrl + 'translations/spellingdictionary').then(
          // success
          function(response) {
            console.debug('  handlers ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Copy spelling dictionary from one translation to another
        this.copySpellingDictionary = function(fromTranslationId,
          toTranslationId) {
          console.debug('copySpellingDictionary');
          var deferred = $q.defer();

          gpService.increment();
          $http.get(
            translationUrl + 'spelling/copy?fromTranslationId='
              + fromTranslationId + '&toTranslationId=' + toTranslationId)
            .then(
            // success
            function(response) {
              console.debug('  copy ', response.data);
              gpService.decrement();
              deferred.resolve(response.data);
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        }

        // Add spelling dictionary entry
        this.addSpellingDictionaryEntry = function(translationId, entry) {
          console.debug('addSpellingDictionaryEntry');
          var deferred = $q.defer();

          gpService.increment();
          $http.put(
            translationUrl + 'spelling/add?translationId=' + translationId,
            entry, {
              headers : {
                'Content-type' : 'text/plain'
              }
            }).then(
          // success
          function(response) {
            console.debug('  entry ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Add spelling dictionary entry
        this.addBatchSpellingDictionaryEntries = function(translationId,
          entries) {
          console.debug('addBatchSpellingDictionaryEntries');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            translationUrl + 'spelling/add/batch?translationId='
              + translationId, {
              strings : entries
            }).then(
          // success
          function(response) {
            console.debug('  entry ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Remove spelling dictionary entry
        this.removeSpellingDictionaryEntry = function(translationId, entry) {
          console.debug('removeSpellingDictionaryEntry');
          var deferred = $q.defer();

          gpService.increment();
          $http['delete'](
            translationUrl + 'spelling/remove?translationId=' + translationId
              + '&entry=' + encodeURIComponent(entry)).then(
          // success
          function(response) {
            console.debug('  entry = ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Suggest spellings from dictionary for entry
        this.suggestSpelling = function(translationId, entry) {
          console.debug('suggestSpelling');
          var deferred = $q.defer();

          // suggest spelling
          gpService.increment();
          $http.get(
            translationUrl + 'spelling/suggest/' + translationId + '/'
              + encodeURIComponent(entry)).then(
          // success
          function(response) {
            console.debug('  suggest = ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Suggest unique spellings from dictionary for each of batch entries
        this.suggestBatchSpelling = function(translationId, lookupTerms) {
          console.debug('suggestBatchSpelling');
          var deferred = $q.defer();

          // INTENSIONALLY doesn't use gp
          // gpService.increment();
          $http.post(
            translationUrl + 'spelling/suggest/batch?translationId='
              + translationId, {
              strings : lookupTerms
            }).then(
          // success
          function(response) {
            console.debug('  batch suggest = ', response.data);
            // gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            // gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Suggest translations from phrase memory for entry
        this.suggestTranslation = function(translationId, entry) {
          console.debug('suggestTranslation');
          var deferred = $q.defer();

          // suggest translation
          gpService.increment();
          $http.get(
            translationUrl + 'phrasememory/suggest/' + translationId + '/'
              + encodeURIComponent(entry)).then(
          // success
          function(response) {
            console.debug('  suggest = ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Suggest unique translations from dictionary for each of batch entries
        this.suggestBatchTranslation = function(translationId, phrases) {
          console.debug('suggestBatchTranslation');
          var deferred = $q.defer();

          // INTENSIONALLY doesn't use gp
          // gpService.increment();
          $http.post(
            translationUrl + 'phrasememory/suggest/batch?translationId='
              + translationId, {
              strings : phrases
            }).then(
          // success
          function(response) {
            console.debug('  batch suggest = ', response.data);
            // gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            // gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Clear spelling dictionary
        this.clearSpellingDictionary = function(translationId) {
          console.debug('clearSpellingDictionary');
          var deferred = $q.defer();

          gpService.increment();
          $http['delete'](
            translationUrl + 'spelling/clear?translationId=' + translationId)
            .then(
            // success
            function(response) {
              console.debug('  spelling = ', response.data);
              gpService.decrement();
              deferred.resolve(response.data);
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        }

        // Get translations with phrase memory
        this.findTranslationsWithPhraseMemory = function() {
          console.debug('findTranslationsWithPhraseMemory');
          var deferred = $q.defer();

          gpService.increment();
          $http.get(translationUrl + 'translations/phrasememory').then(
          // success
          function(response) {
            console.debug('  handlers ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Copy spelling dictionary from one translation to another
        this.copyPhraseMemory = function(fromTranslationId, toTranslationId) {
          console.debug('copyPhraseMemory');
          var deferred = $q.defer();

          gpService.increment();
          $http.get(
            translationUrl + 'phrasememory/copy?fromTranslationId='
              + fromTranslationId + '&toTranslationId=' + toTranslationId)
            .then(
            // success
            function(response) {
              console.debug('  copy ', response.data);
              gpService.decrement();
              deferred.resolve(response.data);
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        }

        // Add new entry to phrase memory
        this.addPhraseMemoryEntry = function(translationId, name,
          translatedName) {
          console.debug('addPhraseMemoryEntry');
          var deferred = $q.defer();

          gpService.increment();
          $http.put(
            translationUrl + 'phrasememory/add?translationId=' + translationId
              + '&name=' + encodeURIComponent(name), translatedName, {
              headers : {
                'Content-type' : 'text/plain'
              }
            }).then(
          // success
          function(response) {
            console.debug('  memory entry = ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Remove phrase memory entry
        this.removePhraseMemoryEntry = function(translationId, name,
          translatedName) {
          console.debug('removePhraseMemoryEntry');
          var deferred = $q.defer();

          gpService.increment();
          $http['delete'](
            translationUrl + 'phrasememory/remove?translationId='
              + translationId + '&name=' + encodeURIComponent(name)
              + '&translatedName=' + encodeURIComponent(translatedName)).then(
          // success
          function(response) {
            console.debug('  removed ' + name);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Clear phrase memory
        this.clearPhraseMemory = function(translationId) {
          console.debug('clearPhraseMemory');
          var deferred = $q.defer();

          gpService.increment();
          $http['delete'](
            translationUrl + 'phrasememory/clear?translationId='
              + translationId).then(
          // success
          function(response) {
            console.debug('  phraseMemory = ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Compares two translations
        this.compareTranslations = function(translationId1, translationId2) {
          console.debug('compareTranslations');
          var deferred = $q.defer();

          gpService.increment();
          $http.get(
            translationUrl + 'compare?translationId1=' + translationId1
              + '&translationId2=' + translationId2).then(
          // success
          function(response) {
            console.debug('  compare ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Finds concepts in common
        this.findConceptsInCommon = function(reportToken, query, pfs) {
          console.debug('findConceptsInCommon');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            translationUrl + 'common/concepts?reportToken=' + reportToken
              + '&query=' + utilService.prepQuery(query),
            utilService.prepPfs(pfs)).then(
          // success
          function(response) {
            console.debug('  concepts ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Returns diff report
        this.getDiffReport = function(reportToken) {
          console.debug('getDiffReport');
          var deferred = $q.defer();

          gpService.increment();
          $http
            .get(translationUrl + 'diff/concepts?reportToken=' + reportToken)
            .then(
            // success
            function(response) {
              console.debug('  diff ', response.data);
              gpService.decrement();
              deferred.resolve(response.data);
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        }

        // Returns diff report
        this.releaseReportToken = function(reportToken) {
          console.debug('releaseReportToken');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            translationUrl + 'release/report?reportToken=' + reportToken).then(
          // success
          function(response) {
            console.debug('  release ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        this.addTranslationNote = function(translationId, note) {
          console.debug('add translation note', translationId, note);
          var deferred = $q.defer();

          // Add translation
          gpService.increment();
          $http.put(translationUrl + 'add/note?translationId=' + translationId,
            note, {
              headers : {
                'Content-type' : 'text/plain'
              }
            }).then(
          // success
          function(response) {
            console.debug('  note = ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        this.removeTranslationNote = function(translationId, noteId) {
          console.debug('remove translation note', translationId, noteId);
          var deferred = $q.defer();

          gpService.increment();
          $http['delete'](
            translationUrl + '/remove/note?translationId=' + translationId
              + '&noteId=' + noteId).then(
          // success
          function(response) {
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        this.addTranslationConceptNote = function(translationId, conceptId,
          note) {
          console.debug('add concept note', translationId, conceptId, note);
          var deferred = $q.defer();

          // Add translation
          gpService.increment();
          $http.put(
            translationUrl + 'concept/add/note?translationId=' + translationId
              + '&conceptId=' + conceptId, note, {
              headers : {
                'Content-type' : 'text/plain'
              }
            }).then(
          // success
          function(response) {
            console.debug('  note = ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        this.removeTranslationConceptNote = function(conceptId, noteId) {
          console.debug('remove concept note', conceptId, noteId);
          var deferred = $q.defer();

          gpService.increment();
          $http['delete'](
            translationUrl + '/concept/remove/note?conceptId=' + conceptId
              + '&noteId=' + noteId).then(
          // success
          function(response) {
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Export spelling dictionary
        this.exportSpellingDictionary = function(translation) {
          console.debug('exportSpellingDictionary');
          gpService.increment();
          $http.get(
            translationUrl + 'spelling/export?translationId=' + translation.id)
            .then(
            // Success
            function(response) {
              var blob = new Blob([ response.data ], {
                type : ''
              });

              // fake a file URL and download it
              var fileURL = URL.createObjectURL(blob);
              var a = document.createElement('a');
              a.href = fileURL;
              a.target = '_blank';
              a.download = 'spelling.' + translation.terminologyId + '.txt';
              document.body.appendChild(a);
              gpService.decrement();
              a.click();
              window.URL.revokeObjectURL(fileURL);

            },
            // Error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
            });
        };

        // Begin import spelling Dictionary - if validation is result, OK to
        // proceed.
        this.importSpellingDictionary = function(translationId, file) {
          console.debug('import spelling dictionary');
          var deferred = $q.defer();
          gpService.increment();
          Upload.upload(
            {
              url : translationUrl + 'spelling/import?translationId='
                + translationId,
              data : {
                file : file
              }
            })
            .then(
              // success
              function(response) {
                console.debug('  validation result = ', response.data);
                gpService.decrement();
                deferred.resolve(response.data);
              },
              // error
              function(response) {
                utilService.handleError(response);
                gpService.decrement();
                deferred.reject(response.data);
              },
              // event
              function(evt) {
                var progressPercentage = parseInt(100.0 * evt.loaded
                  / evt.total);
                console.debug('progress: ' + progressPercentage + '% '
                  + evt.config.data.file.name);
              });
          return deferred.promise;
        };

        // Export phrase memory
        this.exportPhraseMemory = function(translation) {
          console.debug('exportPhraseMemory');
          gpService.increment();
          $http.get(
            translationUrl + 'phrasememory/export?translationId='
              + translation.id).then(
          // Success
          function(response) {
            var blob = new Blob([ response.data ], {
              type : ''
            });

            // fake a file URL and download it
            var fileURL = URL.createObjectURL(blob);
            var a = document.createElement('a');
            a.href = fileURL;
            a.target = '_blank';
            a.download = 'phraseMemory.' + translation.terminologyId + '.txt';
            document.body.appendChild(a);
            gpService.decrement();
            a.click();
            window.URL.revokeObjectURL(fileURL);

          },
          // Error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
          });
        };

        // Export concepts
        this.exportConcepts = function(translation, handler) {
          console.debug('exportConcepts');

          gpService.increment();
          $http.get(
            translationUrl + 'export?translationId=' + translation.id
              + '&handlerId=' + handler.id, {
              responseType : 'arraybuffer'
            }).then(
            // Success
            function(response) {
              var blob = new Blob([ response.data ], {
                type : "application/octet-stream"
              });

              // fake a file URL and download it
              var fileURL = URL.createObjectURL(blob);
              var a = document.createElement('a');
              a.href = fileURL;
              a.target = '_blank';
              a.download = 'concepts.' + translation.terminologyId
                + handler.fileTypeFilter;
              document.body.appendChild(a);
              gpService.decrement();
              a.click();
              window.URL.revokeObjectURL(fileURL);

            },
            // Error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
            });
        };

        // Begin import concepts - if validation is result, OK to proceed.
        this.beginImportConcepts = function(translationId, handlerId) {
          console.debug('begin import concepts');
          var deferred = $q.defer();
          gpService.increment()
          $http.get(
            translationUrl + 'import/begin?translationId=' + translationId
              + '&handlerId=' + handlerId).then(
          // success
          function(response) {
            console.debug('  validation result = ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        };

        // Cancel import concepts
        this.cancelImportConcepts = function(translationId) {
          console.debug('cancel import concepts');
          var deferred = $q.defer();
          gpService.increment()
          $http.get(
            translationUrl + 'import/cancel?translationId=' + translationId)
            .then(
            // success
            function(response) {
              gpService.decrement();
              deferred.resolve(response.data);
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        };

        // Finish import concepts - if validation is result, OK to proceed.
        this.finishImportConcepts = function(translationId, handlerId, file) {
          console.debug('finish import concepts');
          var deferred = $q.defer();
          gpService.increment()
          Upload.upload(
            {
              url : translationUrl + 'import/finish?translationId='
                + translationId + '&handlerId=' + handlerId,
              data : {
                file : file
              }
            })
            .then(
              // Success
              function(response) {
                gpService.decrement();
                deferred.resolve(response.data);

              },
              // error
              function(response) {
                utilService.handleError(response);
                gpService.decrement();
                deferred.reject(response.data);
              },
              // event
              function(evt) {
                var progressPercentage = parseInt(100.0 * evt.loaded
                  / evt.total);
                console.debug('progress: ' + progressPercentage + '% '
                  + evt.config.data.file.name);
              });
          return deferred.promise;
        };
        // Begin import phrase memory
        this.importPhraseMemory = function(translationId, file) {
          console.debug('import phrase memory');
          var deferred = $q.defer();
          gpService.increment();
          Upload.upload(
            {
              url : translationUrl + 'phrasememory/import?translationId='
                + translationId,
              data : {
                file : file
              }
            })
            .then(
              // success
              function(response) {
                console.debug('  validation result = ', response.data);
                gpService.decrement();
                deferred.resolve(response.data);
              },
              // error
              function(response) {
                utilService.handleError(response);
                gpService.decrement();
                deferred.reject(response.data);
              },
              // event
              function(evt) {
                var progressPercentage = parseInt(100.0 * evt.loaded
                  / evt.total);
                console.debug('progress: ' + progressPercentage + '% '
                  + evt.config.data.file.name);
              });
          return deferred.promise;
        };

        // get the progress of the name/status concept lookup process
        this.getLookupProgress = function(translationId) {
          console.debug('getLookupProgress');
          // Setup deferred
          var deferred = $q.defer();

          $http.get(
            translationUrl + 'lookup/status?translationId=' + translationId, {
              headers : {
                'Content-type' : 'text/plain'
              }
            }).then(
          // success
          function(response) {
            console.debug('  output = ', response.data);
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // start lookup of concept names/statuses
        this.startLookup = function(translationId) {
          console.debug('startLookup');
          var deferred = $q.defer();

          // get translation revision
          $http.get(
            translationUrl + 'lookup/start?translationId=' + translationId)
            .then(
            // success
            function(response) {
              console.debug('  start lookup names = ', response.data);
              deferred.resolve(response.data);
            },
            // error
            function(response) {
              utilService.handleError(response);
              deferred.reject(response.data);
            });
          return deferred.promise;
        }

        // get the available language description types
        this.getLanguageDescriptionTypes = function() {
          console.debug('getLanguageDescriptionTypes');
          // Setup deferred
          var deferred = $q.defer();

          gpService.increment();
          $http.get(translationUrl + 'langpref').then(
          // success
          function(response) {
            console.debug('  language desc types = ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // end

      } ]);
