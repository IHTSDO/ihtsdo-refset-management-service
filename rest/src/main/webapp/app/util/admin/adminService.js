// Admin Service
tsApp
  .service(
    'adminService',
    [
      '$http',
      '$q',
      'gpService',
      'utilService',
      function($http, $q, gpService, utilService) {
        console.debug("configure adminService");

        // get all projects
        this.getProjects = function() {
          console.debug("getProjects");
          var deferred = $q.defer();

          // Get projects
          gpService.increment()
          $http.get(projectUrl + 'projects').then(
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
          $http.delete(projectUrl + 'remove' + "/" + project.id).then(
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
        
        // Sets the project
        this.setProject = function(project) {
          if (typeof project === undefined) {
            return;
          }
        }


      } ]);
