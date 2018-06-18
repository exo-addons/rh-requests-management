define("rhRequestHistoryControllers", ["SHARED/jquery", "SHARED/juzu-ajax", "SHARED/userInvitation"], function ($, jz, invite, moment) {
    var rhRequestHistoryCtrl = function ($scope, $q, $timeout, $http, $filter) {
        var rhRequestHistoryContainer = $('#rhRequestHistory');
        var deferred = $q.defer();


        $scope.balanceHistory = [];
        $scope.orderByField = 'title';
        $scope.reverseSort = false;

        $scope.toDateShow;
        $scope.fromDateShow;

        $scope.itemsPerPage = 10;
        $scope.currentPage = 0;
        $scope.pages=[];

        $scope.bFromDate;
        $scope.bToDate;
        $scope.userBlanceId="";
        $scope.emFilter="active";


        $scope.initController = function () {
            // initialize to page 1
            $scope.vm.setPage(1);
        }



        $scope.setResultMessage = function (text, type) {
            $scope.resultMessageClass = "alert-" + type;
            $scope.resultMessageClassExt = "uiIcon" + type.charAt(0).toUpperCase()
                + type.slice(1);
            $scope.showAlert = true;
            $scope.resultMessage = text;
        }

        $scope.loadBundles = function () {
            $http({
                method: 'GET',
                url: rhRequestHistoryContainer.jzURL('RhRequestsHistoryController.getBundle') + "&locale=" + eXo.env.portal.language
            }).then(function successCallback(data) {
                $scope.i18n = data.data;
                deferred.resolve(data);
                /*$scope.setResultMessage(data, "success");*/
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.getUser = function (newUserId) {

            // $scope.getEmployees($scope.currentUser, newUserId);

            if (newUserId) {
                $("#getUser").removeClass("invalid");
                $http({
                    method: 'GET',
                    url: rhRequestHistoryContainer.jzURL('RhRequestsHistoryController.getUser') + "&userId=" + newUserId
                }).then(function successCallback(data) {
                    $scope.userDetails = data.data;
                    $scope.showAlert = false;


                    if (data.data == "") {
                        $scope.showAddForm = false;
                        //    $scope.setResultMessage($scope.i18n.noUser, "info");
                        $timeout(function () {
                            $scope.showAlert = false;
                        }, 2000);
                    } else {

                        $scope.loadUserHRData(data.data);
                        $scope.showAddForm = true;
                        if (!data.data.avatar) {
                            $scope.userDetails.avatar = "/eXoSkin/skin/images/system/UserAvtDefault.png";
                        }
                    }

                }, function errorCallback(data) {
                    $scope.showAddForm = false;
                    $scope.setResultMessage($scope.i18n.defaultError, "error");
                });
            } else {
                $("#getUser").addClass("invalid").focus();
            }
        };



        $scope.geti18n = function (id) {

            for (var propt in $scope.i18n) {
                if (id == propt) return $scope.i18n[propt];
            }
            return id;
        };


        $scope.getLocaleDate = function(date) {
            if($scope.i18n&&$scope.i18n.offset){
                return date+$scope.i18n.offset;
            }else{
                return  date;
            }

        };




        $scope.getEmployees = function (currentUser, searchUser = null, searchType) {
            var rsetUrl = "/rest/rhrequest/users/find?currentUser=" + currentUser + "&spaceURL=exo_employees&nameToSearch=" + searchUser;//+$scope.employeesSpace;

            $http({
                method: 'GET',
                url: rsetUrl
            }).then(function successCallback(data) {

                /* create a table of users IDs*/
                $(".newUserName").autocomplete({
                    source: function( request, response ) {
                        var users = [];
                        angular.forEach(data.data.options, function (value, key) {
                            users[key] = [];
                            users[key]['value'] = value.value;
                            users[key]['fullName'] = value.text;
                            users[key]['avatar'] = value.avatarUrl || '/eXoSkin/skin/images/system/UserAvtDefault.png';
                        });
                        response( users );
                    },
                    minLength: 3,
                    focus: function (event, ui) {
                        $(".newUserName").val(ui.item.fullName);
                        if(searchType=="employee"){
                            $scope.getUser(ui.item.value);
                        }
                        return false;
                    },
                    select: function (event, ui) {
                        $(".newUserName").val(ui.item.fullName);
                        if(searchType=="employee"){
                            $scope.getUser(ui.item.value);
                        }else if(searchType=="history"){
                            $scope.userBlanceId=ui.item.value;
                        }

                        return false;
                    }
                }).autocomplete("instance")._renderItem = function (ul, item) {

                    return $("<li>")
                        .append("<div> <img src='" + item.avatar + "' class='avataruser' /> " + item.fullName + "</div>")
                        .appendTo(ul);
                };

            }, function errorCallback(data) {
                console.log("error getEmployees");
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.loadBalanceHistory = function () {
            $scope.showAlert = false;
            if ($("#fromDate").val() !== "") {
                var date = ($("#fromDate").val()).split("-");
                $scope.bFromDate = new Date(date[2]+'-'+date[1]+'-'+date[0]);
            }else{
                $scope.setResultMessage("Please define from date", "error");
            }
            if ($("#toDate").val() !== "") {
                var date = ($("#toDate").val()).split("-");
                $scope.bToDate = new Date(date[2]+'-'+date[1]+'-'+date[0]);
            }else{
                $scope.setResultMessage("Please define to date", "error");
            }

            $('#historyTable').css('visibility', 'hidden');
            $("#balanceLoadingBar").css('display', 'block');

            $http({
                method: 'GET',
                url: rhRequestHistoryContainer.jzURL('RhRequestsHistoryController.getBalanceHistoryByUserId') + "&userId=" + $scope.userBlanceId  + "&from=" + $scope.bFromDate.getTime()  + "&to=" + $scope.bToDate.getTime()
            }).then(function successCallback(data) {
                $scope.balanceHistory = data.data;
                $("#balanceLoadingBar").css('display', 'none');
                $('#historyTable').css('visibility', 'visible');

            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.exportData = function () {
            var blob = new Blob([document.getElementById('exportable').innerHTML], {
                type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8"
            });
            reportOwner="all";
            if($scope.userBlanceId!="")reportOwner=$scope.userBlanceId;
            saveAs(blob, "Report_"+reportOwner+".xls");
        };


        /**
         *
         * SCROLL TO TOP
         */
        // function currentYPosition() {
        $scope.currentYPosition = function() {
            // Firefox, Chrome, Opera, Safari
            if (self.pageYOffset) return self.pageYOffset;
            // Internet Explorer 6 - standards mode
            if (document.documentElement && document.documentElement.scrollTop)
                return document.documentElement.scrollTop;
            // Internet Explorer 6, 7 and 8
            if (document.body.scrollTop) return document.body.scrollTop;
            return 0;
        };

        $scope.scrollTo = function() {

            // This scrolling function
            // is from http://www.itnewb.com/tutorial/Creating-the-Smooth-Scroll-Effect-with-JavaScript
            var startY = $scope.currentYPosition();
            var stopY = 0;
            var distance = stopY > startY ? stopY - startY : startY - stopY;
            if (distance < 100) {
                scrollTo(0, stopY);
                return;
            }
            var speed = Math.round(distance / 100);
            if (speed >= 20) speed = 20;
            var step = Math.round(distance / 25);
            var leapY = stopY > startY ? startY + step : startY - step;
            var timer = 0;
            if (stopY > startY) {
                for (var i = startY; i < stopY; i += step) {
                    setTimeout("window.scrollTo(0, " + leapY + ")", timer * speed);
                    leapY += step;
                    if (leapY > stopY) leapY = stopY;
                    timer++;
                }
                return;
            }
            for (var i = startY; i > stopY; i -= step) {
                setTimeout("window.scrollTo(0, " + leapY + ")", timer * speed);
                leapY -= step;
                if (leapY < stopY) leapY = stopY;
                timer++;
            }
        };


        $scope.loadBundles();

        $('#rhRequestHistory').css('visibility', 'visible');
        $("#rhLoadingBar").remove();
    };
    return rhRequestHistoryCtrl;


});
