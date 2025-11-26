package edu.univ.erp.data;

import edu.univ.erp.domain.Course;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class CourseDao {
    private final DataSource ds;

    //constructor
    public CourseDao(DataSource ds) {
        this.ds = ds;
    }

    //add new course
    public void insertCourse(String code, String title, int credits) throws SQLException {
        String sql = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setString(2, title);
            ps.setInt(3, credits);

            ps.executeUpdate();
        }
    }

    //update course
    public void updateCourse(String code, String title, int credits) throws SQLException {
        String sql = "UPDATE courses SET title = ?, credits = ? WHERE code = ?";
        try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setInt(2, credits);
            ps.setString(3, code);

            ps.executeUpdate();
        }
    }

    //get Course by id
    public Course getCourse(int courseId) throws SQLException {
        String sql = "SELECT * FROM courses WHERE course_id = ?";
        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Course(courseId,
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getInt("credits"));

            }
        }
        return null;
    }

    //get all courses
    public List<Course> findAllCourses() throws SQLException {
        String sql = "SELECT * FROM courses ORDER BY code";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Course> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new Course(
                        rs.getInt("course_id"),
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getInt("credits")
                ));
            }
            return list;
        }
    }
}
