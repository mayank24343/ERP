package edu.univ.erp.data;

import edu.univ.erp.domain.Assessment;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssessmentDao {

    private final DataSource ds;

    public AssessmentDao(DataSource ds) {
        this.ds = ds;
    }

    // ------------------------------
    // Get all assessments under a section
    // ------------------------------
    public List<Assessment> getBySection(int sectionId) throws SQLException {
        String sql = """
            SELECT assessment_id, section_id, name, max_marks, weight
            FROM assessments
            WHERE section_id = ?
            ORDER BY assessment_id
        """;

        List<Assessment> list = new ArrayList<>();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    // ------------------------------
    // Get a single assessment by ID
    // ------------------------------
    public Assessment getById(int assessmentId) throws SQLException {
        String sql = """
            SELECT assessment_id, section_id, name, max_marks, weight
            FROM assessments
            WHERE assessment_id = ?
        """;

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, assessmentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null; // not found
    }

    // ------------------------------
    // Insert a new assessment
    // ------------------------------

    public void insert(Assessment a) throws SQLException {
        String sql = """
            INSERT INTO assessments (section_id, name, max_marks, weight)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, a.getSectionId());
            ps.setString(2, a.getName());
            ps.setDouble(3, a.getMaxMarks());
            ps.setDouble(4, a.getWeight());

            ps.executeUpdate();
        }
    }

    // ------------------------------
    // Update assessment
    // ------------------------------
    public void update(Assessment a) throws SQLException {
        String sql = """
            UPDATE assessments
            SET name = ?, max_marks = ?, weight = ?
            WHERE assessment_id = ?
        """;

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, a.getName());
            ps.setDouble(2, a.getMaxMarks());
            ps.setDouble(3, a.getWeight());
            ps.setInt(4, a.getId());

            ps.executeUpdate();
        }
    }

    // ------------------------------
    // Delete
    // ------------------------------
    public void delete(int assessmentId) throws SQLException {
        String sql = """
            DELETE FROM assessments
            WHERE assessment_id = ?
        """;

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, assessmentId);
            ps.executeUpdate();
        }
    }

    // ------------------------------
    // Map ResultSet → Assessment object
    // ------------------------------
    private Assessment mapRow(ResultSet rs) throws SQLException {
        return new Assessment(
                rs.getInt("assessment_id"),
                rs.getInt("section_id"),
                rs.getString("name"),
                rs.getDouble("max_marks"),
                rs.getDouble("weight")
        );
    }
}
