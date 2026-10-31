package com.college.events.repository;

import com.college.events.model.EventRegistration;

import java.util.List;
import java.util.Optional;

/**
 * Storage contract for student registrations.
 */
public interface RegistrationRepository {

    EventRegistration save(EventRegistration registration);

    Optional<EventRegistration> findById(Long id);

    List<EventRegistration> findAll();

    List<EventRegistration> findByStudentId(Long studentId);

    List<EventRegistration> findByEventId(Long eventId);

    Optional<EventRegistration> findByEventIdAndStudentId(Long eventId, Long studentId);

    long countByEventId(Long eventId);

    long countByStudentId(Long studentId);

    void deleteById(Long id);

    long count();
}
