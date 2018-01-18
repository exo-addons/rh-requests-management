package org.exoplatform.rhmanagement.services.jobs;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.rhmanagement.dto.BalanceHistoryDTO;
import org.exoplatform.rhmanagement.dto.UserRHDataDTO;
import org.exoplatform.rhmanagement.integration.notification.HRContractAnniversaryNotificationPlugin;
import org.exoplatform.rhmanagement.integration.notification.VacationBalanceNotificationPlugin;
import org.exoplatform.rhmanagement.services.BalanceHistoryService;
import org.exoplatform.rhmanagement.services.UserDataService;
import org.exoplatform.rhmanagement.services.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Medamine on 28/02/2017.
 */
public class UpdateHolidaysBalanceJob implements Job {

    private static final Log LOG = ExoLogger.getLogger(UpdateHolidaysBalanceJob.class);
    private UserDataService userDataService= CommonsUtils.getService(UserDataService.class);
    private BalanceHistoryService balanceHistoryService= CommonsUtils.getService(BalanceHistoryService.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info("==================Update Holidays Balance Job started==================");
        Long start = System.currentTimeMillis();
       // userDataService = CommonsUtils.getService(UserDataService.class);
        List<UserRHDataDTO> employees = userDataService.getRhDataByStatus(true,0,0);
        Calendar cDate = Calendar.getInstance();
        for(UserRHDataDTO employee : employees){
            float holidays=employee.getHolidaysBalance();
            employee.setHolidaysBalance(holidays+2);
            if(cDate.get(Calendar.MONTH)==0 && employee.getHolidaysBalance()>24){
                employee.setHolidaysBalance(24);
            }
            userDataService.save(employee);

            try {
                BalanceHistoryDTO balanceHistoryDTO=new BalanceHistoryDTO();
                balanceHistoryDTO.setUserId(employee.getUserId());
                balanceHistoryDTO.setIntialHolidaysBalance(holidays);
                balanceHistoryDTO.setIntialSickBalance(employee.getSickdaysBalance());
                balanceHistoryDTO.setHolidaysBalance(employee.getHolidaysBalance());
                balanceHistoryDTO.setSickBalance(employee.getSickdaysBalance());
                balanceHistoryDTO.setVacationType("holiday");
                balanceHistoryDTO.setVacationId(-1);
                Float nVb=employee.getHolidaysBalance();
                balanceHistoryDTO.setDaysNumber(nVb-holidays);
                balanceHistoryDTO.setUpdateType("monthlyHolidayUpdate");
                balanceHistoryDTO.setUpdaterId("System");
                balanceHistoryService.save(balanceHistoryDTO);
            } catch (Exception e) {
                LOG.error("Error when adding history entry", e);
            }

            if(cDate.get(Calendar.MONTH)==5 || cDate.get(Calendar.MONTH)>=8){
                Float days=Utils.getEndYearBalance(employee.getHolidaysBalance());
                if (days>24){
                    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(VacationBalanceNotificationPlugin.EMPLOYEE, employee).append(VacationBalanceNotificationPlugin.DAYS_TO_CONSUME, days-24);
                    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(VacationBalanceNotificationPlugin.ID))).execute(ctx);
                }
            }
        }
        LOG.info("=============================== Update Holidays Balance Job ended in " + String.valueOf(System.currentTimeMillis() - start) + " ms ===============================.");

    }

}
