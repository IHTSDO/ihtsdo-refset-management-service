// Refset Service
tsApp
  .service(
    'refsetService',
    [
      '$http',
      '$q',
      'gpService',
      'utilService',
      function($http, $q, gpService, utilService) {
        console.debug("configure refsetService");

        // TODO: wrap the refset service calls

      } ]);
