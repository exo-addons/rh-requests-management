package org.exoplatform.rhmanagement.portlet.rhProfileInfo;

import java.util.*;

import javax.inject.Inject;
import javax.persistence.NoResultException;

import juzu.HttpMethod;
import juzu.MimeType;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.SessionScoped;
import juzu.View;
import juzu.impl.common.JSON;
import juzu.plugin.jackson.Jackson;
import juzu.template.Template;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.portal.application.RequestNavigationData;
import org.exoplatform.rhmanagement.dto.*;
import org.exoplatform.rhmanagement.integration.notification.RequestCreatedPlugin;
import org.exoplatform.rhmanagement.integration.notification.RequestRepliedPlugin;
import org.exoplatform.rhmanagement.integration.notification.RequestStatusChangedPlugin;
import org.exoplatform.rhmanagement.services.CommentService;
import org.exoplatform.rhmanagement.services.UserDataService;
import org.exoplatform.rhmanagement.services.VacationRequestService;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.rhmanagement.services.ValidatorService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.router.ExoRouter;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.space.spi.SpaceService;

@SessionScoped
public class RHProfileController {
  private static Log log = ExoLogger.getLogger(RHProfileController.class);

  // Don't use inject to not get the merge of all resource bundles
  // @Inject
  ResourceBundle     bundle;

  @Inject
  IdentityManager identityManager;

  @Inject
  UserDataService userDataService;


  @Inject
  @Path("index.gtmpl")
  Template           indexTmpl;

  private String     bundleString;

  private final String currentUser = ConversationState.getCurrent().getIdentity().getUserId();


  @View
  public Response.Content index() {
    return indexTmpl.ok();
  }

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public UserRHDataDTO loadUserRhData() {
    try {
       /*org.exoplatform.services.security.Identity currentUserIdentity = ConversationState.getCurrent().getIdentity();
      String currentUserName = currentUserIdentity.getUserId();
      if(!currentUserIdentity.isMemberOf(allowedGroupId)) {
        return null;
      }*/
      String currentUserName="";
      PortalRequestContext portalRequestContext = PortalRequestContext.getCurrentInstance();
      String requestPath = "/" + portalRequestContext.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);
      ExoRouter.Route route = ExoRouter.route(requestPath);
      if (route != null) {
        UserRHDataDTO userRHDataDTO=new UserRHDataDTO();
        currentUserName = route.localArgs.get("streamOwnerId");
        try {
          userRHDataDTO=userDataService.getUserRHDataByUserId(currentUserName);
        } catch (NoResultException e) {
          userRHDataDTO.setUserId(currentUserName);
        }
        return userRHDataDTO;

      }
    } catch (Throwable e) {
      log.error(e);

    }
    return null;
  }


  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public UserRHDataDTO saveUserData(@Jackson UserRHDataDTO obj) {
    if (obj.getUserId()==null){
      try {
        String currentUserName="";
        PortalRequestContext portalRequestContext = PortalRequestContext.getCurrentInstance();
        String requestPath = "/" + portalRequestContext.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);
        ExoRouter.Route route = ExoRouter.route(requestPath);
        if (route != null) {
          currentUserName = route.localArgs.get("streamOwnerId");
          obj.setUserId(currentUserName);
        }
      } catch (Throwable e) {
        log.error("Can't get UserID");
        return obj;
      }
    }
    return userDataService.save(obj);
  }


  private ResourceBundle getResourceBundle(Locale locale) {
    return bundle = ResourceBundle.getBundle("locale.portlet.rh-management", locale, this.getClass().getClassLoader());
  }

  private ResourceBundle getResourceBundle() {
    if (bundle == null) {
      bundle = getResourceBundle(PortalRequestContext.getCurrentInstance().getLocale());
    }
    return bundle;
  }


}
