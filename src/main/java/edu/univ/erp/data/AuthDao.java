package edu.univ.erp.data;

import edu.univ.erp.domain.User;
import org.mindrot.jbcrypt.BCrypt;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class AuthDao {
    private final DataSource authDS;

    public AuthDao(DataSource authDS) {
        this.authDS = authDS;
    }

    // this is used to find a user in the database using their username
    public Optional<User> findByUsername(String username) throws SQLException {
        var sql = "SELECT * FROM auth_users WHERE username = ?";

        try (var conn = authDS.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                // if a row is found, map it to the user, otherwise return empty
                if (rs.next()) return Optional.of(mapUser(rs));
            }
        }
        return Optional.empty();
    }

    // this is used to find a user in the database using their user ID
    public Optional<User> findByUserId(String userId) throws SQLException {
        var sql = "SELECT * FROM auth_users WHERE user_id=?";

        try (var conn = authDS.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapUser(rs));
            }
        }
        return Optional.empty();
    }

    // adds 1 to the failed login counter for a specific user
    public void incrementFailedAttempts(String userId) throws SQLException {
        var sql = "UPDATE auth_users SET failed_attempts = failed_attempts + 1 WHERE user_id = ?";

        try (var conn = authDS.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

    // used to reset the failed login counter back to 0 
    public void resetFailedAttempts(String userId) throws SQLException {
        var sql = "UPDATE auth_users SET failed_attempts = 0 WHERE user_id = ?";

        try (var conn = authDS.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

    // it locks the user account until a specific time
    public void lockUser(String userId, Timestamp until) throws SQLException {
        var sql = "UPDATE auth_users SET locked_until=?, status='locked' WHERE user_id=?";

        try (var conn = authDS.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, until);
            ps.setString(2, userId);
            ps.executeUpdate();
        }
    }

    // it unlocks the user account and if there are any failed attempts, it clears them
    public void unlockUser(String userId) throws SQLException {
        var sql = "UPDATE auth_users SET status='active', locked_until=NULL, failed_attempts=0 WHERE user_id=?";

        try (var conn = authDS.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

    // used to update the 'last_login' timestamp to the current time
    public void updateLastLogin(String userId) throws SQLException {
        var sql = "UPDATE auth_users SET last_login=NOW() WHERE user_id=?";

        try (var conn = authDS.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

    // updates the user's password with a new hashed password
    public void changePassword(String username, String newPassHash) throws SQLException {
        var sql = "UPDATE auth_users SET password_hash=? WHERE username=?";

        try (var conn = authDS.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassHash);
            ps.setString(2, username);
            ps.executeUpdate();
        }
    }

    // creates a new user
    // hashes user's  password
    // save user and password details to the database
    public void addUser(String fullName, String username, String password, String role, String userId) throws SQLException {
        // it securely hashes the password before storing it
        var hash = BCrypt.hashpw(password, BCrypt.gensalt(12));

        var sql = "INSERT INTO auth_users (user_id, username, role, password_hash, status, failed_attempts, full_name) VALUES (?, ?, ?, ?, 'active', 0, ?)";

        try (var conn = authDS.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, username);
            ps.setString(3, role);
            ps.setString(4, hash);
            ps.setString(5, fullName);
            ps.executeUpdate();
        }
    }

    // this is a helper function to convert a database row into a User object
    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getString("full_name"),
            rs.getString("user_id"),
            rs.getString("username"),
            rs.getString("role"),
            rs.getString("password_hash"),
            rs.getString("status"),
            rs.getInt("failed_attempts"),
            rs.getTimestamp("locked_until"),
            rs.getTimestamp("last_login")
        );
    }
}
