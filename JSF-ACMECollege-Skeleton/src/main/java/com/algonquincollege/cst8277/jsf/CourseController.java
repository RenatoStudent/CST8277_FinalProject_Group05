/********************************************************************************************************
 * File: CourseController.java
 * Course Materials CST 8277
 *
 * @author Teddy Yap
 *
 */
package com.algonquincollege.cst8277.jsf;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;

import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.rest.resource.MyObjectMapperProvider;
import com.algonquincollege.cst8277.utility.MyConstants;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

@Named("courseController")
@SessionScoped
public class CourseController implements Serializable, MyConstants {

    private static final long serialVersionUID = 1L;

    @Inject
    protected FacesContext facesContext;

    @Inject
    protected LoginBean loginBean;

    protected List<Course> listOfCourses;
    protected boolean adding;

    protected URI uri;
    protected Client client;
    protected WebTarget webTarget;

    public CourseController() {
        super();
    }

    @PostConstruct
    public void initialize() {
        uri = UriBuilder
            .fromUri(
                APPLICATION_CONTEXT_ROOT
                    + APPLICATION_API_VERSION
            )
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();

        client = ClientBuilder.newClient(
            new ClientConfig()
                .register(MyObjectMapperProvider.class)
                .register(new LoggingFeature())
        );

        webTarget = client.target(uri);
        listOfCourses = new ArrayList<>();
    }

    /**
     * Creates authentication using the current login values.
     */
    protected HttpAuthenticationFeature getAuthentication() {
        return HttpAuthenticationFeature.basic(
            loginBean.getUsername(),
            loginBean.getPassword()
        );
    }

    public List<Course> getCourses() {
        if (listOfCourses == null) {
            listOfCourses = new ArrayList<>();
        }

        return listOfCourses;
    }

    public void setCourses(List<Course> listOfCourses) {
        this.listOfCourses = listOfCourses;
    }

    public void loadCourses() {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(COURSE_RESOURCE_NAME)
                .request()
                .get();

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                listOfCourses = response.readEntity(
                    new GenericType<List<Course>>() {
                    }
                );
            }
            else {
                listOfCourses = new ArrayList<>();

                addErrorMessage(
                    "Unable to load courses. HTTP status: "
                        + response.getStatus()
                );
            }
        }
        catch (Exception e) {
            listOfCourses = new ArrayList<>();

            addErrorMessage(
                "Unable to connect to the REST application: "
                    + e.getMessage()
            );
        }
        finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public boolean isAdding() {
        return adding;
    }

    public void setAdding(boolean adding) {
        this.adding = adding;
    }

    public void toggleAdding() {
        setAdding(!isAdding());
    }

    public String editCourse(Course course) {
        course.setEditable(true);
        return null;
    }

    public String updateCourse(Course course) {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(
                    COURSE_RESOURCE_NAME
                        + "/"
                        + course.getId()
                )
                .request()
                .put(Entity.json(course));

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                Course updatedCourse =
                    response.readEntity(Course.class);

                updatedCourse.setEditable(false);

                int index =
                    listOfCourses.indexOf(course);

                if (index >= 0) {
                    listOfCourses.set(
                        index,
                        updatedCourse
                    );
                }

                loadCourses();
            }
            else {
                addErrorMessage(
                    "Unable to update course. HTTP status: "
                        + response.getStatus()
                );
            }
        }
        finally {
            if (response != null) {
                response.close();
            }
        }

        return null;
    }

    public String cancelUpdate(Course course) {
        course.setEditable(false);
        loadCourses();

        return null;
    }

    public String deleteCourse(int courseId) {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(
                    COURSE_RESOURCE_NAME
                        + "/"
                        + courseId
                )
                .request()
                .delete();

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                loadCourses();
            }
            else {
                addErrorMessage(
                    "Unable to delete course. HTTP status: "
                        + response.getStatus()
                );
            }
        }
        finally {
            if (response != null) {
                response.close();
            }
        }

        return null;
    }

    public String addNewCourse(Course newCourse) {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(COURSE_RESOURCE_NAME)
                .request()
                .post(Entity.json(newCourse));

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                loadCourses();
            }
            else {
                addErrorMessage(
                    "Unable to add course. HTTP status: "
                        + response.getStatus()
                );
            }
        }
        finally {
            if (response != null) {
                response.close();
            }
        }

        return null;
    }

    public String refreshCourseForm() {
        Iterator<FacesMessage> messages =
            facesContext.getMessages();

        while (messages.hasNext()) {
            messages.next();
            messages.remove();
        }

        loadCourses();

        return null;
    }

    protected void addErrorMessage(String message) {
        facesContext.addMessage(
            null,
            new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                message,
                message
            )
        );
    }
}