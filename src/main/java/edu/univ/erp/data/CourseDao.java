package edu.univ.erp.data;

import edu.univ.erp.domain.Course;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class CourseDao {
    private final DataSource ds;

    public CourseDao(DataSource ds) {
        this.ds = ds;
    }

    // this adds a new course to the database
    public void insertCourse(String code, String title, int credits) throws SQLException {
        var sql = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, title);
            ps.setInt(3, credits);
            ps.executeUpdate();
        }
    }

    // this updates the title and credits of an existing course using its course code
    public void updateCourse(String code, String title, int credits) throws SQLException {
        var sql = "UPDATE courses SET title = ?, credits = ? WHERE code = ?";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setInt(2, credits);
            ps.setString(3, code);
            ps.executeUpdate();
        }
    }

    // function to remove a course from the database using its unique numeric ID
    public void deleteCourse(int id) throws SQLException {
        var sql = "DELETE FROM courses WHERE course_id = ?";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // used to find a specific course by its numeric ID
    public Course getCourse(int courseId) throws SQLException {
        var sql = "SELECT * FROM courses WHERE course_id = ?";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Course(courseId, rs.getString("code"), rs.getString("title"), rs.getInt("credits"));
                }
            }
        }
        return null; // Return null if not found
    }

    // retrieves a list of all courses, sorted by their course code
    public List<Course> findAllCourses() throws SQLException {
        var sql = "SELECT * FROM courses ORDER BY code";
        var list = new ArrayList<Course>();

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql); var rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Course(
                    rs.getInt("course_id"),
                    rs.getString("code"),
                    rs.getString("title"),
                    rs.getInt("credits")
                ));
            }
        }
        return list;
    }
}
