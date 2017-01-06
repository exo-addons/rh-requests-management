package org.exoplatform.rhmanagement.services.rest;


import org.exoplatform.rhmanagement.dto.VacationRequestDTO;
import org.exoplatform.rhmanagement.dto.ValidatorDTO;
import org.exoplatform.rhmanagement.services.VacationRequestService;
import org.exoplatform.rhmanagement.services.ValidatorService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
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
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
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
    private VacationRequestService vacationRequestService;
    private ValidatorService validatorService;

    public RequestRestService(IdentityManager identityManager,VacationRequestService vacationRequestService, ValidatorService validatorService) {
        this.identityManager=identityManager;
        this.vacationRequestService=vacationRequestService;
        this.validatorService=validatorService;
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


    @GET
    @Path("getevents")
    @RolesAllowed("users")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getEvents(@Context HttpServletRequest request,
                         @Context UriInfo uriInfo) throws Exception {

        MediaType mediaType = RestChecker.checkSupportedFormat("json", SUPPORTED_FORMATS);
        String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            JSONArray events = new JSONArray();

            List<VacationRequestDTO> vrs=vacationRequestService.getVacationRequestsByUserId(currentUser,0,100);

            if (vrs.size() > 0) {
                for (VacationRequestDTO vr : vrs) {
                    JSONObject event = new JSONObject();
                    event.put("id",vr.getId());
                    event.put("title",vr.getUserFullName());
                    event.put("start",dt1.format(vr.getFromDate()));
                    event.put("end",dt1.format(vr.getToDate()));
                    event.put("backgroundColor","blue");
                    events.put(event);
                }
            }
            List<ValidatorDTO> validators= validatorService.getValidatorsByValidatorUserId(currentUser,0,100);

            if (vrs.size() > 0) {
                for (ValidatorDTO validator : validators) {
                    VacationRequestDTO requestDTO=vacationRequestService.getVacationRequest(validator.getRequestId());
                    if(requestDTO!=null) {
                        JSONObject event = new JSONObject();
                        event.put("id",requestDTO.getId());
                        event.put("title",requestDTO.getUserFullName());
                        event.put("start",dt1.format(requestDTO.getFromDate()));
                        event.put("end",dt1.format(requestDTO.getFromDate()));
                        event.put("backgroundColor","red");
                        events.put(event);
                    }
                }
            }
            return Response.ok(events.toString(), mediaType).build();
        } catch (Exception e) {
            LOG.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An internal error has occured").build();
        }
    }


}
