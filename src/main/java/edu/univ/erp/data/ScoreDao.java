package edu.univ.erp.data;

import edu.univ.erp.domain.Score;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScoreDao {

    private final DataSource ds;

    public ScoreDao(DataSource ds) {
        this.ds = ds;
    }

    // --------------------------------------------------
    // Get all scores for a given assessment
    // --------------------------------------------------
    public List<Score> getScoresByAssessment(int assessmentId) throws SQLException {
        String sql = """
            SELECT assessment_id, student_id, marks_obtained
            FROM scores
            WHERE assessment_id = ?
            ORDER BY student_id
        """;

        List<Score> list = new ArrayList<>();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, assessmentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // --------------------------------------------------
    // Get all scores for a specific student
    // --------------------------------------------------
    public List<Score> getScoresByStudent(String studentId) throws SQLException {
        String sql = """
            SELECT assessment_id, student_id, marks_obtained
            FROM scores
            WHERE student_id = ?
            ORDER BY assessment_id
        """;

        List<Score> list = new ArrayList<>();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // --------------------------------------------------
    // Get score for a specific student + assessment
    // --------------------------------------------------
    public Score getScore(int assessmentId, String studentId) throws SQLException {
        String sql = """
            SELECT assessment_id, student_id, marks_obtained
            FROM scores
            WHERE assessment_id = ? AND student_id = ?
        """;

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, assessmentId);
            ps.setString(2, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // --------------------------------------------------
    // UPSERT SCORE (Insert or Update)
    // --------------------------------------------------
    public void upsertScore(Score s) throws SQLException {
        String sql = """
            INSERT INTO scores (assessment_id, student_id, marks_obtained)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained)
        """;

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, s.getAssessmentId());
            ps.setString(2, s.getStudentId());
            ps.setDouble(3, s.getMarksObtained());

            ps.executeUpdate();
        }
    }

    // --------------------------------------------------
    // Bulk upsert (for InstructorDashboard)
    // --------------------------------------------------
    public void upsertScores(List<Score> scores) throws SQLException {
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            String sql = """
                INSERT INTO scores (assessment_id, student_id, marks_obtained)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained)
            """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Score s : scores) {
                    ps.setInt(1, s.getAssessmentId());
                    ps.setString(2, s.getStudentId());
                    ps.setDouble(3, s.getMarksObtained());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
        }
    }

    // --------------------------------------------------
    // Delete all scores for an assessment (if assessment deleted)
    // --------------------------------------------------
    public void deleteByAssessment(int assessmentId) throws SQLException {
        String sql = "DELETE FROM scores WHERE assessment_id = ?";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, assessmentId);
            ps.executeUpdate();
        }
    }

    // --------------------------------------------------
    // Helper: map ResultSet → Score
    // --------------------------------------------------
    private Score mapRow(ResultSet rs) throws SQLException {
        return new Score(
                rs.getInt("assessment_id"),
                rs.getString("student_id"),
                rs.getDouble("marks_obtained")
        );
    }
}
