package edu.univ.erp.auth;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class AuthDao {
    private final DataSource ds;

    public AuthDao(DataSource ds) {
        this.ds = ds;
    }

    public Optional<AuthUser> findByUsername(String username) throws SQLException {
        String q = "SELECT user_id, username, role, password_hash, status, failed_attempts, locked_until, last_login FROM auth_users WHERE username = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                AuthUser u = new AuthUser(
                        rs.getString("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("password_hash"),
                        rs.getString("status"),
                        rs.getInt("failed_attempts"),
                        rs.getTimestamp("locked_until"),
                        rs.getTimestamp("last_login")
                );
                return Optional.of(u);
            }
        }
    }

    public void resetFailedAttempts(String userId) throws SQLException {
        String u = "UPDATE auth_users SET failed_attempts = 0, locked_until = NULL, last_login = NOW() WHERE user_id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(u)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

    public void incrementFailedAttempts(String userId, int newAttempts, Timestamp lockedUntil) throws SQLException {
        String u = "UPDATE auth_users SET failed_attempts = ?, locked_until = ? WHERE user_id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(u)) {
            ps.setInt(1, newAttempts);
            if (lockedUntil != null) ps.setTimestamp(2, lockedUntil);
            else ps.setNull(2, Types.TIMESTAMP);
            ps.setString(3, userId);
            ps.executeUpdate();
        }
    }

    // update password (and optionally add to history outside)
    public void updatePassword(String userId, String newHash) throws SQLException {
        String u = "UPDATE auth_users SET password_hash = ?, failed_attempts = 0, locked_until = NULL WHERE user_id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(u)) {
            ps.setString(1, newHash);
            ps.setString(2, userId);
            ps.executeUpdate();
        }
    }
}
