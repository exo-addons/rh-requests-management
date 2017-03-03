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
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.gatein.common.text.EntityEncoder;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

@TemplateConfigs(templates = {

    @TemplateConfig(pluginId = RequestRepliedPlugin.ID, template = "war:/notification/templates/mail/RequestReplyPlugin.gtmpl"),
    @TemplateConfig(pluginId = RequestStatusChangedPlugin.ID, template = "war:/notification/templates/mail/UpdateRequestPlugin.gtmpl"),
    @TemplateConfig(pluginId = RequestCreatedPlugin.ID, template = "war:/notification/templates/mail/CreateRequestPlugin.gtmpl")
})
public class MailTemplateProvider extends TemplateProvider {

  public MailTemplateProvider(InitParams initParams) {
    super(initParams);

    this.templateBuilders.put(PluginKey.key(RequestRepliedPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(RequestStatusChangedPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(RequestCreatedPlugin.ID), new TemplateBuilder());
  }
  
  private class TemplateBuilder extends AbstractTemplateBuilder {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {

      EntityEncoder encoder = HTMLEntityEncoder.getInstance();
      
      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);

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

      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      Identity author = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, creator, true);
      Profile profile = author.getProfile();
      //creator
      templateContext.put("USER", encoder.encode(profile.getFullName()));
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(profile));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", author.getRemoteId()));
      //receiver
      Identity receiver = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, notification.getTo(), true);
      templateContext.put("FIRST_NAME", encoder.encode(receiver.getProfile().getProperty(Profile.FIRST_NAME).toString()));
      //
/*      templateContext.put("TASK_TITLE", encoder.encode(taskTitle));
      templateContext.put("TASK_DESCRIPTION", encoder.encode(getExcerpt(taskDesc, 130)));
      templateContext.put("COMMENT_TEXT", commentText == null ? "" : encoder.encode(getExcerpt(commentText, 130)));*/
      templateContext.put("VACATION_URL", vacationUrl);
      templateContext.put("FROM_DATE", Utils.formatDate(fromDate, Utils.getUserTimezone(notification.getTo())));
      templateContext.put("FROM_TO", Utils.formatDate(toDate, Utils.getUserTimezone(notification.getTo())));
      //
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
        String vacationkUrl = first.getValueOwnerParameter(NotificationUtils.VACATION_URL);

        String taskTitle = "";
        if (notifs.size() == 1) {
         // taskTitle = first.getValueOwnerParameter(NotificationUtils.TASK_TITLE);
          templateContext.digestType(ElementType.DIGEST_ONE.getValue());
        } else {
          templateContext.digestType(ElementType.DIGEST_MORE.getValue());
        }
     //   templateContext.put("TASK_TITLE", "<a href=\"" + taskUrl + "\" style=\"text-decoration: none; color: #2f5e92; font-family: 'HelveticaNeue Bold', Helvetica, Arial, sans-serif\">" + encoder.encode(getExcerpt(taskTitle, 30)) + "</a>");
       // templateContext.put("DUE_DATE", getDueDate(first));
        
        sb.append("<li style=\"margin:0 0 13px 14px;font-size:13px;line-height:18px;font-family:HelveticaNeue,Helvetica,Arial,sans-serif\"><div style=\"color: #333;\">");
        String digester = TemplateUtils.processDigest(templateContext);
        sb.append(digester);
        sb.append("</div></li>");
      }

      return sb.toString();
    }


  };

