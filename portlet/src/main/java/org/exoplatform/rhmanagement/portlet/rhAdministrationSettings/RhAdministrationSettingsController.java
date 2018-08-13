package org.exoplatform.rhmanagement.portlet.rhAdministrationSettings;

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
public class RhAdministrationSettingsController {

  private static Log  LOG = ExoLogger.getLogger(RhAdministrationSettingsController.class);
  private String     bundleString;
  ResourceBundle     bundle;
  private final String RH_MANAGER = "lastAccess";

  @Inject
  VacationRequestService vacationRequestService;

  @Inject
  ConventionalVacationService conventionalVacationService;

  @Inject
  OfficialVacationService officialVacationService;

  @Inject
  SettingService settingService;


  @Inject
  ListenerService listenerService;

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
      String rhManager="";
      if(settingService.get(Context.GLOBAL, Scope.GLOBAL, RH_MANAGER)!=null) rhManager=settingService.get(Context.GLOBAL, Scope.GLOBAL, RH_MANAGER).getValue().toString();
      data.set("rhManager",rhManager);
      return Response.ok(data.toString());
    } catch (Throwable e) {
      LOG.error("error while getting context", e);
      return Response.status(500);
    }
  }


  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void saveSettings(@Jackson SettingsDTO obj) {

    settingService.set(Context.GLOBAL, Scope.GLOBAL, RH_MANAGER, SettingValue.create(obj.getRhManager()));
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


// Conventional Vacations

  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public ConventionalVacationDTO getConventionalVacationsById (Long id) {
    try {
      return conventionalVacationService.getConventionalVacationsById(id);

    } catch (Throwable e) {
      LOG.error(e);
      return null;
    }
  }


  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public List<ConventionalVacationDTO> getConventionalVacations () {
    try {
      return conventionalVacationService.getConventionalVacations(0,0);
    } catch (Throwable e) {
      LOG.error(e);
      return null;
    }
  }

  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void saveConventionalVacation(@Jackson ConventionalVacationDTO obj) {

    conventionalVacationService.save(obj,true);
  }

  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void updateConventionalVacation(@Jackson ConventionalVacationDTO obj) {

    conventionalVacationService.save(obj,false);
  }

  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public Response deleteConventionalVacation(@Jackson ConventionalVacationDTO obj) throws Exception {
    try {
      conventionalVacationService.remove(obj);
      return Response.ok();
    } catch (Exception e) {
      LOG.error("Error when updating Conventional Vacation", e);
      return Response.error("");
    }
  }


  // Official vacations


  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public OfficialVacationDTO getOfficialVacationsById (Long id) {
    try {
      return officialVacationService.getOfficialVacationsById(id);

    } catch (Throwable e) {
      LOG.error(e);
      return null;
    }
  }


  @Ajax
  @juzu.Resource
  @MimeType.JSON
  @Jackson
  public List<OfficialVacationDTO> getOfficialVacations () {
    try {
      return officialVacationService.getOfficialVacations(0,0);
    } catch (Throwable e) {
      LOG.error(e);
      return null;
    }
  }

  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void saveOfficialVacation(@Jackson OfficialVacationDTO obj) {
    Calendar c = Calendar.getInstance();
    c.setTime(obj.getBeginDate());
    c.add(Calendar.DATE, ((int)obj.getDaysNumber()-1));
    obj.setEndDate(c.getTime());
    officialVacationService.save(obj,true);
    upadteNumberOfDays(obj.getBeginDate());
  }

  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void updateOfficialVacation(@Jackson OfficialVacationDTO obj) {
    Calendar c = Calendar.getInstance();
    c.setTime(obj.getBeginDate());
    c.add(Calendar.DATE, ((int)obj.getDaysNumber()));
    obj.setEndDate(c.getTime());
   officialVacationService.save(obj,false);
    upadteNumberOfDays(obj.getBeginDate());
  }

  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public Response deleteOfficialVacation(@Jackson OfficialVacationDTO obj) throws Exception {
    try {
      officialVacationService.remove(obj);
      upadteNumberOfDays(obj.getBeginDate());
      return Response.ok();
    } catch (Exception e) {
      LOG.error("Error when updating Conventional Vacation", e);
      return Response.error("");
    }
  }


  @Ajax
  @Resource(method = HttpMethod.POST)
  @MimeType.JSON
  @Jackson
  public void saveVacationRequest(@Jackson VacationRequestDTO obj) {



/*    if(obj.getStatus().equals(VALIDATED)) {
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
    }*/

 /*
    CommentDTO comment=new CommentDTO();
    comment.setRequestId(obj.getId());
    comment.setCommentText("requestDaysUpdated");
    comment.setPosterId(currentUser);
    comment.setCommentType(Utils.HISTORY);
    commentService.save(comment);*/
    vacationRequestService.save(obj,true);

    try {
      listenerService.broadcast("exo.hrmanagement.requestCreated", "", obj);
    } catch (Exception e) {
      LOG.error("Cannot broadcast request creation event");
    }

  }


  public void upadteNumberOfDays(Date date){
    List<VacationRequestDTO> vRequests = vacationRequestService.getVacationRequestByDate(date);
    List<Date> oVacation = officialVacationService.getOfficialVacationDays();
    for (VacationRequestDTO vr : vRequests){
      if(!("conventional".equals(vr.getType()))&&!("leave".equals(vr.getType()))){
        float nb= Utils.calculateNumberOfDays(oVacation, vr.getFromDate(),vr.getToDate());
        if(vr.getDaysNumber()!=nb){
          vr.setDaysNumber(nb);
          saveVacationRequest(vr);
          LOG.info("Number of days of the request "+vr.getId()+" is updated");
        }
      }
    }
  }


 }
