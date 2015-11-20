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
      function($scope, $http, $location, securityService, gpService,
        utilService) {

        $scope.message = "Authenticating ...";

        // Need to call IMS/api/accounts
        // THis requires an nginx setup to redirect ims-api to
        // https://ims.ihtsdotools.org
        $http
          .get('ims-api/account').then(
            function(data) {
              utilService.clearError();
              console.debug("user = ", data);

              $http.post(securityUrl + 'authenticate/' + data.login, data).then(
                // Success 
                function(data) {
                  console.debug("user = ", data);
                  securityService.setUser(data);

                  // set request header authorization and
                                    // reroute
                  $http.defaults.headers.common.Authorization = data.authToken;
                  $location.path("/refset");

                },
                // Error
                function(data, status, headers, config) {
                    utilService.handleError(data, status, headers, config);
                    $scope.message = "You may need to login first at https://ims.ihtsdotools.org";
                  });

            },
            function(data, status, headers, config) {
              $scope.message = "You may need to login first at https://ims.ihtsdotools.org";
            });

      } ]);