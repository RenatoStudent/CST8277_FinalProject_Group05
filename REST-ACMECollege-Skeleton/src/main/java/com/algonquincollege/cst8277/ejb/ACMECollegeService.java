/********************************************************************************************************
 * File:  ACMECollegeService.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * 
 */
package com.algonquincollege.cst8277.ejb;

import static com.algonquincollege.cst8277.entity.Student.ALL_STUDENTS_QUERY_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_KEY_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_SALT_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER_PREFIX;
import static com.algonquincollege.cst8277.utility.MyConstants.PARAM1;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_ALGORITHM;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_ITERATIONS;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_KEY_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_SALT_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.PU_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.USER_ROLE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.entity.Professor;
import com.algonquincollege.cst8277.entity.SecurityRole;
import com.algonquincollege.cst8277.entity.SecurityUser;
import com.algonquincollege.cst8277.entity.Student;
import com.algonquincollege.cst8277.entity.StudentClub;

@SuppressWarnings("unused")

/**
 * Stateless Singleton EJB Bean - ACMECollegeService
 */
@Singleton
public class ACMECollegeService implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LogManager.getLogger();

    private static final String READ_ALL_PROGRAMS = "SELECT name FROM program";

    //TODO ACMECS01 - Add your query constants here.

    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;

    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;

    /*
     * Student service methods
     */

    public List<Student> getAllStudents() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Student> cq = cb.createQuery(Student.class);
        cq.select(cq.from(Student.class));
        return em.createQuery(cq).getResultList();
    }

    public Student getStudentById(int id) {
        return em.find(Student.class, id);
    }

    @Transactional
    public Student persistStudent(Student newStudent) {
        em.persist(newStudent);
        return newStudent;
    }

    @Transactional
    public void buildUserForNewStudent(Student newStudent) {
        SecurityUser userForNewStudent = new SecurityUser();

        userForNewStudent.setUsername(
            DEFAULT_USER_PREFIX + "_" +
            newStudent.getFirstName() + "." +
            newStudent.getLastName()
        );

        Map<String, String> pbAndjProperties = new HashMap<>();
        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
        pbAndjProperties.put(PROPERTY_SALT_SIZE, DEFAULT_SALT_SIZE);
        pbAndjProperties.put(PROPERTY_KEY_SIZE, DEFAULT_KEY_SIZE);

        pbAndjPasswordHash.initialize(pbAndjProperties);

        String pwHash =
            pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());

        userForNewStudent.setPwHash(pwHash);
        userForNewStudent.setStudent(newStudent);

        SecurityRole userRole =
            /* TODO ACMEMS01 - Use NamedQuery on SecurityRole to find USER_ROLE */
            null;

        userForNewStudent.getRoles().add(userRole);
        userRole.getUsers().add(userForNewStudent);

        em.persist(userForNewStudent);
    }

    /**
     * To update a student.
     *
     * @param id id of entity to update
     * @param studentWithUpdates entity with updated information
     * @return entity with updated information
     */
    @Transactional
    public Student updateStudentById(
        int id,
        Student studentWithUpdates
    ) {
        Student studentToBeUpdated = getStudentById(id);

        if (studentToBeUpdated != null) {
            em.refresh(studentToBeUpdated);
            em.merge(studentWithUpdates);
            em.flush();
        }

        return studentWithUpdates;
    }

    /**
     * To delete a student by ID.
     *
     * @param id student ID to delete
     * @return deleted student, or null if it was not found
     */
    @Transactional
    public Student deleteStudentById(int id) {
        Student student = getStudentById(id);

        if (student != null) {
            em.refresh(student);

            /*
             * TODO ACMEMS02 - Use NamedQuery on SecurityRole to find this
             * related Physician so that when we remove it, the relationship
             * from SECURITY_USER table is not dangling.
             */
            TypedQuery<SecurityUser> findUser =
                em.createNamedQuery(
                    "SecurityUser.userByStudentId",
                    SecurityUser.class
                ).setParameter(PARAM1, id);

            SecurityUser sUser = findUser.getSingleResult();

            em.remove(sUser);
            em.remove(student);
        }

        return student;
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllPrograms() {
        List<String> programs = new ArrayList<>();

        try {
            programs = (List<String>) em
                .createNativeQuery(READ_ALL_PROGRAMS)
                .getResultList();
        }
        catch (Exception e) {
            LOG.debug("Unable to retrieve programs", e);
        }

        return programs;
    }

    /*
     * Course service methods
     */

    //TODO ACMECS02 - Add the rest of your CRUD methods here.

    /**
     * Retrieves all courses.
     *
     * @return list of courses
     */
    public List<Course> getAllCourses() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Course> cq = cb.createQuery(Course.class);

        cq.select(cq.from(Course.class));

        return em.createQuery(cq).getResultList();
    }

    /**
     * Retrieves a course by its ID.
     *
     * @param id course ID
     * @return matching course, or null if it was not found
     */
    public Course getCourseById(int id) {
        return em.find(Course.class, id);
    }

    /**
     * Creates a new course.
     *
     * @param newCourse course to create
     * @return persisted course
     */
    @Transactional
    public Course persistCourse(Course newCourse) {
        em.persist(newCourse);
        return newCourse;
    }

    /**
     * Updates an existing course.
     *
     * @param id ID of the course to update
     * @param courseWithUpdates new course information
     * @return updated course, or null if the course was not found
     */
    @Transactional
    public Course updateCourseById(
        int id,
        Course courseWithUpdates
    ) {
        Course courseToBeUpdated = getCourseById(id);

        if (courseToBeUpdated != null) {
            courseToBeUpdated.setCourseCode(
                courseWithUpdates.getCourseCode()
            );

            courseToBeUpdated.setCourseTitle(
                courseWithUpdates.getCourseTitle()
            );

            courseToBeUpdated.setCreditUnits(
                courseWithUpdates.getCreditUnits()
            );

            courseToBeUpdated.setOnline(
                courseWithUpdates.getOnline()
            );

            em.flush();
        }

        return courseToBeUpdated;
    }

    /**
     * Deletes a course by its ID.
     *
     * @param id course ID
     * @return deleted course, or null if it was not found
     */
    @Transactional
    public Course deleteCourseById(int id) {
        Course course = getCourseById(id);

        if (course != null) {
            em.remove(course);
        }

        return course;
    }
}