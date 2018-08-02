define("rhAddonControllers", [ "SHARED/jquery", "SHARED/juzu-ajax","SHARED/userInvitation","calendar"], function($, jz,invite,calendar)  {

    var rhCtrl = function($scope, $q, $timeout, $http, $filter, Upload) {
        var rhContainer = $('#rhAddon');
        var deferred = $q.defer();

        $scope.infoPageUrl = "/portal/intranet/rh-info";
        $scope.currentUser="";
        $scope.employeesSpace="";
        $scope.currentUserAvatar="";
        $scope.currentUserName="";
        $scope.sickBalance="";
        $scope.holidaysBalance="";
        $scope.hrId="";
        $scope.insuranceId="";
        $scope.socialSecNumber="";
        $scope.vacationRequestsToValidate = [];
        $scope.vacationSubsRequests = [];
        $scope.myVacationRequests = [];
        $scope.comments = [];
        $scope.history = [];
        $scope.userCalendars = [];
        $scope.userCalendarId = "";
        $scope.managerCalendarId = "";
        $scope.vrmanagers = [];
        $scope.vrsubs = [];
        $scope.calEvents = [];
        $scope.attachements = [];
        $scope.showForm = false;
        $scope.showDetails = false;
        $scope.showResume = false;
        $scope.showList = true;
        $scope.showCal = false;
        $scope.showSick = false;
        $scope.showRequestsTab = true;
        $scope.showHollidays = true;
        $scope.showLeave = false;
        $scope.showConventional = false;
        $scope.showLogs = false;
        $scope.showFullReq= false;
        $scope.showAlert = false;
        $scope.showInfoBox = true;
        $scope.newVacationRequest = {id: null};
        $scope.vacationsToVAlidateFilter = "active";
        $scope.myVacationsFilter = "active";
        $scope.vrOwnerData = null;
        $scope.newComment = null;
        $scope.cVacations = [];
        $scope.officialDays = [];
        $scope.cVacation = null;
        $scope.oVacations = [];





        $scope.vacationRequesttoShow = null;

        $scope.initModals= function(){
            $(".nbrDays").text('');
            $("#substitutes, #managers").val('');
            $scope.newVacationRequest.type = '';
            $scope.newVacationRequest.fromDate ='';
            $scope.newVacationRequest.toDate = '';
            $scope.newVacationRequest.reason = '';
        }

        $scope.showBalance= function(){
            if( $scope.newVacationRequest.type == 'holiday'){
                $scope.showSick=false;
                $scope.showHollidays=true;
                $scope.showLeave=false;
                $scope.showConventional=false;
            } else if( $scope.newVacationRequest.type == 'sick'){
                $scope.showSick=true;
                $scope.showHollidays=false;
                $scope.showLeave=false;
                $scope.showConventional=false;
            } else if( $scope.newVacationRequest.type == 'leave'){
                $scope.showSick=false;
                $scope.showHollidays=false;
                $scope.showLeave=true;
                $scope.showConventional=false;
            }else if( $scope.newVacationRequest.type == 'conventional'){
                $scope.showSick=false;
                $scope.showHollidays=false;
                $scope.showLeave=false;
                $scope.showConventional=true;
                }
            else {
                $scope.showSick=false;
                $scope.showHollidays=false;
                $scope.showLeave=false;
                $scope.showConventional=false;
            }
        }



        $scope.setConventional = function(cVacation){
            $scope.cVacation=cVacation;
            $scope.newVacationRequest.daysNumber=cVacation.daysNumber;
         }



        $scope.showFormFn = function(){
            $scope.showInfoTab = false;
            $scope.showRequestsTab=true;
            $scope.showForm = true;
            if($scope.getUrlParameterByName('rid')){
                $scope.showDetails = true;
                $scope.showFullReq = false;
                }

                }

        $scope.toggleView = function(){
            calendar.refresh('myCalendar');
            $scope.showInfoTab = false;
            $scope.showRequestsTab=true;
            $scope.showCal = !$scope.showCal;
            $text = $(".show-calender").text();
            if($text.indexOf($scope.i18n.showCalendar) >= 0){
                /*$text = $scope.i18n.hideCalendar;*/
                $text = "<i class='uiIconPLFEvent'></i><span id='forum' class='tabName show-calender'>"+$scope.i18n.hideCalendar+"</span>";

                $scope.showForm = false;
                $scope.showList = false;
                $scope.showDetails = false;
            }else{
                /*$text = $scope.i18n.showCalendar;*/
                $text = "<i class='uiIconPLFEvent'></i><span id='forum' class='tabName show-calender'>"+$scope.i18n.showCalendar+"</span>";
                $scope.showResume = false;
                $scope.showList = true;
                $scope.showDetails = true;
            }
            $("#resultMessage").toggleClass("hide_alert");
            $text = $(".show-calender").html($text);
        }

        $scope.loadBundles = function() {
            $http({
                method : 'GET',
                url : rhContainer.jzURL('RHRequestManagementController.getBundle')
            }).then(function successCallback(data) {
                $scope.i18n = data.data;
                $scope.showAlert = false;
                $scope.loadData();
                deferred.resolve(data);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.loadContext = function() {
            $http({
                method : 'GET',
                url : rhContainer.jzURL('RHRequestManagementController.getContext')
            }).then(function successCallback(data) {

                $scope.completeCurrentUser = data.data;
                $scope.currentUser=data.data.currentUser;
                $scope.employeesSpace=data.data.employeesSpace
                $scope.currentUserAvatar=data.data.currentUserAvatar;
                $scope.currentUserName=data.data.currentUserName;
                $scope.sickBalance=data.data.sickBalance;
                $scope.holidaysBalance=data.data.holidaysBalance;
                $scope.hrId=data.data.hrId;
                $scope.insuranceId=data.data.insuranceId;
                $scope.socialSecNumber=data.data.socialSecNumber;
                $scope.officialDays=data.data.officialDays;
                var rsetUrl="/rest/rhrequest/users/find?currentUser="+$scope.currentUser+"&spaceURL="+$scope.employeesSpace;
                invite.build('managers', rsetUrl,'choose user');
                console.log(invite.build('substitutes', rsetUrl,'choose user'));
                console.log(deferred.resolve(data));

                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.getUrlParameterByName = function(name, url) {
            if (!url) {
                url = window.location.href;
            }
            name = name.replace(/[\[\]]/g, "\\$&");
            var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
                results = regex.exec(url);
            if (!results) return null;
            if (!results[2]) return '';
            return decodeURIComponent(results[2].replace(/\+/g, " "));
        }


        $scope.showRequestfromUrl = function() {
            var requestId = $scope.getUrlParameterByName('rid');
            if (typeof requestId !== 'undefined' && requestId !==null) {
                $http({
                    method : 'GET',
                    url : rhContainer.jzURL('RHRequestManagementController.getVacationRequest')+ "&id=" +requestId
                }).then(function successCallback(data) {
                    if(data.data==""){
                        $scope.setResultMessage($scope.i18n.requestNotFound, "error");
                    }else{
                        $scope.showVacationRequest(data.data);
                        $scope.showInfoBox=false;
                        $scope.showFullReq = true;
                        $scope.showAlert = false;
                    }

                }, function errorCallback(data) {
                    $scope.setResultMessage($scope.i18n.defaultError, "error");
                });
            }
        }


        $scope.loadVacationRequestsToValidate= function(status) {
            var url="";
            if(status!=null){
                url=url+ "&status="+status;
            }
            $http({
                method : 'GET',
                url : rhContainer.jzURL('RHRequestManagementController.getVacationRequestsForCurrentValidator')+url
            }).then(function successCallback(data) {
                $scope.vacationRequestsToValidate = data.data;
                if($scope.vacationRequestsToValidate.length>0){
                    $scope.showList=true;
                    $scope.showForm=false;
                }
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };


        $scope.loadSubordonateVacationRequests= function() {

            $http({
                method : 'GET',
                url : rhContainer.jzURL('RHRequestManagementController.getVacationRequestsForCurrentManager')
            }).then(function successCallback(data) {
                $scope.vacationSubsRequests = data.data;
                if($scope.vacationSubsRequests.length>0){
                    $scope.showList=true;
                    $scope.showForm=false;
                }
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };


        $scope.loadMyVacationRequests = function(status) {
            var url="";
            if(status!=null){
                url=url+ "&status="+status;
            }
            $http({
                method : 'GET',
                url : rhContainer.jzURL('RHRequestManagementController.getVacationRequestsOfCurrentUser')+url
            }).then(function successCallback(data) {
                $scope.myVacationRequests = data.data;
                if($scope.myVacationRequests.length>0){
                    $scope.showList=true;
                    $scope.showForm=false;
                }
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };

        $scope.saveVacationRequest = function() {
//            if (!$scope.validateVacationRequestForm($scope.newVacationRequest)) {
//                return;
//            }
            var datefrom = $scope.updateDateFormat($("#fromDate").val(), $("#fromTime option[value]:selected").text());
            var dateto = $scope.updateDateFormat($("#toDate").val(), $("#toTime option[value]:selected").text());

            if(($scope.newVacationRequest.type != "leave") && ($scope.newVacationRequest.type != "conventional") && (!$scope.newVacationRequest.daysNumber)){
                $scope.setResultMessage($scope.i18n.nbrDate, "error");
                $("#daysNumberHollidays, #daysNumberSick").addClass("ng-invalid");
                $("#toDate, #fromDate").removeClass("ng-invalid");
            }else if( ($scope.newVacationRequest.type != "leave") && ($scope.newVacationRequest.type != "conventional") && (($("#toDate").val() == "") || ($("#fromDate").val() == "")||
            (($("#toTime option[value]:selected").text() == "") || ($("#toTime option[value]:selected").text() == "")) ||
            (dateto == NaN) || (datefrom == NaN))){
                $("#daysNumberHollidays, #daysNumberSick").removeClass("ng-invalid");
                $scope.setResultMessage($scope.i18n.fromToDate, "error");
                $("#toDate, #fromDate, #toTime, #fromTime").addClass("ng-invalid");
            }else if(($scope.newVacationRequest.type == "leave") && ($("#fromDate").val() == "") || (($scope.newVacationRequest.type == "leave") && ($("#fromTime option[value]:selected").text() == ""))){
                $("#daysNumberHollidays, #daysNumberSick").removeClass("ng-invalid");
                $scope.setResultMessage($scope.i18n.leaveDateMsg, "error");
                $("#toDate, #fromDate, #toTime, #fromTime").addClass("ng-invalid");
            }else if(datefrom > dateto){

                $("#daysNumberHollidays, #daysNumberSick").removeClass("ng-invalid");
                $scope.setResultMessage($scope.i18n.fromToSup, "error");
                $("#toDate, #fromDate").addClass("ng-invalid");

            } else if($scope.getUsers("managers")[0] == ""){
                $scope.setResultMessage($scope.i18n.manager, "error");
                $(".managersInput .selectize-input").addClass("ng-invalid");

            }else if(($scope.newVacationRequest.type == "conventional") && (($("#fromDate").val() == ""))){
                $scope.setResultMessage($scope.i18n.leaveDateMsg, "error");
                $(".managersInput .selectize-input").addClass("ng-invalid");

             }else{

                $("#daysNumberHollidays, #daysNumberSick,.managersInput .selectize-input").removeClass("ng-invalid");
                $("#toDate, #fromDate").removeClass("ng-invalid");
                $scope.showAlert=false;
                $scope.newVacationRequest.fromDate = (datefrom);
                $scope.newVacationRequest.toDate = (dateto);

                var managers= $scope.getUsers("managers");
                var substitutes= $scope.getUsers("substitutes");

                $scope.newVacationRequestWithManagers  = {
                    vacationRequestDTO : $scope.newVacationRequest,
                    managers : managers,
                    substitutes : substitutes,
                    exoCalendarId: $scope.userCalendarId
                };

                $scope.newVacationRequestWithManagers.cVacation=$scope.cVacation;
                $http({
                    data : $scope.newVacationRequestWithManagers,
                    method : 'POST',
                    headers : {
                        'Content-Type' : 'application/json'
                    },
                    url : rhContainer.jzURL('RHRequestManagementController.saveVacationRequest')
                }).then(function successCallback(data) {
                    $scope.myVacationRequests = data.data;
                    $scope.showForm = false;
                    $scope.newVacationRequest = {id : null };
                    $("#managers").val("");
                    $("#substitutes").val("");
                    $("#userCalendar").val("");
                    $scope.showAlert = false;
                    calendar.refresh('myCalendar');

                    $scope.initModals();
                }, function errorCallback(data) {
                    $scope.setResultMessage($scope.i18n.defaultError, "error");
                });
            }
        }


        $scope.shareCalendar = function(vacationRequest) {
            var vr = {
                vacationRequestDTO : vacationRequest ,
                exoCalendarId: $scope.managerCalendarId
            };
            $http({
                data : vr,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhContainer.jzURL('RHRequestManagementController.shareCalendar')
            }).then(function successCallback(data) {
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

        $scope.deleteRequest = function(vacationRequest) {

            if (!confirm($scope.i18n.deleteConfirm + " " + vacationRequest.id)) {
                return;
            }
            /*$scope.setResultMessage($scope.i18n.savingVacationRequest, "info");*/
            $http({
                data : vacationRequest,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhContainer.jzURL('RHRequestManagementController.deleteRequest')
            }).then(function successCallback(data) {
                $scope.myVacationRequests = data.data;
                $scope.showForm = false;
                $scope.showForm = false;
                calendar.refresh('myCalendar');
                $scope.setResultMessage($scope.i18n.deleteRequest, "success");
                $timeout(function() {
                    $scope.showAlert = false;
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };

        $scope.showVacationRequest = function(vacationRequest) {
            $scope.vacationRequesttoShow=vacationRequest;
            $scope.loadVrOwnerData(vacationRequest);
            $scope.loadManagers(vacationRequest);
            $scope.loadSubstitues(vacationRequest);
            $scope.loadAttachments(vacationRequest);
            $scope.loadComments(vacationRequest);
            $scope.loadHistory(vacationRequest);
            $scope.showDetails = true;
        };


        $scope.showVacationRequestById = function(id) {

            $http({
                method : 'GET',
                url : rhContainer.jzURL('RHRequestManagementController.getVacationRequest')+ "&id=" +id
            }).then(function successCallback(data) {
                $scope.showVacationRequest(data.data);
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.showVacationResume = function(id) {

            $http({
                method : 'GET',
                url : rhContainer.jzURL('RHRequestManagementController.getVacationRequest')+ "&id=" +id
            }).then(function successCallback(data) {


                /*POPUP HERE*/

                $scope.vacationRequesttoShow=data.data;
                $scope.loadManagers(data.data);
                $scope.loadVrOwnerData(data.data);
                $scope.loadSubstitues(data.data);
                $scope.loadComments(data.data);
                $scope.loadHistory(data.data);

                $scope.showResume = true;

                /*POPUP HERE*/

                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

        $scope.loadUserCalendars = function() {
            $http({
                url : rhContainer.jzURL('RHRequestManagementController.getUserCalendars')
            }).then(function successCallback(data) {
                $scope.userCalendars = data.data;
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };


        $scope.loadSubstitues = function(vacationRequest) {
            $http({
                data : vacationRequest,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhContainer.jzURL('RHRequestManagementController.getSubstitutesByRequestID')
            }).then(function successCallback(data) {
                $scope.vrsubs = data.data;
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };

        $scope.loadManagers = function(vacationRequest) {
            $http({
                data : vacationRequest,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhContainer.jzURL('RHRequestManagementController.getValidatorsByRequestID')
            }).then(function successCallback(data) {
                $scope.vrmanagers = data.data;
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };


        $scope.loadVrOwnerData = function(vacationRequest) {
            $http({
                data : vacationRequest,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhContainer.jzURL('RHRequestManagementController.getVrOwnerData')
            }).then(function successCallback(data) {
                $scope.vrOwnerData = data.data;
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };


        $scope.loadComments = function(vacationRequest) {
            $http({
                data : vacationRequest,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhContainer.jzURL('RHRequestManagementController.getComments')
            }).then(function successCallback(data) {
                $scope.comments = data.data;
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };

        $scope.loadHistory = function(vacationRequest) {
            $http({
                data : vacationRequest,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhContainer.jzURL('RHRequestManagementController.getHistory')
            }).then(function successCallback(data) {
                $scope.history = data.data;
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };


        $scope.saveComment = function() {
            if($scope.newComment.commentText){
                $scope.showAlert = false;
                $scope.setResultMessage($scope.i18n.savingVacationRequest, "info");


                $scope.newComment.requestId=$scope.vacationRequesttoShow.id;
                $scope.newComment.postedTime= Date.now();
                $scope.newComment.posterId=$scope.currentUser;
                $scope.newComment.posterAvatar=$scope.currentUserAvatar;
                $scope.newComment.posterName=$scope.currentUserName;

                $http({
                    data : $scope.newComment,
                    method : 'POST',
                    headers : {
                        'Content-Type' : 'application/json'
                    },
                    url : rhContainer.jzURL('RHRequestManagementController.saveComment')
                }).then(function successCallback(data) {

                    $scope.comments.push($scope.newComment);
                    $scope.newComment = {
                        id : null
                    };
                    $scope.showAlert = false;
                }, function errorCallback(data) {
                    $scope.setResultMessage($scope.i18n.defaultError, "error");
                });
            }else{
                $scope.setResultMessage($scope.i18n.emptyComment, "error");
            }
        }

        $scope.approveRequest = function(vacationRequest) {
            $http({
                data : vacationRequest,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhContainer.jzURL('RHRequestManagementController.approveRequest')
            }).then(function successCallback(data) {
                $scope.loadVacationRequestsToValidate();
                $http({
                    method : 'GET',
                    url : rhContainer.jzURL('RHRequestManagementController.getVacationRequest')+ "&id=" +$scope.vacationRequesttoShow.id
                }).then(function successCallback(data) {
                    $scope.showVacationRequest(data.data);
                    $scope.showAlert = false;
                    calendar.refresh('myCalendar');
                }, function errorCallback(data) {
                    $scope.setResultMessage($scope.i18n.defaultError, "error");
                });

            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

        $scope.declineRequest = function(vacationRequest) {
            $http({
                data : vacationRequest,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhContainer.jzURL('RHRequestManagementController.declineRequest')
            }).then(function successCallback(data) {
                $scope.loadVacationRequestsToValidate();
                $http({
                    method : 'GET',
                    url : rhContainer.jzURL('RHRequestManagementController.getVacationRequest')+ "&id=" +$scope.vacationRequesttoShow.id
                }).then(function successCallback(data) {
                    $scope.showVacationRequest(data.data);
                    $scope.showAlert = false;
                    calendar.refresh('myCalendar');
                }, function errorCallback(data) {
                    $scope.setResultMessage($scope.i18n.defaultError, "error");
                });
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.getUsers = function(id) {
            var data = $("#"+id).val().split(",");
            return data;
        }

        // function which set the result message with the given style
        $scope.setResultMessage = function(text, type) {
            $scope.resultMessageClass = "alert-" + type;
            $scope.resultMessageClassExt = "uiIcon" + type.charAt(0).toUpperCase()
                + type.slice(1);
            $scope.showAlert = true;
            $scope.resultMessage = text;
        }

        $scope.validateVacationRequestForm = function(vacationRequest) {
            return true;
        };


        $scope.uploadFiles = function(file, errFiles) {
            $scope.f = file;
            $scope.errFile = errFiles && errFiles[0];
            if (file) {
                file.upload = Upload.upload({
                    url: rhContainer.jzURL('RHRequestManagementController.uploadFile'),
                    data: {requestId: $scope.vacationRequesttoShow.id,
                        file: file}
                });

                file.upload.then(function (response) {
                    $timeout(function () {
                        file.result = response.data;


                        $scope.attachements = $scope.loadAttachments($scope.vacationRequesttoShow);
                        file.progress =undefined;

                    });
                }, function (response) {
                    if (response.status > 0)
                        $scope.errorMsg = response.status + ': ' + response.data;
                }, function (evt) {
                    file.progress = Math.min(100, parseInt(100.0 *
                        evt.loaded / evt.total));
                });
            }
        }

        $scope.loadAttachments = function(vacationRequest) {
            $http({
                data : vacationRequest,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhContainer.jzURL('RHRequestManagementController.getRequestAttachements')
            }).then(function successCallback(data) {
                $scope.showAlert = false;
                $scope.attachements = data.data;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };



        $scope.deleteAttachement = function(fileName) {
            $http({
                url : rhContainer.jzURL('RHRequestManagementController.deleteFile')+"&requestId="+$scope.vacationRequesttoShow.id+"&fileName="+fileName
            }).then(function successCallback(data) {
                $scope.attachements = $scope.loadAttachments($scope.vacationRequesttoShow);

            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };


        $scope.geti18n = function(id) {

            for(var propt in $scope.i18n){
                if(id==propt) return $scope.i18n[propt];
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

        $scope.loadData = function() {
            var requestId = $scope.getUrlParameterByName('rid');
            var urlParam = "";
            if (typeof requestId !== 'undefined' && requestId !==null) {
                urlParam = "&rid=" +requestId ;
            }

            $http({
                method : 'GET',
                url : rhContainer.jzURL('RHRequestManagementController.getData')+ urlParam
            }).then(function successCallback(data) {
                $scope.currentUser=data.data.currentUser;
                $scope.currentUserAvatar = data.data.currentUserAvatar;
                $scope.currentUserName = data.data.currentUserName;
                $scope.employeesSpace = data.data.employeesSpace;
                $scope.sickBalance = data.data.sickBalance;
                $scope.holidaysBalance = data.data.holidaysBalance;
                $scope.hrId = data.data.hrId;
                $scope.insuranceId = data.data.insuranceId;
                $scope.socialSecNumber = data.data.socialSecNumber;
                $scope.subNumber = data.data.subNumber;
                if(data.data.vacationRequestsToShow!=null){
                    $scope.showVacationRequest(data.data.vacationRequestsToShow);
                    $scope.showInfoBox=false;
                    $scope.showFullReq = true;
                    $scope.showAlert = false;
                    $scope.showRequestsTab = true;
                }
                $scope.myVacationRequests = data.data.myVacationRequests;
                $scope.vacationRequestsToValidate = data.data.vacationRequestsToValidate;
                $scope.vacationSubsRequests = data.data.vacationSubsRequests;
                if(data.data.conventionalVacations!=null) {$scope.cVacations = data.data.conventionalVacations;}
                if(data.data.officialVacations!=null) {$scope.oVacations = data.data.officialVacations;}
                $scope.officialDays=data.data.officialDays;
                var rsetUrl="/rest/rhrequest/users/find?currentUser="+$scope.currentUser+"&spaceURL="+$scope.employeesSpace;
                invite.build('managers', rsetUrl,'choose user');
                invite.build('substitutes', rsetUrl,'choose user');
                $scope.loadUserCalendars();
                calendar.build('myCalendar');
                console.log(deferred.resolve(data));
                $scope.showAlert = false;
                deferred.resolve(data);
                $('#rhAddon').css('visibility', 'visible');
                $(".rhLoadingBar").remove()
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }




        $scope.refreshController = function() {
            try {
                $scope.$digest()
            } catch (excep) {
                // No need to display errors in console
            }
        };


		        $scope.updateDateFormat = function(date, time) {
            var dateSplit = date;
            dateSplit = dateSplit.split("-");
//            return Date.parse(dateSplit[1] + '-' + dateSplit[0] + '-' + dateSplit[2]);
            dateSplit = moment(dateSplit[2] + '-' + dateSplit[1] + '-' + dateSplit[0] + ' '+ time);

            return Date.parse(dateSplit);

        };

			$scope.formatDate = function(date) {
         var day = date.getDate();
          var monthIndex = date.getMonth();
          monthIndex=monthIndex+1;
          return day + '-' + monthIndex;
        };


        $scope.calculateDays = function(vr) {
            var j = 0;
            var offDays="";
            var datefrom = $scope.updateDateFormat($("#fromDate").val(), $("#fromTime option[value]:selected").text());
            var dateto = $scope.updateDateFormat($("#toDate").val(), $("#toTime option[value]:selected").text());

            var dateArray = new Array();
            var fromDate = new Date();
            fromDate.setTime(datefrom);
            var toDate = new Date();
            toDate.setTime(dateto);
            var currentDate = new Date();
            currentDate.setTime(datefrom);
            while (currentDate <= toDate) {
                    var isOff=isOffDay(currentDate);
                if(isOff){
                offDays=offDays+' '+ $scope.formatDate(currentDate)+',';
                }
                if (currentDate.getDay() != 6 && currentDate.getDay() != 0 && !isOff) {
                    j++;
                }
                currentDate.setDate(currentDate.getDate() + 1);
            }
            if (fromDate.getHours() > 12 && fromDate.getHours() < 19 ) j = j - 0.5;
            if (toDate.getHours() > 7 && toDate.getHours() < 15) j = j - 0.5;
            $scope.newVacationRequest.daysNumber = j;
            $(".nbrDays").text(j);
           if(offDays!=""){
           $(".oDays").text("( "+$scope.i18n.bankHolidays+": "+offDays+")");
           } else $(".oDays").text("");
        };

        function isOffDay(date) {
            if ($scope.officialDays.length == 0) {
                return false;
            } else {
                for (i = 0; i < $scope.officialDays.length; i++) {
                    var od = new Date();
                    od.setTime($scope.officialDays[i]);
                    if (sameDay(date, od)) return true;
                }

            }
            return false;
        }


        function sameDay(d1, d2) {
          return d1.getFullYear() === d2.getFullYear() &&  d1.getMonth() === d2.getMonth() && d1.getDate() === d2.getDate();
        }

        $scope.loadBundles();

        $( "#toDate, #fromDate, #fromTime, #toTime" ).on( "change", function() {
            if(($("#toDate").val() != "") && ($("#fromDate").val() != "") &&
            ($("#toTime option[value]:selected").text() != "") && ($("#toTime option[value]:selected").text() != "") ){$scope.calculateDays($scope.newVacationRequest);}
       });

    };
    return rhCtrl;


});