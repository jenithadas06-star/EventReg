package com.college.events.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Links a {@link Student} to an {@link Event}.
 *
 * <p>The student's basic details are copied into the registration so an organizer
 * can read the participant list and the exported history without extra lookups,
 * and so the record stays intact if the student later edits their profile.</p>
 */
public class EventRegistration {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private Long id;
    private Long eventId;
    private Long studentId;

    // Snapshot of the basic registration form
    private String fullName;
    private String email;
    private String department;
    private String section;
    private String year;
    private String registerNumber;
    private String contactNumber;

    private LocalDateTime registeredAt = LocalDateTime.now();
    private RegistrationStatus status = RegistrationStatus.CONFIRMED;

    private final List<RegistrationAnswer> answers = new ArrayList<>();

    public EventRegistration() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
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

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public List<RegistrationAnswer> getAnswers() {
        return answers;
    }

    public String getRegisteredAtLabel() {
        return registeredAt == null ? "-" : registeredAt.format(DT_FMT);
    }

    public boolean hasAnswers() {
        return !answers.isEmpty();
    }
}
