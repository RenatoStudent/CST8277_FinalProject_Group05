/********************************************************************************************************
 * File: CourseResource.java
 * Course Materials CST 8277
 *
 * @author Scott Vandijk
 */
package com.algonquincollege.cst8277.rest.resource;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.USER_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_PATH;

import java.util.List;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.algonquincollege.cst8277.ejb.ACMECollegeService;
import com.algonquincollege.cst8277.entity.Course;

@Path(COURSE_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CourseResource {

    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    //TODO CR01 - Add REST endpoint to retrieve all courses.
    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getCourses() {
        LOG.debug("retrieving all courses");

        List<Course> courses = service.getAllCourses();

        return Response.ok(courses).build();
    }

    //TODO CR02 - Add REST endpoint to retrieve a specific course.
    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getCourseById(
        @PathParam(RESOURCE_PATH_ID_ELEMENT) int id
    ) {
        LOG.debug("retrieving course with id {}", id);

        Course course = service.getCourseById(id);

        return Response
            .status(course == null ? Status.NOT_FOUND : Status.OK)
            .entity(course)
            .build();
    }

    //TODO CR03 - Add REST endpoint to create a course.
    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addCourse(Course newCourse) {
        LOG.debug("adding a new course");

        Course persistedCourse = service.persistCourse(newCourse);

        return Response.ok(persistedCourse).build();
    }

    //TODO CR04 - Add REST endpoint to update a course.
    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateCourseById(
        @PathParam(RESOURCE_PATH_ID_ELEMENT) int id,
        Course courseWithUpdates
    ) {
        LOG.debug("updating course with id {}", id);

        Course updatedCourse =
            service.updateCourseById(id, courseWithUpdates);

        return Response
            .status(updatedCourse == null ? Status.NOT_FOUND : Status.OK)
            .entity(updatedCourse)
            .build();
    }

    //TODO CR05 - Add REST endpoint to delete a course.
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteCourseById(
        @PathParam(RESOURCE_PATH_ID_ELEMENT) int id
    ) {
        LOG.debug("deleting course with id {}", id);

        Course deletedCourse = service.deleteCourseById(id);

        return Response
            .status(deletedCourse == null ? Status.NOT_FOUND : Status.OK)
            .entity(deletedCourse)
            .build();
    }
}