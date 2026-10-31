package com.college.events.controller;

import com.college.events.config.SessionKeys;
import com.college.events.dto.EventForm;
import com.college.events.dto.QuestionForm;
import com.college.events.model.Event;
import com.college.events.model.EventRegistration;
import com.college.events.model.Organizer;
import com.college.events.model.RegistrationQuestion;
import com.college.events.service.AuthService;
import com.college.events.service.BusinessException;
import com.college.events.service.EligibilityService;
import com.college.events.service.EventService;
import com.college.events.service.RegistrationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;

/**
 * Everything behind the Organizer role: overview, event creation / editing,
 * custom registration questions, participant lists and the profile page.
 */
@Controller
@RequestMapping("/organizer")
public class OrganizerController {

    private static final List<String> ALLOWED_POSTER_EXTENSIONS =
            List.of(".png", ".jpg", ".jpeg", ".webp", ".gif");

    private final AuthService authService;
    private final EventService eventService;
    private final RegistrationService registrationService;
    private final EligibilityService eligibilityService;

    public OrganizerController(AuthService authService,
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
        Organizer organizer = currentOrganizer(session);
        List<Event> events = eventService.organizerEvents(organizer.getId());

        model.addAttribute("stats", eventService.stats(organizer.getId()));
        model.addAttribute("recentEvents", events.stream().limit(5).toList());
        model.addAttribute("allEvents", events);
        return "organizer/dashboard";
    }

    // ------------------------------------------------------------------
    // Manage events
    // ------------------------------------------------------------------

    @GetMapping("/events")
    public String manageEvents(@RequestParam(required = false) String status,
                               HttpSession session, Model model) {
        Organizer organizer = currentOrganizer(session);
        List<Event> events = eventService.organizerEvents(organizer.getId());

        List<Event> filtered = events;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            filtered = events.stream()
                    .filter(e -> status.equalsIgnoreCase(e.getStatusLabel()))
                    .toList();
        }

