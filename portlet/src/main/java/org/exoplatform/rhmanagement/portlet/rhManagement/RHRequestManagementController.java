package org.exoplatform.rhmanagement.portlet.rhManagement;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import juzu.HttpMethod;
import juzu.MimeType;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.SessionScoped;
import juzu.View;
import juzu.impl.common.JSON;
import juzu.io.Stream;
import juzu.plugin.jackson.Jackson;
import juzu.request.HttpContext;
import juzu.template.Template;

import org.apache.commons.fileupload.FileItem;
import org.exoplatform.calendar.model.Calendar;
import org.exoplatform.calendar.service.*;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.rhmanagement.dto.*;
import org.exoplatform.rhmanagement.integration.notification.RequestCreatedPlugin;
import org.exoplatform.rhmanagement.integration.notification.RequestRepliedPlugin;
import org.exoplatform.rhmanagement.integration.notification.RequestStatusChangedPlugin;
import org.exoplatform.rhmanagement.services.CommentService;
import org.exoplatform.rhmanagement.services.UserDataService;
import org.exoplatform.rhmanagement.services.VacationRequestService;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.rhmanagement.services.ValidatorService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.json.JSONArray;
import org.json.JSONObject;


@SessionScoped
public class RHRequestManagementController {
  private static Log log = ExoLogger.getLogger(RHRequestManagementController.class);

  // Don't use inject to not get the merge of all resource bundles
  // @Inject
  ResourceBundle     bundle;

  @Inject
  VacationRequestService vacationRequestService;

  @Inject
  ValidatorService validatorService;

  @Inject
  CommentService commentService;

  @Inject
  IdentityManager identityManager;

  @Inject
  ActivityManager activityManager;

  @Inject
  SpaceService spaceService;

  @Inject
  UserDataService userDataService;

  @Inject
  CalendarService calendarService;

  @Inject
  RepositoryService repositoryService;


  @Inject
  @Path("index.gtmpl")
  Template           indexTmpl;

  private String     bundleString;

  private final String APPROVED="approved";
  private final String DECLINED="declined";
  private final String PENDING="pending";

  private final String currentUser = ConversationState.getCurrent().getIdentity().getUserId();


