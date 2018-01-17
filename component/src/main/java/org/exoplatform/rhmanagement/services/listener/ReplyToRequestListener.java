package org.exoplatform.rhmanagement.services.listener;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.rhmanagement.dto.VacationRequestDTO;
import org.exoplatform.rhmanagement.dto.ValidatorDTO;
import org.exoplatform.rhmanagement.integration.notification.RequestRepliedPlugin;
import org.exoplatform.rhmanagement.services.Utils;
import org.exoplatform.rhmanagement.services.VacationRequestService;
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
public class ReplyToRequestListener extends Listener<String,VacationRequestDTO> {
    private static final Log LOG = ExoLogger.getLogger(ReplyToRequestListener.class);

    @Override
    public void onEvent(Event event) throws Exception {
        ValidatorDTO validator=(ValidatorDTO)event.getData();

        Set<String> receivers = new HashSet<String>();
        VacationRequestDTO vr = CommonsUtils.getService(VacationRequestService.class).getVacationRequest(validator.getRequestId());

        receivers.add(vr.getUserId());

        for (User rhManager : Utils.getRhManagers()){
            if(!receivers.contains(rhManager.getUserName()))
                receivers.add(rhManager.getUserName());
        }

        ValidatorService validatorService=CommonsUtils.getService(ValidatorService.class);
        for (ValidatorDTO val :validatorService.getValidatorsByRequestId(vr.getId(),0,0)){
            receivers.add(val.getValidatorUserId());
        }


        NotificationContext ctx = NotificationContextImpl.cloneInstance().append(RequestRepliedPlugin.VALIDATOR, validator).append(RequestRepliedPlugin.RECEIVERS, receivers).append(RequestRepliedPlugin.VACATION_REQUEST, vr);
        ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestRepliedPlugin.ID))).execute(ctx);

    }
}
