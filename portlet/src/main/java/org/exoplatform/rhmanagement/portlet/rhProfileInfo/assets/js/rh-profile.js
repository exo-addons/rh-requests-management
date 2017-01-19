require( ["SHARED/jquery", "rhPrflAddonControllers"], function ( $,  rhPrflControllers)
{
    $( document ).ready(function() {
        var rhPrflAppRoot = $('#rhPrflAddon');
        var rhPrflApp = angular.module('rhPrflApp', []);
        rhPrflApp.directive('onlyDigits', function () {

            return {
                restrict: 'A',
                require: '?ngModel',
                link: function (scope, element, attrs, modelCtrl) {
                    modelCtrl.$parsers.push(function (inputValue) {
                        if (inputValue == undefined) return '';
                        var transformedInput = inputValue.replace(/[^0-9]/g, '');
                        if (transformedInput !== inputValue) {
                            modelCtrl.$setViewValue(transformedInput);
                            modelCtrl.$render();
                        }
                        return transformedInput;
                    });
                }
            };
        });
        try {
            rhPrflApp.controller('rhPrflCtrl', rhPrflControllers);
            angular.bootstrap(rhPrflAppRoot, ['rhPrflApp']);
        } catch(e) {
            console.log(e);
        }
    });
});