package com.college.events.controller;

import com.college.events.config.SessionKeys;
import com.college.events.dto.RegistrationForm;
import com.college.events.dto.RegistrationView;
import com.college.events.model.Event;
import com.college.events.model.EventRegistration;
import com.college.events.model.RegistrationQuestion;
import com.college.events.model.RegistrationStatus;
import com.college.events.model.Student;
import com.college.events.service.AuthService;
import com.college.events.service.BusinessException;
import com.college.events.service.EligibilityService;
import com.college.events.service.EventService;
import com.college.events.service.RegistrationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Everything behind the Student role: dashboard, live events, event details,
 * the registration form, my registrations, past events and the profile page.
 */
@Controller
@RequestMapping("/student")
public class StudentController {

    private final AuthService authService;
    private final EventService eventService;
    private final RegistrationService registrationService;
    private final EligibilityService eligibilityService;

    public StudentController(AuthService authService,
                             EventService eventService,
                             RegistrationService registrationService,
                             EligibilityService eligibilityService) {
        this.authService = authService;
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.eligibilityService = eligibilityService;
    }

    // ------------------------------------------------------------------
    // Dashboard
    // ------------------------------------------------------------------

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Student student = currentStudent(session);

        List<Event> liveEvents = eventService.liveEventsFor(student, null, null, "dateAsc");
        List<RegistrationView> myRegistrations = toViews(registrationService.registrationsForStudent(student.getId()));

