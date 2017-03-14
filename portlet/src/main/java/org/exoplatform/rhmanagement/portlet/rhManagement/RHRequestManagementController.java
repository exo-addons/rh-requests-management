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
import org.exoplatform.calendar.model.Event;
import org.exoplatform.calendar.model.query.CalendarQuery;
import org.exoplatform.calendar.model.query.EventQuery;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.ExtendedCalendarService;
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
import org.exoplatform.rhmanagement.services.*;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
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
  ExtendedCalendarService xCalendarService;

  @Inject
  CalendarService calendarService;

  @Inject
  RepositoryService repositoryService;

  @Inject
  ListenerService listenerService;


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
      CalendarQuery query = new CalendarQuery();
      query.setIdentity(ConversationState.getCurrent().getIdentity());
      for (Calendar cal: xCalendarService.getCalendarHandler().findCalendars(query)){
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
        if(status.equals(Utils.ALL)){
          return vacationRequestService.getVacationRequestsByUserId(currentUser,0,100);
        }else{
          return vacationRequestService.getVacationRequestsByUserIdAndStatus(currentUser,status,0,100);
        }

       }else{
        return vacationRequestService.getActiveVacationRequestsByUserId(currentUser,0,100);
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
      if(status!=null){
        for(ValidatorDTO validator : validatorService.getValidatorsByValidatorUserId(currentUser,0,100)){
          VacationRequestDTO requestDTO=vacationRequestService.getVacationRequest(validator.getRequestId());
          if(requestDTO!=null) {
            if(!status.equals(Utils.ALL)&&!status.equals(requestDTO.getStatus())) continue;
            dtos.add(requestDTO);
          }
        }
      }else{
        for(ValidatorDTO validator : validatorService.getValidatorsByValidatorUserId(currentUser,0,100)){
          VacationRequestDTO requestDTO=vacationRequestService.getVacationRequest(validator.getRequestId());
          if(requestDTO!=null) {
            if(Utils.DECLINED.equals(requestDTO.getStatus())||Utils.CANCELED.equals(requestDTO.getStatus())) continue;
            dtos.add(requestDTO);
          }
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
      List<CommentDTO> comments= commentService.getCommentsByRequestId(obj.getId(), Utils.COMMENT,0,100);
      for (CommentDTO comment : comments){
        Profile profile=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, comment.getPosterId(), false).getProfile();
        comment.setPosterName(profile.getFullName());
        if(profile.getAvatarUrl()!=null){
          comment.setPosterAvatar(profile.getAvatarUrl());
        }else{
          comment.setPosterAvatar("/eXoSkin/skin/images/system/UserAvtDefault.png");
        }
      }
      return comments;
    } catch (Throwable e) {
      log.error(e);
      return null;
    }
  }

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public List<CommentDTO> getHistory(@Jackson VacationRequestDTO obj) {
    try {
      List<CommentDTO> comments= commentService.getCommentsByRequestId(obj.getId(), Utils.HISTORY,0,100);
      for (CommentDTO comment : comments){
        Profile profile=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, comment.getPosterId(), false).getProfile();
        comment.setPosterName(profile.getFullName());
        if(profile.getAvatarUrl()!=null){
          comment.setPosterAvatar(profile.getAvatarUrl());
        }else{
          comment.setPosterAvatar("/eXoSkin/skin/images/system/UserAvtDefault.png");
        }
      }
      return comments;
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
    List<ValidatorDTO> validators=new ArrayList<ValidatorDTO>() ;
    try {
      for (ValidatorDTO validator :validatorService.getValidatorsByRequestId(obj.getId(),0,0)){
        validator.setValidatorName(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, validator.getValidatorUserId(), false).getProfile().getFullName());
        validators.add(validator);
      }
      return validators;
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
    VacationRequestDTO vr=obj.getVacationRequestDTO();
    vr.setUserId(currentUser);
    vr.setUserFullName(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUser, false).getProfile().getFullName());
    vr.setStatus(Utils.PENDING);
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
      val_.setReply(Utils.PENDING);
     validatorService.save(val_);
    }
    if (obj.getEXoCalendarId()!=""){
      shareCalendar_(vr,obj.getEXoCalendarId());
    }
    CommentDTO comment=new CommentDTO();
    comment.setRequestId(vr.getId());
    comment.setCommentText("requestCreated");
    comment.setPosterId(currentUser);
    comment.setPostedTime(new Date());
    comment.setCommentType(Utils.HISTORY);
    commentService.save(comment);
    try {
      listenerService.broadcast("exo.hrmanagement.requestCreation", "", obj);
    } catch (Exception e) {
     log.error("Cannot broadcast request creation event");
    }
    return getVacationRequestsOfCurrentUser(null);
  }


  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void saveComment(@Jackson CommentDTO obj) {
    obj.setPosterId(currentUser);
    obj.setPostedTime(new Date());
    obj.setCommentType(Utils.COMMENT);
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
    for(CommentDTO comment :commentService.getCommentsByRequestId(obj.getId(),Utils.COMMENT,0,0)){
      commentService.remove(comment);
    }
    for(CommentDTO comment :commentService.getCommentsByRequestId(obj.getId(),Utils.HISTORY,0,0)){
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
      validator.setReply(Utils.DECLINED);
      validatorService.save(validator);
      try {
        listenerService.broadcast("exo.hrmanagement.requestReply", "", validator);
      } catch (Exception e) {
        log.error("Cannot broadcast request reply event");
      }
    }
    obj.setStatus(Utils.DECLINED);
    vacationRequestService.save(obj,false);
    Set<String> managers = new HashSet<String>();
    for (ValidatorDTO validator :validatorService.getValidatorsByRequestId(obj.getId(),0,0)){
      managers.add(validator.getUserId());
    }

    try {
      listenerService.broadcast("exo.hrmanagement.requestUpadate", managers, obj);
    } catch (Exception e) {
      log.error("Cannot broadcast request reply event");
    }
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
        validator.setReply(Utils.APPROVED);
        validatorService.save(validator);
        try {
          listenerService.broadcast("exo.hrmanagement.requestReply", "", validator);
        } catch (Exception e) {
          log.error("Cannot broadcast request reply event");
        }
      }else{
        if (validator.getReply().equals(Utils.DECLINED)) declined=true;
        if (validator.getReply().equals(Utils.PENDING)) validated=false;
      }
      managers.add(validator.getUserId());
    }
   if(declined) {
      obj.setStatus(Utils.DECLINED);
      vacationRequestService.save(obj,false);
     CommentDTO comment=new CommentDTO();
     comment.setRequestId(obj.getId());
     comment.setCommentText("requestDeclined");
     comment.setPosterId(currentUser);
     comment.setPostedTime(new Date());
     comment.setCommentType(Utils.HISTORY);
     commentService.save(comment);
     try {
       listenerService.broadcast("exo.hrmanagement.requestUpadate", managers, obj);
     } catch (Exception e) {
       log.error("Cannot broadcast update request event");
     }

   }else if(validated){
     obj.setStatus(Utils.APPROVED);
     vacationRequestService.save(obj,false);
     CommentDTO comment=new CommentDTO();
     comment.setRequestId(obj.getId());
     comment.setCommentText("requestApproved");
     comment.setPosterId(currentUser);
     comment.setPostedTime(new Date());
     comment.setCommentType(Utils.HISTORY);
     commentService.save(comment);
     try {
       for (User rh : Utils.getRhManagers()){
         managers.add(rh.getUserName());
       }
     } catch (Exception e) {

     }
     try {
       listenerService.broadcast("exo.hrmanagement.requestUpadate", managers, obj);
     } catch (Exception e) {
       log.error("Cannot broadcast update request event");
     }
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
      Profile profile=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUser, false).getProfile();
      if(profile.getAvatarUrl()!=null){
        data.set("currentUserAvatar",profile.getAvatarUrl());
      }else{
        data.set("currentUserAvatar","/eXoSkin/skin/images/system/UserAvtDefault.png");
      }
      data.set("currentUserName",profile.getFullName());
      String employeesSpace = System.getProperty(Utils.EMPLOYEES_SPACE);
      if(employeesSpace==null){
        employeesSpace =Utils.EMPLOYEES_SPACE_DEFAULT;
      }
      data.set("employeesSpace",employeesSpace);
      UserRHDataDTO userRHDataDTO = userDataService.getUserRHDataByUserId(currentUser);
      if (userRHDataDTO != null) {
        data.set("sickBalance",userRHDataDTO.getSickdaysBalance());
        data.set("holidaysBalance",userRHDataDTO.getHolidaysBalance());
        data.set("hrId",userRHDataDTO.getHrId());
        data.set("insuranceId",userRHDataDTO.getInsuranceId());
        data.set("socialSecNumber",userRHDataDTO.getSocialSecNumber());
      }
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
      Event calendarEvent = new Event();
      calendarEvent.setId(calId+"_"+currentUser+"_"+obj.getId());
      calendarEvent.setEventCategoryId("defaultEventCategoryIdHoliday");
      calendarEvent.setEventCategoryName("defaultEventCategoryNameHoliday");
      calendarEvent.setSummary(obj.getUserFullName()+" Off");
      calendarEvent.setFromDateTime(obj.getFromDate());
      calendarEvent.setToDateTime(obj.getToDate());
      calendarEvent.setEventType(Event.TYPE_EVENT) ;
      calendarEvent.setParticipant(new String[]{currentUser}) ;
      calendarEvent.setParticipantStatus(new String[] {currentUser + ":"});
      calendarEvent.setCalendarId(calId);
      xCalendarService.getEventHandler().saveEvent(calendarEvent) ;
    }
  } catch (Exception e) {
    log.error("Exception while create user event", e);
  }
}


  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson

  public Response uploadFile(Long requestId, FileItem file) throws IOException {

      if (file != null) {
        Utils.saveFile(file,"requests","req_"+requestId);
        CommentDTO comment=new CommentDTO();
        comment.setRequestId(requestId);
        comment.setCommentText("attachementAdded");
        comment.setPosterId(currentUser);
        comment.setPostedTime(new Date());
        comment.setCommentType(Utils.HISTORY);
        commentService.save(comment);
          return Response.ok();
    } else {
      return Response.notFound();
    }
  }

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson

  public Response deleteFile(Long requestId, String fileName) throws IOException {

    Utils.deleteFile("Application Data/hrmanagement/requests/req_"+requestId+"/"+fileName);
    CommentDTO comment=new CommentDTO();
    comment.setRequestId(requestId);
    comment.setCommentText("attachementDeleted");
    comment.setPosterId(currentUser);
    comment.setPostedTime(new Date());
    comment.setCommentType(Utils.HISTORY);
    commentService.save(comment);
      return Response.ok();

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
      if (rootNode.hasNode("Application Data/hrmanagement/requests/req_"+requestId)) {
        Node requestsFolder= rootNode.getNode("Application Data/hrmanagement/requests/req_"+requestId);
                NodeIterator iter = requestsFolder.getNodes();
        while (iter.hasNext()) {
          Node node = (Node) iter.next();
          JSON attachment=new JSON();
          attachment.set("name",node.getName());
          attachment.set("url","/rest/jcr/repository/collaboration/Application Data/hrmanagement/requests/req_"+requestId+"/"+node.getName());
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
}
