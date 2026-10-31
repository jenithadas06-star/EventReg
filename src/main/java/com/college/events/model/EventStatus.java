package com.college.events.model;

/**
 * Lifecycle state of an event, controlled by the organizer.
 *
 * <p>The human readable timeline state (Upcoming / Ongoing / Completed) is derived
 * from the event dates instead - see {@link Event#getStatusLabel()}.</p>
 */
public enum EventStatus {

    DRAFT("Draft"),
    PUBLISHED("Published"),
    CANCELLED("Cancelled");

    private final String label;

    EventStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isVisibleToStudents() {
        return this == PUBLISHED;
    }
}
