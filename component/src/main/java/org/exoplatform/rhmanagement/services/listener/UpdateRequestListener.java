package org.exoplatform.rhmanagement.services.listener;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.rhmanagement.dto.VacationRequestDTO;
import org.exoplatform.rhmanagement.dto.VacationRequestWithManagersDTO;
import org.exoplatform.rhmanagement.integration.notification.RequestCreatedPlugin;
import org.exoplatform.rhmanagement.integration.notification.RequestStatusChangedPlugin;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.Set;

/**
 * Created by Medamine on 25/01/2017.
 */
public class UpdateRequestListener extends Listener<Set<String>,VacationRequestDTO> {
    private static final Log LOG = ExoLogger.getLogger(UpdateRequestListener.class);

    @Override
    public void onEvent(Event event) throws Exception {
        VacationRequestDTO vr=(VacationRequestDTO)event.getData();
        Set<String> managers = (Set<String>)event.getSource();
        NotificationContext ctx = NotificationContextImpl.cloneInstance().append(RequestStatusChangedPlugin.REQUEST, vr).append(RequestStatusChangedPlugin.MANAGERS, managers);
        ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestStatusChangedPlugin.ID))).execute(ctx);

    }
}
