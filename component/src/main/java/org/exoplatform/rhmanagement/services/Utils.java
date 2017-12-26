package org.exoplatform.rhmanagement.services;

import org.apache.commons.fileupload.FileItem;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.rhmanagement.dto.BalanceHistoryDTO;
import org.exoplatform.rhmanagement.dto.UserRHDataDTO;
import org.exoplatform.rhmanagement.dto.VacationRequestDTO;
import org.exoplatform.rhmanagement.dto.ValidatorDTO;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;

import javax.jcr.Node;
import javax.jcr.Session;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Medamine on 14/02/2017.
 */
public class Utils {

    private static Log log = ExoLogger.getLogger(Utils.class);
    private final static String HR_MANAGERS_GROUP = "exo.hrmanagement.managers.group";
    private final static String HR_MANAGERS_GROUP_DEFAULT = "/rh-managers";

    public static final String ALL="all";
    public static final String ACTIVE="active";
    public static final String APPROVED="approved";
    public static final String DECLINED="declined";
    public static final String PENDING="pending";
    public static final String CANCELED="canceled";
    public static final String VALIDATED="validated";
    public static final String COMMENT="comment";
    public static final String HISTORY="history";
    public final static String EMPLOYEES_SPACE = "exo.hrmanagement.employees.space";
    public final static String EMPLOYEES_SPACE_DEFAULT = "exo_employees";


    public static void saveFile(FileItem item, String typeFolder, String parentNode){
        RepositoryService repositoryService = CommonsUtils.getService(RepositoryService.class);
        SessionProvider sessionProvider = SessionProvider.createSystemProvider();
        try {
            Session session = sessionProvider.getSession("collaboration",
                    repositoryService.getCurrentRepository());
            Node rootNode = session.getRootNode();

            if (!rootNode.hasNode("Application Data")) {
                rootNode.addNode("Application Data", "nt:folder");
                session.save();
            }

            rootNode= rootNode.getNode("Application Data");

            if (!rootNode.hasNode("hrmanagement")) {
                rootNode.addNode("hrmanagement", "nt:folder");
                session.save();
            }

            Node applicationDataNode = rootNode.getNode("hrmanagement");
            if (!applicationDataNode.hasNode(typeFolder)) {
                applicationDataNode.addNode(typeFolder, "nt:folder");
                session.save();
            }
            Node tFolder= applicationDataNode.getNode(typeFolder);

            Node fFolder = null;
            if (!tFolder.hasNode(parentNode )) {
                tFolder.addNode(parentNode, "nt:folder");
                session.save();
            }
            fFolder = tFolder.getNode(parentNode);

            Node fileNode = fFolder.addNode(item.getName(), "nt:file");
            Node jcrContent = fileNode.addNode("jcr:content", "nt:resource");
            jcrContent.setProperty("jcr:data", item.getInputStream());
            jcrContent.setProperty("jcr:lastModified", java.util.Calendar.getInstance());
            jcrContent.setProperty("jcr:encoding", "UTF-8");
            jcrContent.setProperty("jcr:mimeType", item.getContentType());
            fFolder.save();
            session.save();
        } catch (Exception e) {
            log.error("Error while saving the file: ", e.getMessage());
        } finally {
            sessionProvider.close();
        }

    }
    public static void deleteFile(String path){
        RepositoryService repositoryService = (RepositoryService) PortalContainer.getInstance().getComponentInstanceOfType(RepositoryService.class);
        SessionProvider sessionProvider = SessionProvider.createSystemProvider();
        try {
            Session session = sessionProvider.getSession("collaboration",repositoryService.getCurrentRepository());
            Node rootNode = session.getRootNode();
            if (rootNode.hasNode(path)) {
                Node att= rootNode.getNode(path);
                att.remove();
                session.save();
            }
        } catch (Exception e) {
            log.error("Error while deleting the file: ", e.getMessage());
        } finally {
            sessionProvider.close();
        }
    }

    public static User[]  getRhManagers(){

        String groupId = System.getProperty(HR_MANAGERS_GROUP);
        if(groupId==null){
            groupId =HR_MANAGERS_GROUP_DEFAULT;
        }
        OrganizationService organizationService = (OrganizationService) PortalContainer.getInstance().getComponentInstanceOfType(OrganizationService.class);
        UserHandler userHandler = organizationService.getUserHandler();
        ListAccess<User> allManagers=null;
        try {

            allManagers = userHandler.findUsersByGroupId(groupId);
           return (allManagers.load(0, allManagers.getSize()));

        } catch (Exception e) {
            log.error(" ERROR get manager ",e);
            return null;
        }
    }

