package com.college.events.repository;

import com.college.events.model.Event;
import com.college.events.model.EventStatus;

import java.util.List;
import java.util.Optional;

/**
 * Storage contract for events (including their eligibility rules and custom
 * registration questions, which are owned by the event).
 */
public interface EventRepository {

    Event save(Event event);

    Optional<Event> findById(Long id);

    List<Event> findAll();

    List<Event> findByOrganizerId(Long organizerId);

    List<Event> findByStatus(EventStatus status);

    void deleteById(Long id);

    long count();
}
