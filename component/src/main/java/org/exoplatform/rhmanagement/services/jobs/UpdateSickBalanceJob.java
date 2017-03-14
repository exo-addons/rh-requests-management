package org.exoplatform.rhmanagement.services.jobs;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.rhmanagement.dto.UserRHDataDTO;
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
public class UpdateSickBalanceJob implements Job {

    private static final Log LOG = ExoLogger.getLogger(UpdateSickBalanceJob.class);
    private UserDataService userDataService = CommonsUtils.getService(UserDataService.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info("==================Update Sick Balance Job started==================");
        Long start = System.currentTimeMillis();
        //userDataService = CommonsUtils.getService(UserDataService.class);
        List<UserRHDataDTO> employees = userDataService.getAllRhData(0,0);

        for(UserRHDataDTO employee : employees){
            employee.setSickdaysBalance(10);
            userDataService.save(employee);
        }
        LOG.info("=============================== Update Sick Balance Job ended in " + String.valueOf(System.currentTimeMillis() - start) + " ms ===============================.");

    }

}
