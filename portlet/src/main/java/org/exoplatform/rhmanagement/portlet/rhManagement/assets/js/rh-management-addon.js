require( ["SHARED/jquery", "rhAddonControllers"], function ( $,  rhControllers)
{
    $( document ).ready(function() {
        var rhAppRoot = $('#rhAddon');
        var rhApp = angular.module('rhApp', ['ngFileUpload']);
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

        /* -- -- */

        var dateFormat = "yy-mm-dd",
        from = $( "#fromDate" ).datepicker({
            defaultDate: "+1w",
            numberOfMonths: 1
        }),

        to = $( "#toDate" ).datepicker({
            defaultDate: "+1w",
            numberOfMonths: 1
        });

        $(".selectize-input.items.not-full.has-options.ng-invalid.has-items").removeClass("ng-invalid");

    });

    function getDate( element ) {
        console.log(element);
        var date;
        try {
//            date = $.datepicker.parseDate( dateFormat, element );
              date = new Date(element, 1 - 1, 1);
        } catch( error ) {
            date = null;
        }
        return date;
    }

    //$("#toDate").val()
    //$("#fromDate").val()

//    $( "#fromDate, #toDate" ).datepicker();
    $( "#fromDate, #toDate" ).datepicker( "option", "dateFormat", "yy-mm-dd" );
    $( "#toDate" ).on( "change", function() {
        if($("#toDate").val() != ""){
            $("#toDate").removeClass("ng-invalid");
            $("#toDate").attr("value", $("#toDate").val());
            $( "#fromDate" ).datepicker( "option", "maxDate", $("#toDate").val() );
        }else{
        $("#toDate").addClass("ng-invalid");}
    });
    $( "#fromDate" ).on( "change", function() {
        if($("#fromDate").val() != ""){
            $("#fromDate").removeClass("ng-invalid");
            $("#fromDate").attr("value", $("#fromDate").val());
            $( "#toDate" ).datepicker( "option", "minDate", $("#fromDate").val() );
        }else{
        $("#fromDate").addClass("ng-invalid");}
    });


    $( "#daysNumberHollidays, #daysNumberSick" ).on( "change", function() {
            if(($("#daysNumberHollidays").val() != "") || ($("#daysNumberSick").val() != "")){
                $("#daysNumberHollidays, #daysNumberSick").removeClass("ng-invalid");
            }else{
                $("#daysNumberHollidays, #daysNumberSick").addClass("ng-invalid");
            }
    });




    $("#saveVacationRequest").click(function(){
        if($("#toDate").val() != ""){
            $("#toDate").removeClass("ng-invalid");
        }else{
            $("#toDate").addClass("ng-invalid");}

        if($("#fromDate").val() != ""){
            $("#fromDate").removeClass("ng-invalid");
        }else{
        $("#fromDate").addClass("ng-invalid");}

        if(($("#daysNumberHollidays").val() != "") || ($("#daysNumberSick").val() != "")){
            $("#daysNumberHollidays, #daysNumberSick").removeClass("ng-invalid");
        }else{
            $("#daysNumberHollidays, #daysNumberSick").addClass("ng-invalid");
        }
    });
});
