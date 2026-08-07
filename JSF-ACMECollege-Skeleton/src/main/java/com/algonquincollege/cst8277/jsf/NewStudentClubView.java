/********************************************************************************************************
 * File: NewStudentClubView.java
 * Course Materials CST 8277
 *
 * @author Scott Van Dijk
 * @author Renato Perocchio
 *
 */
package com.algonquincollege.cst8277.jsf;

import java.io.Serializable;

import com.algonquincollege.cst8277.entity.Academic;
import com.algonquincollege.cst8277.entity.NonAcademic;
import com.algonquincollege.cst8277.entity.StudentClub;

import jakarta.faces.annotation.ManagedProperty;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("newStudentClub")
@ViewScoped
public class NewStudentClubView implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String name;
    protected String desc;
    protected boolean academic;

    @Inject
    @ManagedProperty("#{studentClubController}")
    protected StudentClubController studentClubController;

    public NewStudentClubView() {
        super();
        academic = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isAcademic() {
        return academic;
    }

    public void setAcademic(boolean academic) {
        this.academic = academic;
    }

    public void addStudentClub() {
        StudentClub newStudentClub = isAcademic()
            ? new Academic()
            : new NonAcademic();

        newStudentClub.setName(getName());
        newStudentClub.setDesc(getDesc());

        studentClubController.addNewStudentClub(newStudentClub);

        studentClubController.toggleAdding();

        setName(null);
        setDesc(null);
        setAcademic(true);
    }
}