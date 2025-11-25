package edu.univ.erp.domain;

import java.sql.Timestamp;

public class Instructor extends User {

    private final String department;
    private final String designation;

    public Instructor(
            String userId,
            String username,
            String role,
            String passwordHash,
            String status,
            int failedAttempts,
            Timestamp lockedUntil,
            Timestamp lastLogin,

            // instructor-specific
            String department,
            String designation
    ) {
        super(userId, username, role, passwordHash, status, failedAttempts, lockedUntil, lastLogin);
        this.department = department;
        this.designation = designation;
    }

    public String getDepartment() {
        return department;
    }

    public String getDesignation() {
        return designation;
    }
}
