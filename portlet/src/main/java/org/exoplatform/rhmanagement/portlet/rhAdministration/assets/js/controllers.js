define("rhAdminAddonControllers", [ "SHARED/jquery", "SHARED/juzu-ajax"], function($) {
    var rhAdminCtrl = function($scope, $q, $timeout, $http, $filter, PagerService , Upload) {
        var rhAdminContainer = $('#rhAdminAddon');
        var deferred = $q.defer();


        $scope.currentUser="";
        $scope.currentUserAvatar="";
        $scope.currentUserName="";
        $scope.showAddForm = false;
        $scope.showEmployees=true;
        $scope.showElementDetail=false;
        $scope.getUserLigne=false;
        $scope.rhEmployees = [];
        $scope.vacationRequests = [];
        $scope.currentPage = 0;
        $scope.pageSize = 10;
        $scope.def = '';
        $scope.userDetails=null;
        $scope.allVacationRequests = [];
        $scope.attachements = [];
        $scope.orderByField = 'title';
        $scope.reverseSort = false;
        $scope.rhEmployee = {};
        $scope.newUserId = "";
        $scope.newUserDetails={
            id : null
        };
        $scope.showDetails = false;
        $scope.showAlert = false;

        $scope.newComment = {
            id : null
        };


        $scope.vacationRequesttoShow = {
            validatorUserId : null
        };

        $scope.vm = this;
        $scope.vm.pager = {};
        $scope.vm.setPage = setPage;

        $scope.initController = function() {
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

        $scope.setResultMessage = function(text, type) {
            $scope.resultMessageClass = "alert-" + type;
            $scope.resultMessageClassExt = "uiIcon" + type.charAt(0).toUpperCase()
                + type.slice(1);
            $scope.showAlert = true;
            $scope.resultMessage = text;
        }

        $scope.loadBundles = function() {
            $http({
                method : 'GET',
                url : rhAdminContainer.jzURL('RhAdministrationController.getBundle') + "&locale=" + eXo.env.portal.language
            }).then(function successCallback(data) {
                $scope.i18n = data.data;
                deferred.resolve(data);
                /*$scope.setResultMessage(data, "success");*/
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }



        $scope.loadEmployees = function() {
            $http({
                method : 'GET',
                url : rhAdminContainer.jzURL('RhAdministrationController.getAllUsersRhData')
            }).then(function successCallback(data) {
                $scope.rhEmployees = data.data;
                //$scope.vm.setPage(1);
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
            $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });

           // return $filter('filter')($scope.rhEmployees, $scope.def)
        };

        $scope.loadUserHRData = function(userRhData) {
                    $scope.userDetails = userRhData;
                    if(!userRhData.avatar){
                        $scope.userDetails.avatar = "/eXoSkin/skin/images/system/UserAvtDefault.png";
                    }
                    $http({
                        method : 'GET',
                        url : rhAdminContainer.jzURL('RhAdministrationController.getVacationRequestsbyUserId')+ "&userId=" +userRhData.userId
                    }).then(function successCallback(data) {
                        $scope.loadAttachments(userRhData);
                        $scope.vacationRequests = data.data;
                        $scope.showAddForm=true;
                        $scope.showAlert = false;
        //                $timeout(function() {
        //                    $scope.setResultMessage(data, "success")
        //                }, 1000);
                    }, function errorCallback(data) {
                        $scope.showAddForm=false;
                        $scope.setResultMessage($scope.i18n.defaultError, "error");
                    });
                };



                $scope.getUser = function(newUserId) {

                    if(newUserId){
                        $("#getUser").removeClass("invalid");
                        $http({
                            method : 'GET',
                            url : rhAdminContainer.jzURL('RhAdministrationController.getUser')+ "&userId=" +newUserId
                        }).then(function successCallback(data) {
                            $scope.newUserDetails = data.data;
                            $scope.showAlert = false;
                            $scope.loadUserHRData(data.data);
                            if(data.data == ""){
                                $scope.showAddForm=false;
                                $scope.setResultMessage($scope.i18n.noUser, "info");
                                $timeout(function() {
                                    $scope.showAlert = false;
                                }, 2000);
                            }else{
                                $scope.showAddForm=true;
                                if(!data.data.avatar){
                                    $scope.newUserDetails.avatar = "/eXoSkin/skin/images/system/UserAvtDefault.png";
                                }
                            }

        //                    $timeout(function() {
        //                        $scope.setResultMessage(data, "success")
        //                    }, 1000);
                        }, function errorCallback(data) {
                            $scope.setResultMessage($scope.i18n.defaultError, "error");
                        });
                    }else{
                        $("#getUser").addClass("invalid").focus();
                    }
                };



        $scope.loadAllVacationRequests = function() {
            $http({
                method : 'GET',
                url : rhAdminContainer.jzURL('RhAdministrationController.getAllVacationRequests')
            }).then(function successCallback(data) {
                $scope.allVacationRequests = data.data;
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };

        $scope.editUserRhData = function(modifiedEmplyee){
            $scope.rhEmployee = modifiedEmplyee;
        }


        $scope.saveUserHRData = function(employee) {

            console.log(employee);
            $http({
                data : employee,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhAdminContainer.jzURL('RhAdministrationController.saveUserRHData')
            }).then(function successCallback(data) {
                employee.id = data.data.id;
                $scope.loadEmployees();
                $scope.showAddForm=false;

                $scope.setResultMessage($scope.i18n.userUpdate, "success");


            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

        $scope.deleteUserHRData = function(employee) {
            $http({
                data : employee,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhAdminContainer.jzURL('RhAdministrationController.deleteUserRHData')
            }).then(function successCallback(data) {
                $scope.loadEmployees();
                $scope.setResultMessage($scope.i18n.Userdelete, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

        $scope.validateRequest = function(vacationRequest,user) {
            $http({
                data : vacationRequest,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhAdminContainer.jzURL('RhAdministrationController.validateRequest')
            }).then(function successCallback(data) {
                if(user==null) {
                    $scope.loadAllVacationRequests();
                }else{
                    if (data.data!=null){
                        user.hrData=data.data;
                    }
                    $scope.loadUserHRData(user);
                    }
//                $scope.setResultMessage(data, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

        $scope.cancelRequest = function(vacationRequest,user) {
            $http({
                data : vacationRequest,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhAdminContainer.jzURL('RhAdministrationController.cancelRequest')
            }).then(function successCallback(data) {
                if(user==null) {
                    $scope.loadAllVacationRequests();
                }else{
                    if (data.data!=null){
                        user.hrData=data.data;
                    }
                    $scope.loadUserHRData(user);
                }
//                $scope.setResultMessage(data, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.openTab = function (tabName, tabHide) {
            $("#"+tabName).css("display", "block");
            $("#"+tabHide).css("display", "none");
            $("#"+tabName + "Tab").addClass("active");
            $("#"+tabHide + "Tab").removeClass("active");
        }

        $scope.enableInput = function (id) {
            $(id).removeAttr("readonly");
            $("#submit").css("display", "block");
        }


        $scope.uploadFiles = function(file, errFiles) {
            $scope.f = file;
            $scope.errFile = errFiles && errFiles[0];
            if (file) {
                file.upload = Upload.upload({
                    url: rhAdminContainer.jzURL('RhAdministrationController.uploadFile'),
                    data: {userId: $scope.newUserDetails.userId,
                        file: file}
                });
                file.upload.then(function (response) {
                    $timeout(function () {
                        file.result = response.data;
                        $scope.attachements = $scope.loadAttachments($scope.newUserDetails);
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

        $scope.loadAttachments = function(newUserDetails) {
            $http({
                data : newUserDetails,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhAdminContainer.jzURL('RhAdministrationController.getEmployeeAttachements')
            }).then(function successCallback(data) {
                $scope.attachements = data.data;
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };

        $scope.deleteAttachement = function(fileName) {
            $http({
                url : rhAdminContainer.jzURL('RhAdministrationController.deleteFile')+"&userId="+$scope.newUserDetails.userId+"&fileName="+fileName
            }).then(function successCallback(data) {
                $scope.attachements = $scope.loadAttachments($scope.newUserDetails);
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
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

        $scope.loadSubstitues = function(vacationRequest) {
            $http({
                data : vacationRequest,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhAdminContainer.jzURL('RhAdministrationController.getSubstitutesByRequestID')
            }).then(function successCallback(data) {
                $scope.vrsubs = data.data;
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
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
                url : rhAdminContainer.jzURL('RhAdministrationController.getValidatorsByRequestID')
            }).then(function successCallback(data) {
                $scope.vrmanagers = data.data;
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
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
                url : rhAdminContainer.jzURL('RhAdministrationController.getComments')
            }).then(function successCallback(data) {
                $scope.comments = data.data;
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };


        $scope.saveComment = function() {

//            $scope.setResultMessage($scope.i18n.savingVacationRequest, "info");
            if($scope.newComment.commentText){
            $scope.showAlert = false;
                $scope.newComment.requestId=$scope.vacationRequesttoShow.id;
                $scope.newComment.postedTime= new Date();
                $scope.newComment.posterId=$scope.currentUser;
                $scope.newComment.posterAvatar=$scope.currentUserAvatar;
                $scope.newComment.posterName=$scope.currentUserName;

                $http({
                    data : $scope.newComment,
                    method : 'POST',
                    headers : {
                        'Content-Type' : 'application/json'
                    },
                    url : rhAdminContainer.jzURL('RhAdministrationController.saveComment')
                }).then(function successCallback(data) {

    //                $scope.setResultMessage(data, "success");
                    $scope.comments.push($scope.newComment);
                    $scope.newComment = {
                        id : null
                    };
    //                $timeout(function() {
    //                    $scope.setResultMessage(data, "success")
    //                }, 1000);
                }, function errorCallback(data) {
                    $scope.setResultMessage($scope.i18n.defaultError, "error");
                });
            }else{
                $scope.setResultMessage($scope.i18n.emptyComment, "error");
            }
        }


        $scope.loadRequestAttachments = function(vacationRequest) {
            $http({
                data : vacationRequest,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhAdminContainer.jzURL('RhAdministrationController.getRequestAttachements')
            }).then(function successCallback(data) {
                $scope.attachements = data.data;
//                $timeout(function() {
//                    $scope.setResultMessage(data, "success")
//                }, 1000);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };

        $scope.showVacationRequest = function(vacationRequest) {
            $scope.vacationRequesttoShow=vacationRequest;
            $scope.loadManagers(vacationRequest);
            $scope.loadSubstitues(vacationRequest);
            $scope.loadRequestAttachments(vacationRequest);
            $scope.loadComments(vacationRequest);
            $scope.showDetails = true;
        };


        $scope.loadContext = function() {
            $http({
                method : 'GET',
                url : rhAdminContainer.jzURL('RhAdministrationController.getContext')
            }).then(function successCallback(data) {
                $scope.currentUser=data.data.currentUser;
                $scope.currentUserAvatar=data.data.currentUserAvatar;
                $scope.currentUserName=data.data.currentUserName;
                deferred.resolve(data);
//                $scope.setResultMessage(data, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }
        $scope.loadBundles();
        $scope.loadContext();
        $scope.loadEmployees();

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
