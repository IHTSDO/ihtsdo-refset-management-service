// Error service
tsApp
  .service(
    'utilService',
    [
      '$location',
      function($location) {
        console.debug('configure utilService');
        // declare the error
        this.error = {
          message : null
        };

        // tinymce options
        var tinymceOptions = {
          menubar : false,
          statusbar : false,
          plugins : "autolink autoresize link image charmap searchreplace lists paste",
          toolbar : "undo redo | styleselect lists | bold italic underline strikethrough | charmap link image",
          forced_root_block : ''
        }

        // Get tinymce options
        this.getTinymceOptions = function() {
          return tinymceOptions;
        }

        // Get tinymce options
        this.getTinymceOptions = function() {
          return tinymceOptions;
        }

        // Sets the error
        this.setError = function(message) {
          this.error.message = message;
        }

        // Clears the error
        this.clearError = function() {
          this.error.message = null;
        }
        // Handle error message
        this.handleError = function(response) {
          console.debug("Handle error: ", response);
          this.error.message = response.data;
          // If authtoken expired, relogin
          if (this.error.message && this.error.message.indexOf("AuthToken") != -1) {
            // Reroute back to login page with "auth token has
            // expired" message
            $location.path("/");
          }
        }

        // Convert date to a string
        this.toDate = function(lastModified) {
          var date = new Date(lastModified);
          var year = "" + date.getFullYear();
          var month = "" + (date.getMonth() + 1);
          if (month.length == 1) {
            month = "0" + month;
          }
          var day = "" + date.getDate();
          if (day.length == 1) {
            day = "0" + day;
          }
          var hour = "" + date.getHours();
          if (hour.length == 1) {
            hour = "0" + hour;
          }
          var minute = "" + date.getMinutes();
          if (minute.length == 1) {
            minute = "0" + minute;
          }
          var second = "" + date.getSeconds();
          if (second.length == 1) {
            second = "0" + second;
          }
          return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
        }

        // Convert date to a short string
        this.toShortDate = function(lastModified) {
          var date = new Date(lastModified);
          var year = "" + date.getFullYear();
          var month = "" + (date.getMonth() + 1);
          if (month.length == 1) {
            month = "0" + month;
          }
          var day = "" + date.getDate();
          if (day.length == 1) {
            day = "0" + day;
          }
          return year + "-" + month + "-" + day;
        }

        // Convert date to a simple string
        this.toSimpleDate = function(lastModified) {
          var date = new Date(lastModified);
          var year = "" + date.getFullYear();
          var month = "" + (date.getMonth() + 1);
          if (month.length == 1) {
            month = "0" + month;
          }
          var day = "" + date.getDate();
          if (day.length == 1) {
            day = "0" + day;
          }
          return year + month + day;
        }

        // Utility for cleaning a query
        this.cleanQuery = function(queryStr) {
          if (queryStr == null) {
            return "";
          }
          var cleanQuery = queryStr;
          // Replace all slash characters
          cleanQuery = queryStr.replace(new RegExp('[/\\\\]', 'g'), ' ');
          // Remove brackets if not using a fielded query
          if (queryStr.indexOf(':') == -1) {
            cleanQuery = queryStr.replace(new RegExp('[^a-zA-Z0-9:\\.\\-\'\\*]', 'g'), ' ');
          }
          // console.debug(queryStr, " => ", cleanQuery);
          return cleanQuery;
        }

        // Table sorting mechanism
        this.setSortField = function(table, field, paging) {
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

        // Return up or down sort chars if sorted
        this.getSortIndicator = function(table, field, paging) {
          if (paging[table].ascending == null) {
            return "";
          }
          if (paging[table].sortField == field && paging[table].ascending) {
            return "▴";
          }
          if (paging[table].sortField == field && !paging[table].ascending) {
            return "▾";
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
            newArray.sort(this.sort_by(paging.sortField, paging.ascending))
          }

          // apply filter
          if (paging.filter) {
            newArray = this.getArrayByFilter(newArray, paging.filter);
          }

          // get the page indices
          var fromIndex = (paging.page - 1) * pageSize;
          var toIndex = Math.min(fromIndex + pageSize, array.length);

          // slice the array
          var results = newArray.slice(fromIndex, toIndex);

          // add the total count before slicing
          results.totalCount = newArray.length;

          return results;
        }

        // function for sorting an array by (string) field and direction
        this.sort_by = function(field, reverse) {

          // key: function to return field value from object
          var key = function(x) {
            return x[field]
          };

          // convert reverse to integer (1 = ascending, -1 =
          // descending)
          reverse = !reverse ? 1 : -1;

          return function(a, b) {
            return a = key(a), b = key(b), reverse * ((a > b) - (b > a));
          }
        }

        // Get array by filter text matching terminologyId or name
        this.getArrayByFilter = function(array, filter) {
          var newArray = [];

          for ( var object in array) {

            if (this.objectContainsFilterText(array[object], filter)) {
              newArray.push(array[object]);
            }
          }
          return newArray;
        }

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
        }

        // Get words of a string
        this.getWords = function(str) {
          // Same as in tinymce options
          return str.match(/[^\s,\.]+/g);
        }

      } ]);

