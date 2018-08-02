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
import org.json.JSONException;
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
  public Response getFunctionalOrganization(String userId) {
    try {
      JSONArray orgf=new JSONArray();

      UserRHDataDTO currentUserData = userDataService.getUserRHDataByUserId(userId);


        JSONObject current = new JSONObject();
        Identity id= null;
        try {
          id = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);
          if(id!=null) {
            Profile profile = id.getProfile();
            current.put("userId",userId);
            current.put("fullName",profile.getFullName());
            current.put("manager", currentUserData!=null ? currentUserData.getFunctionalManager() : "");
            current.put("job",profile.getPosition());
              if (profile.getAvatarUrl() != null) {
                  current.put("avatar",profile.getAvatarUrl());
              } else {
                  current.put("avatar","/eXoSkin/skin/images/system/UserAvtDefault.png");
              }
            orgf.put(current);
          }
        } catch (Exception e) {
          log.warn("cannot get user "+currentUserData.getUserId()+" informations");
        }
        if (currentUserData!=null && currentUserData.getFunctionalManager()!=null) {
        try {
            id = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUserData.getFunctionalManager(), false);
            if (id != null) {
              Profile profile = id.getProfile();
              JSONObject manager = new JSONObject();
              manager.put("userId", currentUserData.getFunctionalManager());
              manager.put("fullName", profile.getFullName());
              manager.put("manager", "");
              manager.put("job", profile.getPosition());
                if (profile.getAvatarUrl() != null) {
                    manager.put("avatar",profile.getAvatarUrl());
                } else {
                    manager.put("avatar","/eXoSkin/skin/images/system/UserAvtDefault.png");
                }
              orgf.put(manager);
            }
          } catch(Exception e){
            log.warn("cannot get user " + currentUserData.getFunctionalManager() + " informations");
          }
        }


      List<UserRHDataDTO> fsubsList = new ArrayList<UserRHDataDTO>();
      fsubsList = userDataService.createFSubordonatesDetailedList(userId,fsubsList);
      if(fsubsList.size()>0){
        for(UserRHDataDTO sub : fsubsList){
          try {
            id=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, sub.getUserId(), false);
            if(id!=null) {
              Profile profile = id.getProfile();
              JSONObject data = new JSONObject();
              data.put("userId", sub.getUserId());
              data.put("fullName", profile.getFullName());
              data.put("manager", sub.getFunctionalManager());
              data.put("job", profile.getPosition());
                if (profile.getAvatarUrl() != null) {
                    data.put("avatar",profile.getAvatarUrl());
                } else {
                    data.put("avatar","/eXoSkin/skin/images/system/UserAvtDefault.png");
                }
              orgf.put(data);
            }
          } catch (Exception e) {
            log.warn("cannot get user "+sub.getUserId()+" informations");
          }
        }
      }


      return Response.ok(orgf.toString());
    } catch (Throwable e) {
      log.error("error while getting chart", e);
      return Response.status(500);
    }
  }

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public Response getHierarchicalOrganization(String userId) {
    try {
      JSONArray orgh=new JSONArray();

      UserRHDataDTO currentUserData = userDataService.getUserRHDataByUserId(userId);


        JSONObject current = new JSONObject();
        Identity id= null;
        try {
          id = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);
          if(id!=null) {
            Profile profile = id.getProfile();
            current.put("userId",userId);
            current.put("fullName",profile.getFullName());
            current.put("manager", currentUserData!=null ? currentUserData.getHierarchicalManager() : "");
            current.put("job",profile.getPosition());
              if (profile.getAvatarUrl() != null) {
                  current.put("avatar",profile.getAvatarUrl());
              } else {
                  current.put("avatar","/eXoSkin/skin/images/system/UserAvtDefault.png");
              }
            orgh.put(current);
          }
        } catch (Exception e) {
          log.warn("cannot get user "+currentUserData.getUserId()+" informations");
        }
        if (currentUserData!=null && currentUserData.getHierarchicalManager()!=null) {
        try {
            id = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUserData.getHierarchicalManager(), false);
            if (id != null) {
              Profile profile = id.getProfile();
              JSONObject manager = new JSONObject();
              manager.put("userId", currentUserData.getHierarchicalManager());
              manager.put("fullName", profile.getFullName());
              manager.put("manager", "");
              manager.put("job", profile.getPosition());
                if (profile.getAvatarUrl() != null) {
                    manager.put("avatar",profile.getAvatarUrl());
                } else {
                    manager.put("avatar","/eXoSkin/skin/images/system/UserAvtDefault.png");
                }
              orgh.put(manager);
            }
          } catch(Exception e){
            log.warn("cannot get user " + currentUserData.getHierarchicalManager() + " informations");
          }
        }


      List<UserRHDataDTO> hsubsList = new ArrayList<UserRHDataDTO>();
      hsubsList = userDataService.createHSubordonatesDetailedList(userId,hsubsList);
      if(hsubsList.size()>0){
        for(UserRHDataDTO sub : hsubsList){
          try {
            id=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, sub.getUserId(), false);
            if(id!=null) {
              Profile profile = id.getProfile();
              JSONObject data = new JSONObject();
              data.put("userId", sub.getUserId());
              data.put("fullName", profile.getFullName());
              data.put("manager", sub.getHierarchicalManager());
              data.put("job", profile.getPosition());
              if (profile.getAvatarUrl() != null) {
                data.put("avatar",profile.getAvatarUrl());
              } else {
                data.put("avatar","/eXoSkin/skin/images/system/UserAvtDefault.png");
              }
              orgh.put(data);
            }
          } catch (Exception e) {
            log.warn("cannot get user "+sub.getUserId()+" informations");
          }
        }
      }

      return Response.ok(orgh.toString());
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
