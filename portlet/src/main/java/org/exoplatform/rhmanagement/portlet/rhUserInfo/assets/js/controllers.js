define("rhUserInfoControllers", [ "SHARED/jquery", "SHARED/juzu-ajax"], function($, jz)  {
    var rhUserInfoCtrl = function($scope, $q, $timeout, $http, $filter) {
        var rhUserInfoContainer = $('#rhUserInfo');
        var deferred = $q.defer();
        $scope.userData = {
            id : null
        };
$scope.requestsPageUrl = "/portal/dw/rh-management";
$("#charts").css("display", "none");
        $scope.openTab = function (tabName) {

            $("#vacations").css("display", "none");
            $("#charts").css("display", "none");

            $("#vacationsTab").removeClass("active");
            $("#chartsTab").removeClass("active");

            $("#" + tabName).css("display", "block");
            $("#" + tabName + "Tab").addClass("active");
            $scope.showDetails=false;
            $scope.showAddForm =false;
        }


                $scope.orgFChartObject = {};
                $scope.orgFChartObject.options = {
					allowHtml:true,
					nodeClass:'exoer'

                };
                $scope.orgFChartObject.type = "OrgChart";

                $scope.orgHChartObject = {};
                $scope.orgHChartObject.options = {
					allowHtml:true,
					nodeClass:'exoer'

                };
                $scope.orgHChartObject.type = "OrgChart";

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
                $scope.loadFunctionalOrg($scope.currentUser);
                $scope.loadHierarchicalOrg($scope.currentUser);
                console.log(deferred.resolve(data));
                $scope.showAlert = false;
                deferred.resolve(data);
				$('#rhUserInfo').css('visibility', 'visible');
				$(".rhLoadingBar").remove();
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

        $scope.loadHierarchicalOrg = function(userId) {
            $http({
                url : rhUserInfoContainer.jzURL('RHUserInfoController.getHierarchicalOrganization')+"&userId="+userId
            }).then(function successCallback(data) {

                $scope.HierOrgList = data.data;




                      var HierOrgArray = [];
                        for(var i = 0; i < $scope.HierOrgList.length; i++) {
                             var obj = $scope.HierOrgList[i];
                                      if(obj.userId===userId){
                                      HierOrgArray.push({ c: [{v: obj.userId, f: '<img src="'+obj.avatar+'"/><div style="color:blue;font-style:italic;font-size: 16px;font-weight: bold;">'+obj.fullName+'</div>'+obj.job}, {v: obj.manager}, {"v": obj.job}]});
                                      }else{
                                      HierOrgArray.push({ c: [{v: obj.userId, f: '<img src="'+obj.avatar+'"/><div style="font-style:italic;font-size: 16px;font-weight: bold;">'+obj.fullName+'</div>'+obj.job}, {v: obj.manager}, {"v": obj.job}]});
                                      }

                                 }

                                    $scope.orgHChartObject.data = {
                                                                      "cols" : [
                                                                          {"label": "Name", "pattern": "", "type": "string"},
                                                                          {"label": "Manager", "pattern": "", "type": "string"},
                                                                          {"label": "ToolTip", "pattern": "", "type": "string"}
                                                                      ], "rows":  HierOrgArray
                    };

                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };

        $scope.loadFunctionalOrg = function(userId) {
            $http({
                url : rhUserInfoContainer.jzURL('RHUserInfoController.getFunctionalOrganization')+"&userId="+userId
            }).then(function successCallback(data) {

                $scope.funcOrgList = data.data;



                      var funcOrgArray = [];
                        for(var i = 0; i < $scope.funcOrgList.length; i++) {
                             var obj = $scope.funcOrgList[i];
                                      if(obj.userId===userId){
                                      funcOrgArray.push({ c: [{v: obj.userId, f: '<img src="'+obj.avatar+'"/><div style="color:blue;font-style:italic;font-size: 16px;font-weight: bold;">'+obj.fullName+'</div>'+obj.job}, {v: obj.manager}, {"v": obj.job}]});
                                      }else{
                                      funcOrgArray.push({ c: [{v: obj.userId, f: '<img src="'+obj.avatar+'"/><div style="font-style:italic;font-size: 16px;font-weight: bold;">'+obj.fullName+'</div>'+obj.job}, {v: obj.manager}, {"v": obj.job}]});
                                      }

                                 }

                                    $scope.orgFChartObject.data = {
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

$scope.setOrgFChartUser = function(selectedItem){
var userId = $scope.orgFChartObject.data.rows[selectedItem.row].c[0].v;
if (userId != null) {
$scope.loadFunctionalOrg(userId);
$scope.loadHierarchicalOrg(userId);
}

}

$scope.setOrgHChartUser = function(selectedItem){
var userId = $scope.orgHChartObject.data.rows[selectedItem.row].c[0].v;
if (userId != null) {
$scope.loadHierarchicalOrg(userId);
$scope.loadFunctionalOrg(userId);
}

}

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