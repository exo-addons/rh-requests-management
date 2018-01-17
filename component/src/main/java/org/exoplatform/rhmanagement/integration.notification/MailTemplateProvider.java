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

import org.bouncycastle.ocsp.Req;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.DigestTemplate.ElementType;
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
import org.exoplatform.social.notification.LinkProviderUtils;
import org.gatein.common.text.EntityEncoder;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@TemplateConfigs(templates = {

    @TemplateConfig(pluginId = RequestRepliedPlugin.ID, template = "war:/notification/templates/mail/RequestReplyPlugin.gtmpl"),
    @TemplateConfig(pluginId = RequestStatusChangedPlugin.ID, template = "war:/notification/templates/mail/UpdateRequestPlugin.gtmpl"),
    @TemplateConfig(pluginId = RequestCreatedPlugin.ID, template = "war:/notification/templates/mail/CreateRequestPlugin.gtmpl"),
     @TemplateConfig(pluginId = HRBirthdayNotificationPlugin.ID, template = "war:/notification/templates/mail/HRBirthdayNotificationPlugin.gtmpl"),
        @TemplateConfig(pluginId = HRContractAnniversaryNotificationPlugin.ID, template = "war:/notification/templates/mail/HRContractAnniversaryNotificationPlugin.gtmpl"),
        @TemplateConfig(pluginId = RequestCommentedPlugin.ID, template = "war:/notification/templates/mail/RequestCommentedPlugin.gtmpl")
})
public class MailTemplateProvider extends TemplateProvider {
  //--- Use a dedicated DateFormatter to handle date pattern coming from underlying levels : Wed Mar 15 01:00:00 CET 2017
  // --- Create formatter
  protected DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
  //protected DateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
  protected static Log log = ExoLogger.getLogger(MailTemplateProvider.class);

  public MailTemplateProvider(InitParams initParams) {
    super(initParams);

    this.templateBuilders.put(PluginKey.key(RequestRepliedPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(RequestStatusChangedPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(RequestCreatedPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(HRBirthdayNotificationPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(HRContractAnniversaryNotificationPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(RequestCommentedPlugin.ID), new TemplateBuilder());
  }
  
  private class TemplateBuilder extends AbstractTemplateBuilder {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {

      EntityEncoder encoder = HTMLEntityEncoder.getInstance();
      
      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);

      String creator = notification.getValueOwnerParameter(NotificationUtils.CREATOR);
      String vacationUrl = notification.getValueOwnerParameter(NotificationUtils.VACATION_URL);
      String userName = notification.getValueOwnerParameter(NotificationUtils.USER_NAME);

      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
      Identity author = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, creator, true);
      Profile profile = author.getProfile();
      //creator
      templateContext.put("USER", encoder.encode(profile.getFullName()));
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(profile));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", author.getRemoteId()));
      //receiver
      Identity receiver = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, notification.getTo(), true);
      templateContext.put("FIRST_NAME", encoder.encode(receiver.getProfile().getProperty(Profile.FIRST_NAME).toString()));
      //
        if(vacationUrl!=null) {
            templateContext.put("VACATION_URL", vacationUrl);
        }

      if(userName!=null) {
        Identity id=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName, false);
        templateContext.put("USER_NAME", id.getProfile().getFullName());
      }


      //--- Get Date From :
      String fromDate = notification.getValueOwnerParameter(NotificationUtils.FROM_DATE);
      if (fromDate != null) {
        Date theDate = new Date();
        try {
          theDate = (Date)formatter.parse(fromDate);
        } catch (Exception e){
          log.error("Error when parsing FROM_DATE var {}",fromDate, e);
        }
        templateContext.put("FROM_DATE", Utils.formatDate(theDate, Utils.getUserTimezone(notification.getTo())));
      }
      //--- Get Date To : underlying levels : Wed Mar 15 01:00:00 CET 2017
      String toDate = notification.getValueOwnerParameter(NotificationUtils.TO_DATE);
      if (toDate != null) {
        Date theDate = new Date();
        try {
          theDate = (Date)formatter.parse(toDate);
        } catch (Exception e){
          log.error("Error when parsing TO_DATE var {}",fromDate, e);
        }
        templateContext.put("TO_DATE", Utils.formatDate(theDate, Utils.getUserTimezone(notification.getTo())));
      }


      String birthDate = notification.getValueOwnerParameter(NotificationUtils.BIRTHDAY_DATE);
      if (birthDate != null) {
        Date theDate = new Date();
        try {
          theDate = (Date)formatter.parse(birthDate);
          templateContext.put("BIRTHDAY_DATE", Utils.formatDate(theDate, Utils.getUserTimezone(notification.getTo())));


        } catch (Exception e){
          log.error("Error when parsing BIRTHDAY_DATE var {}",birthDate, e);
        }
      }

      String contractAnnivDate = notification.getValueOwnerParameter(NotificationUtils.CONTRACT_ANNIV_DATE);
      if (contractAnnivDate != null) {
        Date theDate = new Date();
        try {
          theDate = (Date)formatter.parse(contractAnnivDate);
          templateContext.put("CONTRACT_ANNIV_DATE", Utils.formatDate(theDate, Utils.getUserTimezone(notification.getTo())));


        } catch (Exception e){
          log.error("Error when parsing CONTRACT_ANNIV_DATE var {}",contractAnnivDate, e);
        }
      }
      templateContext.put("FOOTER_LINK", LinkProviderUtils.getRedirectUrl("notification_settings", receiver.getRemoteId()));
      String subject = TemplateUtils.processSubject(templateContext);

      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.subject(subject).body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      EntityEncoder encoder = HTMLEntityEncoder.getInstance();
      
      List<NotificationInfo> notifications = ctx.getNotificationInfos();
      NotificationInfo first = notifications.get(0);

      String language = getLanguage(first);
      TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
      //
      Identity receiver = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, first.getTo(), true);
      templateContext.put("FIRST_NAME", encoder.encode(receiver.getProfile().getProperty(Profile.FIRST_NAME).toString()));
      templateContext.put("FOOTER_LINK", LinkProviderUtils.getRedirectUrl("notification_settings", receiver.getRemoteId()));
      
      try {
        writer.append(buildDigestMsg(notifications, templateContext));
      } catch (IOException e) {
        ctx.setException(e);
        return false;
      }
      return true;
    }

