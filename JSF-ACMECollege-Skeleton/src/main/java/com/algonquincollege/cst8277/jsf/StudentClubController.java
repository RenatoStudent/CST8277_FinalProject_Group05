/********************************************************************************************************
 * File: StudentClubController.java
 * Course Materials CST 8277
 *
 * @author Scott Van Dijk
 * @author Renato Perocchio
 *
 */
package com.algonquincollege.cst8277.jsf;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;

import com.algonquincollege.cst8277.entity.Student;
import com.algonquincollege.cst8277.entity.StudentClub;
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

@Named("studentClubController")
@SessionScoped
public class StudentClubController implements Serializable, MyConstants {

    private static final long serialVersionUID = 1L;

    @Inject
    protected FacesContext facesContext;

    @Inject
    protected LoginBean loginBean;

    @Inject
    protected StudentController studentController;

    protected List<StudentClub> listOfStudentClubs;
    protected Map<Integer, List<Student>> clubMembers;
    protected boolean adding;

    protected Integer selectedStudentId;
    protected Integer selectedClubId;

    protected URI uri;
    protected Client client;
    protected WebTarget webTarget;

    public StudentClubController() {
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
        listOfStudentClubs = new ArrayList<>();
        clubMembers = new HashMap<>();

        studentController.loadStudents();
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

    public List<StudentClub> getStudentClubs() {
        if (listOfStudentClubs == null) {
            listOfStudentClubs = new ArrayList<>();
        }

        return listOfStudentClubs;
    }

    public void setStudentClubs(List<StudentClub> listOfStudentClubs) {
        this.listOfStudentClubs = listOfStudentClubs;
    }

    public void loadStudentClubs() {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(STUDENTCLUB_RESOURCE_NAME)
                .request()
                .get();

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                listOfStudentClubs = response.readEntity(
                    new GenericType<List<StudentClub>>() {
                    }
                );
            }
            else {
                listOfStudentClubs = new ArrayList<>();

                addErrorMessage(
                    "Unable to load student clubs. HTTP status: "
                        + response.getStatus()
                );
            }
        }
        catch (Exception e) {
            listOfStudentClubs = new ArrayList<>();

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

    public Map<Integer, List<Student>> getClubMembers() {
        if (clubMembers == null) {
            clubMembers = new HashMap<>();
        }

        return clubMembers;
    }

    public void loadAllClubMembers() {
        for (StudentClub studentClub : getStudentClubs()) {
            loadClubMembers(studentClub.getId());
        }
    }

    public void loadClubMembers(int clubId) {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(
                    STUDENTCLUB_RESOURCE_NAME
                        + "/"
                        + clubId
                        + "/student"
                )
                .request()
                .get();

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                List<Student> members = response.readEntity(
                    new GenericType<List<Student>>() {
                    }
                );

                getClubMembers().put(clubId, members);
            }
            else {
                getClubMembers().put(clubId, new ArrayList<>());

                addErrorMessage(
                    "Unable to load club members. HTTP status: "
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

    public String registerStudentToClub(int studentId, int clubId) {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(
                    STUDENTCLUB_RESOURCE_NAME
                        + "/"
                        + clubId
                        + "/student/"
                        + studentId
                )
                .request()
                .post(Entity.json(null));

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                loadClubMembers(clubId);
            }
            else {
                addErrorMessage(
                    "Unable to register student to club. HTTP status: "
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

    public String removeStudentFromClub(int studentId, int clubId) {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(
                    STUDENTCLUB_RESOURCE_NAME
                        + "/"
                        + clubId
                        + "/student/"
                        + studentId
                )
                .request()
                .delete();

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                loadClubMembers(clubId);
            }
            else {
                addErrorMessage(
                    "Unable to remove student from club. HTTP status: "
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

    public boolean isAdding() {
        return adding;
    }

    public void setAdding(boolean adding) {
        this.adding = adding;
    }

    public void toggleAdding() {
        setAdding(!isAdding());
    }

    public Integer getSelectedStudentId() {
        return selectedStudentId;
    }

    public void setSelectedStudentId(Integer selectedStudentId) {
        this.selectedStudentId = selectedStudentId;
    }

    public Integer getSelectedClubId() {
        return selectedClubId;
    }

    public void setSelectedClubId(Integer selectedClubId) {
        this.selectedClubId = selectedClubId;
    }

    public String addNewMembership() {
        if (selectedStudentId == null || selectedClubId == null) {
            addErrorMessage("Please select both a student and a club.");
            return null;
        }

        return registerStudentToClub(selectedStudentId, selectedClubId);
    }

    public String editStudentClub(StudentClub studentClub) {
        studentClub.setEditable(true);
        return null;
    }

    public String updateStudentClub(StudentClub studentClub) {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(
                    STUDENTCLUB_RESOURCE_NAME
                        + "/"
                        + studentClub.getId()
                )
                .request()
                .put(Entity.json(studentClub));

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                loadStudentClubs();
            }
            else {
                addErrorMessage(
                    "Unable to update student club. HTTP status: "
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

    public String cancelUpdate(StudentClub studentClub) {
        studentClub.setEditable(false);
        loadStudentClubs();

        return null;
    }

    public String deleteStudentClub(int studentClubId) {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(
                    STUDENTCLUB_RESOURCE_NAME
                        + "/"
                        + studentClubId
                )
                .request()
                .delete();

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                loadStudentClubs();
            }
            else {
                addErrorMessage(
                    "Unable to delete student club. HTTP status: "
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

    public String addNewStudentClub(StudentClub newStudentClub) {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(STUDENTCLUB_RESOURCE_NAME)
                .request()
                .post(Entity.json(newStudentClub));

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                loadStudentClubs();
            }
            else {
                addErrorMessage(
                    "Unable to add student club. HTTP status: "
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

    public String refreshClubMembershipForm() {
        Iterator<FacesMessage> messages =
            facesContext.getMessages();

        while (messages.hasNext()) {
            messages.next();
            messages.remove();
        }

        loadStudentClubs();
        loadAllClubMembers();

        return null;
    }

    public String refreshStudentClubForm() {
        Iterator<FacesMessage> messages =
            facesContext.getMessages();

        while (messages.hasNext()) {
            messages.next();
            messages.remove();
        }

        loadStudentClubs();

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