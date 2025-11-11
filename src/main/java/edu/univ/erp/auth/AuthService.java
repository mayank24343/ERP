package edu.univ.erp.auth;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class AuthService {
    private final AuthDao dao;
    private final int MAX_ATTEMPTS = 3;
    private final int LOCK_MINUTES = 15;

    public AuthService(DataSource ds) {
        this.dao = new AuthDao(ds);
    }

    public AuthResult authenticate(String username, String plainPassword) {
        try {
            Optional<AuthUser> maybe = dao.findByUsername(username);
            if (maybe.isEmpty()) return AuthResult.invalidCredentials();

            AuthUser u = maybe.get();

            // check status
            if (!"active".equalsIgnoreCase(u.status)) {
                return AuthResult.disabled();
            }

            // check lock
            Timestamp lockedUntil = u.lockedUntil;
            if (lockedUntil != null && lockedUntil.toInstant().isAfter(Instant.now())) {
                return AuthResult.locked(lockedUntil);
            }

            // verify password
            boolean ok = PasswordUtil.verifyPassword(plainPassword, u.passwordHash);
            if (ok) {
                // successful: reset counters
                dao.resetFailedAttempts(u.userId);
                return AuthResult.success(u.userId, u.role);
            } else {
                // failed attempt
                int newAttempts = u.failedAttempts + 1;
                Timestamp newLockedUntil = null;
                if (newAttempts >= MAX_ATTEMPTS) {
                    Instant until = Instant.now().plus(LOCK_MINUTES, ChronoUnit.MINUTES);
                    newLockedUntil = Timestamp.from(until);
                    newAttempts = MAX_ATTEMPTS; // cap
                }
                dao.incrementFailedAttempts(u.userId, newAttempts, newLockedUntil);
                if (newLockedUntil != null) return AuthResult.locked(newLockedUntil);
                else return AuthResult.invalidCredentials();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return AuthResult.error("Internal error");
        }
    }
}

