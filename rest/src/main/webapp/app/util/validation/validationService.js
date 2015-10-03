// Validation Service
tsApp.service('validationService', [ '$http', '$q', 'gpService', 'utilService',
  function($http, $q, gpService, utilService) {
    console.debug("configure validationService");

    // validate concept
    this.validateConcept = function(concept) {
      console.debug("validateConcept");
      var deferred = $q.defer();

      // Add concept
      gpService.increment()
      $http.post(projectUrl + 'concept', concept).then(
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
      $http.post(projectUrl + 'refset', refset).then(
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
      $http.post(projectUrl + 'translation', translation).then(
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
    this.validateSimpleRefsetMember = function(member) {
      console.debug("validateSimpleRefsetMember");
      var deferred = $q.defer();

      // Add translation
      gpService.increment()
      $http.post(projectUrl + 'member', member).then(
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

  } ]);
