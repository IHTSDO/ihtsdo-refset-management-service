// Login controller
tsApp.controller('LoginCtrl', [
  '$scope',
  '$http',
  '$location',
  '$cookieStore',
  'securityService',
  'gpService',
  'utilService',
  'tabService',
  'projectService',
  'appConfig',
  function($scope, $http, $location, $cookieStore, securityService, gpService, utilService,
    tabService, projectService, appConfig) {
    console.debug('configure LoginCtrl');

    $scope.appConfig = appConfig;

    // Clear user info
    securityService.clearUser();
    console.debug("user = ", securityService.getUser());
    // Declare the user
    $scope.user = securityService.getUser();
    console.debug("user = ", $scope.user);

    // Login function
    $scope.login = function(name, password) {
      if (!name) {
        alert('You must specify a user name');
        return;
      } else if (!password) {
        alert('You must specify a password');
        return;
      }

      // If this is the dev environment and we have a "local.ihtsdotools.org"
      // URL,
      // then obtain the IHTSDO cookies and send those in as the password
      // INSTEAD
      // of the user-typed password.
      console.debug('xxxlocation=', $location.path());
      console.debug('xxxcookies=', $cookieStore);

      // login
      gpService.increment();
      return $http({
        url : securityUrl + 'authenticate/' + name,
        method : 'POST',
        data : password,
        headers : {
          'Content-Type' : 'text/plain'
        }
      }).then(
      // success
      function(response) {
        utilService.clearError();
        console.debug('user = ', response.data);
        securityService.setUser(response.data);

        // set request header authorization and reroute
        $http.defaults.headers.common.Authorization = response.data.authToken;
        if (response.data.userPreferences && response.data.userPreferences.lastTab) {
          $location.path(response.data.userPreferences.lastTab);
        } else {
          $location.path('/directory');
        }
        gpService.decrement();
      },

      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
    };

    // Logout function
    $scope.logout = function() {
      securityService.logout();
    };
  } ]);