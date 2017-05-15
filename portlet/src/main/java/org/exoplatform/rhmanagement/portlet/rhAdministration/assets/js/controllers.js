define("rhAdminAddonControllers", ["SHARED/jquery", "SHARED/juzu-ajax", "SHARED/userInvitation"], function ($, jz, invite, calendar) {
    var rhAdminCtrl = function ($scope, $q, $timeout, $http, $filter, PagerService, Upload) {
        var rhAdminContainer = $('#rhAdminAddon');
        var deferred = $q.defer();


        $scope.currentUser = "";
        $scope.currentUserAvatar = "";
        $scope.currentUserName = "";
        $scope.showAddForm = false;
        $scope.showEmployees = true;
        $scope.showElementDetail = false;
        $scope.getUserLigne = false;
        $scope.rhEmployees = [];
        $scope.vacationRequests = [];
        $scope.currentPage = 0;
        $scope.pageSize = 10;
        $scope.def = '';
        $scope.userDetails = {
            id: null
        };
        $scope.allVacationRequests = [];
        $scope.attachements = [];
        $scope.comments = [];
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


        $scope.vacationRequesttoShow = {
            validatorUserId: null
        };

        $scope.vm = this;
        $scope.vm.pager = {};
        $scope.vm.setPage = setPage;

        $scope.vrFilter="active";
        $scope.userVrFilter="active";

        $scope.initController = function () {
            // initialize to page 1
            $scope.vm.setPage(1);
        }

        function setPage(page) {
            var rhEmployees = $filter('filter')($scope.rhEmployees, $scope.def)

            // get pager object from service
            $scope.vm.pager = PagerService.GetPager(rhEmployees.length, page);

            if (page < 1 || page > $scope.vm.pager.totalPages) {
                return;
            }
            // get current page of items
            $scope.vm.items = rhEmployees.slice($scope.vm.pager.startIndex, $scope.vm.pager.endIndex + 1);
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
                url: rhAdminContainer.jzURL('RhAdministrationController.getBundle') + "&locale=" + eXo.env.portal.language
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


        $scope.loadEmployees = function () {
            $http({
                method: 'GET',
                url: rhAdminContainer.jzURL('RhAdministrationController.getAllUsersRhData')
            }).then(function successCallback(data) {
                $scope.rhEmployees = data.data;
                //$scope.vm.setPage(1);

                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });

            // return $filter('filter')($scope.rhEmployees, $scope.def)
        };

        $scope.loadUserHRData = function (userRhData) {

            $scope.userDetails = userRhData;
            if (!userRhData.avatar) {
                $scope.userDetails.avatar = "/eXoSkin/skin/images/system/UserAvtDefault.png";
            }


            /*here*/
            if($scope.userDetails.hrData.birthDay){
                var birthDay = new Date($scope.getLocaleDate($scope.userDetails.hrData.birthDay));

                $( "#birthDay" ).datepicker( "option", "defaultDate", birthDay );
                $( "#birthDay" ).val(birthDay.getDate() + '-' + (birthDay.getMonth() + 1) + '-' +  birthDay.getFullYear());
            }
            if($scope.userDetails.hrData.startDate){
                var startDate = new Date($scope.getLocaleDate($scope.userDetails.hrData.startDate));
                $( "#startDate" ).datepicker( "option", "defaultDate", startDate );
                $( "#startDate" ).val(startDate.getDate() + '-' + (startDate.getMonth() + 1) + '-' +  startDate.getFullYear());
            }
            if($scope.userDetails.hrData.leaveDate){
                var leaveDate = new Date($scope.getLocaleDate($scope.userDetails.hrData.leaveDate));
                $( "#leaveDate" ).datepicker( "option", "defaultDate", leaveDate );
                $( "#leaveDate" ).val(leaveDate.getDate() + '-' + (leaveDate.getMonth() + 1) + '-' +  leaveDate.getFullYear());
            }
            if($scope.userDetails.hrData.contractStartDate){
                var contractStartDate = new Date($scope.getLocaleDate($scope.userDetails.hrData.contractStartDate));
                $( "#contractStartDate" ).datepicker( "option", "defaultDate", contractStartDate );
                $( "#contractStartDate" ).val(contractStartDate.getDate() + '-' + (contractStartDate.getMonth() + 1) + '-' +  contractStartDate.getFullYear());
            }
            if($scope.userDetails.hrData.contractEndDate){
                var contractEndDate = new Date($scope.getLocaleDate($scope.userDetails.hrData.contractEndDate));
                $( "#contractEndDate" ).datepicker( "option", "defaultDate", contractEndDate );
                $( "#contractEndDate" ).val(contractEndDate.getDate() + '-' + (contractEndDate.getMonth() + 1) + '-' +  contractEndDate.getFullYear());
            }


            $scope.loadVacationRequestsbyUserId(userRhData.userId,$scope.userVrFilter);
            $scope.loadAttachments(userRhData);

        };


        $scope.loadVacationRequestsbyUserId = function (userId,userVrFilter) {
            $http({
                method: 'GET',
                url: rhAdminContainer.jzURL('RhAdministrationController.getVacationRequestsbyUserId') + "&userId=" + userId+ "&vrFilter=" + userVrFilter
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
                    url: rhAdminContainer.jzURL('RhAdministrationController.getUser') + "&userId=" + newUserId
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


        $scope.loadVacationRequests = function (vrFilter) {
            var url="";
            if(status!=null){
                url="&vrFilter="+vrFilter;
            }
            $http({
                method: 'GET',
                url: rhAdminContainer.jzURL('RhAdministrationController.getVacationRequests')+url
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

        $scope.loadActivVacationRequests = function () {
            $http({
                method: 'GET',
                url: rhAdminContainer.jzURL('RhAdministrationController.getActivVacationRequests')
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


        $scope.saveUserHRData = function (employee) {
            if ($("#birthDay").val() !== "") {
                var date = ($("#birthDay").val()).split("-");
                employee.hrData.birthDay = new Date(date[2]+'-'+date[1]+'-'+date[0]);
            }
            if ($("#startDate") !== "") {
                var date = ($("#startDate").val()).split("-");
                employee.hrData.startDate = new Date(date[2]+'-'+date[1]+'-'+date[0]);
            }
            if ($("#leaveDate").val() !== "") {
                var date = ($("#leaveDate").val()).split("-");
                employee.hrData.leaveDate = new Date(date[2]+'-'+date[1]+'-'+date[0]);
            }
            if ($("#contractStartDate").val() !== "") {
                var date = ($("#contractStartDate").val()).split("-");
                employee.hrData.contractStartDate = new Date(date[2]+'-'+date[1]+'-'+date[0]);
            }
            if ($("#contractEndDate").val() !== "") {
                var date = ($("#contractEndDate").val()).split("-");
                employee.hrData.contractEndDate = new Date(date[2]+'-'+date[1]+'-'+date[0]);
            }

            $http({
                data: employee,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: rhAdminContainer.jzURL('RhAdministrationController.saveUserRHData')
            }).then(function successCallback(data) {
                employee.id = data.data.id;


                $("#requestsList .elementDetail input").attr("readonly", "true");
                $("#requestsList .elementDetail input[type=submit], #requestsList .elementDetail input[type=cancel]").removeAttr("readonly");

                /* $scope.setResultMessage($scope.i18n.userUpdate, "success");*/
                $scope.loadEmployees();
                $scope.getUser(employee.id);
                $scope.showAddForm = false;

            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.deleteUserHRData = function (employee) {
            $http({
                data: employee,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: rhAdminContainer.jzURL('RhAdministrationController.deleteUserRHData')
            }).then(function successCallback(data) {
                $scope.loadEmployees();
                $scope.setResultMessage($scope.i18n.Userdelete, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

        $scope.validateRequest = function (vacationRequest, user) {
            $http({
                data: vacationRequest,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: rhAdminContainer.jzURL('RhAdministrationController.validateRequest')
            }).then(function successCallback(data) {
                if (user == null) {
                    $scope.loadVacationRequests($scope.vrFilter);
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
                url: rhAdminContainer.jzURL('RhAdministrationController.cancelRequest')
            }).then(function successCallback(data) {
                if (user == null) {
                    $scope.loadVacationRequests($scope.vrFilter);
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


        $scope.openTab = function (tabName, tabHide) {
            $("#" + tabName).css("display", "block");
            $("#" + tabHide).css("display", "none");
            $("#" + tabName + "Tab").addClass("active");
            $("#" + tabHide + "Tab").removeClass("active");
            $scope.showDetails=false;
            $scope.showAddForm =false;
        }

        $scope.enableInput = function (id) {
            $(id).removeAttr("readonly");
            $("#submit").css("display", "block");
        }


        $scope.uploadFiles = function (file, errFiles) {
            $scope.f = file;
            $scope.errFile = errFiles && errFiles[0];
            if (file) {
                file.upload = Upload.upload({
                    url: rhAdminContainer.jzURL('RhAdministrationController.uploadFile'),
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
                url: rhAdminContainer.jzURL('RhAdministrationController.getEmployeeAttachements')
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
                url: rhAdminContainer.jzURL('RhAdministrationController.deleteFile') + "&userId=" + $scope.userDetails.userId + "&fileName=" + fileName
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
                url: rhAdminContainer.jzURL('RhAdministrationController.getSubstitutesByRequestID')
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
                url: rhAdminContainer.jzURL('RhAdministrationController.getValidatorsByRequestID')
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
                url: rhAdminContainer.jzURL('RhAdministrationController.getComments')
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
                    url: rhAdminContainer.jzURL('RhAdministrationController.saveComment')
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
                url: rhAdminContainer.jzURL('RhAdministrationController.getHistory')
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
                url: rhAdminContainer.jzURL('RhAdministrationController.getRequestAttachements')
            }).then(function successCallback(data) {
                $scope.attachements = data.data;
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };

        $scope.showVacationRequest = function (vacationRequest) {
            $scope.vacationRequesttoShow = vacationRequest;
            $scope.loadManagers(vacationRequest);
            $scope.loadSubstitues(vacationRequest);
            $scope.loadRequestAttachments(vacationRequest);
            $scope.loadComments(vacationRequest);
            $scope.loadHistory(vacationRequest);
            $scope.showDetails = true;
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
                url: rhAdminContainer.jzURL('RhAdministrationController.getContext')
            }).then(function successCallback(data) {
                $scope.currentUser = data.data.currentUser;
                $scope.currentUserAvatar = data.data.currentUserAvatar;
                $scope.currentUserName = data.data.currentUserName;
                $scope.loadEmployees();
                deferred.resolve(data);
//                $scope.setResultMessage(data, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

        $scope.getEmployees = function (currentUser, searchUser = null) {
            var rsetUrl = "/rest/rhrequest/users/find?currentUser=" + currentUser + "&spaceURL=exo_employees&nameToSearch=" + searchUser;//+$scope.employeesSpace;

            $http({
                method: 'GET',
                url: rsetUrl
            }).then(function successCallback(data) {

                /* create a table of users IDs*/
                $("#newUserName").autocomplete({
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
                        $("#newUserName").val(ui.item.fullName);
                        $scope.getUser(ui.item.value);
                        return false;
                    },
                    select: function (event, ui) {
                        $("#newUserName").val(ui.item.fullName);
                        $scope.getUser(ui.item.value);
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
                    url : rhAdminContainer.jzURL('RhAdministrationController.getVacationRequest')+ "&id=" +requestId
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

        $scope.loadBundles();
        $scope.showRequestfromUrl();
       // $scope.loadContext();
       // $scope.loadEmployees();

        $('#rhAdminAddon').css('visibility', 'visible');
        $(".rhLoadingBar").remove();
    };
    return rhAdminCtrl;

    /*
     $timeout(function() {
     $scope.showAlert = false;
     }, 2000);
     */
});
