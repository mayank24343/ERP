package edu.univ.erp.domain;

public abstract class User {
    private String username;
    private String userId;
    private String role;
    private int fontPreference;
    private int modePreference;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
