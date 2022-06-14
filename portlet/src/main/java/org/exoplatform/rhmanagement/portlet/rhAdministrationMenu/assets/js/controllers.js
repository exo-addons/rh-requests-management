define("rhAdminMenuControllers", ["SHARED/jquery", "SHARED/juzu-ajax"], function ($, jz) {
    var rhAdminMenuCtrl = function ($scope, $q, $timeout, $http) {
        var rhAdminContainer = $('#rhAdminMenu');

		var deferred = $q.defer();

		var currentLocation = window.location.href;

		$scope.employeesPageUrl = "/portal/g/:platform:hr-managers/rh-employees-admin/rh-employees-admin";
		$scope.requestsPageUrl = "/portal/g/:platform:hr-managers/rh-employees-admin/rh-requests-admin";
		$scope.historyPageUrl = "/portal/g/:platform:hr-managers/rh-employees-admin/rh-history";
		$scope.settingsPageUrl = "/portal/g/:platform:hr-managers/rh-employees-admin/rh-settings";

        $scope.openTab = function (tabName) {
            $("#employeesTab").removeClass("active");
            $("#requestsTab").removeClass("active");
            $("#historyTab").removeClass("active");
			$("#settingsTab").removeClass("active");
            $("#" + tabName + "Tab").addClass("active");
        }

		var tabName="";
		if(currentLocation.indexOf($scope.employeesPageUrl) !== -1) $scope.openTab("employees");
		else if(currentLocation.indexOf($scope.requestsPageUrl) !== -1) $scope.openTab("requests");
		else if(currentLocation.indexOf($scope.historyPageUrl) !== -1) $scope.openTab("history");
		else if(currentLocation.indexOf($scope.settingsPageUrl) !== -1) $scope.openTab("settings");


        $http({
                method: 'GET',
                url: rhAdminContainer.jzURL('RhAdministrationMenuController.getBundle') + "&locale=" + eXo.env.portal.language
            }).then(function successCallback(data) {
                $scope.i18n = data.data;
                deferred.resolve(data);

            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
        });    };

    return rhAdminMenuCtrl;

});