    public static String formatDate(Date date, TimeZone timezone) {
        if (date == null) {
            return null;
        }

        Calendar today = Calendar.getInstance(timezone);
        Calendar cal = Calendar.getInstance(timezone);
        cal.setTime(date);
        String format = "MMM dd yyyy";
        if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            format = "MMM dd";
        }
        SimpleDateFormat df = new SimpleDateFormat(format);
        df.setTimeZone(timezone);
        return df.format(date);
    }

    public static TimeZone getUserTimezone(String username) {
        try {
            CalendarService calService=CommonsUtils.getService(CalendarService.class);
            CalendarSetting setting = calService.getCalendarSetting(username);
            return TimeZone.getTimeZone(setting.getTimeZone());
        } catch (Exception e) {
            log.error("Can't retrieve timezone", e);
        }
        return null;
    }

    public static Boolean canView(VacationRequestDTO vr, String user){
        if(user.equals(vr.getUserId())) return true;
        ValidatorService validatorService=CommonsUtils.getService(ValidatorService.class);
        for (ValidatorDTO validator : validatorService.getValidatorsByRequestId(vr.getId(),0,0)){
            if(user.equals(validator.getValidatorUserId())) return true;
        }
        for (User manager : Utils.getRhManagers()){
            if(user.equals(manager.getUserName())) return true;
        }
        return false;
    }


    public static void addBalanceHistoryEntry(VacationRequestDTO vReauqest, UserRHDataDTO userRHDataDTO, float intialHolidaysBalance, float intialSickBalance, String UpadateType, String upadater){
        try {
            BalanceHistoryDTO balanceHistoryDTO=new BalanceHistoryDTO();
            balanceHistoryDTO.setUserId(vReauqest.getUserId());
            balanceHistoryDTO.setIntialHolidaysBalance(intialHolidaysBalance);
            balanceHistoryDTO.setIntialSickBalance(intialSickBalance);
            balanceHistoryDTO.setHolidaysBalance(userRHDataDTO.getHolidaysBalance());
            balanceHistoryDTO.setSickBalance(userRHDataDTO.getSickdaysBalance());
            balanceHistoryDTO.setVacationType(vReauqest.getType());
            balanceHistoryDTO.setVacationId(vReauqest.getId());
            balanceHistoryDTO.setDaysNumber(vReauqest.getDaysNumber());
            balanceHistoryDTO.setUpdateType(UpadateType);
            balanceHistoryDTO.setUpdaterId(upadater);
            BalanceHistoryService balanceHistoryService=CommonsUtils.getService(BalanceHistoryService.class);
            balanceHistoryService.save(balanceHistoryDTO);
        } catch (Exception e) {
            log.error("Error when adding history entry", e);
        }
    }


    public static List<Date> getDaysBetweenDates(Date startdate, Date enddate)
    {
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
        List<Date> dates = new ArrayList<Date>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startdate);
        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(enddate);
        endCalendar.add(Calendar.DATE, 1);
        while (calendar.getTime().before(endCalendar.getTime()))
        {
            Date result = calendar.getTime();
            dates.add(result);
            calendar.add(Calendar.DATE, 1);
            log.info("_____"+result.toString());
        }
        return dates;
    }

    public static float calculateNumberOfDays(List<Date> oVacation, Date from, Date to){
        float nb=0;
        Calendar cFrom = Calendar.getInstance();
        cFrom.setTime(from);
        Calendar cTo = Calendar.getInstance();
        cTo.setTime(to);
        Calendar c = cFrom;
        cTo.add(Calendar.DATE, 1);
        while (c.before(cTo)) {
            if (Calendar.SUNDAY != c.get(Calendar.DAY_OF_WEEK) && Calendar.SATURDAY != c.get(Calendar.DAY_OF_WEEK) && !Utils.isOffDay(c, oVacation)) {
                nb++;
            }
            c.add(Calendar.DATE, 1);
        }
        if (cFrom.get(Calendar.HOUR_OF_DAY)> 12 && cFrom.get(Calendar.HOUR_OF_DAY)< 19 ) nb = nb - (float) 0.5;
        if (cTo.get(Calendar.HOUR_OF_DAY) < 15 && cTo.get(Calendar.HOUR_OF_DAY) > 7) nb = nb - (float) 0.5;
        return nb;
    }

    public static Date getTodate(Date from, int nbr){
        OfficialVacationService officialVacationService=CommonsUtils.getService(OfficialVacationService.class);
        int i =1;
        List<Date> oVacation=officialVacationService.getOfficialVacationDays();
        Calendar date = Calendar.getInstance();
        date.setTime(from);
        while (i<nbr){
           if  (Calendar.SUNDAY != date.get(Calendar.DAY_OF_WEEK) && Calendar.SATURDAY != date.get(Calendar.DAY_OF_WEEK)  && !Utils.isOffDay(date, oVacation)){
               i++;
           }
            date.add(Calendar.DATE, 1);
        }
       return date.getTime();
    }




    public static boolean isOffDay(Calendar date, List<Date> oVacation){
        if (oVacation.size() == 0) {
            return false;
        } else {
            for (Date oDate : oVacation) {
                Calendar cDate = Calendar.getInstance();
                cDate.setTime(oDate);
                if (sameDay(date, cDate)) return true;
            }

        }
        return false;
    }
    public static boolean sameDay(Calendar cal1, Calendar cal2){
        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }


}
