package com.college.events.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The basic student details collected on every event registration form.
 * These are always required, regardless of the organizer's custom questions.
 *
 * <p>{@code answers} holds the optional organizer-defined questions keyed by
 * question id, so one form object covers the fixed and dynamic parts.</p>
 */
public class RegistrationForm {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Please select your department")
    private String department;

    @NotBlank(message = "Please select your section")
    private String section;

    @NotBlank(message = "Please select your year of study")
    private String year;

    @NotBlank(message = "Register number is required")
    private String registerNumber;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9+\\-\\s]{10,15}$", message = "Enter a valid contact number (10 to 15 digits)")
    private String contactNumber;

    /** Answers to the organizer's custom questions, keyed by question id. */
    private Map<String, String> answers = new LinkedHashMap<>();

    public Map<String, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, String> answers) {
        this.answers = answers == null ? new LinkedHashMap<>() : answers;
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
}
