package edu.univ.erp.data;

import edu.univ.erp.domain.Score;
import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

public class ScoreDao {
    private final DataSource ds;
    public ScoreDao(DataSource ds) {
        this.ds = ds;
    }

    // finds a specific score record for one student on one assessment
    public Score getScore(int assessmentId, String studentId) throws SQLException {
        var sql = "SELECT assessment_id, student_id, marks_obtained FROM scores WHERE assessment_id = ? AND student_id = ?";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assessmentId);
            ps.setString(2, studentId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null; // Return null if no score found
    }

    // bulk saves a list of scores efficiently 
    // used for Dashboards
    public void upsertScores(List<Score> scores) throws SQLException {
        var sql = "INSERT INTO scores (assessment_id, student_id, marks_obtained) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained)";

        try (var conn = ds.getConnection()) {
            conn.setAutoCommit(false); // Turn off auto-save to group operations

            try (var ps = conn.prepareStatement(sql)) {
                for (Score s : scores) {
                    ps.setInt(1, s.getAssessmentId());
                    ps.setString(2, s.getStudentId());
                    ps.setDouble(3, s.getMarksObtained());
                    ps.addBatch(); // Add to the batch queue
                }
                ps.executeBatch(); // Send all at once
            }
            conn.commit(); // Save changes
        }
    }

    // deletes all scores related to a specific assessment
    public void deleteByAssessment(int assessmentId) throws SQLException {
        var sql = "DELETE FROM scores WHERE assessment_id = ?";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assessmentId);
            ps.executeUpdate();
        }
    }

    // this is a helper function to map database results to a score object
    private Score mapRow(ResultSet rs) throws SQLException {
        return new Score(
            rs.getInt("assessment_id"),
            rs.getString("student_id"),
            rs.getDouble("marks_obtained")
        );
    }
}
