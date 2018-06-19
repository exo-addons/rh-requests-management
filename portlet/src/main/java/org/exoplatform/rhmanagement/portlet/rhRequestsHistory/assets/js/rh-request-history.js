// Bootstrap manually the ng application, since :
// - it allows to correctly reinit the ng app after a (portal) page edition. Indeed, if we use the automatic init,
//   when the (portal) page is edited and then saved, the module are not registered again, so they are not executed
// - it is a good practice if we want to allow several ng app in the same html page (which could be the case with several portlets)
require( ["SHARED/jquery", "rhRequestHistoryControllers"], function ( $,  rhRequestHistoryControllers)
{
	$( document ).ready(function() {
	  var rhRequestHistoryAppRoot = $('#rhRequestHistory');
      var rhRequestHistoryApp = angular.module('rhRequestHistoryApp', []);


      try {
          rhRequestHistoryApp.controller('rhRequestHistoryCtrl', rhRequestHistoryControllers);
		  angular.bootstrap(rhRequestHistoryAppRoot, ['rhRequestHistoryApp']);
      } catch(e) {
    	  console.log(e);
      }


});

    $( "#toDate, #fromDate" ).datepicker();
    $( "#toDate, #fromDate" ).datepicker( "option", "dateFormat", "dd-mm-yy" );
    $( "#toDate, #fromDate" ).datepicker( "option", "changeYear", true );
    $( "#toDate, #fromDate" ).datepicker( "option", "yearRange", "1970:c+nn" );


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

    /* -- DATE PICKER CONFIG -- */


    $( "#fromDate" ).on( "change", function() {
        $( "#toDate" ).datepicker( "option", "minDate", $("#fromDate").val() );
    });
    $( "#toDate" ).on( "change", function() {
        $( "#fromDate" ).datepicker( "option", "maxDate", $("#toDate").val() );
    });




    /**/

});