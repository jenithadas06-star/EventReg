package com.college.events.model;

/**
 * The two user roles supported by the portal.
 */
public enum Role {

    STUDENT("Student"),
    ORGANIZER("Organizer");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
