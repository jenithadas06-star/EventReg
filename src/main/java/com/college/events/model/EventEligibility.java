package com.college.events.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Eligibility + visibility rules for a single event.
 *
 * <p>An <b>open event</b> ({@code visibleToAll = true}) is shown to every student.</p>
 *
 * <p>A <b>restricted event</b> ({@code visibleToAll = false}) is only shown to students
 * that match <i>all</i> populated dimensions. An empty set for a dimension means
 * "any value is allowed" for that dimension, which is how the organizer UI stores
 * the "All departments / All years / All sections" choice.</p>
 */
@Entity
@Table(name = "event_eligibility")
public class EventEligibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long eventId;

    private boolean visibleToAll = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_eligibility_departments", joinColumns = @JoinColumn(name = "eligibility_id"))
    @Column(name = "department")
    private Set<String> departments = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_eligibility_years", joinColumns = @JoinColumn(name = "eligibility_id"))
    @Column(name = "year_val")
    private Set<String> years = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_eligibility_sections", joinColumns = @JoinColumn(name = "eligibility_id"))
    @Column(name = "section")
    private Set<String> sections = new LinkedHashSet<>();

    public EventEligibility() {
    }

    public EventEligibility(Long id, Long eventId) {
        this.id = id;
        this.eventId = eventId;
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

    public boolean isVisibleToAll() {
        return visibleToAll;
    }

    public void setVisibleToAll(boolean visibleToAll) {
        this.visibleToAll = visibleToAll;
    }

    public Set<String> getDepartments() {
        return departments;
    }

    public void setDepartments(Set<String> departments) {
        this.departments = departments == null ? new LinkedHashSet<>() : new LinkedHashSet<>(departments);
    }

    public Set<String> getYears() {
        return years;
    }

    public void setYears(Set<String> years) {
        this.years = years == null ? new LinkedHashSet<>() : new LinkedHashSet<>(years);
    }

    public Set<String> getSections() {
        return sections;
    }

    public void setSections(Set<String> sections) {
        this.sections = sections == null ? new LinkedHashSet<>() : new LinkedHashSet<>(sections);
    }

    // ------------------------------------------------------------------
    // Display helpers used by Thymeleaf templates
    // ------------------------------------------------------------------

    public String getVisibilityLabel() {
        return visibleToAll ? "Open to all students" : "Restricted event";
    }

    public String getDepartmentsLabel() {
        return departments.isEmpty() ? "All departments" : String.join(", ", departments);
    }

    public String getYearsLabel() {
        return years.isEmpty() ? "All years" : String.join(", ", years);
    }

    public String getSectionsLabel() {
        return sections.isEmpty() ? "All sections" : String.join(", ", sections);
    }

    public boolean isRestricted() {
        return !visibleToAll;
    }
}
