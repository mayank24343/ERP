package edu.univ.erp.data;

import edu.univ.erp.domain.*;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserDao {
    private final DataSource authDS;
    private final DataSource erpDS;

    public UserDao(DataSource authDS, DataSource erpDS) {
        this.authDS = authDS;
        this.erpDS = erpDS;
    }

    // this finds a user by username and loads their specific profile like Student/Instructor
    public User findFullUserByUsername(String username) throws SQLException {
        var authDao = new AuthDao(authDS);
        var opt = authDao.findByUsername(username);

        if (opt.isEmpty()) return null;
        return enrichUser(opt.get());
    }

    // finds a user by user ID and loads their specific profile like Student/Instructor
    public User findFullUserByUserId(String userId) throws SQLException {
        var authDao = new AuthDao(authDS);
        var opt = authDao.findByUserId(userId);

        if (opt.isEmpty()) return null;
        return enrichUser(opt.get());
    }

    // this is a helper function that takes a basic user and decides if we need to fetch extra Student or Instructor details
    private User enrichUser(User base) throws SQLException {
        switch (base.getRole().toLowerCase()) {
            case "student":
                return loadStudent(base);
            case "instructor":
                return loadInstructor(base);
            case "admin":
                return new Admin(
                    base.getFullname(), base.getUserId(), base.getUsername(), base.getRole(),
                    base.getPasswordHash(), base.getStatus(), base.getFailedAttempts(),
                    base.getLockedUntil(), base.getLastLogin()
                );
            default:
                throw new SQLException("Unknown role: " + base.getRole());
        }
    }

    // fetches extra academic details (roll no, program) for a student
    private Student loadStudent(User base) throws SQLException {
        var sql = "SELECT roll_no, program, year FROM students WHERE user_id = ?";

        try (var conn = erpDS.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, base.getUserId());
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Student profile missing for user_id: " + base.getUserId());

                return new Student(
                    base.getFullname(), base.getUserId(), base.getUsername(), base.getRole(),
                    base.getPasswordHash(), base.getStatus(), base.getFailedAttempts(),
                    base.getLockedUntil(), base.getLastLogin(),
                    rs.getString("roll_no"), rs.getString("program"), rs.getInt("year")
                );
            }
        }
    }

    // fetches extra professional details (department + designation) for an instructor
    private Instructor loadInstructor(User base) throws SQLException {
        var sql = "SELECT department, designation FROM instructors WHERE user_id = ?";

        try (var conn = erpDS.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, base.getUserId());
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Instructor profile missing for user_id: " + base.getUserId());

                return new Instructor(
                    base.getFullname(), base.getUserId(), base.getUsername(), base.getRole(),
                    base.getPasswordHash(), base.getStatus(), base.getFailedAttempts(),
                    base.getLockedUntil(), base.getLastLogin(),
                    rs.getString("department"), rs.getString("designation")
                );
            }
        }
    }

    // returns a list of all users from the authentication database
    public List<User> findAllUsers() throws SQLException {
        var list = new ArrayList<User>();
        var sql = "SELECT * FROM auth_users";

        try (var conn = authDS.getConnection(); var ps = conn.prepareStatement(sql); var rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapUser(rs));
        }
        return list;
    }

    // filters all users to find only instructors
    // then loads their full details
    public List<Instructor> findAllInstructors() throws SQLException {
        var list = new ArrayList<Instructor>();
        for (User user : findAllUsers()) {
            if (Objects.equals(user.getRole(), "instructor")) {
                list.add(loadInstructor(user));
            }
        }
        return list;
    }

    // this is a helper function that maps a database row to a basic user object
    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getString("full_name"), rs.getString("user_id"), rs.getString("username"),
            rs.getString("role"), rs.getString("password_hash"), rs.getString("status"),
            rs.getInt("failed_attempts"), rs.getTimestamp("locked_until"), rs.getTimestamp("last_login")
        );
    }
}
