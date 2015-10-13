// Validation Service
tsApp.service('validationService', [
  '$http',
  '$q',
  'gpService',
  'utilService',
  function($http, $q, gpService, utilService) {
    console.debug("configure validationService");

    // validate concept
    this.validateConcept = function(concept) {
      console.debug("validateConcept");
      var deferred = $q.defer();

      // validate concept
      gpService.increment()
      $http.post(validationUrl + 'concept', concept).then(
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

    // validate refset
    this.validateRefset = function(refset) {
      console.debug("validateRefset");
      var deferred = $q.defer();

      // Add refset
      gpService.increment()
      $http.post(validationUrl + 'refset', refset).then(
      // success
      function(response) {
        console.debug("  refset = ", response.data);
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

    // validate translation
    this.validateTranslation = function(translation) {
      console.debug("validateTranslation");
      var deferred = $q.defer();

      // Add translation
      gpService.increment()
      $http.post(validationUrl + 'translation', translation).then(
      // success
      function(response) {
        console.debug("  translation = ", response.data);
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

    // validate member
    this.validateMember = function(member) {
      console.debug("validateMember");
      var deferred = $q.defer();

      gpService.increment()
      $http.post(validationUrl + 'member', member).then(
      // success
      function(response) {
        console.debug("  member = ", response.data);
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

    // Validate all translation concept member
    this.validateAllConcepts = function(translationId) {
      console.debug("validateAllConcepts");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(validationUrl + 'concepts' + "?translationId=" + translationId)
        .then(
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

    // Validate all refset members
    this.validateAllMembers = function(refsetId) {
      console.debug("validateAllMembers");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(validationUrl + 'members' + "?refsetId=" + refsetId).then(
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

  } ]);
