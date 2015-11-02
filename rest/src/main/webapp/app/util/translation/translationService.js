// Translation Service
tsApp.service('translationService', [
  '$http',
  '$q',
  'gpService',
  'utilService',
  function($http, $q, gpService, utilService) {
    console.debug("configure translationService");

    // Get translation for id and date
    this.getTranslationRevision = function(translationId, date) {
      console.debug("getTranslationRevision");
      var deferred = $q.defer();

      // Get translation for id and date
      gpService.increment()
      $http.get(translationUrl + translationId + "/" + date).then(
      // success
      function(response) {
        console.debug("  translation ", response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
      return deferred.promise;
    }

    // Finds concepts for translation revision
    this.findTranslationRevisionConceptsForQuery = function(translationId,
      date, pfs) {
      console.debug("findTranslationRevisionConceptsForQuery");
      var deferred = $q.defer();

      // Finds concepts for translation revision
      gpService.increment()
      $http.post(
        translationUrl + translationId + "/" + date + "/" + 'concepts', pfs)
        .then(
        // success
        function(response) {
          console.debug("  concepts ", response.data);
          gpService.decrement();
          deferred.resolve(response.data);
        },
        // error
        function(response) {
          utilService.handleError(response);
          gpService.decrement();
        });
      return deferred.promise;
    }

    // Get translation for id
    this.getTranslation = function(translationId) {
      console.debug("getTranslation");
      var deferred = $q.defer();

      // Get translation for id
      gpService.increment()
      $http.get(translationUrl + translationId).then(
      // success
      function(response) {
        console.debug("  translation ", response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
      return deferred.promise;
    }

    // Get translation for refset
    this.getTranslationsForRefset = function(refsetId) {
      console.debug("getTranslationsForRefset");
      var deferred = $q.defer();

      // Get translation for id
      gpService.increment()
      $http.get(translationUrl + 'translations' + "/" + refsetId).then(
      // success
      function(response) {
        console.debug("  translations ", response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
      return deferred.promise;
    }

    // Finds translations
    this.findTranslationsForQuery = function(query, pfs) {
      console.debug("findTranslationsForQuery");
      var deferred = $q.defer();

      // Finds translations
      gpService.increment()
      $http.post(translationUrl + 'translations' + "?query=" + query, pfs)
        .then(
        // success
        function(response) {
          console.debug("  translations ", response.data);
          gpService.decrement();
          deferred.resolve(response.data);
        },
        // error
        function(response) {
          utilService.handleError(response);
          gpService.decrement();
        });
      return deferred.promise;
    }

    // Add new translation
    this.addTranslation = function(translation) {
      console.debug("addTranslation");
      var deferred = $q.defer();

      // Add new translation
      gpService.increment()
      $http.put(translationUrl + 'add', translation).then(
      // success
      function(response) {
        console.debug("  translation ", response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
      return deferred.promise;
    }

    // Update translation
    this.updateTranslation = function(translation) {
      console.debug("updateTranslation");
      var deferred = $q.defer();

      // Update translation
      gpService.increment()
      $http.post(translationUrl + 'update', translation).then(
      // success
      function(response) {
        console.debug("  translation ", response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
      return deferred.promise;
    }

    // Remove translation
    this.removeTranslation = function(translationId) {
      console.debug("removeTranslation");
      var deferred = $q.defer();

      // Remove translation
      gpService.increment()
      $http['delete'](translationUrl + 'remove' + "/" + translationId).then(
      // success
      function(response) {
        console.debug("  project = ", response.data);
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
    this.findTranslationConceptsForQuery = function(translationId, query, pfs) {
      console.debug("findTranslationConceptsForQuery");
      var deferred = $q.defer();

      // find concepts
      gpService.increment()
      $http.post(
        translationUrl + "concepts" + "?query=" + query + "&translationId="
          + translationId, pfs).then(
      // success
      function(response) {
        console.debug("  concepts = ", response.data);
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
      console.debug("getImportTranslationHandlers");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(translationUrl + 'import' + "/" + 'handlers').then(
      // success
      function(response) {
        console.debug("  handlers ", response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
      return deferred.promise;
    }

    // Get export translation handlers
    this.getExportTranslationHandlers = function() {
      console.debug("getExportTranslationHandlers");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(translationUrl + 'export' + "/" + 'handlers').then(
      // success
      function(response) {
        console.debug("  handlers ", response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
      return deferred.promise;
    }

    // Add new translation concept
    this.addTranslationConcept = function(concept) {
      console.debug("addTranslationConcept");
      var deferred = $q.defer();

      gpService.increment()
      $http.put(translationUrl + 'concept' + "/" + 'add', concept).then(
      // success
      function(response) {
        console.debug("  concept ", response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
      return deferred.promise;
    }

    // Remove translation concept
    this.removeTranslationConcept = function(conceptId) {
      console.debug("removeTranslationConcept");
      var deferred = $q.defer();

      gpService.increment()
      $http['delete'](
        translationUrl + 'concept' + "/" + 'remove' + "/" + conceptId).then(
      // success
      function(response) {
        console.debug("  concept = ", response.data);
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
      console.debug("getTranslationsWithSpellingDictionary");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(translationUrl + 'translations' + "/" + 'spellingdictionary')
        .then(
        // success
        function(response) {
          console.debug("  handlers ", response.data);
          gpService.decrement();
          deferred.resolve(response.data);
        },
        // error
        function(response) {
          utilService.handleError(response);
          gpService.decrement();
        });
      return deferred.promise;
    }

    // Copy spelling dictionary from one translation to another
    this.copySpellingDictionary = function(fromTranslationId, toTranslationId) {
      console.debug("copySpellingDictionary");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(
        translationUrl + 'spelling' + "/" + 'copy' + "/" + fromTranslationId
          + "/" + toTranslationId).then(
      // success
      function(response) {
        console.debug("  copy ", response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
      return deferred.promise;
    }

    // Add spelling dictionary entry
    this.addSpellingDictionaryEntry = function(translationId, entry) {
      console.debug("addSpellingDictionaryEntry");
      var deferred = $q.defer();

      gpService.increment()
      $http.put(
        translationUrl + translationId + "/" + 'spelling' + "/" + 'add' + "/"
          + entry).then(
      // success
      function(response) {
        console.debug("  entry ", response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
      return deferred.promise;
    }

    // Remove spelling dictionary entry
    this.removeSpellingDictionaryEntry = function(translationId, entry) {
      console.debug("removeSpellingDictionaryEntry");
      var deferred = $q.defer();

      gpService.increment()
      $http['delete'](
        translationUrl + translationId + "/" + 'spelling' + "/" + 'remove'
          + "/" + entry).then(
      // success
      function(response) {
        console.debug("  entry = ", response.data);
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

    // Clear spelling dictionary
    this.clearSpellingDictionary = function(translationId) {
      console.debug("clearSpellingDictionary");
      var deferred = $q.defer();

      gpService.increment()
      $http['delete'](
        translationUrl + translationId + "/" + 'spelling' + "/" + 'clear')
        .then(
        // success
        function(response) {
          console.debug("  spelling = ", response.data);
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
      console.debug("findTranslationsWithPhraseMemory");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(translationUrl + 'translations' + "/" + 'phrasememory').then(
      // success
      function(response) {
        console.debug("  handlers ", response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
      return deferred.promise;
    }

    // Copy spelling dictionary from one translation to another
    this.copyPhraseMemory = function(fromTranslationId, toTranslationId) {
      console.debug("copyPhraseMemory");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(
        translationUrl + 'phrasememory' + "/" + 'copy' + "/"
          + fromTranslationId + "/" + toTranslationId).then(
      // success
      function(response) {
        console.debug("  copy ", response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
      return deferred.promise;
    }

    // Add new entry to phrase memory
    this.addPhraseMemoryEntry = function(translationId, entry) {
      console.debug("addPhraseMemoryEntry");
      var deferred = $q.defer();

      gpService.increment()
      $http.put(
        translationUrl + translationId + "/" + 'phrasememory' + "/" + 'add',
        entry).then(
      // success
      function(response) {
        console.debug("  entry ", response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
      return deferred.promise;
    }

    // Remove phrase memory entry
    this.removePhraseMemoryEntry = function(translationId, entryId) {
      console.debug("removePhraseMemoryEntry");
      var deferred = $q.defer();

      gpService.increment()
      $http['delete'](
        translationUrl + translationId + "/" + 'phrasememory' + "/" + 'remove'
          + "/" + entryId).then(
      // success
      function(response) {
        console.debug("  entryId = ", response.data);
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
      console.debug("clearPhraseMemory");
      var deferred = $q.defer();

      gpService.increment()
      $http['delete'](
        translationUrl + translationId + "/" + 'phraseMemory' + "/" + 'clear')
        .then(
        // success
        function(response) {
          console.debug("  phraseMemory = ", response.data);
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
      console.debug("compareTranslations");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(
        translationUrl + 'compare' + "?translationId1=" + translationId1
          + "&translationId2=" + translationId2).then(
      // success
      function(response) {
        console.debug("  compare ", response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
      return deferred.promise;
    }

    // Finds concepts in common
    this.findConceptsInCommon = function(reportToken, query, pfs) {
      console.debug("findConceptsInCommon");
      var deferred = $q.defer();

      gpService.increment()
      $http.post(
        translationUrl + 'common' + "/" + 'concepts' + "?reportToken="
          + reportToken + "&query=" + query, pfs).then(
      // success
      function(response) {
        console.debug("  concepts ", response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
      return deferred.promise;
    }

    // Returns diff report
    this.getDiffReport = function(reportToken) {
      console.debug("getDiffReport");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(
        translationUrl + "diff/concepts" + "?reportToken=" + reportToken).then(
      // success
      function(response) {
        console.debug("  diff ", response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
      return deferred.promise;
    }

    // Returns diff report
    this.releaseReportToken = function(reportToken) {
      console.debug("releaseReportToken");
      var deferred = $q.defer();

      gpService.increment()
      $http.post(
        translationUrl + "release/report" + "?reportToken=" + reportToken)
        .then(
        // success
        function(response) {
          console.debug("  release ", response.data);
          gpService.decrement();
          deferred.resolve(response.data);
        },
        // error
        function(response) {
          utilService.handleError(response);
          gpService.decrement();
        });
      return deferred.promise;
    }

  } ]);
