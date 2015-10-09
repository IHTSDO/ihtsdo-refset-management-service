// Translation Service
tsApp.service('translationService',
  [
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
      this.findConceptsForTranslationRevision = function(translationId, date,
        pfs) {
        console.debug("findConceptsForTranslationRevision");
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
        $http.post(translationUrl + 'translations' + "?query=" + query, pfs).then(
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

    } ]);
