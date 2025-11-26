package edu.univ.erp.data;

import edu.univ.erp.domain.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class AuthDao {
    private final DataSource authDS;

    //constructor
    public AuthDao(DataSource authDS) {
        this.authDS = authDS;
    }

    //find & return user by username
    //using try on db connection for automatic closing
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM auth_users WHERE username = ?";

        try (Connection conn = authDS.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            return Optional.of(mapUser(rs));
        }
    }

    //find by user_id
    public Optional<User> findByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM auth_users WHERE user_id=?";

        try (Connection conn = authDS.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapUser(rs));
            }
            return Optional.empty();
        }
    }

    //increased failed login attempts
    public void incrementFailedAttempts(String userId) throws SQLException {
        String sql = "UPDATE auth_users SET failed_attempts = failed_attempts + 1 WHERE user_id = ?";

        try (Connection conn = authDS.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

    //reset failed login attempts
    public void resetFailedAttempts(String userId) throws SQLException {
        String sql = "UPDATE auth_users SET failed_attempts = 0 WHERE user_id = ?";

        try (Connection conn = authDS.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

    //lock account until timestamp
    public void lockUser(String userId, Timestamp until) throws SQLException {
        String sql = "UPDATE auth_users SET locked_until=?, status='locked' WHERE user_id=?";

        try (Connection conn = authDS.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, until);
            ps.setString(2, userId);
            ps.executeUpdate();
        }
    }

    //unlock account
    public void unlockUser(String userId) throws SQLException {
        String sql = "UPDATE auth_users SET status='active', locked_until=NULL, failed_attempts=0 WHERE user_id=?";

        try (Connection conn = authDS.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

    //update last login with current time
    public void updateLastLogin(String userId) throws SQLException {
        String sql = "UPDATE auth_users SET last_login=NOW() WHERE user_id=?";

        try (Connection conn = authDS.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

    //change password functionality
    public void changePassword(String username, String newPassHash) throws SQLException {
        String sql = "UPDATE auth_users SET password_hash=? WHERE username=?; ";
        try (Connection conn = authDS.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassHash);
            ps.setString(2, username);
            ps.executeUpdate();
        }
    }

    //add user to auth db
    public void addUser(String fullName, String username, String password, String role, String userId) throws SQLException {
        //password hash
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));

        //insert into auth DB
        String authSql = "INSERT INTO auth_users (user_id, username, role, password_hash, status, failed_attempts,full_name) " + "VALUES (?, ?, ?, ?, 'active', 0, ?)";

        try (Connection conn = authDS.getConnection(); PreparedStatement ps = conn.prepareStatement(authSql)) {
            ps.setString(1, userId);
            ps.setString(2, username);
            ps.setString(3, role);
            ps.setString(4, hash);
            ps.setString(5, fullName);
            ps.executeUpdate();
        }
    }

    //map the result of sql query to user object
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
