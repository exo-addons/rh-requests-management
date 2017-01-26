define("rhAdminAddonControllers", [ "SHARED/jquery", "SHARED/juzu-ajax"], function($) {
    var rhAdminCtrl = function($scope, $q, $timeout, $http, $filter, PagerService) {
        var rhAdminContainer = $('#rhAdminAddon');
        var deferred = $q.defer();


        $scope.showEmployees=true;
        $scope.rhEmployees = [];
        $scope.vacationRequests = [];
        $scope.currentPage = 0;
        $scope.pageSize = 10;
        $scope.def = '';

        $scope.allVacationRequests = [];

        $scope.orderByField = 'title';
        $scope.reverseSort = false;
        $scope.rhEmployee = {};

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
            $scope.resultMessage = text;
        }

        $scope.loadBundles = function() {
            $http({
                method : 'GET',
                url : rhAdminContainer.jzURL('RhAdministrationController.getBundle') + "&locale=" + eXo.env.portal.language
            }).then(function successCallback(data) {
                $scope.i18n = data.data;
                deferred.resolve(data);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        }

        $scope.loadEmployees = function() {
            $http({
                method : 'GET',
                url : rhAdminContainer.jzURL('RhAdministrationController.getAllUsersRhData')
            }).then(function successCallback(data) {
                $scope.setResultMessage(data, "success");
                $scope.rhEmployees = data.data;
                $scope.vm.setPage(1);
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });

            return $filter('filter')($scope.rhEmployees, $scope.def)
        };

        $scope.loadVacationRequests = function(userId) {
            $http({
                method : 'GET',
                url : rhAdminContainer.jzURL('RhAdministrationController.getVacationRequestsbyUserId')+ "&userId=" +userId
            }).then(function successCallback(data) {
                $scope.setResultMessage(data, "success");
                $scope.vacationRequests = data.data;
                $scope.showEmployees=false;
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        };


        $scope.loadAllVacationRequests = function() {
            $http({
                method : 'GET',
                url : rhAdminContainer.jzURL('RhAdministrationController.getAllVacationRequests')
            }).then(function successCallback(data) {
                $scope.setResultMessage(data, "success");
                $scope.allVacationRequests = data.data;
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        };

        $scope.editUserRhData = function(modifiedEmplyee){
            $scope.rhEmployee = modifiedEmplyee;
        }


        $scope.saveUserHRData = function(employee) {
            $http({
                data : employee,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhAdminContainer.jzURL('RhAdministrationController.saveUserRHData')
            }).then(function successCallback(data) {
                employee.id = data.data.id;
            }, function errorCallback(data) {
                $scope.setResultMessage("", "error");
            });
        }



        $scope.validateRequest = function(vacationRequest) {
            $http({
                data : vacationRequest,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhAdminContainer.jzURL('RhAdministrationController.validateRequest')
            }).then(function successCallback(data) {
                $scope.setResultMessage(data, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        }

        $scope.cancelRequest = function(vacationRequest) {
            $http({
                data : vacationRequest,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhAdminContainer.jzURL('RhAdministrationController.cancelRequest')
            }).then(function successCallback(data) {
                $scope.setResultMessage(data, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        }


        $scope.openTab = function (evt, tabName) {
            // Declare all variables
            var i, tabcontent, tablinks;

            // Get all elements with class="tabcontent" and hide them
            tabcontent = document.getElementsByClassName("tabcontent");
            for (i = 0; i < tabcontent.length; i++) {
                tabcontent[i].style.display = "none";
            }

            // Get all elements with class="tablinks" and remove the class "active"
            tablinks = document.getElementsByClassName("tablinks");
            for (i = 0; i < tablinks.length; i++) {
                tablinks[i].className = tablinks[i].className.replace(" active", "");
            }

            // Show the current tab, and add an "active" class to the link that opened the tab
            document.getElementById(tabName).style.display = "block";
            document.getElementById(evt).className += " active";
        }

        $scope.loadEmployees();
        $scope.loadAllVacationRequests();
        $('#rhAdminAddon').css('visibility', 'visible');
        $(".rhLoadingBar").remove();
    };
    return rhAdminCtrl;
});
