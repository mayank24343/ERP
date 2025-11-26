package edu.univ.erp.data;

import edu.univ.erp.domain.*;
import edu.univ.erp.util.DataSourceProvider;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class FinalGradeDao {
    private final DataSource ds;
    private final AssessmentDao assessmentDao;
    private final ScoreDao scoreDao;
    private final SectionDao sectionDao;
    private final UserDao userDao;
    private final GradeSlabDao slabDao;

    //constructor
    public FinalGradeDao(DataSource ds, AssessmentDao assessmentDao, ScoreDao scoreDao, EnrollmentDao enrollmentDao) {

        this.ds = ds;
        this.assessmentDao = assessmentDao;
        this.scoreDao = scoreDao;
        this.userDao = new UserDao(DataSourceProvider.getAuthDataSource(), DataSourceProvider.getERPDataSource());
        this.sectionDao = new SectionDao(DataSourceProvider.getERPDataSource());
        this.slabDao = new GradeSlabDao(DataSourceProvider.getERPDataSource());
    }

    //compute final grades for section
    public List<FinalGrade> computeFinals(int sectionId) throws SQLException {
        List<Assessment> assessments = assessmentDao.getBySection(sectionId);
        if (assessments.isEmpty()) return List.of();

        //students in section
        List<String> studentIds = getStudentIdsForSection(sectionId);

        List<FinalGrade> results = new ArrayList<>();

        //get slabs
        List<GradeSlab> slabs = slabDao.getSlabs(sectionId);

        //for each student, calculate final grade
        for (String studentId : studentIds) {
            double percentage = computeFinalPercentageForStudent(sectionId, studentId, assessments);
            String letter = computeLetter(percentage, slabs);
            Section section = sectionDao.getSection(sectionId);
            Student student = (Student) userDao.findFullUserByUserId(studentId);

            results.add(new FinalGrade(student, section, percentage, letter));
        }

        return results;
    }

    //compute letter grades
    public String computeLetter(double percent, List<GradeSlab> slabs) {
        for (GradeSlab s : slabs) {
            if (percent >= s.getMin() && percent <= s.getMax()) {
                return s.getLetter();
            }
        }

        //default
        if (percent >= 95) return "A+";
        if (percent >= 90) return "A";
        if (percent >= 80) return "A-";
        if (percent >= 70) return "B";
        if (percent >= 60) return "C";
        return "F";
    }

    //store final grades to db
    public void computeAndStoreFinals(int sectionId) throws SQLException {
        List<FinalGrade> finals = computeFinals(sectionId);

        for (FinalGrade fg : finals) {
            upsertFinalGrade(fg);
        }
    }

    //compute final for a student
    public FinalGrade computeForStudent(int sectionId, String studentId) throws SQLException {
        List<Assessment> assessments = assessmentDao.getBySection(sectionId);
        double percentage = computeFinalPercentageForStudent(sectionId, studentId, assessments);
        String letter = computeLetter(percentage, slabDao.getSlabs(sectionId));
        Section section = sectionDao.getSection(sectionId);
        Student student = (Student) userDao.findFullUserByUserId(studentId);

        return new FinalGrade(student, section, percentage, letter);
    }

    //get final grade for student in section
    public FinalGrade getFinalGrade(int sectionId, String studentId) throws SQLException {
        String sql = "SELECT section_id, student_id, percentage, letter_grade FROM final_grades WHERE section_id = ? AND student_id = ?";

        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ps.setString(2, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new FinalGrade(
                            (Student) userDao.findFullUserByUserId(rs.getString("student_id")),
                            sectionDao.getSection(rs.getInt("section_id")),
                            rs.getDouble("percentage"),
                            rs.getString("letter_grade")
                    );
                }
            }
        }

        return null;
    }

    //get final grades for student
    public List<FinalGrade> getFinalGradesForStudent(String studentId) throws SQLException {
        String sql = "SELECT section_id, student_id, percentage, letter_grade FROM final_grades WHERE student_id = ? ORDER BY section_id ";

        List<FinalGrade> list = new ArrayList<>();

        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new FinalGrade(
                            (Student) userDao.findFullUserByUserId(rs.getString("student_id")),
                            sectionDao.getSection(rs.getInt("section_id")),
                            rs.getDouble("percentage"),
                            rs.getString("letter_grade")
                    ));
                }
            }
        }

        return list;
    }

    //uodate and insert final grades
    public void upsertFinalGrade(FinalGrade g) throws SQLException {
        String sql = "INSERT INTO final_grades (section_id, student_id, percentage, letter_grade) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE percentage = VALUES(percentage), letter_grade = VALUES(letter_grade) ";

        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, g.getSection().getSectionId());
            ps.setString(2, g.getStudent().getUserId());
            ps.setDouble(3, g.getPercentage());
            ps.setString(4, g.getLetter());

            ps.executeUpdate();
        }
    }

    //delete final grades
    public void deleteFinalsForSection(int sectionId) throws SQLException {
        String sql = "DELETE FROM final_grades WHERE section_id = ?";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ps.executeUpdate();
        }
    }

    //final %age for student
    private double computeFinalPercentageForStudent(int sectionId, String studentId, List<Assessment> assessments) throws SQLException {

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

    //ids of student in a section
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
}
