package org.exoplatform.rhmanagement.services.listener;

import org.exoplatform.calendar.service.ExtendedCalendarService;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.rhmanagement.dto.VacationRequestDTO;
import org.exoplatform.rhmanagement.dto.ValidatorDTO;
import org.exoplatform.rhmanagement.integration.notification.RequestStatusChangedPlugin;
import org.exoplatform.rhmanagement.services.Utils;
import org.exoplatform.rhmanagement.services.ValidatorService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Medamine on 25/01/2017.
 */
public class UpdateRequestListener extends Listener<Set<String>,VacationRequestDTO> {
    private static final Log LOG = ExoLogger.getLogger(UpdateRequestListener.class);


    @Override
    public void onEvent(Event event) throws Exception {
        VacationRequestDTO vr=(VacationRequestDTO)event.getData();
        String currentUser = (String)event.getSource();
        Set<String> receivers = new HashSet<String>();
        receivers.add(vr.getUserId());
        ValidatorService validatorService=CommonsUtils.getService(ValidatorService.class);
        for (ValidatorDTO validator :validatorService.getValidatorsByRequestId(vr.getId(),0,0)){
            receivers.add(validator.getValidatorUserId());
        }
        if( Utils.VALIDATED.equals(vr.getStatus())||Utils.CANCELED.equals(vr.getStatus())||Utils.APPROVED.equals(vr.getStatus()) ){
            try {
                for (User rh : Utils.getRhManagers()){
                    if(!rh.getUserName().equals(currentUser)) receivers.add(rh.getUserName());
                }
            } catch (Exception e) {
                LOG.error("Error when add receivers user",e.getMessage(), e);

            }
        }
        NotificationContext ctx = NotificationContextImpl.cloneInstance().append(RequestStatusChangedPlugin.REQUEST, vr).append(RequestStatusChangedPlugin.CURRENT_USER, currentUser).append(RequestStatusChangedPlugin.RECEIVERS, receivers);
        ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestStatusChangedPlugin.ID))).execute(ctx);

        if (Utils.VALIDATED.equals(vr.getStatus())){
            ExtendedCalendarService  xCalendarService=CommonsUtils.getService(ExtendedCalendarService.class);
            String employeesSpace = System.getProperty(Utils.EMPLOYEES_SPACE, Utils.EMPLOYEES_SPACE_DEFAULT);
            String calId=(employeesSpace + "_space_calendar");
            org.exoplatform.calendar.model.Event calendarEvent = new org.exoplatform.calendar.model.Event();
            calendarEvent.setId(calId+"_"+vr.getUserId()+"_"+vr.getId());
            calendarEvent.setEventCategoryId(vr.getType());
            calendarEvent.setEventCategoryName(vr.getType());
            calendarEvent.setSummary(vr.getUserFullName()+" "+ vr.getType());
            calendarEvent.setFromDateTime(vr.getFromDate());
            calendarEvent.setToDateTime(vr.getToDate());
            calendarEvent.setEventType(org.exoplatform.calendar.model.Event.TYPE_EVENT) ;
            calendarEvent.setCalendarId(calId);
            xCalendarService.getEventHandler().saveEvent(calendarEvent) ;
        }


        if (Utils.CANCELED.equals(vr.getStatus())){
            ExtendedCalendarService  xCalendarService=CommonsUtils.getService(ExtendedCalendarService.class);
            String employeesSpace = System.getProperty(Utils.EMPLOYEES_SPACE, Utils.EMPLOYEES_SPACE_DEFAULT);
            String calId=(employeesSpace + "_space_calendar");
            xCalendarService.getEventHandler().removeEvent(calId+"_"+vr.getUserId()+"_"+vr.getId());
        }

    }
}
