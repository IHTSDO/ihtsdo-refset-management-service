// Util service
tsApp
  .service(
    'utilService',
    [
      '$location',
      '$anchorScroll',
      '$uibModal',
      function($location, $anchorScroll, $uibModal) {
        console.debug('configure utilService');
        // declare the error
        this.error = {
          message : null,
          longMessage : null,
          expand : false
        };

        this.terminologies = [];

        // tinymce options
        this.tinymceOptions = {
          menubar : false,
          statusbar : false,
          plugins : 'autolink link image charmap searchreplace lists paste',
          toolbar : 'undo redo | styleselect lists | bold italic underline strikethrough | charmap link image',
          forced_root_block : ''
        };

        // Get page sizes
        this.getPageSizes = function() {
          return [ {
            name : 10,
            value : 10
          }, {
            name : 20,
            value : 30
          }, {
            name : 40,
            value : 50
          }, {
            name : 100,
            value : 100
          }, {
            name : 500,
            value : 500
          }, {
            name : 'All',
            value : 100000
          } ];
        }
        // Prep query
        this.prepQuery = function(query, wildcardFlag) {
          if (!query) {
            return '';
          }

          // Add a * to the filter if set and doesn't contain a ':' indicating
          // filter search
          if (!wildcardFlag && query.indexOf("(") == -1 && query.indexOf(":") == -1
            && query.indexOf("=") == -1 && query.indexOf("\"") == -1) {
            var query2 = query.concat('*');
            return encodeURIComponent(query2);
          }
          return encodeURIComponent(query);
        };

        // Prep pfs filter
        this.prepPfs = function(pfs) {
          if (!pfs) {
            return {};
          }

          // Add a * to the filter if set and doesn't contain a :
          if (pfs.queryRestriction && pfs.queryRestriction.indexOf(":") == -1
            && pfs.queryRestriction.indexOf("=") == -1 && pfs.queryRestriction.indexOf("\"") == -1) {
            var pfs2 = angular.copy(pfs);
            pfs2.queryRestriction += "*";
            return pfs2;
          }
          return pfs;
        };

        this.toText = function(camelCase, captializefirst) {
          if (capitalizeFirst) {
            var str = camelCase.replace(/([A-Z]+)/g, " $1").replace(/([A-Z][a-z])/g, " $1");
            return str[0].toUpperCase() + str.slice(1)
          } else {
            return camelCase.replace(/([A-Z]+)/g, " $1").replace(/([A-Z][a-z])/g, " $1")
          }
        }

        this.toCamelCase = function(text) {
          // Lower cases the string
          return text.toLowerCase()
          // Replaces any - or _ characters with a space
          .replace(/[-_]+/g, ' ')
          // Removes any non alphanumeric characters
          .replace(/[^\w\s]/g, '')
          // remove duplicate spaces
          .replace(/\s{2,}/g, ' ')
          // Uppercases the first character in each group immediately following
          // a space
          // (delimited by spaces)
          .replace(/ (.)/g, function($1) {
            return $1.toUpperCase();
          })
          // Removes spaces
          .replace(/ /g, '');
        }

        this.yyyymmdd = function(dateIn) {
          var yyyy = dateIn.getFullYear();
          // getMonth() is zero-based
          var mm = dateIn.getMonth() + 1;
          var dd = dateIn.getDate();
          // Leading zeros for mm and dd
          return String(10000 * yyyy + 100 * mm + dd);
        }

        // Sets the error
        this.setError = function(message) {
          this.error.message = message;
        };

        // Clears the error
        this.clearError = function() {
          this.error.message = null;
          this.error.longMessage = null;
          this.error.expand = false;
        };

        // Handle error message
        this.handleError = function(response) {
          console.debug('Handle error: ', response);
          if (response.data && response.data.length > 120) {
            this.error.message = "Unexpected error, click the icon to view attached full error";
            this.error.longMessage = response.data;
          } else {
            this.error.message = response.data;
          }
          // handle no message
          if (!this.error.message) {
            this.error.message = "Unexpected server side error.";
          }
          // If authtoken expired, relogin
          if (this.error.message && this.error.message.indexOf('AuthToken') != -1) {
            // Reroute back to login page with 'auth token has
            // expired' message
            $location.path('/login');
          } else {
            // scroll to top of page
            $location.hash('top');
            $anchorScroll();
          }
        };

        // Dialog error handler
        this.handleDialogError = function(errors, error) {
          console.debug('Handle dialog error: ', errors, error);
          // handle long error
          if (error && error.length > 100) {
            errors[0] = "Unexpected error, click the icon to view attached full error";
            errors[1] = error;
          } else {
            errors[0] = error;
          }
          // handle no message
          if (!error) {
            errors[0] = "Unexpected server side error.";
          }
          // If authtoken expired, relogin
          if (error && error.indexOf('AuthToken') != -1) {
            // Reroute back to login page with 'auth token has
            // expired' message
            $location.path('/login');
          }
          // otherwise clear the top-level error
          else {
            this.clearError();
          }
        };

        // Compose a URL properly for opening new window
        this.composeUrl = function(extension) {
          var currentUrl = $location.absUrl();
          var baseUrl = currentUrl.substring(0, currentUrl.indexOf('#') + 1);
          var newUrl = baseUrl + extension;
          return newUrl;
        }

        // Return the time for a YYYYMMDD string
        function parseYYYYMMDD(str) {
          if (!/^(\d){8}$/.test(str))
            return 0;
          var y = str.substr(0, 4), m = str.substr(4, 2) - 1, d = str.substr(6, 2);
          return new Date(y, m, d).getTime();
        }

        // Convert date to a string
        var workDate = new Date();
        this.toDate = function(lastModified) {
          var date = new Date(lastModified + ((60 + workDate.getTimezoneOffset()) * 60000));
          var year = '' + date.getFullYear();
          var month = '' + (date.getMonth() + 1);
          if (month.length == 1) {
            month = '0' + month;
          }
          var day = '' + date.getDate();
          if (day.length == 1) {
            day = '0' + day;
          }
          var hour = '' + date.getHours();
          if (hour.length == 1) {
            hour = '0' + hour;
          }
          var minute = '' + date.getMinutes();
          if (minute.length == 1) {
            minute = '0' + minute;
          }
          var second = '' + date.getSeconds();
          if (second.length == 1) {
            second = '0' + second;
          }
          return year + '-' + month + '-' + day + ' ' + hour + ':' + minute + ':' + second;
        };

        // Convert date to a short string
        this.toShortDate = function(lastModified) {
          var date = new Date(lastModified + ((60 + workDate.getTimezoneOffset()) * 60000));
          var year = '' + date.getFullYear();
          var month = '' + (date.getMonth() + 1);
          if (month.length == 1) {
            month = '0' + month;
          }
          var day = '' + date.getDate();
          if (day.length == 1) {
            day = '0' + day;
          }
          return year + '-' + month + '-' + day;
        };

        // Convert date to a simple string
        this.toSimpleDate = function(lastModified) {
          if (lastModified == null) {
        	  return '';
          }
          var date = new Date(lastModified + ((60 + workDate.getTimezoneOffset()) * 60000));
          var year = '' + date.getFullYear();
          var month = '' + (date.getMonth() + 1);
          if (month.length == 1) {
            month = '0' + month;
          }
          var day = '' + date.getDate();
          if (day.length == 1) {
            day = '0' + day;
          }
          return year + month + day;
        };

        this.toWCISimpleDate = function(date) {
        	if (date == null) {
            	return '';
            }
        	var year = '' + date.getFullYear();
            var month = '' + (date.getMonth() + 1);
            if (month.length == 1) {
              month = '0' + month;
            }
            var day = '' + date.getDate();
            if (day.length == 1) {
              day = '0' + day;
            }
            return year + month + day;
          };

        // Table sorting mechanism
        this.setSortField = function(table, field, paging) {
          console.debug("utilService set sort field", table, field, paging);
          paging[table].sortField = field;
          // reset page number too
          paging[table].page = 1;
          // handles null case also
          if (!paging[table].ascending) {
            paging[table].ascending = true;
          } else {
            paging[table].ascending = false;
          }
          // reset the paging for the correct table
          for ( var key in paging) {
            if (paging.hasOwnProperty(key)) {
              if (key == table)
                paging[key].page = 1;
            }
          }
        };

        this.uniq = function uniq(a) {
          var seen = {};
          return a.filter(function(item) {
            return seen.hasOwnProperty(item) ? false : (seen[item] = true);
          });
        };

        // Return up or down sort chars if sorted
        this.getSortIndicator = function(table, field, paging) {
          if (paging[table].ascending == null) {
            return '';
          }
          if (paging[table].sortField == field && paging[table].ascending) {
            return '▴';
          }
          if (paging[table].sortField == field && !paging[table].ascending) {
            return '▾';
          }
        };

        // Helper to get a paged array with show/hide flags
        // and filtered by query string
        this.getPagedArray = function(array, paging, pageSize) {
          var newArray = new Array();

          // if array blank or not an array, return blank list
          if (array == null || array == undefined || !Array.isArray(array)) {
            return newArray;
          }

          newArray = array;

          // apply sort if specified
          if (paging.sortField) {
            // if ascending specified, use that value, otherwise use false
            newArray.sort(this.sortBy(paging.sortField, paging.ascending));
          }

          // apply filter
          if (paging.filter) {
            newArray = this.getArrayByFilter(newArray, paging.filter);
          }

          // apply active status filter
          if (paging.typeFilter) {
            newArray = this.getArrayByActiveStatus(newArray, paging.typeFilter);
          }

          // get the page indices
          var fromIndex = (paging.page - 1) * pageSize;
          var toIndex = Math.min(fromIndex + pageSize, array.length);

          // slice the array
          var results = newArray.slice(fromIndex, toIndex);

          // add the total count before slicing
          results.totalCount = newArray.length;

          return results;
        };

        // function for sorting an array by (string) field and direction
        this.sortBy = function(field, reverse) {

          // key: function to return field value from object
          var key = function(x) {
            return x[field];
          };

          // convert reverse to integer (1 = ascending, -1 =
          // descending)
          reverse = !reverse ? 1 : -1;

          return function(a, b) {
            return a = key(a), b = key(b), reverse * ((a > b) - (b > a));
          };
        };

        // Get array by filter text matching terminologyId or name
        this.getArrayByFilter = function(array, filter) {
          var newArray = [];

          for ( var object in array) {

            if (this.objectContainsFilterText(array[object], filter)) {
              newArray.push(array[object]);
            }
          }
          return newArray;
        };

        // Get array by filter on conceptActive status
        this.getArrayByActiveStatus = function(array, filter) {
          var newArray = [];

          for ( var object in array) {

            if (array[object].conceptActive && filter == 'Active') {
              newArray.push(array[object]);
            } else if (!array[object].conceptActive && filter == 'Inactive') {
              newArray.push(array[object]);
            } else if (array[object].conceptActive && filter == 'All') {
              newArray.push(array[object]);
            }
          }
          return newArray;
        };

        // Returns true if any field on object contains filter text
        this.objectContainsFilterText = function(object, filter) {

          if (!filter || !object)
            return false;

          for ( var prop in object) {
            var value = object[prop];
            // check property for string, note this will cover child elements
            if (value && value.toString().toLowerCase().indexOf(filter.toLowerCase()) != -1) {
              return true;
            }
          }

          return false;
        };

        // Finds the object in a list by the field
        this.findBy = function(list, obj, field) {

          // key: function to return field value from object
          var key = function(x) {
            return x[field];
          };

          for (var i = 0; i < list.length; i++) {
            if (key(list[i]) == key(obj)) {
              return list[i];
            }
          }
          return null;
        };

        // Get words of a string
        this.getWords = function(str) {
          // Same as in tinymce options
          return str.match(/[^\s,\.]+/g);
        };

        // Single and multiple-word ordered phrases
        this.getPhrases = function(str) {
          var words = str.match(/[^\s,\.]+/g);
          var phrases = [];

          for (var i = 0; i < words.length; i++) {
            for (var j = i + 1; j <= words.length; j++) {
              var phrase = words.slice(i, j).join(' ');
              // a phrase have at least 5 chars and no start/end words that are
              // purely punctuation
              if (phrase.length > 5 && words[i].match(/.*[A-Za-z0-9].*/)
                && words[j - 1].match(/.*[A-Za-z0-9].*/)) {
                phrases.push(phrase.toLowerCase());
              }
            }
          }
          return phrases;
        };

      } ]);

