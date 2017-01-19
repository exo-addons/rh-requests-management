define("rhAddonControllers", [ "SHARED/jquery", "SHARED/juzu-ajax","SHARED/userInvitation","SHARED/calendar"], function($, jz,invite,calendar)  {
    var rhCtrl = function($scope, $q, $timeout, $http, $filter) {
        var rhContainer = $('#rhAddon');
        var deferred = $q.defer();
        $scope.currentUser="";
        $scope.vacationRequestsToValidate = [];
        $scope.myVacationRequests = [];
        $scope.comments = [];
        $scope.vrmanagers = [];
        $scope.vrsubs = [];
        $scope.calEvents = [];
        $scope.showForm = true;
        $scope.showDetails = false;
        $scope.showList = false;
        $scope.showCal = false;
        $scope.newVacationRequest = {
            id : null
        };

        $scope.newComment = {
            id : null
        };


        $scope.vacationRequesttoShow = {
            validatorUserId : null
        };

        $scope.toggleView = function(){
            $scope.showCal = !$scope.showCal;
            $text = $(".show-calender").text();
            if($text.indexOf("Show Calendar") >= 0){
                $text = "Hide Calendar";
            }else{
                $text = "Show Calendar";
            }
            $text = $(".show-calender").text($text);
        }

        $scope.loadBundles = function() {
            $http({
                method : 'GET',
                url : rhContainer.jzURL('RHRequestManagementController.getBundle')
            }).then(function successCallback(data) {
                $scope.i18n = data.data;
                $scope.currentUser=data.data.currentUser;
                console.log($scope.i18n);
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

            console.log("New vacation");
            console.log($scope.newVacationRequest);

            var managers= $scope.getUsers("managers");
            var substitutes= $scope.getUsers("substitutes");
            $scope.setResultMessage($scope.i18n.savingVacationRequest, "info");

            $scope.newVacationRequestWithManagers  = {
                vacationRequestDTO : $scope.newVacationRequest,
                managers : managers,
                substitutes : substitutes
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
                console.log(data);
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

        $scope.loadVacationRequestsToValidate(null);
        $scope.loadMyVacationRequests(null);
        $scope.loadBundles();
        $scope.showRequestfromUrl();



        $scope.refreshController = function() {
            try {
                $scope.$digest()
            } catch (excep) {
                // No need to display errors in console
            }
        };
        var rsetUrl="/rest/rhrequest/uers/find";
        invite.build('managers', rsetUrl,'choose user');
        invite.build('substitutes', rsetUrl,'choose user');
        calendar.build('myCalendar');
        $('#rhAddon').css('visibility', 'visible');
        $(".rhLoadingBar").remove();
    };
    return rhCtrl;
});