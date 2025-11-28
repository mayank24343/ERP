package edu.univ.erp.data;

import edu.univ.erp.domain.*;
import edu.univ.erp.util.DataSourceProvider;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDao {
    private final DataSource ds;
    private final CourseDao courseDao;
    private final UserDao userDao;

    public EnrollmentDao(DataSource ds) {
        this.ds = ds;
        this.courseDao = new CourseDao(ds);
        // initialize UserDao with both data sources
        this.userDao = new UserDao(DataSourceProvider.getAuthDataSource(), DataSourceProvider.getERPDataSource());
    }

    // to check wether a student is already actively registered for a specific section
    public boolean isAlreadyEnrolled(String studentId, int sectionId) throws SQLException {
        var sql = "SELECT 1 FROM enrollments WHERE student_id = ? AND section_id = ? AND status = 'registered'";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, sectionId);
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // registers a student
    // If they were previously 'dropped', it reactivates them to 'registered'.
    public void register(String studentId, int sectionId) throws SQLException {
        var sql = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'registered') ON DUPLICATE KEY UPDATE status = 'registered'";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, sectionId);
            ps.executeUpdate();
        }
    }

    // it soft deletes a student by changing their status to 'dropped'
    public void drop(String studentId, int sectionId) throws SQLException {
        var sql = "UPDATE enrollments SET status = 'dropped' WHERE student_id = ? AND section_id = ? AND status = 'registered'";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, sectionId);
            ps.executeUpdate();
        }
    }

    // provides a list of full Student objects enrolled in a specific section 
    // combines ERP and auth data
    public List<Student> getEnrolledStudents(int sectionId) throws SQLException {
        var sql = "SELECT stu.user_id, stu.roll_no, stu.program, stu.year FROM enrollments e JOIN students stu ON e.student_id = stu.user_id WHERE e.section_id = ? ORDER BY stu.roll_no";
        var list = new ArrayList<Student>();

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (var rs = ps.executeQuery()) {
                // we need AuthDao here to fetch the login details for every student found
                var authDao = new AuthDao(DataSourceProvider.getAuthDataSource());

                while (rs.next()) {
                    String userId = rs.getString("user_id");
                    // it fetches base user details from Auth DB
                    User base = authDao.findByUserId(userId).orElseThrow(() -> new SQLException("Missing auth user: " + userId));

                    // combine Auth data with student specific data
                    list.add(new Student(
                        base.getFullname(), base.getUserId(), base.getUsername(), base.getRole(),
                        base.getPasswordHash(), base.getStatus(), base.getFailedAttempts(),
                        base.getLockedUntil(), base.getLastLogin(),
                        rs.getString("roll_no"), rs.getString("program"), rs.getInt("year")
                    ));
                }
            }
        }
        return list;
    }

    // used to marks a section as 'completed' for all students in it
    public void markSectionCompleted(int sectionId) throws SQLException {
        var sql = "UPDATE enrollments SET status='completed' WHERE section_id=? AND status='registered'";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.executeUpdate();
        }
    }

    // gets a list of section objects that a student has finished
    public List<Section> getCompletedSections(String studentId) throws SQLException {
        var sql = "SELECT s.* FROM enrollments e JOIN sections s ON e.section_id = s.section_id WHERE e.student_id=? AND e.status='completed'";
        var result = new ArrayList<Section>();

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapsSection(rs));
            }
        }
        return result;
    }

    // this is a helper function
    // it maps database row to section object 
    // it basically fetches course and instructor details
    private Section mapsSection(ResultSet rs) throws SQLException {
        return new Section(
            rs.getInt("section_id"),
            courseDao.getCourse(rs.getInt("course_id")),
            (Instructor) userDao.findFullUserByUserId(rs.getString("instructor_id")),
            rs.getString("day_time"),
            rs.getString("room"),
            rs.getInt("capacity"),
            rs.getString("semester"),
            rs.getInt("year")
        );
    }
}
