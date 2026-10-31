package com.college.events.config;

import com.college.events.model.Organizer;
import com.college.events.model.QuestionType;
import com.college.events.model.Student;
import com.college.events.model.User;
import com.college.events.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Adds the reference data (departments, years, sections, categories) and the
 * signed-in user to every view so templates and the navigation bar have what
 * they need without each controller repeating it.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    private final UserRepository userRepository;

    public GlobalModelAttributes(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute
    public void addCommonAttributes(Model model, HttpSession session, HttpServletRequest request) {
        model.addAttribute("departments", AppConstants.DEPARTMENTS);
        model.addAttribute("years", AppConstants.YEARS);
        model.addAttribute("sections", AppConstants.SECTIONS);
        model.addAttribute("categories", AppConstants.CATEGORIES);
        model.addAttribute("teamSizes", AppConstants.TEAM_SIZES);
        model.addAttribute("questionTypes", QuestionType.values());

        // Current request path, used by the navigation bar to highlight the active link.
        model.addAttribute("currentPath",
                request.getRequestURI() == null ? "" : request.getRequestURI());

        // Default to a signed-in view; the explore controller flips this to true
        // for guests so templates can rely on the flag always being present.
        model.addAttribute("guest", Boolean.FALSE);

        Object userId = session.getAttribute(SessionKeys.USER_ID);
        if (userId instanceof Long id) {
            userRepository.findById(id).ifPresent(user -> populate(model, user));
        }
    }

    private void populate(Model model, User user) {
        model.addAttribute("currentUser", user);
        model.addAttribute("currentRole", user.getRole());
        model.addAttribute("isStudent", user instanceof Student);
        model.addAttribute("isOrganizer", user instanceof Organizer);
        if (user instanceof Student student) {
            model.addAttribute("currentStudent", student);
        }
        if (user instanceof Organizer organizer) {
            model.addAttribute("currentOrganizer", organizer);
        }
    }
}
