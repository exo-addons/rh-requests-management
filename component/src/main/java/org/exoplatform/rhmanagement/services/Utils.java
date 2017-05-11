package org.exoplatform.rhmanagement.services;

import org.apache.commons.fileupload.FileItem;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
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
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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


}
