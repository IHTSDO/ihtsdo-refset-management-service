// Release Service
tsApp.service('releaseService', [ '$http', '$q', 'gpService', 'utilService',
  function($http, $q, gpService, utilService) {
    console.debug("configure releaseService");

    // Get release history for refsetId
    this.getReleaseHistoryForRefset = function(refsetId) {
      console.debug("getReleaseHistoryForRefset");
      var deferred = $q.defer();

      // Get release history for refsetId
      gpService.increment()
      $http.get(releaseUrl + 'refset' + "/" + refsetId).then(
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
      });
      return deferred.promise;
    }

    // Get release history for translationId
    this.getReleaseHistoryForTranslation = function(translationId) {
      console.debug("getReleaseHistoryForTranslation");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(releaseUrl + 'translation' + "/" + translationId).then(
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
      });
      return deferred.promise;
    }

    // retrieve current refset release info
    this.getCurrentRefsetRelease = function(refsetId) {
      console.debug("getCurrentRefsetRelease");
      var deferred = $q.defer();

      // Assign user to project
      gpService.increment()
      $http.get(
        releaseUrl + 'info' + "?refsetId=" + refsetId).then(
      // success
      function(response) {
        console.debug("  release info = ", response.data);
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
