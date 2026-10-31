package com.college.events.service;

import com.college.events.config.AppConstants;
import com.college.events.dto.SignupForm;
import com.college.events.model.Organizer;
import com.college.events.model.Role;
import com.college.events.model.Student;
import com.college.events.model.User;
import com.college.events.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Handles sign-up and login.
 *
 * <p><b>DEMO AUTHENTICATION.</b> This is deliberately a simple, transparent
 * implementation: passwords are stored and compared as plain text and the login
 * state is a plain HTTP session attribute. It is <i>not</i> production ready.
 * Production would use Spring Security, BCrypt password hashing, CSRF tokens,
 * rate limiting, email verification and password reset tokens.</p>
 */
@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Validates credentials and the selected role tab.
     *
     * @throws BusinessException with a user friendly message when login fails
     */
    public User authenticate(String email, String rawPassword, Role selectedRole) {
        User user = userRepository.findByEmail(email == null ? null : email.trim())
                .orElseThrow(() -> new BusinessException(
                        "No account found for " + email + ". Please sign up first."));

        if (!user.getPassword().equals(rawPassword)) {
            throw new BusinessException("Incorrect password. Please try again.");
        }
        if (selectedRole != null && user.getRole() != selectedRole) {
            throw new BusinessException("This account is registered as a " + user.getRole().getLabel()
                    + ". Please switch to the " + user.getRole().getLabel() + " tab to continue.");
        }
        return user;
    }

    /** Creates a new Student or Organizer account. */
    public User register(SignupForm form) {
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new BusinessException("An account with this email already exists. Try logging in instead.");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("The two passwords do not match.");
        }

        Role role = form.getRole() == null ? Role.STUDENT : form.getRole();

        if (role == Role.STUDENT) {
            requireText(form.getDepartment(), "Please select your department.");
            requireText(form.getYear(), "Please select your year of study.");
            requireText(form.getSection(), "Please select your section.");
            requireText(form.getRegisterNumber(), "Please enter your register number.");
            requireText(form.getContactNumber(), "Please enter your contact number.");

            return userRepository.save(new Student(null, form.getFullName().trim(),
                    form.getEmail().trim().toLowerCase(), form.getPassword(),
                    form.getDepartment(), form.getYear(), form.getSection(),
                    form.getRegisterNumber().trim(), form.getContactNumber().trim()));
        }

        requireText(form.getClubOrDepartment(), "Please enter your club or department.");

        return userRepository.save(new Organizer(null, form.getFullName().trim(),
                form.getEmail().trim().toLowerCase(), form.getPassword(),
                form.getClubOrDepartment().trim(),
                form.getDesignation() == null || form.getDesignation().isBlank()
                        ? "Event Organizer" : form.getDesignation().trim(),
                form.getContactNumber() == null ? "" : form.getContactNumber().trim()));
    }

    /** Sends a demo "reset link" - no email is actually sent in this stage. */
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("No account found for " + email + "."));
        // Placeholder for the email step that arrives with production authentication.
        // The UI reports the same success message either way to avoid leaking accounts.
        if (user == null) {
            throw new BusinessException("No account found for " + email + ".");
        }
    }

    public Student requireStudent(Long userId) {
        return userRepository.findById(userId)
                .filter(Student.class::isInstance)
                .map(Student.class::cast)
                .orElseThrow(() -> new BusinessException("Student account not found."));
    }

    public Organizer requireOrganizer(Long userId) {
        return userRepository.findById(userId)
                .filter(Organizer.class::isInstance)
                .map(Organizer.class::cast)
                .orElseThrow(() -> new BusinessException("Organizer account not found."));
    }

    public User updateProfile(Long userId, String fullName, String email,
                              String department, String year, String section,
                              String registerNumber, String contactNumber,
                              String clubOrDepartment, String designation) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Account not found."));

        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName.trim());
        }
        if (email != null && !email.isBlank()) {
            userRepository.findByEmail(email.trim()).ifPresent(other -> {
                if (!other.getId().equals(userId)) {
                    throw new BusinessException("That email address is already in use by another account.");
                }
            });
            user.setEmail(email.trim().toLowerCase());
        }

        if (user instanceof Student student) {
            if (department != null && !department.isBlank()) {
                student.setDepartment(department);
            }
            if (year != null && !year.isBlank()) {
                student.setYear(year);
            }
            if (section != null && !section.isBlank()) {
                student.setSection(section);
            }
            if (registerNumber != null && !registerNumber.isBlank()) {
                student.setRegisterNumber(registerNumber.trim());
            }
            if (contactNumber != null && !contactNumber.isBlank()) {
                student.setContactNumber(contactNumber.trim());
            }
        } else if (user instanceof Organizer organizer) {
            if (clubOrDepartment != null && !clubOrDepartment.isBlank()) {
                organizer.setClubOrDepartment(clubOrDepartment.trim());
            }
            if (designation != null && !designation.isBlank()) {
                organizer.setDesignation(designation.trim());
            }
            if (contactNumber != null && !contactNumber.isBlank()) {
                organizer.setContactNumber(contactNumber.trim());
            }
        }
        return userRepository.save(user);
    }

    public String departmentLabel(String code) {
        return AppConstants.departmentName(code);
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
    }
}
