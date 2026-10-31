package com.college.events.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * A student account. Carries the academic details that drive event eligibility
 * (department / year / section) and the basic registration form fields.
 */
@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {

    private String department;
    private String year;
    private String section;
    private String registerNumber;
    private String contactNumber;

    public Student() {
        setRole(Role.STUDENT);
    }

    public Student(Long id, String fullName, String email, String password,
                   String department, String year, String section,
                   String registerNumber, String contactNumber) {
        super(id, fullName, email, password, Role.STUDENT);
        this.department = department;
        this.year = year;
        this.section = section;
        this.registerNumber = registerNumber;
        this.contactNumber = contactNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
