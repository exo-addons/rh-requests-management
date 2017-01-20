@Portlet
@Application(name = "RHRequestManagementController")
@Bindings({ @Binding(value = VacationRequestService.class),
        @Binding(value = SpaceService.class),
        @Binding(value = IdentityManager.class),
        @Binding(value = ActivityManager.class)
})
@Scripts({
    @Script(id = "jQueryUI", value = "js/lib/jquery-ui.js"),
    // AngularJS is still global, should be AMDified
    @Script(id = "angularjs", value = "js/lib/angular.min.js"),
    @Script(id = "ngSanitize", value = "js/lib/angular-sanitize.js", depends = "angularjs"),
        @Script(id = "ariajs", value = "js/lib/angular-aria.js", depends = "angularjs"),
        @Script(id = "animate", value = "js/lib/angular-animate.js", depends = "angularjs"),
        @Script(id = "material", value = "js/lib/angular-material.min.js", depends = "angularjs"),
    // services and controllers js are AMD modules, required by controllers.js
    @Script(id = "controllers", value = "js/controllers.js", depends = { "angularjs" }),
    @Script(id = "rhAddon", value = "js/rh-management-addon.js", depends = { "controllers" }) })
@Less("style/sample-addon.less")
@Stylesheets({
        @Stylesheet(id = "jQueryUISkin", value = "style/jquery-ui.css") ,
        @Stylesheet(id = "fullcalendarSkin", value = "style/fullcalendar.css") ,
        @Stylesheet(id = "sample-addonSkin", value = "style/sample-addon.css") })
@Assets("*")
package org.exoplatform.rhmanagement.portlet.rhManagement;

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

import org.exoplatform.rhmanagement.services.VacationRequestService;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.spi.SpaceService;

