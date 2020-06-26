  // Refset Table directive
// e.g. <div refset-table value='PUBLISHED' />
tsApp
  .directive(
    'refsetTable',
    [
      '$uibModal',
      '$location',
      '$window',
      '$route',
      '$routeParams',
      '$sce',
      '$interval',
      '$timeout',
      'utilService',
      'securityService',
      'projectService',
      'refsetService',
      'releaseService',
      'workflowService',
      'validationService',
      'appConfig',
      function($uibModal, $location, $window, $route, $routeParams, $sce, $interval, $timeout, utilService,
        securityService, projectService, refsetService, releaseService, workflowService,
        validationService, appConfig) {
        console.debug('configure refsetTable directive');
        return {
          restrict : 'A',
          scope : {
            // Legal 'value' settings include
            // For directory tab: PUBLISHED, BETA
            // For refset tab: AVAILABLE, ASSIGNED
            // RELEASE
            value : '@',
            projects : '=',
            metadata : '=',
            stats : '='
          },
          templateUrl : 'app/component/refsetTable/refsetTable.html',
          controller : [
            '$scope',
            function($scope) {

              // Variables
              $scope.user = securityService.getUser();
              $scope.userProjectsInfo = projectService.getUserProjectsInfo();
              $scope.selected = {
                refset : null,
                member : null,
                concept : null,
                terminology : null,
                version : null
              };
              $scope.refsetReleaseInfo = null;
              $scope.refsets = null;
              $scope.refsetLookupProgress = {};
              $scope.lookupInterval = null;
              $scope.requiresNameLookup = false;
              $scope.project = null;
              $scope.cancelling = null;
              $scope.showLatest = true;
              $scope.withNotesOnly = false;
              $scope.filters = [];
              $scope.showDuplicatesExport = false;
              $scope.conceptIds = [];
              $scope.showImportFromExistingProject = false;
              $scope.preferredLanguage = 'en';
              
              // Page metadata
              var memberTypes = [ 'Member', 'Exclusion', 'Inclusion', 'Active', 'Inactive',
                'Translated', 'Not Translated' ];
              var memberTypes2 = [ 'Member', 'Exclusion', 'Inclusion', 'Active', 'Inactive' ];

              $scope.refsetTypes = [ 'Refset', 'Local', 'Extensional', 'Intensional', 'External' ];

              // Used for project admin to know what users are assigned to
              // something.
              $scope.refsetAuthorsMap = {};
              $scope.refsetReviewersMap = {};

              // Paging variables
              $scope.pageSizes = utilService.getPageSizes();
              $scope.paging = {};
              $scope.paging['refset'] = {
                page : 1,
                filter : $routeParams.refsetId ? 'id:' + $routeParams.refsetId : '',
                typeFilter : '',
                sortField : $scope.value == 'ASSIGNED' ? 'refsetName' : 'name',
                ascending : null,
                pageSize : 10
              };
              $scope.paging['member'] = {
                page : 1,
                filter : '',
                typeFilter : '',
                sortField : $scope.value == 'PUBLISHED' || $scope.value == 'BETA' ? 'conceptName'
                  : 'lastModified',
                ascending : true,
                pageSize : 10
              };
              $scope.paging['membersInCommon'] = {
                page : 1,
                filter : '',
                typeFilter : '',
                sortField : 'name',
                ascending : null,
                pageSize : 10
              };
              $scope.paging['oldRegularMembers'] = {
                page : 1,
                filter : '',
                typeFilter : '',
                sortField : 'name',
                ascending : null
              };
              $scope.paging['newRegularMembers'] = {
                page : 1,
                filter : '',
                typeFilter : '',
                sortField : 'name',
                ascending : null
              };

              $scope.ioImportHandlers = [];
              $scope.ioExportHandlers = [];
              $scope.isSnowowl = false;
              

              $scope.migrationFiles = new Array();

              // Refset Changed handler
              $scope.$on('refset:refsetChanged', function(event, data) {
                console.debug('on refset:refsetChanged', data);
                $scope.getRefsets();
              });

              // Project Changed Handler
              $scope.$on('refset:projectChanged', function(event, data) {
                console.debug('on refset:projectChanged', data);
                // Set project, refresh refset list
                $scope.setProject(data);
              });

              // link to error handling
              function handleError(errors, error) {
                utilService.handleDialogError(errors, error);
              }

              // ..
              $scope.getMemberTypes = function() {
                if ($scope.selected.refset && $scope.selected.refset.translated) {
                  return memberTypes;
                } else {
                  return memberTypes2;
                }
              }

              // Set $scope.project and reload
              // $scope.refsets
              $scope.setProject = function(project) {
                $scope.project = project;
                $scope.getRefsets();
                $scope.getFilters();
                // $scope.projects.role already updated
                $scope.isSnowowl = $scope.project.terminologyHandlerKey.indexOf('SNOWOWL') >= 0;
              };

              // Get $scope.refsets
              // Logic for this depends on the $scope.value and
              // $scope.projects.role
              $scope.getRefsets = function() {
                if ($routeParams.clone === 'true') {
                  $scope.paging['refset'].sortField = 'lastModified';
                  $scope.paging['refset'].ascending = false;
                }

                var pfs = {
                  startIndex : ($scope.paging['refset'].page - 1)
                    * $scope.paging['refset'].pageSize,
                  maxResults : $scope.paging['refset'].pageSize,
                  sortField : $scope.paging['refset'].sortField,
                  ascending : $scope.paging['refset'].ascending == null ? true
                    : $scope.paging['refset'].ascending,
                  queryRestriction : '',
                  latestOnly : false
                };

                if ($scope.paging['refset'].typeFilter) {
                  var value = $scope.paging['refset'].typeFilter;

                  // Handle query restrictions
                  if (value == 'Local') {
                    pfs.queryRestriction = 'localSet:true';
                  } else if (value == 'Refset') {
                    pfs.queryRestriction = 'localSet:false';
                  } else if (value == 'Intensional'
                    && ($scope.value == 'RELEASE' || $scope.value == 'PUBLISHED' || $scope.value == 'BETA')) {
                    pfs.queryRestriction = 'type:INTENSIONAL';
                  } else if (value == 'Extensional'
                    && ($scope.value == 'RELEASE' || $scope.value == 'PUBLISHED' || $scope.value == 'BETA')) {
                    pfs.queryRestriction = 'type:EXTENSIONAL';
                  } else if (value == 'External'
                    && ($scope.value == 'RELEASE' || $scope.value == 'PUBLISHED' || $scope.value == 'BETA')) {
                    pfs.queryRestriction = 'type:EXTERNAL';
                  } else if (value == 'Intensional') {
                    pfs.queryRestriction = 'INTENSIONAL';
                  } else if (value == 'Extensional') {
                    pfs.queryRestriction = 'EXTENSIONAL';
                  } else if (value == 'External') {
                    pfs.queryRestriction = 'EXTERNAL';
                  }
                }

                if ($scope.value == 'PUBLISHED' || $scope.value == 'BETA') {
                  if (pfs.queryRestriction) {
                    pfs.queryRestriction = pfs.queryRestriction + " AND ";
                  } else {
                    pfs.queryRestriction = '';
                  }
                  pfs.queryRestriction = pfs.queryRestriction + 'workflowStatus:' + $scope.value;
                  pfs.latestOnly = $scope.showLatest;
                  refsetService.findRefsetsForQuery($scope.paging['refset'].filter, pfs).then(
                    function(data) {
                      $scope.refsets = data.refsets;
                      $scope.refsets.totalCount = data.totalCount;
                      $scope.stats.count = $scope.refsets.totalCount;
                      $scope.reselect();
                    });
                }

                if ($scope.value == 'AVAILABLE') {
                  if (pfs.queryRestriction && $scope.paging['refset'].filter) {
                    pfs.queryRestriction = pfs.queryRestriction + " AND ";
                  }
                  pfs.queryRestriction = pfs.queryRestriction + $scope.paging['refset'].filter;
                  workflowService.findAvailableRefsets($scope.projects.role, $scope.project.id,
                    $scope.user.userName, pfs).then(function(data) {
                    $scope.refsets = data.refsets;
                    $scope.refsets.totalCount = data.totalCount;
                    $scope.stats.count = $scope.refsets.totalCount;
                    $scope.reselect();
                  });
                }

                // on TrackingRecords, not on Refsets table
                if ($scope.value == 'ASSIGNED' && $scope.projects.role == 'ADMIN') {
                  if (pfs.queryRestriction && $scope.paging['refset'].filter) {
                    pfs.queryRestriction = pfs.queryRestriction + " AND ";
                  }
                  if ($scope.paging['refset'].filter) {
                    pfs.queryRestriction = pfs.queryRestriction + $scope.paging['refset'].filter;
                  }
                  workflowService
                    .findAssignedRefsets('ADMIN', $scope.project.id, null, pfs)
                    .then(
                      // Success
                      function(data) {
                        $scope.refsets = $scope.getRefsetsFromRecords(data.records);
                        $scope.refsets.totalCount = data.totalCount;
                        $scope.stats.count = $scope.refsets.totalCount;
                        // get refset tracking records in order to get refset
                        // authors
                        for (var i = 0; i < data.records.length; i++) {
                          if (data.records[i].authors.length > 0) {
                            $scope.refsetAuthorsMap[data.records[i].refset.id] = data.records[i].authors;
                          }
                          if (data.records[i].reviewers.length > 0) {
                            $scope.refsetReviewersMap[data.records[i].refset.id] = data.records[i].reviewers;
                          }
                        }
                        $scope.reselect();
                      });
                }
                if ($scope.value == 'ASSIGNED' && $scope.projects.role != 'ADMIN') {
                  if (pfs.queryRestriction && $scope.paging['refset'].filter) {
                    pfs.queryRestriction = pfs.queryRestriction + " AND ";
                  }
                  if ($scope.paging['refset'].filter) {
                    pfs.queryRestriction = pfs.queryRestriction + $scope.paging['refset'].filter;
                  }
                  workflowService
                    .findAssignedRefsets($scope.projects.role, $scope.project.id,
                      $scope.user.userName, pfs)
                    .then(
                      // Success
                      function(data) {
                        $scope.refsets = $scope.getRefsetsFromRecords(data.records);
                        $scope.refsets.totalCount = data.totalCount;
                        $scope.stats.count = $scope.refsets.totalCount;
                        // get refset tracking records in order to get refset
                        // authors
                        for (var i = 0; i < data.records.length; i++) {
                          if (data.records[i].authors.length > 0) {
                            $scope.refsetAuthorsMap[data.records[i].refset.id] = data.records[i].authors;
                          }
                          if (data.records[i].reviewers.length > 0) {
                            $scope.refsetReviewersMap[data.records[i].refset.id] = data.records[i].reviewers;
                          }
                        }
                        $scope.reselect();
                      });
                }

                if ($scope.value == 'RELEASE') {
                  if (pfs.queryRestriction) {
                    pfs.queryRestriction = pfs.queryRestriction + " AND ";
                  } else {
                    pfs.queryRestriction = '';
                  }
                  pfs.queryRestriction = pfs.queryRestriction
                    + 'projectId:'
                    + $scope.project.id
                    + ' AND revision:false AND (workflowStatus:READY_FOR_PUBLICATION OR workflowStatus:BETA  OR workflowStatus:PUBLISHED)';
                  pfs.latestOnly = $scope.showLatest;
                  refsetService.findRefsetsForQuery($scope.paging['refset'].filter, pfs).then(
                    function(data) {
                      $scope.refsets = data.refsets;
                      $scope.refsets.totalCount = data.totalCount;
                      $scope.stats.count = $scope.refsets.totalCount;
                      $scope.reselect();
                    });
                }
              };

              // Convert an array of tracking records to an array of refsets.
              $scope.getRefsetsFromRecords = function(records) {
                var refsets = new Array();
                for (var i = 0; i < records.length; i++) {
                  refsets.push(records[i].refset);
                }
                return refsets;
              };

              // Set the scope link
              $scope.getLink = function(refset) {
                $scope.link = utilService.composeUrl('/directory?refsetId=' + refset.id)
              }

              // Clear the url params when "clear" gets clicked
              $scope.clearUrlParams = function() {
                if ($scope.value == 'PUBLISHED' && $scope.link) {
                  var index = $scope.link.indexOf("?");
                  if (index != -1) {
                    $scope.link = $scope.link.substring(0, index);
                    $window.location.href = $scope.link;
                    $route.reload();
                  }
                }
              }

              // Reselect selected refset to refresh
              $scope.reselect = function() {
                // If no selected refset, use user preferences
                if (!$scope.selected.refset && $routeParams.refsetId && $scope.value == 'PUBLISHED') {
                  $scope.selected.refset = {
                    id : $routeParams.refsetId
                  };
                }

                // If no selected refset, use user preferences
                if (!$scope.selected.refset && $scope.user.userPreferences.lastRefsetId
                  && $scope.value == $scope.user.userPreferences.lastRefsetAccordion) {

                  $scope.selected.refset = {
                    id : $scope.user.userPreferences.lastRefsetId
                  };
                }

                if ($scope.selected.refset) {
                  // If $scope.selected.refset is in the list, select it, if not
                  // clear $scope.selected.refset
                  var found = false;
                  for (var i = 0; i < $scope.refsets.length; i++) {
                    if ($scope.selected.refset.id == $scope.refsets[i].id) {
                      $scope.selectRefset($scope.refsets[i]);
                      found = true;
                      break;
                    }
                  }

                  if (!found) {
                    $scope.selected.refset = null;
                    $scope.selected.concept = null;
                    $scope.clearLastRefsetId();
                  }
                }

                // If still no selection, clear lastRefsetId and select first in
                // list
                else {
                  $scope.clearLastRefsetId();
                }

                // If 'lookup in progress' is set, get progress
                for (var i = 0; i < $scope.refsets.length; i++) {
                  if ($scope.refsets[i].lookupInProgress) {
                    startLookup($scope.refsets[i]);
                  }
                }

              };

              // clear the last refset id
              $scope.clearLastRefsetId = function() {
                if ($scope.user.userPreferences.lastRefsetId
                  && $scope.value == $scope.user.userPreferences.lastRefsetAccordion) {
                  $scope.user.userPreferences.lastRefsetId = null;
                  securityService.updateUserPreferences($scope.user.userPreferences);
                }
              }

              // Get $scope.filters
              $scope.getFilters = function() {
                var projectId = $scope.project ? $scope.project.id : null;
                var workflowStatus = null;
                if ($scope.value == 'PUBLISHED' || $scope.value == 'BETA') {
                  workflowStatus = $scope.value;
                }
                refsetService.getFilters(projectId, workflowStatus).then(
                // Success
                function(data) {
                  $scope.filters = data.keyValuePairs;
                });
              };

              // Get $scope.metadata.descriptionTypes
              $scope.getStandardDescriptionTypes = function(terminology, version) {
                projectService.getStandardDescriptionTypes(terminology, version).then(
                // Success
                function(data) {
                  // Populate 'selected' for refsetTable.html
                  // and metadata for addMember.html
                  $scope.selected.descriptionTypes = data.types;
                  $scope.metadata.descriptionTypes = data.types;
                });
              };

              // Get $scope.members
              $scope.getMembers = function(refset) {

                var pfs = prepPfs();
                var value = $scope.paging['member'].typeFilter;
                var translated;
                var language = $scope.preferredLanguage;
                
                // Drop any dialect code when doing lookups.
                // This is possibly temporary
                if(language.length > 2){
                  language = language.substring(0,2);
                }

                if (value == 'Translated') {
                  translated = true;
                } else if (value == 'Not Translated') {
                  translated = false;
                }

                refsetService.findRefsetMembersForQuery(refset.id, $scope.paging['member'].filter,
                  pfs, translated, language).then(
                  // Success
                  function(data) {
                    refset.members = data.members;
                    refset.members.totalCount = data.totalCount;
                    $scope.requiresNameLookup = false;
                    var found = false;
                    for (var i = 0; i < data.members.length; i++) {
                      if (data.members[i].conceptName == 'name lookup in progress'
                        || data.members[i].conceptName == 'unable to determine name'
                          || data.members[i].conceptName == 'requires name lookup') {
                        found = true;
                        break;
                      }
                    }
                    $scope.requiresNameLookup = false;
                    if (found) {
                      $scope.requiresNameLookup = true;
                    }

                  });

              };

              // Prepare PFS for searches and for export
              function prepPfs() {
                var pfs = {
                  startIndex : ($scope.paging['member'].page - 1)
                    * $scope.paging['member'].pageSize,
                  maxResults : $scope.paging['member'].pageSize,
                  sortField : $scope.paging['member'].sortField,
                  ascending : $scope.paging['member'].ascending == null ? false
                    : $scope.paging['member'].ascending,
                  queryRestriction : null
                };

                if ($scope.paging['member'].typeFilter) {
                  var value = $scope.paging['member'].typeFilter;

                  // Handle inactive
                  if (value == 'Inactive') {
                    pfs.queryRestriction = 'conceptActive:false';
                  } else if (value == 'Active') {
                    pfs.queryRestriction = 'conceptActive:true';
                  } else if (value == 'Translated' || value == 'Not Translated') {
                    // do nothing
                  } else {
                    // Handle member type
                    value = value.replace(' ', '_').toUpperCase();
                    pfs.queryRestriction = 'memberType:' + value;
                  }

                }

                if ($scope.withNotesOnly && pfs.queryRestriction) {
                  pfs.queryRestriction += ' AND notes.value:[* TO *]';
                }
                if ($scope.withNotesOnly && !pfs.queryRestriction) {
                  pfs.queryRestriction = 'notes.value:[* TO *]';
                }
                return pfs;
              }

              // Get $scope.refsetReleaseInfo
              $scope.getRefsetReleaseInfo = function(refset) {
                $scope.refsetReleaseInfo = null;
                var pfs = {
                  startIndex : -1,
                  maxResults : 10,
                  sortField : null,
                  ascending : null,
                  queryRestriction : null
                };
                releaseService.findRefsetReleasesForQuery(refset.id, null, pfs).then(
                  function(data) {
                    $scope.refsetReleaseInfo = data.releaseInfos[0];
                  });
              };

              // optimizes the definition
              $scope.optimizeDefinition = function(refset) {
                refsetService.optimizeDefinition(refset.id).then(function() {
                  refsetService.fireRefsetChanged(refset);
                });
              };

              // Convert date to a string
              $scope.toDate = function(lastModified) {
                return utilService.toDate(lastModified);

              };

              // Convert date to a string
              $scope.toSimpleDate = function(lastModified) {
                return utilService.toSimpleDate(lastModified);

              };

              // Convert date to a string
              $scope.toShortDate = function(lastModified) {
                return utilService.toShortDate(lastModified);

              };

              // Indicates whether we are in a directory page section
              var valueFlag = ($scope.value == 'PUBLISHED' || $scope.value == 'BETA');
              $scope.isDirectory = function() {
                return valueFlag;
              };

              // Return the name for a terminology
              $scope.getTerminologyName = function(terminology) {
                if ($scope.metadata && $scope.metadata.terminologyNames) {
                  return $scope.metadata.terminologyNames[terminology];
                } else {
                  return terminology;
                }
              };

              // Get ordered definition clauses
              $scope.getOrderedDefinitionClauses = function() {
                if ($scope.selected.refset && $scope.selected.refset.definitionClauses) {
                  return $scope.selected.refset.definitionClauses.sort(utilService
                    .sortBy('negated'));
                }
              };

              // Table sorting mechanism
              $scope.setSortField = function(table, field, object) {

                // handle 'ASSIGNED' vs 'AVAILABLE' fields
                // refsetTable.html expresses the fields in terms of available
                var lfield = field;
                if (table == 'refset' && ($scope.value == 'ASSIGNED')) {
                  if (field == 'terminologyId') {
                    lfield = 'refsetId';
                  } else if (field == 'lastModified') {
                    lfield = 'lastModified';
                  } else if (field == 'workflowStatus') {
                    lfield = 'workflowStatus';
                  } else {
                    // uppercase and prepend refset in all other cases
                    lfield = 'refset' + field.charAt(0).toUpperCase() + field.slice(1);
                  }
                }

                utilService.setSortField(table, lfield, $scope.paging);
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
                var lfield = field;
                if (table == 'refset' && ($scope.value == 'ASSIGNED')) {
                  if (field == 'terminologyId') {
                    lfield = 'refsetId';
                  } else if (field == 'lastModified') {
                    lfield = 'lastModified';
                  } else if (field == 'workflowStatus') {
                    lfield = 'workflowStatus';
                  } else {
                    // uppercase and prepend refset in all other cases
                    lfield = 'refset' + field.charAt(0).toUpperCase() + field.slice(1);
                  }
                }
                return utilService.getSortIndicator(table, lfield, $scope.paging);
              };

              // Selects a refset (setting $scope.selected.refset).
              // Looks up current release info and members.
              $scope.selectRefset = function(refset) {
                $scope.selected.refset = refset;
                // remove stale selected concept to clear Concept Details pane
                $scope.selected.concept = null;
                $scope.selected.terminology = refset.terminology;
                $scope.selected.version = refset.version;
                refsetService.getRequiredLanguageRefsets(refset.id).then(function(data) {
                  $scope.requiredLanguages = data.strings;
                  // If new refset doesn't contain the most recently selected preferred language, revert to english
                  if(!$scope.requiredLanguages.includes($scope.preferredLanguage)){
                    $scope.preferredLanguage = "en";
                  }
                  $scope.getRefsetReleaseInfo(refset);
                  $scope.getMembers(refset);
                  $scope.getStandardDescriptionTypes(refset.terminology, refset.version);
                  $scope.getLink(refset);
                  if (refset.id != $scope.user.userPreferences.lastRefsetId) {
                    $scope.user.userPreferences.lastRefsetId = refset.id;
                    securityService.updateUserPreferences($scope.user.userPreferences);
                  }
                  //show refset copy button 
                  if (appConfig['deploy.refset.member.copy.group']) {
                    var refsetList = appConfig['deploy.refset.member.copy.group'].split('|');
                    if (refsetList.includes($scope.selected.refset.name)) {
                      $scope.showImportFromExistingProject = true;
                    }
                    else{
                      $scope.showImportFromExistingProject = false;
                    }
                  }
                });
                // check if migration files are on server available for download
                $scope.migrationFiles = new Array();
                refsetService.getMigrationFileNames($scope.project.id, $scope.selected.refset.terminologyId).then(
                  function(data) {
                    if (data != '') {                     
                    var fileNames = data.split("\|");
                    for (var i = 0, l = fileNames.length; i < l; i++) {
                      $scope.migrationFiles.push(fileNames[i]);
                      // Have the files sorted in reverse order, so the most recent is on top.
                      $scope.migrationFiles.sort();
                      $scope.migrationFiles.reverse();
                    }
                    }
                  });
              };

              // Selects a member (setting $scope.selected.member)
              $scope.selectMember = function(member) {
                $scope.selected.member = member;
                // Refresh the concept for display in concept-info
                $scope.selected.concept = null;
                $scope.selected.concept = {
                  terminologyId : member.conceptId,
                  terminology : member.terminology,
                  version : member.version
                };

              };

              // Member type style
              $scope.getMemberStyle = function(member) {
                if (member.memberType == 'MEMBER') {
                  return '';
                }
                return member.memberType.replace('_STAGED', '');
              };

              // Remove a refset
              $scope.removeRefset = function(refset) {
                workflowService.findAssignedRefsets('ADMIN', $scope.project.id, null, {
                  startIndex : 0,
                  maxResults : 1,
                  queryRestriction : 'refsetId:' + refset.id
                }).then(
                  // Success
                  function(data) {
                    if (data.records.length > 0
                      && !$window
                        .confirm('The refset is assigned, are you sure you want to proceed?')) {
                      return;
                    }
                    $scope.removeRefsetHelper(refset);
                  });
              };

              // Helper for removing a refest
              $scope.removeRefsetHelper = function(refset) {

                refsetService.findRefsetMembersForQuery(refset.id, '', {
                  startIndex : 0,
                  maxResults : 1
                }).then(
                  function(data) {
                    if (data.members.length == 1) {
                      if (!$window
                        .confirm('The refset has members, are you sure you want to proceed.')) {
                        return;
                      }
                    }
                    refsetService.removeRefset(refset.id).then(function() {
                      $scope.selected.refset = null;
                      refsetService.fireRefsetChanged();
                    });
                  });
              };

              // Remove refset member
              $scope.removeRefsetMember = function(refset, member) {

                refsetService.removeRefsetMember(member.id).then(
                // Success
                function() {
                  $scope.selected.concept = null;
                  $scope.handleWorkflow(refset);
                });
              };
              // Remove refset inclusion
              $scope.removeRefsetInclusion = function(refset, member) {

                refsetService.removeRefsetMember(member.id).then(
                // Success
                function() {
                  $scope.handleWorkflow(refset);
                });
              };

              // Adds a refset exclusion and refreshes member
              // list with current PFS settings
              $scope.addRefsetExclusion = function(refset, member) {
                refsetService.addRefsetExclusion(refset, member.conceptId, false).then(function() {
                  $scope.handleWorkflow(refset);
                  // refresh Concept Details panel
                  $scope.selected.concept = null;
                  $scope.selected.concept = {
                    terminologyId : member.conceptId,
                    terminology : member.terminology,
                    version : member.version
                  };
                });

              };

              // Remove refset exclusion and refreshes members
              $scope.removeRefsetExclusion = function(refset, member) {
                refsetService.removeRefsetExclusion(member.id).then(function() {
                  $scope.handleWorkflow(refset);
                  // refresh Concept Details panel
                  $scope.selected.concept = null;
                  $scope.selected.concept = {
                    terminologyId : member.conceptId,
                    terminology : member.terminology,
                    version : member.version
                  };
                });

              };

              // Unassign refset from user
              $scope.unassign = function(refset, userName) {
                $scope.performWorkflowAction(refset, 'UNASSIGN', userName);
              };

              // handle workflow advancement
              $scope.handleWorkflow = function(refset) {
                if ($scope.value == 'ASSIGNED'
                  && refset
                  && (refset.workflowStatus == 'NEW' || refset.workflowStatus == 'READY_FOR_PUBLICATION')) {
                  $scope.performWorkflowAction(refset, 'SAVE', $scope.user.userName);
                } else {
                  refsetService.fireRefsetChanged(refset);
                }
              };

              // Performs a workflow action
              $scope.performWorkflowAction = function(refset, action, userName) {
                var role = workflowService.refsetGetRole(action, $scope.projects.role,
                  refset.workflowStatus, $scope.metadata.workflowConfig);
                workflowService.performWorkflowAction($scope.project.id, refset.id, userName, role,
                  action).then(function(data) {
                  refsetService.fireRefsetChanged(data);
                });
              };

              // Removes all refset members
              $scope.removeAllRefsetMembers = function(refset) {
                refsetService.removeAllRefsetMembers(refset.id).then(function(data) {
                  refsetService.fireRefsetChanged(refset);
                });
              };

              // Exports a release artifact (and begins the
              // download)
              $scope.exportReleaseArtifact = function(artifact) {
                releaseService.exportReleaseArtifact(artifact);
              };

              // Directive scoped method for cancelling an import/migration
              $scope.cancelAction = function(refset) {
                $scope.cancelling = true;
                if (refset.stagingType == 'IMPORT') {
                  refsetService.cancelImportMembers(refset.id).then(
                  // Success
                  function() {
                    $scope.cancelling = false;
                    refsetService.fireRefsetChanged(refset);
                  },
                  // Error
                  function() {
                    $scope.cancelling = false;
                  });
                }
                if (refset.stagingType == 'MIGRATION') {

                  refsetService.cancelMigration(refset.id).then(
                  // Success
                  function(data) {
                    // Some local management of refset state to avoid
                    // a million callbacks to the server while
                    // startLookup is running
                    refset.staged = false;
                    refset.stagingType = null;
                    // If INTENSIONAL, we need to re-look up old/not/new members
                    if (refset.type == 'INTENSIONAL') {
                      refset.lookupInProgress = true;
                      startLookup(refset);
                    }
                    $scope.cancelling = false;
                    // refsetService.fireRefsetChanged($scope.refset);
                  },
                  // Error
                  function() {
                    $scope.cancelling = false;
                  });

                }
                if (refset.stagingType == 'BETA' || refset.stagingType == null) {
                  releaseService.cancelRefsetRelease(refset.id).then(
                  // Success
                  function() {
                    $scope.cancelling = false;
                    refsetService.fireRefsetChanged(refset);
                  },
                  // Error
                  function() {
                    $scope.cancelling = false;
                  });
                }
              };

              // cancelling a release given the staged refset
              $scope.cancelActionForStaged = function(refset) {
                if (refset.workflowStatus == 'BETA') {
                  refsetService.getOriginForStagedRefsetId(refset.id).then(
                  // Success
                  function(data) {
                    refsetService.getRefset(data).then(
                    // Success
                    function(data) {
                      $scope.cancelAction(data);
                    });
                  });
                }
              };

              // Need both a $scope version and a non one for modals.
              $scope.startLookup = function(refset) {
                startLookup(refset);
              };

              // Start lookup again - not $scope because modal must access it
              function startLookup(refset) {
                
                refsetService.startLookup(refset.id).then(
                // Success
                function(data) {
                  $scope.refsetLookupProgress[refset.id] = 1;
                  refset.lookupInProgress = true;
                  
                  //update $scope.refsets copy of refset
                  for (var i = 0; i < $scope.refsets.length; i++) {
                    if ($scope.refsets[i].id == refset.id) {
                      $scope.refsets[i].lookupInProgress=true;
                      break;
                    }
                  }
                  
                  // Start if not already running
                  if (!$scope.lookupInterval) {
                    $scope.lookupInterval = $interval(function() {
                      $scope.refreshLookupProgress(refset);
                    }, 2000);
                  }
                });
              }

              // Refresh lookup progress
              $scope.refreshLookupProgress = function(refset) {
                refsetService.getLookupProgress(refset.id).then(
                // Success
                function(data) {
                  if (data === "100" || data == 100) {
                    refset.lookupInProgress = false;
                  }
                  $scope.refsetLookupProgress[refset.id] = data;
                  // If all lookups in progress are at 100%, stop interval
                  var found = true;
                  for ( var key in $scope.refsetLookupProgress) {
                    if ($scope.refsetLookupProgress[key] > -1 && $scope.refsetLookupProgress[key] < 100) {
                      found = false;
                      break;
                    }
                  }
                  if (found) {
                    if($scope.lookupInterval){
                      $interval.cancel($scope.lookupInterval);
                      $scope.lookupInterval = null;
                      refsetService.fireRefsetChanged(data);
                    }
                  }

                },
                // Error
                function(data) {
                  // Cancel automated lookup on error
                  $interval.cancel($scope.lookupInterval);
                  $scope.refsetLookupProgress[refset.id] = -1;
                });
              };

              // Return true if the refset is actively in the middle of a name lookup
              $scope.isLookupInProgress = function(refset) {
                if (refset != null && refset.lookupInProgress && $scope.refsetLookupProgress[refset.id] > -1 && $scope.refsetLookupProgress[refset.id]  < 100) {
                  return true;
                }
                return false;
              };              
              
              // Cancel lookup process
              $scope.cancelLookup = function(refset) {
                refsetService.cancelLookup(refset.id).then(
                // Success
                function(data) {
                  $scope.refsetLookupProgress[refset.id] = -1;
                  //update $scope.refsets copy of refset
                  for (var i = 0; i < $scope.refsets.length; i++) {
                    if ($scope.refsets[i].id == refset.id) {
                      $scope.refsets[i].requiresLookup=true;
                      $scope.refsets[i].lookupInProgress=false;
                      break;
                    }
                  }
                },
                // Error
                function(data) {
                  // Cancel failed - do nothing
                });
              };              
              
              // Get the most recent note for display
              $scope.getLatestNote = function(refset) {
                if (refset && refset.notes && refset.notes.length > 0) {
                  return $sce
                    .trustAsHtml(refset.notes.sort(utilService.sortBy('lastModified', -1))[0].value);
                }
                return $sce.trustAsHtml('');
              };

              // lookup and add replacement concepts
              $scope.replace = function(refset, member) {
                projectService.getReplacementConcepts($scope.project.id, member.conceptId,
                  refset.terminology, refset.version).then(
                // Success
                function(data) {

                  // if no replacements, just add the inclusion
                  if (data.concepts.length == 0) {
                    $window.alert("No replacement concepts available");
                    return;
                  } else {
                    $scope.openReplacementConceptsModal(refset, member, data.concepts);
                  }
                },
                // Error
                function(data) {
                  handleError($scope.errors, data);
                });
              };

              $scope.convertRefset = function(refset) {
                if ($window
                  .confirm('Are you sure that you want to convert this refset to an extensional refset?')) {
                  refsetService.convertRefset(refset, 'EXTENSIONAL').then(
                  // Success
                  function(data) {
                    $scope.getRefsets();
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                }
              }

              $scope.isAllowed = function(action, refset) {
                if ($scope.value == 'PUBLISHED' || $scope.value == 'BETA') {
                  return false;
                }

                return workflowService.refsetIsAllowed(action, $scope.projects.role,
                  refset.workflowStatus, $scope.metadata.workflowConfig);
              }

              $scope.getRole = function(action, refset) {
                if ($scope.value == 'PUBLISHED' || $scope.value == 'BETA') {
                  return $scope.projects.role;
                }
                return workflowService.refsetGetRole(action, $scope.projects.role,
                  refset.workflowStatus, $scope.metadata.workflowConfig);
              }
              
              $scope.showDelta = function(refset) {
                releaseService.findCurrentRefsetReleaseInfo(refset.id).then(
                  function(data) {
                    // if no previous release,
                    if (!data.refsetId || data.refsetId == 0) {
                      window.alert("No release for " + refset.name);
                      return;
                    }
                    // get the release refset
                    refsetService.compareRefsets(refset.id, data.refsetId).then(
                      function(data) {
                        var reportToken = data;
                        refsetService.exportDiffReport('diff', reportToken, refset).then(
                          function(data) {
                            // consider delaying next statement if it doesn't
                            // work
                            refsetService.releaseReportToken(reportToken);
                          });
                      });
                  });
              }

              $scope.submitRefset = function(refset) {

                // Validate refset
                validationService.validateRefset(refset).then(
                  function(data) {

                    // If there are errors, make them available and stop.
                    if (data.errors && data.errors.length > 0) {
                      $scope.errors = data.errors;
                      return;
                    } else {
                      $scope.errors = [];
                    }

                    // if $scope.warnings is empty, and data.warnings is not,
                    // show warnings and stop
                    if (data.warnings && data.warnings.length > 0
                      && $scope.warnings.join() !== data.warnings.join()) {
                      $scope.warnings = data.warnings;
                      return;
                    } else {
                      $scope.warnings = [];
                    }

                    // Success - validate refset
                    refsetService.updateRefset(refset).then(
                    // Success - update refset
                    function(data) {
                      //$uibModalInstance.close(refset);
                    },
                    // Error - update refset
                    function(data) {
                      handleError($scope.errors, data);
                    });

                  },
                  // Error - validate refset
                  function(data) {
                    handleError($scope.errors, data);
                  });

              };

              
              //
              // MODALS
              //

              // Open concept replacements modal
              $scope.openReplacementConceptsModal = function(lrefset, lmember, lconcepts) {
                console.debug('openReplacementConceptsModal ', lrefset, lmember, lconcepts);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/replacements.html',
                  controller : ReplacementConceptsModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
                    },
                    member : function() {
                      return lmember;
                    },
                    concepts : function() {
                      return lconcepts;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.handleWorkflow(data);
                });

              };

              // replacement concepts modal controller
              var ReplacementConceptsModalCtrl = function($scope, $uibModalInstance, refset,
                member, concepts) {
                console.debug('Entered replacement concepts modal control', refset, member,
                  concepts);

                $scope.errors = [];
                $scope.refset = refset;
                $scope.member = member;
                $scope.concepts = concepts;

                $scope.selection = {
                  ids : {
                    "test" : true
                  }
                };
                // $scope.invalidIds = new Array();
                $scope.invalid = {
                  ids : {
                    "test" : true
                  }
                };
                $scope.expectedCt = 0;

                // initialize
                var pfs = {
                  startIndex : 0,
                  maxResults : 1,
                  sortField : null,
                  ascending : null,
                  queryRestriction : null
                };

                // check if replacements are already members
                for (var i = 0; i < $scope.concepts.length; i++) {
                  var query = 'conceptId:' + $scope.concepts[i].terminologyId;
                  refsetService.findRefsetMembersForQuery($scope.refset.id, query, pfs).then(
                  // Success
                  function(data) {
                    if (data.members.length != 0) {
                      $scope.invalid.ids[data.members[0].conceptId] = true;
                    }
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                }

                // Add button
                $scope.submitAdd = function() {
                  // calculate total number of replacement options
                  for (var i = 0; i < concepts.length; i++) {
                    if ($scope.selection.ids[concepts[i].terminologyId]) {
                      $scope.expectedCt++;
                    }
                  }
                  // if intensional, check if inactive concept itself should
                  // be included
                  if (refset.type == 'INTENSIONAL') {
                    if ($scope.selection.ids[$scope.member.conceptId]) {
                      $scope.expectedCt++;
                      $scope.addRefsetInclusionOrMember($scope.refset, $scope.member);
                    }
                  }
                  // if a concept is selected, add it as an inclusion or
                  // member
                  for (var i = 0; i < concepts.length; i++) {
                    if ($scope.selection.ids[concepts[i].terminologyId]) {
                      var member = {
                        active : true,
                        conceptId : concepts[i].terminologyId,
                        conceptName : concepts[i].name,
                        conceptActive : concepts[i].active,
                        memberType : (refset.type == 'INTENSIONAL' ? 'INCLUSION' : 'MEMBER'),
                        moduleId : refset.moduleId,
                        refsetId : $scope.refset.id
                      };
                      $scope.addRefsetInclusionOrMember($scope.refset, member);
                    }
                  }
                };

                $scope.addRefsetInclusionOrMember = function(refset, member) {
                  member.refsetId = refset.id;
                  if (refset.type == 'INTENSIONAL') {
                    refsetService.addRefsetInclusion(member, false).then(
                    // Success
                    function(data) {
                      $scope.expectedCt--;
                      if ($scope.expectedCt == 0) {
                        $uibModalInstance.close();
                      }
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  } else if (refset.type == 'EXTENSIONAL') {
                    refsetService.addRefsetMember(member).then(
                    // Success
                    function(data) {
                      $scope.expectedCt--;
                      if ($scope.expectedCt == 0) {
                        $uibModalInstance.close();
                      }
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Definition clauses modal
              $scope.openDefinitionClausesModal = function(lrefset, lvalue) {
                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/definitionClauses.html',
                  controller : DefinitionClausesModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
                    },
                    metadata : function() {
                      return $scope.metadata;
                    },
                    value : function() {
                      return lvalue;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.handleWorkflow(data);
                });

              };

              // Definition clauses controller
              var DefinitionClausesModalCtrl = function($scope, $uibModalInstance, utilService,
                refset, metadata, value) {
                console.debug('Entered definition clauses modal control', refset, value);

                $scope.refset = refset;
                $scope.metadata = metadata;
                $scope.value = value;
                $scope.newClause = null;

                // Paging parameters
                $scope.newClauses = angular.copy($scope.refset.definitionClauses);
                $scope.pageSize = 5;
                $scope.pagedClauses = [];
                $scope.paging = {};
                $scope.paging['clauses'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.warnings = {};
                $scope.warningFlag = false;
                $scope.errors = [];

                // Indicate whether a clause is in a warning condition
                $scope.isWarning = function(value) {
                  return $scope.warnings[value];
                };

                // Get paged clauses (assume all are loaded)
                $scope.getPagedClauses = function() {
                  $scope.pagedClauses = utilService
                    .getPagedArray($scope.newClauses.sort(utilService.sortBy('negated')),
                      $scope.paging['clauses'], $scope.pageSize);
                };

                // identify whether defintion has changed
                $scope.isDefinitionDirty = function() {
                  if ($scope.newClauses.length != $scope.refset.definitionClauses.length) {
                    return true;
                  }

                  // Compare scope.refset.definitionClauses to newClauses
                  for (var i = 0; i < $scope.newClauses.length; i++) {
                    if ($scope.newClauses[i].value != $scope.refset.definitionClauses[i].value) {
                      return true;
                    }
                    if ($scope.newClauses[i].negated != $scope.refset.definitionClauses[i].negated) {
                      return true;
                    }
                  }
                  return false;
                };

                // remove clause
                $scope.removeClause = function(refset, clause) {
                  for (var i = 0; i < $scope.newClauses.length; i++) {
                    var index = $scope.newClauses.indexOf(clause);
                    if (index != -1) {
                      $scope.newClauses.splice(index, 1);
                    }
                  }
                  $scope.getPagedClauses();

                  // reset the warnings based on remaining clauses
                  if ($scope.warnings.length > 0) {
                    $scope.warnings = {};
                    $scope.warningFlag = false;
                    for (var i = 0; i < $scope.newClauses.length; i++) {
                      refsetService
                        .countExpression($scope.project.id, $scope.newClauses[i].value,
                          refset.terminology, refset.version)
                        .then(
                          // Success - count expression
                          function(data) {
                            var count = data;
                            if (count >= 40000) { 
                              $scope.errors[0] = 'Submitted definition clause is invalid.  Definition clause resolves to '
                                + count + ' members.  Refsets of this size are not allowed as they can lead to an inconsistent user experience.';
                              return;
                            } else if (count >= 20000) {
                              $scope.warnings[$scope.newClauses[i].value] = 'Definition clause resolves to '
                                + count + ' members.  Refsets of this size are discouraged and can lead to an inconsistent user experience.';
                              $scope.warningFlag = true;
                            }
                          },
                          // Error - count expression
                          function(data) {
                            handleError($scope.errors, data);
                          });
                    }
                  }
                };

                // add new clause
                $scope.addClause = function(refset, clause) {
                  $scope.errors = [];

                  // Confirm clauses are unique, skip if not
                  for (var i = 0; i < $scope.newClauses.length; i++) {
                    if ($scope.newClauses[i].value == clause.value) {
                      $scope.errors[0] = 'Duplicate definition clause';
                      return;
                    }
                    if ($scope.newClauses[i].value.indexOf("MINUS") != -1) {
                      $scope.errors[0] = 'Definition clause may not contain MINUS';
                      return;
                    }
                    if ($scope.newClauses[i].value.indexOf(" OR ") != -1) {
                      $scope.errors[0] = 'Definition clause may not contain OR';
                      return;
                    }
                  }
                  refsetService
                    .isExpressionValid($scope.refset.projectId, clause.value, refset.terminology,
                      refset.version)
                    .then(
                      // Success - add refset
                      function(data) {
                        if (data == 'true') {
                          $scope.newClauses.push(clause);
                          $scope.getPagedClauses();
                          $scope.newClause = null;
                          $scope.warnings = {};
                          $scope.warningFlag = false;
                          refsetService
                            .countExpression($scope.refset.projectId, clause.value,
                              refset.terminology, refset.version)
                            .then(
                              // Success - count expression
                              function(data) {
                                var count = data;
                                if (count >= 40000) {
                                	 $scope.errors[0] = 'Submitted definition clause is invalid.  Definition clause resolves to '
                                     + count + ' members.  Refsets of this size are not allowed as they can lead to an inconsistent user experience.';
                                     return;
                                } else if (count >= 20000) {
                                  $scope.warnings[$scope.newClauses[i].value] = 'Definition clause resolves to '
                                    + count + ' members.  Refsets of this size are discouraged and can lead to an inconsistent user experience.';
                                  $scope.warningFlag = true;
                                }
                              },
                              // Error - count expression
                              function(data) {
                                handleError($scope.errors, data);
                              });
                        } else {
                          $scope.errors[0] = 'Submitted definition clause is invalid';
                          return;
                        }
                      },
                      // Error - add refset
                      function(data) {
                        handleError($scope.errors, data);
                      });
                };

                // Save refset
                $scope.save = function(refset) {
                  if ($scope.errors.length > 0) { 
                	  $uibModalInstance.close(refset);
                	  handleError($scope.errors, $scope.errors[0]);
                	  return;
                  }
                  refset.definitionClauses = $scope.newClauses;
                  $scope.warnings = [];
                  refsetService.updateRefset(refset).then(
                  // Success - add refset
                  function(data) {
                    $uibModalInstance.close(refset);
                    $scope.selected.concept = null;
                  },
                  // Error - add refset
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

                // initialize modal
                $scope.getPagedClauses();
              };

              // Notes modal
              $scope.openNotesModal = function(lobject, ltype) {
                console.debug('openNotesModal ', lobject, ltype);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/notes.html',
                  controller : NotesModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    object : function() {
                      return lobject;
                    },
                    type : function() {
                      return ltype;
                    },
                    tinymceOptions : function() {
                      return utilService.tinymceOptions;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.handleWorkflow(data);
                });

              };

              // Notes controller
              var NotesModalCtrl = function($scope, $uibModalInstance, $sce, utilService, object,
                type, tinymceOptions) {
                console.debug('Entered notes modal control', object, type);
                $scope.object = object;
                $scope.type = type;
                $scope.tinymceOptions = tinymceOptions;
                $scope.newNote = null;

                // Paging parameters
                $scope.pageSize = 5;
                $scope.pagedNotes = [];
                $scope.pageSizes = utilService.getPageSizes();
                $scope.paging = {};
                $scope.paging['notes'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.errors = [];

                // Get paged notes (assume all are loaded)
                $scope.getPagedNotes = function() {
                  $scope.pagedNotes = utilService.getPagedArray($scope.object.notes,
                    $scope.paging['notes'], $scope.pageSize);
                };

                $scope.getNoteValue = function(note) {
                  return $sce.trustAsHtml(note.value);
                };

                // remove note
                $scope.removeNote = function(object, note) {

                  if ($scope.type == 'Refset') {
                    refsetService.removeRefsetNote(object.id, note.id).then(
                    // Success - add refset
                    function(data) {
                      $scope.newNote = null;
                      refsetService.getRefset(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add refset
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - add refset
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  } else if ($scope.type == 'Member') {
                    refsetService.removeRefsetMemberNote(object.id, note.id).then(
                    // Success - add refset
                    function(data) {
                      $scope.newNote = null;
                      refsetService.getMember(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add refset
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - add refset
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }
                };

                // add new note
                $scope.submitNote = function(object, text) {

                  if ($scope.type == 'Refset') {
                    refsetService.addRefsetNote(object.id, text).then(
                    // Success - add refset
                    function(data) {
                      $scope.newNote = null;
                      refsetService.getRefset(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add refset
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - add refset
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  } else if ($scope.type == 'Member') {
                    refsetService.addRefsetMemberNote(object.refsetId, object.id, text).then(
                    // Success - add refset
                    function(data) {
                      $scope.newNote = null;

                      refsetService.getMember(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add refset
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - add refset
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }
                };

                // Convert date to a string
                $scope.toDate = function(lastModified) {
                  return utilService.toDate(lastModified);
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

                // initialize modal
                $scope.getPagedNotes();
              };

              // Clone Refset modal
              $scope.openCloneRefsetModal = function(lrefset) {
                console.debug('cloneRefsetModal ', lrefset);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/editRefset.html',
                  controller : CloneRefsetModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
                    },
                    metadata : function() {
                      return $scope.metadata;
                    },
                    filters : function() {
                      return $scope.filters;
                    },
                    projects : function() {
                      return $scope.projects;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {

                  // Successful clone, go to the refset
                  $scope.user.userPreferences.lastRefsetAccordion = 'AVAILABLE';
                  $scope.user.userPreferences.lastProjectRole = 'AUTHOR';
                  $scope.user.userPreferences.lastProjectId = data.projectId;
                  $scope.user.userPreferences.lastRefsetId = data.id;
                  securityService.updateUserPreferences($scope.user.userPreferences).then(
                  // Success
                  function() {

                    if ($location.url() == '/refset?clone=true') {
                      $window.location.reload();
                    } else {
                      $location.url('/refset?clone=true');
                      $window.location.reload();
                    }
                  });
                });

              };

              // Clone Refset controller
              var CloneRefsetModalCtrl = function($scope, $uibModalInstance, refset, filters,
                metadata, projects) {
                console.debug('Entered clone refset modal control', refset, projects);

                $scope.action = 'Clone';
                $scope.project = null;
                $scope.projects = projects;
                $scope.localMetadata = angular.copy(metadata);
                $scope.filters = filters;
                $scope.versionsMap = {};
                $scope.terminologies = [];
                $scope.versions = [];
                $scope.validVersion = null;

                // Copy refset and clear terminology id
                $scope.refset = JSON.parse(JSON.stringify(refset));
                $scope.newRefset = null;
                $scope.refset.terminologyId = null;
                $scope.refset.version = null;
                $scope.modules = [];
                $scope.errors = [];
                $scope.comments = [];

                // Handler for project change
                $scope.projectSelected = function(project) {
                  $scope.project = project;
                  $scope.refset.namespace = project.namespace;
                  if (!$scope.refset.localSet) {
                    $scope.refset.moduleId = project.moduleId;
                  }
                  $scope.getTerminologyEditions();
                };

                // Get $scope.terminologies
                $scope.getTerminologyEditions = function() {
                  projectService.getTerminologyEditions($scope.project).then(function(data) {
                    $scope.terminologies = data.terminologies;
                    $scope.localMetadata.versions = {};
                    
                    // Look up all versions
                    for (var i = 0; i < data.terminologies.length; i++) {
                      $scope.getTerminologyVersions($scope.project, data.terminologies[i].terminology);
                    }
                    $scope.refset.terminology = $scope.project.terminology;
                    if (!$scope.refset.terminology) {
                      $scope.refset.terminology = data.terminologies[0];
                    }

                  });

                };

                // Get $scope.versions
                $scope.getTerminologyVersions = function(project, terminology) {
                  projectService.getTerminologyVersions(project, terminology).then(function(data) {
                    $scope.localMetadata.versions[terminology] = [];
                    for (var i = 0; i < data.terminologies.length; i++) {
                      $scope.localMetadata.versions[terminology].push(data.terminologies[i].version);
                      if (terminology == $scope.project.terminology) {
                        $scope.terminologySelected(terminology);
                      }
                    }
                  });
                };

                // Get $scope.modules
                $scope.getModules = function() {
                  if ($scope.refset.terminology && $scope.refset.version) {
                    projectService.getModules($scope.project, $scope.refset.terminology,
                      $scope.refset.version).then(
                    // Success
                    function(data) {
                      $scope.modules = data.concepts;
                    });
                  }
                };

                $scope.testTerminologyVersion = function() {
                  refsetService.isTerminologyVersionValid($scope.project.id,
                    $scope.refset.terminology, $scope.refset.version).then(function(data) {
                    $scope.validVersion = data;
                  });
                }

                $scope.resetValidVersion = function() {
                  $scope.validVersion = null;
                }

                // Determine whether the refset version is in the list
                $scope.versionNotInPicklist = function() {
                  for (var i = 0; i < $scope.versions.length; i++) {
                    if ($scope.versions[i] == $scope.refset.version) {
                      $scope.validVersion = 'true';
                      return false;
                    }
                  }
                  return true;
                }

                // Handle terminology selected
                $scope.terminologySelected = function(terminology) {
                  $scope.versions = refsetService.filterTerminologyVersions(
                    $scope.project.terminologyHandlerKey, terminology, $scope.localMetadata.versions);
                  if ($scope.project.terminologyHandlerKey === 'MANAGED-SERVICE') {
                    if (terminology === 'SNOMEDCT') {
                      $scope.refset.version = "MAIN";
                    } else {
                      $scope.refset.version = "MAIN/" + terminology;
                    }
                  }
		                };

                // Handle version selected
                $scope.versionSelected = function(version) {
                  $scope.getModules();
                };

                // Assign refset id
                $scope.assignRefsetTerminologyId = function(refset) {
                  refsetService.assignRefsetTerminologyId(refset.projectId, refset).then(
                  // success
                  function(data) {
                    refset.terminologyId = data;
                  },
                  // error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Submit refset
                $scope.submitRefset = function(refset) {

                  if (!$scope.project) {
                    $scope.errors[0] = 'A project must be chosen from the picklist.';
                    return;
                  }
                  // validate refset before cloning it
                  validationService.validateRefset(refset).then(
                    function(data) {

                      // If there are errors, make them available and stop.
                      if (data.errors && data.errors.length > 0) {
                        $scope.errors = data.errors;
                        return;
                      } else {
                        $scope.errors = [];
                      }

                      // if $scope.warnings is empty, and data.warnings is not,
                      // show warnings and stop
                      if (data.warnings && data.warnings.length > 0
                        && $scope.warnings.join() !== data.warnings.join()) {
                        $scope.warnings = data.warnings;
                        return;
                      } else {
                        $scope.warnings = [];
                      }
                      $scope.comments = [];
                      refsetService.cloneRefset($scope.project.id, refset).then(
                        // Success - clone refset
                        function(data) {
                          $scope.newRefset = data;

                          // Show a message upon completion
                          var name = null;
                          for (var i = 0; i < $scope.projects.data.length; i++) {
                            if (data.projectId == $scope.projects.data[i].id) {
                              name = $scope.projects.data[i].name;
                            }
                          }
                          $scope.comments.push('Refset "' + data.name
                            + '" successfully cloned to "' + name + '"');

                          startLookup(newRefset);
                          
                        },
                        // Error - clone refset
                        function(data) {
                          handleError($scope.errors, data);
                        });
                    },
                    // Error - validate
                    function(data) {
                      handleError($scope.errors, data);
                    });
                };

     
                // Dismiss modal
                $scope.close = function() {
                  $uibModalInstance.close($scope.newRefset);
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };
              
              
              $scope.openImportFromExistingProjectsModal = function() {
                console.debug('openImportFromExistingProjectsModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/importFromExistingProjects.html',
                  controller : ImportFromExistingProjectsCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return $scope.selected.refset;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.handleWorkflow(data);
                });
              };
              
              // Add Refset Member List controller
              var ImportFromExistingProjectsCtrl = function($scope, $uibModalInstance, utilService, refset, refsetService) {
                console.debug('Entered add refset member list modal control', utilService, refset, refsetService);
                
                $scope.refsetMemberList = [];
                $scope.refset = refset;
                $scope.save = [];
                $scope.added = [];
                $scope.exists = [];
                $scope.invalid = [];
                $scope.refsets = [];
                $scope.originalRefsets = [];
                $scope.includeRefsets = [];
                $scope.errors = [];
                $scope.warnings = [];
                $scope.comments = [];
                
                $scope.workflowStatusList = [];
                $scope.moduleIdList = [];
                $scope.effectiveTimeList = [];
                
                refsetService.getRefsetsForProject($scope.refset.projectId).then(
                    // Success
                    function(data) {
                      if (data.refsets.length > 0) {
                        $scope.refsets = data.refsets;
                        $scope.originalRefsets = data.refsets;
                        //remove current refset
                        $scope.refsets = $scope.refsets.filter(r => r.id !== $scope.refset.id);

                        $scope.refsets.forEach(function(refset) {
                          if (!$scope.workflowStatusList.includes(refset.workflowStatus)) {
                            $scope.workflowStatusList.push(refset.workflowStatus)
                          }
                          if (!$scope.moduleIdList.includes(refset.moduleId)) {
                            $scope.moduleIdList.push(refset.moduleId)
                          }
                          if (refset.effectiveTime && !$scope.effectiveTimeList.includes(utilService.toShortDate(refset.effectiveTime))) {
                            $scope.effectiveTimeList.push(utilService.toShortDate(refset.effectiveTime))
                          }                          
                        });
                      } else {
                        console.info("None returned");
                      }
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    }
                );
                
                $scope.filterRefsets = function() {
                  
                  $scope.refsets = JSON.parse(JSON.stringify($scope.originalRefsets));
                  
                  console.debug("workflow:", $scope.selectedWorkflowStatus);
                  console.debug("module:", $scope.selectedModule);
                  console.debug("eff time:", $scope.selectedEffectiveTime);
                  
                  if ($scope.selectedWorkflowStatus && $scope.selectedWorkflowStatus !== "") {
                    $scope.refsets = $scope.refsets.filter(
                      r => r.workflowStatus === $scope.selectedWorkflowStatus);
                  }
                  if ($scope.selectedModule && $scope.selectedModule !== "") {
                    $scope.refsets = $scope.refsets.filter(
                      r => r.moduleId === $scope.selectedModule);
                  }
                  if ($scope.selectedEffectiveTime && $scope.selectedEffectiveTime !== "") {
                    $scope.refsets = $scope.refsets.filter(
                      r => utilService.toShortDate(r.effectiveTime) === $scope.selectedEffectiveTime);
                  }
                  //exclude items which were added
                  angular.forEach($scope.includeRefsets, function(refset) {
                    $scope.refsets = $scope.refsets.filter(r => r.id !== refset.id );
                  });
                }
                
                // Add members in the list
                $scope.includeRefset = function() {
                  console.debug("Include refset", $scope.selectedRefset.id);
                  $scope.refsets = $scope.refsets.filter(r => r.id !== $scope.selectedRefset.id);
                  $scope.includeRefsets.push($scope.selectedRefset);
                  console.debug("Include refsets:", $scope.includeRefsets);
                  
                  // initialize
                  var pfs = {
                    startIndex : 0
                  };
                  
                  //get members for refset (refsetId, query, pfs, translated)
                  refsetService.findRefsetMembersForQuery($scope.selectedRefset.id, '', pfs).then(
                      function(data) {
                        if (data.members.length > 0 ) {
                          console.debug("contains", data.members);
                          angular.forEach(data.members, function(member) {
                            var exists = $scope.refsetMemberList.filter(m => m.conceptId === member.conceptId);
                            if (exists.length === 0 ) {
                              $scope.refsetMemberList.push(member);
                            } else {
                              $scope.exists.push(member);
                            }
                          });
                        }
                      }
                  );
                };
                
                // remove from list and add to select.
                $scope.removeRefset = function(refset) {
                  console.debug("Remove refset", refset.id, refset.name);
                  $scope.includeRefsets = $scope.includeRefsets.filter(r => r.id !== refset.id);
                  $scope.refsetMemberList = $scope.refsetMemberList.filter(r => r.refsetId !== refset.id);
                  $scope.exists = $scope.exists.filter(r => r.refsetId !== refset.id);
                  $scope.refsets.push(refset);
                  $scope.filterRefsets();
                  
                  console.debug("REMOVE ", 
                      "refsetMemberList: ", $scope.refsetMemberList.length,
                      "save: ", $scope.save.length,
                      "added: ", $scope.added.length,
                      "exists: ", $scope.exists.length,
                      "invalid: ", $scope.invalid.length,
                      "indcludeRefsets: ", $scope.includeRefsets.length,
                      "errors: ", $scope.errors.length,
                      "warnings: ", $scope.warnings.length,
                      "comments: ", $scope.comments.length     
                  );
                };
                
                $scope.clear = function() {
                  console.debug("Clear refsets");
                  angular.forEach($scope.includeRefsets, function(refset) {
                    $scope.refsets.push(refset);
                  });
                  $scope.refsetMemberList = [];
                  $scope.includeRefsets = [];
                  $scope.exists = [];
                  $scope.filterRefsets();
                  
                  console.debug("CLEAR ", 
                      "refsetMemberList: ", $scope.refsetMemberList.length,
                      "save: ", $scope.save.length,
                      "added: ", $scope.added.length,
                      "exists: ", $scope.exists.length,
                      "invalid: ", $scope.invalid.length,
                      "indcludeRefsets: ", $scope.includeRefsets.length,
                      "errors: ", $scope.errors.length,
                      "warnings: ", $scope.warnings.length,
                      "comments: ", $scope.comments.length     
                  );
                  
                };
                
                $scope.importMembers = function (){
                  var response = $window.confirm('All members of Refset ' + $scope.refset.name 
                      + ' will be removed before adding new members, are you sure you want to proceed?')
                  if ($scope.refsetMemberList.length > 0 && response) {
                    console.debug("Remove members", $scope.refset.name);
                    console.debug("Import members", $scope.refsetMemberList.length);                    
                    refsetService.removeAllRefsetMembers(refset.id).then(function(data) {
                      refsetService.fireRefsetChanged(refset);
                      includeMembers($scope.refset, $scope.refsetMemberList);
                    });
                  }
                };
                
                function includeMembers(refset, members) {
                  console.debug("add member");
                  angular.forEach(members, function(member){
                    // check that concept id is only digits before proceeding
                    var reg = /^\d+$/;
                    if (!reg.test(member.conceptId)) {
                      console.debug("invalid member", member);
                      $scope.invalid.push(member);
                      return;
                    } else {
                      var newMember = {
                        active : true,
                        conceptId : member.conceptId,
                        memberType : 'MEMBER',
                        moduleId : refset.moduleId,
                        refsetId : refset.id
                      };
                      $scope.save.push(newMember);
                    }                    
                  });
                  refsetService.addRefsetMembers($scope.save).then(
                    // Success
                    function(data) {
                      $scope.added = data.members;
                      console
                      console.debug("members added ", data.members.length);
                      $scope.save = [];  // clear
                    },
                    // Error
                    function(data) {
                      $scope.save = []; // clear
                      handleError($scope.errors, data);
                    }
                  );
                }

                // Dismiss modal
                $scope.close = function() {
                  $uibModalInstance.close(refset);
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };
                
                $scope.downloadDuplicates = function() {
                  console.debug("download duplicates");
                  var filerows = "id\teffectiveTime\tactive\tmoduleId\trefsetId\treferencedComponentId\r\n";
                  angular.forEach($scope.exists, function(member) {
                    var row = "" + member.terminologyId 
                      + "\t" + ((member.effectiveTime !== "null") ? utilService.toShortDate(member.effectiveTime) : "")  
                      + "\t" + ((member.active === true) ? "1" : "0") 
                      + "\t" + ((member.moduleId !== "null") ? member.moduleId : "")  
                      + "\t" + member.refsetId
                      + "\t" + member.conceptId
                      + "\r\n";
                    filerows = filerows + row;
                  });
                  
                  var blob = new Blob([filerows], { type: "text/plain;charset=utf-8;"});
                  var downloadLink = document.createElement('a');
                  downloadLink.setAttribute('download', 'duplicates.txt');
                  downloadLink.setAttribute('href', window.URL.createObjectURL(blob));
                  downloadLink.click();
                }
                
                $scope.selectDisplay = function (refset) {
                  var display = refset.name + ' - ' + refset.moduleId + ' - ' + refset.workflowStatus;
                  if (refset.effectiveTime != null) {
                    var d = utilService.toShortDate(new Date(refset.effectiveTime));
                    return display + ' - ' + d;
                  }
                  return display
                }
                
              }; //end 
              
              // Import/Export modal
              $scope.openImportExportModal = function(lrefset, loperation, ltype) {
                console.debug('exportModal ', lrefset);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/importExport.html',
                  controller : ImportExportModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
                    },
                    metadata : function() {
                      return $scope.metadata;
                    },
                    operation : function() {
                      return loperation;
                    },
                    type : function() {
                      return ltype;
                    },
                    ioHandlers : function() {
                      if (loperation == 'Import') {
                        return $scope.metadata.refsetImportHandlers;
                      } else {
                        return $scope.metadata.refsetExportHandlers;
                      }
                    },
                    query : function() {
                      return $scope.paging['member'].filter;
                    },
                    pfs : function() {
                      var pfs = prepPfs();
                      pfs.startIndex = -1;
                      return pfs;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  if (loperation == 'Import') {
                    $scope.handleWorkflow(data);
                  } else {
                    refsetService.fireRefsetChanged(data);
                  }
                });
              };
              
              // Import/Export controller
              var ImportExportModalCtrl = function($scope, $uibModalInstance, refset, metadata,
                operation, type, ioHandlers, query, pfs) {
                console.debug('Entered import export modal control', refset.id, ioHandlers,
                  operation, type);
                $scope.refset = refset;
                $scope.metadata = metadata;
                $scope.query = query;
                $scope.pfs = pfs;
                $scope.ioHandlers = [];
                // Skip "with name" handlers if user is not logged in
                // IHTSDO-specific, may be able make this more data driven
                for (var i = 0; i < ioHandlers.length; i++) {
                  if (!securityService.isLoggedIn()
                    && ioHandlers[i].name.toLowerCase().indexOf('with name') != -1) {
                    continue;
                  }
                  $scope.ioHandlers.push(ioHandlers[i]);
                }

                $scope.selectedIoHandler = null;
                for (var i = 0; i < ioHandlers.length; i++) {

                  // Choose first one if only one
                  if ($scope.selectedIoHandler == null) {
                    $scope.selectedIoHandler = ioHandlers[i];
                  }
                  // choose 'rf2' as default otherwise
                  // IHTSDO-specific, may be able make this more data driven
                  if (ioHandlers[i].name.endsWith('RF2')) {
                    $scope.selectedIoHandler = ioHandlers[i];
                  }
                }
                $scope.type = type;
                $scope.operation = operation;
                $scope.comments = [];
                $scope.warnings = [];
                $scope.errors = [];
                $scope.importStarted = false;
                $scope.importFinished = false;
                if ($scope.query || $scope.pfs) {
                  $scope.warnings
                    .push(operation + " is based on current search criteria and may not include all members.");
                }
                // Handle export
                $scope.export = function(file) {
                  if (type === 'Definition') {
                    refsetService.exportDefinition($scope.refset, $scope.selectedIoHandler);
                  }
                  if (type === 'Refset Members') {
                    refsetService.exportMembers($scope.refset, $scope.selectedIoHandler,
                      $scope.query, $scope.pfs);
                  }
                  $uibModalInstance.close(refset);
                };
                
                $scope.exportDuplicateMembers = function(file) {
                  if (type === 'Refset Members') {
                    //GET concept Ids from file
                    refsetService.exportDuplicateMembers($scope.refset, $scope.selectedIoHandler, $scope.conceptIds);
                  }
                  $uibModalInstance.close(refset);
                }
                
                
                // Handle import
                $scope.import = function(file) {

                  if (type == 'Definition') {
                    refsetService.importDefinition($scope.refset.id, $scope.selectedIoHandler.id,
                      file).then(
                    // Success - close dialog
                    function(data) {
                      $uibModalInstance.close(refset);
                    },
                    // Failure - show error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }

                  if (type === 'Refset Members') {
                    
                    var conceptIds = [];
                    var reader = new FileReader();
                    reader.onload = function(progressEvent) {
                      var lines = this.result.split(/\r\n|\n/);
                      for(var i = 1; i < lines.length; i++){
                        //console.log("    LINE: ", lines[i]);
                        if (lines[i] !== "") {
                          var fields = lines[i].split(/\t/);
                          //console.log("    CONCEPT: ", fields[5]);
                          conceptIds.push(fields[5]);
                        }
                        $scope.conceptIds = conceptIds;
                      }
                      
                      refsetService.beginImportMembers($scope.refset.id, $scope.selectedIoHandler.id, $scope.conceptIds)
                      .then(

                        // Success
                        function(data) {
                          $scope.importStarted = true;
                          // data is a validation result, check for errors
                          if (data.errors.length > 0) {
                          if (data.errors.includes("Refset contains duplicate members.")) {
                            $scope.showDuplicatesExport = true;
                          } 
                            $scope.errors = data.errors;                           
                          } else {
                            // If there are no errors, finish import
                            refsetService.finishImportMembers($scope.refset.id,
                              $scope.selectedIoHandler.id, file).then(
                            // Success - close dialog
                            function(data) {
                              $scope.importFinished = true;
                              $scope.comments = data.comments;
                              $scope.warnings = data.warnings;
                              $scope.errors = data.errors;
                              startLookup(refset);
                            },
                            // Failure - show error
                            function(data) {
                              handleError($scope.errors, data);
                            });
                          }
                        },

                        // Failure - show error, clear global error
                        function(data) {
                          handleError($scope.errors, data);
                        });
                    }
                    reader.readAsText(file, "UTF-8");
                  }
                };

                // Handle continue import
                $scope.continueImport = function(file) {

                  if (type == 'Refset Members') {
                    refsetService.finishImportMembers($scope.refset.id,
                      $scope.selectedIoHandler.id, file).then(
                    // Success - close dialog
                    function(data) {
                      $scope.importFinished = true;
                      $scope.comments = data.comments;
                      $scope.warnings = data.warnings;
                      $scope.errors = data.errors;
                      startLookup(refset);
                    },
                    // Failure - show error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }
                };

                // Dismiss modal
                $scope.cancel = function() {
                  // If there are lingering errors, cancel the import
                  if ($scope.errors.length > 0 && type == 'Refset Members') {
                    refsetService.cancelImportMembers($scope.refset.id);
                  }
                  // dismiss the dialog
                  $uibModalInstance.dismiss('cancel');
                };

                $scope.close = function() {
                  // close the dialog and reload refsets
                  $uibModalInstance.close(refset);
                };
              };
              
              // Release Process modal
              $scope.openReleaseProcessModal = function(lrefset) {
                console.debug('releaseProcessModal ', lrefset);
                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/release.html',
                  controller : ReleaseProcessModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
                    },
                    ioHandlers : function() {
                      return $scope.metadata.refsetExportHandlers;
                    },
                    utilService : function() {
                      return utilService;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  refsetService.fireRefsetChanged(data);
                });
              };

              // Open release process modal given staged refset
              $scope.openReleaseProcessModalForStaged = function(refset) {

                refsetService.getOriginForStagedRefsetId(refset.id).then(
                // Success
                function(data) {
                  refsetService.getRefset(data).then(
                  // Success
                  function(data) {
                    $scope.openReleaseProcessModal(data);
                  });
                });
              };

              // Release Process controller
              var ReleaseProcessModalCtrl = function($scope, $uibModalInstance, refset, ioHandlers,
                utilService) {
                console.debug('Entered release process modal', refset.id, ioHandlers);

                $scope.refset = refset;
                $scope.ioHandlers = ioHandlers;
                $scope.selectedIoHandler = $scope.ioHandlers[0];
                $scope.releaseInfo = [];
                $scope.validationResult = null;
                $scope.format = 'yyyyMMdd';
                $scope.releaseDate = utilService.toSimpleDate($scope.refset.effectiveTime);
                $scope.status = {
                  opened : false
                };
                $scope.errors = [];

                if (refset.stagingType == 'BETA') {
                  releaseService.resumeRelease(refset.id).then(
                  // Success
                  function(data) {
                    $scope.stagedRefset = data;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                }

                // Begin release
                $scope.beginRefsetRelease = function(refset) {

                  if (!refset.effectiveTime) {
                     window.alert('Release Date cannot be empty');
                     return;
                  }
                	
                  releaseService.beginRefsetRelease(refset.id,
                    utilService.toWCISimpleDate(refset.effectiveTime)).then(
                  // Success
                  function(data) {
                    $scope.releaseInfo = data;
                    $scope.refset.inPublicationProcess = true;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                // Validate release
                $scope.validateRefsetRelease = function(refset) {

                  releaseService.validateRefsetRelease(refset.id).then(
                  // Success
                  function(data) {
                    $scope.validationResult = data;
                    refsetService.fireRefsetChanged(refset);
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Initiate Beta
                $scope.betaRefsetRelease = function(refset) {
                  // clear validation result
                  $scope.validationResult = null;
                  releaseService.betaRefsetRelease(refset.id, $scope.selectedIoHandler.id).then(
                  // Success
                  function(data) {
                    $scope.stagedRefset = data;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Finish the release
                $scope.finishRefsetRelease = function(refset) {

                  releaseService.finishRefsetRelease(refset.id, $scope.selectedIoHandler.id).then(
                  // Success
                  function(data) {
                    $uibModalInstance.close(refset);
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Cancel release process and dismiss modal
                $scope.cancel = function() {
                  releaseService.cancelRefsetRelease($scope.refset.id).then(
                  // Success
                  function(data) {
                    $uibModalInstance.close($scope.refset);
                  });
                };

                // Close the window - to return later
                $scope.close = function() {
                  $uibModalInstance.close($scope.refset);
                };

                $scope.open = function($event) {
                  $scope.status.opened = true;
                };

                $scope.format = 'yyyyMMdd';
              };

              // Assign refset modal
              $scope.openAssignRefsetModal = function(lrefset, laction) {
                console.debug('openAssignRefsetModal ', lrefset, laction);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/assignRefset.html',
                  controller : AssignRefsetModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
                    },
                    metadata : function() {
                      return $scope.metadata;
                    },
                    action : function() {
                      return laction;
                    },
                    currentUser : function() {
                      return $scope.user;
                    },
                    assignedUsers : function() {
                      return $scope.projects.assignedUsers;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    role : function() {
                      return $scope.projects.role;
                    },
                    tinymceOptions : function() {
                      return utilService.tinymceOptions;
                    },
                    refsetAuthorsMap : function() {
                      return $scope.refsetAuthorsMap;
                    },
                    refsetReviewersMap : function() {
                      return $scope.refsetReviewersMap;
                    }
                  }

                });

                modalInstance.result.then(
                // Success
                function(data) {
                  refsetService.fireRefsetChanged(data);
                });
              };

              // Assign refset controller
              var AssignRefsetModalCtrl = function($scope, $uibModalInstance, $sce, refset,
                metadata, action, currentUser, assignedUsers, project, role, tinymceOptions,
                refsetAuthorsMap, refsetReviewersMap) {
                console.debug('Entered assign refset modal control', assignedUsers, project.id);
                $scope.refset = refset;
                $scope.metadata = metadata;
                $scope.workflowConfig = metadata.workflowConfig;
                $scope.action = action;
                $scope.project = project;
                $scope.role = workflowService.refsetGetRole(action, role, refset.workflowStatus,
                  $scope.workflowConfig);
                $scope.tinymceOptions = tinymceOptions;
                $scope.assignedUsers = [];
                $scope.user = utilService.findBy(assignedUsers, currentUser, 'userName');
                $scope.note;
                $scope.errors = [];
                $scope.feedbackRoleOptions = [];

                // if feedback assignment, only options are users that were
                // authors or reviewers
                if (action == 'FEEDBACK') {
                  var previousUsers = [];
                  $scope.role = 'AUTHOR';
                  var authors = refsetAuthorsMap[$scope.refset.id];
                  var reviewers = refsetReviewersMap[$scope.refset.id];
                  for (var i = 0; i < authors.length; i++) {
                    for (var j = 0; j < assignedUsers.length; j++) {
                      if (authors[i] == assignedUsers[j].userName
                        && currentUser.userName != authors[i]
                        && previousUsers.indexOf(assignedUsers[j]) == -1) {
                        previousUsers.push(assignedUsers[j]);
                        $scope.feedbackRoleOptions.push('AUTHOR');
                      }
                    }
                  }
                  for (var i = 0; i < reviewers.length; i++) {
                    for (var j = 0; j < assignedUsers.length; j++) {

                      if (reviewers[i] == assignedUsers[j].userName
                        && currentUser.userName != reviewers[i]
                        && previousUsers.indexOf(assignedUsers[j]) == -1) {
                        previousUsers.push(assignedUsers[j]);
                        if ($scope.feedbackRoleOptions.indexOf('REVIEWER') == -1
                          && role != 'REVIEWER') {
                          $scope.feedbackRoleOptions.push('REVIEWER');
                        }
                      }
                    }
                  }
                  if (previousUsers.length == 0) {
                    previousUsers.push(currentUser);
                    $scope.feedbackRoleOptions.push('AUTHOR');
                  }
                  assignedUsers = previousUsers;
                  $scope.user = assignedUsers[0];
                }

                // Sort users by name and role restricts
                var sortedUsers = assignedUsers.sort(utilService.sortBy('name'));
                for (var i = 0; i < sortedUsers.length; i++) {
                  if ($scope.role == 'AUTHOR'
                    || $scope.project.userRoleMap[sortedUsers[i].userName] == 'REVIEWER'
                    || $scope.project.userRoleMap[sortedUsers[i].userName] == 'REVIEWER2'
                    || $scope.project.userRoleMap[sortedUsers[i].userName] == 'ADMIN') {
                    $scope.assignedUsers.push(sortedUsers[i]);
                  }
                }

                // Assign
                $scope.assignRefset = function() {
                  if (!$scope.user) {
                    $scope.errors[0] = 'The user must be selected. ';
                    return;
                  }

                  if (action == 'ASSIGN') {
                    workflowService.performWorkflowAction($scope.project.id, refset.id,
                      $scope.user.userName, $scope.role, 'ASSIGN').then(
                    // Success
                    function(data) {

                      // Add a note as well
                      if ($scope.note) {
                        refsetService.addRefsetNote(refset.id, $scope.note).then(
                        // Success
                        function(data) {
                          $uibModalInstance.close(refset);
                        },
                        // Error
                        function(data) {
                          handleError($scope.errors, data);
                        });
                      }
                      // close dialog if no note
                      else {
                        $uibModalInstance.close(refset);
                      }

                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  } else if (action == 'FEEDBACK') {
                    workflowService.performWorkflowAction($scope.project.id, refset.id,
                      $scope.user.userName, $scope.role, 'FEEDBACK').then(
                    // Success
                    function(data) {

                      // Add a note as well
                      if ($scope.note) {
                        refsetService.addRefsetNote(refset.id, $scope.note).then(
                        // Success
                        function(data) {
                          $uibModalInstance.close(refset);
                        },
                        // Error
                        function(data) {
                          handleError($scope.errors, data);
                        });
                      }
                      // close dialog if no note
                      else {
                        $uibModalInstance.close(refset);
                      }

                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }
                }

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Log modal
              $scope.openLogModal = function() {
                console.debug('openLogModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/log.html',
                  controller : LogModalCtrl,
                  backdrop : 'static',
                  size : 'lg',
                  resolve : {
                    refset : function() {
                      return $scope.selected.refset;
                    },
                    project : function() {
                      return $scope.project;
                    }
                  }
                });

                // NO need for result function - no action on close
                // modalInstance.result.then(function(data) {});
              };

              // Log controller
              var LogModalCtrl = function($scope, $uibModalInstance, refset, project) {
                console.debug('Entered log modal control', refset, project);

                $scope.filter = '';
                $scope.errors = [];
                $scope.warnings = [];

                // Get log to display
                $scope.getLog = function() {
                  projectService.getLog(project.id, refset.id, $scope.filter).then(
                  // Success
                  function(data) {
                    $scope.log = data;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                // Dismiss modal
                $scope.close = function() {
                  // nothing changed, don't pass a refset
                  $uibModalInstance.close();
                };

                // initialize
                $scope.getLog();
              };

              // Add Refset Member List modal
              $scope.openAddRefsetMemberListModal = function() {
                console.debug('openAddRefsetMemberListModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/addMemberList.html',
                  controller : AddRefsetMemberListModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return $scope.selected.refset;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.handleWorkflow(data);
                });
              };

              // Add Refset Member List controller
              var AddRefsetMemberListModalCtrl = function($scope, $uibModalInstance, refset) {
                console.debug('Entered add refset member list modal control', refset);

                $scope.refsetMemberList = '';
                $scope.refset = refset;
                $scope.memberIdList = '';
                $scope.ids = [];
                $scope.save = [];
                $scope.added = [];
                $scope.exists = [];
                $scope.removed = [];
                $scope.notExists = [];
                $scope.invalid = [];
                $scope.errors = [];
                $scope.warnings = [];
                $scope.comments = [];
                

                // Used for enabling/disabling in UI
                $scope.hasResults = function() {
                  return $scope.added.length > 0 || $scope.removed.length > 0
                    || $scope.invalid.length > 0
                    || $scope.exists.length > 0 || $scope.notExists.length > 0;
                };

                // Add members in the list
                $scope.includeMembers = function() {
                  $scope.errors = [];
                  $scope.ids = getIds($scope.memberIdList);
                  // practical limit
                  if ($scope.ids.length > 2100) {
                    window.alert('The practical limit for adding members from a list is 2000. '
                      + 'Break your list up into 2000 entry chunks.');
                    return;
                  }
                  // Pull all of the members in the refset, so we can make sure not to create duplicates
                  var alreadyPresentConceptIds = [];

                  refsetService.findRefsetMembersForQuery(refset.id, '', {
                    startIndex : 0
                  }).then(
                  // Success
                  function(data) {
                      for(var i = 0; i < data.members.length; i++) {
                        alreadyPresentConceptIds.push(data.members[i].conceptId)
                      }  
                      //Now check that list against the list we're attempting to add
                      for (var i = 0; i < $scope.ids.length; i++) {
                        var conceptId = $scope.ids[i];
                        // check that concept id is only digits before proceeding
                        var reg = /^\d+$/;
                        if (!reg.test(conceptId)) {
                        $scope.invalid.push(conceptId);
                        }
                        // check if the concept id already exists in the refset
                        else if(alreadyPresentConceptIds.includes(conceptId)){
                          $scope.exists.push(conceptId);
                        }
                        else {
                              var member = {
                                active : true,
                                conceptId : conceptId,
                                memberType : 'MEMBER',
                                moduleId : refset.moduleId,
                                refsetId : refset.id
                              };
                              $scope.save.push(member);
                            }
                      }
                      if($scope.save.length>0){
                        refsetService.addRefsetMembers($scope.save).then(
                          // Success
                          function(data) {
                            for(var i = 0; i < data.members.length; i++) {
                              $scope.added.push(data.members[i].conceptId)
                            }
                            console.debug("members added ", data.members.length);
                            $scope.save = [];  // clear
                          },
                          // Error
                          function(data) {
                            $scope.save = []; // clear
                            handleError($scope.errors, data);
                          }
                        );                        
                      }
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });      
                };

                // find member and add if not exists
                function includeMember(refset, conceptId) {
                  // check that concept id is only digits before proceding
                  var reg = /^\d+$/;
                  if (!reg.test(conceptId)) {
                	  $scope.invalid.push(conceptId);
                	  return;
                  }
                  refsetService.findRefsetMembersForQuery(refset.id, 'conceptId:' + conceptId, {
                    startIndex : 0,
                    maxResults : 1
                  }).then(
                  // Success
                  function(data) {
                    if (data.members.length > 0) {
                      $scope.exists.push(conceptId);
                    } else {
                      var member = {
                        active : true,
                        conceptId : conceptId,
                        memberType : 'MEMBER',
                        moduleId : refset.moduleId,
                        refsetId : refset.id
                      };
                      refsetService.addRefsetMember(member).then(
                      // Success
                      function(data) {
                        $scope.added.push(conceptId);
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    }
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                }

                // Exclude members in list
                $scope.excludeMembers = function() {
                  $scope.errors = [];
                  $scope.ids = getIds($scope.memberIdList);
                  var notExists = new Array();
                  var removed = new Array();
                  for (var i = 0; i < $scope.ids.length; i++) {
                    var conceptId = $scope.ids[i];
                    removeMember(refset, conceptId);
                  }
                };

                // validation
                function removeMember(refset, conceptId) {
                  refsetService.findRefsetMembersForQuery(refset.id, 'conceptId:' + conceptId, {
                    startIndex : 0,
                    maxResults : 1
                  }).then(
                  // Success
                  function(data) {

                    if (data.members.length == 0) {
                      $scope.notExists.push(conceptId);
                    } else {

                      var memberId = data.members[0].id;
                      refsetService.removeRefsetMember(memberId).then(
                      // Success
                      function(data) {
                        $scope.removed.push(conceptId);
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    }
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                }

                // Get the ids from the value
                function getIds(value) {
                  // Split on punctuation
                  var list = $scope.memberIdList.split(/[\s;,\.]/);

                  var result = new Array();
                  // remove empty stuff
                  for (var i = 0; i < list.length; i++) {
                    if (list[i]) {
                      result.push(list[i]);
                    }
                  }
                  var x = result.length;
                  result = utilService.uniq(result);
                  var y = result.length;
                  if (x != y) {
                    $scope.warnings.push((x - y) + " duplicate entries found.");
                  }
                  return result;
                }

                // Dismiss modal
                $scope.close = function() {
                  $uibModalInstance.close(refset);
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Add Refset modal
              $scope.openAddRefsetModal = function(localSet) {
                console.debug('openAddRefsetModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/editRefset.html',
                  controller : AddRefsetModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    metadata : function() {
                      return $scope.metadata;
                    },
                    filters : function() {
                      return $scope.filters;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    projects : function() {
                      return $scope.projects;
                    },
                    localSet : function() {
                      return localSet;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.selected.refset = data;
                  refsetService.fireRefsetChanged(data);
                });
              };

              // Add Refset controller
              var AddRefsetModalCtrl = function($scope, $uibModalInstance, metadata, filters,
                project, projects, localSet) {
                console.debug('Entered add refset modal control', metadata, project);

                $scope.action = 'Add';
                $scope.project = project;
                $scope.projects = projects;
                $scope.definition = null;
                $scope.metadata = metadata;
                $scope.validVersion = null;
                $scope.terminologies = metadata.terminologies;
                /*
                                 * $scope.metadataVersions =
                                 * $scope.metadata.versions[refset.terminology] ? angular
                                 * .copy($scope.metadata.versions[refset.terminology].sort().reverse()) :
                                 * [];
                                 */
                $scope.filters = filters;
                $scope.localSet = localSet;

                $scope.clause = {
                  value : null
                };
                $scope.refset = {
                  workflowPath : metadata.workflowPaths[0],
                  version : null,
                  namespace : $scope.project.namespace,
                  moduleId : $scope.project.moduleId,
                  organization : $scope.project.organization,
                  terminology : $scope.project.terminology,
                  feedbackEmail : $scope.project.feedbackEmail,
                  type : metadata.refsetTypes[0],
                  definitionClauses : [],
                  project : $scope.project,
                  localSet : $scope.localSet
                };
                $scope.modules = [];
                $scope.errors = [];
                $scope.warnings = [];

                $scope.versions = [];
                
                // Get $scope.modules
                $scope.getModules = function() {
                  projectService.getModules($scope.project, $scope.refset.terminology,
                    $scope.refset.version).then(
                  // Success
                  function(data) {
                    $scope.modules = data.concepts;
                  });
                };

                $scope.testTerminologyVersion = function() {
                  refsetService.isTerminologyVersionValid($scope.project.id,
                    $scope.refset.terminology, $scope.refset.version).then(function(data) {
                    $scope.validVersion = data;
                    $scope.getModules();
                  });
                }

                $scope.resetValidVersion = function() {
                  $scope.validVersion = null;
                }

                $scope.versionNotInPicklist = function() {
                  for (var i = 0; i < $scope.versions.length; i++) {
                    if ($scope.versions[i] == $scope.refset.version) {
                      $scope.validVersion = 'true';
                      return false;
                    }
                  }
                  return true;
                }

                // Initialize modules if terminology/version set
                if ($scope.refset.terminology && $scope.refset.version) {
                  $scope.getModules();
                }
                
                // Handle terminology selected
                $scope.terminologySelected = function(terminology) {
                  $scope.versions = refsetService.filterTerminologyVersions(
                    $scope.project.terminologyHandlerKey, terminology, $scope.metadata.versions);
                  if ($scope.project.terminologyHandlerKey === 'MANAGED-SERVICE') {
                    if (terminology === 'SNOMEDCT') {
                      $scope.refset.version = "MAIN";
                    } else {
                      $scope.refset.version = "MAIN/" + terminology;
                    }
                  }
                };

                // Handle version selected
                $scope.versionSelected = function(version) {
                  $scope.getModules();
                };

                // Assign refset id
                $scope.assignRefsetTerminologyId = function(refset) {
                  refsetService.assignRefsetTerminologyId(project.id, refset).then(
                  // success
                  function(data) {
                    refset.terminologyId = data;
                  },
                  // error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Add refset
                $scope.submitRefset = function(refset) {

                  refset.projectId = project.id;
                  refset.version = $scope.refset.version;
                  // Setup definition if configured
                  if (refset.type == 'EXTENSIONAL') {
                    $scope.clause = null;
                  }
                  if ($scope.clause && $scope.clause.value) {
                    refset.definitionClauses = [ {
                      value : $scope.clause.value,
                      negated : false
                    } ];
                  }
                  if (refset.localSet) {
                    refset.moduleId = 0;
                  }

                  // validate refset before adding it
                  validationService.validateRefset(refset).then(
                    function(data) {

                      // If there are errors, make them available and stop.
                      if (data.errors && data.errors.length > 0) {
                        $scope.errors = data.errors;
                        return;
                      } else {
                        $scope.errors = [];
                      }

                      // if $scope.warnings is empty, and data.warnings is
                      // not,
                      // show warnings and stop
                      if (data.warnings && data.warnings.length > 0
                        && $scope.warnings.join() !== data.warnings.join()) {
                        $scope.warnings = data.warnings;
                        return;
                      } else {
                        $scope.warnings = [];
                      }

                      if (!refset.localSet && !refset.moduleId) {
                        $scope.errors[0] = 'ModuleId must not be empty.';
                        return;
                      }

                      if (!refset.name || !refset.description) {
                        $scope.errors[0] = 'Refset name and description must not be empty.';
                        return;
                      }

                      // Success - validate refset
                      refsetService.addRefset(refset).then(
                      // Success - add refset
                      function(data) {
                        var newRefset = data;
                        //Run lookup if this is a new intensional refset with clauses specified
                        if ($scope.clause && $scope.clause.value) {
                          startLookup(newRefset);
                        }
                        $uibModalInstance.close(newRefset);
                      },
                      // Error - add refset
                      function(data) {
                        handleError($scope.errors, data);
                      });

                    },
                    // Error - validate refset
                    function(data) {
                      handleError($scope.errors, data);
                    });
                };

                // Initialize
                $scope.terminologySelected($scope.project.terminology);
                
                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Edit refset modal
              $scope.openEditRefsetModal = function(lrefset, localToRefsetConvertFlag) {
                console.debug('openEditRefsetModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/editRefset.html',
                  controller : EditRefsetModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      if (localToRefsetConvertFlag) {
                        // Copy and turn off the local set flag, when saved,
                        // this will convert the refset
                        var retval = angular.copy(lrefset);
                        retval.localSet = false;
                        return retval;
                      } else {
                        return lrefset;
                      }
                    },
                    metadata : function() {
                      return $scope.metadata;
                    },
                    filters : function() {
                      return $scope.filters;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    projects : function() {
                      return $scope.projects;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  // handle workflow advancement
                  $scope.handleWorkflow(data);
                  if (data == 'cancel') {
                    $scope.getRefsets();
                  }
                });
              };

              // Edit refset controller
              var EditRefsetModalCtrl = function($scope, $uibModalInstance, refset, metadata,
                filters, project, projects) {
                console.debug('Entered edit refset modal control', refset, metadata);

                $scope.action = 'Edit';
                $scope.refset = refset;
                $scope.filters = filters;
                $scope.project = project;
                $scope.refset.project = project;
                $scope.projects = projects;
                $scope.localSet = refset.localSet;
                $scope.metadata = metadata;
                $scope.validVersion = null;
                $scope.terminologies = metadata.terminologies;

                $scope.modules = [];
                $scope.errors = [];

                // Get $scope.modules
                $scope.getModules = function() {
                  projectService.getModules($scope.project, $scope.refset.terminology,
                    $scope.refset.version).then(
                  // Success
                  function(data) {
                    $scope.modules = data.concepts;
                  });
                };

                $scope.testTerminologyVersion = function() {
                  refsetService.isTerminologyVersionValid($scope.project.id,
                    $scope.refset.terminology, $scope.refset.version).then(function(data) {
                    $scope.validVersion = data;
                    $scope.getModules();
                  });
                }

                // Initialize modules if terminology/version set
                if ($scope.refset.terminology && $scope.refset.version) {
                  $scope.getModules();
                  $scope.testTerminologyVersion();
                }

                // Handle terminology selected
                $scope.terminologySelected = function(terminology) {
                  $scope.versions = refsetService.filterTerminologyVersions(
                    $scope.project.terminologyHandlerKey, terminology, $scope.metadata.versions);
                  if ($scope.project.terminologyHandlerKey === 'MANAGED-SERVICE') {
                    if (terminology === 'SNOMEDCT') {
                      $scope.refset.version = "MAIN";
                    } else {
                      $scope.refset.version = "MAIN/" + terminology;
                    }
                  } 
                };

                // Handle version selected
                $scope.versionSelected = function(version) {
                  $scope.getModules();
                };

                $scope.resetValidVersion = function() {
                  $scope.validVersion = null;
                }

                $scope.versionNotInPicklist = function() {
                  for (var i = 0; i < $scope.versions.length; i++) {
                    if ($scope.versions[i] == $scope.refset.version) {
                      $scope.validVersion = 'true';
                      return false;
                    }
                  }
                  return true;
                }

                // Assign refset id
                $scope.assignRefsetTerminologyId = function(refset) {
                  refsetService.assignRefsetTerminologyId(project.id, refset).then(
                  // success
                  function(data) {
                    refset.terminologyId = data;
                  },
                  // error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Update refset
                $scope.submitRefset = function(refset) {

                  // the model for the "project" in editRefset is refset.project
                  refset.projectId = refset.project.id;

                  // Validate refset
                  validationService.validateRefset(refset).then(
                    function(data) {

                      // If there are errors, make them available and stop.
                      if (data.errors && data.errors.length > 0) {
                        $scope.errors = data.errors;
                        return;
                      } else {
                        $scope.errors = [];
                      }

                      // if $scope.warnings is empty, and data.warnings is not,
                      // show warnings and stop
                      if (data.warnings && data.warnings.length > 0
                        && $scope.warnings.join() !== data.warnings.join()) {
                        $scope.warnings = data.warnings;
                        return;
                      } else {
                        $scope.warnings = [];
                      }

                      // Success - validate refset
                      refsetService.updateRefset(refset).then(
                      // Success - update refset
                      function(data) {
                        $uibModalInstance.close(refset);
                      },
                      // Error - update refset
                      function(data) {
                        handleError($scope.errors, data);
                      });

                    },
                    // Error - validate refset
                    function(data) {
                      handleError($scope.errors, data);
                    });

                };

                // initialize
                if ($scope.refset.version.lastIndexOf('SNOMEDCT-') >= 0) {
                  var index = $scope.refset.version.lastIndexOf('SNOMEDCT-');
                  var prevTerminology = $scope.refset.version.substring(index, index + 11);
                  $scope.terminologySelected(prevTerminology);
                  $scope.refset.terminology = prevTerminology;
                } else {
                  $scope.terminologySelected($scope.project.terminology);
                }
                
                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.close('cancel');
                };

              };

              $scope.openAddMemberModal = function(lrefset) {
                console.debug('openAddMemberModal ', lrefset);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/addMember.html',
                  controller : AddMemberModalCtrl,
                  backdrop : 'static',
                  size : 'lg',
                  resolve : {
                    metadata : function() {
                      return $scope.metadata;
                    },
                    refset : function() {
                      return lrefset;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    value : function() {
                      return $scope.value;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.handleWorkflow(data);
                });
              };

              // Add member controller
              var AddMemberModalCtrl = function($scope, $uibModalInstance, utilService, metadata,
                refset, project, value) {
                console.debug('Entered add member modal control');
                $scope.value = value;
                $scope.activeOnly = true;
                $scope.pageSize = 10;
                $scope.searchResults = null;
                $scope.data = {
                  concept : null,
                  descriptionTypes : metadata.descriptionTypes,
                  terminology : refset.terminology,
                  version : refset.version,
                  refset : refset,
                  memberTypes : {}
                };
                $scope.pageSize = 10;
                $scope.pageSizes = utilService.getPageSizes();
                $scope.paging = {};
                $scope.paging['search'] = {
                  page : 1,
                  filter : '',
                  sortField : null,
                  ascending : null
                };
                $scope.errors = [];

                if (refset.type == 'EXTENSIONAL') {
                  $scope.memberType = 'MEMBER';
                }
                if (refset.type == 'INTENSIONAL') {
                  $scope.memberType = 'INCLUSION';
                }

                $scope.addMember = function(concept) {

                  var member = {
                    active : true,
                    conceptId : concept.terminologyId,
                    conceptName : concept.name,
                    conceptActive : concept.active,
                    memberType : $scope.memberType,
                    moduleId : refset.moduleId,
                  };
                  member.refsetId = refset.id;

                  // validate member before adding it
                  validationService.validateMember(member, project.id).then(
                    function(data) {

                      // If there are errors, make them available and stop.
                      if (data.errors && data.errors.length > 0) {
                        $scope.errors = data.errors;
                        return;
                      } else {
                        $scope.errors = [];
                      }

                      // if data.warnings is set and doesn't match
                      // $scope.warnings
                      if (data.warnings && data.warnings.length > 0
                        && $scope.warnings.join() !== data.warnings.join()) {
                        $scope.warnings = data.warnings;
                        return;
                      } else {
                        $scope.warnings = [];
                      }

                      // Success - validate refset
                      if (member.memberType == 'MEMBER') {

                        refsetService.addRefsetMember(member).then(
                        // Success
                        function(data) {
                          member.id = data.id;
                          $scope.data.memberTypes[concept.terminologyId] = member;
                        },
                        // Error
                        function(data) {
                          handleError($scope.errors, data);
                        });
                      }

                      if (member.memberType == 'INCLUSION') {
                        refsetService.addRefsetInclusion(member, false).then(
                        // Success
                        function(data) {
                          member.id = data.id;
                          $scope.data.memberTypes[concept.terminologyId] = member;
                        },
                        // Error
                        function(data) {
                          handleError($scope.errors, data);
                        });

                      }
                    },
                    // Error - validate refset
                    function(data) {
                      handleError($scope.errors, data);
                    });

                };

                // get search results
                $scope.getSearchResults = function(search, clearPaging) {

                  if (clearPaging) {
                    $scope.paging['search'].page = 1;
                  }

                  // skip search if blank
                  if (!search) {
                    return;
                  }
                  // clear data structures
                  $scope.errors = [];

                  var pfs = {
                    startIndex : ($scope.paging['search'].page - 1) * $scope.pageSize,
                    maxResults : $scope.pageSize,
                    sortField : null,
                    ascending : null,
                    queryRestriction : null
                  };
                  if ($scope.activeOnly) {
                    console.debug('active only');
                    pfs.activeOnly = true;
                  }
                  projectService.findConceptsForQuery(project, search, refset.terminology,
                    refset.version, pfs).then(
                  // Success
                  function(data) {
                    $scope.searchResults = data.concepts;
                    // if using the SnowOwl terminology handler, we no longer have the offset parameter
                    // so we are faking the paging here with a max of 100 results returned for each request
                    if ($scope.searchResults.length > $scope.pageSize) {
                    	startIndex = ($scope.paging['search'].page - 1) * $scope.pageSize;
                    	$scope.searchResults = data.concepts.slice(startIndex, startIndex + $scope.pageSize);
                    }
                    $scope.searchResults.totalCount = data.totalCount;
                    $scope.getMemberTypes();
                    if (data.concepts.length > 0) {
                      $scope.selectConcept(data.concepts[0]);
                    }
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                // select concept and get concept data
                $scope.selectConcept = function(concept) {
                  $scope.data.concept = concept;
                };

                // Gets $scope.data.memberTypes
                $scope.getMemberTypes = function() {
                  var concepts = new Array();
                  for (var i = 0; i < $scope.searchResults.length; i++) {
                    concepts.push($scope.searchResults[i].terminologyId);
                  }
                  var query = concepts[0];
                  for (var i = 1; i < concepts.length; i++) {
                    if (!$scope.data.memberTypes[concepts[i]]) {
                      query += ' OR ';
                      query += concepts[i];
                      // put a placeholder entry for the cases when it isn't a
                      // member of the refset
                      $scope.data.memberTypes[concepts[i]] = {
                        conceptId : concepts[i]
                      };
                    }
                  }
                  var pfs = {
                    startIndex : -1
                  };
                  query = '(' + query + ')';
                  refsetService.findRefsetMembersForQuery($scope.data.refset.id, query, pfs).then(
                  // Success
                  function(data) {
                    for (var i = 0; i < data.members.length; i++) {
                      $scope.data.memberTypes[data.members[i].conceptId] = data.members[i];
                    }
                  });
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.close(refset);
                };
                
                $scope.$on('refset:disableEditing', function(event, data) {
                  console.debug('on refset:disableEditing', data);
                  $uibModalInstance.close(data);
                });                

              };

              // Migration modal
              $scope.openMigrationModal = function(lrefset) {
                console.debug('openMigrationModal ', lrefset);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/migration.html',
                  controller : MigrationModalCtrl,
                  backdrop : 'static',
                  size : 'lg',
                  resolve : {

                    project : function() {
                      return $scope.project;
                    },
                    refset : function() {
                      return lrefset;
                    },
                    paging : function() {
                      return $scope.paging;
                    },
                    metadata : function() {
                      return $scope.metadata;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  // handle workflow advancement
                  $scope.handleWorkflow(data);
                });
              };

              // Migration modal controller
              var MigrationModalCtrl = function($scope, $uibModalInstance, $interval, gpService,
                utilService, project, refset, paging, metadata) {
                console.debug('Entered migration modal control');

                // set up variables
                $scope.project = project;
                $scope.refset = refset;
                $scope.newTerminology = refset.terminology;
                $scope.membersInCommonStatusType = 'all';
                $scope.validInclusionsStatusType = 'all';
                $scope.stagedInclusionsStatusType = 'all';
                $scope.oldRegularMembersStatusType = 'all';
                $scope.membersInCommon = null;
                $scope.pageSize = 5;
                $scope.pageSizes = utilService.getPageSizes();
                $scope.paging = paging;
                $scope.metadata = metadata;
                $scope.terminologies = [];
                /*
                                 * $scope.versions =
                                 * angular.copy($scope.metadata.versions[$scope.newTerminology]
                                 * .sort().reverse());
                                 */
                $scope.newVersion = null;
                $scope.validVersion = null;
                $scope.errors = [];
                $scope.statusTypes = [{"state":"all","name":"All"},
                                      {"state":"active","name":"Active"},
                                      {"state":"inactive","name":"Inactive"}]
                $scope.pagedStagedInclusions = [];
                $scope.paging['newRegularMembers'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.paging['oldRegularMembers'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.paging['membersInCommon'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.paging['stagedInclusions'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.pagedValidInclusions = [];
                $scope.paging['validInclusions'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.pagedInvalidInclusions = [];
                $scope.paging['invalidInclusions'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.pagedStagedExclusions = [];
                $scope.paging['stagedExclusions'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.pagedValidExclusions = [];
                $scope.paging['validExclusions'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.pagedInvalidExclusions = [];
                $scope.paging['invalidExclusions'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.lookupInterval = null;

                // Initialize
                if ($scope.refset.stagingType == 'MIGRATION') {
                  refsetService.resumeMigration($scope.refset.id).then(
                  // Success
                  function(data) {
                    $scope.stagedRefset = data;
                    $scope.newTerminology = $scope.stagedRefset.terminology;
                    $scope.newVersion = $scope.stagedRefset.version;
                    refsetService.compareRefsets($scope.refset.id, $scope.stagedRefset.id).then(
                    // Success
                    function(data) {
                      $scope.reportToken = data;
                      $scope.getDiffReport();
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                }

                // Return the name for a terminology
                $scope.getTerminologyName = function(terminology) {
                  if ($scope.metadata && $scope.metadata.terminologyNames) {
                    return $scope.metadata.terminologyNames[terminology];
                  } else {
                    return terminology;
                  }
                };

                // Handle terminology selected
                $scope.terminologySelected = function(terminology) {
                  $scope.versions = refsetService.filterTerminologyVersions(
                    $scope.project.terminologyHandlerKey, terminology, $scope.metadata.versions);
                  if (terminology === 'SNOMEDCT') {
                    $scope.newVersion = "MAIN";
                  } else {
                    $scope.newVersion = "MAIN/" + terminology;
                  }
                };                

                // Table sorting mechanism
                $scope.setSortField = function(table, field, object) {
                  utilService.setSortField(table, field, $scope.paging);
                  // retrieve the correct table
                  if (table == 'membersInCommon') {
                    $scope.findMembersInCommon();
                  } else if (table == 'oldRegularMembers') {
                    $scope.getOldRegularMembers();
                  } else if (table == 'newRegularMembers') {
                    $scope.getNewRegularMembers();
                  } else if (table == 'stagedInclusions') {
                    $scope.getPagedStagedInclusions();
                  } else if (table == 'validInclusions') {
                    $scope.getPagedValidInclusions();
                  } else if (table == 'invalidInclusions') {
                    $scope.getPagedInvalidInclusions();
                  } else if (table == 'stagedExclusions') {
                    $scope.getPagedStagedExclusions();
                  } else if (table == 'validExclusions') {
                    $scope.getPagedValidExclusions();
                  } else if (table == 'invalidExclusions') {
                    $scope.getPagedInvalidExclusions();
                  }
                };

                // Return up or down sort chars if sorted
                $scope.getSortIndicator = function(table, field) {
                  return utilService.getSortIndicator(table, field, $scope.paging);
                };

                $scope.exportDiffReport = function(action) {
                  refsetService.exportDiffReport(action, $scope.reportToken, $scope.refset, $scope.newTerminology, $scope.newVersion);
                  // update migration files list
                  $scope.migrationFiles = new Array();
                  refsetService.getMigrationFileNames($scope.project.id, $scope.refset.terminologyId).then(
                    function(data) {
                      if (data != '') {                     
                      var fileNames = data.split("\|");
                      for (var i = 0, l = fileNames.length; i < l; i++) {
                        $scope.migrationFiles.push(fileNames[i]);
                        // Have the files sorted in reverse order, so the most recent is on top.
                        $scope.migrationFiles.sort();
                        $scope.migrationFiles.reverse();
                      }
                      }
                    });
                }

                $scope.testTerminologyVersion = function() {
                  refsetService.isTerminologyVersionValid($scope.project.id, $scope.newTerminology,
                    $scope.newVersion).then(function(data) {
                    $scope.validVersion = data;
                  });
                }

                $scope.resetValidVersion = function() {
                  $scope.validVersion = null;
                }

                $scope.versionNotInPicklist = function() {
                  for (var i = 0; i < $scope.versions.length; i++) {
                    if ($scope.versions[i] == $scope.newVersion) {
                      $scope.validVersion = 'true';
                      return false;
                    }
                  }
                  return true;
                }

                // get diff report
                $scope.getDiffReport = function() {
                  refsetService.getDiffReport($scope.reportToken).then(
                  // Success
                  function(data) {
                    $scope.diffReport = data;
                    $scope.validInclusions = data.validInclusions;
                    $scope.validExclusions = data.validExclusions;
                    $scope.invalidInclusions = data.invalidInclusions;
                    $scope.invalidExclusions = data.invalidExclusions;
                    $scope.stagedInclusions = data.stagedInclusions;
                    $scope.stagedExclusions = data.stagedExclusions;
                    $scope.findMembersInCommon();
                    $scope.getNewRegularMembers();
                    $scope.getOldRegularMembers();
                    $scope.getPagedStagedInclusions();
                    $scope.getPagedValidInclusions();
                    $scope.getPagedInvalidInclusions();
                    $scope.getPagedStagedExclusions();
                    $scope.getPagedValidExclusions();
                    $scope.getPagedInvalidExclusions();
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Load 'old regular members' with paging
                $scope.getOldRegularMembers = function() {
                  var pfs = {
                    startIndex : ($scope.paging['oldRegularMembers'].page - 1) * $scope.pageSize,
                    maxResults : $scope.pageSize,
                    sortField : $scope.paging['oldRegularMembers'].sortField,
                    ascending : $scope.paging['oldRegularMembers'].ascending,
                    queryRestriction : $scope.paging['oldRegularMembers'].filter != undefined ? $scope.paging['oldRegularMembers'].filter
                      : null
                  };

                  var conceptActive;
                  if ($scope.oldRegularMembersStatusType == 'active') {
                	$scope.paging['oldRegularMembers'].typeFilter = 'Active';
                    conceptActive = true;
                  } else if ($scope.oldRegularMembersStatusType == 'inactive') {
                	$scope.paging['oldRegularMembers'].typeFilter = 'Inactive';
                    conceptActive = false;
                  } else {
                	$scope.paging['oldRegularMembers'].typeFilter = ''
                    conceptActive = null;
                  }

                  refsetService.getOldRegularMembers($scope.reportToken, null, pfs, conceptActive)
                    .then(
                    // Success
                    function(data) {
                      $scope.oldRegularMembers = data.members;
                      $scope.oldRegularMembers.totalCount = data.totalCount;
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                };

                // Load 'new regular members' with paging
                $scope.getNewRegularMembers = function() {
                  var pfs = {
                    startIndex : ($scope.paging['newRegularMembers'].page - 1) * $scope.pageSize,
                    maxResults : $scope.pageSize,
                    sortField : $scope.paging['newRegularMembers'].sortField,
                    ascending : $scope.paging['newRegularMembers'].ascending,
                    queryRestriction : $scope.paging['newRegularMembers'].filter != undefined ? $scope.paging['newRegularMembers'].filter
                      : null
                  };
                  refsetService.getNewRegularMembers($scope.reportToken, null, pfs, null).then(
                  // Success
                  function(data) {
                    $scope.newRegularMembers = data.members;
                    $scope.newRegularMembers.totalCount = data.totalCount;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Load 'members in common' with paging
                $scope.findMembersInCommon = function() {
                  var pfs = {
                    startIndex : ($scope.paging['membersInCommon'].page - 1) * $scope.pageSize,
                    maxResults : $scope.pageSize + 2,
                    sortField : $scope.paging['membersInCommon'].sortField,
                    ascending : $scope.paging['membersInCommon'].ascending,
                    queryRestriction : $scope.paging['membersInCommon'].filter != undefined ? $scope.paging['membersInCommon'].filter
                      : null
                  };

                  var conceptActive;
                  if ($scope.membersInCommonStatusType == 'active') {
                	$scope.paging['membersInCommon'].typeFilter = 'Active';
                    conceptActive = true;
                  } else if ($scope.membersInCommonStatusType == 'inactive') {
                	$scope.paging['membersInCommon'].typeFilter = 'Inactive';
                    conceptActive = false;
                  } else {
                  	$scope.paging['membersInCommon'].typeFilter = '';
                    conceptActive = null;
                  }

                  refsetService.findMembersInCommon($scope.reportToken, null, pfs, conceptActive)
                    .then(
                    // Succcess
                    function(data) {
                      $scope.membersInCommon = data.members;
                      $scope.membersInCommon.totalCount = data.totalCount;
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                };

                // Get paged staged inclusions (assume all are loaded)
                $scope.getPagedStagedInclusions = function() {
                	if ($scope.stagedInclusionsStatusType == 'active') {
                        $scope.paging['stagedInclusions'].typeFilter = 'Active';
                      } else if ($scope.stagedInclusionsStatusType == 'inactive') {
                        $scope.paging['stagedInclusions'].typeFilter = 'Inactive';
                      } else {
                    	$scope.paging['stagedInclusions'].typeFilter = ''  
                      }
                  $scope.pagedStagedInclusions = utilService.getPagedArray($scope.stagedInclusions,
                    $scope.paging['stagedInclusions'], $scope.pageSize);
                };

                // Get paged valid inclusions (assume all are loaded)
                $scope.getPagedValidInclusions = function() {
                  if ($scope.validInclusionsStatusType == 'active') {
                    $scope.paging['validInclusions'].typeFilter = 'Active';
                  } else if ($scope.validInclusionsStatusType == 'inactive') {
                    $scope.paging['validInclusions'].typeFilter = 'Inactive';
                  } else {
                	$scope.paging['validInclusions'].typeFilter = '';  
                  }
                  $scope.pagedValidInclusions = utilService.getPagedArray($scope.validInclusions,
                    $scope.paging['validInclusions'], $scope.pageSize);
                };

                // Get paged invalid inclusions (assume all are loaded)
                $scope.getPagedInvalidInclusions = function() {
                  $scope.pagedInvalidInclusions = utilService.getPagedArray(
                    $scope.invalidInclusions, $scope.paging['invalidInclusions'], $scope.pageSize);
                };

                // Get paged staged exclusions (assume all are loaded)
                $scope.getPagedStagedExclusions = function() {
                  $scope.pagedStagedExclusions = utilService.getPagedArray($scope.stagedExclusions,
                    $scope.paging['stagedExclusions'], $scope.pageSize);
                };

                // Get paged valid exclusions (assume all are loaded)
                $scope.getPagedValidExclusions = function() {
                  $scope.pagedValidExclusions = utilService.getPagedArray($scope.validExclusions,
                    $scope.paging['validExclusions'], $scope.pageSize);
                };

                // Get paged invalid exclusions (assume all are loaded)
                $scope.getPagedInvalidExclusions = function() {
                  $scope.pagedInvalidExclusions = utilService.getPagedArray(
                    $scope.invalidExclusions, $scope.paging['invalidExclusions'], $scope.pageSize);
                };

                // Initialize
                $scope.terminologySelected($scope.refset.terminology);
                
                // Close migration dialog
                $scope.close = function(refset) {
                  $uibModalInstance.close(refset);
                };

                // Cancel migration and close dialog
                $scope.cancel = function(refset) {
                  refsetService.cancelMigration(refset.id).then(
                  // Success
                  function(data) {
                    // mark as cancelled
                    refset.staged = false;
                    refset.stagingType = null;
                    $scope.stagedRefset = null;
                    // If INTENSIONAL, we need to re-look up old/not/new members
                    if (refset.type == 'INTENSIONAL') {
                      startLookup(refset);
                    }
                    $uibModalInstance.close(refset);
                  },
                  // Error - cancel migration
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                // Refresh lookup progress until ready
                // then compare refsets
                $scope.refreshLookupProgress = function(refset) {
                  refsetService.getLookupProgress(refset.id).then(
                  // Success
                  function(data) {
                    $scope.lookupProgress = data;
                    if (data >= 100) {
                      gpService.decrement();
                      $scope.stagedRefset.lookupInProgress = false;
                      $scope.refset.lookupInProgress = false;

                      // Cancel interval
                      $interval.cancel($scope.lookupInterval);
                      $scope.lookupInterval = null;

                      // Compare refsets original and staged, regardless of the
                      // parameter
                      refsetService.compareRefsets($scope.refset.id, $scope.stagedRefset.id).then(
                      // Success
                      function(data) {
                        $scope.reportToken = data;
                        $scope.getDiffReport();
                        $scope.exportDiffReport('Migrate');
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    }
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Begin migration and compare refsets and get diff report
                $scope.beginMigration = function(newTerminology, newVersion) {
                  $scope.errors = [];
                 
                  // Full version comparison no longer works for MANAGED-SERVICE projects, since it can include the edition's working project
                  // e.g. MAIN/2019-07-31/SNOMEDCT-BE/BEMAR20
                  if ($scope.project.terminologyHandlerKey === 'PUBLIC-BROWSER' && newTerminology == $scope.refset.terminology
                    && newVersion < $scope.refset.version) {
                    $scope.errors[0] = 'New version must be greater than existing version';
                    return;
                  }
                    
                  if (!newVersion) {
                    $scope.errors[0] = 'New version must not be blank';
                    return;
                  }

                  refsetService.beginMigration(refset.id, newTerminology, newVersion).then(
                  // Success
                  function(data) {
                    $scope.stagedRefset = data;
                    // manage local state witout re-reading refset
                    $scope.refset.stagingType = 'MIGRATION';
                    $scope.refset.staged = true;

                    // What to do next depends on type
                    var lookupRefset = null;
                    $scope.lookupProgress = 1;
                    if (refset.type == 'EXTENSIONAL') {
                      lookupRefset = $scope.stagedRefset;
                    } else {
                      lookupRefset = refset;
                    }

                    gpService.increment();
                    lookupRefset.lookupInProgress = true;
                    $scope.lookupInterval = $interval(function() {
                      $scope.refreshLookupProgress(data);
                    }, 2000);
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Finish migration
                $scope.finish = function(refset) {

                  refsetService.finishMigration(refset.id).then(
                  // Success
                  function(data) {
                    startLookup(refset);
                    $uibModalInstance.close(refset);
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                $scope.removeMember = function(member) {

                  refsetService.removeRefsetMember(member.id).then(
                  // Success
                  function(data) {
                    refsetService.compareRefsets($scope.refset.id, $scope.stagedRefset.id).then(
                    // Success
                    function(data) {
                      $scope.reportToken = data;
                      $scope.getDiffReport();
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                // Save for later, allow state to be resumed
                $scope.saveForLater = function(refset) {
                  // added solely for delay to migration files update
                  startLookup(refset);
                  // updates refset on close
                  $uibModalInstance.close(refset);
                };

                // add exclusion
                $scope.exclude = function(refset, member, staged) {
                  refsetService.addRefsetExclusion($scope.stagedRefset, member.conceptId, staged)
                    .then(
                      // Success
                      function() {
                        refsetService.releaseReportToken($scope.reportToken).then(
                          // Success
                          function() {
                            refsetService.compareRefsets($scope.refset.id, $scope.stagedRefset.id)
                              .then(
                              // Success
                              function(data) {
                                $scope.reportToken = data;
                                $scope.getDiffReport();
                              },
                              // Error
                              function(data) {
                                handleError($scope.errors, data);
                              });
                          },
                          // Error
                          function(data) {
                            handleError($scope.errors, data);
                          });
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                };

                // add inclusion
                $scope.include = function(member, staged, lookup) {
                  // if inactive, find if there are replacement concepts
                  if (lookup) {
                    projectService.getReplacementConcepts($scope.project.id, member.conceptId,
                      $scope.refset.terminology, $scope.newVersion).then(
                      // Success
                      function(data) {
                        $scope.concepts = data.concepts;

                        // if no replacements, just add the inclusion
                        /*
                                                 * if ($scope.concepts.length == 0 // the second
                                                 * clause here is because intensional // refsets
                                                 * never have inactive members in common &&
                                                 * $scope.stagedRefset.type == 'INTENSIONAL') {
                                                 * $scope.addRefsetInclusion($scope.stagedRefset,
                                                 * member, staged);
                                                 */
                        if ($scope.concepts.length == 0) {
                        	window.alert('There are no replacement concepts available.');
                        } else {
                          $scope.openReplacementConceptsModal(member, staged, $scope.concepts,
                            $scope.reportToken);
                        }
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                  } else {
                    // Add an inclusion or a member
                    $scope.addRefsetMember($scope.stagedRefset, member, staged,
                      $scope.stagedRefset.type == 'INTENSIONAL' ? 'Inclusion' : 'Member');
                  }
                };

                $scope.addRefsetMember = function(refset, member, staged, fn) {
                  member.refsetId = refset.id;
                  member.id = null;
                  // add member or add inclusion - recalculate everything
                  refsetService['addRefset' + fn](member, staged).then(
                  // Success
                  function(data) {
                    refsetService.releaseReportToken($scope.reportToken).then(
                    // Success
                    function(data) {
                      refsetService.compareRefsets($scope.refset.id, $scope.stagedRefset.id).then(
                      // Success
                      function(data) {
                        $scope.reportToken = data;
                        $scope.getDiffReport();
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // add all inclusions
                $scope.includeAll = function(refset, list, staged, active) {
                  for (var i = 0; i < list.length; i++) {
                    $scope.include(refset, list[i]);
                  }
                };

                // exclude all
                $scope.excludeAll = function(refset, list, staged, active) {
                  for (var i = 0; i < list.length; i++) {
                    $scope.exclude(refset, list[i]);
                  }
                };

                // revert all inclusions or exclusions
                $scope.revertAll = function(refset, list) {
                  for (var i = 0; i < list.length; i++) {
                    $scope.revert(refset, list[i]);
                  }
                };

                // revert inclusions and exclusions
                $scope.revert = function(refset, member) {
                  if (member.memberType == 'INCLUSION' || member.memberType == 'INCLUSION_STAGED') {
                    refsetService.removeRefsetMember(member.id).then(
                      // Success - remove refse member
                      function() {
                        refsetService.releaseReportToken($scope.reportToken).then(
                          // Success - release report token
                          function() {
                            refsetService.compareRefsets($scope.refset.id, $scope.stagedRefset.id)
                              .then(
                              // Success - compare refsets
                              function(data) {
                                $scope.reportToken = data;
                                $scope.getDiffReport();
                              },
                              // Error - compare refsets
                              function(data) {
                                handleError($scope.errors, data);
                              });
                          },
                          // Error - release report token
                          function(data) {
                            handleError($scope.errors, data);
                          });
                      },
                      // Error - remove refset member
                      function(data) {
                        handleError($scope.errors, data);
                      });
                  } else if (member.memberType == 'EXCLUSION'
                    || member.memberType == 'EXCLUSION_STAGED') {
                    refsetService.removeRefsetExclusion(member.id).then(
                      // Success
                      function() {
                        refsetService.releaseReportToken($scope.reportToken).then(
                          // Success - release report token
                          function() {
                            refsetService.compareRefsets($scope.refset.id, $scope.stagedRefset.id)
                              .then(
                              // Success - compare refsets
                              function(data) {
                                $scope.reportToken = data;
                                $scope.getDiffReport();
                              },
                              // Error - compare refsets
                              function(data) {
                                handleError($scope.errors, data);
                              });
                          },
                          // Error - release report token
                          function(data) {
                            handleError($scope.errors, data);
                          });
                      },
                      // Error - remove refset exclusion
                      function(data) {
                        handleError($scope.errors, data);
                      });
                  }
                };

                // Used for styling - coordinated with css file
                $scope.getMemberStyle = function(member) {
                  if (member.memberType == 'MEMBER') {
                    return '';
                  }
                  return member.memberType.replace('_STAGED', '');
                };
                // Close modal
                $scope.close = function() {
                  $uibModalInstance.close();
                };

                // Add modal
                $scope.openReplacementConceptsModal = function(lmember, lstaged, lconcepts,
                  lreportToken) {
                  console.debug('openReplacementConceptsModal ', lmember, lstaged, lconcepts,
                    lreportToken);

                  var modalInstance = $uibModal.open({
                    templateUrl : 'app/component/refsetTable/replacements.html',
                    controller : ReplacementConceptsModalCtrl,
                    backdrop : 'static',
                    resolve : {
                      refset : function() {
                        return $scope.refset;
                      },
                      stagedRefset : function() {
                        return $scope.stagedRefset;
                      },
                      member : function() {
                        return lmember;
                      },
                      staged : function() {
                        return lstaged;
                      },
                      concepts : function() {
                        return lconcepts;
                      },
                      reportToken : function() {
                        return lreportToken;
                      }
                    }
                  });

                  modalInstance.result.then(
                  // Success
                  function(data) {

                    refsetService.releaseReportToken($scope.reportToken).then(
                    // Success
                    function() {
                      refsetService.compareRefsets($scope.refset.id, $scope.stagedRefset.id).then(
                      // Success
                      function(data) {
                        $scope.reportToken = data;
                        $scope.getDiffReport();
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });

                  });

                };

                // Add modal controller
                var ReplacementConceptsModalCtrl = function($scope, $uibModalInstance, refset,
                  stagedRefset, member, staged, concepts, reportToken) {
                  console.debug('Entered replacement concepts modal control', refset, stagedRefset,
                    member, staged, concepts, reportToken);

                  $scope.errors = [];
                  $scope.refset = refset;
                  $scope.member = member;
                  $scope.staged = staged;
                  $scope.concepts = concepts;
                  $scope.reportToken = reportToken;
                  $scope.selection = {
                    ids : {
                      "test" : true
                    }
                  };
                  // $scope.invalidIds = new Array();
                  $scope.invalid = {
                    ids : {
                      "test" : true
                    }
                  };
                  $scope.expectedCt = 0;

                  // initialize
                  var pfs = {
                    startIndex : 0,
                    maxResults : 1,
                    sortField : null,
                    ascending : null,
                    queryRestriction : null
                  };

                  // check if replacements are already members
                  for (var i = 0; i < $scope.concepts.length; i++) {
                    var query = 'conceptId:' + $scope.concepts[i].terminologyId;
                    refsetService.findRefsetMembersForQuery(stagedRefset.id, query, pfs).then(
                    // Success
                    function(data) {
                      if (data.members.length != 0) {
                        $scope.invalid.ids[data.members[0].conceptId] = true;
                      }
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                    refsetService.findMembersInCommon($scope.reportToken, query, pfs, null).then(
                    // Success
                    function(data) {
                      if (data.members.length != 0) {
                        $scope.invalid.ids[data.members[0].terminologyId] = true;
                      }
                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });

                  }
                  refsetService
                    .getDiffReport($scope.reportToken)
                    .then(
                      // Success
                      function(data) {
                        $scope.stagedInclusions = data.stagedInclusions;
                        for (var i = 0; i < $scope.concepts.length; i++) {
                          for (var j = 0; j < $scope.stagedInclusions.length; j++) {
                            if ($scope.stagedInclusions[j].conceptId == $scope.concepts[i].terminologyId) {
                              $scope.invalid.ids[$scope.stagedInclusions[j].conceptId] = true;
                            }
                          }
                        }
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });

                  $scope.getLength = function(obj) {
                	    return Object.keys(obj).length;
                	}
                  
                  // Add button
                  $scope.submitAdd = function() {
                    // calculate total number of replacement options
                    for (var i = 0; i < concepts.length; i++) {
                      if ($scope.selection.ids[concepts[i].terminologyId]) {
                        $scope.expectedCt++;
                      }
                    }
                    // if intensional, check if inactive concept itself should
                    // be
                    // included
                    if (refset.type == 'INTENSIONAL') {
                      if ($scope.selection.ids[$scope.member.conceptId]) {
                        $scope.expectedCt++;
                        $scope.addRefsetInclusionOrMember(stagedRefset, $scope.member,
                          $scope.staged);
                      }
                    }
                    // if a concept is selected, add it as an inclusion or
                    // member
                    for (var i = 0; i < concepts.length; i++) {
                      if ($scope.selection.ids[concepts[i].terminologyId]) {
                        var member = {
                          active : true,
                          conceptId : concepts[i].terminologyId,
                          conceptName : concepts[i].name,
                          conceptActive : concepts[i].active,
                          memberType : (refset.type == 'INTENSIONAL' ? 'INCLUSION' : 'MEMBER'),
                          moduleId : stagedRefset.moduleId,
                          refsetId : stagedRefset.id
                        };
                        $scope.addRefsetInclusionOrMember(stagedRefset, member, $scope.staged);
                      }
                    }
                  };

                  $scope.addRefsetInclusionOrMember = function(refset, member, staged) {
                    member.refsetId = refset.id;
                    if (refset.type == 'INTENSIONAL') {
                      refsetService.addRefsetInclusion(member, staged).then(
                      // Success
                      function(data) {
                        $scope.expectedCt--;
                        if ($scope.expectedCt == 0) {
                          $uibModalInstance.close();
                        }
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    } else if (refset.type == 'EXTENSIONAL') {
                      refsetService.addRefsetMember(member).then(
                      // Success
                      function(data) {
                        $scope.expectedCt--;
                        if ($scope.expectedCt == 0) {
                          $uibModalInstance.close();
                        }
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    }
                  };

                  // Dismiss modal
                  $scope.cancel = function() {
                    $uibModalInstance.dismiss('cancel');
                  };

                };

              };

              // Feedback modal
              $scope.openFeedbackModal = function(lrefset) {
                console.debug('Open feedbackModal ', lrefset);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/refsetTable/feedback.html',
                  controller : FeedbackModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    refset : function() {
                      return lrefset;
                    },
                    metadata : function() {
                      return $scope.metadata;
                    },
                    tinymceOptions : function() {
                      return utilService.tinymceOptions;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  refsetService.fireRefsetChanged(data);
                });

              };

              // Feedback controller
              var FeedbackModalCtrl = function($scope, $uibModalInstance, refset, metadata,
                tinymceOptions) {
                console.debug('Entered feedback modal control', refset);

                $scope.metadata = metadata;
                $scope.refset = JSON.parse(JSON.stringify(refset));
                $scope.tinymceOptions = tinymceOptions;
                $scope.errors = [];

                // Add feedback
                $scope.addFeedback = function(refset, name, email, message) {

                  if (!message) {
                    window.alert('The message cannot be empty');
                    return;
                  }
                  if (!name) {
                    window.alert('Name cannot be empty');
                    return;
                  }
                  if (!validateEmail(email)) {
                    window
                      .alert('Invalid email address provided (e.g. should be like someone@example.com)');
                    return;
                  }
                  workflowService.addFeedback(refset, name, email, message).then(
                  // Success
                  function(data) {
                    $uibModalInstance.dismiss('cancel');
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

                // email validation via regex
                function validateEmail(email) {
                  var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
                  return re.test(email);
                }

              };
              
              
              // INITIALIZE

              // Initialize if project setting isn't used
              if ($scope.value == 'BETA' || $scope.value == 'PUBLISHED') {
                $scope.getRefsets();
              }

              $scope.getFilters();

              
              
              // end

            } ]
        };
      } ]);
