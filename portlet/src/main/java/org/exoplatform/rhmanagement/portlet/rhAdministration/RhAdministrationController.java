package org.exoplatform.rhmanagement.portlet.rhAdministration;

import juzu.HttpMethod;
import juzu.MimeType;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.View;
import juzu.impl.common.JSON;
import juzu.plugin.jackson.Jackson;
import juzu.template.Template;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.rhmanagement.dto.EmployeesDTO;
import org.exoplatform.rhmanagement.dto.UserRHDataDTO;
import org.exoplatform.rhmanagement.dto.VacationRequestDTO;
import org.exoplatform.rhmanagement.dto.ValidatorDTO;
import org.exoplatform.rhmanagement.integration.notification.RequestRepliedPlugin;
import org.exoplatform.rhmanagement.integration.notification.RequestStatusChangedPlugin;
import org.exoplatform.rhmanagement.services.UserDataService;
import org.exoplatform.rhmanagement.services.VacationRequestService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;


import javax.inject.Inject;
import java.util.*;

/**
 * Created by exo on 8/3/16.
 */
public class RhAdministrationController {
/*  private static final String SUPPORT_TEAM_NAME_DEFAULT = "support-team";
  private static final String SUPPORT_GROUP_NAME_CONFIGURATION = "exo.addon.cs.support.group.name";*/
  private static Log  LOG = ExoLogger.getLogger(RhAdministrationController.class);
  private String     bundleString;
  ResourceBundle     bundle;
  private final String APPROVED="approved";
  private final String VALIDATED="validated";
  private final String CANCELED="canceled";

  @Inject
  UserDataService userDataService;

  @Inject
  VacationRequestService vacationRequestService;

  @Inject
  IdentityManager identityManager;


  @Inject
  OrganizationService orgService;

  @Inject
  @Path("index.gtmpl")
  Template            indexTmpl;

  @View
  public Response.Content index() {
    return indexTmpl.ok();
  }

  @Ajax
  @Resource
  @MimeType.JSON
  @Jackson
  public List<EmployeesDTO> getAllUsersRhData() {
    try {
      List<EmployeesDTO> userRHDataDTOS = userDataService.getAllUsersRhData(0,0);
      return userRHDataDTOS;
    } catch (Throwable e) {
      LOG.error("Exception when retrieving tickets" + e);
      return null;
    }
  }

  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public UserRHDataDTO saveUserRHData(@Jackson EmployeesDTO user) throws Exception {
    try {

        return userDataService.save(user.getHrData());

    } catch (Exception e) {
      LOG.error("Error when updating userData", e);
      throw e;
    }
  }

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public List<VacationRequestDTO> getVacationRequestsbyUserId(String userId) {
    try {

        return vacationRequestService.getVacationRequestsByUserId(userId,0,100);

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
        Profile profile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false).getProfile();
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
  public List<VacationRequestDTO> getAllVacationRequests() {
    try {

      return vacationRequestService.getVacationRequests(0,100);

    } catch (Throwable e) {
      LOG.error(e);
      return null;
    }
  }



  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public VacationRequestDTO validateRequest(@Jackson VacationRequestDTO obj) {
    if(obj.getStatus().equals(APPROVED)) {
      obj.setStatus(VALIDATED);
      obj=vacationRequestService.save(obj,false);
      UserRHDataDTO userRHDataDTO=userDataService.getUserRHDataByUserId(obj.getUserId());
      if(obj.getType().equals("holiday")){
        float holidays=userRHDataDTO.getNbrHolidays();
        float nbDays=obj.getDaysNumber();
        userRHDataDTO.setNbrHolidays(holidays-nbDays);
        userDataService.save(userRHDataDTO);
      }if(obj.getType().equals("sick")){
        float sickdays=userRHDataDTO.getNbrSickdays();
        float nbDays=obj.getDaysNumber();
        userRHDataDTO.setNbrSickdays(sickdays-nbDays);
        userDataService.save(userRHDataDTO);
      }
/*      NotificationContext ctx = NotificationContextImpl.cloneInstance().append(RequestStatusChangedPlugin.REQUEST, obj).append(RequestStatusChangedPlugin.MANAGERS, managers);
      ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestStatusChangedPlugin.ID))).execute(ctx);*/
    }else {

    }
    return obj;
  }

  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public VacationRequestDTO cancelRequest(@Jackson VacationRequestDTO obj) {
    if(obj.getStatus().equals(VALIDATED)) {
      obj.setStatus(CANCELED);
      obj=vacationRequestService.save(obj,false);
      UserRHDataDTO userRHDataDTO=userDataService.getUserRHDataByUserId(obj.getUserId());
      if(obj.getType().equals("holiday")){
        float holidays=userRHDataDTO.getNbrHolidays();
        float nbDays=obj.getDaysNumber();
        userRHDataDTO.setNbrHolidays(holidays+nbDays);
        userDataService.save(userRHDataDTO);
      }if(obj.getType().equals("sick")){
        userRHDataDTO.setNbrSickdays(userRHDataDTO.getNbrSickdays()+obj.getDaysNumber());
        userDataService.save(userRHDataDTO);
      }
/*      NotificationContext ctx = NotificationContextImpl.cloneInstance().append(RequestStatusChangedPlugin.REQUEST, obj).append(RequestStatusChangedPlugin.MANAGERS, managers);
      ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestStatusChangedPlugin.ID))).execute(ctx);*/
    }else {
      obj.setStatus(CANCELED);
      obj=vacationRequestService.save(obj,false);
    }
    return obj;
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



/*  *//**
   * This service will return the names of all support team members
   * @param ticketDTO
   * @return
   *//*
  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public List<User> getSupportUsernames(@Jackson TicketDTO ticketDTO) {
    try {
      String groupId = PropertyManager.getProperty(SUPPORT_GROUP_NAME_CONFIGURATION);
      if(groupId == null || groupId.isEmpty()){
        groupId = SUPPORT_TEAM_NAME_DEFAULT;
      }
      ListAccess<User> EngSupportList = organizationService.getUserHandler().findUsersByGroupId(groupId);
      User[] users = EngSupportList.load(0, EngSupportList.getSize() < 30 ? EngSupportList.getSize() : 30);
      List<User> engSupport = new ArrayList<>();
      for (User user : users) {
        engSupport.add(user);
      }
      return engSupport;
    } catch (Throwable e) {
      LOG.error("Can't retrieve the list of support engineers " + e);
      return null;
    }
  }

  *//**
   * this method will return the list of severities
   * @return List of ticket severity
   *//*
  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public List<String> getSeverities(@Jackson TicketDTO ticketDTO) {
      List<String> severities = new ArrayList<>();
      if(ticketDTO.getType().equals(IssueType.INCIDENT)){
        severities.add(IssueSeverity.SEVERITY_1.name());
        severities.add(IssueSeverity.SEVERITY_2.name());
        severities.add(IssueSeverity.SEVERITY_3.name());
      } else if(ticketDTO.getType().equals(IssueType.INFORMATION)) {
        severities.add(IssueSeverity.SEVERITY_4.name());
      }
      return severities;
  }

  *//**
   * this method returns the static list of ticket types
   * @param ticketDTO
   * @return
   *//*
  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public List<String> getTicketTypes(@Jackson TicketDTO ticketDTO) {
      List<String> ticketTypes = new ArrayList<>();
      ticketTypes.add(IssueType.INCIDENT.name());
      ticketTypes.add(IssueType.INFORMATION.name());
      return ticketTypes;
  }*/


}
