/********************************************************************************************************
 * File:  ProfessorResource.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 *
 */
package com.algonquincollege.cst8277.rest.resource;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.PROFESSOR_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_PATH;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.algonquincollege.cst8277.ejb.ACMECollegeService;
import com.algonquincollege.cst8277.entity.Professor;

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
 * REST resource providing CRUD operations for Professor entities.
 */
@Path(PROFESSOR_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProfessorResource {

    private static final Logger LOG =
        LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    /**
     * Retrieves all professors.
     *
     * @return HTTP 200 with the professor list
     */
    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getProfessors() {
        LOG.debug("Retrieving all professors");

        List<Professor> professors =
            service.getAllProfessors();

        return Response.ok(professors).build();
    }

    /**
     * Retrieves one professor by ID.
     *
     * @param id professor ID
     * @return HTTP 200 if found, otherwise HTTP 404
     */
    @GET
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getProfessorById(
        @PathParam(RESOURCE_PATH_ID_ELEMENT) int id
    ) {
        LOG.debug(
            "Retrieving professor with ID {}",
            id
        );

        Professor professor =
            service.getProfessorById(id);

        if (professor == null) {
            return Response
                .status(Status.NOT_FOUND)
                .build();
        }

        return Response.ok(professor).build();
    }

    /**
     * Creates a professor.
     *
     * @param newProfessor professor data
     * @return HTTP 200 with the persisted professor
     */
    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addProfessor(
        Professor newProfessor
    ) {
        LOG.debug(
            "Creating professor {}",
            newProfessor
        );

        Professor persistedProfessor =
            service.persistProfessor(newProfessor);

        return Response
            .ok(persistedProfessor)
            .build();
    }

    /**
     * Updates a professor by ID.
     *
     * @param id professor ID
     * @param professorWithUpdates updated professor data
     * @return HTTP 200 if updated, otherwise HTTP 404
     */
    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateProfessorById(
        @PathParam(RESOURCE_PATH_ID_ELEMENT) int id,
        Professor professorWithUpdates
    ) {
        LOG.debug(
            "Updating professor with ID {}",
            id
        );

        Professor updatedProfessor =
            service.updateProfessorById(
                id,
                professorWithUpdates
            );

        if (updatedProfessor == null) {
            return Response
                .status(Status.NOT_FOUND)
                .build();
        }

        return Response
            .ok(updatedProfessor)
            .build();
    }

    /**
     * Deletes a professor by ID.
     *
     * @param id professor ID
     * @return HTTP 200 if deleted, otherwise HTTP 404
     */
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteProfessorById(
        @PathParam(RESOURCE_PATH_ID_ELEMENT) int id
    ) {
        LOG.debug(
            "Deleting professor with ID {}",
            id
        );

        Professor deletedProfessor =
            service.deleteProfessorById(id);

        if (deletedProfessor == null) {
            return Response
                .status(Status.NOT_FOUND)
                .build();
        }

        return Response
            .ok(deletedProfessor)
            .build();
    }
}