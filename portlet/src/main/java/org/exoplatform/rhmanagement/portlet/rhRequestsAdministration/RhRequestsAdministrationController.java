package org.exoplatform.rhmanagement.portlet.rhRequestsAdministration;

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
public class RhRequestsAdministrationController {

  private static Log  LOG = ExoLogger.getLogger(RhRequestsAdministrationController.class);
  private String     bundleString;
  ResourceBundle     bundle;
  private final String APPROVED="approved";
  private final String VALIDATED="validated";
  private final String CANCELED="canceled";
  private final String RH_MANAGER = "lastAccess";

  @Inject
  UserDataService userDataService;

  @Inject
  CommentService commentService;

  @Inject
  ValidatorService validatorService;

  @Inject
  VacationRequestService vacationRequestService;

  @Inject
  IdentityManager identityManager;


  @Inject
  OrganizationService orgService;

  @Inject
  RepositoryService repositoryService;

  @Inject
  ListenerService listenerService;

  @Inject
  BalanceHistoryService balanceHistoryService;

  @Inject
  ConventionalVacationService conventionalVacationService;

  @Inject
  OfficialVacationService officialVacationService;

  @Inject
  SettingService settingService;

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
  public List<VacationRequestDTO> getVacationRequestsbyUserId(String userId, String vrFilter) {
    try {

      if (vrFilter != null&&vrFilter.equals(Utils.ACTIVE)) {

        return vacationRequestService.getActiveVacationRequestsByUserId(userId,0,100);
      }else{
        return vacationRequestService.getVacationRequestsByUserId(userId,0,100);
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
  public VacationRequestsPageDTO getVacationRequests(String vrFilter, int offset, int limit) {
    try {
      VacationRequestsPageDTO vacationRequestsPageDTO =new VacationRequestsPageDTO();
      if (vrFilter != null&&vrFilter.equals(Utils.ACTIVE)) {
        vacationRequestsPageDTO.setVacationRequests(vacationRequestService.getActivVacationRequests(offset,limit));
        vacationRequestsPageDTO.setSize(vacationRequestService.getActiveVacationRequestsCount());
          return vacationRequestsPageDTO;
        }else if(vrFilter != null&&vrFilter.equals(Utils.WAITING)){
        vacationRequestsPageDTO.setVacationRequests(vacationRequestService.getWaitingVacationRequests(offset,limit));
        vacationRequestsPageDTO.setSize(vacationRequestService.getWaitingVacationRequestsCount());
        return vacationRequestsPageDTO;
      }else if(vrFilter != null&&(vrFilter.equals(Utils.SICK)||vrFilter.equals(Utils.HOLIDAY)||vrFilter.equals(Utils.LEAVE))){
        vacationRequestsPageDTO.setVacationRequests(vacationRequestService.getVacationRequestsByType(vrFilter,offset,limit));
        vacationRequestsPageDTO.setSize(vacationRequestService.getVacationRequestsByTypeCount(vrFilter));
        return vacationRequestsPageDTO;
      }else{
        vacationRequestsPageDTO.setVacationRequests(vacationRequestService.getVacationRequests(offset,limit));
        vacationRequestsPageDTO.setSize(vacationRequestService.getVacationRequestesCount());
        return vacationRequestsPageDTO;
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
  public List<VacationRequestDTO> getActivVacationRequests() {
    try {
      List<VacationRequestDTO> vrs = vacationRequestService.getActivVacationRequests(0,100);
      for (VacationRequestDTO vr : vrs){
        vr.setExpanded(false);
      }
      return vrs;
    } catch (Throwable e) {
      LOG.error(e);
      return null;
    }
  }



  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public List<VacationRequestDTO> getWaitingVacationRequests() {
    try {
      List<VacationRequestDTO> vrs = vacationRequestService.getWaitingVacationRequests(0,100);
      for (VacationRequestDTO vr : vrs){
        vr.setExpanded(false);
      }
      return vrs;
    } catch (Throwable e) {
      LOG.error(e);
      return null;
    }
  }



  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public UserRHDataDTO validateRequest(@Jackson VacationRequestDTO obj) {

    UserRHDataDTO userRHDataDTO=null;
    if(!obj.getStatus().equals(VALIDATED)) {
      obj.setStatus(VALIDATED);
      obj=vacationRequestService.save(obj,false);
      userRHDataDTO=userDataService.getUserRHDataByUserId(obj.getUserId());
      if(obj.getType().equals("holiday")){
        float holidays=userRHDataDTO.getHolidaysBalance();
        float nbDays=obj.getDaysNumber();
        userRHDataDTO.setHolidaysBalance(holidays-nbDays);
        userDataService.save(userRHDataDTO);
        Utils.addBalanceHistoryEntry(obj, userRHDataDTO,holidays, userRHDataDTO.getSickdaysBalance(),"holidayValidated",currentUser);
      }if(obj.getType().equals("sick")){
        float sickdays=userRHDataDTO.getSickdaysBalance();
        float nbDays=obj.getDaysNumber();
        userRHDataDTO.setSickdaysBalance(sickdays-nbDays);
        userDataService.save(userRHDataDTO);
        Utils.addBalanceHistoryEntry(obj, userRHDataDTO,userRHDataDTO.getHolidaysBalance(), sickdays,"sickValidated",currentUser);
      }
      CommentDTO comment=new CommentDTO();
      comment.setRequestId(obj.getId());
      comment.setCommentText("requestValidated");
      comment.setPosterId(currentUser);
      comment.setCommentType(Utils.HISTORY);
      commentService.save(comment);


      try {
        listenerService.broadcast("exo.hrmanagement.requestUpadate", currentUser, obj);
      } catch (Exception e) {
        LOG.error("Cannot broadcast update request event");
      }
    }
    return userRHDataDTO;
  }

  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public UserRHDataDTO cancelRequest(@Jackson VacationRequestDTO obj) {
    UserRHDataDTO userRHDataDTO=null;
    if(!obj.getStatus().equals(CANCELED)) {

      if(obj.getStatus().equals(VALIDATED)) {
        userRHDataDTO=userDataService.getUserRHDataByUserId(obj.getUserId());
        if (obj.getType().equals("holiday")) {
          float holidays = userRHDataDTO.getHolidaysBalance();
          float nbDays = obj.getDaysNumber();
          userRHDataDTO.setHolidaysBalance(holidays + nbDays);
          userDataService.save(userRHDataDTO);
          Utils.addBalanceHistoryEntry(obj, userRHDataDTO,holidays, userRHDataDTO.getSickdaysBalance(),"holidayCanceled",currentUser);
        }
        if (obj.getType().equals("sick")) {
          float sickdays=userRHDataDTO.getSickdaysBalance();
          float nbDays=obj.getDaysNumber();
          userRHDataDTO.setSickdaysBalance(sickdays+nbDays);
          userDataService.save(userRHDataDTO);
          Utils.addBalanceHistoryEntry(obj, userRHDataDTO,userRHDataDTO.getHolidaysBalance(), sickdays,"sickCanceled",currentUser);
        }
      }
      obj.setStatus(CANCELED);
      obj=vacationRequestService.save(obj,false);

      CommentDTO comment=new CommentDTO();
      comment.setRequestId(obj.getId());
      comment.setCommentText("requestCanceled");
      comment.setPosterId(currentUser);
      comment.setCommentType(Utils.HISTORY);
      commentService.save(comment);


      try {
        listenerService.broadcast("exo.hrmanagement.requestUpadate", currentUser, obj);
      } catch (Exception e) {
        LOG.error("Cannot broadcast update request event");
      }
    }
    return userRHDataDTO;
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
      data.set("vRCount",vacationRequestService.getVacationRequestesCount());
      String rhManager="";
      if(settingService.get(Context.GLOBAL, Scope.GLOBAL, RH_MANAGER)!=null) rhManager=settingService.get(Context.GLOBAL, Scope.GLOBAL, RH_MANAGER).getValue().toString();
      data.set("rhManager",rhManager);
      return Response.ok(data.toString());
    } catch (Throwable e) {
      LOG.error("error while getting context", e);
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


  /**
   * method uploadFile() records an image in AddMembersStorage
   *
   */
  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson

  public Response uploadFile(String userId, FileItem file) throws IOException {

    if (file != null) {
      Utils.saveFile(file,"employees","emp_"+userId);
      return Response.ok();
    } else {
      return Response.notFound();
    }
  }


  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson

  public Response deleteFile(String userId, String fileName) throws IOException {
    Utils.deleteFile("Application Data/hrmanagement/employees/emp_"+userId+"/"+fileName);
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

      LOG.error("Error while getting attachements: ", e.getMessage());
      return null;
    } finally {
      sessionProvider.close();
    }
  }

  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void saveComment(@Jackson CommentDTO obj) {
    obj.setPosterId(currentUser);
    obj.setCommentType(Utils.COMMENT);
    commentService.save(obj);
    try {
      listenerService.broadcast("exo.hrmanagement.requestComment", currentUser, obj);
    } catch (Exception e) {
      LOG.error("Cannot broadcast comment request event");
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
      LOG.error(e);
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
      LOG.error(e);
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
      LOG.error(e);
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
      for(String userId : obj.getSubstitute().split(",")){
        Identity id=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);

        if(id!=null){
          EmployeesDTO employeesDTO=new EmployeesDTO();
          employeesDTO.setUserId(userId);
          employeesDTO.setName(id .getProfile().getFullName());
          userDTOs.add(employeesDTO);
        }
      }
      return userDTOs;
    } catch (Throwable e) {
      LOG.error(e);
      return null;
    }
  }



  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public VacationRequestDTO getVacationRequest (Long id) {
    try {
      VacationRequestDTO vr=vacationRequestService.getVacationRequest(id);
      if (Utils.canView(vr,currentUser)) return vr;
      return null;
    } catch (Throwable e) {
      LOG.error(e);
      return null;
    }
  }



  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void updateVacationRequest(@Jackson VacationRequestDTO obj) {



    if(obj.getStatus().equals(VALIDATED)) {
      UserRHDataDTO userRHDataDTO=userDataService.getUserRHDataByUserId(obj.getUserId());
      VacationRequestDTO oldVr = vacationRequestService.getVacationRequest(obj.getId());
      if (obj.getType().equals("holiday")) {

        if (oldVr.getDaysNumber() != obj.getDaysNumber()) {
          float holidays = userRHDataDTO.getHolidaysBalance();
          float oldNbDays = oldVr.getDaysNumber();
          float newNbDays = obj.getDaysNumber();
          userRHDataDTO.setHolidaysBalance(holidays + (oldNbDays-newNbDays));
          userDataService.save(userRHDataDTO);
          Utils.addBalanceHistoryEntry(obj, userRHDataDTO,holidays, userRHDataDTO.getSickdaysBalance(),"holidayUpdated",currentUser);
        }
      }
      if (obj.getType().equals("sick")) {
        if (oldVr.getDaysNumber() != obj.getDaysNumber()) {
        float sickDays = userRHDataDTO.getSickdaysBalance();
        float oldNbDays = oldVr.getDaysNumber();
        float newNbDays = obj.getDaysNumber();
        userRHDataDTO.setSickdaysBalance(sickDays + (oldNbDays-newNbDays));
        userDataService.save(userRHDataDTO);
        Utils.addBalanceHistoryEntry(obj, userRHDataDTO,userRHDataDTO.getHolidaysBalance(), sickDays,"sickDaysUpdated",currentUser);
      }
      }
    }

    vacationRequestService.save(obj,false);
    CommentDTO comment=new CommentDTO();
    comment.setRequestId(obj.getId());
    comment.setCommentText("requestDaysUpdated");
    comment.setPosterId(currentUser);
    comment.setCommentType(Utils.HISTORY);
    commentService.save(comment);
    try {
      listenerService.broadcast("exo.hrmanagement.requestUpdated", "", obj);
    } catch (Exception e) {
      LOG.error("Cannot broadcast request creation event");
    }

  }


  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void saveVacationRequest(@Jackson VacationRequestDTO obj) {

    vacationRequestService.save(obj,true);

    try {
      listenerService.broadcast("exo.hrmanagement.requestCreated", "", obj);
    } catch (Exception e) {
      LOG.error("Cannot broadcast request creation event");
    }

  }

 }
