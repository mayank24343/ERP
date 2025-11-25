package edu.univ.erp.data;

import edu.univ.erp.domain.Section;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class SectionDao {

    private final DataSource ds;

    public SectionDao(DataSource ds) {
        this.ds = ds;
    }

    public void insertSection(int courseId, String instructorId, String dayTime,
                              String room, int capacity, String semester, int year)
            throws SQLException {

        String sql = "INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) " +
                "VALUES (?,?,?,?,?,?,?)";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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


    public Section getSection(int sectionId) throws SQLException {
        String sql = "SELECT * FROM sections WHERE section_id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        }
    }

    public List<Section> getSectionsByCourse(int courseId) throws SQLException {
        String sql = "SELECT * FROM sections WHERE course_id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();

            List<Section> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        }
    }

    public List<Section> getSectionsByInstructor(String instructorId) throws SQLException {
        String sql = "SELECT * FROM sections WHERE instructor_id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, instructorId);
            ResultSet rs = ps.executeQuery();

            List<Section> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        }
    }

    private Section mapRow(ResultSet rs) throws SQLException {
        return new Section(
                rs.getInt("section_id"),
                rs.getInt("course_id"),
                rs.getString("instructor_id"),
                rs.getString("day_time"),
                rs.getString("room"),
                rs.getInt("capacity"),
                rs.getString("semester"),
                rs.getInt("year")
        );
    }

    public void updateInstructor(int sectionId, String instructorId) throws SQLException {
        String sql = "UPDATE sections SET instructor_id=? WHERE section_id=?";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, instructorId);
            ps.setInt(2, sectionId);

            ps.executeUpdate();
        }
    }

}
