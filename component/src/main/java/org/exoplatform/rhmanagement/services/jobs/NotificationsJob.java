package org.exoplatform.rhmanagement.services.jobs;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.rhmanagement.dto.UserRHDataDTO;
import org.exoplatform.rhmanagement.integration.notification.HRBirthdayNotificationPlugin;
import org.exoplatform.rhmanagement.services.UserDataService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Medamine on 28/02/2017.
 */
public class NotificationsJob implements Job {

    private static final Log LOG = ExoLogger.getLogger(NotificationsJob.class);
    private UserDataService userDataService = CommonsUtils.getService(UserDataService.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info("==================Notifications Job started==================");
       Long start = System.currentTimeMillis();
         IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
        List<UserRHDataDTO> employees = userDataService.getAllRhData(0,0);
        Calendar now= Calendar.getInstance();
        for(UserRHDataDTO employee : employees){
            if(employee.getBirthDay()!=null){
            Calendar cal=Calendar.getInstance();
            cal.setTime(employee.getBirthDay());
            cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
                int rem=daysBetween(cal,now);
                if(rem==0){
                    Profile userProfile=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, employee.getUserId(), false).getProfile();
                    String message= "The Birthday of "+userProfile.getFullName()+"will be in "+rem+" days";
                    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(HRBirthdayNotificationPlugin.EMPLOYEE, employee).append(HRBirthdayNotificationPlugin.NOTIF_TYPE, message);
                    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(HRBirthdayNotificationPlugin.ID))).execute(ctx);
                }
            }
/*            if(employee.getContractStartDate()!=null){
                Calendar cal=Calendar.getInstance();
                cal.setTime(employee.getContractStartDate());
                cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
                int rem=daysBetween(cal,now);
            if(rem<10){
                LOG.info("=============contact annif for "+employee.getUserId() +"================== ");
                Profile userProfile=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, employee.getUserId(), false).getProfile();
                String message= "The Anniversary of contract of "+userProfile.getFullName()+"will be in "+rem+" days";
                NotificationContext ctx = NotificationContextImpl.cloneInstance().append(HRNotificationPlugin.EMPLOYEE, employee).append(HRNotificationPlugin.NOTIF_TYPE, message);
                ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(HRNotificationPlugin.ID))).execute(ctx);
            }}*/


        }
        LOG.info("=============================== Notifications Job ended in " + String.valueOf(System.currentTimeMillis() - start) + " ms ===============================.");

    }
    public int daysBetween(Calendar d1, Calendar d2){
        return (int)( (d2.getTime().getTime() - d1.getTime().getTime()) / (1000 * 60 * 60 * 24));
    }
}
