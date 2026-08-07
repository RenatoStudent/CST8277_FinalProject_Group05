/********************************************************************************************************
 * File:  CourseRegistration.java Course Materials CST 8277
 *
 * @author Teddy Yap
 *
 */
package com.algonquincollege.cst8277.entity;

import java.io.Serializable;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * Persistent entity for the course_registration database table.
 */
@Entity(name = "CourseRegistration")
@Table(name = "course_registration")
@Access(AccessType.FIELD)
@NamedQuery(
    name = CourseRegistration.ALL_COURSE_REGISTRATIONS_QUERY_NAME,
    query = """
        SELECT DISTINCT cr
        FROM CourseRegistration cr
        JOIN FETCH cr.student
        JOIN FETCH cr.course
        LEFT JOIN FETCH cr.professor
        """
)
@NamedQuery(
    name = CourseRegistration.QUERY_SPECIFIC_COURSE_REGISTRATION,
    query = """
        SELECT cr
        FROM CourseRegistration cr
        JOIN FETCH cr.student
        JOIN FETCH cr.course
        LEFT JOIN FETCH cr.professor
        WHERE cr.student.id = :param1
          AND cr.course.id = :param2
        """
)
public class CourseRegistration
    extends PojoBaseCompositeKey<CourseRegistrationPK>
    implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ALL_COURSE_REGISTRATIONS_QUERY_NAME =
        "CourseRegistration.findAll";

    public static final String QUERY_SPECIFIC_COURSE_REGISTRATION =
        "CourseRegistration.findSpecificCourseRegistration";

    /**
     * Composite primary key containing student ID and course ID.
     */
    @EmbeddedId
    protected CourseRegistrationPK id;

    /**
     * Student associated with this registration.
     */
    @MapsId("studentId")
    @ManyToOne(
        cascade = {CascadeType.PERSIST, CascadeType.MERGE},
        optional = false,
        fetch = FetchType.LAZY
    )
    @JoinColumn(
        name = "student_id",
        referencedColumnName = "id",
        nullable = false
    )
    protected Student student;

    /**
     * Course associated with this registration.
     */
    @MapsId("courseId")
    @ManyToOne(
        cascade = CascadeType.MERGE,
        optional = false,
        fetch = FetchType.LAZY
    )
    @JoinColumn(
        name = "course_id",
        referencedColumnName = "course_id",
        nullable = false
    )
    protected Course course;

    /**
     * Professor assigned to the registration.
     *
     * A professor may be assigned after the registration is created.
     */
    @ManyToOne(
        cascade = CascadeType.MERGE,
        optional = true,
        fetch = FetchType.LAZY
    )
    @JoinColumn(
        name = "professor_id",
        referencedColumnName = "professor_id",
        nullable = true
    )
    protected Professor professor;

    /**
     * Academic year for the registration.
     */
    @Basic(optional = false)
    @Column(name = "year", nullable = false)
    protected int year;

    /**
     * Semester for the registration.
     */
    @Basic(optional = false)
    @Column(
        name = "semester",
        nullable = false,
        length = 6
    )
    protected String semester;

    /**
     * Optional final letter grade.
     */
    @Basic(optional = true)
    @Column(
        name = "letter_grade",
        nullable = true,
        length = 3
    )
    protected String letterGrade;

    /**
     * JPA-required constructor.
     */
    public CourseRegistration() {
        id = new CourseRegistrationPK();
    }

    @Override
    public CourseRegistrationPK getId() {
        return id;
    }

    @Override
    public void setId(CourseRegistrationPK id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;

        if (id == null) {
            id = new CourseRegistrationPK();
        }

        if (student != null) {
            id.setStudentId(student.getId());
        }
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;

        if (id == null) {
            id = new CourseRegistrationPK();
        }

        if (course != null) {
            id.setCourseId(course.getId());
        }
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public void setLetterGrade(String letterGrade) {
        this.letterGrade = letterGrade;
    }
}