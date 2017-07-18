package org.exoplatform.rhmanagement.portlet.rhAdministration;

import juzu.*;
import juzu.impl.common.JSON;
import juzu.plugin.jackson.Jackson;
import juzu.template.Template;
import org.apache.commons.fileupload.FileItem;
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

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import java.io.IOException;
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
  public List<EmployeesDTO> getAllUsersRhData() {
    try {
      return userDataService.getAllUsersRhData(0,100);

    } catch (Throwable e) {
      LOG.error(e);
      return null;
    }
  }


  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public UserRHDataDTO saveUserRHData(@Jackson EmployeesDTO user) throws Exception {
    try {

      UserRHDataDTO userRHDataDTO=userDataService.getUserRHDataByUserId(user.getUserId());
      float oldHolidayBalance=userRHDataDTO.getHolidaysBalance();
      float newHolidayBalance=user.getHrData().getHolidaysBalance();
       float oldSickBalance=userRHDataDTO.getSickdaysBalance();
      float newSickBalance=user.getHrData().getSickdaysBalance();


      if(oldHolidayBalance!=newHolidayBalance){
        try {
          BalanceHistoryDTO balanceHistoryDTO=new BalanceHistoryDTO();
          balanceHistoryDTO.setUserId(user.getUserId());
          balanceHistoryDTO.setIntialHolidaysBalance(oldHolidayBalance);
          balanceHistoryDTO.setIntialSickBalance(oldSickBalance);
          balanceHistoryDTO.setHolidaysBalance(newHolidayBalance);
          balanceHistoryDTO.setSickBalance(oldSickBalance);
          balanceHistoryDTO.setVacationType("holiday");
          balanceHistoryDTO.setVacationId(-1);
          balanceHistoryDTO.setDaysNumber(newHolidayBalance-oldHolidayBalance);
          balanceHistoryDTO.setUpdateType("holidaysManualUpdate");
          balanceHistoryDTO.setUpdaterId(currentUser);

          balanceHistoryService.save(balanceHistoryDTO);
        } catch (Exception e) {
          LOG.error("Error when adding history entry", e);
        }
      }
       if(oldSickBalance!=newSickBalance){

         try {
           BalanceHistoryDTO balanceHistoryDTO=new BalanceHistoryDTO();
           balanceHistoryDTO.setUserId(user.getUserId());
           balanceHistoryDTO.setIntialHolidaysBalance(oldHolidayBalance);
           balanceHistoryDTO.setIntialSickBalance(oldSickBalance);
           balanceHistoryDTO.setHolidaysBalance(oldHolidayBalance);
           balanceHistoryDTO.setSickBalance(newSickBalance);
           balanceHistoryDTO.setVacationType("sick");
           balanceHistoryDTO.setVacationId(-1);
           balanceHistoryDTO.setDaysNumber(newSickBalance-oldSickBalance);
           balanceHistoryDTO.setUpdateType("sicksManualUpdate");
           balanceHistoryDTO.setUpdaterId(currentUser);

           balanceHistoryService.save(balanceHistoryDTO);
         } catch (Exception e) {
           LOG.error("Error when adding history entry", e);
         }
       }

        return userDataService.save(user.getHrData());

    } catch (Exception e) {
      LOG.error("Error when updating userData", e);
      throw e;
    }
  }


  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public Response deleteUserRHData(@Jackson EmployeesDTO user) throws Exception {
    try {
       userDataService.remove(user.getHrData());
       Utils.deleteFile("Application Data/hrmanagement/employees/emp_"+user.getUserId());
      return Response.ok();
    } catch (Exception e) {
      LOG.error("Error when updating userData", e);
      return Response.error("");
    }
  }

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
  public VacationRequestsPageDTO getVacationRequests(String vrFilter, int offset, int limit) {
    try {
      VacationRequestsPageDTO vacationRequestsPageDTO =new VacationRequestsPageDTO();
      if (vrFilter != null&&vrFilter.equals(Utils.ACTIVE)) {
        vacationRequestsPageDTO.setVacationRequests(vacationRequestService.getActivVacationRequests(offset,limit));
        vacationRequestsPageDTO.setSize(vacationRequestService.getActiveVacationRequestsCount());
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

      return vacationRequestService.getActivVacationRequests(0,100);

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

        try {
          BalanceHistoryDTO balanceHistoryDTO=new BalanceHistoryDTO();
          balanceHistoryDTO.setUserId(obj.getUserId());
          balanceHistoryDTO.setIntialHolidaysBalance(holidays);
          balanceHistoryDTO.setIntialSickBalance(userRHDataDTO.getSickdaysBalance());
          balanceHistoryDTO.setHolidaysBalance(userRHDataDTO.getHolidaysBalance());
          balanceHistoryDTO.setSickBalance(userRHDataDTO.getSickdaysBalance());
          balanceHistoryDTO.setVacationType(obj.getType());
          balanceHistoryDTO.setVacationId(obj.getId());
          balanceHistoryDTO.setDaysNumber(obj.getDaysNumber());
          balanceHistoryDTO.setUpdateType("holidayValidated");
          balanceHistoryDTO.setUpdaterId(currentUser);

          balanceHistoryService.save(balanceHistoryDTO);
        } catch (Exception e) {
          LOG.error("Error when adding history entry", e);
        }

      }if(obj.getType().equals("sick")){
        float sickdays=userRHDataDTO.getSickdaysBalance();
        float nbDays=obj.getDaysNumber();
        userRHDataDTO.setSickdaysBalance(sickdays-nbDays);
        userDataService.save(userRHDataDTO);

        try {
          BalanceHistoryDTO balanceHistoryDTO=new BalanceHistoryDTO();
          balanceHistoryDTO.setUserId(obj.getUserId());
          balanceHistoryDTO.setIntialHolidaysBalance(userRHDataDTO.getHolidaysBalance());
          balanceHistoryDTO.setIntialSickBalance(sickdays);
          balanceHistoryDTO.setHolidaysBalance(userRHDataDTO.getHolidaysBalance());
          balanceHistoryDTO.setSickBalance(userRHDataDTO.getSickdaysBalance());
          balanceHistoryDTO.setVacationType(obj.getType());
          balanceHistoryDTO.setVacationId(obj.getId());
          balanceHistoryDTO.setDaysNumber(obj.getDaysNumber());
          balanceHistoryDTO.setUpdateType("sickValidated");
          balanceHistoryDTO.setUpdaterId(currentUser);

          balanceHistoryService.save(balanceHistoryDTO);
        } catch (Exception e) {
          LOG.error("Error when adding history entry", e);
        }

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

          try {
            BalanceHistoryDTO balanceHistoryDTO=new BalanceHistoryDTO();
            balanceHistoryDTO.setUserId(obj.getUserId());
            balanceHistoryDTO.setIntialHolidaysBalance(holidays);
            balanceHistoryDTO.setIntialSickBalance(userRHDataDTO.getSickdaysBalance());
            balanceHistoryDTO.setHolidaysBalance(userRHDataDTO.getHolidaysBalance());
            balanceHistoryDTO.setSickBalance(userRHDataDTO.getSickdaysBalance());
            balanceHistoryDTO.setVacationType(obj.getType());
            balanceHistoryDTO.setVacationId(obj.getId());
            balanceHistoryDTO.setDaysNumber(obj.getDaysNumber());
            balanceHistoryDTO.setUpdateType("holidayCanceled");
            balanceHistoryDTO.setUpdaterId(currentUser);

            balanceHistoryService.save(balanceHistoryDTO);
          } catch (Exception e) {
            LOG.error("Error when adding history entry", e);
          }
        }
        if (obj.getType().equals("sick")) {
          float sickdays=userRHDataDTO.getSickdaysBalance();
          float nbDays=obj.getDaysNumber();
          userRHDataDTO.setSickdaysBalance(sickdays+nbDays);
          userDataService.save(userRHDataDTO);

          try {
            BalanceHistoryDTO balanceHistoryDTO=new BalanceHistoryDTO();
            balanceHistoryDTO.setUserId(obj.getUserId());
            balanceHistoryDTO.setIntialHolidaysBalance(userRHDataDTO.getHolidaysBalance());
            balanceHistoryDTO.setIntialSickBalance(sickdays);
            balanceHistoryDTO.setHolidaysBalance(userRHDataDTO.getHolidaysBalance());
            balanceHistoryDTO.setSickBalance(userRHDataDTO.getSickdaysBalance());
            balanceHistoryDTO.setVacationType(obj.getType());
            balanceHistoryDTO.setVacationId(obj.getId());
            balanceHistoryDTO.setDaysNumber(obj.getDaysNumber());
            balanceHistoryDTO.setUpdateType("sickCanceled");
            balanceHistoryDTO.setUpdaterId(currentUser);

            balanceHistoryService.save(balanceHistoryDTO);
          } catch (Exception e) {
            LOG.error("Error when adding history entry", e);
          }

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

  public Response  getEmployeeAttachements(@Jackson EmployeesDTO obj) {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    List<JSON> atts = new ArrayList<JSON>();
    try {
      Session session = sessionProvider.getSession("collaboration",
              repositoryService.getCurrentRepository());
      Node rootNode = session.getRootNode();
      String userId=obj.getUserId();
      if (rootNode.hasNode("Application Data/hrmanagement/employees/emp_"+userId)) {
        Node requestsFolder= rootNode.getNode("Application Data/hrmanagement/employees/emp_"+userId);
        NodeIterator iter = requestsFolder.getNodes();
        while (iter.hasNext()) {
          Node node = (Node) iter.next();
          JSON attachment=new JSON();
          attachment.set("name",node.getName());
          attachment.set("url","/rest/jcr/repository/collaboration/Application Data/hrmanagement/employees/emp_"+userId+"/"+node.getName());
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


}
