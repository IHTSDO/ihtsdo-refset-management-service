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
      'errorService',
      function($scope, $http, $location, securityService, gpService,
        errorService) {

        $scope.message = "Authenticating ...";

        // Need to call IMS/api/accounts
        // THis requires an nginx setup to redirect ims-api to
                // https://ims.ihtsdotools.org
        $http
          .get('ims-api/account')
          .success(
            function(data) {
              errorService.clearError();
              console.debug("user = ", data);

              $http({
                url : securityUrl + 'authenticate/' + data.login,
                dataType : "json",
                data : data,
                method : "POST",
                headers : {
                  "Content-Type" : "text/plain"
                }
              })
                .success(function(data) {
                  console.debug("user = ", data);
                  securityService.setUser(data);

                  // set request header authorization and
                                    // reroute
                  $http.defaults.headers.common.Authorization = data.authToken;
                  $location.path("/refset");

                })
                .error(
                  function(data, status, headers, config) {
                    errorService.handleError(data, status, headers, config);
                    $scope.message = "You may need to login first at https://ims.ihtsdotools.org";
                  });

            })
          .error(
            function(data, status, headers, config) {
              $scope.message = "You may need to login first at https://ims.ihtsdotools.org";
              // errorService.handleError(data, status, headers,
                            // config);
            });

      } ]);