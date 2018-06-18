define("rhRequestAdminControllers", ["SHARED/jquery", "SHARED/juzu-ajax", "SHARED/userInvitation", "moment"], function ($, jz, invite, moment) {
    var rhRequestAdminCtrl = function ($scope, $q, $timeout, $http, $filter, PagerService, Upload) {
        var rhRequestAdminContainer = $('#rhRequestAdmin');
        var deferred = $q.defer();


        $scope.currentUser = "";
        $scope.settings = {};
        $scope.vRequestdet = null;
        $scope.currentUserAvatar = "";
        $scope.currentUserName = "";
        $scope.showAddForm = false;
        $scope.showEmployees = true;
        $scope.showElementDetail = false;
        $scope.getUserLigne = false;
        $scope.showCvacations = false;
        $scope.showOvacations = false;
        $scope.profilPreview = false;
        $scope.rhEmployees = [];
        $scope.vacationRequests = [];
        $scope.createNewRequest= false;
		$scope.cVacations = [];
		$scope.newConventionalVacation = {
            id: null
        };
		$scope.oVacations = [];
		$scope.newOfficialVacation = {
            id: null
        };
        $scope.currentPage = 0;
        $scope.pageSize = 10;
        $scope.vRCount=0;
        $scope.def = '';
        $scope.userDetails = {
            id: null
        };
        $scope.allVacationRequests = [];
        $scope.attachements = [];
        $scope.comments = [];
        $scope.balanceHistory = [];
        $scope.history = [];
        $scope.orderByField = 'title';
        $scope.reverseSort = false;
        $scope.rhEmployee = {};
        $scope.newUserId = "";
//        $scope.newUserDetails={
//            id : null
//        };
        $scope.showDetails = false;
        $scope.showAlert = false;

        $scope.newComment = {
            id: null
        };

        $scope.newVacationRequest = {id: null};

        $scope.vacationRequesttoShow = {
            validatorUserId: null
        };
        $scope.toDateShow;
        $scope.fromDateShow;


        $scope.templateVr = { name: 'templateVr',
                             url: 'templateVr'};

        $scope.templateUdata = { name: 'templateUdata',
                                  url: 'templateUdata'};



        $scope.itemsPerPage = 10;
        $scope.currentPage = 0;
        $scope.pages=[];

        $scope.vrFilter="active";
        $scope.userVrFilter="active";

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
                url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.getBundle') + "&locale=" + eXo.env.portal.language
            }).then(function successCallback(data) {
                $scope.i18n = data.data;
               // $scope.loadContext();
               loadVacationRequests('active',0,10);
                deferred.resolve(data);
                /*$scope.setResultMessage(data, "success");*/
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }



        $scope.loadUserHRData = function (userRhData) {

            $scope.userDetails = userRhData;
            if (!userRhData.avatar) {
                $scope.userDetails.avatar = "/eXoSkin/skin/images/system/UserAvtDefault.png";
            }


            /*here*/
            if($scope.userDetails.hrData.birthDay){
                var birthDay = new Date($scope.getLocaleDate($scope.userDetails.hrData.birthDay));
                $( "#birthDay" ).datepicker( "option", "defaultDate", birthDay );
                birthDay = birthDay.getDate() + '-' + (birthDay.getMonth() + 1) + '-' +  birthDay.getFullYear() ;
            }
            if($scope.userDetails.hrData.startDate){
                var startDate = new Date($scope.getLocaleDate($scope.userDetails.hrData.startDate));
                $( "#startDate" ).datepicker( "option", "defaultDate", startDate );
                startDate = startDate.getDate() + '-' + (startDate.getMonth() + 1) + '-' +  startDate.getFullYear();
            }
            if($scope.userDetails.hrData.leaveDate){
                var leaveDate = new Date($scope.getLocaleDate($scope.userDetails.hrData.leaveDate));
                $( "#leaveDate" ).datepicker( "option", "defaultDate", leaveDate );
                leaveDate = leaveDate.getDate() + '-' + (leaveDate.getMonth() + 1) + '-' +  leaveDate.getFullYear();
            }
            if($scope.userDetails.hrData.contractStartDate){
                var contractStartDate = new Date($scope.getLocaleDate($scope.userDetails.hrData.contractStartDate));
                $( "#contractStartDate" ).datepicker( "option", "defaultDate", contractStartDate );
                contractStartDate = contractStartDate.getDate() + '-' + (contractStartDate.getMonth() + 1) + '-' +  contractStartDate.getFullYear();
            }
            if($scope.userDetails.hrData.contractEndDate){
                var contractEndDate = new Date($scope.getLocaleDate($scope.userDetails.hrData.contractEndDate));
                $( "#contractEndDate" ).datepicker( "option", "defaultDate", contractEndDate );
                contractEndDate = contractEndDate.getDate() + '-' + (contractEndDate.getMonth() + 1) + '-' +  contractEndDate.getFullYear();
            }


            $scope.loadVacationRequestsbyUserId(userRhData.userId,$scope.userVrFilter);
            $scope.loadAttachments(userRhData);


            /**/

                $( "#birthDay" ).val(birthDay);
                $( "#startDate" ).val(startDate);
                $( "#leaveDate" ).val(leaveDate);
                $( "#contractStartDate" ).val(contractStartDate);
                $( "#contractEndDate" ).val(contractEndDate);
            /**/

        };

        $scope.loadUserPreview = function (userRhData) {
            $scope.userDetails = userRhData;
            if (!userRhData.avatar) {
                $scope.userDetails.avatar = "/eXoSkin/skin/images/system/UserAvtDefault.png";
            }

            /*here*/
            if($scope.userDetails.hrData.birthDay){
                var birthDay = new Date($scope.getLocaleDate($scope.userDetails.hrData.birthDay));
                birthDay = birthDay.getDate() + '-' + (birthDay.getMonth() + 1) + '-' +  birthDay.getFullYear() ;
            }
            if($scope.userDetails.hrData.startDate){
                var startDate = new Date($scope.getLocaleDate($scope.userDetails.hrData.startDate));
                startDate = startDate.getDate() + '-' + (startDate.getMonth() + 1) + '-' +  startDate.getFullYear();
            }
            if($scope.userDetails.hrData.leaveDate){
                var leaveDate = new Date($scope.getLocaleDate($scope.userDetails.hrData.leaveDate));
                leaveDate = leaveDate.getDate() + '-' + (leaveDate.getMonth() + 1) + '-' +  leaveDate.getFullYear();
            }
            if($scope.userDetails.hrData.contractStartDate){
                var contractStartDate = new Date($scope.getLocaleDate($scope.userDetails.hrData.contractStartDate));
                contractStartDate = contractStartDate.getDate() + '-' + (contractStartDate.getMonth() + 1) + '-' +  contractStartDate.getFullYear();
            }
            if($scope.userDetails.hrData.contractEndDate){
                var contractEndDate = new Date($scope.getLocaleDate($scope.userDetails.hrData.contractEndDate));
                contractEndDate = contractEndDate.getDate() + '-' + (contractEndDate.getMonth() + 1) + '-' +  contractEndDate.getFullYear();
            }


            /**/
            if(birthDay == undefined) $( "#birthDayPreview" ).text("");
            else $( "#birthDayPreview" ).text(birthDay);

            if(startDate == undefined) $( "#startDatePreview" ).text("");
            else $( "#startDatePreview" ).text(startDate);


             if(leaveDate == undefined) $( "#leaveDatePreview" ).text("");
             else $( "#leaveDatePreview" ).text(leaveDate);

             if(contractStartDate == undefined) $( "#contractStartDatePreview" ).text("");
             else $( "#contractStartDatePreview" ).text(contractStartDate);

             if(contractEndDate == undefined) $( "#contractEndDatePreview" ).text("");
             else $( "#contractEndDatePreview" ).text(contractEndDate);
            /**/
          //  $scope.profilPreview = true;
        };

        $scope.loadVacationRequestsbyUserId = function (userId,userVrFilter) {
            $http({
                method: 'GET',
                url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.getVacationRequestsbyUserId') + "&userId=" + userId+ "&vrFilter=" + userVrFilter
            }).then(function successCallback(data) {

                $scope.vacationRequests = data.data;
                $scope.showAddForm = true;


            }, function errorCallback(data) {
                $scope.showAddForm = false;
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.getUser = function (newUserId) {

            // $scope.getEmployees($scope.currentUser, newUserId);

            if (newUserId) {
                $("#getUser").removeClass("invalid");
                $http({
                    method: 'GET',
                    url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.getUser') + "&userId=" + newUserId
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


        $scope.loadVacationRequests = function (vrFilter,offset,limit) {
            var url="";
            if(status!=null){
                url="&vrFilter="+vrFilter;
            }
            $http({
                method: 'GET',
                url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.getVacationRequests')+url+ "&offset="+offset+ "&limit="+limit
            }).then(function successCallback(data) {
                $scope.allVacationRequests = data.data.vacationRequests;
                $scope.vRCount = data.data.size;
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };

        $scope.editUserRhData = function (modifiedEmplyee) {
            $scope.rhEmployee = modifiedEmplyee;
        }

        $scope.loadActivVacationRequests = function () {
            $http({
                method: 'GET',
                url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.getActivVacationRequests')
            }).then(function successCallback(data) {
                $scope.allVacationRequests = data.data;
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };

        $scope.editUserRhData = function (modifiedEmplyee) {
            $scope.rhEmployee = modifiedEmplyee;
        }


        $scope.validateRequest = function (vacationRequest, user) {
            $http({
                data: vacationRequest,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.validateRequest')
            }).then(function successCallback(data) {
                if (user == null) {
                    $scope.loadVacationRequests($scope.vrFilter,$scope.currentPage*$scope.itemsPerPage, $scope.itemsPerPage);
                } else {
                    if (data.data != null) {
                        user.hrData = data.data;
                    }
                    $scope.loadUserHRData(user);
                }
//                $scope.setResultMessage(data, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

        $scope.cancelRequest = function (vacationRequest, user) {
            $http({
                data: vacationRequest,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.cancelRequest')
            }).then(function successCallback(data) {
                if (user == null) {
                    $scope.loadVacationRequests($scope.vrFilter,$scope.currentPage*$scope.itemsPerPage, $scope.itemsPerPage);
                } else {
                    if (data.data != null) {
                        user.hrData = data.data;
                    }
                    $scope.loadUserHRData(user);
                }
//                $scope.setResultMessage(data, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.openTab = function (tabName) {

            $("#employees").css("display", "none");
            $("#requests").css("display", "none");
            $("#history").css("display", "none");
			$("#settings").css("display", "none");

            $("#employeesTab").removeClass("active");
            $("#requestsTab").removeClass("active");
            $("#historyTab").removeClass("active");
			$("#settingsTab").removeClass("active");

            $("#" + tabName).css("display", "block");
            $("#" + tabName + "Tab").addClass("active");
            $scope.showDetails=false;
            $scope.showAddForm =false;
        }

        $scope.enableInput = function (id) {
            $(id).removeAttr("readonly");
            $("#submit").css("display", "block");
            $(".savebtnAdmin").removeClass("hidden");
        }

        $scope.disableInput = function (id) {
            $(id).attr("readonly", "true");
            $(".savebtnAdmin").addClass("hidden");
        }

        $scope.uploadFiles = function (file, errFiles) {
            $scope.f = file;
            $scope.errFile = errFiles && errFiles[0];
            if (file) {
                file.upload = Upload.upload({
                    url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.uploadFile'),
                    data: {
                        userId: $scope.userDetails.userId,
                        file: file
                    }
                });
                file.upload.then(function (response) {
                    $timeout(function () {
                        file.result = response.data;
                        $scope.attachements = $scope.loadAttachments($scope.userDetails);
                        file.progress = undefined;

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

        $scope.loadAttachments = function (userDetails) {
            $http({
                data: userDetails,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.getEmployeeAttachements')
            }).then(function successCallback(data) {
                $scope.attachements = data.data;
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };

        $scope.deleteAttachement = function (fileName) {
            $http({
                url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.deleteFile') + "&userId=" + $scope.userDetails.userId + "&fileName=" + fileName
            }).then(function successCallback(data) {
                $scope.attachements = $scope.loadAttachments($scope.userDetails);
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };


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

        $scope.loadSubstitues = function (vacationRequest) {
            $http({
                data: vacationRequest,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.getSubstitutesByRequestID')
            }).then(function successCallback(data) {
                $scope.vrsubs = data.data;
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };

        $scope.loadManagers = function (vacationRequest) {
            $http({
                data: vacationRequest,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.getValidatorsByRequestID')
            }).then(function successCallback(data) {


                $scope.vrmanagers = data.data;
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };


        $scope.loadComments = function (vacationRequest) {
            $http({
                data: vacationRequest,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.getComments')
            }).then(function successCallback(data) {
                $scope.comments = data.data;
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };


        $scope.saveComment = function () {

//            $scope.setResultMessage($scope.i18n.savingVacationRequest, "info");
            if ($scope.newComment.commentText) {
                $scope.showAlert = false;
                $scope.newComment.requestId = $scope.vacationRequesttoShow.id;
                $scope.newComment.postedTime = Date.now();
                $scope.newComment.posterId = $scope.currentUser;
                $scope.newComment.posterAvatar = $scope.currentUserAvatar;
                $scope.newComment.posterName = $scope.currentUserName;

                $http({
                    data: $scope.newComment,
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.saveComment')
                }).then(function successCallback(data) {

                    //                $scope.setResultMessage(data, "success");
                    $scope.comments.push($scope.newComment);
                    $scope.newComment = {
                        id: null
                    };
                    //                $timeout(function() {
                    //                    $scope.setResultMessage(data, "success")
                    //                }, 1000);
                }, function errorCallback(data) {
                    $scope.setResultMessage($scope.i18n.defaultError, "error");
                });
            } else {
                $scope.setResultMessage($scope.i18n.emptyComment, "error");
            }
        }


        $scope.loadHistory = function (vacationRequest) {
            $http({
                data: vacationRequest,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.getHistory')
            }).then(function successCallback(data) {
                $scope.history = data.data;
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };


        $scope.loadRequestAttachments = function (vacationRequest) {
            $http({
                data: vacationRequest,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.getRequestAttachements')
            }).then(function successCallback(data) {
                $scope.attachements = data.data;
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };

        $scope.dateFormat = function (date) {
            var newDate = new Date(date);
            newDate = {
                date: newDate.getDate() + "-" + (newDate.getMonth() + 1) +"-"+ newDate.getFullYear(),
                time: newDate.getHours()+":"+newDate.getMinutes()
            };
            return newDate;
        };

        $scope.showRequest = function (vacationRequest) {
            $scope.loadVacationRequests('active',0,10);
            $scope.openTab('requests');
            $scope.showVacationRequest(vacationRequest);
        };
        $scope.showVacationRequest = function (vacationRequest) {
            $scope.vacationRequesttoShow = vacationRequest;

            $scope.toDateShow = $scope.dateFormat(vacationRequest.toDate);
            $scope.fromDateShow= $scope.dateFormat(vacationRequest.fromDate);

            $scope.loadManagers(vacationRequest);
            $scope.loadSubstitues(vacationRequest);
            $scope.loadRequestAttachments(vacationRequest);
            $scope.loadComments(vacationRequest);
            $scope.loadHistory(vacationRequest);
           // $scope.showDetails = true;
            $(".table-striped tbody tr").removeClass("selected");
            $(".request_"+vacationRequest.id).addClass("selected");
        };

        $scope.CloseVacationRequest = function (vacationRequest) {
            $scope.showDetails = false;
            $(".table-striped tbody tr").removeClass("selected");
        };

        $scope.loadContext = function () {
            $http({
                method: 'GET',
                url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.getContext')
            }).then(function successCallback(data) {
                $scope.currentUser = data.data.currentUser;
                $scope.currentUserAvatar = data.data.currentUserAvatar;
                $scope.currentUserName = data.data.currentUserName;
                $scope.settings.rhManager=data.data.rhManager;
               // $scope.vRCount = data.data.vRCount;
                $scope.loadEmployees('active');
				$scope.loadConventionalVacations();
				$scope.loadOfficialVacations();
                deferred.resolve(data);
//                $scope.setResultMessage(data, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

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
                    url : rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.getVacationRequest')+ "&id=" +requestId
                }).then(function successCallback(data) {
                    if(data.data==""){
                        $scope.setResultMessage($scope.i18n.requestNotFound, "error");
                    }else{
                        $scope.openTab('requests', 'employees')
                        $scope.showVacationRequest(data.data);
                    }
                }, function errorCallback(data) {
                    $scope.setResultMessage($scope.i18n.defaultError, "error");
                });
            }
        }



        $scope.closeFormAdd = function () {
            $scope.showAddForm = false;
            $("#requestsList .elementDetail input").attr("readonly", "true");
            $("#requestsList .elementDetail input[type=submit], #requestsList .elementDetail input[type=cancel]").removeAttr("readonly");
        }



        $scope.range = function() {
            var rangeSize = 10;
            var ret = [];
            var start=0;
            var pgCount=$scope.pageCount();
            if(pgCount>rangeSize){
                var d = $scope.currentPage-Math.ceil(rangeSize/2);
                if(d>0) start=d;
            }
            var end=start+rangeSize;
            if(end>pgCount) end=pgCount;
            for (var i=start; i<end; i++) {
                ret.push(i);
            }
            return ret;
        };

        $scope.prevPage = function() {
            if ($scope.currentPage > 0) {
                $scope.currentPage--;
                $scope.pages=$scope.range();
                $scope.loadVacationRequests($scope.vrFilter,$scope.currentPage*$scope.itemsPerPage, $scope.itemsPerPage);
            }
        };

        $scope.prevPageDisabled = function() {
            return $scope.currentPage === 0 ? "disabled" : "";
        };

        $scope.nextPage = function() {
            if ($scope.currentPage < $scope.pageCount() - 1) {
                $scope.currentPage++;
                $scope.pages=$scope.range();
                $scope.loadVacationRequests($scope.vrFilter,$scope.currentPage*$scope.itemsPerPage, $scope.itemsPerPage);
            }
        };

        $scope.nextPageDisabled = function() {
            return $scope.currentPage === $scope.pageCount() - 1 ? "disabled" : "";
        };

        $scope.pageCount = function() {
            return Math.ceil($scope.vRCount/$scope.itemsPerPage);
        };

        $scope.setPage = function(n) {
            if (n >= 0 && n < $scope.pageCount()) {
                $scope.currentPage = n;
                $scope.pages=$scope.range();
                $scope.loadVacationRequests($scope.vrFilter,n*$scope.itemsPerPage, $scope.itemsPerPage)
            }
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

        $scope.updateDateFormat = function(date, time) {
            var dateSplit = date;
            dateSplit = dateSplit.split("-");
//            return Date.parse(dateSplit[1]+'-'+dateSplit[0]+'-'+dateSplit[2]);

            dateSplit = moment(dateSplit[2] + '-' + dateSplit[1] + '-' + dateSplit[0] + ' '+ time);
            return Date.parse(dateSplit);
        };

        $scope.updateVacationRequest = function (vacation) {
            var datefrom = $scope.updateDateFormat($("#fromDateAdmin").val(), $("#fromTime option[value]:selected").text());
            var dateto = $scope.updateDateFormat($("#toDateAdmin").val(), $("#toTime option[value]:selected").text());

            if(vacation.daysNumber < 0){
                $scope.setResultMessage($scope.i18n.nbrDate, "error");
                $("#daysNumber").addClass("ng-invalid");
                $scope.scrollTo();
            }else if(datefrom > dateto){
                $("#daysNumber").removeClass("ng-invalid");
                $scope.setResultMessage($scope.i18n.fromToSup, "error");
                $("#fromDateAdmin, #toDateAdmin").addClass("ng-invalid");
                $scope.scrollTo();
            }else {

                $("#fromDateAdmin, #toDateAdmin").removeClass("ng-invalid");
                vacation.fromDate = datefrom;
                vacation.toDate = dateto;
                $http({
                    data: vacation,
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.updateVacationRequest')
                }).then(function successCallback(data) {
                    $scope.setResultMessage($scope.i18n.requestUpdated, "success");
                    $scope.disableInput("#daysNumber, #fromDateAdmin, #fromDateAdmin");
                    $scope.scrollTo();

                    $timeout(function () {
                        $scope.showAlert = false;
                    }, 2000);
                }, function errorCallback(data) {
                    $scope.setResultMessage($scope.i18n.defaultError, "error");
                });
            }
        };


                $scope.saveVacationRequest = function (vacation) {
                    var datefrom = $scope.updateDateFormat($("#fromDateRequest").val(), $("#fromTimeRequest option[value]:selected").text());
                    var dateto = $scope.updateDateFormat($("#toDateRequest").val(), $("#toTimeRequest option[value]:selected").text());

                    if(vacation.daysNumber < 0){
                        $scope.setResultMessage($scope.i18n.nbrDate, "error");
                        $("#daysNumber").addClass("ng-invalid");
                        $scope.scrollTo();
                    }else if(datefrom > dateto){
                        $("#daysNumber").removeClass("ng-invalid");
                        $scope.setResultMessage($scope.i18n.fromToSup, "error");
                        $("#fromDateAdmin, #toDateAdmin").addClass("ng-invalid");
                        $scope.scrollTo();
                    }else {

                        $("#fromDateRequest, #toDateRequest").removeClass("ng-invalid");
                        vacation.fromDate = datefrom;
                        vacation.toDate = dateto;

                        vacation.userId=$scope.userDetails.userId;
                        vacation.userFullName=$scope.userDetails.name
                        $http({
                            data: vacation,
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            url: rhRequestAdminContainer.jzURL('RhRequestsAdministrationController.saveVacationRequest')
                        }).then(function successCallback(data) {
                            $scope.setResultMessage($scope.i18n.requestCreated, "success");
                            $scope.disableInput("#daysNumber, #fromDateRequest, #fromDateRequest");
                            $scope.scrollTo();

                            $timeout(function () {
                                $scope.showAlert = false;
                            }, 2000);
                        }, function errorCallback(data) {
                            $scope.setResultMessage($scope.i18n.defaultError, "error");
                        });
                    }
                };


        function sameDay(d1, d2) {
          return d1.getFullYear() === d2.getFullYear() &&  d1.getMonth() === d2.getMonth() && d1.getDate() === d2.getDate();
        }

    $scope.setVr = function(vRequest) {
        $scope.vacationRequesttoShow = vRequest;
    }

        $scope.setRhData = function(userRhData) {
            $scope.userDetails = userRhData;
        }

        $scope.loadBundles();
        $scope.showRequestfromUrl();

       // $scope.loadContext();
       // $scope.loadEmployees();

        $('#rhRequestAdmin').css('visibility', 'visible');
        $("#rhLoadingBar").remove();
    };
    return rhRequestAdminCtrl;

    /*
     $timeout(function() {
     $scope.showAlert = false;
     }, 2000);
     */
});