// Glass pane service
tsApp.service('gpService', function() {
  console.debug('configure gpService');
  // declare the glass pane counter
  this.glassPane = {
    counter : 0,
    messages : []
  };

  this.isGlassPaneSet = function() {
    return this.glassPane.counter;
  }

  this.isGlassPaneNegative = function() {
    return this.glassPane.counter < 0;
  }

  // Increments glass pane counter
  this.increment = function(message) {
    if (message) {
      this.glassPane.messages.push(message);
    }
    this.glassPane.counter++;
  }

  // Decrements glass pane counter
  this.decrement = function(message) {
    if (message) {
      var index = this.glassPane.messages.indexOf(message); // <-- Not supported in <IE9
      if (index !== -1) {
        this.glassPane.messages.splice(index, 1);
      }
    }
    this.glassPane.counter--;
  }

});

// Security service
tsApp.service('securityService', [ '$http', '$location', '$q', 'utilService', 'gpService',
  function($http, $location, $q, utilService, gpService) {
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
    }

    // Gets the user
    this.getUser = function() {
      return user;
    }

    // Sets the user
    this.setUser = function(data) {
      user.userName = data.userName;
      user.name = data.name;
      user.authToken = data.authToken;
      user.password = "";
      user.applicationRole = data.applicationRole;
      user.userPreferences = data.userPreferences;
    }

    // Clears the user
    this.clearUser = function() {
      user.userName = null;
      user.name = null;
      user.authToken = null;
      user.password = null;
      user.applicationRole = null;
      user.userPreferences = null;
    }

    var httpClearUser = this.clearUser;

    // isLoggedIn function
    this.isLoggedIn = function() {
      return user.authToken;
    }

    // isAdmin function
    this.isAdmin = function() {
      return user.applicationRole == 'ADMIN';
    }

    // isUser function
    this.isUser = function() {
      return user.applicationRole == 'ADMIN' || user.applicationRole == 'USER';
    }

    // Logout
    this.logout = function() {
      if (user.authToken == null) {
        alert("You are not currently logged in");
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
        window.location.href = "${logout.url}";
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
    }

    // Accessor for search params
    this.getSearchParams = function() {
      return searchParams;
    }

    // get all users
    this.getUsers = function() {
      console.debug("getUsers");
      var deferred = $q.defer();

      // Get users
      gpService.increment()
      $http.get(securityUrl + 'user/users').then(
      // success
      function(response) {
        console.debug("  users = ", response.data);
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

    // add user
    this.addUser = function(user) {
      console.debug("addUser");
      var deferred = $q.defer();

      // Add user
      gpService.increment()
      $http.put(securityUrl + 'user/add', user).then(
      // success
      function(response) {
        console.debug("  user = ", response.data);
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

    // update user
    this.updateUser = function(user) {
      console.debug("updateUser");
      var deferred = $q.defer();

      // Add user
      gpService.increment()
      $http.post(securityUrl + 'user/update', user).then(
      // success
      function(response) {
        console.debug("  user = ", response.data);
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

    // remove user
    this.removeUser = function(user) {
      console.debug("removeUser");
      var deferred = $q.defer();

      // Add user
      gpService.increment();
      $http['delete'](securityUrl + 'user/remove' + "/" + user.id).then(
      // success
      function(response) {
        console.debug("  user = ", response.data);
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

    // get application roles
    this.getApplicationRoles = function() {
      console.debug("getApplicationRoles");
      var deferred = $q.defer();

      // Get application roles
      gpService.increment()
      $http.get(securityUrl + 'roles').then(
      // success
      function(response) {
        console.debug("  roles = ", response.data);
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

    // Finds users as a list
    this.findUsersAsList = function(queryStr, pfs) {
      console.debug("findUsersAsList", queryStr, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(securityUrl + "user/find" + "?query=" + queryStr, pfs)
      //+ encodeURIComponent(utilService.cleanQuery(queryStr)), pfs)
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
    // add user preferences
    this.addUserPreferences = function(userPreferences) {
      console.debug("addUserPreferences");
      var deferred = $q.defer();

      gpService.increment()
      $http.put(securityUrl + 'user/preferences/add', userPreferences).then(
      // success
      function(response) {
        console.debug("  userPreferences = ", response.data);
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

    // update user preferences
    this.updateUserPreferences = function(userPreferences) {
      console.debug("updateUserPreferences");
      var deferred = $q.defer();

      gpService.increment()
      $http.post(securityUrl + 'user/preferences/update', userPreferences).then(
      // success
      function(response) {
        console.debug("  userPreferences = ", response.data);
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

    // remove user preferences
    this.removeUserPreferences = function(userPreferences) {
      console.debug("removeUserPreferences");
      var deferred = $q.defer();

      gpService.increment();
      $http['delete'](securityUrl + 'user/preferences/remove' + "/" + userPreferences.id).then(
      // success
      function(response) {
        console.debug("  userPreferences = ", response.data);
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
      console.debug("tab label", tab.label);
      return tab.label != 'Admin' || securityService.getUser().applicationRole == 'ADMIN';
    }

    // Sets the selected tab
    this.setSelectedTab = function(tab) {
      this.selectedTab = tab;
    }

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
    }

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
      url = url + "/websocket";
      console.debug("url = " + url);
      return url;

    }

    this.connection = new WebSocket(this.getUrl());

    this.connection.onopen = function() {
      // Log so we know it is happening
      console.log('Connection open');
    }

    this.connection.onclose = function() {
      // Log so we know it is happening
      console.log('Connection closed');
    }

    // error handler
    this.connection.onerror = function(error) {
      utilService.handleError(error, null, null, null);
    }

    // handle receipt of a message
    this.connection.onmessage = function(e) {
      var message = e.data;
      console.log("MESSAGE: " + message);
      // TODO: what else to do?
    }

    // Send a message to the websocket server endpoint
    this.send = function(message) {
      this.connection.send(JSON.stringify(message));
    }

  } ]);