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
import org.exoplatform.rhmanagement.dto.EmployeesDTO;
import org.exoplatform.rhmanagement.dto.VacationRequestWithManagersDTO;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class RequestCreatedPlugin extends BaseNotificationPlugin {

  public final static ArgumentLiteral<VacationRequestWithManagersDTO> REQUEST = new ArgumentLiteral<VacationRequestWithManagersDTO>(VacationRequestWithManagersDTO.class, "request");

  private static final Log LOG = ExoLogger.getLogger(RequestCreatedPlugin.class);

  public final static String ID = "RequestCreatedPlugin";



  public RequestCreatedPlugin(InitParams initParams) {

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

    VacationRequestWithManagersDTO obj = ctx.value(REQUEST);

    Set<String> receivers = new HashSet<String>();


    try {

      for (EmployeesDTO manager : obj.getManagers()){
        receivers.add(manager.computeId());
      }

    } catch (Exception ex) {

      LOG.error(ex.getMessage(), ex);

    }

    return NotificationInfo.instance()

            .setFrom(obj.getVacationRequestDTO().getUserId())

            .to(new ArrayList<String>(receivers))

            .setTitle(obj.getVacationRequestDTO().getUserFullName() + " Created new <a href='/portal/intranet/rh-management?rid="+obj.getVacationRequestDTO().getId()+"'>Vacation request</a>.<br/>")

            .key(getId());

  }
}