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
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.webui.utils.TimeConvertUtils;
import org.gatein.common.text.EntityEncoder;

import java.io.Writer;
import java.util.*;

@TemplateConfigs(
   templates = {
       @TemplateConfig( pluginId=RequestRepliedPlugin.ID, template="war:/notification/templates/web/RequestReplyPlugin.gtmpl"),
       @TemplateConfig( pluginId=RequestStatusChangedPlugin.ID, template="war:/notification/templates/web/UpdateRequestPlugin.gtmpl"),
       @TemplateConfig( pluginId=RequestCreatedPlugin.ID, template="war:/notification/templates/web/CreateRequestPlugin.gtmpl")
   }
)
public class WebTemplateProvider extends TemplateProvider {
  
  public WebTemplateProvider(InitParams initParams) {
    super(initParams);

    this.templateBuilders.put(PluginKey.key(RequestRepliedPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(RequestStatusChangedPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(RequestCreatedPlugin.ID), new TemplateBuilder());
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

      Date fromDate = null;
      String tmpD = notification.getValueOwnerParameter(NotificationUtils.FROM_DATE);
      if (tmpD != null) {
        fromDate = new Date(Long.parseLong(tmpD));
      }

      Date toDate = null;
       tmpD = notification.getValueOwnerParameter(NotificationUtils.TO_DATE);
      if (toDate != null) {
        toDate = new Date(Long.parseLong(tmpD));
      }
      
      EntityEncoder encoder = HTMLEntityEncoder.getInstance();
      Identity identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, creator, true);
      Profile profile = identity.getProfile();
      templateContext.put("USER", encoder.encode(profile.getFullName().toString()));
      templateContext.put("AVATAR", profile.getAvatarUrl() != null ? profile.getAvatarUrl() : LinkProvider.PROFILE_DEFAULT_AVATAR_URL);
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      //
      templateContext.put("VACATION_URL", vacationUrl);
      templateContext.put("FROM_DATE", Utils.formatDate(fromDate, Utils.getUserTimezone(notification.getTo())));
      templateContext.put("FROM_TO", Utils.formatDate(toDate, Utils.getUserTimezone(notification.getTo())));

      //
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