// Glass pane service
tsApp.service('gpService', [ '$timeout', function($timeout) {
  console.debug('configure gpService');
  // declare the glass pane counter
  var glassPane = {
    counter : 0,
    messages : [],
    enabled : true,
    timeout : false
  };

  this.getGlassPane = function() {
    return glassPane;
  }

  this.isGlassPaneSet = function() {
    return glassPane.enabled;
  };

  this.isGlassPaneNegative = function() {
    return glassPane.counter < 0;
  };

  // Increments glass pane counter
  this.increment = function(message) {
    if (message) {
      glassPane.messages.push(message);
    }
    glassPane.counter++;
    if (!glassPane.timeout) {
      $timeout(function() {
        if (glassPane.counter > 0) {
          glassPane.enabled = true;
        }
        glassPane.timeout = false;
      }, 100);
    }
  };

  // Decrements glass pane counter
  this.decrement = function(message) {
    if (message) {
      var index = glassPane.messages.indexOf(message);
      if (index !== -1) {
        glassPane.messages.splice(index, 1);
      }
    }
    glassPane.counter--;
    if (glassPane.counter == 0) {
      $timeout(function() {
        if (glassPane.counter == 0) {
          glassPane.enabled = false;
        }
      }, 100);
    }
  };

} ]);

// Security service
tsApp.service('securityService', [
  '$http',
  '$location',
  '$q',
  '$cookies',
  'utilService',
  'gpService',
  'appConfig',
  function($http, $location, $q, $cookies, utilService, gpService, appConfig) {
    console.debug('configure securityService');

    // Declare the user
    var user = {
      userName : null,
      password : null,
      name : null,
      authToken : null,
      applicationRole : null,
      userPreferences : null
    };

    // Search results
    var searchParams = {
      page : 1,
      query : null
    };

    // Gets the user
    this.getUser = function() {

      // Determine if page has been reloaded
      if (!$http.defaults.headers.common.Authorization) {
        // Retrieve cookie
        if ($cookies.get('user')) {
          var cookieUser = JSON.parse($cookies.get('user'));
          // If there is a user cookie, load it
          if (cookieUser) {
            this.setUser(cookieUser);
            $http.defaults.headers.common.Authorization = user.authToken;
          }
        }

        // If no cookie, just come in as "guest" user
        else {
          this.setGuestUser();
        }
      }
      return user;
    };

    // Sets the user
    this.setUser = function(data) {
      user.userName = data.userName;
      user.name = data.name;
      user.authToken = data.authToken;
      user.password = '';
      user.applicationRole = data.applicationRole;
      user.userPreferences = data.userPreferences;

      // Whenver set user is called, we should save a cookie
      $cookies.put('user', JSON.stringify(user));

    };

    this.setGuestUser = function() {
      user.userName = 'guest';
      user.name = 'Guest';
      user.authToken = 'guest';
      user.password = 'guest';
      user.applicationRole = 'VIEWER';
      user.userPreferences = {};

      // Whenever set user is called, we should save a cookie
      $cookies.put('user', JSON.stringify(user));

    };

    // Clears the user
    this.clearUser = function() {
      user.userName = null;
      user.name = null;
      user.authToken = null;
      user.password = null;
      user.applicationRole = null;
      user.userPreferences = null;
      $cookies.remove('user');
    };

    var httpClearUser = this.clearUser;

    // isLoggedIn function
    this.isLoggedIn = function() {
      return user.authToken && user.authToken != 'guest';
    };

    // isAdmin function
    this.isAdmin = function() {
      return user.applicationRole == 'ADMIN';
    };

    // isUser function
    this.isUser = function() {
      return user.applicationRole == 'ADMIN' || user.applicationRole == 'USER';
    };

    // Logout
    this.logout = function() {
      if (user.authToken == null) {
        alert('You are not currently logged in');
        return;
      }
      gpService.increment();

      // logout
      $http.get(securityUrl + 'logout/' + user.authToken).then(
      // success
      function(response) {

        // clear scope variables
        httpClearUser();

        // clear http authorization header
        $http.defaults.headers.common.Authorization = null;
        gpService.decrement();
        window.location.href = appConfig['logout.url'];
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
    };

    // Accessor for search params
    this.getSearchParams = function() {
      return searchParams;
    };

    // get all users
    this.getUsers = function() {
      console.debug('getUsers');
      var deferred = $q.defer();

      // Get users
      gpService.increment();
      $http.get(securityUrl + 'user/users').then(
      // success
      function(response) {
        console.debug('  users = ', response.data);
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

    // get user for auth token
    this.getUserForAuthToken = function() {
      console.debug('getUserforAuthToken');
      var deferred = $q.defer();

      // Get users
      gpService.increment();
      $http.get(securityUrl + 'user').then(
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
    // add user
    this.addUser = function(user) {
      console.debug('addUser');
      var deferred = $q.defer();

      // Add user
      gpService.increment();
      $http.put(securityUrl + 'user/add', user).then(
      // success
      function(response) {
        console.debug('  user = ', response.data);
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

    // update user
    this.updateUser = function(user) {
      console.debug('updateUser');
      var deferred = $q.defer();

      // Add user
      gpService.increment();
      $http.post(securityUrl + 'user/update', user).then(
      // success
      function(response) {
        console.debug('  user = ', response.data);
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

    // remove user
    this.removeUser = function(user) {
      console.debug('removeUser');
      var deferred = $q.defer();

      // Add user
      gpService.increment();
      $http['delete'](securityUrl + 'user/remove/' + user.id).then(
      // success
      function(response) {
        console.debug('  user = ', response.data);
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

    // get application roles
    this.getApplicationRoles = function() {
      console.debug('getApplicationRoles');
      var deferred = $q.defer();

      // Get application roles
      gpService.increment();
      $http.get(securityUrl + 'roles').then(
      // success
      function(response) {
        console.debug('  roles = ', response.data);
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

    // Finds users as a list
    this.findUsersAsList = function(query, pfs) {
      console.debug('findUsersAsList', query, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(securityUrl + 'user/find?query=' + utilService.prepQuery(query),
        utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  users = ', response.data);
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

    // update user preferences
    this.updateUserPreferences = function(userPreferences) {
      console.debug('updateUserPreferences');
      // skip if user preferences is not set
      if (!userPreferences) {
        return;
      }

      // Whenever we update user preferences, we need to update the cookie
      $cookies.put('user', JSON.stringify(user));

      var deferred = $q.defer();

      gpService.increment();
      $http.post(securityUrl + 'user/preferences/update', userPreferences).then(
      // success
      function(response) {
        console.debug('  userPreferences = ', response.data);
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

    // Get config properties
    this.getConfigProperties = function() {
      console.debug("get config properties");
      var deferred = $q.defer();

      gpService.increment();
      $http.get(securityUrl + '/properties').then(
      // success
      function(response) {
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // Error
      function(response) {
        gpService.decrement();
        utilService.handleError(response);
        deferred.reject();
      });
      return deferred.promise;
    };
  } ]);

// Tab service
tsApp.service('tabService', [ '$location', 'utilService', 'gpService', 'securityService',
  function($location, utilService, gpService, securityService) {
    console.debug('configure tabService');

    // Available tabs
    this.tabs = [ {
      link : '#/directory',
      label : 'Directory'
    }, {
      link : '#/refset',
      label : 'Refset'
    }, {
      link : '#/translation',
      label : 'Translation'
    }, {
      link : '#/admin',
      label : 'Admin'
    } ];

    this.selectedTab = this.tabs[0];

    // Show admin tab for admins only
    this.showTab = function(tab) {
      console.debug('tab label', tab.label);
      return tab.label != 'Admin' || securityService.getUser().applicationRole == 'ADMIN';
    };

    // Sets the selected tab
    this.setSelectedTab = function(tab) {
      this.selectedTab = tab;
    };

    // sets the selected tab by label
    // to be called by controllers when their
    // respective tab is selected
    this.setSelectedTabByLabel = function(label) {
      for (var i = 0; i < this.tabs.length; i++) {
        if (this.tabs[i].label === label) {
          this.selectedTab = this.tabs[i];
          break;
        }
      }
    };

  } ]);

// Websocket service

tsApp.service('websocketService', [ '$location', 'utilService', 'gpService',
  function($location, utilService, gpService) {
    console.debug('configure websocketService');
    this.data = {
      message : null
    };

    // Determine URL without requiring injection
    // should support wss for https
    // and assumes REST services and websocket are deployed together
    this.getUrl = function() {
      var url = window.location.href;
      url = url.replace('http', 'ws');
      url = url.replace('index.html', '');
      url = url.substring(0, url.indexOf('#'));
      url = url + '/websocket';
      console.debug('url = ' + url);
      return url;

    };

    this.connection = new WebSocket(this.getUrl());

    this.connection.onopen = function() {
      // Log so we know it is happening
      console.log('Connection open');
    };

    this.connection.onclose = function() {
      // Log so we know it is happening
      console.log('Connection closed');
    };

    // error handler
    this.connection.onerror = function(error) {
      utilService.handleError(error, null, null, null);
    };

    // handle receipt of a message
    this.connection.onmessage = function(e) {
      var message = e.data;
      console.log('MESSAGE: ' + message);
      // what else to do?
    };

    // Send a message to the websocket server endpoint
    this.send = function(message) {
      this.connection.send(JSON.stringify(message));
    };

  } ]);