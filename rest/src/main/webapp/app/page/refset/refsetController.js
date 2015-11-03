// Refset controller
tsApp.controller('RefsetCtrl', [ '$scope', '$http', 'tabService','projectService',
                                 'securityService',
  function($scope, $http, tabService, projectService, securityService) {
    console.debug('configure RefsetCtrl');

    // Handle resetting tabs on "back" button
    if (tabService.selectedTab.label != 'Refset') {
      tabService.setSelectedTabByLabel('Refset');
    }
    
    // Initialize
    projectService.prepareIconConfig();

    $scope.user = securityService.getUser();

    projectService.getUserHasAnyRole(); // TODO: is this needed?
    $scope.selectedProject = null;
    
    // get all projects where user has a role
    $scope.retrieveCandidateProjects = function() {

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
        $scope.candidateProjects = data.projects;
        $scope.candidateProjects.totalCount = data.totalCount;
        $scope.selectedProject = $scope.candidateProjects[0];
        $scope.findAssignedUsersForProject();

      })

    };
    
    
    // get assigned users - this is the list of users that are
    // already
    // assigned to the selected project
    $scope.findAssignedUsersForProject = function() {

      var pfs = {
        startIndex : 0,
        maxResults : 100,
        sortField : 'userName',
        queryRestriction : null
      };
      projectService.findAssignedUsersForProject($scope.selectedProject.id,
        "", pfs).then(function(data) {
        $scope.assignedUsers = data.users;
        for (var i = 0; i< $scope.assignedUsers.length; i++) {
          if ($scope.assignedUsers[i].userName == $scope.user.userName) {
            $scope.user = $scope.assignedUsers[i];
         
          }
        }
      })

    };
    
    $scope.getSelectedProject = function() {
      return selectedProject.id;
    }
    
    $scope.retrieveCandidateProjects();
    
  }

]);

