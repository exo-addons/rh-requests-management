package org.exoplatform.rhmanagement.portlet.rhUserInfo;

import juzu.*;
import juzu.impl.common.JSON;
import juzu.plugin.jackson.Jackson;
import juzu.template.Template;
import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.rhmanagement.dto.*;
import org.exoplatform.rhmanagement.services.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.*;



@SessionScoped
public class RHUserInfoController {
  private static Log log = ExoLogger.getLogger(RHUserInfoController.class);

  // Don't use inject to not get the merge of all resource bundles
  // @Inject
  ResourceBundle     bundle;

  @Inject
  IdentityManager identityManager;

  @Inject
  UserDataService userDataService;


  @Inject
  ConventionalVacationService conventionalVacationService;

  @Inject
  OfficialVacationService officialVacationService;


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
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public ContextDTO getData() {

    ContextDTO data = new ContextDTO();

      data.setCurrentUser(currentUser);
      Profile profile=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUser, false).getProfile();
      if(profile.getAvatarUrl()!=null){
        data.setCurrentUserAvatar(profile.getAvatarUrl());
      }else{
        data.setCurrentUserAvatar("/eXoSkin/skin/images/system/UserAvtDefault.png");
      }
      data.setCurrentUserName(profile.getFullName());

      UserRHDataDTO userRHDataDTO = userDataService.getUserRHDataByUserId(currentUser);
      if (userRHDataDTO != null) {
        data.setSickBalance(userRHDataDTO.getSickdaysBalance());
        data.setHolidaysBalance(userRHDataDTO.getHolidaysBalance());
        data.setHrId(userRHDataDTO.getHrId());
        data.setInsuranceId(userRHDataDTO.getInsuranceId());
        data.setSocialSecNumber(userRHDataDTO.getSocialSecNumber());
      }
      data.setConventionalVacations(conventionalVacationService.getConventionalVacations(0,0));
      data.setOfficialDays(officialVacationService.getOfficialVacationDays());
      data.setOfficialVacations(officialVacationService.getOfficialVacations(0,0));

    return data;
  }



  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public Response getFunctionalOrganization() {
    try {
      JSONArray org=new JSONArray();

      UserRHDataDTO currentUserData = userDataService.getUserRHDataByUserId(currentUser);
      if(currentUserData!=null){

        JSONObject current = new JSONObject();
        Identity id=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUserData.getUserId(), false);
        if(id!=null) {
          Profile profile = id.getProfile();
          current.put("userId",currentUserData.getUserId());
          current.put("fullName",profile.getFullName());
          current.put("manager",currentUserData.getFunctionalManager());
          current.put("job",profile.getPosition());
          org.put(current);
        }
        id=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUserData.getFunctionalManager(), false);
        if(id!=null) {
          Profile profile = id.getProfile();
          JSONObject manager = new JSONObject();
          manager.put("userId", currentUserData.getFunctionalManager());
          manager.put("fullName", profile.getFullName());
          manager.put("manager", "");
          manager.put("job", profile.getPosition());
          org.put(manager);
        }
      }

      List<UserRHDataDTO> subsList = new ArrayList<UserRHDataDTO>();
      subsList = userDataService.createAllSubordonatesDetailedList(currentUser,subsList);
      if(subsList.size()>0){
        for(UserRHDataDTO sub : subsList){
          Identity id=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, sub.getUserId(), false);
          if(id!=null) {
            Profile profile = id.getProfile();
            JSONObject data = new JSONObject();
            data.put("userId", sub.getUserId());
            data.put("fullName", profile.getFullName());
            data.put("manager", sub.getFunctionalManager());
            data.put("job", profile.getPosition());
            org.put(data);
          }
        }
      }


      return Response.ok(org.toString());
    } catch (Throwable e) {
      log.error("error while getting chart", e);
      return Response.status(500);
    }
  }


  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public Response getBundle() {
    try {
      if (!PropertyManager.isDevelopping() && bundleString != null && getResourceBundle().getLocale().equals(PortalRequestContext.getCurrentInstance().getLocale())) {
        return Response.ok(bundleString);
      }
      bundle = getResourceBundle(PortalRequestContext.getCurrentInstance().getLocale());
      JSON data = new JSON();
      Enumeration<String> enumeration = getResourceBundle().getKeys();
      while (enumeration.hasMoreElements()) {
        String key = (String) enumeration.nextElement();
        try {
          data.set(key.replaceAll("(.*)\\.", ""), getResourceBundle().getObject(key));
        } catch (MissingResourceException e) {
          // Nothing to do, this happens sometimes
        }
      }


      bundleString = data.toString();
      return Response.ok(bundleString);
    } catch (Throwable e) {
      log.error("error while getting bundele", e);
      return Response.status(500);
    }
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
