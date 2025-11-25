package edu.univ.erp.data;

import edu.univ.erp.domain.*;

import javax.sql.DataSource;
import java.sql.*;

public class UserDao {

    private final DataSource authDS;
    private final DataSource erpDS;

    //constructor
    public UserDao(DataSource authDS, DataSource erpDS) {
        this.authDS = authDS;
        this.erpDS = erpDS;
    }

    //return the user of required type for the dashboard
    public User findFullUserByUsername(String username) throws SQLException {

        AuthDao authDao = new AuthDao(authDS);
        var opt = authDao.findByUsername(username);

        if (opt.isEmpty()) return null;

        User base = opt.get();

        switch (base.getRole().toLowerCase()) {
            case "student":
                return loadStudent(base);
            case "instructor":
                return loadInstructor(base);
            case "admin":
                return new Admin(base.getUserId(),
                        base.getUsername(),
                        base.getRole(),
                        base.getPasswordHash(),
                        base.getStatus(),
                        base.getFailedAttempts(),
                        base.getLockedUntil(),
                        base.getLastLogin());
            default:
                throw new SQLException("Unknown role: " + base.getRole());
        }
    }

    //check student table for user and return student object if found
    private Student loadStudent(User base) throws SQLException {
        String sql = "SELECT roll_no, program, year FROM students WHERE user_id = ?";

        try (Connection conn = erpDS.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, base.getUserId());
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                throw new SQLException("Student profile missing for user_id: " + base.getUserId());

            return new Student(
                    base.getUserId(),
                    base.getUsername(),
                    base.getRole(),
                    base.getPasswordHash(),
                    base.getStatus(),
                    base.getFailedAttempts(),
                    base.getLockedUntil(),
                    base.getLastLogin(),
                    rs.getString("roll_no"),
                    rs.getString("program"),
                    rs.getInt("year")
            );
        }
    }

    //check instructor table for user & return instructor object if found
    private Instructor loadInstructor(User base) throws SQLException {
        String sql = "SELECT department, designation FROM instructors WHERE user_id = ?";

        try (Connection conn = erpDS.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, base.getUserId());
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                throw new SQLException("Instructor profile missing for user_id: " + base.getUserId());

            return new Instructor(
                    base.getUserId(),
                    base.getUsername(),
                    base.getRole(),
                    base.getPasswordHash(),
                    base.getStatus(),
                    base.getFailedAttempts(),
                    base.getLockedUntil(),
                    base.getLastLogin(),
                    rs.getString("department"),
                    rs.getString("designation")
            );
        }
    }

}
