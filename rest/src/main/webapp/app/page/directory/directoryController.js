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
  function($scope, $http, $modal, $location, $anchorScroll, gpService,
    utilService, tabService, securityService, refsetService) {
    console.debug('configure DirectoryCtrl');

    // Handle resetting tabs on "back" button
    if (tabService.selectedTab.label != 'Directory') {
      tabService.setSelectedTabByLabel('Directory');
    }

    // eventually need to make some kind of call so that a failed call can redirect 
    // to login page

    // Model variables
    $scope.refsets = null;

    // Paging variables
    $scope.pageSize = 10;

    $scope.paging = {};
    $scope.paging["refset"] = {
      page : 1,
      filter : "",
      sortField : 'lastModified',
      ascending : null
    }
    $scope.paging["member"] = {
      page : 1,
      filter : "",
      sortField : 'lastModified',
      ascending : null
    }
    
    // get refsets
    $scope.retrieveRefsets = function() {

      var pfs = {
        startIndex : ($scope.paging["refset"].page - 1) * $scope.pageSize,
        maxResults : $scope.pageSize,
        sortField : $scope.paging["refset"].sortField,
        ascending : $scope.paging["refset"].ascending == null ? true
          : $scope.paging["refset"].ascending,
        queryRestriction : null
      };

      refsetService.findRefsetsForQuery($scope.paging["refset"].filter,
        pfs).then(function(data) {
        $scope.refsets = data.refsets;
        $scope.refsets.totalCount = data.totalCount;
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

      // TODO: no query field for getting members?
      memberService.findMembersForRefsetRevision(/*$scope.paging["member"].filter,*/
        refset.id, refset.effectiveTime, pfs).then(function(data) {
        $scope.members = data.members;
        $scope.members.totalCount = data.totalCount;
      })

    };    

    // get translations
    $scope.retrieveTranslations = function(refset) {

      var pfs = {
        startIndex : ($scope.paging["translation"].page - 1) * $scope.pageSize,
        maxResults : $scope.pageSize,
        sortField : $scope.paging["translation"].sortField,
        ascending : $scope.paging["translation"].ascending == null ? true
          : $scope.paging["translation"].ascending,
        queryRestriction : null
      };

      // TODO: no query field for getting translations? or getTranslationsForRefset()
      translationService.findTranslationsForQuery(/*$scope.paging["translation"].filter*/
        translation.id, translation.effectiveTime, pfs).then(function(data) {
        $scope.translations = data.translations;
        $scope.translations.totalCount = data.totalCount;
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

      // TODO: no query field for getting translations?
      translationService.findConceptsForTranslationRevision(/*$scope.paging["concept"].filter*/
        translation.id, translation.effectiveTime, pfs).then(function(data) {
        $scope.concepts = data.translations;
        $scope.concepts.totalCount = data.totalCount;
      })

    };    
    
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
      if (table === 'refset') {
        $scope.retrieveRefsets();
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

    
    //
    // call these during initialization
    //

    $scope.retrieveRefsets();
  }

]);
