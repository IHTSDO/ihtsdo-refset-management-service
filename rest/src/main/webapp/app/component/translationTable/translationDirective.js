// Translation Table directive
// e.g. <div refset-table value="PUBLISHED"></div>
tsApp.directive('translationTable', [
  'utilService',
  'translationService',
  'releaseService',
  function(utilService, translationService, releaseService) {
    console.debug('configure translationTable directive');
    return {
      restrict : 'A',
      scope : {
        value : '@'
      },
      templateUrl : 'app/component/translationTable/translationTable.html',
      controller : [
        '$scope',
        function($scope) {

          // Model variables
          $scope.translations = null;
          $scope.pageSize = 10;
          $scope.paging = {};
          $scope.paging["translation"] = {
            page : 1,
            filter : "",
            sortField : 'name',
            ascending : null
          };
          $scope.paging["concept"] = {
            page : 1,
            filter : "",
            sortField : 'lastModified',
            ascending : null
          };

          // get translations
          $scope.getTranslations = function() {
            var pfs = {
              startIndex : ($scope.paging["translation"].page - 1)
                * $scope.pageSize,
              maxResults : $scope.pageSize,
              sortField : $scope.paging["translation"].sortField,
              ascending : $scope.paging["translation"].ascending == null ? true
                : $scope.paging["translation"].ascending,
              queryRestriction : 'workflowStatus:' + $scope.status
            };

            translationService.findTranslationsForQuery(
              $scope.paging["translation"].filter, pfs).then(function(data) {
              $scope.translations = data.translations;
              $scope.translations.totalCount = data.totalCount;
              for (var i = 0; i < $scope.translations.length; i++) {
                $scope.translations[i].isExpanded = false;
              }
            })
          };

          // get translation concepts
          $scope.getConcepts = function(translation) {

            var pfs = {
              startIndex : ($scope.paging["concept"].page - 1)
                * $scope.pageSize,
              maxResults : $scope.pageSize,
              sortField : $scope.paging["concept"].sortField,
              ascending : $scope.paging["concept"].ascending == null ? true
                : $scope.paging["concept"].ascending,
              queryRestriction : null
            };

            translationService.findTranslationConceptsForQuery(translation.id,
              $scope.paging["concept"].filter, pfs).then(function(data) {
              translation.concepts = data.concepts;
              translation.concepts.totalCount = data.totalCount;
            })

          };

          // get current translation release info
          $scope.getCurrentTranslationReleaseInfo = function(translation) {

            releaseService.getCurrentTranslationRelease(translation.id).then(
              function(data) {
                translation.releaseInfo = data;
                translation.releaseArtifacts = data.artifacts;
              })
          };

          // export release artifact
          $scope.exportReleaseArtifact = function(artifact) {
            releaseService.exportReleaseArtifact(artifact);
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
          $scope.setSortField = function(table, field, object) {
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
            for ( var key in $scope.paging) {
              if ($scope.paging.hasOwnProperty(key)) {
                // console.debug(key + " -> " +
                // $scope.paging[key].page);
                if (key == table)
                  $scope.paging[key].page = 1;
              }
            }
            // retrieve the correct table
            if (table === 'translation') {
              $scope.getTranslations();
            }
            if (table === 'concept') {
              $scope.retrieveTranslationConcepts(object);
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

          $scope.setTranslation = function(translation) {
            $scope.translation = translation;
            $scope.getCurrentTranslationReleaseInfo(translation)
          }

          $scope.status = {
            isItemOpen : new Array(10),
            isFirstDisabled : false
          };

          // Initialize
          $scope.getTranslations();
        } ]
    }
  } ]);