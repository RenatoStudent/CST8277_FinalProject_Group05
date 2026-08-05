/********************************************************************************************************
 * File:  StudentResource.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman
 *
 */
package com.algonquincollege.cst8277.rest.resource;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.PROGRAM_RESOURCE_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.USER_ROLE;

import java.security.Principal;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import com.algonquincollege.cst8277.ejb.ACMECollegeService;
import com.algonquincollege.cst8277.entity.SecurityUser;
import com.algonquincollege.cst8277.entity.Student;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path(STUDENT_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StudentResource {

    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    @Inject
    protected SecurityContext sc;

    /**
     * Only an ADMIN_ROLE user can retrieve all students.
     *
     * @return response containing all students
     */
    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getStudents() {
        LOG.debug("Retrieving all students");

        List<Student> students = service.getAllStudents();

        return Response.ok(students).build();
    }

    /**
     * An ADMIN_ROLE user can retrieve any student.
     * A USER_ROLE user can retrieve only the Student connected to their
     * SecurityUser account.
     *
     * @param id student ID
     * @return response containing the student
     */
    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getStudentById(
        @PathParam(RESOURCE_PATH_ID_ELEMENT) int id
    ) {
        LOG.debug("Trying to retrieve student with ID {}", id);

        if (sc.isCallerInRole(ADMIN_ROLE)) {
            Student student = service.getStudentById(id);

            return Response
                .status(student == null ? Status.NOT_FOUND : Status.OK)
                .entity(student)
                .build();
        }

        Principal callerPrincipal = sc.getCallerPrincipal();

        if (!(callerPrincipal instanceof WrappingCallerPrincipal)) {
            throw new ForbiddenException(
                "Unable to resolve the authenticated SecurityUser"
            );
        }

        WrappingCallerPrincipal wrappingPrincipal =
            (WrappingCallerPrincipal) callerPrincipal;

        Object wrappedPrincipal = wrappingPrincipal.getWrapped();

        if (!(wrappedPrincipal instanceof SecurityUser)) {
            throw new ForbiddenException(
                "Authenticated principal is not a SecurityUser"
            );
        }

        SecurityUser securityUser =
            (SecurityUser) wrappedPrincipal;

        Student student = securityUser.getStudent();

        if (student == null || student.getId() != id) {
            throw new ForbiddenException(
                "User trying to access a student resource it does not own"
            );
        }

        return Response
            .status(Status.OK)
            .entity(student)
            .build();
    }

    /**
     * Only an ADMIN_ROLE user can add a student.
     *
     * @param newStudent student to add
     * @return response containing the newly created student
     */
    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addStudent(Student newStudent) {
        Student persistedStudent =
            service.persistStudent(newStudent);

        service.buildUserForNewStudent(persistedStudent);

        return Response.ok(persistedStudent).build();
    }

    /**
     * Only an ADMIN_ROLE user can update a student.
     *
     * @param id student ID
     * @param studentWithUpdates updated student information
     * @return response containing the updated student
     */
    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateStudentById(
        @PathParam(RESOURCE_PATH_ID_ELEMENT) int id,
        Student studentWithUpdates
    ) {
        Student updatedStudent =
            service.updateStudentById(id, studentWithUpdates);

        if (updatedStudent == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(updatedStudent).build();
    }

    /**
     * Only an ADMIN_ROLE user can delete a student.
     *
     * @param id student ID
     * @return response containing the deleted student
     */
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteStudentById(
        @PathParam(RESOURCE_PATH_ID_ELEMENT) int id
    ) {
        Student deletedStudent =
            service.deleteStudentById(id);

        if (deletedStudent == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(deletedStudent).build();
    }

    /**
     * ADMIN_ROLE and USER_ROLE users can retrieve program names.
     *
     * @return response containing available programs
     */
    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(PROGRAM_RESOURCE_PATH)
    public Response getPrograms() {
        List<String> programs = service.getAllPrograms();

        return Response.ok(programs).build();
    }
}