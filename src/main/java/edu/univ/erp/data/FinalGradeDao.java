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

    public FinalGradeDao(DataSource ds, AssessmentDao assessmentDao, ScoreDao scoreDao, EnrollmentDao enrollmentDao) {
        this.ds = ds;
        this.assessmentDao = assessmentDao;
        this.scoreDao = scoreDao;
        this.userDao = new UserDao(DataSourceProvider.getAuthDataSource(), DataSourceProvider.getERPDataSource());
        this.sectionDao = new SectionDao(DataSourceProvider.getERPDataSource());
        this.slabDao = new GradeSlabDao(DataSourceProvider.getERPDataSource());
    }

    // calculates final grades for every student in a section based on their assessment scores
    public List<FinalGrade> computeFinals(int sectionId) throws SQLException {
        var assessments = assessmentDao.getBySection(sectionId);
        if (assessments.isEmpty()) return List.of();

        var studentIds = getStudentIdsForSection(sectionId);
        var slabs = slabDao.getSlabs(sectionId);
        var results = new ArrayList<FinalGrade>();

        // loop through every student to calculate their specific grade
        for (String studentId : studentIds) {
            double percentage = computeFinalPercentageForStudent(sectionId, studentId, assessments);
            String letter = computeLetter(percentage, slabs);
            
            var section = sectionDao.getSection(sectionId);
            var student = (Student) userDao.findFullUserByUserId(studentId);

            results.add(new FinalGrade(student, section, percentage, letter));
        }
        return results;
    }

    // determines the Letter Grade (A, B, C...) based on the percentage score
    public String computeLetter(double percent, List<GradeSlab> slabs) {
        // Check against custom grade slabs defined for this section
        for (GradeSlab s : slabs) {
            if (percent >= s.getMin() && percent <= s.getMax()) return s.getLetter();
        }
        // Default grading scale if no custom slabs match
        if (percent >= 95) return "A+";
        if (percent >= 90) return "A";
        if (percent >= 80) return "A-";
        if (percent >= 70) return "B";
        if (percent >= 60) return "C";
        return "F";
    }

    // calculates grades and immediately saves them to the database
    public void computeAndStoreFinals(int sectionId) throws SQLException {
        List<FinalGrade> finals = computeFinals(sectionId);
        for (FinalGrade fg : finals) {
            upsertFinalGrade(fg);
        }
    }

    // calculates the final grade for a single student on demand (without saving)
    public FinalGrade computeForStudent(int sectionId, String studentId) throws SQLException {
        var assessments = assessmentDao.getBySection(sectionId);
        double percentage = computeFinalPercentageForStudent(sectionId, studentId, assessments);
        String letter = computeLetter(percentage, slabDao.getSlabs(sectionId));
        
        return new FinalGrade(
            (Student) userDao.findFullUserByUserId(studentId),
            sectionDao.getSection(sectionId),
            percentage,
            letter
        );
    }

    // fetches a previously saved final grade from the database
    public FinalGrade getFinalGrade(int sectionId, String studentId) throws SQLException {
        var sql = "SELECT section_id, student_id, percentage, letter_grade FROM final_grades WHERE section_id = ? AND student_id = ?";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.setString(2, studentId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // gets a list of all final grades ever recorded for a specific student
    public List<FinalGrade> getFinalGradesForStudent(String studentId) throws SQLException {
        var sql = "SELECT section_id, student_id, percentage, letter_grade FROM final_grades WHERE student_id = ? ORDER BY section_id";
        var list = new ArrayList<FinalGrade>();

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // saves a grade 
    // if a grade already exists for this student/section, it updates it.
    public void upsertFinalGrade(FinalGrade g) throws SQLException {
        var sql = "INSERT INTO final_grades (section_id, student_id, percentage, letter_grade) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE percentage = VALUES(percentage), letter_grade = VALUES(letter_grade)";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, g.getSection().getSectionId());
            ps.setString(2, g.getStudent().getUserId());
            ps.setDouble(3, g.getPercentage());
            ps.setString(4, g.getLetter());
            ps.executeUpdate();
        }
    }

    // deletes all final grades for a specific section
    public void deleteFinalsForSection(int sectionId) throws SQLException {
        var sql = "DELETE FROM final_grades WHERE section_id = ?";
        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.executeUpdate();
        }
    }

    // sums up (marks obtained / maximum marks) * weight for all assessments
    private double computeFinalPercentageForStudent(int sectionId, String studentId, List<Assessment> assessments) throws SQLException {
        double total = 0;
        for (Assessment a : assessments) {
            Score score = scoreDao.getScore(a.getId(), studentId);
            if (score == null) continue; // If no score, treat as 0
            total += (score.getMarksObtained() / a.getMaxMarks()) * a.getWeight();
        }
        return total;
    }

    // this is a helper function to get a list of Student IDs enrolled in a section
    private List<String> getStudentIdsForSection(int sectionId) throws SQLException {
        var sql = "SELECT student_id FROM enrollments WHERE section_id = ?";
        var list = new ArrayList<String>();

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString("student_id"));
            }
        }
        return list;
    }

    // a helper function to build the FinalGrade object from database results
    private FinalGrade mapRow(ResultSet rs) throws SQLException {
        return new FinalGrade(
            (Student) userDao.findFullUserByUserId(rs.getString("student_id")),
            sectionDao.getSection(rs.getInt("section_id")),
            rs.getDouble("percentage"),
            rs.getString("letter_grade")
        );
    }
}
