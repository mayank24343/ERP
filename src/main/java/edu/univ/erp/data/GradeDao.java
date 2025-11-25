package edu.univ.erp.data;

import edu.univ.erp.domain.Grade;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class GradeDao {

    private final DataSource ds;

    public GradeDao(DataSource ds) {
        this.ds = ds;
    }

    public List<Grade> getGradesForEnrollment(int enrollmentId) throws SQLException {
        String sql = "SELECT * FROM grades WHERE enrollment_id = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, enrollmentId);
            ResultSet rs = ps.executeQuery();

            List<Grade> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new Grade(
                        rs.getInt("grade_id"),
                        rs.getInt("enrollment_id"),
                        rs.getString("component"),
                        rs.getDouble("score"),
                        rs.getString("final_grade")
                ));
            }
            return list;
        }
    }

    public void addComponentScore(int enrollmentId, String component, double score) throws SQLException {
        String sql = "INSERT INTO grades (enrollment_id, component, score) VALUES (?,?,?)";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, enrollmentId);
            ps.setString(2, component);
            ps.setDouble(3, score);
            ps.executeUpdate();
        }
    }

    public void assignFinal(int enrollmentId, String finalGrade) throws SQLException {
        String sql =
                "UPDATE grades SET final_grade=? WHERE enrollment_id=? " +
                        "AND component='final'";

        try (Connection conn = ds.getConnection()) {

            // upsert final grade
            PreparedStatement check = conn.prepareStatement(
                    "SELECT COUNT(*) FROM grades WHERE enrollment_id=? AND component='final'");
            check.setInt(1, enrollmentId);
            ResultSet rs = check.executeQuery();
            rs.next();

            if (rs.getInt(1) == 0) {
                PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO grades(enrollment_id,component,final_grade) VALUES (?, 'final', ?)");
                insert.setInt(1, enrollmentId);
                insert.setString(2, finalGrade);
                insert.executeUpdate();
            } else {
                PreparedStatement update = conn.prepareStatement(sql);
                update.setString(1, finalGrade);
                update.setInt(2, enrollmentId);
                update.executeUpdate();
            }
        }
    }

    public List<Map<String,Object>> getSectionGrades(int sectionId) throws SQLException {

        String sql =
                "SELECT g.enrollment_id, e.student_id, g.component, g.score, g.final_grade " +
                        "FROM grades g " +
                        "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                        "WHERE e.section_id = ?";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();

            List<Map<String,Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String,Object> row = new HashMap<>();
                row.put("enrollmentId", rs.getInt("enrollment_id"));
                row.put("studentId", rs.getString("student_id"));
                row.put("component", rs.getString("component"));
                row.put("score", rs.getDouble("score"));
                row.put("final", rs.getString("final_grade"));
                rows.add(row);
            }
            return rows;
        }
    }
}
