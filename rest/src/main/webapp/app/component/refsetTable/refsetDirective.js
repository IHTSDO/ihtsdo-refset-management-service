// Refset Table directive
// e.g. <div refset-table value="PUBLISHED" />
tsApp.directive('refsetTable',
  [
    'utilService',
    'refsetService',
    'releaseService',
    function(utilService, refsetService, releaseService) {
      console.debug('configure refsetTable directive');
      return {
        restrict : 'A',
        scope : {
          value : '@'
        },
        templateUrl : 'app/component/refsetTable/refsetTable.html',
        controller : [
          '$scope',
          function($scope) {
            // Variable
            $scope.refset = null;
            $scope.refsets = null;
            $scope.pageSize = 10;

            $scope.paging = {};
            $scope.paging["refset"] = {
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

            // get refsets
            $scope.getRefsets = function() {

              var pfs = {
                startIndex : ($scope.paging["refset"].page - 1)
                  * $scope.pageSize,
                maxResults : $scope.pageSize,
                sortField : $scope.paging["refset"].sortField,
                ascending : $scope.paging["refset"].ascending == null ? true
                  : $scope.paging["refset"].ascending,
                queryRestriction : 'workflowStatus:' + $scope.value
              };

              refsetService.findRefsetsForQuery($scope.paging["refset"].filter,
                pfs).then(function(data) {
                $scope.refsets = data.refsets;
                $scope.refsets.totalCount = data.totalCount;
                for (var i = 0; i < $scope.refsets.length; i++) {
                  $scope.refsets[i].isExpanded = false;
                }
              })
            };

            // get members
            $scope.getMembers = function(refset) {

              var pfs = {
                startIndex : ($scope.paging["member"].page - 1)
                  * $scope.pageSize,
                maxResults : $scope.pageSize,
                sortField : $scope.paging["member"].sortField,
                ascending : $scope.paging["member"].ascending == null ? true
                  : $scope.paging["member"].ascending,
                queryRestriction : null
              };

              refsetService.findRefsetMembersForQuery(refset.id,
                $scope.paging["member"].filter, pfs).then(function(data) {
                refset.members = data.members;
                refset.members.totalCount = data.totalCount;
              })
            };

            // get current refset release info
            $scope.getCurrentRefsetReleaseInfo = function(refset) {
              releaseService.getCurrentRefsetRelease(refset.id).then(
                function(data) {
                  refset.releaseInfo = data;
                })
            };

            // Convert date to a string
            $scope.toDate = function(lastModified) {
              return utilService.toDate(lastModified);
            };

            // Convert date to a string
            $scope.toShortDate = function(lastModified) {
              return utilService.toShortDate(lastModified);
            };

            // sort mechanism
            $scope.setSortField = function(table, field, object) {
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
                  if (key == table)
                    $scope.paging[key].page = 1;
                }
              }
              // retrieve the correct table
              if (table === 'refset') {
                $scope.getRefsets();
              }
              if (table === 'member') {
                $scope.getMembers(object);
              }
            };

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
            };

            $scope.selectRefset = function(refset) {
              $scope.refset = refset;
              $scope.getCurrentRefsetReleaseInfo(refset);
              $scope.getMembers(refset);
            };

            // Initialize
            $scope.getRefsets();
          } ]
      }
    } ]);
