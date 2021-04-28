package org.exoplatform.rhmanagement.services.listener;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.exoplatform.agenda.exception.AgendaException;
import org.exoplatform.agenda.model.Calendar;
import org.exoplatform.agenda.service.AgendaCalendarService;
import org.exoplatform.agenda.service.AgendaEventService;
import org.exoplatform.agenda.util.RestUtils;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainer;
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
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * Created by Medamine on 25/01/2017.
 */
public class UpdateRequestListener extends Listener<Set<String>, VacationRequestDTO> {
  private static final Log      LOG = ExoLogger.getLogger(UpdateRequestListener.class);

  private ExoContainer          container;

  private IdentityManager       identityManager;

  private AgendaEventService    agendaEventService;

  private AgendaCalendarService agendaCalendarService;

  private SpaceService          spaceService;

  public UpdateRequestListener(ExoContainer container, IdentityManager identityManager, SpaceService spaceService) {
    this.container = container;
    this.identityManager = identityManager;
    this.spaceService = spaceService;
  }

  @Override
  public void onEvent(Event event) throws Exception, AgendaException {
    VacationRequestDTO vr = (VacationRequestDTO) event.getData();
    String currentUser = (String) event.getSource();
    Set<String> receivers = new HashSet<String>();
    receivers.add(vr.getUserId());
    ValidatorService validatorService = CommonsUtils.getService(ValidatorService.class);
    for (ValidatorDTO validator : validatorService.getValidatorsByRequestId(vr.getId(), 0, 0)) {
      receivers.add(validator.getValidatorUserId());
    }
    if (Utils.VALIDATED.equals(vr.getStatus()) || Utils.CANCELED.equals(vr.getStatus())
        || Utils.APPROVED.equals(vr.getStatus())) {
      try {
        for (User rh : Utils.getRhManagers()) {
          if (!rh.getUserName().equals(currentUser))
            receivers.add(rh.getUserName());
        }
      } catch (Exception e) {
        LOG.error("Error when add receivers user", e.getMessage(), e);

      }
    }
    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .append(RequestStatusChangedPlugin.REQUEST, vr)
                                                     .append(RequestStatusChangedPlugin.CURRENT_USER, currentUser)
                                                     .append(RequestStatusChangedPlugin.RECEIVERS, receivers);
    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestStatusChangedPlugin.ID))).execute(ctx);
    long userIdentityId = RestUtils.getCurrentUserIdentityId(identityManager);
    if (Utils.VALIDATED.equals(vr.getStatus())) {
      org.exoplatform.agenda.model.Event calendarEvent = new org.exoplatform.agenda.model.Event();
      Space space = spaceService.getSpaceByPrettyName(Utils.EMPLOYEES_SPACE_DEFAULT);
      if (space != null) {
        List<Calendar> calendar = getCalendarService().getCalendars(0, 0, vr.getUserId());
        for (Calendar cal : calendar) {
          if (space.getDisplayName().equals(cal.getTitle())) {
            calendarEvent.setCalendarId(cal.getId());
          }
        }
      }
      calendarEvent.setSummary(vr.getUserFullName() + " " + vr.getType());
      ZonedDateTime start = ZonedDateTime.ofInstant(vr.getFromDate().toInstant(), ZoneId.systemDefault());
      ZonedDateTime end = ZonedDateTime.ofInstant(vr.getToDate().toInstant(), ZoneId.systemDefault());
      calendarEvent.setStart(start);
      calendarEvent.setEnd(end);
      org.exoplatform.agenda.model.Event rhEvent = getEventService().createEvent(calendarEvent,
                                                                                 Collections.emptyList(),
                                                                                 Collections.emptyList(),
                                                                                 Collections.emptyList(),
                                                                                 Collections.emptyList(),
                                                                                 null,
                                                                                 true,
                                                                                 userIdentityId);
    }
    if (Utils.CANCELED.equals(vr.getStatus())) {
      getEventService().deleteEventById(vr.getId(), userIdentityId);
    }
  }

  private AgendaEventService getEventService() {
    if (agendaEventService == null) {
      agendaEventService = this.container.getComponentInstanceOfType(AgendaEventService.class);
    }
    return agendaEventService;
  }

  private AgendaCalendarService getCalendarService() {
    if (agendaCalendarService == null) {
      agendaCalendarService = this.container.getComponentInstanceOfType(AgendaCalendarService.class);
    }
    return agendaCalendarService;
  }
}
