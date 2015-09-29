// Translation Service
tsApp
  .service(
    'translationService',
    [
      '$http',
      '$q',
      'gpService',
      'utilService',
      function($http, $q, gpService, utilService) {
        console.debug("configure translationService");

        // TODO: wrap the translation service calls

      } ]);
