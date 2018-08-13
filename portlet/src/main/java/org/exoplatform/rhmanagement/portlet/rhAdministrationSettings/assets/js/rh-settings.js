// Bootstrap manually the ng application, since :
// - it allows to correctly reinit the ng app after a (portal) page edition. Indeed, if we use the automatic init,
//   when the (portal) page is edited and then saved, the module are not registered again, so they are not executed
// - it is a good practice if we want to allow several ng app in the same html page (which could be the case with several portlets)
require( ["SHARED/jquery", "rhSettingsController"], function ( $,  rhSettingsController)
{
	$( document ).ready(function() {
	  var rhSettingsAppRoot = $('#rhSettings');
      var rhSettingsApp = angular.module('rhSettingsApp', []);

      try {
          rhSettingsApp.controller('rhSettingsCtrl', rhSettingsController);
		  angular.bootstrap(rhSettingsAppRoot, ['rhSettingsApp']);
      } catch(e) {
    	  console.log(e);
      }




    $( "#beginDate" ).datepicker();
    $( "#beginDate" ).datepicker( "option", "dateFormat", "dd-mm-yy" );
    $( "#beginDate" ).datepicker( "option", "changeYear", true );
    $( "#beginDate" ).datepicker( "option", "yearRange", "1970:c+nn" );

});
    function getDate( element ) {
       // console.log(element);
        var date;
        try {
//            date = $.datepicker.parseDate( dateFormat, element );
              date = new Date(element, 1 - 1, 1);
        } catch( error ) {
            date = null;
        }
        return date;
    }


});