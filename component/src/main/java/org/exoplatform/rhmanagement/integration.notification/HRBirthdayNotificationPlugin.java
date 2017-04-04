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
import org.exoplatform.social.core.manager.IdentityManager;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;


public class HRBirthdayNotificationPlugin extends BaseNotificationPlugin {

  public final static ArgumentLiteral<UserRHDataDTO> EMPLOYEE = new ArgumentLiteral<UserRHDataDTO>(UserRHDataDTO.class, "employee");
  public final static ArgumentLiteral<String> NOTIF_TYPE = new ArgumentLiteral<String>(String.class, "notifType");

  private static final Log LOG = ExoLogger.getLogger(HRBirthdayNotificationPlugin.class);

  public final static String ID = "HRBirthdayNotificationPlugin";

  IdentityManager identityManager;

  public HRBirthdayNotificationPlugin(InitParams initParams, IdentityManager identityManager) {

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

    UserRHDataDTO obj = ctx.value(EMPLOYEE);
    String notifType = ctx.value(NOTIF_TYPE);

    Set<String> receivers = new HashSet<String>();


    try {
      for (User rh : Utils.getRhManagers()){
        receivers.add(rh.getUserName());
      }

    } catch (Exception ex) {

      LOG.error(ex.getMessage(), ex);

    }


    String userId=obj.getUserId();
    StringBuilder activityId = new StringBuilder(userId);
    activityId.append("-").append(obj.getUserId());
    return NotificationInfo.instance()

            .setFrom(userId)
            .to(new LinkedList<String>(receivers))
            .with(NotificationUtils.CREATOR, userId)
            .with(NotificationUtils.BIRTHDAY_DATE, obj.getBirthDay().toString())
            .with(NotificationUtils.ACTIVITY_ID, activityId.toString())
            .key(getKey()).end();

  }
}