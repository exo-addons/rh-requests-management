define("rhAddonControllers", [ "SHARED/jquery", "SHARED/juzu-ajax", "SHARED/mentionsPlugin"  ], function($, jz, mentionsPlugin)  {
    var rhCtrl = function($scope, $q, $timeout, $http, $filter) {
        var rhContainer = $('#rhAddon');
        var deferred = $q.defer();
        $scope.currentUser="";
        $scope.vacationRequestsToValidate = [];
        $scope.myVacationRequests = [];
        $scope.comments = [];
        $scope.vrmanagers = [];
        $scope.vrsubs = [];
        $scope.showForm = false;
        $scope.showDetails = false;
        $scope.newVacationRequest = {
            id : null
        };

        $scope.newComment = {
            id : null
        };


        $scope.vacationRequesttoShow = {
            validatorUserId : null
        };


        $scope.loadBundles = function() {
            $http({
                method : 'GET',
                url : rhContainer.jzURL('RHRequestManagementController.getBundle')
            }).then(function successCallback(data) {
                $scope.i18n = data.data;
                $scope.currentUser=data.data.currentUser;
                console.log($scope.i18n);
                deferred.resolve(data);
                $scope.initMentions();
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
            var managers= $scope.getMentions("managers");
            var substitutes= $scope.getMentions("substitutes");
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
                $scope.initMentions();
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

        $scope.initMentions = function() {

            rhContainer.find('#substitutes').exoMentions({
                onDataRequest : function(mode, query, callback) {
                    var _this = this;
                    rhContainer.jzAjax('RHRequestManagementController.findUsers()', {
                        data : {
                            query : query
                        },
                        success : function(data) {
                            callback.call(_this, data);
                        }
                    });
                },
                elastic : {
                    maxHeight : '52px',
                    minHeight : '22px',
                    marginButton : '4px',
                    enableMargin : false
                },
                messages : {
                    helpSearch: $scope.i18n.mentionsTypeToSearchUsers,
                    searching: $scope.i18n.mentionsSearchingFor,
                    foundNoMatch : $scope.i18n.mentionsNoMatchingForUsers
                },
                minChars : 3,
                cacheResult : true,
            });

            rhContainer.find('#managers').exoMentions({
                onDataRequest : function(mode, query, callback) {
                    var _this = this;
                    rhContainer.jzAjax('RHRequestManagementController.findUsers()', {
                        data : {
                            query : query
                        },
                        success : function(data) {
                            callback.call(_this, data);
                        }
                    });
                },
                elastic : {
                    maxHeight : '52px',
                    minHeight : '22px',
                    marginButton : '4px',
                    enableMargin : false
                },
                messages : {
                    helpSearch: $scope.i18n.mentionsTypeToSearchUsers,
                    searching: $scope.i18n.mentionsSearchingFor,
                    foundNoMatch : $scope.i18n.mentionsNoMatchingForUsers
                },
                minChars : 3,
                cacheResult : true,
            });

        }


        $scope.getMentions = function(id) {
            var data = $("#"+id).parents('form:first')
                .parent().data(id);
            if(data && data.mentions && data.mentions.length > 0) {
                return data.mentions;
            }
            return null;
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
        $('#rhAddon').css('visibility', 'visible');
        $(".rhLoadingBar").remove();
    };
    return rhCtrl;
});