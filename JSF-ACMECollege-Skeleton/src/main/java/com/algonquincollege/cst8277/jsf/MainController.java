/********************************************************************************************************
 * File: MainController.java
 * Course Materials CST 8277
 *
 * @author Teddy Yap
 *
 */
package com.algonquincollege.cst8277.jsf;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.algonquincollege.cst8277.utility.MyConstants;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("mainController")
@SessionScoped
public class MainController implements Serializable, MyConstants {

    private static final long serialVersionUID = 1L;

    public static final String NONE =
        "None";

    public static final String STUDENT_MANAGEMENT =
        "Student Management";

    public static final String COURSE_MANAGEMENT =
        "Course Management";

    public static final String PROFESSOR_MANAGEMENT =
        "Professor Management";

    public static final String STUDENT_CLUB_MANAGEMENT =
        "Student Club Management";

    public static final String COURSE_REGISTRATION =
        "Course Registration";

    public static final String ASSIGN_PROFESSOR =
        "Assign Professor";

    public static final String ASSIGN_GRADE =
        "Assign Grade";

    public static final String CLUB_MEMBERSHIP_REGISTRATION =
        "Club Membership Registration";

    @Inject
    protected StudentController studentController;

    @Inject
    protected CourseController courseController;

    @Inject
    protected ProfessorController professorController;

    @Inject
    protected StudentClubController studentClubController;

    @Inject
    protected CourseRegistrationController courseRegistrationController;

    protected String optionChosen = NONE;

    protected List<String> options = Stream.of(
        NONE,
        STUDENT_MANAGEMENT,
        COURSE_MANAGEMENT,
        PROFESSOR_MANAGEMENT,
        STUDENT_CLUB_MANAGEMENT,
        COURSE_REGISTRATION,
        ASSIGN_PROFESSOR,
        ASSIGN_GRADE,
        CLUB_MEMBERSHIP_REGISTRATION
    ).collect(Collectors.toList());

    public MainController() {
        super();
    }

    public String getOptionChosen() {
        return optionChosen;
    }

    public void setOptionChosen(
        String optionChosen
    ) {
        this.optionChosen = optionChosen;
    }

    public List<String> getOptions() {
        return options;
    }

    public String submitForm() {
        loadData();
        return null;
    }

    public void loadData() {
        if (optionChosen == null) {
            return;
        }

        switch (optionChosen) {
            case STUDENT_MANAGEMENT:
                studentController.loadStudents();
                break;

            case COURSE_MANAGEMENT:
                courseController.loadCourses();
                break;

            case PROFESSOR_MANAGEMENT:
                professorController.loadProfessors();
                break;

            case STUDENT_CLUB_MANAGEMENT:
                studentClubController.loadStudentClubs();
                break;

            case COURSE_REGISTRATION:
                courseRegistrationController.loadCourseRegistrations();
                break;

            default:
                break;
        }
    }
}