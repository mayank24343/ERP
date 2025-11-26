package edu.univ.erp.service;

import edu.univ.erp.access.AccessManager;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InstructorService {

    private final SectionDao sectionDao;
    private final EnrollmentDao enrollmentDao;
    private final AssessmentDao assessmentDao;
    private final ScoreDao scoreDao;
    private final FinalGradeDao finalGradeDao;

    public InstructorService(DataSource ds, AccessManager a) {

        this.sectionDao = new SectionDao(ds);
        this.enrollmentDao = new EnrollmentDao(ds);
        this.assessmentDao = new AssessmentDao(ds);
        this.scoreDao = new ScoreDao(ds);
        this.finalGradeDao = new FinalGradeDao(ds, assessmentDao, scoreDao, enrollmentDao);
    }

    // ---------------------------------------------------------
    // SECTIONS TAUGHT BY THE INSTRUCTOR
    // ---------------------------------------------------------
    public List<Section> getMySections(String instructorId) throws SQLException {
        return sectionDao.getSectionsByInstructor(instructorId);
    }

    // ---------------------------------------------------------
    // LIST STUDENTS IN A SECTION
    // ---------------------------------------------------------
    public List<Student> getEnrolledStudents(int sectionId) throws SQLException {
        return enrollmentDao.getEnrolledStudents(sectionId);
    }

    // ---------------------------------------------------------
    // ASSESSMENT MANAGEMENT
    // ---------------------------------------------------------
    public List<Assessment> getAssessments(int sectionId) throws SQLException {
        return assessmentDao.getBySection(sectionId);
    }

    public void addAssessment(Assessment a) throws SQLException {
        assessmentDao.insert(a);
    }

    public void updateAssessment(Assessment a) throws SQLException {
        assessmentDao.update(a);
    }

    public void deleteAssessment(int assessmentId) throws SQLException {
        // optional: delete its scores too
        scoreDao.deleteByAssessment(assessmentId);

        assessmentDao.delete(assessmentId);
    }

    // ---------------------------------------------------------
    // SCORE MANAGEMENT (COMPONENTS)
    // ---------------------------------------------------------
    public List<Score> getScoresForAssessment(int assessmentId) throws SQLException {
        return scoreDao.getScoresByAssessment(assessmentId);
    }

    public Score getScore(int assessmentId, String studentId) throws SQLException {
        return scoreDao.getScore(assessmentId, studentId);
    }

    public void saveScore(Score score) throws SQLException {
        scoreDao.upsertScore(score);
    }

    public void saveScores(List<Score> scores) throws SQLException {
        scoreDao.upsertScores(scores);
    }

    // ---------------------------------------------------------
    // FINAL GRADE COMPUTATION
    // ---------------------------------------------------------
    // compute on demand (no DB save)
    public List<FinalGrade> computeFinalsPreview(int sectionId) throws SQLException {
        return finalGradeDao.computeFinals(sectionId);
    }

    // compute + store in final_grades table
    public void computeAndStoreFinals(int sectionId) throws SQLException {
        finalGradeDao.computeAndStoreFinals(sectionId);
    }

    public FinalGrade getFinalGrade(int sectionId, String studentId) throws SQLException {
        // return stored final grade if exists
        FinalGrade fg = finalGradeDao.getFinalGrade(sectionId, studentId);
        if (fg != null) return fg;

        // or compute fresh if not saved yet
        return finalGradeDao.computeForStudent(sectionId, studentId);
    }

    // ---------------------------------------------------------
    // FULL SUMMARY FOR GRADEBOOK PAGE
    // ---------------------------------------------------------
    public SectionGradeSummary getSectionGradeSummary(int sectionId) throws SQLException {
        System.out.println("HERE");
        List<Student> students = enrollmentDao.getEnrolledStudents(sectionId);
        System.out.println("HERE");
        List<Assessment> assessments = assessmentDao.getBySection(sectionId);

        List<StudentGradeRow> gradeRows = new ArrayList<>();

        // Compute for each student
        for (Student stu : students) {

            List<Score> scores = new ArrayList<>();
            for (Assessment a : assessments) {
                Score sc = scoreDao.getScore(a.getId(), stu.getUserId());
                if (sc != null) scores.add(sc);
            }

            FinalGrade finalGrade = finalGradeDao.computeForStudent(sectionId, stu.getUserId());

            gradeRows.add(new StudentGradeRow(stu, scores, finalGrade));
        }

        return new SectionGradeSummary(assessments, gradeRows);
    }

    // ---------------------------------------------------------
    // WRAPPER DTOs for InstructorDashboard
    // ---------------------------------------------------------
    public static class SectionGradeSummary {
        private final List<Assessment> assessments;
        private final List<StudentGradeRow> rows;

        public SectionGradeSummary(List<Assessment> assessments, List<StudentGradeRow> rows) {
            this.assessments = assessments;
            this.rows = rows;
        }

        public List<Assessment> getAssessments() { return assessments; }
        public List<StudentGradeRow> getRows() { return rows; }
    }

    public static class StudentGradeRow {
        private final Student student;
        private final List<Score> scores;
        private final FinalGrade finalGrade;

        public StudentGradeRow(Student student, List<Score> scores, FinalGrade finalGrade) {
            this.student = student;
            this.scores = scores;
            this.finalGrade = finalGrade;
        }

        public Student getStudent() { return student; }
        public List<Score> getScores() { return scores; }
        public FinalGrade getFinalGrade() { return finalGrade; }
    }
}
