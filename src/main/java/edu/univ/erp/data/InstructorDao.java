package edu.univ.erp.data;

import edu.univ.erp.domain.Instructor;
import javax.sql.DataSource;
import java.sql.*;

public class InstructorDao {
    private final DataSource ds;

    public InstructorDao(DataSource ds) {
        this.ds = ds;
    }

    // it saves a new instructor's specific details (department + job title) to the database
    public void insertInstructor(String userId, String department, String designation) throws SQLException {
        var sql = "INSERT INTO instructors (user_id, department, designation) VALUES (?, ?, ?)";

        try (var c = ds.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, department);
            ps.setString(3, designation);
            ps.executeUpdate();
        }
    }
}
