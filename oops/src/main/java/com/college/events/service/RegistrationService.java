package com.college.events.service;

import com.college.events.dto.RegistrationForm;
import com.college.events.model.Event;
import com.college.events.model.EventRegistration;
import com.college.events.model.QuestionType;
import com.college.events.model.RegistrationAnswer;
import com.college.events.model.RegistrationQuestion;
import com.college.events.model.RegistrationStatus;
import com.college.events.model.Student;
import com.college.events.repository.RegistrationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Validates and stores student registrations.
 *
 * <p>Every rule (eligibility, duplicate, capacity, deadline, required answers) is
 * re-checked here on the server, so the checks in the browser are only a
 * convenience for the user.</p>
 */
@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EligibilityService eligibilityService;

    public RegistrationService(RegistrationRepository registrationRepository,
                               EligibilityService eligibilityService) {
        this.registrationRepository = registrationRepository;
        this.eligibilityService = eligibilityService;
    }

    /**
     * Registers a student for an event.
     *
     * @throws BusinessException when any rule is violated
     */
    public EventRegistration register(Event event, Student student, RegistrationForm form) {
        if (event == null) {
            throw new BusinessException("That event could not be found.");
        }
        if (student == null) {
            throw new BusinessException("Please log in as a student to register for events.");
        }
        if (!eligibilityService.canStudentView(event, student)) {
            throw new BusinessException("You are not eligible to register for \"" + event.getName()
                    + "\". This event is restricted to specific departments, years or sections.");
        }
        if (event.isCancelled()) {
            throw new BusinessException("This event has been cancelled by the organizer.");
        }
        if (!event.isPublished()) {
            throw new BusinessException("This event is not open for registration yet.");
        }

        LocalDate today = LocalDate.now();
        if (event.getRegistrationOpenDate() != null && today.isBefore(event.getRegistrationOpenDate())) {
            throw new BusinessException("Registrations for this event open on "
                    + event.getRegistrationOpenDate() + ".");
        }
        if (event.getRegistrationDeadline() != null && today.isAfter(event.getRegistrationDeadline())) {
            throw new BusinessException("The registration deadline for this event has passed.");
        }

        Optional<EventRegistration> existing =
                registrationRepository.findByEventIdAndStudentId(event.getId(), student.getId());
        if (existing.isPresent() && existing.get().getStatus() != RegistrationStatus.CANCELLED) {
            throw new BusinessException("You have already registered for \"" + event.getName() + "\".");
        }

        long confirmed = countConfirmed(event.getId());
        if (event.getMaxParticipants() != null && confirmed >= event.getMaxParticipants()) {
            throw new BusinessException("Registration is full - all " + event.getMaxParticipants()
                    + " seats for this event have been taken.");
        }

        EventRegistration registration = existing.orElseGet(EventRegistration::new);
        registration.setEventId(event.getId());
        registration.setStudentId(student.getId());
        registration.setFullName(form.getFullName().trim());
        registration.setEmail(form.getEmail().trim());
        registration.setDepartment(form.getDepartment());
        registration.setSection(form.getSection());
        registration.setYear(form.getYear());
        registration.setRegisterNumber(form.getRegisterNumber().trim());
        registration.setContactNumber(form.getContactNumber().trim());
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.CONFIRMED);
        registration.getAnswers().clear();

        EventRegistration saved = registrationRepository.save(registration);

        for (RegistrationAnswer answer : buildAnswers(event, form, saved.getId())) {
            saved.getAnswers().add(answer);
        }
        return registrationRepository.save(saved);
    }

    /** Cancels a student's own registration, keeping the record for history. */
    public void cancel(Long registrationId, Long studentId) {
        EventRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new BusinessException("Registration not found."));
        if (!registration.getStudentId().equals(studentId)) {
            throw new BusinessException("You can only cancel your own registrations.");
        }
        registration.setStatus(RegistrationStatus.CANCELLED);
        registrationRepository.save(registration);
    }

    public List<EventRegistration> registrationsForStudent(Long studentId) {
        return registrationRepository.findByStudentId(studentId);
    }

    public List<EventRegistration> registrationsForEvent(Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    public Optional<EventRegistration> find(Long eventId, Long studentId) {
        return registrationRepository.findByEventIdAndStudentId(eventId, studentId);
    }

    public long countConfirmed(Long eventId) {
        return registrationRepository.findByEventId(eventId).stream()
                .filter(r -> r.getStatus() != RegistrationStatus.CANCELLED)
                .count();
    }

    /** Rebuilds a {@link RegistrationForm} pre-filled from the student's profile. */
    public RegistrationForm prefill(Student student) {
        RegistrationForm form = new RegistrationForm();
        form.setFullName(student.getFullName());
        form.setEmail(student.getEmail());
        form.setDepartment(student.getDepartment());
        form.setYear(student.getYear());
        form.setSection(student.getSection());
        form.setRegisterNumber(student.getRegisterNumber());
        form.setContactNumber(student.getContactNumber());
        return form;
    }

    private List<RegistrationAnswer> buildAnswers(Event event, RegistrationForm form, Long registrationId) {
        List<RegistrationAnswer> answers = new ArrayList<>();
        Map<String, String> submitted = form.getAnswers();

        for (RegistrationQuestion question : event.getQuestions()) {
            String raw = submitted == null ? null : submitted.get(String.valueOf(question.getId()));
            String value = raw == null ? "" : raw.trim();

            if (value.isEmpty()) {
                if (question.isRequired()) {
                    throw new BusinessException("\"" + question.getQuestionText()
                            + "\" is a required question. Please answer it before submitting.");
                }
            } else if (question.getType() == QuestionType.MULTIPLE_CHOICE
                    && !question.getOptions().contains(value)) {
                throw new BusinessException("Please choose one of the given options for \""
                        + question.getQuestionText() + "\".");
            }
            answers.add(new RegistrationAnswer(null, registrationId, question.getId(),
                    question.getQuestionText(), value));
        }
        return answers;
    }
}
