package edu.univ.erp.domain;

import java.sql.Timestamp;

public class Admin extends User {

    public Admin(
            String fullname,
            String userId,
            String username,
            String role,
            String passwordHash,
            String status,
            int failedAttempts,
            Timestamp lockedUntil,
            Timestamp lastLogin
    ) {
        super(fullname,userId, username, role, passwordHash, status, failedAttempts, lockedUntil, lastLogin);
    }
}
