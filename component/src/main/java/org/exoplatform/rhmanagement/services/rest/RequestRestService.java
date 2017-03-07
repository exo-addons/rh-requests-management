package org.exoplatform.rhmanagement.services.rest;


import org.exoplatform.commons.utils.ListAccess;
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
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private SpaceService spaceService;
    private VacationRequestService vacationRequestService;
    private ValidatorService validatorService;

    public RequestRestService(IdentityManager identityManager,SpaceService spaceService, VacationRequestService vacationRequestService, ValidatorService validatorService) {
        this.identityManager=identityManager;
        this.spaceService=spaceService;
        this.vacationRequestService=vacationRequestService;
        this.validatorService=validatorService;
    }


    @GET
    @Path("users/find")
    @RolesAllowed("users")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response find(@Context HttpServletRequest request,
                         @Context UriInfo uriInfo,
                         @QueryParam("nameToSearch") String nameToSearch,
                         @QueryParam("spaceURL") String spaceURL,
                         @QueryParam("currentUser") String currentUser) throws Exception {

        MediaType mediaType = RestChecker.checkSupportedFormat("json", SUPPORTED_FORMATS);
        try {
            JSONArray users = new JSONArray();

            Space space = spaceService.getSpaceByUrl(spaceURL);
            if(space!=null){
                List<Profile> profiles = getSpaceMembersProfiles(space).stream()
                        .filter(a -> a.getFullName().toLowerCase().contains(nameToSearch.toLowerCase()))
                        .collect(Collectors.toList());
                if (profiles != null && profiles.size() > 0) {
                    for (Profile profile : profiles) {
                        JSONObject user = new JSONObject();
                        user.put("value",profile.getIdentity().getRemoteId());
                        user.put("type","user");
                        user.put("invalid",false);
                        user.put("order","1");
                        user.put("avatarUrl",profile.getAvatarUrl());
                        user.put("text",profile.getFullName() + " (" + profile.getIdentity().getRemoteId() + ")");
                        users.put(user);
                    }
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
                    if (("validated").equals(vr.getStatus()))event.put("backgroundColor","green");
                    if (("approved").equals(vr.getStatus())) event.put("backgroundColor","blue");
                    if (("declined").equals(vr.getStatus())||("canceled").equals(vr.getStatus()))event.put("backgroundColor","red");
                    if (("pending").equals(vr.getStatus()))event.put("backgroundColor","grey");
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
                        if (("validated").equals(requestDTO.getStatus()))event.put("backgroundColor","green");
                        if (("approved").equals(requestDTO.getStatus())) event.put("backgroundColor","blue");
                        if (("declined").equals(requestDTO.getStatus())||("canceled").equals(requestDTO.getStatus()))event.put("backgroundColor","red");
                        if (("pending").equals(requestDTO.getStatus()))event.put("backgroundColor","grey");
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

private List<Profile> getSpaceMembersProfiles(Space space){
        List<Profile> profiles=new ArrayList<Profile>();
        for(String userId : space.getMembers()){
            Profile userProfile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false).getProfile();
            profiles.add(userProfile);
        }
        return profiles;

    }
}
