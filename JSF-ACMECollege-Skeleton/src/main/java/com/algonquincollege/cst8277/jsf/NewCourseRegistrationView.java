/********************************************************************************************************
 * File: NewCourseRegistrationView.java
 * Course Materials CST 8277
 *
 * @author Renato Perocchio
 *
 */
package com.algonquincollege.cst8277.jsf;

import java.io.Serializable;

import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.entity.Student;

import jakarta.faces.annotation.ManagedProperty;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("newCourseRegistration")
@ViewScoped
public class NewCourseRegistrationView implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Integer studentId;
    protected Integer courseId;
    protected Integer year;
    protected String semester;

    @Inject
    @ManagedProperty("#{courseRegistrationController}")
    protected CourseRegistrationController courseRegistrationController;

    public NewCourseRegistrationView() {
        super();
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public void addCourseRegistration() {
        Student student = new Student();
        student.setId(getStudentId());

        Course course = new Course();
        course.setId(getCourseId());

        CourseRegistration newRegistration = new CourseRegistration();
        newRegistration.setStudent(student);
        newRegistration.setCourse(course);
        newRegistration.setYear(getYear());
        newRegistration.setSemester(getSemester());

        courseRegistrationController.addNewCourseRegistration(newRegistration);

        courseRegistrationController.toggleAdding();

        setStudentId(null);
        setCourseId(null);
        setYear(null);
        setSemester(null);
    }
}