package com.college.events.service;

import com.college.events.model.Event;
import com.college.events.model.EventEligibility;
import com.college.events.model.EventStatus;
import com.college.events.model.Student;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Central, reusable implementation of the event eligibility & visibility rules.
 *
 * <p>Every rule is enforced here - and therefore on the server - so hiding a card
 * with CSS or typing an event URL by hand cannot let a student past a restriction.</p>
 */
@Service
public class EligibilityService {

    /**
     * True when the student matches the event's department / year / section
     * restrictions. An open event is always eligible.
     */
    public boolean isEligible(Event event, Student student) {
        if (event == null) {
            return false;
        }
        EventEligibility eligibility = event.getEligibility();
        if (eligibility == null || eligibility.isVisibleToAll()) {
            return true;
        }
        if (student == null) {
            return false;
        }
        return matches(eligibility.getDepartments(), student.getDepartment())
                && matches(eligibility.getYears(), student.getYear())
                && matches(eligibility.getSections(), student.getSection());
    }

    /**
     * True when the student is allowed to see the event at all: it must be
     * published, not cancelled, and the student must be eligible.
     */
    public boolean canStudentView(Event event, Student student) {
        return event != null
                && event.getStatus() == EventStatus.PUBLISHED
                && isEligible(event, student);
    }

    /** Anonymous visitors only ever see open, published events. */
    public boolean canGuestView(Event event) {
        return event != null
                && event.getStatus() == EventStatus.PUBLISHED
                && event.getEligibility() != null
                && event.getEligibility().isVisibleToAll();
    }

    /** A student can only register for an event they are eligible for. */
    public boolean canRegister(Event event, Student student) {
        return student != null && canStudentView(event, student);
    }

    /** "CSE, IT" / "Second Year" - shown on cards and the details page. */
    public String describeRestrictions(Event event) {
        if (event == null || event.getEligibility() == null) {
            return "Open to all students";
        }
        EventEligibility eligibility = event.getEligibility();
        if (eligibility.isVisibleToAll()) {
            return "Open to all students of every department and year";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Department: ").append(eligibility.getDepartmentsLabel());
        sb.append(" | Year: ").append(eligibility.getYearsLabel());
        sb.append(" | Section: ").append(eligibility.getSectionsLabel());
        return sb.toString();
    }

    private boolean matches(Collection<String> allowed, String actual) {
        if (allowed == null || allowed.isEmpty()) {
            return true; // "All" was selected for this dimension
        }
        if (actual == null) {
            return false;
        }
        return allowed.stream().anyMatch(value -> value.equalsIgnoreCase(actual));
    }
}
