require( ["SHARED/jquery", "rhAddonControllers"], function ( $,  rhControllers)
{
    $( document ).ready(function() {
        var rhAppRoot = $('#rhAddon');
        var rhApp = angular.module('rhApp', []);
        rhApp.directive('onlyDigits', function () {

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
            rhApp.controller('rhCtrl', rhControllers);
            angular.bootstrap(rhAppRoot, ['rhApp']);
        } catch(e) {
            console.log(e);
        }

    });

    $( "#fromDate, #toDate" ).datepicker();
    $( "#fromDate, #toDate" ).datepicker( "option", "dateFormat", "yy-mm-dd" );
});