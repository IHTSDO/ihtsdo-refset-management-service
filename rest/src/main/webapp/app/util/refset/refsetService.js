// Refset Service
tsApp.service('refsetService', [
  '$http',
  '$q',
  'gpService',
  'utilService',
  'projectService',
  function($http, $q, gpService, utilService, projectService) {
    console.debug("configure refsetService");

    // get refset revision
    this.getRefsetRevision = function(refsetId, date) {
      console.debug("getRefsetRevision");
      var deferred = $q.defer();

      // get refset revision
      gpService.increment()
      $http.get(refsetUrl + refsetId + "/" + date).then(
      // success
      function(response) {
        console.debug("  refset revision = ", response.data);
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

    // find members for refset revision
    this.findMembersForRefsetRevision = function(refsetId, date, pfs) {
      console.debug("findMembersForRefsetRevision");
      var deferred = $q.defer();

      // find refsets
      gpService.increment()
      $http.post(refsetUrl + refsetId + "/" + date + "/" + 'members', pfs)
        .then(
        // success
        function(response) {
          console.debug("  members = ", response.data);
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
    
    // get refset for id
    this.getRefset = function(refsetId) {
      console.debug("getRefset");
      var deferred = $q.defer();

      // get refset for id
      gpService.increment()
      $http.get(refsetUrl + "/" + refsetId).then(
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
    
    // get refsets for project
    this.getRefsetsForProject = function(projectId) {
      console.debug("getRefsetsForProject");
      var deferred = $q.defer();

      // get refset for project
      gpService.increment()
      $http.get(refsetUrl + 'refsets' + "/" + projectId).then(
      // success
      function(response) {
        console.debug("  projects = ", response.data);
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
    
    // Finds refsets based on pfs parameter and query
    this.findRefsetsForQuery = function(queryStr, pfs) {

      var query = (queryStr == null) ? "" : queryStr;
      console.debug("findRefsetsForQuery", query, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(refsetUrl + "refsets" + "?query=" + query, pfs).then(
      // success
      function(response) {
        console.debug("  output = ", response.data);
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
    
    // add refset
    this.addRefset = function(refset) {
      console.debug("addRefset");
      var deferred = $q.defer();

      // Add refset
      gpService.increment()
      $http.put(refsetUrl + 'add', refset).then(
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

    // update refset
    this.updateRefset = function(refset) {
      console.debug("updateRefset");
      var deferred = $q.defer();

      // update refset
      gpService.increment()
      $http.post(refsetUrl + 'update', refset).then(
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

    // remove refset
    this.removeRefset = function(refsetId) {
      console.debug("removeRefset");
      var deferred = $q.defer();

      // remove refset
      gpService.increment()
      $http['delete'](refsetUrl + 'remove' + "/" + refsetId).then(
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
    
    
    
    
    
    
    
    
    
    
    

    // find members for refset
    this.findRefsetMembersForQuery = function(refsetId, query, pfs) {
      console.debug("findRefsetMembersForQuery");
      var deferred = $q.defer();

      // find members
      gpService.increment()
      $http.post(
        refsetUrl + "members" + "?query=" + query + "&refsetId=" + refsetId,
        pfs).then(
      // success
      function(response) {
        console.debug("  members = ", response.data);
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

    // find inclusions for refset
    this.findRefsetInclusionsForQuery = function(refsetId, query, pfs) {
      console.debug("findRefsetInclusionsForQuery");
      var deferred = $q.defer();

      // find inclusions
      gpService.increment()
      $http.post(
        refsetUrl + "inclusions" + "?query=" + query + "&refsetId=" + refsetId,
        pfs).then(
      // success
      function(response) {
        console.debug("  inclusions = ", response.data);
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

    // find exclusions for refset
    this.findRefsetExclusionsForQuery = function(refsetId, query, pfs) {
      console.debug("findRefsetExclusionsForQuery");
      var deferred = $q.defer();

      // find exclusions
      gpService.increment()
      $http.post(
        refsetUrl + "exclusions" + "?query=" + query + "&refsetId=" + refsetId,
        pfs).then(
      // success
      function(response) {
        console.debug("  exclusions = ", response.data);
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







   

    // Initialize user role - only when refset service loads
    projectService.getUserHasAnyRole();

  } ]);
