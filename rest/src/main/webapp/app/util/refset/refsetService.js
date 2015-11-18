// Refset Service
tsApp.service('refsetService', [
  '$http',
  '$q',
  'Upload',
  'gpService',
  'utilService',
  'projectService',
  function($http, $q, Upload, gpService, utilService, projectService) {
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

    // find members of refset revision
    this.findRefsetRevisionMembersForQuery = function(refsetId, date, pfs) {
      console.debug("findRefsetRevisionMembersForQuery");
      var deferred = $q.defer();

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
    
    // clone refset
    this.cloneRefset = function(refset, projectId, terminologyId) {
      console.debug("cloneRefset");
      var deferred = $q.defer();

      // Clone refset
      gpService.increment()
      $http.put(refsetUrl + 'clone' + "?refsetId=" + refset.id + "&projectId=" + projectId + 
        "&terminologyId=" + terminologyId).then(
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

      gpService.increment()
      $http['delete'](refsetUrl + 'remove' + "/" + refsetId + "?cascade=true").then(
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
        refsetUrl + "members" + "?refsetId=" + refsetId + "&query=" + query,
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
    this.addRefsetInclusion = function(refsetId, conceptId) {
      console.debug("addRefsetInclusion");
      var deferred = $q.defer();

      // Add refset inclusion
      gpService.increment()
      $http.get(
        refsetUrl + "inclusion/add/" + refsetId + "?conceptId=" + conceptId)
        .then(
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

    // add refset inclusion
    this.addRefsetExclusion = function(refsetId, conceptId) {
      console.debug("addRefsetExclusion");
      var deferred = $q.defer();

      // Add refset inclusion
      gpService.increment()
      $http.get(
        refsetUrl + "exclusion/add/" + refsetId + "?conceptId=" + conceptId)
        .then(
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

    // get import refset handlers
    this.getImportRefsetHandlers = function() {
      console.debug("getImportRefsetHandlers");
      var deferred = $q.defer();

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

      gpService.increment();
      $http.get(refsetUrl + "release/report" + "?reportToken=" + reportToken)
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

    this.beginRedefinition = function(refsetId, definition) {
      console.debug("beginRedefinition");
      var deferred = $q.defer();

      // get refset revision
      gpService.increment()
      $http.get(refsetUrl + "redefinition/begin?refsetId=" + refsetId
        + "&newDefinition=" + encodeURIComponent(definition)).then(
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
    
    this.finishRedefinition = function(refsetId) {
      console.debug("finishRedefinition");
      var deferred = $q.defer();

      // get refset revision
      gpService.increment()
      $http.get(refsetUrl + "redefinition/finish?refsetId=" + refsetId).then(
      // success
      function(response) {
        console.debug("  finish refset redefinition = ", response.data);
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
    
    this.getExportRefsetHandlers = function() {
      console.debug("getExportRefsetHandlers");
      var deferred = $q.defer();

      // get refset revision
      gpService.increment()
      $http.get(refsetUrl + "export/handlers").then(
      // success
      function(response) {
        console.debug("  export refset handlers = ", response.data);
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
 
    this.exportDefinition = function(refsetId, handlerId, extension) {
      console.debug("exportDefinition");
      var deferred = $q.defer();
      gpService.increment()
      $http.get(refsetUrl + "export/definition?refsetId=" + refsetId + "&handlerId=" + handlerId).then(
      // Success
      function(data) {
        var blob = new Blob([ data ], {
          type : ""
        });

        // hack to download store a file having its URL
        var fileURL = URL.createObjectURL(blob);
        var a = document.createElement('a');
        a.href = fileURL;
        a.target = "_blank";
        a.download = "definition." + refsetId + "." + extension;
        document.body.appendChild(a);
        gpService.decrement();
        a.click();

        deferred.resolve(data);
      },
      // Error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(data);
      });
    };

    this.exportMembers = function(refsetId, handlerId, extension) {
      console.debug("exportMembers");
      var deferred = $q.defer();
      gpService.increment()
      $http.get(refsetUrl + "export/members?refsetId=" + refsetId + "&handlerId=" + handlerId).then(
      // Success
      function(data) {
        var blob = new Blob([ data ], {
          type : ""
        });

        // hack to download store a file having its URL
        var fileURL = URL.createObjectURL(blob);
        var a = document.createElement('a');
        a.href = fileURL;
        a.target = "_blank";
        a.download = "members." + refsetId + "." + extension;;
        document.body.appendChild(a);
        gpService.decrement();
        a.click();

        deferred.resolve(data);
      },
      // Error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(data);
      });
    };

    // Begin import members - if validation is result, OK to proceed.
    this.beginImportMembers = function(refsetId, handlerId) {
      console.debug("begin import members");
      var deferred = $q.defer();
      gpService.increment()
      $http.get(refsetUrl + "import/begin?refsetId=" + refsetId + "&handlerId=" + handlerId).then(
        // success
        function(response) {
          console.debug("  validation result = ",response.data);
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


    // Cancel import members
    this.cancelImportMembers = function(refsetId) {
      console.debug("cancel import members");
      var deferred = $q.defer();
      gpService.increment()
      $http.get(refsetUrl + "import/cancel?refsetId=" + refsetId).then(
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
    
    // Finish import members - if validation is result, OK to proceed.
    this.finishImportMembers = function(refsetId, handlerId, file) {
      console.debug("finish import members");
      var deferred = $q.defer();
      gpService.increment()
      Upload.upload({
            url: refsetUrl + "import/finish?refsetId=" + refsetId + "&handlerId=" + handlerId,
            data: {file: file}
        }).then(
          // Success
          function(response) {
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
          function (evt) {
            var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
            console.debug('progress: ' + progressPercentage + '% ' + evt.config.data.file.name);
        });      
        return deferred.promise;
    };    
    
  } ]);
