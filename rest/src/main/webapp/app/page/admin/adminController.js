// Administration controller
tsApp.controller('AdminCtrl', [
  '$scope',
  '$http',
  '$modal',
  '$location',
  '$anchorScroll',
  'gpService',
  'utilService',
  'tabService',
  'securityService',
  'translationService',
  'refsetService',
  'directoryService',
  'adminService',
  function($scope, $http, $modal, $location, $anchorScroll, gpService,
    utilService, tabService, securityService, translationService,
    refsetService, directoryService, adminService) {
    console.debug('configure AdminCtrl');

    // Handle resetting tabs on "back" button
    if (tabService.selectedTab.label != 'Admin') {
      tabService.setSelectedTabByLabel('Admin');
    }

    //
    // Scope Variables
    //

    // Scope variables initialized from services
    $scope.user = securityService.getUser();
    /*$scope.translation = translationService.getModel();
    $scope.component = directoryService.getModel();
    $scope.pageSizes = directoryService.getPageSizes();

    // Search parameters
    $scope.searchParams = directoryService.getSearchParams();
    $scope.searchResults = directoryService.getSearchResults();
*/

    
    // remove a project, a user
    $scope.remove = function(type, object, objArray) {
      if (!confirm("Are you sure you want to remove the " + type + " ("
        + object.name + ")?")) {
        return;
      }
      if (type == 'project') {
        adminService.removeProject(object).then(
          $scope.getProjects());
      }
      if (type == 'user') {
        adminService.removeUser(object).then(
          $scope.getUsers());
      }
    };
    
    // get projects
    $scope.getProjects = function() {
      adminService.getProjects().then(function(data) {
        for (var i = 0; i < data.projects.length; i++) {
          data.projects[i].isExpanded = false;
        }
        $scope.projects = data.projects;
      })
    };
       
    // get users
    $scope.getUsers = function() {
      adminService.getUsers().then(function(data) {
        $scope.users = data.users;
      })
    };

    // call these during initialization
    $scope.getProjects();
    $scope.getUsers();

    
    
    //
    // Modals
    //
    
    // modal for creating a new project
    $scope.openNewProjectModal = function(lproject) {

      console.debug("openNewProjectModal ");

      var modalInstance = $modal.open({
        templateUrl : 'app/page/admin/newProject.html',
        controller : NewProjectModalCtrl,
        resolve : {
          project : function() {
            return lproject;
          }
        }
      });
    };

    var NewProjectModalCtrl = function($scope, $modalInstance, project) {

      console.debug("Entered new project modal control");

      $scope.project = project;

      $scope.submitNewProject = function(project) {
        console.debug("Submitting new project", project);

        if (project == null || project.name == null
          || project.name == undefined || project.description == null
          || project.description == undefined) {
          window.alert("The name and description fields cannot be blank. ");
          return;
        }

        adminService.addProject(project).then(function(data) {
          // TODO: get this working $scope.projects.push(data);
          $modalInstance.close();
        }, function(data) {
          $modalInstance.close();
        })

      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };

    };

  
  // modal for creating a new user
  $scope.openNewUserModal = function(luser) {

    console.debug("openNewUserModal ");

    var modalInstance = $modal.open({
      templateUrl : 'app/page/admin/newUser.html',
      controller : NewUserModalCtrl,
      resolve : {
        user : function() {
          return luser;
        }
      }
    });
  };

  var NewUserModalCtrl = function($scope, $modalInstance, user) {

    console.debug("Entered new user modal control");

    $scope.user = user;

    $scope.submitNewUser = function(user) {
      console.debug("Submitting new user", user);

      if (user == null || user.name == null
        || user.name == undefined || user.userName == null
        || user.userName == undefined|| user.applicationRole == null
        || user.applicationRole == undefined) {
        window.alert("The name, user name, and application role fields cannot be blank. ");
        return;
      }

      adminService.addUser(user).then(function(data) {
        // TODO get this working $scope.users.push(data);
        $modalInstance.close();
      }, function(data) {
        $modalInstance.close();
      })

    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };

  };
  
  
  // modal for editing a project
  $scope.openEditProjectModal = function(lproject) {

    console.debug("openEditProjectModal ");

    var modalInstance = $modal.open({
      templateUrl : 'app/page/admin/editProject.html',
      controller : EditProjectModalCtrl,
      resolve : {
        project : function() {
          return lproject;
        }
      }
    });
  };

  var EditProjectModalCtrl = function($scope, $modalInstance, project) {

    console.debug("Entered edit project modal control");

    $scope.project = project;

    $scope.submitEditProject = function(project) {
      console.debug("Submitting edit project", project);

      if (project == null || project.name == null
        || project.name == undefined || project.description == null
        || project.description == undefined|| project.terminology == null
        || project.terminology == undefined) {
        window.alert("The name, description, and terminology fields cannot be blank. ");
        return;
      }

      adminService.updateProject(project).then(function(data) {
        // TODO get this working $scope.projects.push(data);
        $modalInstance.close();
      }, function(data) {
        $modalInstance.close();
      })

    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };

  };
  
  // modal for editing a user
  $scope.openEditUserModal = function(luser) {

    console.debug("openEditUserModal ");

    var modalInstance = $modal.open({
      templateUrl : 'app/page/admin/editUser.html',
      controller : EditUserModalCtrl,
      resolve : {
        user : function() {
          return luser;
        }
      }
    });
  };  
  
  var EditUserModalCtrl = function($scope, $modalInstance, user) {

    console.debug("Entered edit user modal control");

    $scope.user = user;

    $scope.submitEditUser = function(user) {
      console.debug("Submitting edit user", user);

      if (user == null || user.name == null
        || user.name == undefined || user.userName == null
        || user.userName == undefined|| user.applicationRole == null
        || user.applicationRole == undefined) {
        window.alert("The name, user name, and application role fields cannot be blank. ");
        return;
      }

      adminService.updateUser(user).then(function(data) {
        // TODO get this working $scope.users.push(data);
        $modalInstance.close();
      }, function(data) {
        $modalInstance.close();
      })

    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };

  };
}


]);
