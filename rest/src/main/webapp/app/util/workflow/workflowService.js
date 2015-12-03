// Workflow Service
tsApp.service('workflowService', [
  '$http',
  '$q',
  'gpService',
  'utilService',
  function($http, $q, gpService, utilService) {
    console.debug("configure workflowService");

    // Get workflow paths
    this.getWorkflowPaths = function() {
      console.debug("getWorkflowPaths");
      var deferred = $q.defer();

      // Perform workflow action on a refset
      gpService.increment()
      $http.get(workflowUrl + 'paths').then(
      // success
      function(response) {
        console.debug("  paths = ", response.data);
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

    // Perform workflow action on a refset
    this.performWorkflowAction = function(projectId, refsetId, userName, action) {
      console.debug("performWorkflowAction");
      var deferred = $q.defer();

      // Perform workflow action on a refset
      gpService.increment()
      $http.get(
        workflowUrl + 'refset' + "/" + action + "?refsetId=" + refsetId + "&userName=" + userName
          + "&projectId=" + projectId).then(
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

    // Find available translation editing work
    this.findAvailableEditingConcepts = function(projectId, translationId, userName, pfs) {
      console.debug("findAvailableEditingConcepts");
      var deferred = $q.defer();

      // Find available editing work
      gpService.increment()
      $http.post(
        workflowUrl + "translation/available/editing" + "?projectId=" + projectId
          + "&translationId=" + translationId + "&userName=" + userName, pfs).then(
      // success
      function(response) {
        console.debug("  work = ", response.data);
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

    // Find assigned translation editing work
    this.findAssignedEditingConcepts = function(projectId, translationId, userName, pfs) {
      console.debug("findAssignedEditingConcepts");
      var deferred = $q.defer();

      // Find assigned editing work
      gpService.increment()
      $http.post(
        workflowUrl + "translation/assigned/editing" + "?projectId=" + projectId
          + "&translationId=" + translationId + "&userName=" + userName, pfs).then(
      // success
      function(response) {
        console.debug("  work = ", response.data);
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

    // Find available translation review work
    this.findAvailableReviewConcepts = function(projectId, translationId, userName, pfs) {
      console.debug("findAvailableReviewConcepts");
      var deferred = $q.defer();

      // Find available review work
      gpService.increment()
      $http.post(
        workflowUrl + "translation/available/review" + "?projectId=" + projectId
          + "&translationId=" + translationId + "&userName=" + userName, pfs).then(
      // success
      function(response) {
        console.debug("  work = ", response.data);
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

    // Find all available translation  work
    this.findAllAvailableConcepts = function(projectId, translationId, pfs) {
      console.debug("findAllAvailableConcepts");
      var deferred = $q.defer();

      // Find all available  work
      gpService.increment()
      $http.post(
        workflowUrl + "translation/available/all" + "?projectId=" + projectId + "&translationId="
          + translationId, pfs).then(
      // success
      function(response) {
        console.debug("  work = ", response.data);
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

    // Find all assigned translation work
    this.findAllAssignedConcepts = function(projectId, translationId, pfs) {
      console.debug("findAllAvailableConcepts");
      var deferred = $q.defer();

      // Find all assigned work
      gpService.increment()
      $http.post(
        workflowUrl + "translation/assigned/all" + "?projectId=" + projectId + "&translationId="
          + translationId, pfs).then(
      // success
      function(response) {
        console.debug("  work = ", response.data);
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

    // Find assigned translation review work
    this.findAssignedReviewConcepts = function(projectId, translationId, userName, pfs) {
      console.debug("findAssignedReviewConcepts");
      var deferred = $q.defer();

      // Find assigned review work
      gpService.increment()
      $http.post(
        workflowUrl + "translation/assigned/review" + "?projectId=" + projectId + "&translationId="
          + translationId + "&userName=" + userName, pfs).then(
      // success
      function(response) {
        console.debug("  work = ", response.data);
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

    // Find release process refsets
    this.findReleaseProcessRefsets = function(projectId, pfs) {
      console.debug("findReleaseProcessRefsets");
      var deferred = $q.defer();

      gpService.increment()
      $http.post(workflowUrl + "refset/release" + "?projectId=" + projectId, pfs).then(
      // success
      function(response) {
        console.debug("  release refsets = ", response.data);
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

    // Find release process translations
    this.findReleaseProcessTranslations = function(projectId, pfs) {
      console.debug("findReleaseProcessTranslations");
      var deferred = $q.defer();

      gpService.increment()
      $http.post(workflowUrl + "translation/release" + "?projectId=" + projectId, pfs).then(
      // success
      function(response) {
        console.debug("  work = ", response.data);
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

    // Find non release process translations
    this.findNonReleaseProcessTranslations = function(projectId, pfs) {
      console.debug("findReleaseProcessTranslations");
      var deferred = $q.defer();

      gpService.increment()
      $http.post(workflowUrl + "translation/nonrelease" + "?projectId=" + projectId, pfs).then(
      // success
      function(response) {
        console.debug("  work = ", response.data);
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

    // Perform workflow action on a translation
    this.performWorkflowActionOnTranslation = function(projectId, translationId, userName, action,
      concept) {
      console.debug("performWorkflowActionOnTranslation");
      var deferred = $q.defer();

      // Perform workflow action on a translation
      gpService.increment()
      $http.post(
        workflowUrl + 'translation' + "/" + action + "?translationId=" + translationId
          + "&userName=" + userName + "&projectId=" + projectId, concept).then(
      // success
      function(response) {
        console.debug("  work = ", response.data);
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

    // Finds refsets available for editing by the specified user
    this.findAvailableEditingRefsets = function(projectId, userName, pfs) {
      console.debug("findAvailableEditingRefsets");
      var deferred = $q.defer();

      // Finds refsets available for editing by the specified user
      gpService.increment()
      $http.post(
        workflowUrl + "refset/available/editing" + "?projectId=" + projectId + "&userName="
          + userName, pfs).then(
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

    // Finds refsets assigned for editing by the specified user
    this.findAssignedEditingRefsets = function(projectId, userName, pfs) {
      console.debug("findAssignedEditingRefsets");
      var deferred = $q.defer();

      // Finds refsets assigned for editing by the specified user
      gpService.increment()
      $http.post(
        workflowUrl + "refset/assigned/editing" + "?projectId=" + projectId + "&userName="
          + userName, pfs).then(
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

    // Finds refsets available for review by the specified user
    this.findAvailableReviewRefsets = function(projectId, userName, pfs) {
      console.debug("findAvailableReviewRefsets");
      var deferred = $q.defer();

      // Finds refsets available for review by the specified user
      gpService.increment()
      $http.get(
        workflowUrl + "refset/available/review" + "?projectId=" + projectId + "&userName="
          + userName, pfs).then(
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

    // Finds refsets assigned for review for the specified user
    this.findAssignedReviewRefsets = function(projectId, userName, pfs) {
      console.debug("findAssignedReviewRefsets");
      var deferred = $q.defer();

      // Finds refsets assigned for review by the specified user
      gpService.increment()
      $http.post(
        workflowUrl + "refset/assigned/review" + "?projectId=" + projectId + "&userName="
          + userName, pfs).then(
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

    // Finds all refsets available
    this.findAllAvailableRefsets = function(projectId, pfs) {
      console.debug("findAllAvailableRefsets");
      var deferred = $q.defer();

      // Finds refsets available for review by the specified user
      gpService.increment()
      $http.post(workflowUrl + "refset/available/all" + "?projectId=" + projectId, pfs).then(
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

    // Finds all assigned refsets
    this.findAllAssignedRefsets = function(projectId, pfs) {
      console.debug("findAllAssignedRefsets");
      var deferred = $q.defer();

      // Finds refsets assigned for review by the specified user
      gpService.increment()
      $http.post(workflowUrl + "refset/assigned/all" + "?projectId=" + projectId, pfs).then(
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

    this.getTrackingRecordForRefset = function(refsetId) {
      console.debug("getTrackingRecordForRefset");
      var deferred = $q.defer();

      // Finds refsets available for review by the specified user
      gpService.increment()
      $http.get(workflowUrl + "record" + "?refsetId=" + refsetId).then(
      // success
      function(response) {
        console.debug("  record = ", response.data);
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

    this.getTrackingRecordForConcept = function(conceptId) {
      console.debug("getTrackingRecordForRefset");
      var deferred = $q.defer();

      // Finds refsets available for review by the specified user
      gpService.increment()
      $http.get(workflowUrl + "record" + "?refsetId=" + refsetId).then(
      // success
      function(response) {
        console.debug("  record = ", response.data);
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
