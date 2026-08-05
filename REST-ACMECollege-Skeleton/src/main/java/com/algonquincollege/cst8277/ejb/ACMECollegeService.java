/********************************************************************************************************
 * File:  ACMECollegeService.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 *
 */
package com.algonquincollege.cst8277.ejb;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.entity.Professor;
import com.algonquincollege.cst8277.entity.SecurityRole;
import com.algonquincollege.cst8277.entity.SecurityUser;
import com.algonquincollege.cst8277.entity.Student;
import com.algonquincollege.cst8277.entity.StudentClub;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.transaction.Transactional;

@SuppressWarnings("unused")

/**
 * Stateless Singleton EJB Bean - ACMECollegeService.
 */
@Singleton
public class ACMECollegeService implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LogManager.getLogger();

    private static final String READ_ALL_PROGRAMS =
        "SELECT name FROM program";

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
            DEFAULT_USER_PREFIX
                + "_"
                + newStudent.getFirstName()
                + "."
                + newStudent.getLastName()
        );

        Map<String, String> passwordProperties = new HashMap<>();

        passwordProperties.put(
            PROPERTY_ALGORITHM,
            DEFAULT_PROPERTY_ALGORITHM
        );

        passwordProperties.put(
            PROPERTY_ITERATIONS,
            DEFAULT_PROPERTY_ITERATIONS
        );

        passwordProperties.put(
            PROPERTY_SALT_SIZE,
            DEFAULT_SALT_SIZE
        );

        passwordProperties.put(
            PROPERTY_KEY_SIZE,
            DEFAULT_KEY_SIZE
        );

        pbAndjPasswordHash.initialize(passwordProperties);

        String passwordHash = pbAndjPasswordHash.generate(
            DEFAULT_USER_PASSWORD.toCharArray()
        );

        userForNewStudent.setPwHash(passwordHash);
        userForNewStudent.setStudent(newStudent);

        SecurityRole userRole = em.createNamedQuery(
                SecurityRole.SECURITY_ROLE_BY_NAME,
                SecurityRole.class
            )
            .setParameter(PARAM1, USER_ROLE)
            .getSingleResult();

        userForNewStudent.getRoles().add(userRole);
        userRole.getUsers().add(userForNewStudent);

        em.persist(userForNewStudent);
    }

    @Transactional
    public Student updateStudentById(
        int id,
        Student studentWithUpdates
    ) {
        Student studentToBeUpdated = getStudentById(id);

        if (studentToBeUpdated != null) {
            studentToBeUpdated.setFirstName(
                studentWithUpdates.getFirstName()
            );

            studentToBeUpdated.setLastName(
                studentWithUpdates.getLastName()
            );

            studentToBeUpdated.setEmail(
                studentWithUpdates.getEmail()
            );

            studentToBeUpdated.setPhone(
                studentWithUpdates.getPhone()
            );

            studentToBeUpdated.setProgram(
                studentWithUpdates.getProgram()
            );

            em.flush();
        }

        return studentToBeUpdated;
    }

    @Transactional
    public Student deleteStudentById(int id) {
        Student student = getStudentById(id);

        if (student != null) {
            TypedQuery<SecurityUser> findUser =
                em.createNamedQuery(
                    SecurityUser.SECURITY_USER_BY_STUDENT_ID,
                    SecurityUser.class
                );

            findUser.setParameter(PARAM1, id);

            List<SecurityUser> securityUsers =
                findUser.getResultList();

            if (!securityUsers.isEmpty()) {
                SecurityUser securityUser =
                    securityUsers.get(0);

                for (SecurityRole role :
                    new ArrayList<>(securityUser.getRoles())) {

                    role.getUsers().remove(securityUser);
                }

                securityUser.getRoles().clear();
                securityUser.setStudent(null);

                em.remove(securityUser);
            }

            em.remove(student);
            em.flush();
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

    public List<Course> getAllCourses() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Course> cq =
            cb.createQuery(Course.class);

        cq.select(cq.from(Course.class));

        return em.createQuery(cq).getResultList();
    }

    public Course getCourseById(int id) {
        return em.find(Course.class, id);
    }

    @Transactional
    public Course persistCourse(Course newCourse) {
        em.persist(newCourse);
        return newCourse;
    }

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

    @Transactional
    public Course deleteCourseById(int id) {
        Course course = getCourseById(id);

        if (course != null) {
            em.remove(course);
            em.flush();
        }

        return course;
    }

    /*
     * Professor service methods
     */

    public List<Professor> getAllProfessors() {
        return em.createNamedQuery(
                Professor.ALL_PROFESSORS_QUERY,
                Professor.class
            )
            .getResultList();
    }

    public Professor getProfessorById(int id) {
        return em.find(Professor.class, id);
    }

    @Transactional
    public Professor persistProfessor(
        Professor newProfessor
    ) {
        em.persist(newProfessor);
        em.flush();

        return newProfessor;
    }

    @Transactional
    public Professor updateProfessorById(
        int id,
        Professor professorWithUpdates
    ) {
        Professor professorToBeUpdated =
            getProfessorById(id);

        if (professorToBeUpdated != null) {
            professorToBeUpdated.setFirstName(
                professorWithUpdates.getFirstName()
            );

            professorToBeUpdated.setLastName(
                professorWithUpdates.getLastName()
            );

            professorToBeUpdated.setDegree(
                professorWithUpdates.getDegree()
            );

            em.flush();
        }

        return professorToBeUpdated;
    }

    @Transactional
    public Professor deleteProfessorById(int id) {
        Professor professor = getProfessorById(id);

        if (professor != null) {
            em.remove(professor);
            em.flush();
        }

        return professor;
    }

    /*
     * Student Club service methods
     */

    /**
     * Retrieves all student clubs with their member relationships loaded.
     *
     * @return all student clubs
     */
    public List<StudentClub> getAllStudentClubs() {
        return em.createNamedQuery(
                StudentClub.ALL_STUDENT_CLUBS_QUERY,
                StudentClub.class
            )
            .getResultList();
    }

    /**
     * Retrieves a student club by ID.
     *
     * @param id club ID
     * @return matching club, or null if not found
     */
    public StudentClub getStudentClubById(int id) {
        List<StudentClub> clubs = em.createNamedQuery(
                StudentClub.STUDENT_CLUB_BY_ID_QUERY,
                StudentClub.class
            )
            .setParameter(PARAM1, id)
            .getResultList();

        if (clubs.isEmpty()) {
            return null;
        }

        return clubs.get(0);
    }

    /**
     * Creates a student club.
     *
     * @param newStudentClub club to persist
     * @return persisted club
     */
    @Transactional
    public StudentClub persistStudentClub(
        StudentClub newStudentClub
    ) {
        em.persist(newStudentClub);
        em.flush();

        return newStudentClub;
    }

    /**
     * Updates a student club's name and description.
     *
     * The academic/non-academic discriminator is not changed because
     * changing an entity's inheritance subtype after persistence is unsafe.
     *
     * @param id club ID
     * @param studentClubWithUpdates updated information
     * @return updated club, or null if not found
     */
    @Transactional
    public StudentClub updateStudentClubById(
        int id,
        StudentClub studentClubWithUpdates
    ) {
        StudentClub clubToBeUpdated =
            getStudentClubById(id);

        if (clubToBeUpdated != null) {
            clubToBeUpdated.setName(
                studentClubWithUpdates.getName()
            );

            clubToBeUpdated.setDesc(
                studentClubWithUpdates.getDesc()
            );

            em.flush();
        }

        return clubToBeUpdated;
    }

    /**
     * Deletes a student club and removes any club-membership rows first.
     *
     * Student owns the many-to-many relationship, so the club must be
     * removed from each student's studentClubs collection.
     *
     * @param id club ID
     * @return deleted club, or null if not found
     */
    @Transactional
    public StudentClub deleteStudentClubById(int id) {
        StudentClub club = getStudentClubById(id);

        if (club != null) {
            for (Student student :
                new ArrayList<>(club.getStudentMembers())) {

                student.getStudentClubs().remove(club);
            }

            club.getStudentMembers().clear();

            em.flush();
            em.remove(club);
            em.flush();
        }

        return club;
    }
}