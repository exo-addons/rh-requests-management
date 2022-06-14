define("rhSettingsController", ["SHARED/jquery", "SHARED/juzu-ajax"], function ($, jz) {
    var rhSettingsCtrl = function ($scope, $q, $timeout, $http, $filter) {
        var rhSettingsContainer = $('#rhSettings');
        var deferred = $q.defer();

        $scope.settings = {};
        $scope.newOfficialVacation = {
                    label: '',
                    beginDate: '',
                    daysNumber: 0,
                    description: '',
                };
        $scope.newConventionalVacation = {
                    label: '',
                    description: '',
                    daysNumber: 0,
                    workingDays: true,
                };
        $scope.toDateShow;
        $scope.fromDateShow;

        $scope.initController = function () {
        
        }

        $scope.initConventionalModels= function(){
            $scope.newConventionalVacation.label = '';
            $scope.newConventionalVacation.daysNumber ='';
            $scope.newConventionalVacation.description = '';
            $scope.newConventionalVacation.workingDays = true;
        }

        $scope.initFerriesModels= function(){
            $scope.newOfficialVacation.label = '';
            $scope.newOfficialVacation.beginDate ='';
            $scope.newOfficialVacation.daysNumber = '';
            $scope.newOfficialVacation.description = '';

            $("#beginDate").val('');
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
                url: rhSettingsContainer.jzURL('RhAdministrationSettingsController.getBundle') + "&locale=" + eXo.env.portal.language
            }).then(function successCallback(data) {
                $scope.i18n = data.data;
                $scope.loadContext();
                deferred.resolve(data);
                /*$scope.setResultMessage(data, "success");*/
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


		$scope.loadConventionalVacations = function () {
            $http({
                method: 'GET',
                url: rhSettingsContainer.jzURL('RhAdministrationSettingsController.getConventionalVacations')
            }).then(function successCallback(data) {
                $scope.cVacations = data.data;
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });

        };


		$scope.saveConventionalVacation = function () {
                if(($scope.newConventionalVacation.label == "")  || ($scope.newConventionalVacation.label == undefined))
                     $("#conventionalLabel").addClass("ng-invalid");
                else $("#conventionalLabel").removeClass("ng-invalid");

                if(($scope.newConventionalVacation.daysNumber == "")  || ($scope.newConventionalVacation.daysNumber == undefined))
                     $("#conventionalDaysNumber").addClass("ng-invalid");
                else $("#conventionalDaysNumber").removeClass("ng-invalid");

                if((($scope.newConventionalVacation.label != "")  && ($scope.newConventionalVacation.label != undefined)) && (($scope.newConventionalVacation.daysNumber != "")  && ($scope.newConventionalVacation.daysNumber != undefined))){
                    if(($scope.newConventionalVacation.workingDays == null)  || ($scope.newConventionalVacation.workingDays == undefined)){
                        $scope.newConventionalVacation.workingDays = false;
                    }
                    $http({
                        data: $scope.newConventionalVacation,
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        url: rhSettingsContainer.jzURL('RhAdministrationSettingsController.saveConventionalVacation')
                    }).then(function successCallback(data) {
                        $scope.loadConventionalVacations();
                        $scope.showCvacations = false;
                        $scope.initConventionalModels();
                    }, function errorCallback(data) {
                        $scope.setResultMessage($scope.i18n.defaultError, "error");
                    });
                }
        }

        $scope.deleteConventionalVacation = function (cVacation) {
            $http({
                data: cVacation,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: rhSettingsContainer.jzURL('RhAdministrationSettingsController.deleteConventionalVacation')
            }).then(function successCallback(data) {
                $scope.loadConventionalVacations();
                $scope.setResultMessage($scope.i18n.vacationdelete, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

		$scope.loadOfficialVacations = function () {
            $http({
                method: 'GET',
                url: rhSettingsContainer.jzURL('RhAdministrationSettingsController.getOfficialVacations')
            }).then(function successCallback(data) {
                $scope.oVacations = data.data;
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });

        };


		$scope.saveOfficialVacation = function () {

                if(($scope.newOfficialVacation.label == "") || ($scope.newOfficialVacation.label == undefined))
                     $("#officialLabel").addClass("ng-invalid");
                else $("#officialLabel").removeClass("ng-invalid");

                if(($scope.newOfficialVacation.beginDate == "Invalid Date") || ($scope.newOfficialVacation.beginDate == ""))
                     $("#beginDate").addClass("ng-invalid");
                else $("#beginDate").removeClass("ng-invalid");

                if(($scope.newOfficialVacation.daysNumber == "") || ($scope.newOfficialVacation.daysNumber == undefined))
                     $("#officialDaysNumber").addClass("ng-invalid");
                else $("#officialDaysNumber").removeClass("ng-invalid");

                if ($("#beginDate") !== "") {
                    var date = ($("#beginDate").val()).split("-");
                    $scope.newOfficialVacation.beginDate = new Date(date[2]+'-'+date[1]+'-'+date[0]);
                }
                if((($scope.newOfficialVacation.label != "") && ($scope.newOfficialVacation.label != undefined)) && (($scope.newOfficialVacation.beginDate != "Invalid Date") && ($scope.newOfficialVacation.beginDate != ""))
                    && (($scope.newOfficialVacation.daysNumber != "") && ($scope.newOfficialVacation.daysNumber != undefined))){
                    $http({
                        data: $scope.newOfficialVacation,
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        url: rhSettingsContainer.jzURL('RhAdministrationSettingsController.saveOfficialVacation')
                    }).then(function successCallback(data) {
                        $scope.loadOfficialVacations();
                        $scope.showOvacations = false;
                        $scope.initFerriesModels();
                    }, function errorCallback(data) {
                        $scope.setResultMessage($scope.i18n.defaultError, "error");
                    });
                }
        }

        $scope.deleteOfficialVacation = function (oVacation) {
            $http({
                data: oVacation,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: rhSettingsContainer.jzURL('RhAdministrationSettingsController.deleteOfficialVacation')
            }).then(function successCallback(data) {
                $scope.loadOfficialVacations();
                $scope.setResultMessage($scope.i18n.vacationdelete, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.geti18n = function (id) {

            for (var propt in $scope.i18n) {
                if (id == propt) return $scope.i18n[propt];
            }
            return id;
        };
/*here*/
        $scope.getLocaleDate = function(date) {
            if($scope.i18n&&$scope.i18n.offset){
                return date+$scope.i18n.offset;
            }else{
                return  date;
            }

        };

   
        $scope.dateFormat = function (date) {
            var newDate = new Date(date);
            newDate = {
                date: newDate.getDate() + "-" + (newDate.getMonth() + 1) +"-"+ newDate.getFullYear(),
                time: newDate.getHours()+":"+newDate.getMinutes()
            };
            return newDate;
        };

   

        $scope.loadContext = function () {
            $http({
                method: 'GET',
                url: rhSettingsContainer.jzURL('RhAdministrationSettingsController.getContext')
            }).then(function successCallback(data) {
                $scope.settings.rhManager=data.data.rhManager;
				$scope.loadConventionalVacations();
                $scope.loadOfficialVacations();
                deferred.resolve(data);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }



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

        $scope.updateDateFormat = function(date, time) {
            var dateSplit = date;
            dateSplit = dateSplit.split("-");
            dateSplit = moment(dateSplit[2] + '-' + dateSplit[1] + '-' + dateSplit[0] + ' '+ time);
            return Date.parse(dateSplit);
        };


                $scope.saveSettings = function (settings) {
                    $http({
                        data: settings,
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        url: rhSettingsContainer.jzURL('RhAdministrationSettingsController.saveSettings')
                    }).then(function successCallback(data) {
                    }, function errorCallback(data) {
                        $scope.setResultMessage($scope.i18n.defaultError, "error");
                    });
                }
        $scope.loadBundles();
        $('#rhSettings').css('visibility', 'visible');
        $("#rhLoadingBar").remove();
    };
    return rhSettingsCtrl;
});
