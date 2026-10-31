package com.college.events.model;

/**
 * Common identity data shared by every account in the portal.
 *
 * <p>{@link Student} and {@link Organizer} extend this class, which keeps a single
 * credential table today and maps cleanly to JPA single-table inheritance later.</p>
 */
public class User {

    private Long id;
    private String fullName;
    private String email;
    private String password;
    private Role role;

    public User() {
    }

    public User(Long id, String fullName, String email, String password, Role role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isStudent() {
        return role == Role.STUDENT;
    }

    public boolean isOrganizer() {
        return role == Role.ORGANIZER;
    }

    /** First letter of the name, used for the avatar bubble. */
    public String getInitials() {
        if (fullName == null || fullName.isBlank()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(parts[0].charAt(0)));
        if (parts.length > 1) {
            sb.append(Character.toUpperCase(parts[parts.length - 1].charAt(0)));
        }
        return sb.toString();
    }
}
