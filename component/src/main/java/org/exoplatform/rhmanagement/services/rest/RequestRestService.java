package org.exoplatform.rhmanagement.services.rest;


import org.exoplatform.rhmanagement.dto.*;
import org.exoplatform.rhmanagement.services.OfficialVacationService;
import org.exoplatform.rhmanagement.services.UserDataService;
import org.exoplatform.rhmanagement.services.VacationRequestService;
import org.exoplatform.rhmanagement.services.ValidatorService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.rest.RestChecker;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    private OrganizationService organizationService;
    private UserDataService userDataService;
    private OfficialVacationService officialVacationService;

    public RequestRestService(IdentityManager identityManager,SpaceService spaceService, VacationRequestService vacationRequestService, ValidatorService validatorService, OrganizationService organizationService, UserDataService userDataService, OfficialVacationService officialVacationService) {
        this.identityManager=identityManager;
        this.spaceService=spaceService;
        this.vacationRequestService=vacationRequestService;
        this.validatorService=validatorService;
        this.organizationService=organizationService;
        this.userDataService=userDataService;
        this.officialVacationService=officialVacationService;
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
                        if(profile.getAvatarUrl()!=null){
                            user.put("avatarUrl",profile.getAvatarUrl());
                        }else{
                            user.put("avatarUrl","/eXoSkin/skin/images/system/UserAvtDefault.png");
                        }
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
                    if (("pending").equals(vr.getStatus()))event.put("backgroundColor","orange");
                    events.put(event);
                }
            }

            List <String> listEmployees = new ArrayList<String>();
            listEmployees = userDataService.createAllSubordonatesList(currentUser,listEmployees);
            if(listEmployees.size()>0){
                vrs = vacationRequestService.getVacationRequestByManager(currentUser,listEmployees,0,100);

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
                        if (("pending").equals(vr.getStatus()))event.put("backgroundColor","orange");
                        events.put(event);
                    }
                }
            }


            List<ValidatorDTO> validators= validatorService.getValidatorsByValidatorUserId(currentUser,0,100);

            if (validators.size() > 0) {
                for (ValidatorDTO validator : validators) {
                    VacationRequestDTO requestDTO=vacationRequestService.getVacationRequest(validator.getRequestId());
                    if(requestDTO!=null) {
                        JSONObject event = new JSONObject();
                        event.put("id",requestDTO.getId());
                        event.put("title",requestDTO.getUserFullName());
                        event.put("start",dt1.format(requestDTO.getFromDate()));
                        event.put("end",dt1.format(requestDTO.getToDate()));
                        if (("validated").equals(requestDTO.getStatus()))event.put("backgroundColor","green");
                        if (("approved").equals(requestDTO.getStatus())) event.put("backgroundColor","blue");
                        if (("declined").equals(requestDTO.getStatus())||("canceled").equals(requestDTO.getStatus()))event.put("backgroundColor","red");
                        if (("pending").equals(requestDTO.getStatus()))event.put("backgroundColor","orange");
                        events.put(event);
                    }
                }
            }

            List<OfficialVacationDTO> oVacations= officialVacationService.getOfficialVacations(0,0);
            if (oVacations.size() > 0) {
                for (OfficialVacationDTO oVacation : oVacations) {

                    try {
                        JSONObject event = new JSONObject();
                        event.put("id",oVacation.getId());
                        event.put("title",oVacation.getLabel());
                        event.put("start",dt1.format(oVacation.getBeginDate()));
                        event.put("end",dt1.format(oVacation.getEndDate()));
                        event.put("backgroundColor","grey");
                        events.put(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            officialVacationService.getOfficialVacationDays();

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


    @POST
    @Path("createemployees")
    @RolesAllowed("administrators")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response createEmployees(@Context HttpServletRequest request,
                              @Context UriInfo uriInfo,
                              List<UserRHDataDTO> emloyees      ) throws Exception {

        MediaType mediaType = RestChecker.checkSupportedFormat("json", SUPPORTED_FORMATS);
        UserHandler uh = organizationService.getUserHandler();
        try {
            for(UserRHDataDTO emp:emloyees){
               if (uh.findUserByName(emp.getUserId()) != null){
                    userDataService.save(emp);
                }

            }
            return Response.ok().build();
        } catch (Exception e) {
            LOG.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An internal error has occured").build();
        }
    }


    @POST
    @Path("updatemanagers")
    @RolesAllowed("administrators")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response updateManagers(@Context HttpServletRequest request,
                                    @Context UriInfo uriInfo,
                                    List<UserRHDataDTO> emloyees) throws Exception {

        MediaType mediaType = RestChecker.checkSupportedFormat("json", SUPPORTED_FORMATS);
        try {
            for(UserRHDataDTO emp:emloyees){
                UserRHDataDTO user = userDataService.getUserRHDataByUserId(emp.getUserId());
                if (user != null){
                    user.setHierarchicalManager(emp.getHierarchicalManager());
                    user.setFunctionalManager(emp.getFunctionalManager());
                    userDataService.save(user);
                }

            }
            return Response.ok().build();
        } catch (Exception e) {
            LOG.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An internal error has occured").build();
        }
    }



    @GET
    @Path("users/exportmanagers")
    @RolesAllowed("users")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response exportManagers(@Context HttpServletRequest request,
                         @Context UriInfo uriInfo) throws Exception {

        MediaType mediaType = RestChecker.checkSupportedFormat("json", SUPPORTED_FORMATS);
        try {
            JSONArray users = new JSONArray();

            List<UserRHDataDTO> usersDTO = userDataService.getRhDataByStatus(true, 0, 0);
                if (usersDTO.size() > 0) {
                    for (UserRHDataDTO userDTO : usersDTO) {
                        JSONObject user = new JSONObject();
                        user.put("userId",userDTO.getUserId());
                        user.put("hierarchicalManager",userDTO.getHierarchicalManager()!=null ? userDTO.getHierarchicalManager() : "");
                        user.put("functionalManager",userDTO.getFunctionalManager()!=null ? userDTO.getFunctionalManager() : "");
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
