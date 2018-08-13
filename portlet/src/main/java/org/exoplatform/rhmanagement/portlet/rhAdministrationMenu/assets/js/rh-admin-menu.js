// Bootstrap manually the ng application, since :
// - it allows to correctly reinit the ng app after a (portal) page edition. Indeed, if we use the automatic init,
//   when the (portal) page is edited and then saved, the module are not registered again, so they are not executed
// - it is a good practice if we want to allow several ng app in the same html page (which could be the case with several portlets)
require( ["SHARED/jquery", "rhAdminMenuControllers"], function ( $,  rhAdminMenuControllers)
{
	$( document ).ready(function() {
	  var rhAdminMenuAppRoot = $('#rhAdminMenu');
      var rhAdminMenuApp = angular.module('rhAdminMenuApp', [])
   
      try {
          rhAdminMenuApp.controller('rhAdminMenuCtrl', rhAdminMenuControllers);
		  angular.bootstrap(rhAdminMenuAppRoot, ['rhAdminMenuApp']);
      } catch(e) {
    	  console.log(e);
      }

	});
});