/*  private class CommentTemplateBuilder extends TemplateBuilder {
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
        String taskUrl = first.getValueOwnerParameter(NotificationUtils.TASK_URL);
        String projectUrl = first.getValueOwnerParameter(NotificationUtils.PROJECT_URL);

        PluginConfig config = CommonsUtils.getService(PluginSettingService.class).getPluginConfig(templateContext.getPluginId());
        Locale locale = org.exoplatform.commons.notification.NotificationUtils.getLocale(templateContext.getLanguage());
        String resourcePath = config.getBundlePath();

        //. Count user
        List<String> creators = new ArrayList<>();
        for (NotificationInfo n : notifs) {
          String creator = n.getValueOwnerParameter(NotificationUtils.CREATOR.getKey());
          creators.remove(creator);
          creators.add(creator);
        }
        Collections.reverse(creators);

        IdentityManager idManager = CommonsUtils.getService(IdentityManager.class);
        Profile lastUser = idManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, creators.get(0), true).getProfile();
        String lastProfileURL = LinkProviderUtils.getRedirectUrl("user", creators.get(0));
        String user = "<a href=\"" + lastProfileURL + "\" style=\"text-decoration: none; color: #2f5e92; font-family: 'HelveticaNeue Bold', Helvetica, Arial, sans-serif\">" + encoder.encode(lastUser.getFullName()) + "</a>";

        if (creators.size() <= 1) {
          templateContext.digestType(ElementType.DIGEST_ONE.getValue());
        } else {
          templateContext.digestType(ElementType.DIGEST_MORE.getValue());

          lastUser = idManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, creators.get(1), true).getProfile();
          lastProfileURL = LinkProviderUtils.getRedirectUrl("user", creators.get(1));

          if (creators.size() == 2) {
            user += " " + TemplateUtils.getResourceBundle("Notification.label.and", locale, resourcePath);
          } else {
            user += ", ";
          }
          user += " <a href=\"" + lastProfileURL + "\" style=\"text-decoration: none; color: #2f5e92; font-family: 'HelveticaNeue Bold', Helvetica, Arial, sans-serif\">" + encoder.encode(lastUser.getFullName()) + "</a>";

          if (creators.size() == 3) {
            user += " " + TemplateUtils.getResourceBundle("Notification.label.one.other", locale, resourcePath);
          } else if (creators.size() > 3) {
            String s = TemplateUtils.getResourceBundle("Notification.label.more.other", locale, resourcePath);
            s = s.replace("{0}", String.valueOf(creators.size() - 2));
            user += " " + s;
          }
        }
        templateContext.put("USER", user);

        // Count task
        List<Long> tasks = new ArrayList<>();
        for (NotificationInfo n : notifs) {
          long id = Long.parseLong(n.getValueOwnerParameter(NotificationUtils.TASKS));
          tasks.remove(id);
          tasks.add(id);
        }

        String countTask = "";
        if (tasks.size() <= 1) {
          countTask = TemplateUtils.getResourceBundle("Notification.label.task", locale, resourcePath);
          String taskTitle = first.getValueOwnerParameter(NotificationUtils.TASK_TITLE);
          templateContext.put("TASK_TITLE", "<a href=\"" + taskUrl + "\" style=\"text-decoration: none; color: #2f5e92; font-family: 'HelveticaNeue Bold', Helvetica, Arial, sans-serif\">" + encoder.encode(getExcerpt(taskTitle, 30)) + "</a>");
        } else {
          countTask = TemplateUtils.getResourceBundle("Notification.label.tasks", locale, resourcePath);
          countTask = countTask.replace("{0}", String.valueOf(tasks.size()));
          templateContext.put("TASK_TITLE", "");
        }
        templateContext.put("COUNT_TASK", countTask);

        String projectName = first.getValueOwnerParameter(NotificationUtils.PROJECT_NAME);
        String inProject = "";
        if (projectName != null && !projectName.isEmpty()) {
          inProject = TemplateUtils.getResourceBundle("Notification.message.inProject", locale, resourcePath);
          inProject = inProject.replace("{0}", "<a href=\"" + projectUrl + "\" style=\"text-decoration: none; color: #2f5e92; font-family: 'HelveticaNeue Bold', Helvetica, Arial, sans-serif\"><strong>" +
                  encoder.encode(projectName) + "</strong></a>");
        } else {
          inProject = "";
        }
        if (tasks.size() <= 1) {
          inProject += ":";
        }
        templateContext.put("PROJECT_NAME", inProject);

        sb.append("<li style=\"margin:0 0 13px 14px;font-size:13px;line-height:18px;font-family:HelveticaNeue,Helvetica,Arial,sans-serif\"><div style=\"color: #333;\">");
        String digester = TemplateUtils.processDigest(templateContext);
        sb.append(digester);
        sb.append("</div></li>");
      }

      return sb.toString();
    }
  }*/

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
