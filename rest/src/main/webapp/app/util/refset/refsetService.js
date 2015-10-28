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

    // add refset member
    this.addRefsetMember = function(member) {
      console.debug("addRefsetMember");
      var deferred = $q.defer();

      // Add refset member
      gpService.increment()
      $http.put(refsetUrl + "member/add", member).then(
      // success
      function(response) {
        console.debug("  refset member = ", response.data);
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

    // remove refset member
    this.removeRefsetMember = function(memberId) {
      console.debug("removeRefsetMember");
      var deferred = $q.defer();

      // remove refset member
      gpService.increment()
      $http['delete'](refsetUrl + "member/remove/" + memberId).then(
      // success
      function(response) {
        console.debug("  refset member = ", response.data);
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

    // add refset inclusion
    this.addRefsetInclusion = function(refsetId, inclusion) {
      console.debug("addRefsetInclusion");
      var deferred = $q.defer();

      // Add refset inclusion
      gpService.increment()
      $http.put(refsetUrl + "inclusion/add/" + refsetId, inclusion).then(
      // success
      function(response) {
        console.debug("  inclusion = ", response.data);
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

    // add refset inclusion
    this.addRefsetExclusion = function(refsetId, exclusion) {
      console.debug("addRefsetExclusion");
      var deferred = $q.defer();

      // Add refset inclusion
      gpService.increment()
      $http.put(refsetUrl + "exclusion/add/" + refsetId, exclusion).then(
      // success
      function(response) {
        console.debug("  exclusion = ", response.data);
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

    // get import refset handlers
    this.getImportRefsetHandlers = function() {
      console.debug("getImportRefsetHandlers");
      var deferred = $q.defer();

      // get import refset handlers
      gpService.increment()
      $http.get(refsetUrl + "import/handlers").then(
      // success
      function(response) {
        console.debug("  import handlers = ", response.data);
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

    // get export refset handlers
    this.getExportRefsetHandlers = function() {
      console.debug("getExportRefsetHandlers");
      var deferred = $q.defer();

      // get export refset handlers
      gpService.increment()
      $http.get(refsetUrl + "export/handlers").then(
      // success
      function(response) {
        console.debug("  export handlers = ", response.data);
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

    // begin refset migration
    this.beginMigration = function(refsetId, newTerminology, newVersion) {
      console.debug("beginMigration");
      var deferred = $q.defer();

      // begin refset migration
      gpService.increment()
      $http.get(
        refsetUrl + "migration/begin" + "?refsetId=" + refsetId
          + "&newTerminology=" + newTerminology + "&newVersion=" + newVersion)
        .then(
        // success
        function(response) {
          console.debug("  migration begin = ", response.data);
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

    // finish refset migration
    this.finishMigration = function(refsetId) {
      console.debug("finishMigration");
      var deferred = $q.defer();

      // finish refset migration
      gpService.increment()
      $http.get(refsetUrl + "migration/finish" + "?refsetId=" + refsetId).then(
      // success
      function(response) {
        console.debug("  migration finish = ", response.data);
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

    // cancel refset migration
    this.cancelMigration = function(refsetId) {
      console.debug("cancelMigration");
      var deferred = $q.defer();

      // finish refset migration
      gpService.increment()
      $http.get(refsetUrl + "migration/cancel" + "?refsetId=" + refsetId).then(
      // success
      function(response) {
        console.debug("  migration cancel = ", response.data);
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

    // redefinition begin
    this.beginRedefinition = function(refsetId, newDefinition) {
      console.debug("beginRedefinition");
      var deferred = $q.defer();

      // redefinition begin
      gpService.increment()
      $http.get(
        refsetUrl + "redefinition/begin" + "?refsetId=" + refsetId
          + "&newDefinition=" + newDefinition).then(
      // success
      function(response) {
        console.debug("  redefinition begin = ", response.data);
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

    // redefinition finish
    this.finishRedefinition = function(refsetId) {
      console.debug("finishRedefinition");
      var deferred = $q.defer();

      // redefinition finish
      gpService.increment()
      $http.get(refsetUrl + "redefinition/finish" + "?refsetId=" + refsetId)
        .then(
        // success
        function(response) {
          console.debug("  redefinition finish = ", response.data);
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

    // redefinition cancel
    this.cancelRedefinition = function(refsetId) {
      console.debug("cancelRedefinition");
      var deferred = $q.defer();

      // redefinition cancel
      gpService.increment()
      $http.get(refsetUrl + "redefinition/cancel" + "?refsetId=" + refsetId)
        .then(
        // success
        function(response) {
          console.debug("  redefinition cancel = ", response.data);
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

    // compare refsets
    this.compareRefsets = function(refsetId1, refsetId2) {
      console.debug("compareRefsets");
      var deferred = $q.defer();

      // redefinition cancel
      gpService.increment()
      $http.get(
        refsetUrl + "compare" + "?refsetId1=" + refsetId1 + "&refsetId2="
          + refsetId2).then(
      // success
      function(response) {
        console.debug("  compare refsets = ", response.data);
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

    // find members in common
    this.findMembersInCommon = function(reportToken, queryStr, pfs) {

      var query = (queryStr == null) ? "" : queryStr;
      console.debug("findMembersInCommon", query, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(
        refsetUrl + "common/members" + "?reportToken=" + reportToken
          + "&query=" + query, pfs).then(
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

    // find members in diff
    this.getDiffReport = function(reportToken) {
      console.debug("getDiffReport");
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.get(refsetUrl + "diff/members" + "?reportToken=" + reportToken)
        .then(
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

    // release report
    this.releaseReportToken = function(reportToken) {
      console.debug("releaseReportToken");
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      //TODO: This should be a DELETE instead of POST. Fix this when RefsetServiceRestImpl is corrected.
      $http.post(refsetUrl + "release/report" + "?reportToken=" + reportToken)
        .then(
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

    // get definition for refset id
    this.extrapolateDefinition = function(refsetId) {
      console.debug("extrapolateDefinition");
      // Setup deferred
      var deferred = $q.defer();

      gpService.increment();
      $http.get(refsetUrl + 'definition' + "/" + refsetId).then(
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

    // begin refset members import
    this.beginImportMembers = function(refsetId, ioHandlerInfoId) {
      console.debug("beginImportMembers");
      // Setup deferred
      var deferred = $q.defer();

      gpService.increment();
      $http.get(
        refsetUrl + "import/begin" + "?refsetId=" + refsetId
          + "&ioHandlerInfoId=" + ioHandlerInfoId).then(
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

    // redefinition resume
    this.resumeRedefinition = function(refsetId) {
      console.debug("resumeRedefinition");
      // Setup deferred
      var deferred = $q.defer();

      gpService.increment();
      $http.get(refsetUrl + "redefinition/resume" + "?refsetId=" + refsetId)
        .then(
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

    // redefinition resume
    this.resumeMigration = function(refsetId) {
      console.debug("resumeMigration");
      // Setup deferred
      var deferred = $q.defer();

      gpService.increment();
      $http.get(refsetUrl + "migration/resume" + "?refsetId=" + refsetId).then(
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

    // resume refset members import
    this.resumeImportMembers = function(refsetId, ioHandlerInfoId) {
      console.debug("resumeImportMembers");
      // Setup deferred
      var deferred = $q.defer();

      gpService.increment();
      $http.get(
        refsetUrl + "import/resume" + "?refsetId=" + refsetId
          + "&ioHandlerInfoId=" + ioHandlerInfoId).then(
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

    // cancel import members
    this.cancelImportMembers = function(refsetId) {
      console.debug("cancelImportMembers");
      // Setup deferred
      var deferred = $q.defer();

      gpService.increment();
      $http.get(refsetUrl + "import/cancel" + "?refsetId=" + refsetId).then(
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

    // get refset types
    this.getRefsetTypes = function() {
      console.debug("getRefsetTypes");
      var deferred = $q.defer();

      // Get refset types
      gpService.increment()
      $http.get(refsetUrl + 'types').then(
      // success
      function(response) {
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
    
    // Initialize user role - only when refset service loads
    //projectService.getUserHasAnyRole();

  } ]);