  @View
  public Response.Content index() {
    return indexTmpl.ok();
  }

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public List<VacationRequestDTO> getVacationRequests() {
    try {
      return vacationRequestService.getVacationRequests(0,100);
    } catch (Throwable e) {
      log.error(e);
      return null;
    }
  }


  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public Response getUserCalendars() {
    JSONArray cals=new JSONArray();
    try {
      for (org.exoplatform.calendar.service.Calendar cal: calendarService.getUserCalendars(currentUser,true)){
        JSONObject data = new JSONObject();
        data.put("calId",cal.getId());
        data.put("calName",cal.getName());
        cals.put(data);
      }

      return Response.ok(cals.toString());
    } catch (Throwable e) {
      log.error("error while getting cals", e);
      return Response.status(500);
    }
  }

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public List<VacationRequestDTO> getVacationRequestsOfCurrentUser(String status) {
    try {
      if (status != null) {
        return vacationRequestService.getVacationRequestsByUserIdAndStatus(currentUser,status,0,100);
       }else{
        return vacationRequestService.getVacationRequestsByUserId(currentUser,0,100);
      }

    } catch (Throwable e) {
      log.error(e);
      return null;
    }
  }

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public List<VacationRequestDTO> getVacationRequestsForCurrentValidator(String status) {
    List<VacationRequestDTO> dtos = new ArrayList<VacationRequestDTO>();
    try {
      for(ValidatorDTO validator : validatorService.getValidatorsByValidatorUserId(currentUser,0,100)){
        VacationRequestDTO requestDTO=vacationRequestService.getVacationRequest(validator.getRequestId());
       if(requestDTO!=null) {
         if(status!=null&&requestDTO.getStatus()!=status) continue;
         dtos.add(requestDTO);
       }
      }
    } catch (Throwable e) {
      log.error(e);
      return null;
    }
    return dtos;
  }

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public VacationRequestDTO getVacationRequest (Long id) {
    try {
      return vacationRequestService.getVacationRequest(id);
    } catch (Throwable e) {
      log.error(e);
      return null;
    }
  }

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public List<CommentDTO> getComments(@Jackson VacationRequestDTO obj) {
    try {
      return commentService.getCommentsByRequestId(obj.getId(),0,100);
    } catch (Throwable e) {
      log.error(e);
      return null;
    }
  }

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public List<ValidatorDTO> getValidatorsByRequestID (@Jackson VacationRequestDTO obj) {
    try {
      return validatorService.getValidatorsByRequestId(obj.getId(),0,0);
    } catch (Throwable e) {
      log.error(e);
      return null;
    }
  }

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public List<EmployeesDTO>  getSubstitutesByRequestID(@Jackson VacationRequestDTO obj) {
    try {
      List<EmployeesDTO> userDTOs = new ArrayList<EmployeesDTO>();
      EmployeesDTO employeesDTO=new EmployeesDTO();
      for(String userId : obj.getSubstitute().split(",")){
        Identity id=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);

        if(id!=null){
          employeesDTO.setUserId(userId);
          employeesDTO.setName(id .getProfile().getFullName());
          userDTOs.add(employeesDTO);
        }
      }
      return userDTOs;
    } catch (Throwable e) {
      log.error(e);
      return null;
    }
  }


  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public List<VacationRequestDTO> saveVacationRequest(@Jackson VacationRequestWithManagersDTO obj) {
    Identity userId=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUser, false);
    VacationRequestDTO vr=obj.getVacationRequestDTO();
    vr.setUserId(currentUser);
    vr.setUserFullName(userId.getProfile().getFullName());
    vr.setStatus(PENDING);
    String substitutes="";

    for (String substitute : obj.getSubstitutes()){
      substitutes=substitutes.concat(substitute+",");
    }
    vr.setSubstitute(substitutes);
    vr=vacationRequestService.save(vr,true);
    obj.setVacationRequestDTO(vr);
    for (String manager : obj.getManagers()){
      ValidatorDTO val_=new ValidatorDTO();
      val_.setRequestId(vr.getId());
      val_.setValidatorUserId(manager);
      val_.setUserId(currentUser);
      val_.setReply(PENDING);
     validatorService.save(val_);
    }
    if (obj.getEXoCalendarId()!=""){
      shareCalendar_(vr,obj.getEXoCalendarId());
    }
    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(RequestCreatedPlugin.REQUEST, obj);
    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestCreatedPlugin.ID))).execute(ctx);
    return getVacationRequestsOfCurrentUser(null);
  }


  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void saveComment(@Jackson CommentDTO obj) {
    obj.setPosterId(currentUser);
    obj.setPostedTime(new Date());
    commentService.save(obj);
  }


  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void saveValiadator(@Jackson ValidatorDTO obj) {
    validatorService.save(obj);
  }

  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void shareCalendar(@Jackson VacationRequestWithManagersDTO obj) {
    shareCalendar_(obj.getVacationRequestDTO(),obj.getEXoCalendarId());
  }

  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public List<VacationRequestDTO> deleteRequest(@Jackson VacationRequestDTO obj) {
    for(ValidatorDTO validator :validatorService.getValidatorsByRequestId(obj.getId(),0,0)){
      validatorService.remove(validator);
    }
    for(CommentDTO comment :commentService.getCommentsByRequestId(obj.getId(),0,0)){
      commentService.remove(comment);
    }
    vacationRequestService.remove(obj);
    return getVacationRequestsOfCurrentUser(null);
  }

  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public VacationRequestDTO declineRequest(@Jackson VacationRequestDTO obj) {
    for(ValidatorDTO validator :validatorService.getValidatorsByValidatorUserIdandRequestId(currentUser,obj.getId())){
      validator.setReply(DECLINED);
      validatorService.save(validator);
      NotificationContext ctx = NotificationContextImpl.cloneInstance().append(RequestRepliedPlugin.VALIDATOR, validator);
      ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestRepliedPlugin.ID))).execute(ctx);
    }
    obj.setStatus(DECLINED);
    vacationRequestService.save(obj,false);
    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(RequestStatusChangedPlugin.REQUEST, obj);
    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestStatusChangedPlugin.ID))).execute(ctx);
    return obj;
  }


  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public VacationRequestDTO approveRequest(@Jackson VacationRequestDTO obj) {
    Boolean validated=true;
    Boolean declined=false;
    Set<String> managers = new HashSet<String>();
    for (ValidatorDTO validator :validatorService.getValidatorsByRequestId(obj.getId(),0,0)){
      if(validator.getValidatorUserId().equals(currentUser)){
        validator.setReply(APPROVED);
        validatorService.save(validator);
        NotificationContext ctx = NotificationContextImpl.cloneInstance().append(RequestRepliedPlugin.VALIDATOR, validator);
        ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestRepliedPlugin.ID))).execute(ctx);
      }else{
        if (validator.getReply().equals(DECLINED)) declined=true;
        if (validator.getReply().equals(PENDING)) validated=false;
      }
      managers.add(validator.getUserId());
    }
   if(declined) {
      obj.setStatus(DECLINED);
      vacationRequestService.save(obj,false);
     NotificationContext ctx = NotificationContextImpl.cloneInstance().append(RequestStatusChangedPlugin.REQUEST, obj).append(RequestStatusChangedPlugin.MANAGERS, managers);
     ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestStatusChangedPlugin.ID))).execute(ctx);
   }else if(validated){
     obj.setStatus(APPROVED);
     vacationRequestService.save(obj,false);
     NotificationContext ctx = NotificationContextImpl.cloneInstance().append(RequestStatusChangedPlugin.REQUEST, obj).append(RequestStatusChangedPlugin.MANAGERS, managers);
     ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestStatusChangedPlugin.ID))).execute(ctx);
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

      data.set("currentUser",currentUser);
      UserRHDataDTO userRHDataDTO = userDataService.getUserRHDataByUserId(currentUser);
      data.set("sickBalance",userRHDataDTO.getNbrSickdays());
      data.set("holidaysBalance",userRHDataDTO.getNbrHolidays());
      bundleString = data.toString();
      return Response.ok(bundleString);
    } catch (Throwable e) {
      log.error("error while getting bundele", e);
      return Response.status(500);
    }
  }


  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public Response getContext() {
    try {
      JSON data = new JSON();
      data.set("currentUser",currentUser);
      UserRHDataDTO userRHDataDTO = userDataService.getUserRHDataByUserId(currentUser);
      data.set("sickBalance",userRHDataDTO.getNbrSickdays());
      data.set("holidaysBalance",userRHDataDTO.getNbrHolidays());
      return Response.ok(data.toString());
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

private void shareCalendar_(VacationRequestDTO obj, String calId){
  try {
    Calendar cal=calendarService.getCalendarById(calId);
    if(cal!=null){
      CalendarEvent calendarEvent = new CalendarEvent();
      calendarEvent.setEventCategoryId("defaultEventCategoryIdHoliday");
      calendarEvent.setEventCategoryName("defaultEventCategoryNameHoliday");
      calendarEvent.setSummary(currentUser+" Off");
      calendarEvent.setFromDateTime(obj.getFromDate());
      calendarEvent.setToDateTime(obj.getToDate());
      calendarEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
      calendarEvent.setParticipant(new String[]{currentUser}) ;
      calendarEvent.setParticipantStatus(new String[] {currentUser + ":"});
      calendarService.saveUserEvent(currentUser, calId, calendarEvent, true) ;
    }
  } catch (Exception e) {
    log.error("Exception while create user event", e);
  }
}


  /**
   * method uploadFile() records an image in AddMembersStorage
   *
   * @param httpContext
   * @param file
   * @return Response.Content
   * @throws java.io.IOException
   */
  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson

  public Response uploadFile(Long requestId, FileItem file) throws IOException {

      if (file != null) {
        saveRequestAttachement(file, requestId);
        return Response.ok();



    } else {
      return Response.notFound();
    }
  }


  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson

  public Response  getRequestAttachements(@Jackson VacationRequestDTO obj) {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    List<JSON> atts = new ArrayList<JSON>();
    try {
      Session session = sessionProvider.getSession("collaboration",
              repositoryService.getCurrentRepository());
      Node rootNode = session.getRootNode();
      long requestId=obj.getId();
      if (rootNode.hasNode("hrmanagement/requests/req_"+requestId)) {
        Node requestsFolder= rootNode.getNode("hrmanagement/requests/req_"+requestId);
                NodeIterator iter = requestsFolder.getNodes();
        while (iter.hasNext()) {
          Node node = (Node) iter.next();
          JSON attachment=new JSON();
          attachment.set("name",node.getName());
          attachment.set("url","/rest/jcr/repository/collaboration/hrmanagement/requests/req_"+requestId+"/"+node.getName());
          atts.add(attachment);
        }
        return Response.ok(atts.toString());
      }else{
        return Response.ok();
      }

    } catch (Exception e) {

      log.error("Error while getting attachements: ", e.getMessage());
      return null;
    } finally {
      sessionProvider.close();
    }
  }

  /**
   * Save  file in the jcr
   *
   * @param item, item file
   */

  public void saveRequestAttachement(FileItem item , long requestId) {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    try {
      Session session = sessionProvider.getSession("collaboration",
              repositoryService.getCurrentRepository());
      Node rootNode = session.getRootNode();
      if (!rootNode.hasNode("hrmanagement")) {
        rootNode.addNode("hrmanagement", "nt:folder");
        session.save();
      }
      Node applicationDataNode = rootNode.getNode("hrmanagement");
      if (!applicationDataNode.hasNode("requests")) {
        applicationDataNode.addNode("requests", "nt:folder");
        session.save();
      }
      Node requestsFolder= applicationDataNode.getNode("requests");

      Node reqFolder = null;
      if (!requestsFolder.hasNode("req_"+requestId )) {
        requestsFolder.addNode("req_"+requestId, "nt:folder");
        session.save();
      }
      reqFolder = requestsFolder.getNode("req_"+requestId);

      Node fileNode = reqFolder.addNode(item.getName(), "nt:file");
      Node jcrContent = fileNode.addNode("jcr:content", "nt:resource");
      jcrContent.setProperty("jcr:data", item.getInputStream());
      jcrContent.setProperty("jcr:lastModified", java.util.Calendar.getInstance());
      jcrContent.setProperty("jcr:encoding", "UTF-8");
      jcrContent.setProperty("jcr:mimeType", item.getContentType());
      reqFolder.save();
      session.save();
    } catch (Exception e) {
      log.error("Error while saving the file: ", e.getMessage());
    } finally {
      sessionProvider.close();
    }
  }
}
