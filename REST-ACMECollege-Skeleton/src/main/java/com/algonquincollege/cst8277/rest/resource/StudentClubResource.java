/********************************************************************************************************
 * File:  StudentClubResource.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 *
 */
package com.algonquincollege.cst8277.rest.resource;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.CLUB_ID_PATH_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENTCLUB_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_ID_PATH_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.USER_ROLE;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.algonquincollege.cst8277.ejb.ACMECollegeService;
import com.algonquincollege.cst8277.entity.Student;
import com.algonquincollege.cst8277.entity.StudentClub;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * REST resource providing CRUD and membership operations for StudentClub
 * entities.
 */
@Path(STUDENTCLUB_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StudentClubResource {

    private static final Logger LOG =
        LogManager.getLogger();

    private static final String CLUB_MEMBERS_PATH =
        "/{" + CLUB_ID_PATH_ELEMENT + "}/student";

    private static final String CLUB_MEMBER_PATH =
        CLUB_MEMBERS_PATH
            + "/{" + STUDENT_ID_PATH_ELEMENT + "}";

    @EJB
    protected ACMECollegeService service;

    /**
     * Both administrators and normal users may retrieve the club list.
     *
     * @return HTTP 200 with all student clubs
     */
    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getStudentClubs() {
        LOG.debug("Retrieving all student clubs");

        List<StudentClub> clubs =
            service.getAllStudentClubs();

        return Response.ok(clubs).build();
    }

    /**
     * Both administrators and normal users may retrieve one club.
     *
     * @param id club ID
     * @return HTTP 200 if found, otherwise HTTP 404
     */
    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getStudentClubById(
        @PathParam(RESOURCE_PATH_ID_ELEMENT) int id
    ) {
        LOG.debug(
            "Retrieving student club with ID {}",
            id
        );

        StudentClub club =
            service.getStudentClubById(id);

        if (club == null) {
            return Response
                .status(Status.NOT_FOUND)
                .build();
        }

        return Response.ok(club).build();
    }

    /**
     * Retrieves the students registered in a particular club.
     *
     * Endpoint:
     * GET /studentclub/{clubId}/student
     *
     * @param clubId club ID
     * @return HTTP 200 with club members, otherwise HTTP 404
     */
    @GET
    @Path(CLUB_MEMBERS_PATH)
    @RolesAllowed({ADMIN_ROLE})
    public Response getStudentClubMembers(
        @PathParam(CLUB_ID_PATH_ELEMENT)
        int clubId
    ) {
        LOG.debug(
            "Retrieving members of student club {}",
            clubId
        );

        List<Student> members =
            service.getStudentClubMembers(clubId);

        if (members == null) {
            return Response
                .status(Status.NOT_FOUND)
                .build();
        }

        return Response.ok(members).build();
    }

    /**
     * Only an administrator may create a student club.
     *
     * @param newStudentClub club information
     * @return HTTP 200 with the persisted club
     */
    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addStudentClub(
        StudentClub newStudentClub
    ) {
        LOG.debug(
            "Creating student club {}",
            newStudentClub
        );

        StudentClub persistedClub =
            service.persistStudentClub(newStudentClub);

        return Response.ok(persistedClub).build();
    }

    /**
     * Registers a student as a member of a club.
     *
     * Endpoint:
     * POST /studentclub/{clubId}/student/{studentId}
     *
     * @param clubId club ID
     * @param studentId student ID
     * @return HTTP 200 if registered, otherwise HTTP 404
     */
    @POST
    @Path(CLUB_MEMBER_PATH)
    @RolesAllowed({ADMIN_ROLE})
    public Response addStudentToClub(
        @PathParam(CLUB_ID_PATH_ELEMENT)
        int clubId,

        @PathParam(STUDENT_ID_PATH_ELEMENT)
        int studentId
    ) {
        LOG.debug(
            "Adding student {} to club {}",
            studentId,
            clubId
        );

        StudentClub updatedClub =
            service.addStudentToClub(
                studentId,
                clubId
            );

        if (updatedClub == null) {
            return Response
                .status(Status.NOT_FOUND)
                .build();
        }

        return Response.ok(updatedClub).build();
    }

    /**
     * Only an administrator may update a student club.
     *
     * @param id club ID
     * @param studentClubWithUpdates updated club information
     * @return HTTP 200 if updated, otherwise HTTP 404
     */
    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateStudentClubById(
        @PathParam(RESOURCE_PATH_ID_ELEMENT) int id,
        StudentClub studentClubWithUpdates
    ) {
        LOG.debug(
            "Updating student club with ID {}",
            id
        );

        StudentClub updatedClub =
            service.updateStudentClubById(
                id,
                studentClubWithUpdates
            );

        if (updatedClub == null) {
            return Response
                .status(Status.NOT_FOUND)
                .build();
        }

        return Response.ok(updatedClub).build();
    }

    /**
     * Removes a student from a club.
     *
     * Endpoint:
     * DELETE /studentclub/{clubId}/student/{studentId}
     *
     * @param clubId club ID
     * @param studentId student ID
     * @return HTTP 200 if removed, otherwise HTTP 404
     */
    @DELETE
    @Path(CLUB_MEMBER_PATH)
    @RolesAllowed({ADMIN_ROLE})
    public Response removeStudentFromClub(
        @PathParam(CLUB_ID_PATH_ELEMENT)
        int clubId,

        @PathParam(STUDENT_ID_PATH_ELEMENT)
        int studentId
    ) {
        LOG.debug(
            "Removing student {} from club {}",
            studentId,
            clubId
        );

        StudentClub updatedClub =
            service.removeStudentFromClub(
                studentId,
                clubId
            );

        if (updatedClub == null) {
            return Response
                .status(Status.NOT_FOUND)
                .build();
        }

        return Response.ok(updatedClub).build();
    }

    /**
     * Only an administrator may delete a student club.
     *
     * @param id club ID
     * @return HTTP 200 if deleted, otherwise HTTP 404
     */
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteStudentClubById(
        @PathParam(RESOURCE_PATH_ID_ELEMENT) int id
    ) {
        LOG.debug(
            "Deleting student club with ID {}",
            id
        );

        StudentClub deletedClub =
            service.deleteStudentClubById(id);

        if (deletedClub == null) {
            return Response
                .status(Status.NOT_FOUND)
                .build();
        }

        return Response.ok(deletedClub).build();
    }
}