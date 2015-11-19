// Refset controller
tsApp.controller('RefsetCtrl', [
  '$scope',
  '$http',
  'tabService',
  'securityService',
  'projectService',
  'refsetService',
  '$rootScope',
  function($scope, $http, tabService, securityService, projectService, refsetService, $rootScope) {
    console.debug('configure RefsetCtrl');

    // Handle resetting tabs on "back" button
    if (tabService.selectedTab.label != 'Refset') {
      tabService.setSelectedTabByLabel('Refset');
    }

    // Initialize
    projectService.prepareIconConfig();
    $scope.user = securityService.getUser();
    $scope.projects = null;
    $scope.project = null;

    // Get $scope.projects
    $scope.getProjects = function() {

      // Get all projects for this user
      var pfs = {
        startIndex : -1,
        maxResults : 10,
        sortField : 'name',
        queryRestriction : 'userAnyRole:' + $scope.user.userName
      };
      projectService.findProjectsAsList("", pfs).then(function(data) {
        $scope.projects = data.projects;
        $scope.projects.totalCount = data.totalCount;
        $scope.project = $scope.projects[0];
        $scope.setProject();
      })

    };


    // Fire a "projectChanged" event after looking up role
    $scope.setProject = function() {
      
      // Empty PFS
      var pfs = {
      };
      // Find role
      projectService.findAssignedUsersForProject($scope.project.id, "", pfs).then(
        function(data) {
          $scope.assignedUsers = data.users;
          for (var i = 0; i < $scope.assignedUsers.length; i++) {
            if ($scope.assignedUsers[i].userName == $scope.user.userName) {
              $scope.user.role = $scope.assignedUsers[i].projectRoleMap[$scope.project.id];
              break;
            }
          }
          // ASSUMPTION: $scope.user.role is set
          refsetService.fireProjectChanged($scope.project);
        })
    }

    // Determine whether the user is a project admin
    $scope.isProjectAdmin = function() {
      return $scope.user.role == 'ADMIN';
    }

    $scope.getProjects();

  }

]);
