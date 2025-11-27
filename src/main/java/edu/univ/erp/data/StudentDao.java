package edu.univ.erp.data;

import javax.sql.DataSource;
import java.sql.*;

public class StudentDao {
    private final DataSource ds;

    public StudentDao(DataSource ds) {
        this.ds = ds;
    }

    // saves a new student's academic details (roll no, program, year) to the database
    public void insertStudent(String userId, String rollNo, String program, int year) throws SQLException {
        var sql = "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";

        try (var c = ds.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, rollNo);
            ps.setString(3, program);
            ps.setInt(4, year);
            ps.executeUpdate();
        }
    }
}
