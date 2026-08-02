/********************************************************************************************************
 * File: StudentClub.java Course Materials CST 8277
 *
 * @author Teddy Yap
 *
 */
package com.algonquincollege.cst8277.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * The persistent class for the student_club database table.
 */
@Entity(name = "StudentClub")
@Table(name = "student_club")
@Access(AccessType.FIELD)
@AttributeOverride(
    name = "id",
    column = @Column(name = "club_id")
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name = "academic",
    discriminatorType = DiscriminatorType.INTEGER
)
@NamedQuery(
    name = StudentClub.ALL_STUDENT_CLUBS_QUERY,
    query = "SELECT DISTINCT sc FROM StudentClub sc "
          + "LEFT JOIN FETCH sc.studentMembers"
)
@NamedQuery(
    name = StudentClub.STUDENT_CLUB_BY_ID_QUERY,
    query = "SELECT DISTINCT sc FROM StudentClub sc "
          + "LEFT JOIN FETCH sc.studentMembers "
          + "WHERE sc.id = :param1"
)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "academic",
    visible = true
)
@JsonSubTypes({
    @Type(value = Academic.class, name = "true"),
    @Type(value = NonAcademic.class, name = "false")
})
public class StudentClub extends PojoBase implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ALL_STUDENT_CLUBS_QUERY =
        "StudentClub.findAll";

    public static final String STUDENT_CLUB_BY_ID_QUERY =
        "StudentClub.findById";

    @Basic(optional = false)
    @Column(name = "name", nullable = false, length = 100)
    protected String name;

    @Basic(optional = false)
    @Column(name = "description", nullable = false, length = 100)
    protected String desc;

    /*
     * The academic column is also the inheritance discriminator.
     * It is therefore read through this field but written by JPA
     * according to the Academic or NonAcademic subclass.
     */
    @Column(
        name = "academic",
        nullable = false,
        insertable = false,
        updatable = false
    )
    protected boolean isAcademic;

    /*
     * Student is the owning side of this many-to-many relationship.
     * Student.studentClubs defines the club_membership join table.
     */
    @ManyToMany(
        cascade = CascadeType.MERGE,
        fetch = FetchType.LAZY,
        mappedBy = "studentClubs"
    )
    protected Set<Student> studentMembers = new HashSet<>();

    @Transient
    protected boolean editable = false;

    public StudentClub() {
        super();
    }

    public StudentClub(boolean isAcademic) {
        this();
        this.isAcademic = isAcademic;
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

    public boolean getAcademic() {
        return isAcademic;
    }

    public void setAcademic(boolean isAcademic) {
        this.isAcademic = isAcademic;
    }

    @JsonIgnore
    public Set<Student> getStudentMembers() {
        return studentMembers;
    }

    public void setStudentMembers(Set<Student> studentMembers) {
        this.studentMembers = studentMembers;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    // Inherited hashCode/equals is sufficient for this entity class.

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("StudentClub[id = ")
            .append(id)
            .append(", name = ")
            .append(name)
            .append(", desc = ")
            .append(desc)
            .append(", isAcademic = ")
            .append(isAcademic)
            .append(", created = ")
            .append(created)
            .append(", updated = ")
            .append(updated)
            .append(", version = ")
            .append(version)
            .append("]");

        return builder.toString();
    }
}