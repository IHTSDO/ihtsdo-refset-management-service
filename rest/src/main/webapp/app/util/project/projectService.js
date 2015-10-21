// Project Service
tsApp
  .service(
    'projectService',
    [
      '$http',
      '$q',
      'gpService',
      'utilService',
      function($http, $q, gpService, utilService) {
        console.debug("configure projectService");

        // Declare the model
        var userProjectsInfo = {
          anyrole : null
        };

        var iconConfig = {};

        // Gets the user projects info
        this.getUserProjectsInfo = function() {
          return userProjectsInfo;
        }

        this.getIconConfig = function() {
          return iconConfig;
        }

        // get icon config info
        this.prepareIconConfig = function() {
          var deferred = $q.defer();

          // Get projects
          gpService.increment()
          $http
            .get(projectUrl + 'icons')
            .then(
              // success
              function(response) {
                console.debug("get icons", response.data);
                // Set the map of key=>value
                for (var i = 0; i < response.data.keyValuePairs.length; i++) {
                  iconConfig[response.data.keyValuePairs[i].key] = response.data.keyValuePairs[i].value;
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
        }
        
        // get all projects
        this.getProjects = function() {
          var deferred = $q.defer();

          // Get projects
          gpService.increment()
          $http.get(projectUrl + 'all').then(
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
        }

        // add project
        this.addProject = function(project) {
          console.debug("addProject");
          var deferred = $q.defer();

          // Add project
          gpService.increment()
          $http.put(projectUrl + 'add', project).then(
          // success
          function(response) {
            console.debug("  project = ", response.data);
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

        // update project
        this.updateProject = function(project) {
          console.debug("updateProject");
          var deferred = $q.defer();

          // Add project
          gpService.increment()
          $http.post(projectUrl + 'update', project).then(
          // success
          function(response) {
            console.debug("  project = ", response.data);
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

        // remove project
        this.removeProject = function(project) {
          console.debug("removeProject");
          var deferred = $q.defer();

          // Add project
          gpService.increment()
          $http['delete'](projectUrl + 'remove' + "/" + project.id).then(
          // success
          function(response) {
            console.debug("  project = ", response.data);
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

        // Finds projects as a list
        this.findProjectsAsList = function(queryStr, pfs) {

          var query = (queryStr == null) ? "" : queryStr;
          console.debug("findProjectsAsList", query, pfs);
          // Setup deferred
          var deferred = $q.defer();

          // Make POST call
          gpService.increment();
          $http.post(projectUrl + "projects" + "?query=" + query, pfs)
          // + encodeURIComponent(utilService.cleanQuery(queryStr)),
          // pfs)
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
        };

        // Finds users on given project
        this.findAssignedUsersForProject = function(projectId, query, pfs) {

          console.debug("findAssignedUsersForProject", projectId, pfs);
          // Setup deferred
          var deferred = $q.defer();

          // Make PUT call
          gpService.increment();
          $http.put(projectUrl + "users/" + projectId + "?query=" + query, pfs)
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
        };

        // Finds users NOT on given project
        this.findUnassignedUsersForProject = function(projectId, query, pfs) {

          console.debug("findUnassignedUsersForProject", projectId, pfs);
          // Setup deferred
          var deferred = $q.defer();

          // Make PUT call
          gpService.increment();
          $http.put(
            projectUrl + "users/" + projectId + "/unassigned?query=" + query,
            pfs).then(
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
        };

        // assign user to project
        this.assignUserToProject = function(projectId, userName, projectRole) {
          console.debug("assignUserToProject");
          var deferred = $q.defer();

          // Assign user to project
          gpService.increment()
          $http.get(
            projectUrl + 'assign' + "?projectId=" + projectId + "&userName="
              + userName + "&role=" + projectRole).then(
          // success
          function(response) {
            console.debug("  project = ", response.data);
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

        // unassign user from project
        this.unassignUserFromProject = function(projectId, userName) {
          console.debug("unassignUserFromProject");
          var deferred = $q.defer();

          // Unassign user from project
          gpService.increment()
          $http.get(
            projectUrl + 'unassign' + "?projectId=" + projectId + "&userName="
              + userName).then(
          // success
          function(response) {
            console.debug("  project = ", response.data);
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

        // get project roles
        this.getProjectRoles = function() {
          var deferred = $q.defer();

          // Get project roles
          gpService.increment()
          $http.get(projectUrl + 'roles').then(
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

        // does user have any role on any project
        this.getUserHasAnyRole = function() {
          var deferred = $q.defer();

          // Get project roles
          gpService.increment()
          $http.get(projectUrl + 'user/anyrole').then(
          // success
          function(response) {
            userProjectsInfo.anyRole = response.data;
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

        // initialize
        this.prepareIconConfig();
      } ]);
