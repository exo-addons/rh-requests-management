define("rhPrflAddonControllers", [ "SHARED/jquery", "SHARED/juzu-ajax"], function($, jz)  {
    var rhPrflCtrl = function($scope, $q, $timeout, $http, $filter) {
        var rhPrflContainer = $('#rhPrflAddon');
        var deferred = $q.defer();
        $scope.showEdit=false;
        $scope.userData = {
            id : null
        };



        $scope.loadBundles = function() {
            $http({
                method : 'GET',
                url : rhPrflContainer.jzURL('RHProfileController.getBundle')
            }).then(function successCallback(data) {
                $scope.i18n = data.data;
                $scope.currentUser=data.data.currentUser;
                console.log($scope.i18n);
                deferred.resolve(data);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        }



        $scope.loadUserRhData= function() {

            $http({
                method : 'GET',
                url : rhPrflContainer.jzURL('RHProfileController.loadUserRhData')
            }).then(function successCallback(data) {
                $scope.setResultMessage(data, "success");
                $scope.userData = data.data;

                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        };


        $scope.saveUserData = function() {

            $http({
                data : $scope.userData,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : rhPrflContainer.jzURL('RHProfileController.saveUserData')
            }).then(function successCallback(data) {
                $scope.setResultMessage(data, "success");
                $scope.showEdit=false;
                $timeout(function() {
                    $scope.setResultMessage("", "info")
                }, 3000);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        }

        $scope.setResultMessage = function(text, type) {
            $scope.resultMessageClass = "alert-" + type;
            $scope.resultMessageClassExt = "uiIcon" + type.charAt(0).toUpperCase()
                + type.slice(1);
            $scope.resultMessage = text;
        }

      //  $scope.loadBundles();
		$scope.loadUserRhData();
        $('#rhPrflAddon').css('visibility', 'visible');
        $(".rhLoadingBar").remove();
    };
    return rhPrflCtrl;
});