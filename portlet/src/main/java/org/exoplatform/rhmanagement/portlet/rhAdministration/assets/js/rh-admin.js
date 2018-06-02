// Bootstrap manually the ng application, since :
// - it allows to correctly reinit the ng app after a (portal) page edition. Indeed, if we use the automatic init,
//   when the (portal) page is edited and then saved, the module are not registered again, so they are not executed
// - it is a good practice if we want to allow several ng app in the same html page (which could be the case with several portlets)
require( ["SHARED/jquery", "rhAdminAddonControllers"], function ( $,  rhAdminControllers)
{
	$( document ).ready(function() {
	  var rhAdminAppRoot = $('#rhAdminAddon');
      var rhAdminApp = angular.module('rhAdminApp', ['ngFileUpload']);

      rhAdminApp.directive("rhEmployeeList", function() {
                    return {
                         templateUrl : "/rh-management-portlet/skin/rhemployeelist.html"
                     };
       });

      rhAdminApp.directive("rhRequestList", function() {
                    return {
                         templateUrl : "/rh-management-portlet/skin/rhrequestlist.html"
                     };
       });

             rhAdminApp.directive("addReqPopup", function() {
                           return {
                                templateUrl : "/rh-management-portlet/skin/addreqpopup.html"
                            };
              });

      try {
          rhAdminApp.controller('rhAdminCtrl', rhAdminControllers);
		  angular.bootstrap(rhAdminAppRoot, ['rhAdminApp']);
      } catch(e) {
    	  console.log(e);
      }



        /**/
        /* CREATE REQUEST FROM ADMIN */
        /**/
            var dateFormat = "dd-mm-yy";
            from = $( "#fromDateRequest" ).datepicker({
                controlType: 'select',
                oneLine: true,
                numberOfMonths: 1,
               /* hourMin: 9,
                hourMax: 18,
                hour: 9,
                minute: 00,*/
                dateFormat : "dd-mm-yy",
            });

            to = $( "#toDateRequest" ).datepicker({
                controlType: 'select',
                oneLine: true,
                defaultDate: "+1w",
                numberOfMonths: 1,
                /*hourMin: 9,
                hourMax: 18,
                hour: 18,
                minute: 00,*/
                dateFormat: "dd-mm-yy",
            });

            $(".selectize-input.items.not-full.has-options.ng-invalid.has-items").removeClass("ng-invalid");

        /**/
        /**/

	});

    $( "#startDate, #birthDay, #leaveDate, #contractStartDate, #contractEndDate, #toDate, #fromDate , #beginDate" ).datepicker();
    $( "#startDate, #birthDay, #leaveDate, #contractStartDate, #contractEndDate, #toDate, #fromDate, #beginDate, #toDateAdmin, #fromDateAdmin" ).datepicker( "option", "dateFormat", "dd-mm-yy" );
    $( "#startDate, #birthDay, #leaveDate, #contractStartDate, #contractEndDate, #toDate, #fromDate, #beginDate" ).datepicker( "option", "changeYear", true );
    $( "#startDate, #birthDay, #leaveDate, #contractStartDate, #contractEndDate, #toDate, #fromDate, #beginDate" ).datepicker( "option", "yearRange", "1970:c+nn" );


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
    $( "#startDate" ).on( "change", function() {
        $( "#leaveDate" ).datepicker( "option", "minDate", $("#startDate").val() );
    });
    $( "#leaveDate" ).on( "change", function() {
        $( "#startDate" ).datepicker( "option", "maxDate", $("#leaveDate").val() );
    });


    /**/
    $( "#contractStartDate" ).on( "change", function() {
        $( "#contractEndDate" ).datepicker( "option", "minDate", $("#contractStartDate").val() );
    });
    $( "#contractEndDate" ).on( "change", function() {
        $( "#contractStartDate" ).datepicker( "option", "maxDate", $("#contractEndDate").val() );
    });

    $( "#fromDate" ).on( "change", function() {
        $( "#toDate" ).datepicker( "option", "minDate", $("#fromDate").val() );
    });
    $( "#toDate" ).on( "change", function() {
        $( "#fromDate" ).datepicker( "option", "maxDate", $("#toDate").val() );
    });


    /* -- / DATE PICKER CONFIG -- */

    /* -- USER AUTOCOMPLETE SEARCH -- */
//    $(function(){
//        /**/
//        /**/
//           if( $(".selectize-control > .selectize-input").hasClass("has-items") ){
//                alert("hi");
//                $(".selectize-control > .selectize-dropdown").css("display", "none!important");
//                $(".selectize-input.items.has-options").removeClass("not-full").addClass("full");
//           }
//           console.log($(".selectize-input").children().length);
//        /**/
//        /**/
//    })

//     $("input#newUserId").on( "change", function() {
//        	alert("hello2");
//        	$("input#newUserId").keypress();
//        });
    /* -- / USER AUTOCOMPLETE SEARCH -- */


    $( "#fromDateAdmin, #toDateAdmin" ).datepicker({
        controlType: 'select',
        defaultDate: "+1w",
        numberOfMonths: 1,
        onClose: function() {
            $(".savebtnAdmin").removeClass("hidden");
        },
    });

    $( "#fromDateAdmin" ).on( "change", function() {
        $( "#fromDateAdmin" ).datepicker( "option", "minDate", $("#fromDateAdmin").val() );
    });

    $( "#toDateAdmin" ).on( "change", function() {
        $( "#toDateAdmin" ).datepicker( "option", "maxDate", $("#toDateAdmin").val() );
    });


    /**/
    /* CREATE REQUEST FROM ADMIN */
    /**/

    $( "#toDateRequest" ).on( "change", function() {
        if($("#toDateRequest").val() != ""){
            $("#toDateRequest").removeClass("ng-invalid");

            $("#toDateRequest").attr("value", $("#toDateRequest").val());
            // $( "#fromDate" ).datetimepicker( "option", "maxDate", $("#toDate").val() );
        }else{
            $("#toDateRequest").addClass("ng-invalid");}
    });

    $( "#fromDateRequest" ).on( "change", function() {
        if($("#fromDateRequest").val() != ""){
            $("#fromDateRequest").removeClass("ng-invalid");

            $("#fromDateRequest").attr("value", $("#fromDateRequest").val());
            // $( "#toDate" ).datetimepicker( "option", "minDate", $("#fromDate").val() );
        }else{
            $("#fromDateRequest").addClass("ng-invalid");}
    });

    $( "#toTimeRequest" ).on( "change", function() {
        if($("#toTimeRequest option[value]:selected").text() != ""){
            $("#toTimeRequest").removeClass("ng-invalid");
        }else{
            $("#toTimeRequest").addClass("ng-invalid");}
    });

    $( "#fromTimeRequest" ).on( "change", function() {
        if($("#fromTimeRequest option[value]:selected").text() != ""){
            $("#fromTimeRequest").removeClass("ng-invalid");
        }else{
            $("#fromTimeRequest").addClass("ng-invalid");}
    });

    /**/

});