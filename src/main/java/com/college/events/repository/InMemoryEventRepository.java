package com.college.events.repository;

import com.college.events.model.Event;
import com.college.events.model.EventStatus;
import com.college.events.model.RegistrationQuestion;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory {@link EventRepository} used until MySQL is wired in.
 * Eligibility rules and custom questions live inside the {@link Event} aggregate.
 */
@Repository
public class InMemoryEventRepository implements EventRepository {

    private final Map<Long, Event> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final AtomicLong questionSequence = new AtomicLong(0);

    @Override
    public Event save(Event event) {
        if (event.getId() == null) {
            event.setId(sequence.incrementAndGet());
        } else {
            sequence.updateAndGet(current -> Math.max(current, event.getId()));
        }
        if (event.getEligibility() != null && event.getEligibility().getEventId() == null) {
            event.getEligibility().setEventId(event.getId());
        }
        if (event.getEligibility() != null && event.getEligibility().getId() == null) {
            event.getEligibility().setId(event.getId());
        }
        // Custom questions are stored inside the event aggregate, so they are
        // assigned their own ids here (used as the answer map key on the form).
        if (event.getQuestions() != null) {
            for (RegistrationQuestion question : event.getQuestions()) {
                if (question.getId() == null) {
                    question.setId(questionSequence.incrementAndGet());
                } else {
                    questionSequence.updateAndGet(current -> Math.max(current, question.getId()));
                }
                question.setEventId(event.getId());
            }
        }
        store.put(event.getId(), event);
        return event;
    }

    @Override
    public Optional<Event> findById(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Event> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Event::getId))
                .toList();
    }

    @Override
    public List<Event> findByOrganizerId(Long organizerId) {
        return store.values().stream()
                .filter(e -> organizerId != null && organizerId.equals(e.getOrganizerId()))
                .sorted(Comparator.comparing(Event::getId))
                .toList();
    }

    @Override
    public List<Event> findByStatus(EventStatus status) {
        return store.values().stream()
                .filter(e -> e.getStatus() == status)
                .sorted(Comparator.comparing(Event::getId))
                .toList();
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
