// Refset controller
tsApp.controller('RefsetCtrl', [
  '$scope',
  '$http',
  'tabService',
  'projectService',
  'securityService',
  '$rootScope',
  function($scope, $http, tabService, projectService, securityService,
    $rootScope) {
    console.debug('configure RefsetCtrl');

    // Handle resetting tabs on "back" button
    if (tabService.selectedTab.label != 'Refset') {
      tabService.setSelectedTabByLabel('Refset');
    }

    // Initialize
    projectService.prepareIconConfig();

    $scope.user = securityService.getUser();
    $scope.role = null;
    $scope.selectedProject = null;

    // get all projects where user has a role
    $scope.retrieveProjects = function() {

      var pfs = {
        startIndex : 0,
        maxResults : 100,
        sortField : 'name',
        queryRestriction : 'userAnyRole:' + $scope.user.userName
      };
      // clear queryRestriction for application admins
      if ($scope.user.applicationRole == 'ADMIN') {
        pfs.queryRestriction = null;
      }

      projectService.findProjectsAsList("", pfs).then(function(data) {
        $scope.projects = data.projects;
        $scope.projects.totalCount = data.totalCount;
        $scope.selectedProject = $scope.projects[0];
        $scope.setSelectedProject();
        $scope.findAssignedUsersForProject();

      })

    };

    // get assigned users - this is the list of users that are
    // already assigned to the selected project
    $scope.findAssignedUsersForProject = function() {

      var pfs = {
        startIndex : 0,
        maxResults : 100,
        sortField : null,
        queryRestriction : null
      };

      projectService.findAssignedUsersForProject($scope.selectedProject.id, "",
        pfs).then(function(data) {
        $scope.assignedUsers = data.users;
        for (var i = 0; i < $scope.assignedUsers.length; i++) {
          if ($scope.assignedUsers[i].userName == $scope.user.userName) {
            $scope.role = $scope.assignedUsers[i].projectRoleMap[selectedProject.id];
            break;
          }
        }
      })

    };

    // Fire a "projectChanged" event
    $scope.setSelectedProject = function() {
      console.log("rootScope.broadcast", $scope.selectedProject);
      $rootScope.$broadcast('refset:project', $scope.selectedProject);

    }

    // Determine whether the user is a project admin
    $scope.isProjectAdmin = function() {
      return $scope.role == 'ADMIN';
    }
    
    
    $scope.retrieveProjects();

  }

]);
