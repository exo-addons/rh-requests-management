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
import org.exoplatform.rhmanagement.services.Utils;
import org.exoplatform.rhmanagement.services.VacationRequestService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.manager.IdentityManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by The eXo Platform SAS
 */
public class RequestRepliedPlugin extends BaseNotificationPlugin {

  public final static ArgumentLiteral<ValidatorDTO> VALIDATOR = new ArgumentLiteral<ValidatorDTO>(ValidatorDTO.class, "validator");
  public static final ArgumentLiteral<Set> RECEIVERS = new ArgumentLiteral<Set>(Set.class, "receivers");
  public final static ArgumentLiteral<VacationRequestDTO> VACATION_REQUEST = new ArgumentLiteral<VacationRequestDTO>(VacationRequestDTO.class, "vacationRequest");
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

    DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
    ValidatorDTO obj = ctx.value(VALIDATOR);
    VacationRequestDTO vacationRequest  = ctx.value(VACATION_REQUEST);

    Set<String> receivers = ctx.value(RECEIVERS);


    String userId=obj.getValidatorUserId();
    receivers.remove(userId);
    StringBuilder activityId = new StringBuilder(userId);
    activityId.append("-").append(obj.getRequestId());
    String vacationUrl = CommonsUtils.getCurrentDomain()+"/portal/dw/rh-management?rid="+obj.getRequestId();





    return NotificationInfo.instance()

            .setFrom(userId)
            .to(new LinkedList<String>(receivers))
            .with(NotificationUtils.CREATOR, userId)
            .with(NotificationUtils.FROM_DATE, String.valueOf(vacationRequest.getFromDate().getTime()))
            .with(NotificationUtils.TO_DATE, String.valueOf(vacationRequest.getToDate().getTime()))
            .with(NotificationUtils.USER_NAME, vacationRequest.getUserId().toString())
            .with(NotificationUtils.VACATION_URL, vacationUrl)
            .with(NotificationUtils.ACTIVITY_ID, activityId.toString())
            .key(getKey()).end();

  }
}