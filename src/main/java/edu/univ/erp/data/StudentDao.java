package edu.univ.erp.data;

import edu.univ.erp.domain.Student;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

public class StudentDao {

    private final DataSource ds;

    public StudentDao(DataSource ds) {
        this.ds = ds;
    }

    public void insertStudent(String userId, String rollNo, String program, int year)
            throws SQLException {

        String sql = """
            INSERT INTO students (user_id, roll_no, program, year)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setString(2, rollNo);
            ps.setString(3, program);
            ps.setInt(4, year);
            ps.executeUpdate();
        }
    }

}
