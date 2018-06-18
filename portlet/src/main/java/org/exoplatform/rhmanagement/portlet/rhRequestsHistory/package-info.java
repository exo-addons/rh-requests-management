@Portlet
@Application(name = "RhRequestsHistoryController")
@Bindings({
        @Binding(value = UserDataService.class),
        @Binding(value = IdentityManager.class),
        @Binding(value = OrganizationService.class),
        @Binding(value = RepositoryService.class),
        @Binding(value = SettingService.class),
        @Binding(value = ValidatorService.class),
        @Binding(value = CommentService.class),
        @Binding(value = ListenerService.class),
        @Binding(value = BalanceHistoryService.class),
        @Binding(value = ConventionalVacationService.class),
        @Binding(value = OfficialVacationService.class),
        @Binding(value = VacationRequestService.class)})
@Scripts({
    @Script(id = "jQueryUI", value = "js/lib/jquery-ui.js"),
    // AngularJS is still global, should be AMDified
    @Script(id = "angularjs", value = "js/lib/angular.min.js"),
    @Script(id = "ngSanitize", value = "js/lib/angular-sanitize.js", depends = "angularjs"),
    // services and controllers js are AMD modules, required by controllers.js
        @Script(id = "fileSaver", value = "js/lib/FileSaver.js", depends = {"angularjs"}),
    @Script(id = "controllers", value = "js/controllers.js", depends = { "angularjs","fileSaver" }),
    @Script(id = "rhRequestHistory", value = "js/rh-request-history.js", depends = { "controllers","jQueryUI" }),

    /* Time PICKER */
    @Script(id = "moment", value = "js/lib/moment.js")
    /*@Script(id = "timepicker-addon", value = "js/lib/jquery-ui-timepicker-addon.js"),*/

})

@Stylesheets({
        @Stylesheet(id = "jQueryUISkin", value = "style/jquery-ui.css") ,
        @Stylesheet(id = "rh-request-history-skin", value = "style/rh-request-history.css") })

@Assets("*")
package org.exoplatform.rhmanagement.portlet.rhRequestsHistory;

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

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.rhmanagement.services.*;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.manager.IdentityManager;


