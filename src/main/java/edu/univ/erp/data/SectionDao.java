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

    public SectionDao(DataSource ds) {
        this.ds = ds;
        this.courseDao = new CourseDao(ds);
        // Initialize UserDao with both Auth and ERP data sources
        this.userDao = new UserDao(DataSourceProvider.getAuthDataSource(), ds);
    }

    // creates a new section for a course
    public void insertSection(int courseId, String instructorId, String dayTime, String room, int capacity, String semester, int year) throws SQLException {
        var sql = "INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) VALUES (?,?,?,?,?,?,?)";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
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

    // finds a specific section by its ID
    public Section getSection(int sectionId) throws SQLException {
        var sql = "SELECT * FROM sections WHERE section_id=?";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // gets a list of all sections taught by a specific instructor
    public List<Section> getSectionsByInstructor(String instructorId) throws SQLException {
        var sql = "SELECT * FROM sections WHERE instructor_id=?";
        var list = new ArrayList<Section>();

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, instructorId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // assigns a different instructor to a section
    public void updateInstructor(int sectionId, String instructorId) throws SQLException {
        var sql = "UPDATE sections SET instructor_id=? WHERE section_id=?";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, instructorId);
            ps.setInt(2, sectionId);
            ps.executeUpdate();
        }
    }

    // updates all details of a section
    public void updateSection(int sectionId, int courseId, String instructorId, String dayTime, String room, int capacity, String semester, int year) throws SQLException {
        var sql = "UPDATE sections SET course_id=?, instructor_id=?, day_time=?, room=?, capacity=?, semester=?, year=? WHERE section_id=?";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
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

    // removes a section from the database
    public void deleteSection(int sectionId) throws SQLException {
        var sql = "DELETE FROM sections WHERE section_id=?";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.executeUpdate();
        }
    }

    // gets a list of every section in the system
    public List<Section> getAllSections() throws SQLException {
        var sql = "SELECT * FROM sections";
        var list = new ArrayList<Section>();

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql); var rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // finds all sections available for a specific course (when a student registers)
    public List<Section> findSectionsForRegistration(int courseId) throws SQLException {
        var sql = "SELECT s.* FROM sections s WHERE s.course_id = ? ORDER BY s.section_id";
        var list = new ArrayList<Section>();

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // checks if there is space available in the section (enrolled < capacity)
    public boolean hasSeat(int sectionId) throws SQLException {
        var sql = "SELECT (SELECT COUNT(*) FROM enrollments WHERE section_id=? AND status='registered') AS enrolled, (SELECT capacity FROM sections WHERE section_id=?) AS cap";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.setInt(2, sectionId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("enrolled") < rs.getInt("cap");
                }
            }
        }
        return false;
    }

    // Checks if there are any students currently enrolled in the section
    public boolean hasStudents(int sectionId) throws SQLException {
        var sql = "SELECT (SELECT COUNT(*) FROM enrollments WHERE section_id=? AND status='registered') AS enrolled, (SELECT capacity FROM sections WHERE section_id=?) AS cap";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.setInt(2, sectionId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("enrolled") > 0;
                }
            }
        }
        return false;
    }

    // gets all sections that a specific student is currently registered for
    public List<Section> getSectionsForStudent(String studentId) throws SQLException {
        var sql = "SELECT s.* FROM sections s JOIN enrollments e ON s.section_id = e.section_id WHERE e.student_id = ? AND e.status='registered'";
        var list = new ArrayList<Section>();

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // this is a helper function that maps a database row to a section object 
    // loads course and instructor details
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
