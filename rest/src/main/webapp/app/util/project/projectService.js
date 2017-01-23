// Project Service
tsApp
  .service(
    'projectService',
    [
      '$http',
      '$q',
      '$rootScope',
      'gpService',
      'utilService',
      function($http, $q, $rootScope, gpService, utilService) {
        console.debug('configure projectService');

        // Declare the model
        var userProjectsInfo = {
          anyrole : null
        };

        var iconConfig = {};

        // broadcasts a new project id
        this.fireProjectChanged = function(project) {
          $rootScope.$broadcast('refset:projectChanged', project);
        };

        // Gets the user projects info
        this.getUserProjectsInfo = function() {
          return userProjectsInfo;
        };

        this.getIconConfig = function() {
          console.debug('get icon config', iconConfig);
          return iconConfig;
        };

        // get icon config info
        this.prepareIconConfig = function() {
          console.debug('prepareIconConfig');
          var deferred = $q.defer();

          // Get projects
          gpService.increment();
          $http
            .get(projectUrl + 'icons')
            .then(
              // success
              function(response) {
                console.debug('  icons = ', response.data);
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
        };

        // Tests that the key has an icon
        this.hasIcon = function(key) {
          return iconConfig[key] !== undefined;
        };

        // Returns the icon path for the key (moduleId or namespaceId)
        this.getIcon = function(key) {
          return iconConfig[key];
        };

        // get terminology handlers
        this.getTerminologyHandlers = function() {
          console.debug('getTerminologyHandlers');
          var deferred = $q.defer();

          // Get projects
          gpService.increment();
          $http.get(projectUrl + 'handlers').then(
          // success
          function(response) {
            console.debug('  handlers = ', response.data);
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

        // test handler url
        this.testHandlerUrl = function(project, terminology, version) {
          console.debug('testHandlerUrl', project, terminology, version);
          var deferred = $q.defer();

          // Get projects
          gpService.increment();
          $http.get(
            projectUrl + 'test?key=' + project.terminologyHandlerKey + '&url='
              + encodeURIComponent(project.terminologyHandlerUrl)
              + (terminology ? '&terminology=' + terminology : '')
              + (version ? '&version =' + version : ''), {
              transformResponse : [ function(data) {
                // Response is plain text at this point
                return data;
              } ]
            }).then(
          // success
          function(response) {
            console.debug('  test = ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            // let this be handled elsewhere: utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        };

        // get all projects
        this.getProjects = function() {
          var deferred = $q.defer();

          // Get projects
          gpService.increment();
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
        };

        // get project
        this.getProject = function(projectId) {
          var deferred = $q.defer();

          // Get projects
          gpService.increment();
          $http.get(projectUrl + projectId).then(
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
        // add project
        this.addProject = function(project) {
          console.debug('addProject');
          var deferred = $q.defer();

          // Add project
          gpService.increment();
          $http.put(projectUrl + 'add', project).then(
          // success
          function(response) {
            console.debug('  project = ', response.data);
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

        // update project
        this.updateProject = function(project) {
          console.debug();
          var deferred = $q.defer();

          // Add project
          gpService.increment();
          $http.post(projectUrl + 'update', project).then(
          // success
          function(response) {
            console.debug('  project = ', response.data);
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

        // remove project
        this.removeProject = function(project) {
          console.debug();
          var deferred = $q.defer();

          // Add project
          gpService.increment();
          $http['delete'](projectUrl + 'remove/' + project.id).then(
          // success
          function(response) {
            console.debug('  project = ', response.data);
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

        // Finds projects as a list
        this.findProjectsAsList = function(query, pfs) {

          console.debug('findProjectsAsList', query, pfs);
          // Setup deferred
          var deferred = $q.defer();

          // Make POST call
          gpService.increment();
          $http.post(projectUrl + 'projects?query=' + utilService.prepQuery(query),
            utilService.prepPfs(pfs)).then(
          // success
          function(response) {
            console.debug('  projects = ', response.data);
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

          console.debug('findAssignedUsersForProject', projectId, pfs);
          // Setup deferred
          var deferred = $q.defer();

          // Make PUT call
          gpService.increment();
          $http.post(projectUrl + 'users/' + projectId + '?query=' + utilService.prepQuery(query),
            utilService.prepPfs(pfs)).then(
          // success
          function(response) {
            console.debug('  assigned users = ', response.data);
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

          console.debug('findUnassignedUsersForProject', projectId, pfs);
          // Setup deferred
          var deferred = $q.defer();

          // Make PUT call
          gpService.increment();
          $http
            .post(
              projectUrl + 'users/' + projectId + '/unassigned?query='
                + utilService.prepQuery(query), utilService.prepPfs(pfs)).then(
            // success
            function(response) {
              console.debug('  unassigned users = ', response.data);
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
          console.debug('assignUserToProject');
          var deferred = $q.defer();

          // Assign user to project
          gpService.increment();
          $http.get(
            projectUrl + 'assign?projectId=' + projectId + '&userName=' + userName + '&role='
              + projectRole).then(
          // success
          function(response) {
            console.debug('  project = ', response.data);
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

        // unassign user from project
        this.unassignUserFromProject = function(projectId, userName) {
          console.debug('unassignUserFromProject');
          var deferred = $q.defer();

          // Unassign user from project
          gpService.increment();
          $http.get(projectUrl + 'unassign?projectId=' + projectId + '&userName=' + userName).then(
          // success
          function(response) {
            console.debug('  project = ', response.data);
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

        // get project roles
        this.getProjectRoles = function() {
          console.debug('getProjectRoles');
          var deferred = $q.defer();

          // Get project roles
          gpService.increment();
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
            deferred.reject(response.data);
          });
          return deferred.promise;
        };

        // does user have any role on any project
        this.getUserHasAnyRole = function() {
          console.debug('getUserHasAnyRole');
          var deferred = $q.defer();

          // Get project roles
          gpService.increment();
          $http.get(projectUrl + 'user/anyrole').then(
          // success
          function(response) {
            console.debug('  anyrole = ' + response.data);
            userProjectsInfo.anyRole = (response.data != 'false');
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

        // Find concepts
        this.findConceptsForQuery = function(project, query, terminology, version, pfs) {
          console.debug('findConceptsForQuery', project, query, pfs);
          // Setup deferred
          var deferred = $q.defer();

          // Make POST call
          gpService.increment();
          $http.post(
            projectUrl + 'concepts?projectId=' + project.id + '&query='
              + utilService.prepQuery(query, true) + '&terminology=' + terminology + '&version='
              + version, utilService.prepPfs(pfs))

          .then(
          // success
          function(response) {
            console.debug('  concepts = ', response.data);
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

        // Get parent concepts
        this.getConceptParents = function(projectId, terminologyId, terminology, version,
          translationId) {
          console.debug('getConceptParents', projectId, terminologyId);
          // Setup deferred
          var deferred = $q.defer();

          // Make POST call
          gpService.increment();
          $http.get(
            projectUrl + 'concept/parents?projectId=' + projectId + '&terminologyId='
              + terminologyId + '&terminology=' + terminology + '&version=' + version
              + (translationId != null ? '&translationId=' + translationId : '')).then(
          // success
          function(response) {
            console.debug('  parents = ', response.data);
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

        // Get child concepts
        this.getConceptChildren = function(projectId, terminologyId, terminology, version,
          translationId, pfs) {
          console.debug('getConceptChildren', projectId, terminologyId);
          // Setup deferred
          var deferred = $q.defer();

          // Make POST call
          gpService.increment();
          $http.post(
            projectUrl + 'concept/children?projectId=' + projectId + '&terminologyId='
              + terminologyId + '&terminology=' + terminology + '&version=' + version
              + (translationId != null ? '&translationId=' + translationId : ''),
            utilService.prepPfs(pfs)).then(
          // success
          function(response) {
            console.debug('  children = ', response.data);
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

        // get concept with descriptions
        this.getFullConcept = function(projectId, terminologyId, terminology, version,
          translationId) {

          console.debug('getFullConcept', projectId, terminologyId, terminology, version,
            translationId);
          // Setup deferred
          var deferred = $q.defer();

          // Make POST call
          gpService.increment();
          $http.get(
            projectUrl + 'concept?projectId=' + projectId + '&terminologyId=' + terminologyId
              + '&terminology=' + terminology + '&version=' + version
              + (translationId != null ? '&translationId=' + translationId : '')).then(
          // success
          function(response) {
            console.debug('  concept = ', response.data);
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

        // get terminology editions
        this.getTerminologyEditions = function(project) {
          console.debug('getTerminologyEditions', project);
          var deferred = $q.defer();

          // Get projects
          gpService.increment();
          $http.post(projectUrl + 'terminology/all', project).then(
          // success
          function(response) {
            console.debug('  editions = ', response.data);
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

        // get all terminology editions
        this.getAllTerminologyEditions = function() {
          console.debug('getAllTerminologyEditions');
          var deferred = $q.defer();

          // Get projects
          gpService.increment();
          $http.get(projectUrl + 'terminology/global').then(
          // success
          function(response) {
            console.debug(' all editions = ', response.data);
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

        // get terminology versions
        this.getTerminologyVersions = function(project, terminology) {
          console.debug('getTerminologyVersions', project, terminology);
          var deferred = $q.defer();

          // Get projects
          gpService.increment();
          $http.post(projectUrl + 'terminology/' + terminology + '/all', project).then(
          // success
          function(response) {
            console.debug('  version = ', response.data);
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

        // Get modules
        this.getModules = function(project, terminology, version) {
          console.debug('getModules', project, terminology, version);
          var deferred = $q.defer();

          // Get projects
          gpService.increment();
          $http.post(projectUrl + 'modules?terminology=' + terminology + '&version=' + version,
            project).then(
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

        // Get standard description types
        this.getStandardDescriptionTypes = function(terminology, version) {
          console.debug('getStandardDescriptionTypes', terminology, version);
          var deferred = $q.defer();

          // Get projects
          gpService.increment();
          $http.get(
            projectUrl + 'descriptiontypes?terminology=' + terminology + '&version=' + version)
            .then(
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

        // get log for project and refset/translation
        this.getLog = function(projectId, objectId, filter) {
          console.debug('getLog');
          var deferred = $q.defer();

          // Assign user to project
          gpService.increment();

          $http.get(
            projectUrl + 'log?projectId=' + projectId + '&objectId=' + objectId + '&lines=1000'
              + (filter != '' ? '&query=' + filter : ''), {
              transformResponse : [ function(data) {
                // Data response is plain text at this point
                // So just return it, or do your parsing here
                return data;
              } ]
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

        // get google translate result for given text
        this.translate = function(projectId, text, language) {
          console.debug('getLog');
          var deferred = $q.defer();

          // Assign user to project
          gpService.increment();

          $http.get(
            projectUrl + 'translate?projectId=' + projectId + '&text=' + text + '&language='
              + language, {
              transformResponse : [ function(data) {
                // Data response is plain text at this point
                // So just return it, or do your parsing here
                return data;
              } ]
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

        // assign user to project
        this.getReplacementConcepts = function(projectId, conceptId, terminology, version) {
          console.debug('getReplacementConcepts', projectId, conceptId, terminology, version);
          var deferred = $q.defer();

          // Assign user to project
          gpService.increment();
          $http.get(
            projectUrl + 'concept/replacements' + '?projectId=' + projectId + '&conceptId='
              + conceptId + '&terminology=' + terminology + '&version=' + version).then(
          // success
          function(response) {
            console.debug('  project = ', response.data);
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

        // Dynamically load terminology handler
        this.loadTerminologyHandler = function(project) {
          if (type === undefined)
            type = 'text/javascript';
          if (url) {
            var script = document.querySelector("script[src*='" + url + "']");
            if (!script) {
              var heads = document.getElementsByTagName("head");
              if (heads && heads.length) {
                var head = heads[0];
                if (head) {
                  script = document.createElement('script');
                  script.setAttribute('src', url);
                  script.setAttribute('type', type);
                  if (charset)
                    script.setAttribute('charset', charset);
                  head.appendChild(script);
                }
              }
            }
            return script;
          }
        };
        // end
      } ]);