    protected String buildDigestMsg(List<NotificationInfo> notifications, TemplateContext templateContext) {
      EntityEncoder encoder = HTMLEntityEncoder.getInstance();
      
      Map<String, List<NotificationInfo>> map = new HashMap<String, List<NotificationInfo>>();
      for (NotificationInfo notif : notifications) {
        String activityID = notif.getValueOwnerParameter(NotificationUtils.ACTIVITY_ID);
        List<NotificationInfo> tmp = map.get(activityID);
        if (tmp == null) {
          tmp = new LinkedList<NotificationInfo>();
          map.put(activityID, tmp);
        }
        tmp.add(notif);
      }
      
      StringBuilder sb = new StringBuilder();
      for (String activityID : map.keySet()) {
        List<NotificationInfo> notifs = map.get(activityID);
        NotificationInfo first = notifs.get(0);
        String vacationUrl = first.getValueOwnerParameter(NotificationUtils.VACATION_URL);
        if (notifs.size() == 1) {
          templateContext.digestType(ElementType.DIGEST_ONE.getValue());
        } else {
          templateContext.digestType(ElementType.DIGEST_MORE.getValue());
        }
        sb.append("<li style=\"margin:0 0 13px 14px;font-size:13px;line-height:18px;font-family:HelveticaNeue,Helvetica,Arial,sans-serif\"><div style=\"color: #333;\">");
        String digester = TemplateUtils.processDigest(templateContext);
        sb.append(digester);
        sb.append("</div></li>");
      }
      return sb.toString();
    }
  }


  public static String getExcerpt(String str, int len) {
    if (str == null) {
      return "";
    } else if (str.length() > len) {
      str = str.substring(0, len);
      int lastSpace = str.lastIndexOf(" ");
      return ((lastSpace > 0) ? str.substring(0, lastSpace) : str) + "...";
    } else {
      return str;      
    }
  }

}
