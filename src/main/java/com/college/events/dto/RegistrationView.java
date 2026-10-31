package com.college.events.dto;

import com.college.events.model.Event;
import com.college.events.model.EventRegistration;

/**
 * Pairs a registration with the event it points at, so "My Registrations" can show
 * event details without the template doing lookups.
 */
public class RegistrationView {

    private final EventRegistration registration;
    private final Event event;

    public RegistrationView(EventRegistration registration, Event event) {
        this.registration = registration;
        this.event = event;
    }

    public EventRegistration getRegistration() {
        return registration;
    }

    public Event getEvent() {
        return event;
    }

    public boolean isEventAvailable() {
        return event != null;
    }
}
