package org.exoplatform.rhmanagement.services;

import org.apache.commons.fileupload.FileItem;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;

import javax.jcr.Node;
import javax.jcr.Session;
import java.util.ArrayList;

/**
 * Created by Medamine on 14/02/2017.
 */
public class Utils {

    private static Log log = ExoLogger.getLogger(Utils.class);

    public static void saveFile(FileItem item, String typeFolder, String parentNode){
        RepositoryService repositoryService = (RepositoryService) PortalContainer.getInstance().getComponentInstanceOfType(RepositoryService.class);
        SessionProvider sessionProvider = SessionProvider.createSystemProvider();
        try {
            Session session = sessionProvider.getSession("collaboration",
                    repositoryService.getCurrentRepository());
            Node rootNode = session.getRootNode();

            if (!rootNode.hasNode("Application Data")) {
                rootNode.addNode("Application Data", "nt:folder");
                session.save();
            }

            rootNode = rootNode.getNode("Application Data");

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
        String groupId = "/rh-managers";
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

}
