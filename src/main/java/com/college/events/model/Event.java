package com.college.events.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * An event created by an organizer.
 *
 * <p>Field names intentionally mirror the organizer "Create event" form so the
 * same object can be persisted to a database later without translation.</p>
 */
@Entity
@Table(name = "events")
public class Event {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Basic information ---
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String category;
    private String posterUrl;

    // --- Scheduling ---
    private LocalDate eventDate;
    private LocalTime startTime;
    private Integer durationMinutes;
    private LocalDate registrationOpenDate;
    private LocalDate registrationDeadline;

    // --- Location ---
    private String venue;
    private String building;
    private String roomNumber;

    // --- Participation ---
    private Integer maxParticipants;
    private boolean teamEvent;
    private Integer teamSize;

    // --- Organizer information ---
    private String organizerName;
    private String clubOrDepartment;
    private String organizerContact;

    // --- Additional information ---
    @Column(columnDefinition = "TEXT")
    private String instructions;
    @Column(columnDefinition = "TEXT")
    private String eligibilityRequirements;
    @Column(columnDefinition = "TEXT")
    private String prerequisites;

    // --- Bookkeeping ---
    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.DRAFT;
    private Long organizerId;
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "eligibility_id")
    private EventEligibility eligibility = new EventEligibility();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id")
    @OrderBy("orderIndex ASC")
    private List<RegistrationQuestion> questions = new ArrayList<>();

    /**
     * Transient seat counter, filled in by {@code EventService} when events are
     * listed so templates can render "seats left" without extra queries.
     */
    @Transient
    private int registeredCount;

    public Event() {
    }

    // ------------------------------------------------------------------
    // Derived values
    // ------------------------------------------------------------------

    public LocalDateTime getStartDateTime() {
        if (eventDate == null) {
            return null;
        }
        return eventDate.atTime(startTime == null ? LocalTime.MIDNIGHT : startTime);
    }

    public LocalDateTime getEndDateTime() {
        LocalDateTime start = getStartDateTime();
        if (start == null) {
            return null;
        }
        return start.plusMinutes(durationMinutes == null ? 60 : durationMinutes);
    }

    /** Upcoming / Ongoing / Completed, or the organizer driven Draft / Cancelled state. */
    public String getStatusLabel() {
        if (status == EventStatus.CANCELLED) {
            return "Cancelled";
        }
        if (status == EventStatus.DRAFT) {
            return "Draft";
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = getStartDateTime();
        LocalDateTime end = getEndDateTime();
        if (start == null) {
            return "Upcoming";
        }
        if (now.isBefore(start)) {
            return "Upcoming";
        }
        if (end != null && now.isAfter(end)) {
            return "Completed";
        }
        return "Ongoing";
    }

    public String getStatusCssClass() {
        return "status-" + getStatusLabel().toLowerCase();
    }

    public boolean isCompleted() {
        return "Completed".equals(getStatusLabel());
    }

    public boolean isCancelled() {
        return status == EventStatus.CANCELLED;
    }

    public boolean isPublished() {
        return status == EventStatus.PUBLISHED;
    }

    public boolean isDraft() {
        return status == EventStatus.DRAFT;
    }

    public int getSeatsLeft() {
        if (maxParticipants == null) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, maxParticipants - registeredCount);
    }

    public boolean isFull() {
        return maxParticipants != null && registeredCount >= maxParticipants;
    }

    public int getFillPercent() {
        if (maxParticipants == null || maxParticipants == 0) {
            return 0;
        }
        return Math.min(100, (int) Math.round(registeredCount * 100.0 / maxParticipants));
    }

    /** True when the registration window is currently open and seats remain. */
    public boolean isRegistrationOpen() {
        if (status != EventStatus.PUBLISHED || isFull()) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (registrationOpenDate != null && today.isBefore(registrationOpenDate)) {
            return false;
        }
        if (registrationDeadline != null && today.isAfter(registrationDeadline)) {
            return false;
        }
        return true;
    }

    /** Human readable explanation shown instead of the register button. */
    public String getRegistrationClosedReason() {
        if (isCancelled()) {
            return "This event has been cancelled by the organizer.";
        }
        if (isDraft()) {
            return "This event has not been published yet.";
        }
        LocalDate today = LocalDate.now();
        if (registrationOpenDate != null && today.isBefore(registrationOpenDate)) {
            return "Registrations open on " + registrationOpenDate.format(DATE_FMT) + ".";
        }
        if (isFull()) {
            return "Registration is full - all " + maxParticipants + " seats have been taken.";
        }
        if (registrationDeadline != null && today.isAfter(registrationDeadline)) {
            return "The registration deadline (" + registrationDeadline.format(DATE_FMT) + ") has passed.";
        }
        return "Registrations are currently closed.";
    }

    public String getEventDateLabel() {
        return eventDate == null ? "To be announced" : eventDate.format(DATE_FMT);
    }

    public String getStartTimeLabel() {
        return startTime == null ? "To be announced" : startTime.format(TIME_FMT);
    }

    public String getDeadlineLabel() {
        return registrationDeadline == null ? "Not specified" : registrationDeadline.format(DATE_FMT);
    }

    public String getRegistrationOpenLabel() {
        return registrationOpenDate == null ? "Immediately" : registrationOpenDate.format(DATE_FMT);
    }

    public String getDurationLabel() {
        if (durationMinutes == null || durationMinutes <= 0) {
            return "Not specified";
        }
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append(hours == 1 ? " hour" : " hours");
        }
        if (minutes > 0) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(minutes).append(" minutes");
        }
        return sb.toString();
    }

    public String getLocationLabel() {
        StringBuilder sb = new StringBuilder();
        if (venue != null && !venue.isBlank()) {
            sb.append(venue);
        }
        if (building != null && !building.isBlank()) {
            sb.append(sb.length() > 0 ? ", " : "").append(building);
        }
        if (roomNumber != null && !roomNumber.isBlank()) {
            sb.append(sb.length() > 0 ? ", Room " : "Room ").append(roomNumber);
        }
        return sb.length() == 0 ? "To be announced" : sb.toString();
    }

    /**
     * Poster shown on cards. Uploaded posters win; otherwise a deterministic
     * placeholder poster is generated from the event name and category.
     */
    public String getPosterUrlOrDefault() {
        if (posterUrl != null && !posterUrl.isBlank()) {
            return posterUrl;
        }
        String prompt = "modern minimalist college event poster for " + getCategory()
                + ", title theme " + safe(name)
                + ", deep navy blue and gold colour palette, elegant, clean typography, "
                + "professional academic design";
        return "https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt="
                + URLEncoder.encode(prompt, StandardCharsets.UTF_8)
                + "&image_size=landscape_4_3";
    }

    private String safe(String value) {
        return value == null ? "campus event" : value;
    }

    public String getShortDescription() {
        if (description == null) {
            return "";
        }
        String trimmed = description.strip();
        return trimmed.length() <= 140 ? trimmed : trimmed.substring(0, 137) + "...";
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public LocalDate getRegistrationOpenDate() {
        return registrationOpenDate;
    }

    public void setRegistrationOpenDate(LocalDate registrationOpenDate) {
        this.registrationOpenDate = registrationOpenDate;
    }

    public LocalDate getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(LocalDate registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public boolean isTeamEvent() {
        return teamEvent;
    }

    public void setTeamEvent(boolean teamEvent) {
        this.teamEvent = teamEvent;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getClubOrDepartment() {
        return clubOrDepartment;
    }

    public void setClubOrDepartment(String clubOrDepartment) {
        this.clubOrDepartment = clubOrDepartment;
    }

    public String getOrganizerContact() {
        return organizerContact;
    }

    public void setOrganizerContact(String organizerContact) {
        this.organizerContact = organizerContact;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getEligibilityRequirements() {
        return eligibilityRequirements;
    }

    public void setEligibilityRequirements(String eligibilityRequirements) {
        this.eligibilityRequirements = eligibilityRequirements;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public EventEligibility getEligibility() {
        return eligibility;
    }

    public void setEligibility(EventEligibility eligibility) {
        this.eligibility = eligibility;
    }

    public List<RegistrationQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<RegistrationQuestion> questions) {
        this.questions = questions;
    }

    public int getRegisteredCount() {
        return registeredCount;
    }

    public void setRegisteredCount(int registeredCount) {
        this.registeredCount = registeredCount;
    }
}
