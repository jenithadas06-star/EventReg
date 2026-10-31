package com.college.events.controller;

import com.college.events.config.SessionKeys;
import com.college.events.dto.LoginForm;
import com.college.events.dto.SignupForm;
import com.college.events.model.Role;
import com.college.events.model.User;
import com.college.events.service.AuthService;
import com.college.events.service.BusinessException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Landing page, sign-up, login, logout and the "forgot password" screen.
 */
@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ------------------------------------------------------------------
    // Landing
    // ------------------------------------------------------------------

    @GetMapping("/")
    public String landing(HttpSession session) {
        Role role = (Role) session.getAttribute(SessionKeys.USER_ROLE);
        if (role == Role.STUDENT) {
            return "redirect:/student/dashboard";
        }
        if (role == Role.ORGANIZER) {
            return "redirect:/organizer/dashboard";
        }
        return "index";
    }

    // ------------------------------------------------------------------
    // Login
    // ------------------------------------------------------------------

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String role,
                            @RequestParam(required = false) String required,
                            @RequestParam(required = false) String registered,
                            HttpSession session, Model model) {
        if (session.getAttribute(SessionKeys.USER_ID) != null) {
            return "redirect:/";
        }
        if (!model.containsAttribute("loginForm")) {
            LoginForm form = new LoginForm();
            form.setRole(parseRole(role != null ? role : required));
            model.addAttribute("loginForm", form);
        }
        if ("1".equals(registered)) {
            model.addAttribute("successMessage", "Your account has been created. Please log in to continue.");
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm form,
                        BindingResult binding, HttpSession session, Model model) {
        if (binding.hasErrors()) {
            return "auth/login";
        }
        try {
            User user = authService.authenticate(form.getEmail(), form.getPassword(), form.getRole());
            session.setAttribute(SessionKeys.USER_ID, user.getId());
            session.setAttribute(SessionKeys.USER_ROLE, user.getRole());
            return user.isStudent() ? "redirect:/student/dashboard" : "redirect:/organizer/dashboard";
        } catch (BusinessException ex) {
            model.addAttribute("loginError", ex.getMessage());
            return "auth/login";
        }
    }

    // ------------------------------------------------------------------
    // Sign up
    // ------------------------------------------------------------------

    @GetMapping("/signup")
    public String signupPage(@RequestParam(required = false) String role, Model model) {
        if (!model.containsAttribute("signupForm")) {
            SignupForm form = new SignupForm();
            form.setRole(parseRole(role));
            model.addAttribute("signupForm", form);
        }
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("signupForm") SignupForm form,
                         BindingResult binding, Model model, RedirectAttributes redirectAttributes) {
        if (binding.hasErrors()) {
            return "auth/signup";
        }
        try {
            User user = authService.register(form);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Welcome " + user.getFullName() + "! Your " + user.getRole().getLabel()
                            + " account is ready. Please log in.");
            return "redirect:/login?role=" + user.getRole();
        } catch (BusinessException ex) {
            model.addAttribute("signupError", ex.getMessage());
            return "auth/signup";
        }
    }

    // ------------------------------------------------------------------
    // Forgot password
    // ------------------------------------------------------------------

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) {
        try {
            authService.requestPasswordReset(email);
            model.addAttribute("successMessage", "If an account exists for " + email
                    + ", a password reset link has been sent.");
        } catch (BusinessException ex) {
            // Reported the same way as success so the page does not reveal which
            // email addresses are registered.
            model.addAttribute("successMessage", "If an account exists for " + email
                    + ", a password reset link has been sent.");
        }
        model.addAttribute("submittedEmail", email);
        return "auth/forgot-password";
    }

    // ------------------------------------------------------------------
    // Logout
    // ------------------------------------------------------------------

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "You have been signed out.");
        return "redirect:/";
    }

    private Role parseRole(String value) {
        if (value == null || value.isBlank()) {
            return Role.STUDENT;
        }
        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Role.STUDENT;
        }
    }
}
