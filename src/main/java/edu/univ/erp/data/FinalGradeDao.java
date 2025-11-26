package edu.univ.erp.data;

import edu.univ.erp.domain.FinalGrade;
import edu.univ.erp.domain.Assessment;
import edu.univ.erp.domain.Score;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class FinalGradeDao {

    private final DataSource ds;
    private final AssessmentDao assessmentDao;
    private final ScoreDao scoreDao;
    private final EnrollmentDao enrollmentDao;

    public FinalGradeDao(DataSource ds,
                         AssessmentDao assessmentDao,
                         ScoreDao scoreDao,
                         EnrollmentDao enrollmentDao) {

        this.ds = ds;
        this.assessmentDao = assessmentDao;
        this.scoreDao = scoreDao;
        this.enrollmentDao = enrollmentDao;
    }

    // --------------------------------------------------------------
    // Compute finals for ALL students in a section (does NOT save)
    // --------------------------------------------------------------
    public List<FinalGrade> computeFinals(int sectionId) throws SQLException {

        List<Assessment> assessments = assessmentDao.getBySection(sectionId);
        if (assessments.isEmpty()) return List.of();

        // all active students in the section
        List<Integer> enrolledIds = enrollmentDao.getActiveSectionIdsForStudent("//INVALID");

        // Actually fetch directly studentIds:
        List<String> studentIds = getStudentIdsForSection(sectionId);

        List<FinalGrade> results = new ArrayList<>();

        for (String studentId : studentIds) {
            double percentage = computeFinalPercentageForStudent(sectionId, studentId, assessments);
            String letter = mapToLetter(percentage);

            results.add(new FinalGrade(studentId, sectionId, percentage, letter));
        }

        return results;
    }

    // --------------------------------------------------------------
    // Compute AND SAVE final grades into final_grades table
    // --------------------------------------------------------------
    public void computeAndStoreFinals(int sectionId) throws SQLException {
        List<FinalGrade> finals = computeFinals(sectionId);

        for (FinalGrade fg : finals) {
            upsertFinalGrade(fg);
        }
    }

    // --------------------------------------------------------------
    // Compute final grade for one student (NOT stored)
    // --------------------------------------------------------------
    public FinalGrade computeForStudent(int sectionId, String studentId) throws SQLException {
        List<Assessment> assessments = assessmentDao.getBySection(sectionId);
        double percentage = computeFinalPercentageForStudent(sectionId, studentId, assessments);
        String letter = mapToLetter(percentage);

        return new FinalGrade(studentId, sectionId, percentage, letter);
    }

    // --------------------------------------------------------------
    // Get final grade entry (from table)
    // --------------------------------------------------------------
    public FinalGrade getFinalGrade(int sectionId, String studentId) throws SQLException {
        String sql = """
            SELECT section_id, student_id, percentage, letter_grade
            FROM final_grades
            WHERE section_id = ? AND student_id = ?
        """;

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ps.setString(2, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new FinalGrade(
                            rs.getString("student_id"),
                            rs.getInt("section_id"),
                            rs.getDouble("percentage"),
                            rs.getString("letter_grade")
                    );
                }
            }
        }

        return null;
    }

    // --------------------------------------------------------------
    // Get all final grades for student (for StudentDashboard)
    // --------------------------------------------------------------
    public List<FinalGrade> getFinalGradesForStudent(String studentId) throws SQLException {
        String sql = """
            SELECT section_id, student_id, percentage, letter_grade
            FROM final_grades
            WHERE student_id = ?
            ORDER BY section_id
        """;

        List<FinalGrade> list = new ArrayList<>();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new FinalGrade(
                            rs.getString("student_id"),
                            rs.getInt("section_id"),
                            rs.getDouble("percentage"),
                            rs.getString("letter_grade")
                    ));
                }
            }
        }

        return list;
    }

    // --------------------------------------------------------------
    // Insert or update a final grade
    // --------------------------------------------------------------
    public void upsertFinalGrade(FinalGrade g) throws SQLException {
        String sql = """
            INSERT INTO final_grades (section_id, student_id, percentage, letter_grade)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                percentage = VALUES(percentage),
                letter_grade = VALUES(letter_grade)
        """;

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, g.getSectionId());
            ps.setString(2, g.getStudentId());
            ps.setDouble(3, g.getPercentage());
            ps.setString(4, g.getLetter());

            ps.executeUpdate();
        }
    }

    // --------------------------------------------------------------
    // Delete all final grades for a section
    // --------------------------------------------------------------
    public void deleteFinalsForSection(int sectionId) throws SQLException {
        String sql = "DELETE FROM final_grades WHERE section_id = ?";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ps.executeUpdate();
        }
    }

    // --------------------------------------------------------------
    // Compute final % for one student
    // --------------------------------------------------------------
    private double computeFinalPercentageForStudent(int sectionId,
                                                    String studentId,
                                                    List<Assessment> assessments)
            throws SQLException {

        double total = 0;

        for (Assessment a : assessments) {
            Score score = scoreDao.getScore(a.getId(), studentId);
            if (score == null) continue; // treat as 0

            double marks = score.getMarksObtained();
            double pct = (marks / a.getMaxMarks()) * a.getWeight();
            total += pct;
        }

        return total;
    }

    // --------------------------------------------------------------
    // Helper to fetch all student IDs in a section
    // --------------------------------------------------------------
    private List<String> getStudentIdsForSection(int sectionId) throws SQLException {
        String sql = """
            SELECT student_id
            FROM enrollments
            WHERE section_id = ? AND status = 'registered'
        """;

        List<String> list = new ArrayList<>();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString("student_id"));
            }
        }

        return list;
    }

    // --------------------------------------------------------------
    // Map percentage → letter grade
    // --------------------------------------------------------------
    private String mapToLetter(double p) {
        if (p >= 90) return "A";
        if (p >= 80) return "B";
        if (p >= 70) return "C";
        if (p >= 60) return "D";
        return "F";
    }
}
