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
import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.rhmanagement.services.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.webui.utils.TimeConvertUtils;
import org.gatein.common.text.EntityEncoder;

import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@TemplateConfigs(
   templates = {
       @TemplateConfig( pluginId=RequestRepliedPlugin.ID, template="war:/notification/rh-requests/templates/web/RequestReplyPlugin.gtmpl"),
       @TemplateConfig( pluginId=RequestStatusChangedPlugin.ID, template="war:/notification/rh-requests/templates/web/UpdateRequestPlugin.gtmpl"),
       @TemplateConfig( pluginId=RequestCreatedPlugin.ID, template="war:/notification/rh-requests/templates/web/CreateRequestPlugin.gtmpl"),
           @TemplateConfig( pluginId=HRBirthdayNotificationPlugin.ID, template="war:/notification/rh-requests/templates/web/HRBirthdayNotificationPlugin.gtmpl"),
           @TemplateConfig( pluginId=HRContractAnniversaryNotificationPlugin.ID, template="war:/notification/rh-requests/templates/web/HRContractAnniversaryNotificationPlugin.gtmpl"),
           @TemplateConfig( pluginId=RequestCommentedPlugin.ID, template="war:/notification/rh-requests/templates/web/RequestCommentedPlugin.gtmpl"),
           @TemplateConfig( pluginId=VacationBalanceNotificationPlugin.ID, template="war:/notification/rh-requests/templates/web/VacationBalanceNotificationPlugin.gtmpl")
   }
)
/**
 * Created by The eXo Platform SAS
 */
public class WebTemplateProvider extends TemplateProvider {

  protected DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
  protected static Log log = ExoLogger.getLogger(MailTemplateProvider.class);
  
  public WebTemplateProvider(InitParams initParams) {
    super(initParams);

    this.templateBuilders.put(PluginKey.key(RequestRepliedPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(RequestStatusChangedPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(RequestCreatedPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(HRBirthdayNotificationPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(HRContractAnniversaryNotificationPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(RequestCommentedPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(VacationBalanceNotificationPlugin.ID), new TemplateBuilder());
  }

  private class TemplateBuilder extends AbstractTemplateBuilder {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();
      String pluginId = notification.getKey().getId();      

      String language = getLanguage(notification);
      TemplateContext templateContext = TemplateContext.newChannelInstance(getChannelKey(), pluginId, language);
      
      String creator = notification.getValueOwnerParameter(NotificationUtils.CREATOR);
      String vacationUrl = notification.getValueOwnerParameter(NotificationUtils.VACATION_URL);
      String userName = notification.getValueOwnerParameter(NotificationUtils.USER_NAME);
      
      EntityEncoder encoder = HTMLEntityEncoder.getInstance();
      IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, creator, true);
      Profile profile = identity.getProfile();
      templateContext.put("USER", encoder.encode(profile.getFullName().toString()));
      templateContext.put("AVATAR", profile.getAvatarUrl() != null ? profile.getAvatarUrl() : LinkProvider.PROFILE_DEFAULT_AVATAR_URL);
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      //
      if(vacationUrl!=null) {
        templateContext.put("VACATION_URL", vacationUrl);
      }

      if(userName!=null) {
        Identity id=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName, false);
        templateContext.put("USER_NAME", id.getProfile().getFullName());
      }
        String daysToConsume = notification.getValueOwnerParameter(NotificationUtils.DAYS_TO_CONSUME);
        if (daysToConsume != null) {
                templateContext.put("DAYS_TO_CONSUME", daysToConsume);
        }


      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())) ? "read" : "unread");
      templateContext.put("NOTIFICATION_ID", notification.getId());
      Calendar lastModified = Calendar.getInstance();
      lastModified.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgoByTimeServer(lastModified.getTime(),"EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));

      //
      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }

  };


}
