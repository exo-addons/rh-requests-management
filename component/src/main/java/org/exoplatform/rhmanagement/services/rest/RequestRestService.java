package org.exoplatform.rhmanagement.services.rest;


import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;
import org.exoplatform.social.service.rest.RestChecker;

/**
 * Created by Medamine on 05/01/2017.
 */

@Path("/rhrequest")
@Produces("application/json")
public class RequestRestService implements ResourceContainer {

    private static final Log LOG = ExoLogger.getLogger(RequestRestService.class);
    private static final String[] SUPPORTED_FORMATS = new String[]{"json"};
    private IdentityManager identityManager;

    public RequestRestService(IdentityManager identityManager) {
        this.identityManager=identityManager;
    }


    @GET
    @Path("uers/find")
    @RolesAllowed("users")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response find(@Context HttpServletRequest request,
                         @Context UriInfo uriInfo,
                         @QueryParam("nameToSearch") String nameToSearch) throws Exception {

        MediaType mediaType = RestChecker.checkSupportedFormat("json", SUPPORTED_FORMATS);
        try {
            JSONArray users = new JSONArray();
            ProfileFilter identityFilter = new ProfileFilter();
            identityFilter.setName(nameToSearch);
            List<Identity> identities = Arrays.asList(identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, identityFilter, false).load(0, 10));
            if (identities != null && identities.size() > 0) {
                for (Identity id : identities) {
                    Profile profile=id.getProfile();
                    JSONObject user = new JSONObject();
                    user.put("value",id.getRemoteId());
                    user.put("type","user");
                    user.put("invalid",false);
                    user.put("order","1");
                    user.put("avatarUrl",profile.getAvatarUrl());
                    user.put("text",profile.getFullName() + " (" + id.getRemoteId() + ")");
                    users.put(user);
                }
            }
            JSONObject jsonGlobal = new JSONObject();
            jsonGlobal.put("options",users);
            return Response.ok(jsonGlobal.toString(), mediaType).build();
        } catch (Exception e) {
            LOG.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An internal error has occured").build();
        }
    }

}
