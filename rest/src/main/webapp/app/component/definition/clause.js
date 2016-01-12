// Definition Clause directive
// e.g. <div definition-clause refset="refset" allow-negation="false"></div>
tsApp.directive('definitionClause', [ '$uibModal', 'utilService', function($uibModal, utilService) {
  console.debug('configure definitionClause directive');
  return {
    restrict : 'A',
    scope : {
      clause : '=',
      allowNegation : '@',
      mode : '@'
    },
    templateUrl : 'app/component/definition/clause.html',
    controller : [ '$scope', function($scope) {

      // end
    } ]
  }

} ]);
