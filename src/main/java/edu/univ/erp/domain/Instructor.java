package edu.univ.erp.domain;

import java.sql.Timestamp;

public class Instructor extends User {
    private final String department;
    private final String designation;
    //constructor
    public Instructor(String fullName, String userId, String username, String role, String passwordHash, String status, int failedAttempts, Timestamp lockedUntil, Timestamp lastLogin, String department, String designation) {
        super(fullName,userId, username, role, passwordHash, status, failedAttempts, lockedUntil, lastLogin);
        this.department = department;
        this.designation = designation;
    }

    //getters
    public String getDepartment() {
        return department;
    }
    public String getDesignation() {
        return designation;
    }
}
