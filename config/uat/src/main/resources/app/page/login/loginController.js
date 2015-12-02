// Login controller
tsApp.controller('LoginCtrl', [ '$scope', '$http', '$location', 'securityService', 'gpService',
  'utilService', function($scope, $http, $location, securityService, gpService, utilService) {

    $scope.message = "Authenticating ...";

    // Need to call IMS/api/accounts
    // THis requires an nginx setup to redirect ims-api to
    // https://ims.ihtsdotools.org
    $http.get('ims-api/account').then(
    // Success
    function(response) {
      utilService.clearError();
      console.debug("user = ", response.data);

      $http.post(securityUrl + 'authenticate/' + response.data.login, 
        JSON.stringify(response.data),
        { headers : { "Content-type" : "text/plain" } }).then(

      // Success 
      function(response) {
        console.debug("user = ", response.data);
        securityService.setUser(response.data);

        // set request header authorization and rerouted
        $http.defaults.headers.common.Authorization = response.authToken;
        $location.path("/directory");

      },
      // Error
      function(response) {
        utilService.handleError(response);
        $scope.message = "Authentication error, log in at https://dev-ims.ihtsdotools.org";
        $location.path("${security.handler.IMS.url}/#/login?serviceReferer=${base.url}")
      });

    },
    // Error
    function(response) {
      utilService.handleError(response);
      $scope.message = "Authentication error, log in at https://dev-ims.ihtsdotools.org";
      $location.path("${security.handler.IMS.url}/#/login?serviceReferer=${base.url}")
    });

  } ])