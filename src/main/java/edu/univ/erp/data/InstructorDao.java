package edu.univ.erp.data;

import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstructorDao {
    private final DataSource ds;

    public InstructorDao(DataSource ds) {
        this.ds = ds;
    }

    public void insertInstructor(String userId, String department, String designation)
            throws SQLException {

        String sql = """
            INSERT INTO instructors (user_id, department, designation)
            VALUES (?, ?, ?)
        """;

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setString(2, department);
            ps.setString(3, designation);
            ps.executeUpdate();
        }
    }
}
