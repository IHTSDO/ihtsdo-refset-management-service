'use strict'

var tsApp = angular.module('tsApp',
  [ 'ngRoute', 'ui.bootstrap', 'ui.tree', 'ngFileUpload', 'ui.tinymce', 'ngCookies' ]).config(
  function($rootScopeProvider) {

    // Set recursive digest limit higher to handle very deep trees.
    $rootScopeProvider.digestTtl(17);
  });

// Declare top level URL vars
var securityUrl = 'security/';
var refsetUrl = 'refset/';
var translationUrl = 'translation/';
var releaseUrl = 'release/';
var projectUrl = 'project/';
var workflowUrl = 'workflow/';
var validationUrl = 'validate/';

// Initialization of tsApp
tsApp.run([ '$rootScope', '$http', '$window', function($rootScope, $http, $window) {
  // n/a
} ]);

// Route provider configuration
tsApp.config([ '$routeProvider', '$logProvider', function($routeProvider, $logProvider) {
  console.debug('configure $routeProvider');
  $logProvider.debugEnabled(true);

  // Set reloadOnSearch so that $location.hash() calls do not reload the
  // controller
  $routeProvider.when('/', {
    templateUrl : 'app/page/login/login.html',
    controller : 'LoginCtrl',
    reloadOnSearch : false
  }).when('/refset', {
    templateUrl : 'app/page/refset/refset.html',
    controller : 'RefsetCtrl',
    reloadOnSearch : false
  }).when('/translation', {
    templateUrl : 'app/page/translation/translation.html',
    controller : 'TranslationCtrl',
    reloadOnSearch : false
  }).when('/directory', {
    templateUrl : 'app/page/directory/directory.html',
    controller : 'DirectoryCtrl',
    reloadOnSearch : false
  }).when('/admin', {
    templateUrl : 'app/page/admin/admin.html',
    controller : 'AdminCtrl',
    reloadOnSearch : false
  }).when('/help/:type', {
    templateUrl : function(params) {
      return 'app/page/' + params.type + '/help/' + params.type + 'Help.html';
    }
  }).otherwise({
    redirectTo : '/'
  });

  // $locationProvider.html5Mode(true);

} ]);

// Simple glass pane controller
tsApp.controller('GlassPaneCtrl', [ '$scope', 'gpService', function($scope, gpService) {
  console.debug('configure GlassPaneCtrl');

  $scope.glassPane = gpService.glassPane;

} ]);

// Simple error controller
tsApp.controller('ErrorCtrl', [ '$scope', 'utilService', function($scope, utilService) {
  console.debug('configure ErrorCtrl');

  $scope.error = utilService.error;

  $scope.clearError = function() {
    utilService.clearError();
  }

  $scope.setError = function(message) {
    utilService.setError(message);
  }

} ]);

// Tab controller
tsApp.controller('TabCtrl', [ '$scope', '$interval', '$timeout', 'securityService', 'tabService',
  'projectService',
  function($scope, $interval, $timeout, securityService, tabService, projectService) {
    console.debug('configure TabCtrl');

    // Setup tabs
    $scope.tabs = tabService.tabs;

    // User projects info from the project service
    $scope.userProjectsInfo = projectService.getUserProjectsInfo();

    // Set selected tab (change the view)
    $scope.setSelectedTab = function(tab) {
      tabService.setSelectedTab(tab);
    }

    // sets the selected tab by label
    // to be called by controllers when their
    // respective tab is selected
    this.setSelectedTabByLabel = function(label) {
      for (var i = 0; i < this.tabs.length; i++) {
        if (this.tabs[i].label === label) {
          this.selectedTab = this.tabs[i];
          break;
        }
      }
    }

    // Set 'active' or not
    $scope.tabClass = function(tab) {
      if (tabService.selectedTab == tab) {
        return 'active';
      } else {
        return '';
      }
    }

    // for ng-show
    $scope.isShowing = function() {
      return securityService.isLoggedIn();
    }

    // for ng-show
    $scope.isAdmin = function() {
      return securityService.isAdmin();
    }

    // for ng-show
    $scope.isUser = function() {
      return securityService.isUser();
    }

  } ]);

// Header controller
tsApp.controller('HeaderCtrl', [ '$scope', '$location', '$http', 'securityService',
  function($scope, $location, $http, securityService) {
    console.debug('configure HeaderCtrl');

    // Declare user
    $scope.user = securityService.getUser();

    // Logout method
    $scope.logout = function() {
      securityService.logout();
    }

    // Open help page dynamically
    $scope.goToHelp = function() {
      var path = $location.path();
      path = '/help' + path + '?authToken=' + $http.defaults.headers.common.Authorization;
      var currentUrl = window.location.href;
      var baseUrl = currentUrl.substring(0, currentUrl.indexOf('#') + 1);
      var newUrl = baseUrl + path;
      var myWindow = window.open(newUrl, 'helpWindow');
      myWindow.focus();
    };

    // for ng-show
    $scope.isShowing = function() {
      return securityService.isLoggedIn();
    }

  } ]);

// Footer controller
tsApp.controller('FooterCtrl', [ '$scope', 'gpService', 'securityService',
  function($scope, gpService, securityService) {
    console.debug('configure FooterCtrl');
    // Declare user
    $scope.user = securityService.getUser();

    // Logout method
    $scope.logout = securityService.logout;

    // for ng-show
    $scope.isShowing = function() {
      return securityService.isLoggedIn();
    }

  } ]);

// Confirm dialog conroller and directive
tsApp.controller('ConfirmModalController', function($scope, $uibModalInstance, data) {
  // Local data for scope
  $scope.data = angular.copy(data);

  // OK function
  $scope.ok = function() {
    $uibModalInstance.close();
  };
  // Cancel function
  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };
});

tsApp
  .value(
    '$confirmModalDefaults',
    {
      template : '<div class="modal-header"><h3 class="modal-title">Confirm</h3></div><div class="modal-body">{{data.text}}</div><div class="modal-footer"><button class="btn btn-primary" ng-click="ok()">OK</button><button class="btn btn-warning" ng-click="cancel()">Cancel</button></div>',
      controller : 'ConfirmModalController'
    });

tsApp.factory('$confirm', function($uibModal, $confirmModalDefaults) {
  return function(data, settings) {
    settings = angular.extend($confirmModalDefaults, (settings || {}));
    data = data || {};

    if ('templateUrl' in settings && 'template' in settings) {
      delete settings.template;
    }

    settings.resolve = {
      data : function() {
        return data;
      }
    };

    return $uibModal.open(settings).result;
  };
});

tsApp.directive('confirm', function($confirm) {
  return {
    priority : 1,
    restrict : 'A',
    scope : {
      confirmIf : '=',
      ngClick : '&',
      confirm : '@'
    },
    link : function(scope, element, attrs) {
      function reBind(func) {
        element.unbind('click').bind('click', function() {
          func();
        });
      }

      function bindConfirm() {
        $confirm({
          text : scope.confirm
        }).then(scope.ngClick);
      }

      if ('confirmIf' in attrs) {
        scope.$watch('confirmIf', function(newVal) {
          if (newVal) {
            reBind(bindConfirm);
          } else {
            reBind(function() {
              scope.$apply(scope.ngClick);
            });
          }
        });
      } else {
        reBind(bindConfirm);
      }
    }
  }
})
