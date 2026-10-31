package com.college.events.controller;

import com.college.events.config.SessionKeys;
import com.college.events.model.Event;
import com.college.events.model.Role;
import com.college.events.model.Student;
import com.college.events.repository.UserRepository;
import com.college.events.service.EligibilityService;
import com.college.events.service.EventService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Public exploration page: browse ongoing, upcoming and past events with filters.
 *
 * <p>Guests only see open events; a signed-in student additionally sees the
 * restricted events they are eligible for. Either way the visibility rules are
 * applied in {@code EventService}/{@code EligibilityService}, never in the view.</p>
 */
@Controller
public class ExploreController {

    private final EventService eventService;
    private final EligibilityService eligibilityService;
    private final UserRepository userRepository;

    public ExploreController(EventService eventService,
                             EligibilityService eligibilityService,
                             UserRepository userRepository) {
        this.eventService = eventService;
        this.eligibilityService = eligibilityService;
        this.userRepository = userRepository;
    }

    @GetMapping("/explore")
    public String explore(@RequestParam(required = false) String q,
                          @RequestParam(required = false) String department,
                          @RequestParam(required = false) String category,
                          @RequestParam(required = false) String year,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                          @RequestParam(required = false, defaultValue = "dateAsc") String sort,
                          HttpSession session, Model model) {

        Student student = currentStudent(session);
        model.addAttribute("events", eventService.exploreEvents(student, q, department, category, year, from, to, sort));
        model.addAttribute("search", q);
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedYear", year);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("sort", sort);
        model.addAttribute("guest", student == null);
        return "explore";
    }

    @GetMapping("/explore/{id}")
    public String eventDetails(@PathVariable Long id, HttpSession session,
                               Model model, RedirectAttributes redirectAttributes) {
        Student student = currentStudent(session);
        Event event = eventService.require(id);

        boolean visible = student == null
                ? eligibilityService.canGuestView(event)
                : eligibilityService.canStudentView(event, student);

        if (!visible) {
            redirectAttributes.addFlashAttribute("errorMessage", student == null
                    ? "That event is restricted. Log in with your college account to see it."
                    : "This event is restricted to specific departments, years or sections.");
            return "redirect:/explore";
        }

        model.addAttribute("event", event);
        model.addAttribute("eligible", student != null && eligibilityService.canStudentView(event, student));
        model.addAttribute("restrictionSummary", eligibilityService.describeRestrictions(event));
        model.addAttribute("guest", student == null);
        return "event-details";
    }

    private Student currentStudent(HttpSession session) {
        Object userId = session.getAttribute(SessionKeys.USER_ID);
        Object role = session.getAttribute(SessionKeys.USER_ROLE);
        if (!(userId instanceof Long id) || role != Role.STUDENT) {
            return null;
        }
        return userRepository.findById(id)
                .filter(Student.class::isInstance)
                .map(Student.class::cast)
                .orElse(null);
    }
}
