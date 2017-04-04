package org.exoplatform.rhmanagement.services.listener;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.rhmanagement.dto.VacationRequestDTO;
import org.exoplatform.rhmanagement.dto.VacationRequestWithManagersDTO;
import org.exoplatform.rhmanagement.integration.notification.RequestCreatedPlugin;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by Medamine on 25/01/2017.
 */
public class NewRequestListener extends Listener<String,VacationRequestDTO> {
    private static final Log LOG = ExoLogger.getLogger(NewRequestListener.class);

    @Override
    public void onEvent(Event event) throws Exception {
        VacationRequestWithManagersDTO vr=(VacationRequestWithManagersDTO)event.getData();

        NotificationContext ctx = NotificationContextImpl.cloneInstance().append(RequestCreatedPlugin.REQUEST, vr);
        ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestCreatedPlugin.ID))).execute(ctx);

    }
}
