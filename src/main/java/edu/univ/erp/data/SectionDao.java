package edu.univ.erp.data;

import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.util.DataSourceProvider;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SectionDao {
    private final DataSource ds;

    private final CourseDao courseDao;
    private final UserDao userDao;

    //constructor
    public SectionDao(DataSource ds) {
        this.ds = ds;
        this.courseDao = new CourseDao(ds);
        this.userDao = new UserDao(DataSourceProvider.getAuthDataSource(), ds);
    }

    //create new section
    public void insertSection(int courseId, String instructorId, String dayTime, String room, int capacity, String semester, int year) throws SQLException {
        String sql = "INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) VALUES (?,?,?,?,?,?,?)";

        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setString(2, instructorId);
            ps.setString(3, dayTime);
            ps.setString(4, room);
            ps.setInt(5, capacity);
            ps.setString(6, semester);
            ps.setInt(7, year);

            ps.executeUpdate();
        }
    }

    //get section by id
    public Section getSection(int sectionId) throws SQLException {
        String sql = "SELECT * FROM sections WHERE section_id=?";

        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    //get all sections taught by instructor
    public List<Section> getSectionsByInstructor(String instructorId) throws SQLException {
        String sql = "SELECT * FROM sections WHERE instructor_id=?";

        List<Section> list = new ArrayList<>();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, instructorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }

        return list;
    }

    //change instructor
    public void updateInstructor(int sectionId, String instructorId) throws SQLException {
        String sql = "UPDATE sections SET instructor_id=? WHERE section_id=?";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, instructorId);
            ps.setInt(2, sectionId);

            ps.executeUpdate();
        }
    }

    //update section
    public void updateSection(int sectionId, int courseId, String instructorId, String dayTime, String room, int capacity, String semester, int year) throws SQLException {
        String sql = "UPDATE sections SET course_id=?, instructor_id=?, day_time=?, room=?, capacity=?, semester=?, year=? WHERE section_id=?";

        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setString(2, instructorId);
            ps.setString(3, dayTime);
            ps.setString(4, room);
            ps.setInt(5, capacity);
            ps.setString(6, semester);
            ps.setInt(7, year);
            ps.setInt(8, sectionId);

            ps.executeUpdate();
        }
    }

    //delete section
    public void deleteSection(int sectionId) throws SQLException {
        String sql = "DELETE FROM sections WHERE section_id=?";
        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.executeUpdate();
        }
    }

    //get all sections
    public List<Section> getAllSections() throws SQLException {
        String sql = "SELECT * FROM sections";

        List<Section> list = new ArrayList<>();

        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    //get all sections for a course
    public List<Section> findSectionsForRegistration(int courseId) throws SQLException {

        String sql = "SELECT s.* FROM sections s WHERE s.course_id = ? ORDER BY s.section_id";

        List<Section> list = new ArrayList<>();

        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }

        return list;
    }

    //check section capacity
    public boolean hasSeat(int sectionId) throws SQLException {
        String sql = " SELECT (SELECT COUNT(*) FROM enrollments WHERE section_id=? AND status='registered') AS enrolled, (SELECT capacity FROM sections WHERE section_id=?) AS cap ";

        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ps.setInt(2, sectionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int enrolled = rs.getInt("enrolled");
                    int cap = rs.getInt("cap");
                    return enrolled < cap;
                }
            }
        }
        return false;
    }

    //check if students in section
    public boolean hasStudents(int sectionId) throws SQLException {
        String sql = " SELECT (SELECT COUNT(*) FROM enrollments WHERE section_id=? AND status='registered') AS enrolled, (SELECT capacity FROM sections WHERE section_id=?) AS cap ";
        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.setInt(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int enrolled = rs.getInt("enrolled");
                    return enrolled > 0;
                }
            }
        }
        return false;
    }

    //get sections for a student
    public List<Section> getSectionsForStudent(String studentId) throws SQLException {
        String sql = " SELECT s.* FROM sections s JOIN enrollments e ON s.section_id = e.section_id WHERE e.student_id = ? AND e.status='registered' ";

        List<Section> list = new ArrayList<>();

        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }

        return list;
    }

    //map row to Section
    private Section mapRow(ResultSet rs) throws SQLException {
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
