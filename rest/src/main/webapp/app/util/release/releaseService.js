// Release Service
tsApp.service('releaseService', [
  '$http',
  '$q',
  'gpService',
  'utilService',
  function($http, $q, gpService, utilService) {
    console.debug("configure releaseService");

    // Get release history for refsetId
    this.findRefsetReleasesForQuery = function(refsetId, query, pfs) {
      console.debug("findRefsetReleasesForQuery");
      var deferred = $q.defer();

      // Get release history for refsetId
      gpService.increment()
      $http
        .get(releaseUrl + 'refset' + "/" + refsetId + "?query=" + query, pfs)
        .then(
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
    this.findTranslationReleasesForQuery = function(translationId, query, pfs) {
      console.debug("findTranslationReleasesForQuery");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(
        releaseUrl + 'translation' + "/" + translationId + "?query=" + query,
        pfs).then(
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

    // Validate refset release
    this.validateRefsetRelease = function(refsetId) {
      console.debug("validateRefsetRelease");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(
        releaseUrl + 'refset' + "/" + 'validate' + "?refsetId=" + refsetId)
        .then(
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

    // Preview refset release
    this.previewRefsetRelease = function(refsetId, ioHandlerId) {
      console.debug("previewRefsetRelease");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(
        releaseUrl + 'refset' + "/" + 'preview' + "?refsetId=" + refsetId
          + "&ioHandlerId=" + ioHandlerId).then(
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

    // Validate translation release
    this.validateTranslationRelease = function(translationId) {
      console.debug("validateTranslationRelease");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(
        releaseUrl + 'translation' + "/" + 'validate' + "?translationId="
          + translationId).then(
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

    // Preview translation release
    this.previewTranslationRelease = function(translationId, ioHandlerId) {
      console.debug("previewTranslationRelease");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(
        releaseUrl + 'translation' + "/" + 'preview' + "?translationId="
          + translationId + "&ioHandlerId=" + ioHandlerId).then(
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

    // Retrieves current refset release
    this.getCurrentReleaseInfoForRefset = function(refsetId) {
      console.debug("getCurrentReleaseInfoForRefset");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(releaseUrl + 'refset/info' + "?refsetId=" + refsetId).then(
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

    // Retrieves current translation release info
    this.getCurrentReleaseInfoForTranslation = function(translationId) {
      console.debug("getCurrentReleaseInfoForTranslation");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(
        releaseUrl + 'translation/info' + "?translationId=" + translationId)
        .then(
        // success
        function(response) {
          console.debug("  translation info = ", response.data);
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

      gpService.increment()
      $http.get(
        releaseUrl + 'translation/info' + "?translationId=" + translationId)
        .then(
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
    
    // begin refset release
    this.beginRefsetRelease = function(refsetId, effectiveTime) {
      console.debug("beginRefsetRelease");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(
        releaseUrl + 'refset/begin' + "?refsetId=" + refsetId + "&effectiveTime=" + effectiveTime)
        .then(
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
    
    // preview refset release
    this.previewRefsetRelease = function(refsetId, ioHandlerId) {
      console.debug("previewRefsetRelease");
      var deferred = $q.defer();

      gpService.increment()
      $http.get(
        releaseUrl + 'refset/preview' + "?refsetId=" + refsetId + "&ioHandlerId=" + ioHandlerId)
        .then(
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

    // Remove release artifact
    this.removeReleaseArtifact = function(artifactId) {
      console.debug("removeReleaseArtifact");
      var deferred = $q.defer();

      gpService.increment()
      $http['delete'](
        releaseUrl + 'remove' + "/" + 'artifact' + "/" + artifactId).then(
      // success
      function(response) {
        console.debug("  artifact = ", response.data);
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
        url : releaseUrl + "export/" + releaseArtifact.id,
        dataType : "json",
        method : "GET",
        headers : {
          "Content-Type" : "application/json"
        },
        responseType : 'arraybuffer'
      }).success(function(data) {
        var blob = new Blob([ data ], {
          type : ""
        });

        // hack to download store a file having its URL
        var fileURL = URL.createObjectURL(blob);
        var a = document.createElement('a');
        a.href = fileURL;
        a.target = "_blank";
        a.download = releaseArtifact.name;
        document.body.appendChild(a);
        gpService.decrement();
        a.click();

      }).error(function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
    };

  } ]);
