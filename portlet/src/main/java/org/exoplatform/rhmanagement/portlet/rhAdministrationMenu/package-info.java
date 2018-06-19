@Portlet
@Application(name = "RhAdministrationMenuController")
@Bindings()
@Scripts({

    @Script(id = "angularjs", value = "js/lib/angular.min.js"),
    @Script(id = "controllers", value = "js/controllers.js", depends = "angularjs"),
    @Script(id = "rhAdminMenu", value = "js/rh-admin-menu.js", depends = "controllers"),

})

@Stylesheets({
        @Stylesheet(id = "rhAdminMenuSkin", value = "style/rh-admin-menu.css") })

@Assets("*")
package org.exoplatform.rhmanagement.portlet.rhAdministrationMenu;

import juzu.Application;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Script;
import juzu.plugin.asset.Scripts;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.asset.Stylesheets;
import juzu.plugin.binding.Bindings;
import juzu.plugin.portlet.Portlet;

