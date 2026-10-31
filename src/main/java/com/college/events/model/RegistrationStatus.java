package com.college.events.model;

/**
 * State of a student's registration for an event.
 */
public enum RegistrationStatus {

    CONFIRMED("Confirmed", "badge-success"),
    WAITLISTED("Waitlisted", "badge-warning"),
    CANCELLED("Cancelled", "badge-muted");

    private final String label;
    private final String cssClass;

    RegistrationStatus(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }
}
