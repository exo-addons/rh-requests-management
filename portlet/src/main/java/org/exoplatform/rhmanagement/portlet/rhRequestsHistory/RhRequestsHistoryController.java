package org.exoplatform.rhmanagement.portlet.rhRequestsHistory;

import juzu.*;
import juzu.impl.common.JSON;
import juzu.plugin.jackson.Jackson;
import juzu.template.Template;
import org.apache.commons.fileupload.FileItem;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.rhmanagement.dto.*;
import org.exoplatform.rhmanagement.services.*;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import java.io.IOException;
import java.util.*;

/**
 * Created by exo on 8/3/16.
 */
public class RhRequestsHistoryController {

  private static Log  LOG = ExoLogger.getLogger(RhRequestsHistoryController.class);
  private String     bundleString;
  ResourceBundle     bundle;
  @Inject
  UserDataService userDataService;


  @Inject
  IdentityManager identityManager;


  @Inject
  OrganizationService orgService;

  @Inject
  BalanceHistoryService balanceHistoryService;


  @Inject
  @Path("index.gtmpl")
  Template            indexTmpl;

  @View
  public Response.Content index() {
    return indexTmpl.ok();
  }

  private final String currentUser = ConversationState.getCurrent().getIdentity().getUserId();



  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public List<BalanceHistoryDTO> getBalanceHistoryByUserId(String userId, Long from, Long to) {
    try {
      if(userId.equals("")){
        return balanceHistoryService.getBalanceHistoryByDate(from, to,0,0);
      }
        return balanceHistoryService.getBalanceHistoryByUserId(userId, from, to,0,0);
    } catch (Throwable e) {
      LOG.error(e);
      return null;
    }
  }

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public EmployeesDTO getUser(String userId) {
    try {
      UserHandler uh = orgService.getUserHandler();
      if(uh.findUserByName(userId)==null){
        return null;
      } else {
        Profile profile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId).getProfile();
        EmployeesDTO employee = new EmployeesDTO();
        employee.setName(profile.getFullName());
        employee.setUserId(userId);
        employee.setEmail(profile.getEmail());
        employee.setGender(profile.getGender());
        employee.setJobTitle(profile.getPosition());
        employee.setAvatar(profile.getAvatarUrl());

        UserRHDataDTO userRHDataDTO = userDataService.getUserRHDataByUserId(userId);
        if (userRHDataDTO == null) {
          userRHDataDTO=new UserRHDataDTO();
          userRHDataDTO.setUserId(userId);
        }
        employee.setHrData(userRHDataDTO);
        return employee;
      }
    } catch (Throwable e) {
      LOG.error(e);
      return null;
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
      TimeZone userTimeZone=Utils.getUserTimezone(currentUser);
      data.set("userTimeZone",userTimeZone.toString());
      data.set("offset",userTimeZone.getOffset(new Date().getTime()));
      int offset = userTimeZone.getOffset(new Date().getTime()) / 3600000;
      String timeZone = ((offset < 0) ? "-" : "") + String.format("%02d", Math.abs(offset))+ "00";
      data.set("timeZone", timeZone);
      data.set("currentUser",currentUser);
      bundleString = data.toString();
      return Response.ok(bundleString);
    } catch (Throwable e) {
      LOG.error("error while getting categories", e);
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
