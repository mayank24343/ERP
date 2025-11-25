package edu.univ.erp.data;

import edu.univ.erp.domain.Enrollment;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class EnrollmentDao {

    private final DataSource ds;

    public EnrollmentDao(DataSource ds) {
        this.ds = ds;
    }

    // ----------------------------
    // Basic getters
    // ----------------------------
    public Enrollment getEnrollment(int enrollmentId) throws SQLException {
        String sql = "SELECT * FROM enrollments WHERE enrollment_id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, enrollmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Enrollment(
                        rs.getInt("enrollment_id"),
                        rs.getString("student_id"),
                        rs.getInt("section_id"),
                        rs.getString("status")
                );
            }
            return null;
        }
    }

    // ----------------------------
    // Registration helpers
    // ----------------------------
    public boolean isAlreadyRegistered(String studentId, int sectionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND section_id = ? AND status='registered'";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ps.setInt(2, sectionId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public int countRegistered(int sectionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status='registered'";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public void createEnrollment(String studentId, int sectionId) throws SQLException {
        String sql = "INSERT INTO enrollments(student_id, section_id, status) VALUES (?, ?, 'registered')";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ps.setInt(2, sectionId);
            ps.executeUpdate();
        }
    }

    public void markDropped(int enrollmentId) throws SQLException {
        String sql = "UPDATE enrollments SET status='dropped' WHERE enrollment_id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ps.executeUpdate();
        }
    }

    // ----------------------------
    // Prerequisite checker
    // ----------------------------
    public boolean hasCompletedCourse(String studentId, int courseId) throws SQLException {
        String sql =
                "SELECT COUNT(*) FROM enrollments e " +
                        "JOIN sections s ON e.section_id = s.section_id " +
                        "WHERE e.student_id = ? AND s.course_id = ? AND e.status='completed'";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ps.setInt(2, courseId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    // ----------------------------
    // For StudentDashboard: list enrollments with section info
    // ----------------------------
    public List<Map<String,Object>> getEnrollmentsWithSectionData(String studentId) throws SQLException {
        String sql =
                "SELECT e.enrollment_id, e.section_id, e.status " +
                        "FROM enrollments e WHERE e.student_id = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            List<Map<String,Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String,Object> row = new HashMap<>();
                row.put("enrollmentId", rs.getInt("enrollment_id"));
                row.put("sectionId", rs.getInt("section_id"));
                row.put("status", rs.getString("status"));
                list.add(row);
            }
            return list;
        }
    }

    // ----------------------------
    // Transcript rows
    // ----------------------------
    public List<Map<String,Object>> getTranscript(String studentId) throws SQLException {
        String sql =
                "SELECT c.code, c.title, c.credits, g.final_grade " +
                        "FROM grades g " +
                        "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                        "JOIN sections s ON e.section_id = s.section_id " +
                        "JOIN courses c ON s.course_id = c.course_id " +
                        "WHERE e.student_id = ? AND e.status='completed'";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            List<Map<String,Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String,Object> row = new HashMap<>();
                row.put("code", rs.getString("code"));
                row.put("title", rs.getString("title"));
                row.put("credits", rs.getInt("credits"));
                row.put("grade", rs.getString("final_grade"));
                list.add(row);
            }
            return list;
        }
    }

    // ----------------------------
    // Instructor: List students in a section
    // ----------------------------
    public List<Map<String,Object>> getStudentsInSection(int sectionId) throws SQLException {
        String sql =
                "SELECT enrollment_id, student_id, status " +
                        "FROM enrollments WHERE section_id = ?";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();

            List<Map<String,Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String,Object> row = new HashMap<>();
                row.put("enrollmentId", rs.getInt("enrollment_id"));
                row.put("studentId", rs.getString("student_id"));
                row.put("status", rs.getString("status"));
                list.add(row);
            }

            return list;
        }
    }
}