        model.addAttribute("liveEvents", liveEvents.stream().limit(6).toList());
        model.addAttribute("liveEventCount", liveEvents.size());
        model.addAttribute("myRegistrations", myRegistrations.stream().limit(4).toList());
        model.addAttribute("totalRegistrations", countActive(myRegistrations));
        model.addAttribute("upcomingRegistrations", myRegistrations.stream()
                .filter(v -> v.getEvent() != null && !v.getEvent().isCompleted()
                        && v.getRegistration().getStatus() != RegistrationStatus.CANCELLED)
                .count());
        model.addAttribute("pastEventsCount", myRegistrations.stream()
                .filter(v -> v.getEvent() != null && v.getEvent().isCompleted())
                .count());
        model.addAttribute("registeredEventIds", activeEventIds(myRegistrations));
        return "student/dashboard";
    }

    // ------------------------------------------------------------------
    // Live events
    // ------------------------------------------------------------------

    @GetMapping("/events")
    public String liveEvents(@RequestParam(required = false) String q,
                             @RequestParam(required = false) String category,
                             @RequestParam(required = false, defaultValue = "dateAsc") String sort,
                             HttpSession session, Model model) {
        Student student = currentStudent(session);
        List<Event> events = eventService.liveEventsFor(student, q, category, sort);
        model.addAttribute("events", events);
        model.addAttribute("search", q);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("sort", sort);
        model.addAttribute("registeredEventIds", registeredEventIds(student));
        return "student/events";
    }

    @GetMapping("/events/{id}")
    public String eventDetails(@PathVariable Long id, HttpSession session,
                               Model model, RedirectAttributes redirectAttributes) {
        Student student = currentStudent(session);
        Event event = eventService.require(id);

        // Backend enforcement: a restricted event cannot be reached by typing its URL.
        if (!eligibilityService.canStudentView(event, student)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "This event is restricted to specific departments, years or sections, "
                            + "so it is not available to your profile.");
            return "redirect:/student/events";
        }

        model.addAttribute("event", event);
        model.addAttribute("eligible", true);
        model.addAttribute("restrictionSummary", eligibilityService.describeRestrictions(event));
        model.addAttribute("registration", registrationService.find(id, student.getId()).orElse(null));
        return "event-details";
    }

    // ------------------------------------------------------------------
    // Registration
    // ------------------------------------------------------------------

    @GetMapping("/events/{id}/register")
    public String registrationForm(@PathVariable Long id, HttpSession session,
                                   Model model, RedirectAttributes redirectAttributes) {
        Student student = currentStudent(session);
        Event event = eventService.require(id);

        if (!eligibilityService.canStudentView(event, student)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "You are not eligible to register for this event.");
            return "redirect:/student/events";
        }
        if (!event.isRegistrationOpen()) {
            redirectAttributes.addFlashAttribute("errorMessage", event.getRegistrationClosedReason());
            return "redirect:/student/events/" + id;
        }
        registrationService.find(id, student.getId())
                .filter(r -> r.getStatus() != RegistrationStatus.CANCELLED)
                .ifPresent(r -> {
                    throw new BusinessException("You have already registered for this event.");
                });

        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", registrationService.prefill(student));
        }
        model.addAttribute("event", event);
        return "student/register";
    }

    @PostMapping("/events/{id}/register")
    public String submitRegistration(@PathVariable Long id,
                                     @Valid @ModelAttribute("registrationForm") RegistrationForm form,
                                     BindingResult binding, HttpSession session,
                                     Model model, RedirectAttributes redirectAttributes) {
        Student student = currentStudent(session);
        Event event = eventService.require(id);

        // Required custom questions are checked before the service runs so the form
        // can be re-displayed with the student's answers still in place.
        if (form.getAnswers() != null) {
            for (RegistrationQuestion question : event.getQuestions()) {
                String value = form.getAnswers().get(String.valueOf(question.getId()));
                if (question.isRequired() && (value == null || value.isBlank())) {
                    binding.rejectValue("answers", "required",
                            "Please answer: " + question.getQuestionText());
                    break;
                }
            }
        }

        if (binding.hasErrors()) {
            model.addAttribute("event", event);
            return "student/register";
        }

        try {
            EventRegistration registration = registrationService.register(event, student, form);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration confirmed for \"" + event.getName() + "\". "
                            + "Your registration reference is #" + registration.getId() + ".");
            return "redirect:/student/registrations";
        } catch (BusinessException ex) {
            model.addAttribute("formError", ex.getMessage());
            model.addAttribute("event", event);
            return "student/register";
        }
    }

    // ------------------------------------------------------------------
    // My registrations
    // ------------------------------------------------------------------

    @GetMapping("/registrations")
    public String myRegistrations(HttpSession session, Model model) {
        Student student = currentStudent(session);
        List<RegistrationView> views = toViews(registrationService.registrationsForStudent(student.getId()));
        model.addAttribute("myRegistrations", views);
        model.addAttribute("activeCount", countActive(views));
        return "student/my-registrations";
    }

    @PostMapping("/registrations/{id}/cancel")
    public String cancelRegistration(@PathVariable Long id, HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        Student student = currentStudent(session);
        try {
            registrationService.cancel(id, student.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Your registration has been cancelled.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/registrations";
    }

    // ------------------------------------------------------------------
    // Past events
    // ------------------------------------------------------------------

    @GetMapping("/past-events")
    public String pastEvents(@RequestParam(required = false) String q,
                             @RequestParam(required = false) String category,
                             @RequestParam(required = false, defaultValue = "dateDesc") String sort,
                             HttpSession session, Model model) {
        Student student = currentStudent(session);
        model.addAttribute("events", eventService.pastEventsFor(student, q, category, sort));
        model.addAttribute("search", q);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("sort", sort);
        return "student/past-events";
    }

    // ------------------------------------------------------------------
    // Profile
    // ------------------------------------------------------------------

    @GetMapping("/profile")
    public String profile(HttpSession session) {
        return "student/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam(required = false) String department,
                                @RequestParam(required = false) String year,
                                @RequestParam(required = false) String section,
                                @RequestParam(required = false) String registerNumber,
                                @RequestParam(required = false) String contactNumber,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        Student student = currentStudent(session);
        try {
            authService.updateProfile(student.getId(), fullName, email, department, year, section,
                    registerNumber, contactNumber, null, null);
            redirectAttributes.addFlashAttribute("successMessage", "Your profile has been updated.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/profile";
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Student currentStudent(HttpSession session) {
        return authService.requireStudent((Long) session.getAttribute(SessionKeys.USER_ID));
    }

    private List<RegistrationView> toViews(List<EventRegistration> registrations) {
        return registrations.stream()
                .map(r -> new RegistrationView(r, eventService.find(r.getEventId()).orElse(null)))
                .toList();
    }

    private long countActive(List<RegistrationView> views) {
        return views.stream()
                .filter(v -> v.getRegistration().getStatus() != RegistrationStatus.CANCELLED)
                .count();
    }

    private Set<Long> activeEventIds(List<RegistrationView> views) {
        return views.stream()
                .filter(v -> v.getRegistration().getStatus() != RegistrationStatus.CANCELLED)
                .map(v -> v.getRegistration().getEventId())
                .collect(Collectors.toSet());
    }

    private Set<Long> registeredEventIds(Student student) {
        return registrationService.registrationsForStudent(student.getId()).stream()
                .filter(r -> r.getStatus() != RegistrationStatus.CANCELLED)
                .map(EventRegistration::getEventId)
                .collect(Collectors.toSet());
    }
}
