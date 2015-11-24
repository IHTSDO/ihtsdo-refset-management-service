// Translation Table directive
// e.g. <div translation-table value="PUBLISHED"></div>
tsApp.directive('translationTable', [
  '$modal',
  '$rootScope',
  'utilService',
  'securityService',
  'projectService',
  'translationService',
  'releaseService',
  'workflowService',
  function($modal, $rootScope, utilService, securityService, projectService, translationService,
    releaseService, workflowService) {
    console.debug('configure translationTable directive');
    return {
      restrict : 'A',
      scope : {
        // Legal "value" settings include
        // For directory tab: PUBLISHED, PREVIEW
        // For refset tab: EDITING, EDITING_ALL, RELEASE
        value : '@',
        projects : '=',
        metadata : '='
      },
      templateUrl : 'app/component/translationTable/translationTable.html',
      controller : [
        '$scope',
        function($scope) {

          // Variables
          $scope.user = securityService.getUser();
          $scope.translation = null;
          $scope.translations = null;
          $scope.translationReleaseInfo = null;
          $scope.project = null;

          // Used for project admin to know what users are assigned to something.
          $scope.conceptIdToAuthorsMap = {};
          $scope.conceptIdToReviewersMap = {};

          // Paging variables
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

          // Tests that the key has an icon
          $scope.hasIcon = function(key) {
            return projectService.hasIcon(key);
          }

          // Returns the icon path for the key (moduleId or namespaceId)
          $scope.getIcon = function(key) {
            return projectService.getIcon(key);
          }

          $scope.ioImportHandlers = [];
          $scope.ioExportHandlers = [];

          // Translation Changed handler
          $scope.$on('refset:translationChanged', function(event, data) {
            console.debug('on refset:translationChanged', data);
            // If the translation is set, refresh refsets list
            if (data) {
              $scope.getTranslations();
            }
          });

          // Concept Changed handler
          $scope.$on('refset:conceptChanged', function(event, data) {
            console.debug('on refset:conceptChanged', data);
            // If the translation is set, refresh refsets list
            if (data) {
              // TODO
            }
          });

          // Project Changed Handler
          $scope.$on('refset:projectChanged', function(event, data) {
            console.debug('on refset:projectChanged', data);
            // Set project, refresh translation list
            $scope.setProject(data);
          });

          // Set $scope.project and reload
          // $scope.refsets
          $scope.setProject = function(project) {
            $scope.project = project;
            $scope.getTranslations();
          };

          // Get $scope.translations
          $scope.getTranslations = function() {
            var pfs = {
              startIndex : ($scope.paging["translation"].page - 1) * $scope.pageSize,
              maxResults : $scope.pageSize,
              sortField : $scope.paging["translation"].sortField,
              ascending : $scope.paging["translation"].ascending == null ? true
                : $scope.paging["translation"].ascending,
              queryRestriction : null
            };

            if ($scope.value == 'PUBLISHED' || $scope.value == 'PREVIEW') {
              pfs.queryRestriction = 'workflowStatus:' + $scope.value;
              translationService.findTranslationsForQuery($scope.paging["translation"].filter, pfs)
                .then(function(data) {
                  $scope.translations = data.translations;
                  $scope.translations.totalCount = data.totalCount;
                })
            }

            if ($scope.value == 'EDITING'
              && ($scope.projects.role == 'AUTHOR' || $scope.projects.role == 'REVIEWER')) {
              workflowService.findNonReleaseProcessTranslations($scope.project.id, pfs).then(
                function(data) {
                  $scope.translations = data.translations;
                  $scope.translations.totalCount = data.totalCount;
                  // Refresh the translation if it is selected
                  if ($scope.translation) {
                    $scope.selectTranslation(translation);
                  }
                })
            }

            if ($scope.value == 'EDITING_ALL') {
              workflowService.findNonReleaseProcessTranslations($scope.project.id, pfs).then(
                function(data) {
                  $scope.translations = data.translations;
                  $scope.translations.totalCount = data.totalCount;
                  // Refresh the translation if it is selected
                  if ($scope.translation) {
                    $scope.selectTranslation(translation);
                  }
                })
            }

            if ($scope.value == 'RELEASE') {
              workflowService.findReleaseProcessTranslations($scope.project.id, pfs).then(
                function(data) {
                  $scope.translations = data.translations;
                  $scope.translations.totalCount = data.totalCount;
                })
            }

            // If $scope.translation is in the list, select it, if not clear $scope.translation
            var found = false;
            if ($scope.translation) {
              for (var i = 0; i < $scope.translations.length; i++) {
                if ($scope.translation.id == $scope.translations[i].id) {
                  found = true;
                  break;
                }
              }
            }
            if (found) {
              $scope.getConcepts($scope.translation);
            } else {
              $scope.translation = null;
            }
          };

          // Get $scope.translation.concepts
          $scope.getConcepts = function(translation) {

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
              translation.concepts.totalCount = data.totalCount;
            })

          };

          // get current translation release info
          $scope.getCurrentTranslationReleaseInfo = function(translation) {

            releaseService.getCurrentTranslationRelease(translation.id).then(function(data) {
              $scope.translationReleaseInfo = data;
            })
          };

          // export release artifact
          $scope.exportReleaseArtifact = function(artifact) {
            releaseService.exportReleaseArtifact(artifact);
          };

          // Convert date to a string
          $scope.toDate = function(lastModified) {
            return utilService.toDate(lastModified);

          };

          // Convert date to a string
          $scope.toShortDate = function(lastModified) {
            return utilService.toShortDate(lastModified);

          };

          // Table sorting mechanism
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
            if ($scope.paging[table].sortField == field && $scope.paging[table].ascending) {
              return "▴";
            }
            if ($scope.paging[table].sortField == field && !$scope.paging[table].ascending) {
              return "▾";
            }

          };

          // Selects a translation (setting $scope.translation).
          // Looks up current release info and members.
          $scope.selectTranslation = function(translation) {
            $scope.translation = translation;
            $scope.getCurrentTranslationReleaseInfo(translation);
            $scope.getConcepts(translation);

          };

          // Initialize if project setting isn't used
          if ($scope.value == 'PREVIEW' || $scope.value == 'PUBLISHED') {
            $scope.getTranslations();
          }

        } ]
    }
  } ]);
