package edu.univ.erp.auth;

import java.sql.Timestamp;

public class AuthUser {
    public final String userId;
    public final String username;
    public final String role;
    public final String passwordHash;
    public final String status;
    public final int failedAttempts;
    public final Timestamp lockedUntil;
    public final Timestamp lastLogin;

    public AuthUser(String userId, String username, String role, String passwordHash, String status, int failedAttempts, Timestamp lockedUntil, Timestamp lastLogin) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.passwordHash = passwordHash;
        this.status = status;
        this.failedAttempts = failedAttempts;
        this.lockedUntil = lockedUntil;
        this.lastLogin = lastLogin;
    }
}
