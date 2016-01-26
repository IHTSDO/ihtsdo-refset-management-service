// Login controller
tsApp.controller('LoginCtrl', [
  '$scope',
  '$http',
  '$location',
  'securityService',
  'gpService',
  'utilService',
  'tabService',
  'projectService',
  function($scope, $http, $location, securityService, gpService, utilService, tabService,
    projectService) {
    console.debug('configure LoginCtrl');

    // Clear user info
    securityService.clearUser();

    // Declare the user
    $scope.user = securityService.getUser();

    // Login function
    $scope.login = function(name, password) {
      if (!name) {
        alert('You must specify a user name');
        return;
      } else if (!password) {
        alert('You must specify a password');
        return;
      }

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