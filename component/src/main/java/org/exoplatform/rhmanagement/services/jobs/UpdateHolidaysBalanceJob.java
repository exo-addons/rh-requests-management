package org.exoplatform.rhmanagement.services.jobs;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.rhmanagement.dto.BalanceHistoryDTO;
import org.exoplatform.rhmanagement.dto.UserRHDataDTO;
import org.exoplatform.rhmanagement.services.BalanceHistoryService;
import org.exoplatform.rhmanagement.services.UserDataService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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
        List<UserRHDataDTO> employees = userDataService.getAllRhData(0,0);

        for(UserRHDataDTO employee : employees){
            float holidays=employee.getHolidaysBalance();
            employee.setHolidaysBalance(holidays+2);
            userDataService.save(employee);

            try {
                BalanceHistoryDTO balanceHistoryDTO=new BalanceHistoryDTO();
                balanceHistoryDTO.setUserId(employee.getUserId());
                balanceHistoryDTO.setIntialHolidaysBalance(holidays);
                balanceHistoryDTO.setIntialSickBalance(employee.getSickdaysBalance());
                balanceHistoryDTO.setHolidaysBalance(employee.getHolidaysBalance());
                balanceHistoryDTO.setSickBalance(employee.getSickdaysBalance());
                balanceHistoryDTO.setVacationType("holiday");
                balanceHistoryDTO.setDaysNumber(2);
                balanceHistoryDTO.setUpdateType("monthlyHolidayUpdate");
                balanceHistoryDTO.setUpdaterId("System");


                balanceHistoryService.save(balanceHistoryDTO);
            } catch (Exception e) {
                LOG.error("Error when adding history entry", e);
            }

        }
        LOG.info("=============================== Update Holidays Balance Job ended in " + String.valueOf(System.currentTimeMillis() - start) + " ms ===============================.");

    }

}
