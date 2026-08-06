/********************************************************************************************************
 * File: ProfessorController.java
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

@Named("professorController")
@SessionScoped
public class ProfessorController implements Serializable, MyConstants {

    private static final long serialVersionUID = 1L;

    @Inject
    protected FacesContext facesContext;

    @Inject
    protected LoginBean loginBean;

    protected List<Professor> listOfProfessors;
    protected boolean adding;

    protected URI uri;
    protected Client client;
    protected WebTarget webTarget;

    public ProfessorController() {
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
        listOfProfessors = new ArrayList<>();
    }

    protected HttpAuthenticationFeature getAuthentication() {
        return HttpAuthenticationFeature.basic(
            loginBean.getUsername(),
            loginBean.getPassword()
        );
    }

    public List<Professor> getProfessors() {
        if (listOfProfessors == null) {
            listOfProfessors = new ArrayList<>();
        }

        return listOfProfessors;
    }

    public void setProfessors(
        List<Professor> listOfProfessors
    ) {
        this.listOfProfessors = listOfProfessors;
    }

    public void loadProfessors() {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(PROFESSOR_RESOURCE_NAME)
                .request()
                .get();

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                listOfProfessors = response.readEntity(
                    new GenericType<List<Professor>>() {
                    }
                );
            }
            else {
                listOfProfessors = new ArrayList<>();

                addErrorMessage(
                    "Unable to load professors. HTTP status: "
                        + response.getStatus()
                );
            }
        }
        catch (Exception e) {
            listOfProfessors = new ArrayList<>();

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

    public String editProfessor(Professor professor) {
        professor.setEditable(true);
        return null;
    }

    public String updateProfessor(
        Professor professor
    ) {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(
                    PROFESSOR_RESOURCE_NAME
                        + "/"
                        + professor.getId()
                )
                .request()
                .put(Entity.json(professor));

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                Professor updatedProfessor =
                    response.readEntity(
                        Professor.class
                    );

                updatedProfessor.setEditable(false);

                int index =
                    listOfProfessors.indexOf(
                        professor
                    );

                if (index >= 0) {
                    listOfProfessors.set(
                        index,
                        updatedProfessor
                    );
                }

                loadProfessors();
            }
            else {
                addErrorMessage(
                    "Unable to update professor. HTTP status: "
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

    public String cancelUpdate(
        Professor professor
    ) {
        professor.setEditable(false);
        loadProfessors();

        return null;
    }

    public String deleteProfessor(
        int professorId
    ) {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(
                    PROFESSOR_RESOURCE_NAME
                        + "/"
                        + professorId
                )
                .request()
                .delete();

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                loadProfessors();
            }
            else {
                addErrorMessage(
                    "Unable to delete professor. HTTP status: "
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

    public String addNewProfessor(
        Professor newProfessor
    ) {
        Response response = null;

        try {
            response = webTarget
                .register(getAuthentication())
                .path(PROFESSOR_RESOURCE_NAME)
                .request()
                .post(Entity.json(newProfessor));

            if (response.getStatus()
                == Response.Status.OK.getStatusCode()) {

                loadProfessors();
            }
            else {
                addErrorMessage(
                    "Unable to add professor. HTTP status: "
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

    public String refreshProfessorForm() {
        Iterator<FacesMessage> messages =
            facesContext.getMessages();

        while (messages.hasNext()) {
            messages.next();
            messages.remove();
        }

        loadProfessors();

        return null;
    }

    protected void addErrorMessage(
        String message
    ) {
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