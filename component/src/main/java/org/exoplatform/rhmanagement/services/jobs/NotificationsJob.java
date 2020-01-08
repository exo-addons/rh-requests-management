package org.exoplatform.rhmanagement.services.jobs;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.rhmanagement.dto.OfficialVacationDTO;
import org.exoplatform.rhmanagement.dto.UserRHDataDTO;
import org.exoplatform.rhmanagement.integration.notification.HRBirthdayNotificationPlugin;
import org.exoplatform.rhmanagement.integration.notification.HRContractAnniversaryNotificationPlugin;
import org.exoplatform.rhmanagement.services.OfficialVacationService;
import org.exoplatform.rhmanagement.services.UserDataService;
import org.exoplatform.rhmanagement.services.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Medamine on 28/02/2017.
 */
public class NotificationsJob implements Job {

    private static final Log LOG = ExoLogger.getLogger(NotificationsJob.class);
    private UserDataService userDataService = CommonsUtils.getService(UserDataService.class);
    private OfficialVacationService officialVacationService = CommonsUtils.getService(OfficialVacationService.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info("Notifications Job started");
        LOG.info("Check Anniversary Notifs");
       Long start = System.currentTimeMillis();
         IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
        List<UserRHDataDTO> employees = userDataService.getRhDataByStatus(true,0,0);
        Calendar now= Calendar.getInstance();
        for(UserRHDataDTO employee : employees){
            if(employee.getBirthDay()!=null){
            Calendar cal=Calendar.getInstance();
            cal.setTime(employee.getBirthDay());
            cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
                long rem=daysBetween(cal,now);
                if(rem==0){
                    Profile userProfile=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, employee.getUserId(), false).getProfile();
                    String message= "The Birthday of "+userProfile.getFullName()+"will be in "+rem+" days";
                    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(HRBirthdayNotificationPlugin.EMPLOYEE, employee).append(HRBirthdayNotificationPlugin.NOTIF_TYPE, message);
                    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(HRBirthdayNotificationPlugin.ID))).execute(ctx);
                }
            }
            if(employee.getContractStartDate()!=null){
                Calendar cal=Calendar.getInstance();
                cal.setTime(employee.getContractStartDate());
                cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
                long rem=daysBetween(cal,now);
            if(rem==0){
                LOG.info("contact annif for "+employee.getUserId() );
                Profile userProfile=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, employee.getUserId(), false).getProfile();
                String message= "The Anniversary of contract of "+userProfile.getFullName()+"will be in "+rem+" days";
                NotificationContext ctx = NotificationContextImpl.cloneInstance().append(HRContractAnniversaryNotificationPlugin.EMPLOYEE, employee).append(HRContractAnniversaryNotificationPlugin.NOTIF_TYPE, message);
                ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(HRContractAnniversaryNotificationPlugin.ID))).execute(ctx);
            }}


        }

/*        LOG.info("Check Bank Holidays Notifs");

        List<OfficialVacationDTO> officialVacationDTOS = officialVacationService.getOfficialVacations(0,0);
        for(OfficialVacationDTO oVacation : officialVacationDTOS){
            Calendar cal=Calendar.getInstance();
            cal.setTime(oVacation.getBeginDate());
            cal.set(Calendar.HOUR, 12);
            cal.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.HOUR, 12);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            long rem=daysBetween(now,cal);
            if(rem==7 || rem == 1){
                createActivity(oVacation);
            }
        }*/

        LOG.info("Notifications Job ended in " + (System.currentTimeMillis() - start) + " ms");

    }
    public long daysBetween(Calendar d1, Calendar d2){
        return Math.round((((float)d2.getTime().getTime() - (float)d1.getTime().getTime())/(60*60*24*1000)));
    }

    public  ExoSocialActivity createActivity (OfficialVacationDTO oVacation){
        DateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy");
                SpaceService spaceService= CommonsUtils.getService(SpaceService.class);
                IdentityManager identityManager= CommonsUtils.getService(IdentityManager.class);
                ActivityManager activityManager= CommonsUtils.getService(ActivityManager.class);
                String userId="root";
                String employeesSpace = System.getProperty(Utils.EMPLOYEES_SPACE, Utils.EMPLOYEES_SPACE_DEFAULT);
                Space space = spaceService.getSpaceByGroupId(employeesSpace);
                String dates="";
                Calendar from=Calendar.getInstance();
                from.setTime(oVacation.getBeginDate());
                Calendar to=Calendar.getInstance();
                to.setTime(oVacation.getEndDate());
                long rem=daysBetween(from,to);
                if (rem>0){
                    dates= "from "+formatter.format(from.getTime())+ " to "+formatter.format(to.getTime());
                }else{
                    dates= formatter.format(from.getTime());
                }
                if(space==null){
                    LOG.warn("Space not found");
                }else{
                    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
                    Identity posterIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);

                    if(posterIdentity!=null&&spaceIdentity!=null){
                        ExoSocialActivity activity = new ExoSocialActivityImpl();
                        activity.setType("DEFAULT_ACTIVITY");
                        activity.setTitle("<span id='rhActivity'>\n" +
                                "Hi,  <br/>\n" +
                                "The office will be closed "+dates+" for \""+oVacation.getDescription()+"\""
                        );
                        activity.setUserId(posterIdentity.getId());
                        return  activityManager.saveActivity(spaceIdentity, activity);
                    }else{
                        LOG.warn("Not able to create the activity, the Poster or Space Identity is missing");
                    }
                }

        return null;
    }
}
