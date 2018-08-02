define("rhUserInfoControllers", [ "SHARED/jquery", "SHARED/juzu-ajax"], function($, jz)  {
    var rhUserInfoCtrl = function($scope, $q, $timeout, $http, $filter) {
        var rhUserInfoContainer = $('#rhUserInfo');
        var deferred = $q.defer();
        $scope.userData = {
            id : null
        };
$scope.requestsPageUrl = "/portal/intranet/rh-management";

                $scope.orgChartObject = {};
                $scope.orgChartObject.options = {
					allowHtml:true

                };
                $scope.orgChartObject.type = "OrgChart";

        $scope.loadBundles = function() {
            $http({
                method : 'GET',
                url : rhUserInfoContainer.jzURL('RHUserInfoController.getBundle')
            }).then(function successCallback(data) {
                $scope.i18n = data.data;
                $scope.currentUser=data.data.currentUser;
                console.log($scope.i18n);
                $scope.loadData();
                deferred.resolve(data);
            }, function errorCallback(data) {
                $scope.setResultMessage(data, "error");
            });
        }



        $scope.loadData = function() {

            $http({
                method : 'GET',
                url : rhUserInfoContainer.jzURL('RHUserInfoController.getData')
            }).then(function successCallback(data) {
                $scope.currentUser=data.data.currentUser;
                $scope.currentUserAvatar = data.data.currentUserAvatar;
                $scope.currentUserName = data.data.currentUserName;
                $scope.sickBalance = data.data.sickBalance;
                $scope.holidaysBalance = data.data.holidaysBalance;
                $scope.hrId = data.data.hrId;
                $scope.insuranceId = data.data.insuranceId;
                $scope.socialSecNumber = data.data.socialSecNumber;
                if(data.data.conventionalVacations!=null) {$scope.cVacations = data.data.conventionalVacations;}
                if(data.data.officialVacations!=null) {$scope.oVacations = data.data.officialVacations;}
                $scope.officialDays=data.data.officialDays;
                $scope.loadFunctionalOrg();
                console.log(deferred.resolve(data));
                $scope.showAlert = false;
                deferred.resolve(data);
				$('#rhUserInfo').css('visibility', 'visible');
				$(".rhLoadingBar").remove();
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }
	  
        $scope.loadFunctionalOrg = function() {
            $http({
                url : rhUserInfoContainer.jzURL('RHUserInfoController.getFunctionalOrganization')
            }).then(function successCallback(data) {

                $scope.funcOrgList = data.data;



                      var funcOrgArray = [];
                        for(var i = 0; i < $scope.funcOrgList.length; i++) {
                             var obj = $scope.funcOrgList[i];
                                      if(obj.userId===$scope.currentUser){
                                      funcOrgArray.push({ c: [{v: obj.userId, f: '<div style="color:red; font-style:italic">'+obj.fullName+'</div>'}, {v: obj.manager}, {"v": obj.job}]});
                                      }else{
                                      funcOrgArray.push({ c: [{v: obj.userId, f: obj.fullName}, {v: obj.manager}, {"v": obj.job}]});
                                      }

                                 }

                                    $scope.orgChartObject.data = {
                                                                      "cols" : [
                                                                          {"label": "Name", "pattern": "", "type": "string"},
                                                                          {"label": "Manager", "pattern": "", "type": "string"},
                                                                          {"label": "ToolTip", "pattern": "", "type": "string"}
                                                                      ], "rows":  funcOrgArray
                    };

                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };



        $scope.setResultMessage = function(text, type) {
            $scope.resultMessageClass = "alert-" + type;
            $scope.resultMessageClassExt = "uiIcon" + type.charAt(0).toUpperCase()
                + type.slice(1);
            $scope.resultMessage = text;
        }

        $scope.loadBundles();

    };
    return rhUserInfoCtrl;
});