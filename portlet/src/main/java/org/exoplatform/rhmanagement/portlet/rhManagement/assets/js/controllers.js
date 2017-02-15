define("rhAddonControllers", [ "SHARED/jquery", "SHARED/juzu-ajax","SHARED/userInvitation","calendar"], function($, jz,invite,calendar)  {
    var rhCtrl = function($scope, $q, $timeout, $http, $filter, Upload) {
        var rhContainer = $('#rhAddon');
        var deferred = $q.defer();


        $scope.currentUser="";
        $scope.sickBalance="";
        $scope.holidaysBalance="";
        $scope.vacationRequestsToValidate = [];
        $scope.myVacationRequests = [];
        $scope.comments = [];
        $scope.userCalendars = [];
        $scope.userCalendarId = "";
        $scope.managerCalendarId = "";
        $scope.vrmanagers = [];
        $scope.vrsubs = [];
        $scope.calEvents = [];
        $scope.attachements = [];
        $scope.showForm = true;
        $scope.showDetails = false;
        $scope.showResume = false;
        $scope.showList = false;
        $scope.showCal = false;
        $scope.showSick = false;
        $scope.showHollidays = false;
        $scope.newVacationRequest = {
            id : null
        };

        $scope.newComment = {
            id : null
        };


        $scope.vacationRequesttoShow = {
            validatorUserId : null
        };

        $scope.showBalance= function(){
           if( $scope.newVacationRequest.type == 'holiday'){
               $scope.showSick=false;
               $scope.showHollidays=true;
           } else if( $scope.newVacationRequest.type == 'sick'){
               $scope.showSick=true;
               $scope.showHollidays=false;
           } else{
               $scope.showSick=false;
               $scope.showHollidays=false;
           }
        }

        $scope.toggleView = function(){
            $scope.showCal = !$scope.showCal;
            $text = $(".show-calender").text();
            if($text.indexOf($scope.i18n.showCalendar) >= 0){
                $text = $scope.i18n.hideCalendar;
                $scope.showForm = false;
                $scope.showList = false;
                $scope.showDetails = false;
            }else{
                $text = $scope.i18n.showCalendar;
                $scope.showResume = false;
                $scope.showList = true;
                $scope.showDetails = true;
            }
            $text = $(".show-calender").text($text);
        }

        $scope.loadBundles = function() {
            $http({
                method : 'GET',
                url : rhContainer.jzURL('RHRequestManagementController.getBundle')
            }).then(function successCallback(data) {
                $scope.i18n = data.data;

                deferred.resolve(data);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        }


        $scope.loadContext = function() {
            $http({
                method : 'GET',
                url : rhContainer.jzURL('RHRequestManagementController.getContext')
            }).then(function successCallback(data) {

                $scope.completeCurrentUser = data.data;
                $scope.currentUser=data.data.currentUser;
                $scope.sickBalance=data.data.sickBalance;
                $scope.holidaysBalance=data.data.holidaysBalance;
                var rsetUrl="/rest/social/people/suggest.json?currentUser="+$scope.currentUser;
                invite.build('managers', rsetUrl,'choose user');
                invite.build('substitutes', rsetUrl,'choose user');
                deferred.resolve(data);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
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
                    $scope.setResultMessage(data, "success");
                    $scope.showVacationRequest(data.data);
                    $timeout(function() {
                        $scope.setResultMessage("", "info")
                    }, 3000);
                }, function errorCallback(data) {
                    $scope.setResultMessage(data, "error");
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
                $scope.setResultMessage(data, "success");
                $scope.vacationRequestsToValidate = data.data;
                if($scope.vacationRequestsToValidate.length>0){
                    $scope.showList=true;
                    $scope.showForm=false;
                }
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
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
                $scope.setResultMessage(data, "success");
                $scope.myVacationRequests = data.data;
                if($scope.myVacationRequests.length>0){
                    $scope.showList=true;
                    $scope.showForm=false;
                }
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        };

        $scope.saveVacationRequest = function() {
            if (!$scope.validateVacationRequestForm($scope.newVacationRequest)) {
                return;
            }

            $scope.newVacationRequest.fromDate = new Date($("#fromDate").val()).getTime();
            $scope.newVacationRequest.toDate = new Date($("#toDate").val()).getTime();


            var managers= $scope.getUsers("managers");
            var substitutes= $scope.getUsers("substitutes");
            $scope.setResultMessage($scope.i18n.savingVacationRequest, "info");

            $scope.newVacationRequestWithManagers  = {
                vacationRequestDTO : $scope.newVacationRequest,
                managers : managers,
                substitutes : substitutes,
                exoCalendarId: $scope.userCalendarId
            };


            $http({
                data : $scope.newVacationRequestWithManagers,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhContainer.jzURL('RHRequestManagementController.saveVacationRequest')
            }).then(function successCallback(data) {
                $scope.setResultMessage(data, "success");
                $scope.myVacationRequests = data.data;
                $scope.showForm = false;
                $scope.newVacationRequest = {id : null };
                $("#managers").val("");
                $("#substitutes").val("");
                $("#userCalendar").val("");
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
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
                $scope.setResultMessage(data, "success");
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        }

        $scope.deleteRequest = function(vacationRequest) {
            if (!confirm($scope.i18n.deleteConfirm + " - " + vacationRequest.id)) {
                return;
            }
            $scope.setResultMessage($scope.i18n.savingVacationRequest, "info");
            $http({
                data : vacationRequest,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhContainer.jzURL('RHRequestManagementController.deleteRequest')
            }).then(function successCallback(data) {
                $scope.setResultMessage(data, "success");
                $scope.myVacationRequests = data.data;
                $scope.showForm = false;

                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        };

        $scope.showVacationRequest = function(vacationRequest) {
            $scope.vacationRequesttoShow=vacationRequest;
            $scope.loadManagers(vacationRequest);
            $scope.loadSubstitues(vacationRequest);
            $scope.loadAttachments(vacationRequest);
            $scope.loadComments(vacationRequest);
            $scope.showDetails = true;
        };


        $scope.showVacationRequestById = function(id) {

            $http({
                method : 'GET',
                url : rhContainer.jzURL('RHRequestManagementController.getVacationRequest')+ "&id=" +id
            }).then(function successCallback(data) {
                $scope.setResultMessage(data, "success");
                $scope.showVacationRequest(data.data);
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        }


        $scope.showVacationResume = function(id) {

            $http({
                method : 'GET',
                url : rhContainer.jzURL('RHRequestManagementController.getVacationRequest')+ "&id=" +id
            }).then(function successCallback(data) {
                $scope.setResultMessage(data, "success");


                /*POPUP HERE*/

                $scope.vacationRequesttoShow=data.data;
                $scope.loadManagers(data.data);
                $scope.loadSubstitues(data.data);
                $scope.loadComments(data.data);


                $scope.showResume = true;

                /*POPUP HERE*/

                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        }

        $scope.loadUserCalendars = function() {
            $http({
                url : rhContainer.jzURL('RHRequestManagementController.getUserCalendars')
            }).then(function successCallback(data) {
                $scope.setResultMessage(data, "success");
                $scope.userCalendars = data.data;
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
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
                $scope.setResultMessage(data, "success");
                $scope.vrsubs = data.data;
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
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
                $scope.setResultMessage(data, "success");
                $scope.vrmanagers = data.data;
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
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
                $scope.setResultMessage(data, "success");
                $scope.comments = data.data;
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        };


        $scope.saveComment = function() {

            $scope.setResultMessage($scope.i18n.savingVacationRequest, "info");


            $scope.newComment.requestId=$scope.vacationRequesttoShow.id;
            $scope.newComment.postedTime= new Date();
            $scope.newComment.posterId=$scope.vacationRequesttoShow.userFullName;

            $http({
                data : $scope.newComment,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhContainer.jzURL('RHRequestManagementController.saveComment')
            }).then(function successCallback(data) {

                $scope.setResultMessage(data, "success");
                $scope.comments.push($scope.newComment);
                $scope.newComment = {
                    id : null
                };
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
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
                $scope.setResultMessage(data, "success");
                $scope.loadVacationRequestsToValidate();
                $http({
                    method : 'GET',
                    url : rhContainer.jzURL('RHRequestManagementController.getVacationRequest')+ "&id=" +$scope.vacationRequesttoShow.id
                }).then(function successCallback(data) {
                    $scope.setResultMessage(data, "success");
                    $scope.showVacationRequest(data.data);
                    $timeout(function() {
                        $scope.setResultMessage("", "info")
                    }, 3000);
                }, function errorCallback(data) {
                    $scope.setResultMessage(data, "error");
                });
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
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
                $scope.setResultMessage(data, "success");
                $scope.loadVacationRequestsToValidate();
                $http({
                    method : 'GET',
                    url : rhContainer.jzURL('RHRequestManagementController.getVacationRequest')+ "&id=" +$scope.vacationRequesttoShow.id
                }).then(function successCallback(data) {
                    $scope.setResultMessage(data, "success");
                    $scope.showVacationRequest(data.data);
                    $timeout(function() {
                        $scope.setResultMessage("", "info")
                    }, 3000);
                }, function errorCallback(data) {
                    $scope.setResultMessage(data, "error");
                });
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
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
                $scope.setResultMessage(data, "success");
                $scope.attachements = data.data;
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        };



        $scope.deleteAttachement = function(fileName) {
            $http({
                url : rhContainer.jzURL('RHRequestManagementController.deleteFile')+"&requestId="+$scope.vacationRequesttoShow.id+"&fileName="+fileName
            }).then(function successCallback(data) {
                $scope.setResultMessage(data, "success");
                $scope.attachements = $scope.loadAttachments($scope.vacationRequesttoShow);
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        };


        $scope.geti18n = function(id) {

            for(var propt in $scope.i18n){
                if(id==propt) return $scope.i18n[propt];
            }
            return id;
        };

        $scope.loadVacationRequestsToValidate(null);
        $scope.loadMyVacationRequests(null);
        $scope.loadBundles();
        $scope.loadContext();
        $scope.showRequestfromUrl();
        $scope.loadUserCalendars();



        $scope.refreshController = function() {
            try {
                $scope.$digest()
            } catch (excep) {
                // No need to display errors in console
            }
        };
        calendar.build('myCalendar');
        $('#rhAddon').css('visibility', 'visible');
        $(".rhLoadingBar").remove();
    };
    return rhCtrl;
});