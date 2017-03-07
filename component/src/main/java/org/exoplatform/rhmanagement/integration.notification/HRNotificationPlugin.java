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
import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.rhmanagement.dto.UserRHDataDTO;
import org.exoplatform.rhmanagement.dto.VacationRequestWithManagersDTO;
import org.exoplatform.rhmanagement.services.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.webui.utils.TimeConvertUtils;

import java.util.*;


public class HRNotificationPlugin extends BaseNotificationPlugin {

  public final static ArgumentLiteral<UserRHDataDTO> EMPLOYEE = new ArgumentLiteral<UserRHDataDTO>(UserRHDataDTO.class, "employee");
  public final static ArgumentLiteral<String> NOTIF_TYPE = new ArgumentLiteral<String>(String.class, "notifType");

  private static final Log LOG = ExoLogger.getLogger(HRNotificationPlugin.class);

  public final static String ID = "RequestCreatedPlugin";

  IdentityManager identityManager;

  public HRNotificationPlugin(InitParams initParams, IdentityManager identityManager) {

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
        Profile userProfile=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false).getProfile();
                Calendar lastModified = Calendar.getInstance();
        String lastModifiedDate= TimeConvertUtils.convertXTimeAgoByTimeServer(lastModified.getTime(),"EE, dd yyyy", new Locale(NotificationPluginUtils.getLanguage(userId)), TimeConvertUtils.YEAR);
        String avatarUrl=userProfile.getAvatarUrl();
        if(avatarUrl==null)avatarUrl="/eXoSkin/skin/images/system/UserAvtDefault.png";
        String content="<li class='unread clearfix'>\n" +
                       "  <div class='media'>\n" +
                       "    <div class='avatarXSmall pull-left'>\n" +
                       "      <img src='"+avatarUrl+"' alt='"+userProfile.getFullName()+"'>\n" +
                       "    </div>\n" +
                       "    <div class='media-body'>\n" +
                       "      \n" +
                       "      <div class='contentSmall' data-link='/portal/intranet/profile/"+userId+"'>\n" +
                       "        <div class='status'>"+notifType+"</div>\n" +
                       "        <div class='lastUpdatedTime'>"+lastModifiedDate+"</div>\n" +
                       "      </div>\n" +
                       "    </div>\n" +
                       "  </div>\n" +
                       "  <span class='remove-item' data-rest=''><i class='uiIconClose uiIconLightGray'></i></span>\n" +
                       "</li>";
    return NotificationInfo.instance()

            .setFrom(userId)
            .to(new ArrayList<String>(receivers))

             .setTitle(content)

            .key(getId());

  }
}