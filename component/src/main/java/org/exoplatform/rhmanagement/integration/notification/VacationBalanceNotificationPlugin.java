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
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.rhmanagement.dto.UserRHDataDTO;
import org.exoplatform.rhmanagement.services.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by The eXo Platform SAS
 */
public class VacationBalanceNotificationPlugin extends BaseNotificationPlugin {

  public final static ArgumentLiteral<UserRHDataDTO> EMPLOYEE = new ArgumentLiteral<UserRHDataDTO>(UserRHDataDTO.class, "employee");
  public final static ArgumentLiteral<Float> DAYS_TO_CONSUME = new ArgumentLiteral<Float>(Float.class, "daysToConsume");

  private static final Log LOG = ExoLogger.getLogger(VacationBalanceNotificationPlugin.class);

  public final static String ID = "VacationBalanceNotificationPlugin";


  public VacationBalanceNotificationPlugin(InitParams initParams) {

    super(initParams);

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

    UserRHDataDTO obj = ctx.value(EMPLOYEE);
    Float nDays = ctx.value(DAYS_TO_CONSUME);
    String userId=obj.getUserId();
    Set<String> receivers = new HashSet<String>();

    receivers.add(userId);

      for (User rh : Utils.getRhManagers()){
        receivers.add(rh.getUserName());
      }

    StringBuilder activityId = new StringBuilder(userId);
    activityId.append("-").append(obj.getUserId());
    return NotificationInfo.instance()

            .setFrom(userId)
            .to(new LinkedList<String>(receivers))
            .with(NotificationUtils.CREATOR, userId)
            .with(NotificationUtils.DAYS_TO_CONSUME, nDays.toString())
            .with(NotificationUtils.ACTIVITY_ID, activityId.toString())
            .key(getKey()).end();

  }
}