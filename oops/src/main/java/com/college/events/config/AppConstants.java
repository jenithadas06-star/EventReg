package com.college.events.config;

import java.util.List;

/**
 * Static reference data used by both dashboards (department / year / section /
 * category pickers). Keeping it here means the same lists feed the organizer
 * restrictions UI and the student registration form, so they can never drift apart.
 */
public final class AppConstants {

    private AppConstants() {
    }

    /** A department shown as {@code CSE - Computer Science and Engineering}. */
    public static class DepartmentOption {
        private final String code;
        private final String name;

        public DepartmentOption(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getLabel() {
            return code + " - " + name;
        }
    }

    public static final List<DepartmentOption> DEPARTMENTS = List.of(
            new DepartmentOption("CSE", "Computer Science and Engineering"),
            new DepartmentOption("ECE", "Electronics and Communication Engineering"),
            new DepartmentOption("IT", "Information Technology"),
            new DepartmentOption("EEE", "Electrical and Electronics Engineering"),
            new DepartmentOption("MECH", "Mechanical Engineering"),
            new DepartmentOption("CIVIL", "Civil Engineering"),
            new DepartmentOption("AIDS", "Artificial Intelligence and Data Science"),
            new DepartmentOption("AIML", "Artificial Intelligence and Machine Learning"),
            new DepartmentOption("BT", "Biotechnology"),
            new DepartmentOption("CHEM", "Chemical Engineering"),
            new DepartmentOption("BME", "Biomedical Engineering"),
            new DepartmentOption("CYBER", "Cyber Security")
    );

    public static final List<String> YEARS = List.of(
            "First Year",
            "Second Year",
            "Third Year",
            "Fourth Year"
    );

    public static final List<String> SECTIONS = List.of("A", "B", "C", "D");

    public static final List<String> CATEGORIES = List.of(
            "Technical",
            "Cultural",
            "Sports",
            "Workshop",
            "Seminar",
            "Hackathon",
            "Club Activity",
            "Other"
    );

    public static final List<String> TEAM_SIZES = List.of("2", "3", "4", "5", "6");

    /** Full department name for a code, falling back to the code itself. */
    public static String departmentName(String code) {
        return DEPARTMENTS.stream()
                .filter(d -> d.getCode().equalsIgnoreCase(code))
                .map(DepartmentOption::getName)
                .findFirst()
                .orElse(code);
    }
}
