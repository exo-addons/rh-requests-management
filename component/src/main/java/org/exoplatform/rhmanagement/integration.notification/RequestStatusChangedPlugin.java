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
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.rhmanagement.dto.EmployeesDTO;
import org.exoplatform.rhmanagement.dto.VacationRequestDTO;
import org.exoplatform.rhmanagement.dto.VacationRequestWithManagersDTO;
import org.exoplatform.rhmanagement.dto.ValidatorDTO;
import org.exoplatform.rhmanagement.services.ValidatorService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class RequestStatusChangedPlugin extends BaseNotificationPlugin {

  public final static ArgumentLiteral<VacationRequestDTO> REQUEST = new ArgumentLiteral<VacationRequestDTO>(VacationRequestDTO.class, "request");

  private static final Log LOG = ExoLogger.getLogger(RequestStatusChangedPlugin.class);

  public final static String ID = "RequestStatusChangedPlugin";

  private ValidatorService        validatorService;


  public RequestStatusChangedPlugin(InitParams initParams, ValidatorService validatorService) {
    super(initParams);
    this.validatorService = validatorService;
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

    VacationRequestDTO obj = ctx.value(REQUEST);

    Set<String> receivers = new HashSet<String>();


    try {

      for (ValidatorDTO validator :validatorService.getValidatorsByRequestId(obj.getId(),0,0)){
        receivers.add(validator.getValidatorUserId());
      }
      receivers.add(obj.getUserId());
    } catch (Exception ex) {

      LOG.error(ex.getMessage(), ex);

    }

    return NotificationInfo.instance()

            .setFrom(obj.getUserId())

            .to(new ArrayList<String>(receivers))

            .setTitle("The <a href='/portal/intranet/rh-management?rid="+obj.getId()+"'>Vacation request</a> of "+obj.getUserFullName()+" has been "+obj.getStatus()+".<br/>")

            .key(getId());

  }
}