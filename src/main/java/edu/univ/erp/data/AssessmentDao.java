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

    // this gets a list of all assessments for a specific section
    public List<Assessment> getBySection(int sectionId) throws SQLException {
        var sql = "SELECT assessment_id, section_id, name, max_marks, weight FROM assessments WHERE section_id = ? ORDER BY assessment_id";
        var list = new ArrayList<Assessment>();

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // this adds a new assessment to the database

    public void insert(Assessment a) throws SQLException {
        var sql = "INSERT INTO assessments (section_id, name, max_marks, weight) VALUES (?, ?, ?, ?)";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getSectionId());
            ps.setString(2, a.getName());
            ps.setDouble(3, a.getMaxMarks());
            ps.setDouble(4, a.getWeight());
            ps.executeUpdate();
        }
    }

    // updates the details of an existing assesment
    public void update(Assessment a) throws SQLException {
        var sql = "UPDATE assessments SET name = ?, max_marks = ?, weight = ? WHERE assessment_id = ?";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getName());
            ps.setDouble(2, a.getMaxMarks());
            ps.setDouble(3, a.getWeight());
            ps.setInt(4, a.getId());
            ps.executeUpdate();
        }
    }

    // Removes an assessment from the database
    public void delete(int assessmentId) throws SQLException {
        var sql = "DELETE FROM assessments WHERE assessment_id = ?";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assessmentId);
            ps.executeUpdate();
        }
    }

   // Helper function to convert database row into an Assessment object
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
