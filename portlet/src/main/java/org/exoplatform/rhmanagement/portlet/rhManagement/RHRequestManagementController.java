package org.exoplatform.rhmanagement.portlet.rhManagement;

import java.util.*;

import javax.inject.Inject;

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
import org.exoplatform.rhmanagement.dto.*;
import org.exoplatform.rhmanagement.integration.notification.RequestCreatedPlugin;
import org.exoplatform.rhmanagement.integration.notification.RequestRepliedPlugin;
import org.exoplatform.rhmanagement.integration.notification.RequestStatusChangedPlugin;
import org.exoplatform.rhmanagement.services.CommentService;
import org.exoplatform.rhmanagement.services.VacationRequestService;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.rhmanagement.services.ValidatorService;
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
          employeesDTO.setValue(id .getProfile().getFullName());
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
      bundleString = data.toString();
      return Response.ok(bundleString);
    } catch (Throwable e) {
      log.error("error while getting categories", e);
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
