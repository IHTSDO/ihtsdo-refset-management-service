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
        releaseUrl + 'refset/info' + "?refsetId=" + refsetId).then(
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
    
    // retrieve current translation release info
    this.getCurrentTranslationRelease = function(translationId) {
      console.debug("getCurrentTranslationRelease");
      var deferred = $q.defer();

      // Assign user to project
      gpService.increment()
      $http.get(
        releaseUrl + 'translation/info' + "?translationId=" + translationId).then(
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

    this.exportReleaseArtifact = function(releaseArtifact) {
      gpService.increment()
      $http({
          url : releaseUrl + "export/"
          + releaseArtifact.id ,
          dataType : "json",
          method : "GET",
          headers : {
            "Content-Type" : "application/json"
          },
          responseType: 'arraybuffer'
      }).success(function(data) {
        //$scope.definitionMsg = "Successfully exported report";
        var blob = new Blob([data], {type: ""});

        // hack to download store a file having its URL
        var fileURL = URL.createObjectURL(blob);
        var a         = document.createElement('a');
        a.href        = fileURL; 
        a.target      = "_blank";
        a.download    = releaseArtifact.name;
        document.body.appendChild(a);
        gpService.decrement();
        a.click();

      }).error(function(response) {
        utilService.handleError(response);
        gpService.decrement();
        //deferred.reject(response.data);
      });
  };
  
  } ]);
