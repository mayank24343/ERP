package edu.univ.erp.domain;

import java.sql.Timestamp;

public class User {
        private final String fullname;
        private final String userId;
        private final String username;
        private final String role;
        private final String passwordHash;
        private final String status;
        private final int failedAttempts;
        private final Timestamp lockedUntil;
        private final Timestamp lastLogin;
        //constructor
        public User(String fullname, String userId, String username, String role, String passwordHash, String status, int failedAttempts, Timestamp lockedUntil, Timestamp lastLogin) {
            this.fullname = fullname;
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.passwordHash = passwordHash;
            this.status = status;
            this.failedAttempts = failedAttempts;
            this.lockedUntil = lockedUntil;
            this.lastLogin = lastLogin;
        }
        //getters
        public String getFullname() {
            return fullname;
        }

        public String getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }

        public String getStatus() { return status; }

        public String getPasswordHash() { return passwordHash; }

        public int getFailedAttempts() { return failedAttempts; }

        public Timestamp getLockedUntil() { return lockedUntil; }

        public Timestamp getLastLogin() { return lastLogin; }
}
