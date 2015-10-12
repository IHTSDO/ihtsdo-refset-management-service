// Directory controller
tsApp.controller('DirectoryCtrl', [
  '$scope',
  '$http',
  '$modal',
  '$location',
  '$anchorScroll',
  'gpService',
  'utilService',
  'tabService',
  'securityService',
  'refsetService',
  'translationService',
  function($scope, $http, $modal, $location, $anchorScroll, gpService,
    utilService, tabService, securityService, refsetService, translationService) {
    console.debug('configure DirectoryCtrl');

    // Handle resetting tabs on "back" button
    if (tabService.selectedTab.label != 'Directory') {
      tabService.setSelectedTabByLabel('Directory');
    }

    // eventually need to make some kind of call so that a failed call can redirect 
    // to login page

    // Model variables
    $scope.publishedRefsets = null;

    // Paging variables
    $scope.pageSize = 10;

    $scope.paging = {};
    $scope.paging["publishedRefset"] = {
      page : 1,
      filter : "",
      sortField : 'name',
      ascending : null
    }
    $scope.paging["member"] = {
      page : 1,
      filter : "",
      sortField : 'lastModified',
      ascending : null
    }
    $scope.paging["inclusion"] = {
      page : 1,
      filter : "",
      sortField : 'lastModified',
      ascending : null
    }
    $scope.paging["exclusion"] = {
      page : 1,
      filter : "",
      sortField : 'lastModified',
      ascending : null
    }
    $scope.paging["concept"] = {
      page : 1,
      filter : "",
      sortField : 'lastModified',
      ascending : null
    }
    
    // get publishedRefsets
    $scope.retrievePublishedRefsets = function() {

      var pfs = {
        startIndex : ($scope.paging["publishedRefset"].page - 1) * $scope.pageSize,
        maxResults : $scope.pageSize,
        sortField : $scope.paging["publishedRefset"].sortField,
        ascending : $scope.paging["publishedRefset"].ascending == null ? true
          : $scope.paging["publishedRefset"].ascending,
        queryRestriction : 'workflowStatus:PUBLISHED'
      };

      refsetService.findRefsetsForQuery($scope.paging["publishedRefset"].filter,
        pfs).then(function(data) {
        $scope.publishedRefsets = data.refsets;
        $scope.publishedRefsets.totalCount = data.totalCount;
        for (var i = 0; i < $scope.publishedRefsets.length; i++) {
          $scope.publishedRefsets[i].isExpanded = false;
          $scope.retrieveTranslations($scope.publishedRefsets[i]);
          $scope.retrieveInclusions($scope.publishedRefsets[i]);
          $scope.retrieveExclusions($scope.publishedRefsets[i]);
        }
      })

    };
    
    // get members
    $scope.retrieveMembers = function(refset) {

      var pfs = {
        startIndex : ($scope.paging["member"].page - 1) * $scope.pageSize,
        maxResults : $scope.pageSize,
        sortField : $scope.paging["member"].sortField,
        ascending : $scope.paging["member"].ascending == null ? true
          : $scope.paging["member"].ascending,
        queryRestriction : null
      };

      refsetService.findRefsetMembersForQuery(refset.id, $scope.paging["member"].filter,
        pfs).then(function(data) {
        refset.members = data.members;
        refset.members.totalCount = data.totalCount;
      })

    };    

    // get inclusion members
    $scope.retrieveInclusions = function(refset) {

      var pfs = {
        startIndex : ($scope.paging["inclusion"].page - 1) * $scope.pageSize,
        maxResults : $scope.pageSize,
        sortField : $scope.paging["inclusion"].sortField,
        ascending : $scope.paging["inclusion"].ascending == null ? true
          : $scope.paging["inclusion"].ascending,
        queryRestriction : null
      };

      refsetService.findRefsetInclusionsForQuery(refset.id, $scope.paging["inclusion"].filter,
        pfs).then(function(data) {
        refset.inclusions = data.members;
      })

    };    
    
    // get exclusion members
    $scope.retrieveExclusions = function(refset) {

      var pfs = {
        startIndex : ($scope.paging["exclusion"].page - 1) * $scope.pageSize,
        maxResults : $scope.pageSize,
        sortField : $scope.paging["exclusion"].sortField,
        ascending : $scope.paging["exclusion"].ascending == null ? true
          : $scope.paging["exclusion"].ascending,
        queryRestriction : null
      };

      refsetService.findRefsetExclusionsForQuery(refset.id, $scope.paging["exclusion"].filter,
        pfs).then(function(data) {
        refset.exclusions = data.members;
      })

    };    
    
    // get translations
    $scope.retrieveTranslations = function(refset) {

      translationService.getTranslationsForRefset(refset.id).then(function(data) {
        refset.translations = data.translations;
        refset.translations.totalCount = data.totalCount;

      })

    };    
    
    // get translation concepts
    $scope.retrieveTranslationConcepts = function(translation) {

      var pfs = {
        startIndex : ($scope.paging["concept"].page - 1) * $scope.pageSize,
        maxResults : $scope.pageSize,
        sortField : $scope.paging["concept"].sortField,
        ascending : $scope.paging["concept"].ascending == null ? true
          : $scope.paging["concept"].ascending,
        queryRestriction : null
      };


      translationService.findTranslationConceptsForQuery(translation.id, 
        $scope.paging["concept"].filter, pfs).then(function(data) {
        translation.concepts = data.concepts;
        translation.totalCount = data.totalCount;
      })

    };    
    
    // Convert date to a string
    $scope.toDate = function(lastModified) {
      return utilService.toDate(lastModified);
    }
    
    // Convert date to a string
    $scope.toShortDate = function(lastModified) {
      return utilService.toShortDate(lastModified);
    }
    
    // sort mechanism 
    $scope.setSortField = function(table, field) {
      console.debug("set " + table + " sortField " + field);
      $scope.paging[table].sortField = field;
      // reset page number too
      $scope.paging[table].page = 1;
      // handles null case also
      if (!$scope.paging[table].ascending) {
        $scope.paging[table].ascending = true;
      } else {
        $scope.paging[table].ascending = false;
      }
      // reset the paging for the correct table
      for (var key in $scope.paging) {
        if ($scope.paging.hasOwnProperty(key)) {
          //console.debug(key + " -> " + $scope.paging[key].page);
          if (key == table)
            $scope.paging[key].page = 1;
        }
      }
      // retrieve the correct table
      if (table === 'publishedRefset') {
        $scope.retrievePublishedRefsets();
      } 
      
    }

    // Return up or down sort chars if sorted
    $scope.getSortIndicator = function(table, field) {
      if ($scope.paging[table].ascending == null) {
        return "";
      }
      if ($scope.paging[table].sortField == field
        && $scope.paging[table].ascending) {
        return "▴";
      }
      if ($scope.paging[table].sortField == field
        && !$scope.paging[table].ascending) {
        return "▾";
      }
    }

    $scope.toggleExpanded = function(refset) {
      if (refset.isExpanded == true) {
        refset.isExpanded = false;
      } else {
        refset.isExpanded = true;
      }
    }
    
    $scope.status = {
      isItemOpen: new Array(10),
      isFirstDisabled: false
    };
    
    //
    // call these during initialization
    //

    $scope.retrievePublishedRefsets();
  }

]);
