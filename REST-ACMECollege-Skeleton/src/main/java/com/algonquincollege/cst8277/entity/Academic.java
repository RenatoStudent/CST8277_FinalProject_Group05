/********************************************************************************************************
 * File: Academic.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 *
 */
package com.algonquincollege.cst8277.entity;

import java.io.Serializable;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity(name = "Academic")
@DiscriminatorValue("1")
public class Academic extends StudentClub implements Serializable {

    private static final long serialVersionUID = 1L;

    public Academic() {
        super(true);
    }
}