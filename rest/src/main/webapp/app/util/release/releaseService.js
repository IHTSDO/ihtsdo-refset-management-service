// Release Service
tsApp.service('releaseService', [
  '$http',
  '$q',
  'gpService',
  'utilService',
  function($http, $q, gpService, utilService) {
    console.debug('configure releaseService');

    // Get release history for refsetId
    this.findRefsetReleasesForQuery = function(refsetId, query, pfs) {
      console.debug('findRefsetReleasesForQuery');
      var deferred = $q.defer();

      // Get release history for refsetId
      gpService.increment();
      $http.post(
        releaseUrl + 'refset?refsetId=' + refsetId + (query != null ? '&query=' + query : ''), pfs)
        .then(
        // success
        function(response) {
          console.debug('  refset = ', response.data);
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

    // Get release history for translationId
    this.findTranslationReleasesForQuery = function(translationId, query, pfs) {
      console.debug('findTranslationReleasesForQuery');
      var deferred = $q.defer();

      gpService.increment();
      $http.post(
        releaseUrl + 'translation?translationId=' + translationId
          + (query != null ? '&query=' + query : ''), pfs).then(
      // success
      function(response) {
        console.debug('  translation = ', response.data);
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

    // Validate refset release
    this.validateRefsetRelease = function(refsetId) {
      console.debug('validateRefsetRelease');
      var deferred = $q.defer();

      gpService.increment();
      $http.get(releaseUrl + 'refset/validate?refsetId=' + refsetId).then(
      // success
      function(response) {
        console.debug('  refset = ', response.data);
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
    
    // Validate refset release
    this.validateRefsetReleases = function(refsetIds) {
      console.debug('validateRefsetReleases');
      var deferred = $q.defer();

      gpService.increment();
      $http.post(releaseUrl + 'refsets/validate', refsetIds).then(
      // success
      function(response) {
        console.debug('  validate releases finished ');
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
    

    this.findCurrentRefsetReleaseInfo = function(refsetId) {
      console.debug('findCurrentRefsetReleaseInfo');
      var deferred = $q.defer();

      gpService.increment();
      $http.get(releaseUrl + 'refset/info?refsetId=' + refsetId).then(
      // success
      function(response) {
        console.debug('  refset = ', response.data);
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

    // Beta refset release
    this.betaRefsetRelease = function(refsetId, ioHandlerId) {
      console.debug('betaRefsetRelease');
      var deferred = $q.defer();

      gpService.increment();
      $http.get(releaseUrl + 'refset/beta?refsetId=' + refsetId + '&ioHandlerId=' + ioHandlerId)
        .then(
        // success
        function(response) {
          console.debug('  refset = ', response.data);
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

    // Beta refset release
    this.betaRefsetReleases = function(refsetIds, ioHandlerId) {
      console.debug('betaRefsetReleases');
      var deferred = $q.defer();

      gpService.increment();
      $http.post(releaseUrl + 'refsets/beta?ioHandlerId=' + ioHandlerId, refsetIds)
        .then(
        // success
        function(response) {
          console.debug('  beta releases finished ');
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
    
    // Validate translation release
    this.validateTranslationRelease = function(translationId) {
      console.debug('validateTranslationRelease');
      var deferred = $q.defer();

      gpService.increment();
      $http.get(releaseUrl + 'translation/validate?translationId=' + translationId).then(
      // success
      function(response) {
        console.debug('  translation = ', response.data);
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

    // Beta translation release
    this.betaTranslationRelease = function(translationId, ioHandlerId) {
      console.debug('betaTranslationRelease');
      var deferred = $q.defer();

      gpService.increment();
      $http.get(
        releaseUrl + 'translation/beta?translationId=' + translationId + '&ioHandlerId='
          + ioHandlerId).then(
      // success
      function(response) {
        console.debug('  translation = ', response.data);
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

    // Retrieves current refset release
    this.getCurrentRefsetReleaseInfo = function(refsetId) {
      console.debug('getCurrentRefsetReleaseInfo');
      var deferred = $q.defer();

      gpService.increment();
      $http.get(releaseUrl + 'refset/info?refsetId=' + refsetId).then(
      // success
      function(response) {
        console.debug('  release info = ', response.data);
        // Service sends back an empty container - for client layer
        if (response.data && !response.data.id) {
          response.data = null;
        }
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

    // Retrieves current translation release info
    this.getCurrentTranslationReleaseInfo = function(translationId) {
      console.debug('getCurrentTranslationReleaseInfo');
      var deferred = $q.defer();

      gpService.increment();
      $http.get(releaseUrl + 'translation/info?translationId=' + translationId).then(
      // success
      function(response) {
        console.debug('  translation info = ', response.data);
        // Service sends back an empty container - for client layer
        if (response.data && !response.data.id) {
          response.data = null;
        }
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

    // begin refset release
    this.beginRefsetRelease = function(refsetId, effectiveTime) {
      console.debug('beginRefsetRelease');
      var deferred = $q.defer();

      gpService.increment();
      $http.get(
        releaseUrl + 'refset/begin?refsetId=' + refsetId + '&effectiveTime=' + effectiveTime).then(
      // success
      function(response) {
        console.debug('  release info = ', response.data);
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
    
    // begin release for list of refsets
    this.beginRefsetReleases = function(refsetIds, effectiveTime) {
      console.debug('beginRefsetReleases');
      var deferred = $q.defer();

      gpService.increment();      
      $http.post(
        releaseUrl + 'refsets/begin?effectiveTime=' + effectiveTime, refsetIds).then(
      // success
      function(response) {
        console.debug('  begin releases finished ');
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

    // begin translation release
    this.beginTranslationRelease = function(translationId, effectiveTime) {
      console.debug('beginTranslationRelease');
      var deferred = $q.defer();

      gpService.increment();
      $http.get(
        releaseUrl + 'translation/begin?translationId=' + translationId + '&effectiveTime='
          + effectiveTime).then(
      // success
      function(response) {
        console.debug('  release info = ', response.data);
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

    // beta refset release
    this.betaRefsetRelease = function(refsetId, ioHandlerId) {
      console.debug('betaRefsetRelease');
      var deferred = $q.defer();

      gpService.increment();
      $http.get(releaseUrl + 'refset/beta?refsetId=' + refsetId + '&ioHandlerId=' + ioHandlerId)
        .then(
        // success
        function(response) {
          console.debug('  release info = ', response.data);
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

    this.resumeRelease = function(refsetId) {
      console.debug('resumeRelease');
      var deferred = $q.defer();

      // get refset revision
      gpService.increment();
      $http.get(refsetUrl + 'release/resume?refsetId=' + refsetId).then(
      // success
      function(response) {
        console.debug('  resume refset release = ', response.data);
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

    this.cancelRefsetRelease = function(refsetId) {
      console.debug('cancelRefsetRelease');
      var deferred = $q.defer();

      gpService.increment();
      $http.get(releaseUrl + 'refset/cancel?refsetId=' + refsetId).then(
      // success
      function(response) {
        console.debug('  cancel refset release = ', response.data);
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
    
    this.cancelRefsetReleases = function(refsetIds) {
      console.debug('cancelRefsetReleases');
      var deferred = $q.defer();

      gpService.increment();
      $http.post(releaseUrl + 'refsets/cancel', refsetIds).then(
      // success
      function(response) {
        console.debug('  cancel refset releases finished ');
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

    this.cancelTranslationRelease = function(translationId) {
      console.debug('cancelTranslationRelease');
      var deferred = $q.defer();

      // get translation revision
      gpService.increment();
      $http.get(releaseUrl + 'translation/cancel?translationId=' + translationId).then(
      // success
      function(response) {
        console.debug('  cancel translation release = ', response.data);
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

    this.finishRefsetRelease = function(refsetId) {
      console.debug('finishRefsetRelease');
      var deferred = $q.defer();

      // get refset revision
      gpService.increment();
      $http.get(releaseUrl + 'refset/finish?refsetId=' + refsetId).then(
      // success
      function(response) {
        console.debug('  finish refset release = ', response.data);
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

    this.finishRefsetReleases = function(refsetIds) {
      console.debug('finishRefsetReleases');
      var deferred = $q.defer();

      // get refset revision
      gpService.increment();
      $http.post(releaseUrl + 'refsets/finish', refsetIds).then(
      // success
      function(response) {
        console.debug('  finish releases finished ');
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
    
    // Remove release artifact
    this.removeReleaseArtifact = function(artifactId) {
      console.debug('removeReleaseArtifact');
      var deferred = $q.defer();

      gpService.increment();
      $http['delete'](releaseUrl + 'remove/artifact/' + artifactId).then(
      // success
      function(response) {
        console.debug('  artifact = ', response.data);
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

    this.finishTranslationRelease = function(translationId) {
      console.debug('finishTranslationRelease');
      var deferred = $q.defer();

      // get translation revision
      gpService.increment();
      $http.get(releaseUrl + 'translation/finish?translationId=' + translationId).then(
      // success
      function(response) {
        console.debug('  finish translation release = ', response.data);
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
    // Remove release artifact
    this.removeReleaseArtifact = function(artifactId) {
      console.debug('removeReleaseArtifact');
      var deferred = $q.defer();

      gpService.increment();
      $http['delete'](releaseUrl + 'remove/artifact/' + artifactId).then(
      // success
      function(response) {
        console.debug('  artifact = ', response.data);
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

    // Begin import release artifact- if validation is result, OK to proceed.
    this.importReleaseArtifact = function(releaseInfoId, file) {
      console.debug('import release artifact');
      var deferred = $q.defer();
      gpService.increment();
      Upload.upload({
        url : translationUrl + 'import/artifact?releaseInfoId=' + releaseInfoId.id,
        data : {
          file : file
        }
      }).then(
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
        var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
        console.debug('progress: ' + progressPercentage + '% ' + evt.config.data.file.name);
      });
      return deferred.promise;
    };

    // Export a release artifact and prompt the download, no promise returned
    this.exportReleaseArtifact = function(releaseArtifact) {
      gpService.increment();
      $http.get(releaseUrl + 'export/' + releaseArtifact.id, {
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
        a.download = releaseArtifact.name;
        document.body.appendChild(a);
        gpService.decrement();
        a.click();
      },
      // Error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
    };
    
    // find all refsets that are still progressing through specified bulk process
    this.getBulkProcessProgress = function(process, refsetIds) {
      console.debug('getBulkProcessProgress');
      // Setup deferred
      var deferred = $q.defer();

      $http.post(releaseUrl + 'lookup/progress?process=' + process, refsetIds).then(
      // success
      function(response) {
        console.debug('  process progress returned ');
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        deferred.reject(response.data);
      });
      return deferred.promise;
    };    

  } ]);
