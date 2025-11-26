package edu.univ.erp.data;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import edu.univ.erp.util.DataSourceProvider;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnrollmentDao {
    private final DataSource ds;

    public EnrollmentDao(DataSource ds) {
        this.ds = ds;
    }

    // ---------------------------------------------------------
    // Check if a student is already enrolled in a section
    // ---------------------------------------------------------
    public boolean isAlreadyEnrolled(String studentId, int sectionId) throws SQLException {
        String sql = """
            SELECT 1 FROM enrollments
            WHERE student_id = ? AND section_id = ? AND status = 'registered'
        """;

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ps.setInt(2, sectionId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ---------------------------------------------------------
    // Register a student
    // ---------------------------------------------------------
    public void register(String studentId, int sectionId) throws SQLException {
        String sql = """
            INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'registered')
                                        ON DUPLICATE KEY UPDATE
                                            status = 'registered';
                                        
        """;

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ps.setInt(2, sectionId);
            ps.executeUpdate();
        }
    }

    // ---------------------------------------------------------
    // Drop a student (soft delete: status = dropped)
    // ---------------------------------------------------------
    public void drop(String studentId, int sectionId) throws SQLException {
        String sql = """
            UPDATE enrollments
            SET status = 'dropped'
            WHERE student_id = ? AND section_id = ? AND status = 'registered'
        """;

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ps.setInt(2, sectionId);
            ps.executeUpdate();
        }
    }

    // ---------------------------------------------------------
    // Get all active enrollments for a student
    // ---------------------------------------------------------
    public List<Enrollment> getEnrollmentsForStudent(String studentId) throws SQLException {
        String sql = """
            SELECT enrollment_id, student_id, section_id, status
            FROM enrollments
            WHERE student_id = ? AND status = 'registered'
        """;

        List<Enrollment> list = new ArrayList<>();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    // ---------------------------------------------------------
    // List all enrolled students in a section (Instructor use)
    // ---------------------------------------------------------
    public List<Student> getEnrolledStudents(int sectionId) throws SQLException {

        String sql = """
        SELECT stu.user_id, stu.roll_no, stu.program, stu.year
        FROM enrollments e
        JOIN students stu ON e.student_id = stu.user_id
        WHERE e.section_id = ? AND e.status = 'registered'
        ORDER BY stu.roll_no
    """;

        List<Student> list = new ArrayList<>();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);

            try (ResultSet rs = ps.executeQuery()) {

                AuthDao authDao = new AuthDao(DataSourceProvider.getAuthDataSource());

                while (rs.next()) {
                    String userId = rs.getString("user_id");

                    // load full auth user from AUTH DB
                    User base = authDao.findByUserId(userId)
                            .orElseThrow(() -> new SQLException("Missing auth user: " + userId));

                    // build Student object
                    list.add(new Student(
                            base.getFullname(),
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
                    ));
                }
            }
        }

        return list;
    }



    // ---------------------------------------------------------
    // List all sectionIds a student is enrolled in
    // (used to join into SectionDao)
    // ---------------------------------------------------------
    public List<Integer> getActiveSectionIdsForStudent(String studentId) throws SQLException {
        String sql = """
            SELECT section_id
            FROM enrollments
            WHERE student_id = ? AND status = 'registered'
        """;

        List<Integer> ids = new ArrayList<>();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("section_id"));
            }
        }

        return ids;
    }

    // ---------------------------------------------------------
    // Convert from ResultSet to Enrollment
    // ---------------------------------------------------------
    private Enrollment mapRow(ResultSet rs) throws SQLException {
        return new Enrollment(
                rs.getInt("enrollment_id"),
                rs.getString("student_id"),
                rs.getInt("section_id"),
                rs.getString("status")
        );
    }
}
