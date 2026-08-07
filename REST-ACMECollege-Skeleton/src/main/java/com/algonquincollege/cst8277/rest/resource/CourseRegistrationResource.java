/********************************************************************************************************
 * File:  CourseRegistrationResource.java Course Materials CST 8277
 *
 * @author Scott Van Dijk
 * @author Renato Perocchio
 *
 */
package com.algonquincollege.cst8277.rest.resource;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_ID_PATH_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSEREGISTRATION_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.LETTERGRADE_RESOURCE_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_COURSE_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_ID_PATH_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.USER_ROLE;

import java.security.Principal;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import com.algonquincollege.cst8277.ejb.ACMECollegeService;
import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.entity.Professor;
import com.algonquincollege.cst8277.entity.SecurityUser;

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

/**
 * REST resource for course registration operations.
 */
@Path(COURSEREGISTRATION_RESOURCE_NAME)
@Produces(MediaType.APPLICATION_JSON)
public class CourseRegistrationResource {

    private static final Logger LOG =
        LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    @Inject
    protected SecurityContext sc;

    /**
     * An ADMIN_ROLE user retrieves every registration.
     * A USER_ROLE user retrieves only their own registrations.
     *
     * @return HTTP 200 with the applicable registrations
     */
    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getAllCourseRegistrations() {
        LOG.debug(
            "Retrieving all course registrations"
        );

        if (sc.isCallerInRole(ADMIN_ROLE)) {
            List<CourseRegistration> registrations =
                service.getAllCourseRegistrations();

            return Response
                .ok(registrations)
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

        if (securityUser.getStudent() == null) {
            throw new ForbiddenException(
                "User trying to access course registrations without a linked student"
            );
        }

        int studentId = securityUser.getStudent().getId();

        List<CourseRegistration> ownRegistrations =
            service.getCourseRegistrationsByStudentId(studentId);

        return Response
            .ok(ownRegistrations)
            .build();
    }

    /**
     * Retrieves all valid letter grades.
     *
     * Endpoint:
     * GET /courseregistration/lettergrade
     *
     * @return HTTP 200 with valid letter grades
     */
    @GET
    @Path(LETTERGRADE_RESOURCE_PATH)
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getAllLetterGrades() {
        LOG.debug(
            "Retrieving all valid letter grades"
        );

        List<String> grades =
            service.getAllLetterGrades();

        return Response
            .ok(grades)
            .build();
    }

    /**
     * Retrieves one registration using student and course IDs.
     *
     * Endpoint:
     * GET /courseregistration/student/{studentId}/course/{courseId}
     *
     * @param studentId student ID
     * @param courseId course ID
     * @return HTTP 200 if found, otherwise HTTP 404
     */
    @GET
    @Path(STUDENT_COURSE_PATH)
    @RolesAllowed({ADMIN_ROLE})
    public Response getCourseRegistration(
        @PathParam(STUDENT_ID_PATH_ELEMENT)
        int studentId,

        @PathParam(COURSE_ID_PATH_ELEMENT)
        int courseId
    ) {
        LOG.debug(
            "Retrieving course registration for student {} and course {}",
            studentId,
            courseId
        );

        CourseRegistration registration =
            service.getCourseRegistration(
                studentId,
                courseId
            );

        if (registration == null) {
            return Response
                .status(Status.NOT_FOUND)
                .build();
        }

        return Response
            .ok(registration)
            .build();
    }

    /**
     * Creates a new course registration.
     *
     * The request body must contain a student ID, course ID, year,
     * and semester.
     *
     * Endpoint:
     * POST /courseregistration
     *
     * @param newRegistration registration information
     * @return HTTP 200 when created, otherwise HTTP 400
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({ADMIN_ROLE})
    public Response createCourseRegistration(
        CourseRegistration newRegistration
    ) {
        LOG.debug(
            "Creating course registration {}",
            newRegistration
        );

        if (newRegistration == null
            || newRegistration.getStudent() == null
            || newRegistration.getCourse() == null) {

            return Response
                .status(Status.BAD_REQUEST)
                .build();
        }

        int studentId =
            newRegistration.getStudent().getId();

        int courseId =
            newRegistration.getCourse().getId();

        if (studentId <= 0 || courseId <= 0) {
            return Response
                .status(Status.BAD_REQUEST)
                .build();
        }

        CourseRegistration persistedRegistration =
            service.persistCourseRegistration(
                studentId,
                courseId,
                newRegistration
            );

        if (persistedRegistration == null) {
            return Response
                .status(Status.BAD_REQUEST)
                .build();
        }

        return Response
            .ok(persistedRegistration)
            .build();
    }

    /**
     * Updates the year and semester of a registration.
     *
     * Endpoint:
     * PUT /courseregistration/student/{studentId}/course/{courseId}
     *
     * @param studentId student ID
     * @param courseId course ID
     * @param registrationWithUpdates updated year and semester
     * @return HTTP 200 if updated, otherwise HTTP 404
     */
    @PUT
    @Path(STUDENT_COURSE_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({ADMIN_ROLE})
    public Response updateCourseRegistration(
        @PathParam(STUDENT_ID_PATH_ELEMENT)
        int studentId,

        @PathParam(COURSE_ID_PATH_ELEMENT)
        int courseId,

        CourseRegistration registrationWithUpdates
    ) {
        LOG.debug(
            "Updating course registration for student {} and course {}",
            studentId,
            courseId
        );

        if (registrationWithUpdates == null) {
            return Response
                .status(Status.BAD_REQUEST)
                .build();
        }

        CourseRegistration updatedRegistration =
            service.updateCourseRegistration(
                studentId,
                courseId,
                registrationWithUpdates
            );

        if (updatedRegistration == null) {
            return Response
                .status(Status.NOT_FOUND)
                .build();
        }

        return Response
            .ok(updatedRegistration)
            .build();
    }

    /**
     * Assigns a professor to a registration.
     *
     * Endpoint:
     * PUT /courseregistration/student/{studentId}/course/{courseId}/professor
     *
     * Request body example:
     * {
     *   "id": 1
     * }
     *
     * @param studentId student ID
     * @param courseId course ID
     * @param professor professor containing the professor ID
     * @return HTTP 200 if assigned, otherwise HTTP 404
     */
    @PUT
    @Path(STUDENT_COURSE_PATH + "/professor")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({ADMIN_ROLE})
    public Response assignProfessor(
        @PathParam(STUDENT_ID_PATH_ELEMENT)
        int studentId,

        @PathParam(COURSE_ID_PATH_ELEMENT)
        int courseId,

        Professor professor
    ) {
        LOG.debug(
            "Assigning professor to student {} and course {}",
            studentId,
            courseId
        );

        if (professor == null
            || professor.getId() <= 0) {

            return Response
                .status(Status.BAD_REQUEST)
                .build();
        }

        CourseRegistration updatedRegistration =
            service.assignProfessor(
                studentId,
                courseId,
                professor.getId()
            );

        if (updatedRegistration == null) {
            return Response
                .status(Status.NOT_FOUND)
                .build();
        }

        return Response
            .ok(updatedRegistration)
            .build();
    }

    /**
     * Assigns a letter grade to a registration.
     *
     * Endpoint:
     * PUT /courseregistration/student/{studentId}/course/{courseId}/grade
     *
     * Request body example:
     * A+
     *
     * Content-Type:
     * text/plain
     *
     * @param studentId student ID
     * @param courseId course ID
     * @param letterGrade new letter grade
     * @return HTTP 200 if assigned, otherwise HTTP 404
     */
    @PUT
    @Path(STUDENT_COURSE_PATH + "/grade")
    @Consumes(MediaType.TEXT_PLAIN)
    @RolesAllowed({ADMIN_ROLE})
    public Response assignLetterGrade(
        @PathParam(STUDENT_ID_PATH_ELEMENT)
        int studentId,

        @PathParam(COURSE_ID_PATH_ELEMENT)
        int courseId,

        String letterGrade
    ) {
        LOG.debug(
            "Assigning letter grade to student {} and course {}",
            studentId,
            courseId
        );

        if (letterGrade == null
            || letterGrade.isBlank()) {

            return Response
                .status(Status.BAD_REQUEST)
                .build();
        }

        CourseRegistration updatedRegistration =
            service.assignLetterGrade(
                studentId,
                courseId,
                letterGrade.trim()
            );

        if (updatedRegistration == null) {
            return Response
                .status(Status.NOT_FOUND)
                .build();
        }

        return Response
            .ok(updatedRegistration)
            .build();
    }

    /**
     * Deletes a course registration.
     *
     * Endpoint:
     * DELETE /courseregistration/student/{studentId}/course/{courseId}
     *
     * @param studentId student ID
     * @param courseId course ID
     * @return HTTP 200 if deleted, otherwise HTTP 404
     */
    @DELETE
    @Path(STUDENT_COURSE_PATH)
    @RolesAllowed({ADMIN_ROLE})
    public Response deleteCourseRegistration(
        @PathParam(STUDENT_ID_PATH_ELEMENT)
        int studentId,

        @PathParam(COURSE_ID_PATH_ELEMENT)
        int courseId
    ) {
        LOG.debug(
            "Deleting course registration for student {} and course {}",
            studentId,
            courseId
        );

        CourseRegistration deletedRegistration =
            service.deleteCourseRegistration(
                studentId,
                courseId
            );

        if (deletedRegistration == null) {
            return Response
                .status(Status.NOT_FOUND)
                .build();
        }

        return Response
            .ok(deletedRegistration)
            .build();
    }
}