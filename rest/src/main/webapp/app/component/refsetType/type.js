// Translation Table directive
// e.g. <div refset-type-icon obj='refset'></div>
tsApp.directive('refsetTypeIcon', [ function() {
  console.debug('configure refsetTypeIcon directive');
  return {
    restrict : 'A',
    scope : {
      refset : '='
    },
    templateUrl : 'app/component/refsetType/type.html',
    controller : [ '$scope', function($scope) {

      // empty controller

    } ]
  }
} ]);