        model.addAttribute("events", filtered);
        model.addAttribute("allCount", events.size());
        model.addAttribute("statusFilter", status == null ? "ALL" : status);
        return "organizer/events";
    }

    @GetMapping("/events/new")
    public String newEventForm(HttpSession session, Model model) {
        Organizer organizer = currentOrganizer(session);
        EventForm form = new EventForm();
        form.setOrganizerName(organizer.getFullName());
        form.setClubOrDepartment(organizer.getClubOrDepartment());
        form.setOrganizerContact(organizer.getContactNumber());
        model.addAttribute("eventForm", form);
        model.addAttribute("mode", "create");
        model.addAttribute("formAction", "/organizer/events");
        model.addAttribute("questionTypeOptions", com.college.events.model.QuestionType.values());
        return "organizer/event-form";
    }

    @PostMapping("/events")
    public String createEvent(@Valid @ModelAttribute("eventForm") EventForm form,
                              BindingResult binding,
                              @RequestParam(value = "action", defaultValue = "publish") String action,
                              @RequestParam(value = "posterFile", required = false) MultipartFile posterFile,
                              HttpSession session, Model model,
                              RedirectAttributes redirectAttributes) {
        Organizer organizer = currentOrganizer(session);
        if (binding.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("formAction", "/organizer/events");
            model.addAttribute("questionTypeOptions", com.college.events.model.QuestionType.values());
            return "organizer/event-form";
        }
        try {
            String uploaded = storePoster(posterFile);
            if (uploaded != null) {
                form.setPosterUrl(uploaded);
            }
            boolean publish = "publish".equalsIgnoreCase(action);
            Event event = eventService.create(form, organizer, publish);
            redirectAttributes.addFlashAttribute("successMessage", publish
                    ? "Event \"" + event.getName() + "\" has been published and is now visible to eligible students."
                    : "Event \"" + event.getName() + "\" has been saved as a draft. Publish it when you are ready.");
            return "redirect:/organizer/events";
        } catch (BusinessException ex) {
            model.addAttribute("formError", ex.getMessage());
            model.addAttribute("mode", "create");
            model.addAttribute("formAction", "/organizer/events");
            model.addAttribute("questionTypeOptions", com.college.events.model.QuestionType.values());
            return "organizer/event-form";
        } catch (Exception ex) {
            model.addAttribute("formError", "Unable to create event: " + (ex.getMessage() != null ? ex.getMessage() : "Unexpected error"));
            model.addAttribute("mode", "create");
            model.addAttribute("formAction", "/organizer/events");
            model.addAttribute("questionTypeOptions", com.college.events.model.QuestionType.values());
            return "organizer/event-form";
        }
    }

    @GetMapping("/events/{id}/edit")
    public String editEventForm(@PathVariable Long id, HttpSession session, Model model) {
        Organizer organizer = currentOrganizer(session);
        Event event = eventService.requireOwned(id, organizer);
        if (!model.containsAttribute("eventForm")) {
            model.addAttribute("eventForm", toForm(event));
        }
        model.addAttribute("event", event);
        model.addAttribute("mode", "edit");
        model.addAttribute("formAction", "/organizer/events/" + id);
        model.addAttribute("questionTypeOptions", com.college.events.model.QuestionType.values());
        return "organizer/event-form";
    }

    @PostMapping("/events/{id}")
    public String updateEvent(@PathVariable Long id,
                              @Valid @ModelAttribute("eventForm") EventForm form,
                              BindingResult binding,
                              @RequestParam(value = "posterFile", required = false) MultipartFile posterFile,
                              HttpSession session, Model model,
                              RedirectAttributes redirectAttributes) {
        Organizer organizer = currentOrganizer(session);
        Event event = eventService.requireOwned(id, organizer);

        if (binding.hasErrors()) {
            model.addAttribute("event", event);
            model.addAttribute("mode", "edit");
            model.addAttribute("formAction", "/organizer/events/" + id);
            model.addAttribute("questionTypeOptions", com.college.events.model.QuestionType.values());
            return "organizer/event-form";
        }
        try {
            String uploaded = storePoster(posterFile);
            if (uploaded != null) {
                form.setPosterUrl(uploaded);
            }
            eventService.update(id, form, organizer);
            redirectAttributes.addFlashAttribute("successMessage", "Event details updated successfully.");
            return "redirect:/organizer/events/" + id + "/edit";
        } catch (BusinessException ex) {
            model.addAttribute("formError", ex.getMessage());
            model.addAttribute("event", event);
            model.addAttribute("mode", "edit");
            model.addAttribute("formAction", "/organizer/events/" + id);
            model.addAttribute("questionTypeOptions", com.college.events.model.QuestionType.values());
            return "organizer/event-form";
        } catch (Exception ex) {
            model.addAttribute("formError", "Unable to update event: " + (ex.getMessage() != null ? ex.getMessage() : "Unexpected error"));
            model.addAttribute("event", event);
            model.addAttribute("mode", "edit");
            model.addAttribute("formAction", "/organizer/events/" + id);
            model.addAttribute("questionTypeOptions", com.college.events.model.QuestionType.values());
            return "organizer/event-form";
        }
    }

    @PostMapping("/events/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam String action,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        Organizer organizer = currentOrganizer(session);
        try {
            switch (action) {
                case "publish" -> {
                    eventService.publish(id, organizer);
                    redirectAttributes.addFlashAttribute("successMessage", "Event published.");
                }
                case "draft" -> {
                    eventService.saveAsDraft(id, organizer);
                    redirectAttributes.addFlashAttribute("successMessage", "Event moved back to draft.");
                }
                case "cancel" -> {
                    eventService.cancelEvent(id, organizer);
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Event cancelled. Registered students will see it as cancelled.");
                }
                case "close" -> {
                    eventService.closeRegistrations(id, organizer);
                    redirectAttributes.addFlashAttribute("successMessage", "Registrations closed for this event.");
                }
                case "reopen" -> {
                    eventService.reopenRegistrations(id, organizer);
                    redirectAttributes.addFlashAttribute("successMessage", "Registrations re-opened until the event date.");
                }
                default -> throw new BusinessException("Unknown action: " + action);
            }
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/organizer/events";
    }

    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Organizer organizer = currentOrganizer(session);
        try {
            Event event = eventService.requireOwned(id, organizer);
            eventService.deleteEvent(id, organizer);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Event \"" + event.getName() + "\" and its registrations were deleted.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/organizer/events";
    }

    // ------------------------------------------------------------------
    // Custom registration questions
    // ------------------------------------------------------------------

    @GetMapping("/events/{id}/questions")
    public String questionsPage(@PathVariable Long id, HttpSession session, Model model) {
        Organizer organizer = currentOrganizer(session);
        Event event = eventService.requireOwned(id, organizer);
        if (!model.containsAttribute("questionForm")) {
            model.addAttribute("questionForm", toQuestionForm(event));
        }
        model.addAttribute("event", event);
        model.addAttribute("questionTypeOptions", com.college.events.model.QuestionType.values());
        return "organizer/questions";
    }

    @PostMapping("/events/{id}/questions")
    public String saveQuestions(@PathVariable Long id,
                                @ModelAttribute("questionForm") QuestionForm form,
                                HttpSession session, Model model,
                                RedirectAttributes redirectAttributes) {
        Organizer organizer = currentOrganizer(session);
        Event event = eventService.requireOwned(id, organizer);
        try {
            eventService.replaceQuestions(id, organizer, form);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration questions saved. They now appear on the student registration form.");
            return "redirect:/organizer/events/" + id + "/questions";
        } catch (BusinessException ex) {
            model.addAttribute("formError", ex.getMessage());
            model.addAttribute("event", event);
            model.addAttribute("questionTypeOptions", com.college.events.model.QuestionType.values());
            return "organizer/questions";
        }
    }

    // ------------------------------------------------------------------
    // Participants
    // ------------------------------------------------------------------

    @GetMapping("/events/{id}/participants")
    public String participants(@PathVariable Long id, HttpSession session, Model model) {
        Organizer organizer = currentOrganizer(session);
        Event event = eventService.requireOwned(id, organizer);
        List<EventRegistration> registrations = registrationService.registrationsForEvent(id);

        model.addAttribute("event", event);
        model.addAttribute("registrations", registrations);
        model.addAttribute("confirmedCount", registrations.stream()
                .filter(r -> r.getStatus() != com.college.events.model.RegistrationStatus.CANCELLED)
                .count());
        model.addAttribute("restrictionSummary", eligibilityService.describeRestrictions(event));
        return "organizer/participants";
    }

    @GetMapping("/events/{id}/participants/{registrationId}")
    public String participantDetail(@PathVariable Long id, @PathVariable Long registrationId,
                                    HttpSession session, Model model) {
        Organizer organizer = currentOrganizer(session);
        Event event = eventService.requireOwned(id, organizer);
        EventRegistration registration = registrationService.registrationsForEvent(id).stream()
                .filter(r -> r.getId().equals(registrationId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("That registration could not be found."));

        model.addAttribute("event", event);
        model.addAttribute("registration", registration);
        return "organizer/participant-detail";
    }

    // ------------------------------------------------------------------
    // Profile
    // ------------------------------------------------------------------

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Organizer organizer = currentOrganizer(session);
        model.addAttribute("eventCount", eventService.organizerEvents(organizer.getId()).size());
        return "organizer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam(required = false) String clubOrDepartment,
                                @RequestParam(required = false) String designation,
                                @RequestParam(required = false) String contactNumber,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        Organizer organizer = currentOrganizer(session);
        try {
            authService.updateProfile(organizer.getId(), fullName, email, null, null, null, null,
                    contactNumber, clubOrDepartment, designation);
            redirectAttributes.addFlashAttribute("successMessage", "Your organizer profile has been updated.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/organizer/profile";
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Organizer currentOrganizer(HttpSession session) {
        return authService.requireOrganizer((Long) session.getAttribute(SessionKeys.USER_ID));
    }

    /** Event -> form, used to pre-fill the edit screen. */
    private EventForm toForm(Event event) {
        EventForm form = new EventForm();
        form.setName(event.getName());
        form.setDescription(event.getDescription());
        form.setCategory(event.getCategory());
        form.setPosterUrl(event.getPosterUrl());
        form.setEventDate(event.getEventDate());
        form.setStartTime(event.getStartTime());
        form.setDurationMinutes(event.getDurationMinutes());
        form.setRegistrationOpenDate(event.getRegistrationOpenDate());
        form.setRegistrationDeadline(event.getRegistrationDeadline());
        form.setVenue(event.getVenue());
        form.setBuilding(event.getBuilding());
        form.setRoomNumber(event.getRoomNumber());
        form.setMaxParticipants(event.getMaxParticipants());
        form.setTeamEvent(event.isTeamEvent());
        form.setTeamSize(event.getTeamSize());
        form.setOrganizerName(event.getOrganizerName());
        form.setClubOrDepartment(event.getClubOrDepartment());
        form.setOrganizerContact(event.getOrganizerContact());
        form.setInstructions(event.getInstructions());
        form.setEligibilityRequirements(event.getEligibilityRequirements());
        form.setPrerequisites(event.getPrerequisites());

        if (event.getEligibility() != null) {
            form.setVisibleToAll(event.getEligibility().isVisibleToAll());
            form.setDepartments(new ArrayList<>(event.getEligibility().getDepartments()));
            form.setYears(new ArrayList<>(event.getEligibility().getYears()));
            form.setSections(new ArrayList<>(event.getEligibility().getSections()));
        }
        for (RegistrationQuestion question : event.getQuestions()) {
            form.getQuestionText().add(question.getQuestionText());
            form.getQuestionType().add(question.getType().name());
            form.getQuestionRequired().add(String.valueOf(question.isRequired()));
            form.getQuestionOptions().add(String.join("\n", question.getOptions()));
        }
        return form;
    }

    private QuestionForm toQuestionForm(Event event) {
        QuestionForm form = new QuestionForm();
        for (RegistrationQuestion question : event.getQuestions()) {
            form.getQuestionText().add(question.getQuestionText());
            form.getQuestionType().add(question.getType().name());
            form.getQuestionRequired().add(String.valueOf(question.isRequired()));
            form.getQuestionOptions().add(String.join("\n", question.getOptions()));
        }
        return form;
    }

    /**
     * Stores an uploaded poster:
     * 1. Reads the raw bytes directly to avoid container stream-consumption issues.
     * 2. Saves a local backup copy under {@code uploads/} if disk is writable.
     * 3. Scales and optimizes image to compact JPEG for persistent storage in Supabase PostgreSQL
     *    as a Base64 Data URI, which survives container redeploys and restarts without ephemeral data loss.
     */
    private String storePoster(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String cleaned = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "poster" : file.getOriginalFilename());
        String extension = cleaned.contains(".")
                ? cleaned.substring(cleaned.lastIndexOf('.')).toLowerCase(Locale.ROOT) : "";

        if (!ALLOWED_POSTER_EXTENSIONS.contains(extension)) {
            throw new BusinessException("The event poster must be a PNG, JPG, WEBP or GIF image.");
        }
        try {
            // Read bytes directly from MultipartFile first
            byte[] imageBytes = file.getBytes();
            if (imageBytes == null || imageBytes.length == 0) {
                return null;
            }

            // Local disk backup
            try {
                Path directory = Paths.get("uploads").toAbsolutePath();
                Files.createDirectories(directory);
                String fileName = "poster-" + UUID.randomUUID() + extension;
                Files.write(directory.resolve(fileName), imageBytes);
            } catch (Exception ignored) {
                // Ignore disk write errors in read-only / ephemeral container runtimes
            }

            // Optimize & encode into a persistent Base64 Data URI
            String mimeType = file.getContentType() != null && !file.getContentType().isBlank()
                    ? file.getContentType() : "image/jpeg";

            try {
                BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
                if (original != null) {
                    int maxDimension = 1000;
                    int width = original.getWidth();
                    int height = original.getHeight();

                    int targetWidth = width;
                    int targetHeight = height;
                    if (width > maxDimension || height > maxDimension) {
                        double scale = Math.min((double) maxDimension / width, (double) maxDimension / height);
                        targetWidth = Math.max(1, (int) Math.round(width * scale));
                        targetHeight = Math.max(1, (int) Math.round(height * scale));
                    }

                    BufferedImage rgbImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = rgbImage.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Fill background white in case original had PNG transparency
                    g2d.setColor(java.awt.Color.WHITE);
                    g2d.fillRect(0, 0, targetWidth, targetHeight);
                    g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
                    g2d.dispose();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(rgbImage, "jpeg", baos);
                    byte[] compressed = baos.toByteArray();
                    if (compressed != null && compressed.length > 0) {
                        imageBytes = compressed;
                        mimeType = "image/jpeg";
                    }
                }
            } catch (Exception ignored) {
                // Fall back to original bytes if ImageIO cannot parse
            }

            return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException ex) {
            throw new BusinessException("The poster could not be saved. Please try again.");
        }
    }
}
