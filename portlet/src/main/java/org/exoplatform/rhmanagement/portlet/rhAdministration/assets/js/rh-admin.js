// Bootstrap manually the ng application, since :
// - it allows to correctly reinit the ng app after a (portal) page edition. Indeed, if we use the automatic init,
//   when the (portal) page is edited and then saved, the module are not registered again, so they are not executed
// - it is a good practice if we want to allow several ng app in the same html page (which could be the case with several portlets)
require( ["SHARED/jquery", "rhAdminAddonControllers"], function ( $,  rhAdminControllers)
{
	$( document ).ready(function() {
	  var rhAdminAppRoot = $('#rhAdminAddon');
      var rhAdminApp = angular.module('rhAdminApp', ['ngFileUpload'])
      .factory('PagerService', PagerService);
        rhAdminApp.filter('startFrom', function() {
          return function(input, start) {
              start = +start; //parse to int
              return input.slice(start);
          }
      });
      function PagerService() {
          // service definition
          var service = {};

          service.GetPager = GetPager;

          return service;

          // service implementation
          function GetPager(totalItems, currentPage, pageSize) {
              // default to first page
              currentPage = currentPage || 1;

              // default page size is 10
              pageSize = pageSize || 10;

              // calculate total pages
              var totalPages = Math.ceil(totalItems / pageSize);

              var startPage, endPage;
              if (totalPages <= 10) {
                  // less than 10 total pages so show all
                  startPage = 1;
                  endPage = totalPages;
              } else {
                  // more than 10 total pages so calculate start and end pages
                  if (currentPage <= 6) {
                      startPage = 1;
                      endPage = 10;
                  } else if (currentPage + 4 >= totalPages) {
                      startPage = totalPages - 9;
                      endPage = totalPages;
                  } else {
                      startPage = currentPage - 5;
                      endPage = currentPage + 4;
                  }
              }

              // calculate start and end item indexes
              var startIndex = (currentPage - 1) * pageSize;
              var endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);

              // create an array of pages to ng-repeat in the pager control
              var pages = [];
                  for (var i = startPage; i <= endPage; i++) {
                      pages.push(i);
                  }
              //var pages = _.range(startPage, endPage + 1);

              // return object with all pager properties required by the view
              return {
                  totalItems: totalItems,
                  currentPage: currentPage,
                  pageSize: pageSize,
                  totalPages: totalPages,
                  startPage: startPage,
                  endPage: endPage,
                  startIndex: startIndex,
                  endIndex: endIndex,
                  pages: pages
              };
          }
      }

      try {
          rhAdminApp.controller('rhAdminCtrl', rhAdminControllers);
		  angular.bootstrap(rhAdminAppRoot, ['rhAdminApp']);
      } catch(e) {
    	  console.log(e);
      }

	});

    $( "#startDate, #bDay, #leaveDate, #contractStartDate, #contractEndDate" ).datepicker();
    $( "#startDate, #bDay, #leaveDate, #contractStartDate, #contractEndDate" ).datepicker( "option", "dateFormat", "yy-mm-dd" );

});