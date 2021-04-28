package org.exoplatform.rhmanagement.services.listener;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.rhmanagement.dto.CommentDTO;
import org.exoplatform.rhmanagement.dto.VacationRequestDTO;
import org.exoplatform.rhmanagement.dto.ValidatorDTO;
import org.exoplatform.rhmanagement.integration.notification.RequestCommentedPlugin;
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
public class CommentRequestListener extends Listener<Set<String>,VacationRequestDTO> {
    private static final Log LOG = ExoLogger.getLogger(CommentRequestListener.class);


    @Override
    public void onEvent(Event event) throws Exception {
        CommentDTO comment=(CommentDTO)event.getData();

        Set<String> receivers = new HashSet<String>();
        VacationRequestDTO vr = CommonsUtils.getService(VacationRequestService.class).getVacationRequest(comment.getRequestId());

            receivers.add(vr.getUserId());

            for (User rhManager : Utils.getRhManagers()){
                if(!receivers.contains(rhManager.getUserName()))
                    receivers.add(rhManager.getUserName());
            }

        ValidatorService validatorService=CommonsUtils.getService(ValidatorService.class);
        for (ValidatorDTO validator :validatorService.getValidatorsByRequestId(vr.getId(),0,0)){
            receivers.add(validator.getValidatorUserId());
        }


        NotificationContext ctx = NotificationContextImpl.cloneInstance().append(RequestCommentedPlugin.COMMENT, comment).append(RequestCommentedPlugin.RECEIVERS, receivers).append(RequestCommentedPlugin.VACATION_REQUEST, vr);
        ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestCommentedPlugin.ID))).execute(ctx);
    }
}
