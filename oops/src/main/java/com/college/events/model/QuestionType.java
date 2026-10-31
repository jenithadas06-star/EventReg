package com.college.events.model;

/**
 * Types of custom registration question an organizer can attach to an event.
 */
public enum QuestionType {

    SHORT_ANSWER("Short answer"),
    LONG_ANSWER("Long answer"),
    MULTIPLE_CHOICE("Multiple choice");

    private final String label;

    QuestionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
