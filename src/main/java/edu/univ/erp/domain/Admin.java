package edu.univ.erp.domain;

import java.sql.Timestamp;

public class Admin extends User {

    public Admin(
            String userId,
            String username,
            String role,
            String passwordHash,
            String status,
            int failedAttempts,
            Timestamp lockedUntil,
            Timestamp lastLogin
    ) {
        super(userId, username, role, passwordHash, status, failedAttempts, lockedUntil, lastLogin);
    }
}
