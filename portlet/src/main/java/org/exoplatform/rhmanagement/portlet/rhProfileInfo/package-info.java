@Portlet
@Application(name = "RHProfileController")
@Bindings({
        @Binding(value = IdentityManager.class),
        @Binding(value = UserDataService.class)
})
@Scripts({
    @Script(id = "jQueryUIprfl", value = "js/lib/jquery-ui.js"),
    // AngularJS is still global, should be AMDified
    @Script(id = "angularjsprfl", value = "js/lib/angular.min.js"),
    @Script(id = "ngSanitizeprfl", value = "js/lib/angular-sanitize.js", depends = "angularjsprfl"),
    // services and controllers js are AMD modules, required by controllers.js
    @Script(id = "controllersprfl", value = "js/controllers.js", depends = { "angularjsprfl" }),
    @Script(id = "rhProfile", value = "js/rh-profile.js", depends = { "controllersprfl" }) })
@Less("style/sample-addon.less")
@Stylesheets({
        @Stylesheet(id = "jQueryUISkin", value = "style/jquery-ui.css") ,
        @Stylesheet(id = "fullcalendarSkin", value = "style/fullcalendar.css") ,
        @Stylesheet(id = "sample-addonSkin", value = "style/sample-addon.css") })
@Assets("*")
package org.exoplatform.rhmanagement.portlet.rhProfileInfo;

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


import org.exoplatform.rhmanagement.services.UserDataService;
import org.exoplatform.social.core.manager.IdentityManager;


