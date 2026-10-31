package com.college.events.service;

import com.college.events.dto.EventForm;
import com.college.events.dto.QuestionForm;
import com.college.events.model.Event;
import com.college.events.model.EventEligibility;
import com.college.events.model.EventRegistration;
import com.college.events.model.EventStatus;
import com.college.events.model.Organizer;
import com.college.events.model.QuestionType;
import com.college.events.model.RegistrationQuestion;
import com.college.events.model.Student;
import com.college.events.repository.EventRepository;
import com.college.events.repository.RegistrationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * All event business logic: creation, editing, lifecycle changes, searching,
 * sorting, and the visibility rules used by the student / explore pages.
 */
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final EligibilityService eligibilityService;

    public EventService(EventRepository eventRepository,
                        RegistrationRepository registrationRepository,
                        EligibilityService eligibilityService) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.eligibilityService = eligibilityService;
    }

    // ------------------------------------------------------------------
    // Reads
    // ------------------------------------------------------------------

    public Optional<Event> find(Long id) {
        return eventRepository.findById(id).map(this::withRegistrationCount);
    }

    public Event require(Long id) {
        return find(id).orElseThrow(() -> new BusinessException("That event could not be found."));
    }

    /** Event plus a seat count. */
    private Event withRegistrationCount(Event event) {
        event.setRegisteredCount((int) registrationRepository.countByEventId(event.getId()));
        if (event.getEligibility() != null) {
            event.getEligibility().setEventId(event.getId());
        }
        return event;
    }

    public List<Event> allEvents() {
        return eventRepository.findAll().stream().map(this::withRegistrationCount).toList();
    }

    public List<Event> organizerEvents(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId).stream()
                .map(this::withRegistrationCount)
                .sorted(Comparator.comparing(Event::getEventDate,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    /**
     * Events a student may currently see and register for: published, not finished,
     * and matching their eligibility.
     */
    public List<Event> liveEventsFor(Student student, String search, String category, String sort) {
        return allEvents().stream()
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .filter(e -> !e.isCompleted())
                .filter(e -> eligibilityService.isEligible(e, student))
                .filter(e -> matchesSearch(e, search))
                .filter(e -> matchesCategory(e, category))
                .sorted(eventComparator(sort, "dateAsc"))
                .toList();
    }

    /** Previously conducted events the student is allowed to explore. */
    public List<Event> pastEventsFor(Student student, String search, String category, String sort) {
        return allEvents().stream()
                .filter(Event::isPublished)
                .filter(Event::isCompleted)
                .filter(e -> eligibilityService.isEligible(e, student))
                .filter(e -> matchesSearch(e, search))
                .filter(e -> matchesCategory(e, category))
                .sorted(eventComparator(sort, "dateDesc"))
                .toList();
    }

    /**
     * Public exploration with the filter bar.
     *
     * @param student the logged in student, or {@code null} for a guest. Guests only
     *                ever see open events, students additionally see restricted events
     *                they match.
     */
    public List<Event> exploreEvents(Student student, String search, String department,
                                     String category, String year, LocalDate from, LocalDate to,
                                     String sort) {
        return allEvents().stream()
                .filter(Event::isPublished)
                .filter(e -> student == null
                        ? eligibilityService.canGuestView(e)
                        : eligibilityService.isEligible(e, student))
                .filter(e -> matchesSearch(e, search))
                .filter(e -> matchesCategory(e, category))
                .filter(e -> matchesDepartment(e, department))
                .filter(e -> matchesYear(e, year))
                .filter(e -> matchesDateRange(e, from, to))
                .sorted(eventComparator(sort, "dateAsc"))
                .toList();
    }

    public OrganizerStats stats(Long organizerId) {
        List<Event> events = organizerEvents(organizerId);
        long upcoming = 0;
        long completed = 0;
        long cancelled = 0;
        long drafts = 0;
        long registrations = 0;
        Set<Long> participants = new HashSet<>();

        for (Event event : events) {
            switch (event.getStatusLabel()) {
                case "Completed" -> completed++;
                case "Cancelled" -> cancelled++;
                case "Draft" -> drafts++;
                default -> upcoming++;
            }
            List<EventRegistration> eventRegistrations = registrationRepository.findByEventId(event.getId());
            registrations += eventRegistrations.size();
            eventRegistrations.forEach(r -> participants.add(r.getStudentId()));
        }
        return new OrganizerStats(events.size(), upcoming, completed, cancelled, drafts,
                registrations, participants.size());
    }

    // ------------------------------------------------------------------
    // Writes
    // ------------------------------------------------------------------

    public Event create(EventForm form, Organizer organizer, boolean publish) {
        Event event = new Event();
        event.setOrganizerId(organizer.getId());
        event.setCreatedAt(LocalDateTime.now());
        applyForm(event, form);
        event.setStatus(publish ? EventStatus.PUBLISHED : EventStatus.DRAFT);
        return withRegistrationCount(eventRepository.save(event));
    }

    public Event update(Long eventId, EventForm form, Organizer organizer) {
        Event event = requireOwned(eventId, organizer);
        applyForm(event, form);
        return withRegistrationCount(eventRepository.save(event));
    }

    public Event requireOwned(Long eventId, Organizer organizer) {
        Event event = require(eventId);
        if (organizer == null || !organizer.getId().equals(event.getOrganizerId())) {
            throw new BusinessException("You can only manage events that you created.");
        }
        return event;
    }

    public void publish(Long eventId, Organizer organizer) {
        Event event = requireOwned(eventId, organizer);
        event.setStatus(EventStatus.PUBLISHED);
        eventRepository.save(event);
    }

    public void saveAsDraft(Long eventId, Organizer organizer) {
        Event event = requireOwned(eventId, organizer);
        event.setStatus(EventStatus.DRAFT);
        eventRepository.save(event);
    }

    public void cancelEvent(Long eventId, Organizer organizer) {
        Event event = requireOwned(eventId, organizer);
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    /** Closes registration immediately by moving the deadline to yesterday. */
    public void closeRegistrations(Long eventId, Organizer organizer) {
        Event event = requireOwned(eventId, organizer);
        event.setRegistrationDeadline(LocalDate.now().minusDays(1));
        event.setStatus(EventStatus.PUBLISHED);
        eventRepository.save(event);
    }

    public void reopenRegistrations(Long eventId, Organizer organizer) {
        Event event = requireOwned(eventId, organizer);
        event.setRegistrationDeadline(event.getEventDate());
        eventRepository.save(event);
    }

    public void deleteEvent(Long eventId, Organizer organizer) {
        requireOwned(eventId, organizer);
        registrationRepository.findByEventId(eventId).forEach(r -> registrationRepository.deleteById(r.getId()));
        eventRepository.deleteById(eventId);
    }

    // ------------------------------------------------------------------
    // Form -> entity mapping
    // ------------------------------------------------------------------

    private void applyForm(Event event, EventForm form) {
        if (form.getRegistrationOpenDate() != null && form.getRegistrationDeadline() != null
                && form.getRegistrationDeadline().isBefore(form.getRegistrationOpenDate())) {
            throw new BusinessException("The registration deadline cannot be before the opening date.");
        }
        if (form.getRegistrationDeadline() != null && form.getEventDate() != null
                && form.getRegistrationDeadline().isAfter(form.getEventDate())) {
            throw new BusinessException("The registration deadline should not fall after the event date.");
        }

        event.setName(form.getName().trim());
        event.setDescription(form.getDescription().trim());
        event.setCategory(form.getCategory());
        if (form.getPosterUrl() != null && !form.getPosterUrl().isBlank()) {
            event.setPosterUrl(form.getPosterUrl().trim());
        }

        event.setEventDate(form.getEventDate());
        event.setStartTime(form.getStartTime());
        event.setDurationMinutes(form.getDurationMinutes());
        event.setRegistrationOpenDate(form.getRegistrationOpenDate());
        event.setRegistrationDeadline(form.getRegistrationDeadline());

        event.setVenue(form.getVenue().trim());
        event.setBuilding(trimToNull(form.getBuilding()));
        event.setRoomNumber(trimToNull(form.getRoomNumber()));

        event.setMaxParticipants(form.getMaxParticipants());
        event.setTeamEvent(form.isTeamEvent());
        event.setTeamSize(form.isTeamEvent() ? form.getTeamSize() : null);

        event.setOrganizerName(form.getOrganizerName().trim());
        event.setClubOrDepartment(trimToNull(form.getClubOrDepartment()));
        event.setOrganizerContact(form.getOrganizerContact().trim());

        event.setInstructions(trimToNull(form.getInstructions()));
        event.setEligibilityRequirements(trimToNull(form.getEligibilityRequirements()));
        event.setPrerequisites(trimToNull(form.getPrerequisites()));

        EventEligibility eligibility = event.getEligibility() == null
                ? new EventEligibility() : event.getEligibility();
        eligibility.setEventId(event.getId());
        eligibility.setVisibleToAll(form.isVisibleToAll());
        eligibility.setDepartments(new LinkedHashSet<>(form.getDepartments()));
        eligibility.setYears(new LinkedHashSet<>(form.getYears()));
        eligibility.setSections(new LinkedHashSet<>(form.getSections()));
        event.setEligibility(eligibility);

        event.getQuestions().clear();
        event.getQuestions().addAll(buildQuestions(form.getQuestionText(), form.getQuestionType(),
                form.getQuestionRequired(), form.getQuestionOptions(), event.getId()));
    }

    /** Replaces just the custom registration questions of an existing event. */
    public void replaceQuestions(Long eventId, Organizer organizer, QuestionForm form) {
        Event event = requireOwned(eventId, organizer);
        event.getQuestions().clear();
        event.getQuestions().addAll(buildQuestions(form.getQuestionText(), form.getQuestionType(),
                form.getQuestionRequired(), form.getQuestionOptions(), event.getId()));
        eventRepository.save(event);
    }

    /** Zips the four parallel question lists into {@link RegistrationQuestion} rows. */
    private List<RegistrationQuestion> buildQuestions(List<String> texts, List<String> types,
                                                      List<String> requiredFlags, List<String> optionLines,
                                                      Long eventId) {
        List<RegistrationQuestion> questions = new ArrayList<>();
        if (texts == null) {
            return questions;
        }

        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            if (text == null || text.isBlank()) {
                continue;
            }
            QuestionType type = parseType(valueAt(types, i));
            boolean required = "true".equalsIgnoreCase(valueAt(requiredFlags, i));
            List<String> options = type == QuestionType.MULTIPLE_CHOICE
                    ? splitOptions(valueAt(optionLines, i))
                    : new ArrayList<>();

            if (type == QuestionType.MULTIPLE_CHOICE && options.isEmpty()) {
                throw new BusinessException("The multiple choice question \"" + text.trim()
                        + "\" needs at least one option.");
            }
            questions.add(new RegistrationQuestion(null, eventId, text.trim(), type,
                    required, options, questions.size()));
        }
        return questions;
    }

    private String valueAt(List<String> list, int index) {
        return (list == null || index >= list.size()) ? null : list.get(index);
    }

    private List<String> splitOptions(String raw) {
        List<String> options = new ArrayList<>();
        if (raw == null) {
            return options;
        }
        for (String option : raw.split("[\\r\\n,]+")) {
            String cleaned = option.trim();
            if (!cleaned.isEmpty()) {
                options.add(cleaned);
            }
        }
        return options;
    }

    private QuestionType parseType(String raw) {
        if (raw == null || raw.isBlank()) {
            return QuestionType.SHORT_ANSWER;
        }
        try {
            return QuestionType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return QuestionType.SHORT_ANSWER;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // ------------------------------------------------------------------
    // Filtering helpers
    // ------------------------------------------------------------------

    private boolean matchesSearch(Event event, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String needle = search.trim().toLowerCase();
        return contains(event.getName(), needle)
                || contains(event.getDescription(), needle)
                || contains(event.getCategory(), needle)
                || contains(event.getVenue(), needle);
    }

    private boolean contains(String haystack, String needle) {
        return haystack != null && haystack.toLowerCase().contains(needle);
    }

    private boolean matchesCategory(Event event, String category) {
        return category == null || category.isBlank() || "ALL".equalsIgnoreCase(category)
                || category.equalsIgnoreCase(event.getCategory());
    }

    private boolean matchesDepartment(Event event, String department) {
        if (department == null || department.isBlank() || "ALL".equalsIgnoreCase(department)) {
            return true;
        }
        EventEligibility eligibility = event.getEligibility();
        if (eligibility == null || eligibility.isVisibleToAll() || eligibility.getDepartments().isEmpty()) {
            return true;
        }
        return eligibility.getDepartments().stream().anyMatch(d -> d.equalsIgnoreCase(department));
    }

    private boolean matchesYear(Event event, String year) {
        if (year == null || year.isBlank() || "ALL".equalsIgnoreCase(year)) {
            return true;
        }
        EventEligibility eligibility = event.getEligibility();
        if (eligibility == null || eligibility.isVisibleToAll() || eligibility.getYears().isEmpty()) {
            return true;
        }
        return eligibility.getYears().stream().anyMatch(y -> y.equalsIgnoreCase(year));
    }

    private boolean matchesDateRange(Event event, LocalDate from, LocalDate to) {
        if (event.getEventDate() == null) {
            return from == null && to == null;
        }
        if (from != null && event.getEventDate().isBefore(from)) {
            return false;
        }
        return to == null || !event.getEventDate().isAfter(to);
    }

    private Comparator<Event> eventComparator(String sort, String fallback) {
        String key = (sort == null || sort.isBlank()) ? fallback : sort;
        Comparator<Event> byDate = Comparator.comparing(Event::getEventDate,
                Comparator.nullsLast(Comparator.naturalOrder()));
        Comparator<Event> byName = Comparator.comparing(Event::getName,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        Comparator<Event> byDeadline = Comparator.comparing(Event::getRegistrationDeadline,
                Comparator.nullsLast(Comparator.naturalOrder()));

        return switch (key) {
            case "dateDesc" -> byDate.reversed();
            case "nameAsc" -> byName;
            case "nameDesc" -> byName.reversed();
            case "deadline" -> byDeadline;
            case "popular" -> Comparator.comparingInt(Event::getRegisteredCount).reversed();
            default -> byDate;
        };
    }
}
