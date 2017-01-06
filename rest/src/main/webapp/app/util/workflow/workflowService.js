// Workflow Service
tsApp.service('workflowService', [
  '$http',
  '$q',
  'gpService',
  'utilService',
  function($http, $q, gpService, utilService) {
    console.debug('configure workflowService');

    // Get workflow paths
    this.getWorkflowPaths = function() {
      console.debug('getWorkflowPaths');
      var deferred = $q.defer();

      // Perform workflow action on a refset
      gpService.increment();
      $http.get(workflowUrl + 'paths').then(
      // success
      function(response) {
        console.debug('  paths = ', response.data);
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

    // Perform workflow action on a refset
    this.performWorkflowAction = function(projectId, refsetId, userName, projectRole, action) {
      console.debug('performWorkflowAction');
      var deferred = $q.defer();

      // Perform workflow action on a refset
      gpService.increment();
      $http.get(
        workflowUrl + 'refset/' + action + '?refsetId=' + refsetId + '&userName=' + userName
          + '&projectId=' + projectId + '&projectRole=' + projectRole).then(
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

    // Find available translation work
    this.findAvailableConcepts = function(userRole, projectId, translationId, userName, pfs) {
      console.debug('findAvailableConcepts');
      var deferred = $q.defer();

      // Find available editing work
      gpService.increment();
      $http.post(
        workflowUrl + 'translation/available?userRole=' + userRole + '&projectId=' + projectId + '&translationId='
          + translationId + '&userName=' + userName, utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  work = ', response.data);
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

    // Find assigned translation work
    this.findAssignedConcepts = function(userRole, projectId, translationId, userName, pfs) {
      console.debug('findAssignedConcepts');
      var deferred = $q.defer();

      // Find assigned editing work
      gpService.increment();
      $http.post(
        workflowUrl + 'translation/assigned?userRole=' + userRole + '&projectId=' + projectId + '&translationId='
          + translationId + '&userName=' + userName, utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  work = ', response.data);
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

    // Find assigned refset work
    this.findAssignedRefsets = function(userRole, projectId, userName, pfs) {
      console.debug('findAssignedRefsets');
      var deferred = $q.defer();

      // Find assigned refset work
      gpService.increment();
      $http.post(
        workflowUrl + 'refset/assigned?userRole=' + userRole + '&projectId=' + projectId + '&userName=' + userName, utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  work = ', response.data);
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

    // Find non release process translations
    this.findNonReleaseProcessTranslations = function(projectId, pfs) {
      console.debug('findReleaseProcessTranslations');
      var deferred = $q.defer();

      gpService.increment();
      $http.post(workflowUrl + 'translation/nonrelease?projectId=' + projectId,
        utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  work = ', response.data);
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

    // Perform workflow action on a translation
    this.performTranslationWorkflowAction = function(projectId, translationId, userName,
      projectRole, action, concept) {
      console.debug('performWorkflowActionOnTranslation');
      var deferred = $q.defer();

      // Perform workflow action on a translation
      gpService.increment();
      $http.post(
        workflowUrl + 'translation/' + action + '?translationId=' + translationId + '&userName='
          + userName + '&projectId=' + projectId + '&projectRole=' + projectRole, concept).then(
      // success
      function(response) {
        console.debug('  work = ', response.data);
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

    // Perform batch workflow action on a translation
    this.performBatchTranslationWorkflowAction = function(projectId, translationId, userName,
      projectRole, action, conceptList) {
      console.debug('performBatchWorkflowActionOnTranslation');
      var deferred = $q.defer();

      // Perform workflow action on a translation
      gpService.increment();
      $http.post(
        workflowUrl + 'translation/' + action + '/batch?translationId=' + translationId
          + '&userName=' + userName + '&projectId=' + projectId + '&projectRole=' + projectRole,
        conceptList).then(
      // success
      function(response) {
        console.debug('  work = ', response.data);
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

    // Finds refsets available by the specified user
    this.findAvailableRefsets = function(userRole, projectId, userName, pfs) {
      console.debug('findAvailableRefsets');
      var deferred = $q.defer();

      // Finds refsets available by the specified user
      gpService.increment();
      $http.post(
        workflowUrl + 'refset/available?userRole=' + userRole + '&projectId=' + projectId + '&userName=' + userName,
        utilService.prepPfs(pfs)).then(
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



    this.getTrackingRecordForRefset = function(refsetId) {
      console.debug('getTrackingRecordForRefset');
      var deferred = $q.defer();

      // Finds refsets available for review by the specified user
      gpService.increment();
      $http.get(workflowUrl + 'record?refsetId=' + refsetId).then(
      // success
      function(response) {
        console.debug('  record = ', response.data);
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

    this.getTrackingRecordForConcept = function(conceptId) {
      console.debug('getTrackingRecordForRefset');
      var deferred = $q.defer();

      // Finds refsets available for review by the specified user
      gpService.increment();
      $http.get(workflowUrl + 'record?refsetId=' + refsetId).then(
      // success
      function(response) {
        console.debug('  record = ', response.data);
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

    // send feedback email
    this.addFeedback = function(object, name, email, message) {
      console.debug('add feedback', name, email, message, object);
      var deferred = $q.defer();
      // find members
      gpService.increment();
      $http.post(
        workflowUrl + 'message?objectId=' + object.id + '&name=' + encodeURIComponent(name)
          + '&email=' + encodeURIComponent(email), message, {
          headers : {
            'Content-type' : 'text/plain'
          }
        }).then(

      // success
      function(response) {
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

    this.getWorkflowConfig = function(projectId) {
      console.debug('getWorkflowConfig');
      var deferred = $q.defer();

      // Gets the workflow config for the given projectId
      gpService.increment();
      $http.get(workflowUrl + 'config?projectId=' + projectId).then(
      // success
      function(response) {
        console.debug('  workflow config = ', response.data);
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

    
    this.refsetIsAllowed = function(action, role, workflowStatus, workflowConfig) {
      var allowed = workflowConfig.refsetAllowedMap[action + role + workflowStatus];
      if (allowed == null) {
        allowed = workflowConfig.refsetAllowedMap[action + role + '*'];
      }
      if (allowed == null) {
        return false;
      }
      return allowed;
    }
    
    this.refsetGetRole = function(action, role, workflowStatus, workflowConfig) {
      var allowed = workflowConfig.refsetRoleMap[action + role + workflowStatus];
      if (allowed == null) {
        allowed = workflowConfig.refsetRoleMap[action + role + '*'];
      }
      if (allowed == null) {
        return role;
      }
      return allowed;
    }
    
    this.translationIsAllowed = function(action, role, workflowStatus, workflowConfig) {
      var allowed = workflowConfig.translationAllowedMap[action + role + workflowStatus];
      if (allowed == null) {
        allowed = workflowConfig.translationAllowedMap[action + role + '*'];
      }
      if (allowed == null) {
        return false;
      }
      return allowed;
    }
    
    this.translationGetRole = function(action, role, workflowStatus, workflowConfig) {
      var allowed = workflowConfig.translationRoleMap[action + role + workflowStatus];
      if (allowed == null) {
        allowed = workflowConfig.translationRoleMap[action + role + '*'];
      }
      if (allowed == null) {
        return role;
      }
      return allowed;
    }
    // end

  } ]);
