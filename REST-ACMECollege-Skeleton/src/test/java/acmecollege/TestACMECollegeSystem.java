/********************************************************************************************************
 * File:  TestACMECollegeSystem.java
 * Course Materials CST 8277
 * Teddy Yap
 * (Original Author) Mike Norman
 *
 */
package acmecollege;

import static com.algonquincollege.cst8277.utility.MyConstants.APPLICATION_API_VERSION;
import static com.algonquincollege.cst8277.utility.MyConstants.APPLICATION_CONTEXT_ROOT;
// Added for Course tests
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_ADMIN_USER;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_ADMIN_USER_PASSWORD;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_RESOURCE_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
// Added for POST/PUT test bodies
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
// Added for the media-type negative test
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

// Added for Course tests
import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.entity.Student;

@SuppressWarnings("unused")

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestACMECollegeSystem {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;

    // Test fixture(s)
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        logger.debug("oneTimeSetUp");
        uri = UriBuilder
            .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();
        adminAuth = HttpAuthenticationFeature.basic(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER, DEFAULT_USER_PASSWORD);
    }

    protected WebTarget webTarget;
    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient().register(MyObjectMapperProvider.class).register(new LoggingFeature());
        webTarget = client.target(uri);
    }

    @Test
    public void test01_get_all_students_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            //.register(userAuth)
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Student> students = response.readEntity(new GenericType<List<Student>>(){});
        assertThat(students, is(not(empty())));
        assertThat(students, hasSize(2));
    }
    
    // TODO TACMECS 01 - Add your meaningful JUnit tests here.
    // ===================================================================
    // The following are JUnit tests validating REST API behavior for
    // the Student and Course resources (CRUD, role security, negative cases).
    // ===================================================================

    /**
     * Confirms a USER_ROLE account cannot list all students (ADMIN_ROLE only, per security rules).
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
     * Confirms a USER_ROLE account can retrieve their own linked student record (id 1, per seed data).
     */
    @Test
    public void test03_get_own_student_by_id_as_user() {
        Response response = webTarget
            .register(userAuth)
            .path(STUDENT_RESOURCE_NAME + "/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        Student student = response.readEntity(Student.class);
        assertThat(student.getId(), is(1));
    }

    /**
     * Confirms a USER_ROLE account cannot retrieve a student record that isn't their own.
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
     * Confirms an ADMIN_ROLE account can list all courses (2 seeded, per acmecollege-data.sql).
     */
    @Test
    public void test05_get_all_courses_as_admin() {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Course> courses = response.readEntity(new GenericType<List<Course>>(){});
        assertThat(courses, is(not(empty())));
        assertThat(courses, hasSize(2));
    }

    /**
     * Confirms a USER_ROLE account can also list all courses (students need to browse courses to register).
     */
    @Test
    public void test06_get_all_courses_as_user() {
        Response response = webTarget
            .register(userAuth)
            .path(COURSE_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Course> courses = response.readEntity(new GenericType<List<Course>>(){});
        assertThat(courses, hasSize(2));
    }

    static int createdCourseId;

    /**
     * Confirms an ADMIN_ROLE account can create a new course; captures its id for later tests.
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
        Course persistedCourse = response.readEntity(Course.class);
        assertThat(persistedCourse.getId(), is(not(0)));
        createdCourseId = persistedCourse.getId();
    }

    /**
     * Confirms a USER_ROLE account cannot create a course (ADMIN_ROLE only).
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
     * Confirms an ADMIN_ROLE account can update the course created in test07.
     */
    @Test
    public void test09_update_course_as_admin() {
        Course courseWithUpdates = new Course();
        courseWithUpdates.setCourseCode("CST9999");
        courseWithUpdates.setCourseTitle("Updated Test Course");
        courseWithUpdates.setCreditUnits(4);
        courseWithUpdates.setOnline((short) 1);

        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME + "/" + createdCourseId)
            .request()
            .put(Entity.json(courseWithUpdates));
        assertThat(response.getStatus(), is(200));
        Course updatedCourse = response.readEntity(Course.class);
        assertThat(updatedCourse.getCourseTitle(), is("Updated Test Course"));
    }

    /**
     * Confirms a GET for a nonexistent course id returns 404, not a server error.
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
     * Confirms an ADMIN_ROLE account can delete the course created in test07 (cleans up test data).
     */
    @Test
    public void test11_delete_course_as_admin() {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME + "/" + createdCourseId)
            .request()
            .delete();
        assertThat(response.getStatus(), is(200));
    }

    /**
     * Negative test: confirms the API never responds with XML, only JSON, per the assignment's example pattern.
     */
    @Test
    public void test12_response_media_type_is_not_xml() {
        Response response = webTarget
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getMediaType(), is(not(MediaType.APPLICATION_XML_TYPE)));
    }
}