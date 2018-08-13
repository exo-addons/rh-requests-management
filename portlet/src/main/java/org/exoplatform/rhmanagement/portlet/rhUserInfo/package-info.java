@Portlet
@Application(name = "RHUserInfoController")
@Bindings({
        @Binding(value = IdentityManager.class),
        @Binding(value = OfficialVacationService.class),
        @Binding(value = ConventionalVacationService.class),
        @Binding(value = UserDataService.class)
})
@Scripts({
    @Script(id = "jQueryUIprfl", value = "js/lib/jquery-ui.js"),
    // AngularJS is still global, should be AMDified
    @Script(id = "angularjs", value = "js/lib/angular.min.js"),
    @Script(id = "ngSanitize", value = "js/lib/angular-sanitize.js", depends = "angularjs"),
	@Script(id = "ng-google-chart", value = "js/lib/ng-google-chart.js", depends = "angularjs"),
    // services and controllers js are AMD modules, required by controllers.js
    @Script(id = "controllers", value = "js/controllers.js", depends = { "angularjs" , "ng-google-chart" }),
    @Script(id = "rhUserInfo", value = "js/rh-user-info.js", depends = { "controllers" }) })
@Stylesheets({
        @Stylesheet(id = "jQueryUISkin", value = "style/jquery-ui.css") ,
        @Stylesheet(id = "rh-user-infoSkin", value = "style/rh-user-info.css") })
@Assets("*")
package org.exoplatform.rhmanagement.portlet.rhUserInfo;

import juzu.Application;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Script;
import juzu.plugin.asset.Scripts;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.asset.Stylesheets;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.less.Less;
import juzu.plugin.portlet.Portlet;


import org.exoplatform.rhmanagement.services.ConventionalVacationService;
import org.exoplatform.rhmanagement.services.OfficialVacationService;
import org.exoplatform.rhmanagement.services.UserDataService;
import org.exoplatform.social.core.manager.IdentityManager;


