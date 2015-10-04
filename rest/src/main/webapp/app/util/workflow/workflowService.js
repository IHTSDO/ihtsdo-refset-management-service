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
          projectUrl + 'refset' + "?action=" + action + "&refsetId=" + refsetId
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

      // Find available editing work
      this.findAvailableEditingWork = function(projectId, translationId,
        userName, pfs) {
        console.debug("findAvailableEditingWork");
        var deferred = $q.defer();

        // Find available editing work
        gpService.increment()
        $http.post(
          projectUrl + 'translation/available/editing' + "?projectId="
            + projectId + "&translationId=" + translationId + "&userName="
            + userName + "&pfs=" + pfs).then(
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

      // Find assigned editing work
      this.findAssignedEditingWork = function(projectId, translationId,
        userName, pfs) {
        console.debug("findAssignedEditingWork");
        var deferred = $q.defer();

        // Find assigned editing work
        gpService.increment()
        $http.post(
          projectUrl + 'translation/assigned/editing' + "?projectId="
            + projectId + "&translationId=" + translationId + "&userName="
            + userName + "&pfs=" + pfs).then(
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

      // Find available review work
      this.findAvailableReviewWork = function(projectId, translationId,
        userName, pfs) {
        console.debug("findAvailableReviewWork");
        var deferred = $q.defer();

        // Find available review work
        gpService.increment()
        $http.post(
          projectUrl + 'translation/assigned/editing' + "?projectId="
            + projectId + "&translationId=" + translationId + "&userName="
            + userName + "&pfs=" + pfs).then(
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

      // Find available review work
      this.findAvailableReviewWork = function(projectId, translationId,
        userName, pfs) {
        console.debug("findAvailableReviewWork");
        var deferred = $q.defer();

        // Find available review work
        gpService.increment()
        $http.post(
          projectUrl + 'translation/assigned/review' + "?projectId="
            + projectId + "&translationId=" + translationId + "&userName="
            + userName + "&pfs=" + pfs).then(
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

      // Find assigned review work
      this.findAssignedReviewWork = function(projectId, translationId,
        userName, pfs) {
        console.debug("findAssignedReviewWork");
        var deferred = $q.defer();

        // Find assigned review work
        gpService.increment()
        $http.post(
          projectUrl + 'translation/assigned/review' + "?projectId="
            + projectId + "&translationId=" + translationId + "&userName="
            + userName + "&pfs=" + pfs).then(
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
        translationId, userName, action) {
        console.debug("performWorkflowActionOnTranslation");
        var deferred = $q.defer();

        // Perform workflow action on a translation
        gpService.increment()
        $http.post(
          projectUrl + 'translation' + "?action=" + action + "&translationId="
            + translationId + "&userName=").then(
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

    } ]);
