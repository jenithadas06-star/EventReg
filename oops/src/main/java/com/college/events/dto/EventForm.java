package com.college.events.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean for the organizer "Create / Edit event" form.
 *
 * <p>Custom registration questions arrive as four parallel lists (text, type,
 * required, options) which keeps the browser side simple - a dynamic row only has
 * to append one value to each list.</p>
 */
public class EventForm {

    // --- Basic information ---
    @NotBlank(message = "Event name is required")
    @Size(max = 120, message = "Event name must be 120 characters or fewer")
    private String name;

    @NotBlank(message = "Event description is required")
    private String description;

    @NotBlank(message = "Please choose an event category")
    private String category;

    private String posterUrl;

    // --- Scheduling ---
    @NotNull(message = "Event date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eventDate;

    @NotNull(message = "Starting time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "Duration is required")
    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 2880, message = "Duration cannot exceed 48 hours")
    private Integer durationMinutes;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate registrationOpenDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate registrationDeadline;

    // --- Location ---
    @NotBlank(message = "Venue is required")
    private String venue;

    private String building;
    private String roomNumber;

    // --- Participation ---
    @NotNull(message = "Maximum number of participants is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer maxParticipants;

    private boolean teamEvent;
    private Integer teamSize;

    // --- Organizer information ---
    @NotBlank(message = "Organizer name is required")
    private String organizerName;

    private String clubOrDepartment;

    @NotBlank(message = "Organizer contact information is required")
    private String organizerContact;

    // --- Additional information ---
    private String instructions;
    private String eligibilityRequirements;
    private String prerequisites;

    // --- Eligibility & visibility ---
    private boolean visibleToAll = true;
    private List<String> departments = new ArrayList<>();
    private List<String> years = new ArrayList<>();
    private List<String> sections = new ArrayList<>();

    // --- Custom registration questions (parallel lists) ---
    private List<String> questionText = new ArrayList<>();
    private List<String> questionType = new ArrayList<>();
    private List<String> questionRequired = new ArrayList<>();
    private List<String> questionOptions = new ArrayList<>();

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

    public boolean isVisibleToAll() {
        return visibleToAll;
    }

    public void setVisibleToAll(boolean visibleToAll) {
        this.visibleToAll = visibleToAll;
    }

    public List<String> getDepartments() {
        return departments;
    }

    public void setDepartments(List<String> departments) {
        this.departments = departments == null ? new ArrayList<>() : departments;
    }

    public List<String> getYears() {
        return years;
    }

    public void setYears(List<String> years) {
        this.years = years == null ? new ArrayList<>() : years;
    }

    public List<String> getSections() {
        return sections;
    }

    public void setSections(List<String> sections) {
        this.sections = sections == null ? new ArrayList<>() : sections;
    }

    public List<String> getQuestionText() {
        return questionText;
    }

    public void setQuestionText(List<String> questionText) {
        this.questionText = questionText == null ? new ArrayList<>() : questionText;
    }

    public List<String> getQuestionType() {
        return questionType;
    }

    public void setQuestionType(List<String> questionType) {
        this.questionType = questionType == null ? new ArrayList<>() : questionType;
    }

    public List<String> getQuestionRequired() {
        return questionRequired;
    }

    public void setQuestionRequired(List<String> questionRequired) {
        this.questionRequired = questionRequired == null ? new ArrayList<>() : questionRequired;
    }

    public List<String> getQuestionOptions() {
        return questionOptions;
    }

    public void setQuestionOptions(List<String> questionOptions) {
        this.questionOptions = questionOptions == null ? new ArrayList<>() : questionOptions;
    }
}
