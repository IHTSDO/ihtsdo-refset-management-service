// Login controller
tsApp
  .controller(
    'LoginCtrl',
    [
      '$scope',
      '$http',
      '$window',
      '$location',
      '$cookies',
      'securityService',
      'gpService',
      'utilService',
      'tabService',
      'projectService',
      'appConfig',
      function($scope, $http, $window, $location, $cookies, securityService, gpService,
        utilService, tabService, projectService, appConfig) {
        console.debug('configure LoginCtrl');

        $scope.appConfig = appConfig;
        $scope.ssoMessage = null;
        $scope.ssoWarning = null;

        // Clear user info
        securityService.clearUser();
        console.debug("user = ", securityService.getUser());
        // Declare the user
        $scope.user = securityService.getUser();

        // If using 'deploy.dev.ims.check'
        // Verify IMS login
        if (appConfig['deploy.dev.ims.check']) {
          console.debug('x');
          if ($window.location.href.indexOf('ihtsdotools.org') != -1) {
            console.debug('y');
            $http.get(appConfig['deploy.dev.ims.check'] + '/account').then(
            // Success
            function(response) {
              $scope.ssoMessage = "User is logged into IMS";
            },
            // Error
            function(response) {
              $scope.ssoWarning = "User must log into IMS first, see the nginx ims-api/ redirect.";
            });
          }

          else {
            $scope.ssoWarning = "Using deploy.dev.ims.check mode without an ihtsdotools.org domain (consider local.ihtsdotools.org:8081)";
          }
        }

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