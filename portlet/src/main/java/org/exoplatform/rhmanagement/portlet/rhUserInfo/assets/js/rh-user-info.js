require( ["SHARED/jquery", "rhUserInfoControllers"], function ( $,  rhUserInfoControllers)
{
    $( document ).ready(function() {
        var rhUserInfoAppRoot = $('#rhUserInfo');
        var rhUserInfoApp = angular.module('rhUserInfoApp', ['googlechart']);
        try {
            rhUserInfoApp.controller('rhUserInfoCtrl', rhUserInfoControllers);
            angular.bootstrap(rhUserInfoAppRoot, ['rhUserInfoApp']);
        } catch(e) {
            console.log(e);
        }
    });
});