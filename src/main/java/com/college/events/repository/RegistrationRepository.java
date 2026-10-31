package com.college.events.repository;

import com.college.events.model.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Storage contract for student registrations, backed by Spring Data JPA.
 */
@Repository
public interface RegistrationRepository extends JpaRepository<EventRegistration, Long> {

    List<EventRegistration> findByStudentId(Long studentId);

    List<EventRegistration> findByEventId(Long eventId);

    Optional<EventRegistration> findByEventIdAndStudentId(Long eventId, Long studentId);

    long countByEventId(Long eventId);

    long countByStudentId(Long studentId);
}
