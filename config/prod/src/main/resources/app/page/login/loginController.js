// Login controller
tsApp
  .controller(
    'LoginCtrl',
    [
      '$scope',
      '$http',
      '$location',
      'securityService',
      'gpService',
      'utilService',
      function($scope, $http, $location, securityService, gpService, utilService) {

        $scope.message = "Authenticating ${security.handler.IMS.url} user ...";

        // Need to call IMS/api/accounts
        // THis requires an nginx setup to redirect ims-api to
        // https://ims.ihtsdotools.org
        gpService.increment();
        $http
          .get('ims-api/account')
          .then(
            // Success
            function(response) {
              utilService.clearError();
              console.debug("user = ", response.data);
              $http
                .post(securityUrl + 'authenticate/' + response.data.login,
                  JSON.stringify(response.data), {
                    headers : {
                      "Content-type" : "text/plain"
                    }
                  })
                .then(

                  // Success 
                  function(response) {
                    console.debug("user = ", response.data);
                    securityService.setUser(response.data);

                    // set request header authorization and rerouted
                    $http.defaults.headers.common.Authorization = response.data.authToken;
                    projectService.getUserHasAnyRole();
                    if (response.data.userPreferences && response.data.userPreferences.lastTab) {
                      $location.path(response.data.userPreferences.lastTab);
                    } else {
                      $location.path("/directory");
                    }
                    gpService.decrement();
                  },
                  // Error
                  function(response) {
                    utilService.handleError(response);
                    $scope.message = "Authentication error, log in at ${security.handler.IMS.url}";
                    window.location.href = "${security.handler.IMS.url}/#/login?serviceReferer=${base.url}%2F%23%2Flogin";
                  });

            },
            // Error
            function(response) {
              utilService.handleError(response);
              $scope.message = "Authentication error, log in at ${security.handler.IMS.url}";
              window.location.href = "${security.handler.IMS.url}/#/login?serviceReferer=${base.url}%2F%23%2Flogin";
            });

      } ])
