// Workflow Service
tsApp.service('workflowService',
  [
    '$http',
    '$q',
    'gpService',
    'utilService',
    function($http, $q, gpService, utilService) {
      console.debug("configure workflowService");

      // Perform workflow action on a refset
      this.performWorkflowAction = function(projectId, refsetId, userName,
        action) {
        console.debug("performWorkflowAction");
        var deferred = $q.defer();

        // Perform workflow action on a refset
        gpService.increment()
        $http.get(
          workflowUrl + 'refset' + "/" + action + "?refsetId=" + refsetId
            + "&userName=" + userName + "&projectId=" + projectId).then(
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
      this.findAvailableEditingConcepts = function(projectId, translationId,
        userName, pfs) {
        console.debug("findAvailableEditingConcepts");
        var deferred = $q.defer();

        // Find available editing work
        gpService.increment()
        $http.post(
          workflowUrl + "translation/available/editing" + "?projectId="
            + projectId + "&translationId=" + translationId + "&userName="
            + userName, pfs).then(
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
      this.findAssignedEditingConcepts = function(projectId, translationId,
        userName, pfs) {
        console.debug("findAssignedEditingConcepts");
        var deferred = $q.defer();

        // Find assigned editing work
        gpService.increment()
        $http.post(
          workflowUrl + "translation/assigned/editing" + "?projectId="
            + projectId + "&translationId=" + translationId + "&userName="
            + userName, pfs).then(
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
      this.findAvailableReviewConcepts = function(projectId, translationId,
        userName, pfs) {
        console.debug("findAvailableReviewConcepts");
        var deferred = $q.defer();

        // Find available review work
        gpService.increment()
        $http.post(
          workflowUrl + "translation/available/review" + "?projectId="
            + projectId + "&translationId=" + translationId + "&userName="
            + userName, pfs).then(
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
      this.findAssignedReviewConcepts = function(projectId, translationId,
        userName, pfs) {
        console.debug("findAssignedReviewConcepts");
        var deferred = $q.defer();

        // Find assigned review work
        gpService.increment()
        $http.post(
          workflowUrl + "translation/assigned/review" + "?projectId="
            + projectId + "&translationId=" + translationId + "&userName="
            + userName, pfs).then(
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
      this.performWorkflowActionOnTranslation = function(projectId,
        translationId, userName, action, concept) {
        console.debug("performWorkflowActionOnTranslation");
        var deferred = $q.defer();

        // Perform workflow action on a translation
        gpService.increment()
        $http.post(
          workflowUrl + 'translation' + "/" + action + "?translationId="
            + translationId + "&userName=" + userName + "&projectId="
            + projectId, concept).then(
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
          workflowUrl + "refset/available/editing" + "?projectId=" + projectId
            + "&userName=" + userName, pfs).then(
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
          workflowUrl + "refset/assigned/editing" + "?projectId=" + projectId
            + "&userName=" + userName, pfs).then(
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
          workflowUrl + "refset/available/review" + "?projectId=" + projectId
            + "&userName=" + userName, pfs).then(
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

      // Finds refsets assigned for review by the specified user
      this.findAssignedReviewRefsets = function(projectId, userName, pfs) {
        console.debug("findAssignedReviewRefsets");
        var deferred = $q.defer();

        // Finds refsets assigned for review by the specified user
        gpService.increment()
        $http.post(
          workflowUrl + "refset/assigned/review" + "?projectId=" + projectId
            + "&userName=" + userName, pfs).then(
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
        $http.get(
          workflowUrl + "record" + "?refsetId=" + refsetId).then(
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
    } ]);
