package com.college.events.model;

/**
 * An organizer account - a faculty member or student club representative who
 * creates and manages events.
 */
public class Organizer extends User {

    private String clubOrDepartment;
    private String designation;
    private String contactNumber;

    public Organizer() {
        setRole(Role.ORGANIZER);
    }

    public Organizer(Long id, String fullName, String email, String password,
                     String clubOrDepartment, String designation, String contactNumber) {
        super(id, fullName, email, password, Role.ORGANIZER);
        this.clubOrDepartment = clubOrDepartment;
        this.designation = designation;
        this.contactNumber = contactNumber;
    }

    public String getClubOrDepartment() {
        return clubOrDepartment;
    }

    public void setClubOrDepartment(String clubOrDepartment) {
        this.clubOrDepartment = clubOrDepartment;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
