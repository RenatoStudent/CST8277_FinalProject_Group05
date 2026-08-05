/********************************************************************************************************
 * File:  TestACMECollegeSystem.java
 * Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Mike Norman
 *
 */
package acmecollege;

import static com.algonquincollege.cst8277.utility.MyConstants.APPLICATION_API_VERSION;
import static com.algonquincollege.cst8277.utility.MyConstants.APPLICATION_CONTEXT_ROOT;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_ADMIN_USER;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_ADMIN_USER_PASSWORD;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static com.algonquincollege.cst8277.utility.MyConstants.PROFESSOR_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENTCLUB_RESOURCE_NAME;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.algonquincollege.cst8277.entity.Academic;
import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.entity.Professor;
import com.algonquincollege.cst8277.entity.Student;
import com.algonquincollege.cst8277.entity.StudentClub;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

@SuppressWarnings("unused")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestACMECollegeSystem {

    private static final Class<?> _thisClaz =
        MethodHandles.lookup().lookupClass();

    private static final Logger logger =
        LogManager.getLogger(_thisClaz);

    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;

    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;

    protected WebTarget webTarget;

    static int createdCourseId;
    static int createdProfessorId;
    static int createdStudentClubId;

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        logger.debug("oneTimeSetUp");

        uri = UriBuilder
            .fromUri(
                APPLICATION_CONTEXT_ROOT
                    + APPLICATION_API_VERSION
            )
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();

        adminAuth = HttpAuthenticationFeature.basic(
            DEFAULT_ADMIN_USER,
            DEFAULT_ADMIN_USER_PASSWORD
        );

        userAuth = HttpAuthenticationFeature.basic(
            DEFAULT_USER,
            DEFAULT_USER_PASSWORD
        );
    }

    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder
            .newClient()
            .register(MyObjectMapperProvider.class)
            .register(new LoggingFeature());

        webTarget = client.target(uri);
    }

    /**
     * Confirms an ADMIN_ROLE account can retrieve all students.
     */
    @Test
    public void test01_get_all_students_with_adminrole()
        throws JsonMappingException, JsonProcessingException {

        Response response = webTarget
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .get();

        assertThat(response.getStatus(), is(200));

        List<Student> students = response.readEntity(
            new GenericType<List<Student>>() {
            }
        );

        assertThat(students, is(not(empty())));
        assertThat(students, hasSize(2));
    }

    /**
     * Confirms a USER_ROLE account cannot list all students.
     */
    @Test
    public void test02_get_all_students_with_userrole_should_fail() {
        Response response = webTarget
            .register(userAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .get();

        assertThat(response.getStatus(), is(403));
    }

    /**
     * Confirms a USER_ROLE account can retrieve its own linked student.
     */
    @Test
    public void test03_get_own_student_by_id_as_user() {
        Response response = webTarget
            .register(userAuth)
            .path(STUDENT_RESOURCE_NAME + "/1")
            .request()
            .get();

        assertThat(response.getStatus(), is(200));

        Student student =
            response.readEntity(Student.class);

        assertThat(student.getId(), is(1));
    }

    /**
     * Confirms a USER_ROLE account cannot retrieve another student.
     */
    @Test
    public void test04_get_other_student_by_id_as_user_should_fail() {
        Response response = webTarget
            .register(userAuth)
            .path(STUDENT_RESOURCE_NAME + "/2")
            .request()
            .get();

        assertThat(response.getStatus(), is(403));
    }

    /**
     * Confirms an ADMIN_ROLE account can retrieve all courses.
     */
    @Test
    public void test05_get_all_courses_as_admin() {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME)
            .request()
            .get();

        assertThat(response.getStatus(), is(200));

        List<Course> courses = response.readEntity(
            new GenericType<List<Course>>() {
            }
        );

        assertThat(courses, is(not(empty())));
        assertThat(courses, hasSize(2));
    }

    /**
     * Confirms a USER_ROLE account can retrieve all courses.
     */
    @Test
    public void test06_get_all_courses_as_user() {
        Response response = webTarget
            .register(userAuth)
            .path(COURSE_RESOURCE_NAME)
            .request()
            .get();

        assertThat(response.getStatus(), is(200));

        List<Course> courses = response.readEntity(
            new GenericType<List<Course>>() {
            }
        );

        assertThat(courses, hasSize(2));
    }

    /**
     * Confirms an ADMIN_ROLE account can create a course.
     */
    @Test
    public void test07_add_course_as_admin() {
        Course newCourse = new Course();

        newCourse.setCourseCode("CST9999");
        newCourse.setCourseTitle("Test Course");
        newCourse.setCreditUnits(3);
        newCourse.setOnline((short) 0);

        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME)
            .request()
            .post(Entity.json(newCourse));

        assertThat(response.getStatus(), is(200));

        Course persistedCourse =
            response.readEntity(Course.class);

        assertThat(
            persistedCourse.getId(),
            is(not(0))
        );

        createdCourseId = persistedCourse.getId();
    }

    /**
     * Confirms a USER_ROLE account cannot create a course.
     */
    @Test
    public void test08_add_course_as_user_should_fail() {
        Course newCourse = new Course();

        newCourse.setCourseCode("CST0000");
        newCourse.setCourseTitle("Should Fail");
        newCourse.setCreditUnits(3);
        newCourse.setOnline((short) 0);

        Response response = webTarget
            .register(userAuth)
            .path(COURSE_RESOURCE_NAME)
            .request()
            .post(Entity.json(newCourse));

        assertThat(response.getStatus(), is(403));
    }

    /**
     * Confirms an ADMIN_ROLE account can update a course.
     */
    @Test
    public void test09_update_course_as_admin() {
        Course courseWithUpdates = new Course();

        courseWithUpdates.setCourseCode("CST9999");
        courseWithUpdates.setCourseTitle(
            "Updated Test Course"
        );
        courseWithUpdates.setCreditUnits(4);
        courseWithUpdates.setOnline((short) 1);

        Response response = webTarget
            .register(adminAuth)
            .path(
                COURSE_RESOURCE_NAME
                    + "/"
                    + createdCourseId
            )
            .request()
            .put(Entity.json(courseWithUpdates));

        assertThat(response.getStatus(), is(200));

        Course updatedCourse =
            response.readEntity(Course.class);

        assertThat(
            updatedCourse.getCourseTitle(),
            is("Updated Test Course")
        );
    }

    /**
     * Confirms a nonexistent course returns HTTP 404.
     */
    @Test
    public void test10_get_course_by_id_not_found_should_return_404() {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME + "/999999")
            .request()
            .get();

        assertThat(response.getStatus(), is(404));
    }

    /**
     * Confirms an ADMIN_ROLE account can delete a course.
     */
    @Test
    public void test11_delete_course_as_admin() {
        Response response = webTarget
            .register(adminAuth)
            .path(
                COURSE_RESOURCE_NAME
                    + "/"
                    + createdCourseId
            )
            .request()
            .delete();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * Confirms the API does not return XML.
     */
    @Test
    public void test12_response_media_type_is_not_xml() {
        Response response = webTarget
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .get();

        assertThat(
            response.getMediaType(),
            is(not(MediaType.APPLICATION_XML_TYPE))
        );
    }

    /**
     * Confirms an ADMIN_ROLE account can list professors.
     */
    @Test
    public void test13_get_all_professors_as_admin() {
        Response response = webTarget
            .register(adminAuth)
            .path(PROFESSOR_RESOURCE_NAME)
            .request()
            .get();

        assertThat(response.getStatus(), is(200));

        List<Professor> professors = response.readEntity(
            new GenericType<List<Professor>>() {
            }
        );

        assertThat(professors, is(not(empty())));
    }

    /**
     * Confirms a USER_ROLE account cannot list professors.
     */
    @Test
    public void test14_get_all_professors_as_user_should_fail() {
        Response response = webTarget
            .register(userAuth)
            .path(PROFESSOR_RESOURCE_NAME)
            .request()
            .get();

        assertThat(response.getStatus(), is(403));
    }

    /**
     * Confirms an ADMIN_ROLE account can create a professor.
     */
    @Test
    public void test15_add_professor_as_admin() {
        Professor newProfessor = new Professor();

        newProfessor.setFirstName("Test");
        newProfessor.setLastName("Professor");
        newProfessor.setDegree("Master of Science");

        Response response = webTarget
            .register(adminAuth)
            .path(PROFESSOR_RESOURCE_NAME)
            .request()
            .post(Entity.json(newProfessor));

        assertThat(response.getStatus(), is(200));

        Professor persistedProfessor =
            response.readEntity(Professor.class);

        assertThat(
            persistedProfessor.getId(),
            is(not(0))
        );

        createdProfessorId =
            persistedProfessor.getId();
    }

    /**
     * Confirms an ADMIN_ROLE account can update a professor.
     */
    @Test
    public void test16_update_professor_as_admin() {
        Professor professorWithUpdates =
            new Professor();

        professorWithUpdates.setFirstName("Updated");
        professorWithUpdates.setLastName("Professor");
        professorWithUpdates.setDegree(
            "Doctor of Philosophy"
        );

        Response response = webTarget
            .register(adminAuth)
            .path(
                PROFESSOR_RESOURCE_NAME
                    + "/"
                    + createdProfessorId
            )
            .request()
            .put(Entity.json(professorWithUpdates));

        assertThat(response.getStatus(), is(200));

        Professor updatedProfessor =
            response.readEntity(Professor.class);

        assertThat(
            updatedProfessor.getFirstName(),
            is("Updated")
        );

        assertThat(
            updatedProfessor.getDegree(),
            is("Doctor of Philosophy")
        );
    }

    /**
     * Confirms a nonexistent professor returns HTTP 404.
     */
    @Test
    public void test17_get_professor_not_found_should_return_404() {
        Response response = webTarget
            .register(adminAuth)
            .path(
                PROFESSOR_RESOURCE_NAME
                    + "/999999"
            )
            .request()
            .get();

        assertThat(response.getStatus(), is(404));
    }

    /**
     * Confirms an ADMIN_ROLE account can delete a professor.
     */
    @Test
    public void test18_delete_professor_as_admin() {
        Response response = webTarget
            .register(adminAuth)
            .path(
                PROFESSOR_RESOURCE_NAME
                    + "/"
                    + createdProfessorId
            )
            .request()
            .delete();

        assertThat(response.getStatus(), is(200));
    }

    /**
     * Confirms an ADMIN_ROLE account can list all student clubs.
     */
    @Test
    public void test19_get_all_student_clubs_as_admin() {
        Response response = webTarget
            .register(adminAuth)
            .path(STUDENTCLUB_RESOURCE_NAME)
            .request()
            .get();

        assertThat(response.getStatus(), is(200));

        List<StudentClub> clubs = response.readEntity(
            new GenericType<List<StudentClub>>() {
            }
        );

        assertThat(clubs, is(not(empty())));
    }

    /**
     * Confirms a USER_ROLE account can retrieve the student club list.
     */
    @Test
    public void test20_get_all_student_clubs_as_user() {
        Response response = webTarget
            .register(userAuth)
            .path(STUDENTCLUB_RESOURCE_NAME)
            .request()
            .get();

        assertThat(response.getStatus(), is(200));

        List<StudentClub> clubs = response.readEntity(
            new GenericType<List<StudentClub>>() {
            }
        );

        assertThat(clubs, is(not(empty())));
    }

    /**
     * Confirms an ADMIN_ROLE account can create an academic student club.
     */
    @Test
    public void test21_add_student_club_as_admin() {
        Academic newClub = new Academic();

        newClub.setName("JUnit Academic Club");
        newClub.setDesc(
            "Temporary academic club created by JUnit"
        );

        Response response = webTarget
            .register(adminAuth)
            .path(STUDENTCLUB_RESOURCE_NAME)
            .request()
            .post(Entity.json(newClub));

        assertThat(response.getStatus(), is(200));

        StudentClub persistedClub =
            response.readEntity(StudentClub.class);

        assertThat(
            persistedClub.getId(),
            is(not(0))
        );

        assertThat(
            persistedClub.getAcademic(),
            is(true)
        );

        createdStudentClubId =
            persistedClub.getId();
    }

    /**
     * Confirms an ADMIN_ROLE account can update a student club.
     */
    @Test
    public void test22_update_student_club_as_admin() {
        Academic clubWithUpdates = new Academic();

        clubWithUpdates.setName(
            "Updated JUnit Academic Club"
        );

        clubWithUpdates.setDesc(
            "Updated temporary academic club"
        );

        Response response = webTarget
            .register(adminAuth)
            .path(
                STUDENTCLUB_RESOURCE_NAME
                    + "/"
                    + createdStudentClubId
            )
            .request()
            .put(Entity.json(clubWithUpdates));

        assertThat(response.getStatus(), is(200));

        StudentClub updatedClub =
            response.readEntity(StudentClub.class);

        assertThat(
            updatedClub.getName(),
            is("Updated JUnit Academic Club")
        );

        assertThat(
            updatedClub.getDesc(),
            is("Updated temporary academic club")
        );
    }

    /**
     * Confirms an ADMIN_ROLE account can delete a student club.
     */
    @Test
    public void test23_delete_student_club_as_admin() {
        Response response = webTarget
            .register(adminAuth)
            .path(
                STUDENTCLUB_RESOURCE_NAME
                    + "/"
                    + createdStudentClubId
            )
            .request()
            .delete();

        assertThat(response.getStatus(), is(200));
    }
}