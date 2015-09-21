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

        // Translation Service
        //var translation = translationService.getModel();
        
        // Directory Service
        //var directory = directoryService.getModel();

        // The component and the history list
        var component = {
          object : null,
          type : null,
          prefix : null,
          error : null,
          history : [],
          historyIndex : -1
        }

        // Page size
        var pageSizes = {
          general : 10,
          rels : 10,
          roots : 25,
          trees : 5,
          search : 10,
          sibling : 10
        }

        // Search results
        var searchParams = {
          page : 1,
          query : null
        }

        // Search results
        var searchResults = {
          list : [],
          tree : []
        }

        // Accessor function for component
        this.getModel = function() {
          return component;
        }

        // Accessor for the page sizes object
        this.getPageSizes = function() {
          return pageSizes;
        }

        // Accessor for search params
        this.getSearchParams = function() {
          return searchParams;
        }

        // Accessor for search results
        this.getSearchResults = function() {
          return searchResults;
        }

        // Autocomplete function
        this.autocomplete = function(searchTerms, autocompleteUrl) {

          // if invalid search terms, return empty array
          if (searchTerms == null || searchTerms == undefined
            || searchTerms.length < 3) {
            return new Array();
          }

          // Setup deferred
          var deferred = $q.defer();

          // NO GLASS PANE
          // Make GET call
          $http.get(autocompleteUrl + encodeURIComponent(searchTerms)).then(
          // success
          function(response) {
            deferred.resolve(response.data.string);
          },
          // error
          function(response) {
            utilHandler.handleError(response);
            deferred.resolve(response.data);
          });

          return deferred.promise;
        }


      } ]);
