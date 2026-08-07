/********************************************************************************************************
 * File: CourseRegistrationController.java
 * Course Materials CST 8277
 *
 * @author Renato Perocchio
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

import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.entity.Professor;
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

@Named("courseRegistrationController")
@SessionScoped
public class CourseRegistrationController implements Serializable, MyConstants {

    private static final long serialVersionUID = 1L;

    @Inject
    protected FacesContext facesContext;

    @Inject
    protected LoginBean loginBean;

    @Inject
    protected StudentController studentController;

    @Inject
    protected CourseController courseController;

    @Inject
    protected ProfessorController professorController;

    protected List<CourseRegistration> listOfCourseRegistrations;
    protected List<String> listOfLetterGrades;
    protected boolean adding;

    protected URI uri;
    protected Client client;
    protected WebTarget webTarget;

    public CourseRegistrationController() {
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
        listOfCourseRegistrations = new ArrayList<>();
        listOfLetterGrades = new ArrayList<>();

        // Ensure the student/course/professor dropdowns have data
        // regardless of which panel the admin visited first.
        studentController.loadStudents();
        courseController.loadCourses();
        professorController.loadProfessors();
        loadLetterGrades();
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

    public List<CourseRegistration> getCourseRegistrations() {
        if (listOfCourseRegistrations == null) {
            listOfCourseRegistrations = new ArrayList<>();
        }

        return listOfCourseRegistrations;
    }

    public void setCourseRegistrations(
        List<CourseRegistration> listOfCourseRegistrations
    ) {
        this.listOfCourseRegistrations = listOfCourseRegistrations;
    }

    public void loadCourseRegistrations() {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(COURSEREGISTRATION_RESOURCE_NAME)
                .request()
                .get();

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                listOfCourseRegistrations = response.readEntity(
                    new GenericType<List<CourseRegistration>>() {
                    }
                );
            }
            else {
                listOfCourseRegistrations = new ArrayList<>();

                addErrorMessage(
                    "Unable to load course registrations. HTTP status: "
                        + response.getStatus()
                );
            }
        }
        catch (Exception e) {
            listOfCourseRegistrations = new ArrayList<>();

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

    public List<String> getLetterGrades() {
        if (listOfLetterGrades == null) {
            listOfLetterGrades = new ArrayList<>();
        }

        return listOfLetterGrades;
    }

    public void loadLetterGrades() {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(COURSEREGISTRATION_RESOURCE_NAME + "/lettergrade")
                .request()
                .get();

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                listOfLetterGrades = response.readEntity(
                    new GenericType<List<String>>() {
                    }
                );
            }
            else {
                listOfLetterGrades = new ArrayList<>();

                addErrorMessage(
                    "Unable to load letter grades. HTTP status: "
                        + response.getStatus()
                );
            }
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

    public String addNewCourseRegistration(
        CourseRegistration newCourseRegistration
    ) {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(COURSEREGISTRATION_RESOURCE_NAME)
                .request()
                .post(Entity.json(newCourseRegistration));

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                loadCourseRegistrations();
            }
            else {
                addErrorMessage(
                    "Unable to add course registration. HTTP status: "
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

    public String deleteCourseRegistration(
        int studentId,
        int courseId
    ) {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(
                    COURSEREGISTRATION_RESOURCE_NAME
                        + "/student/"
                        + studentId
                        + "/course/"
                        + courseId
                )
                .request()
                .delete();

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                loadCourseRegistrations();
            }
            else {
                addErrorMessage(
                    "Unable to delete course registration. HTTP status: "
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

    public String assignProfessor(
        int studentId,
        int courseId,
        int professorId
    ) {
        Response response = null;

        Professor professor = new Professor();
        professor.setId(professorId);

        try {
            response = webTarget
                .register(getAuthentication())
                .path(
                    COURSEREGISTRATION_RESOURCE_NAME
                        + "/student/"
                        + studentId
                        + "/course/"
                        + courseId
                        + "/professor"
                )
                .request()
                .put(Entity.json(professor));

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                loadCourseRegistrations();
            }
            else {
                addErrorMessage(
                    "Unable to assign professor. HTTP status: "
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

    public String assignLetterGrade(
        int studentId,
        int courseId,
        String letterGrade
    ) {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(
                    COURSEREGISTRATION_RESOURCE_NAME
                        + "/student/"
                        + studentId
                        + "/course/"
                        + courseId
                        + "/grade"
                )
                .request()
                .put(Entity.text(letterGrade));

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                loadCourseRegistrations();
            }
            else {
                addErrorMessage(
                    "Unable to assign letter grade. HTTP status: "
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

    public String refreshCourseRegistrationForm() {
        Iterator<FacesMessage> messages =
            facesContext.getMessages();

        while (messages.hasNext()) {
            messages.next();
            messages.remove();
        }

        loadCourseRegistrations();

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