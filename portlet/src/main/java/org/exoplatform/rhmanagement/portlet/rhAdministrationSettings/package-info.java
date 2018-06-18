@Portlet
@Application(name = "RhAdministrationSettingsController")
@Bindings({
        @Binding(value = ConventionalVacationService.class),
        @Binding(value = OfficialVacationService.class),
        @Binding(value = SettingService.class),
        @Binding(value = ListenerService.class),
        @Binding(value = VacationRequestService.class)})
@Scripts({
    @Script(id = "jQueryUI", value = "js/lib/jquery-ui.js"),
    // AngularJS is still global, should be AMDified
    @Script(id = "angularjs", value = "js/lib/angular.min.js"),
    @Script(id = "ngSanitize", value = "js/lib/angular-sanitize.js", depends = "angularjs"),
    // services and controllers js are AMD modules, required by controllers.js
    @Script(id = "controllers", value = "js/controllers.js", depends = "angularjs"),
    @Script(id = "rhSettings", value = "js/rh-settings.js", depends = { "controllers","jQueryUI"})
})

@Stylesheets({
        @Stylesheet(id = "jQueryUISkin", value = "style/jquery-ui.css") ,
        @Stylesheet(id = "rh-settings", value = "style/rh-settings.css") })

@Assets("*")
package org.exoplatform.rhmanagement.portlet.rhAdministrationSettings;

import juzu.Application;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Script;
import juzu.plugin.asset.Scripts;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.asset.Stylesheets;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;

import juzu.plugin.portlet.Portlet;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.rhmanagement.services.*;
import org.exoplatform.services.listener.ListenerService;



