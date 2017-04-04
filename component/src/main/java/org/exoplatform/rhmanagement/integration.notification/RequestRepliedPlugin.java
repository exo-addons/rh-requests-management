/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.rhmanagement.integration.notification;


import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.rhmanagement.dto.VacationRequestDTO;
import org.exoplatform.rhmanagement.dto.ValidatorDTO;
import org.exoplatform.rhmanagement.services.VacationRequestService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.manager.IdentityManager;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;


public class RequestRepliedPlugin extends BaseNotificationPlugin {

  public final static ArgumentLiteral<ValidatorDTO> VALIDATOR = new ArgumentLiteral<ValidatorDTO>(ValidatorDTO.class, "validator");

  private static final Log LOG = ExoLogger.getLogger(RequestRepliedPlugin.class);

  public final static String ID = "RequestRepliedPlugin";

  IdentityManager identityManager;

  public RequestRepliedPlugin(InitParams initParams,IdentityManager identityManager) {

    super(initParams);
    this.identityManager = identityManager;

  }



  @Override

  public String getId() {

    return ID;

  }



  @Override

  public boolean isValid(NotificationContext ctx) {

    return true;

  }



  @Override

  protected NotificationInfo makeNotification(NotificationContext ctx) {

    ValidatorDTO obj = ctx.value(VALIDATOR);

    Set<String> receivers = new HashSet<String>();


    try {
        receivers.add(obj.getUserId());

    } catch (Exception ex) {

      LOG.error(ex.getMessage(), ex);

    }


    String userId=obj.getValidatorUserId();
    StringBuilder activityId = new StringBuilder(userId);
    activityId.append("-").append(obj.getRequestId());
    VacationRequestDTO vacationRequest = CommonsUtils.getService(VacationRequestService.class).getVacationRequest(obj.getRequestId());
    String vacationUrl = CommonsUtils.getCurrentDomain()+"/portal/intranet/rh-management?rid="+obj.getRequestId();



    return NotificationInfo.instance()

            .setFrom(userId)
            .to(new LinkedList<String>(receivers))
            .with(NotificationUtils.CREATOR, userId)
            .with(NotificationUtils.FROM_DATE, vacationRequest.getFromDate().toString())
            .with(NotificationUtils.TO_DATE, vacationRequest.getToDate().toString())
            .with(NotificationUtils.VACATION_URL, vacationUrl)
            .with(NotificationUtils.ACTIVITY_ID, activityId.toString())
            .key(getKey()).end();

  }
}