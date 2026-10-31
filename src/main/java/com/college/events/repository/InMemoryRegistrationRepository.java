package com.college.events.repository;

import com.college.events.model.EventRegistration;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory {@link RegistrationRepository} used until MySQL is wired in.
 */
@Repository
public class InMemoryRegistrationRepository implements RegistrationRepository {

    private final Map<Long, EventRegistration> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public EventRegistration save(EventRegistration registration) {
        if (registration.getId() == null) {
            registration.setId(sequence.incrementAndGet());
        } else {
            sequence.updateAndGet(current -> Math.max(current, registration.getId()));
        }
        store.put(registration.getId(), registration);
        return registration;
    }

    @Override
    public Optional<EventRegistration> findById(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(store.get(id));
    }

    @Override
    public List<EventRegistration> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(EventRegistration::getId))
                .toList();
    }

    @Override
    public List<EventRegistration> findByStudentId(Long studentId) {
        return store.values().stream()
                .filter(r -> studentId != null && studentId.equals(r.getStudentId()))
                .sorted(Comparator.comparing(EventRegistration::getRegisteredAt).reversed())
                .toList();
    }

    @Override
    public List<EventRegistration> findByEventId(Long eventId) {
        return store.values().stream()
                .filter(r -> eventId != null && eventId.equals(r.getEventId()))
                .sorted(Comparator.comparing(EventRegistration::getRegisteredAt))
                .toList();
    }

    @Override
    public Optional<EventRegistration> findByEventIdAndStudentId(Long eventId, Long studentId) {
        return store.values().stream()
                .filter(r -> eventId != null && eventId.equals(r.getEventId()))
                .filter(r -> studentId != null && studentId.equals(r.getStudentId()))
                .findFirst();
    }

    @Override
    public long countByEventId(Long eventId) {
        return findByEventId(eventId).size();
    }

    @Override
    public long countByStudentId(Long studentId) {
        return findByStudentId(studentId).size();
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            store.remove(id);
        }
    }

    @Override
    public long count() {
        return store.size();
    }
}
