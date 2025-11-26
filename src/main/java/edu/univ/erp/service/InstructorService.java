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
    private final AccessManager accessManager;
    private final MaintenanceService maintenanceService;

    public InstructorService(DataSource ds, AccessManager a) {

        this.sectionDao = new SectionDao(ds);
        this.enrollmentDao = new EnrollmentDao(ds);
        this.assessmentDao = new AssessmentDao(ds);
        this.scoreDao = new ScoreDao(ds);
        this.finalGradeDao = new FinalGradeDao(ds, assessmentDao, scoreDao, enrollmentDao);
        this.accessManager = a;
        this.maintenanceService = new MaintenanceService(ds);
    }

    //get instructor sections
    public List<Section> getMySections(String instructorId) throws Exception {
        accessManager.requireInstructor(instructorId);
        return sectionDao.getSectionsByInstructor(instructorId);
    }

    //get students in section
    public List<Student> getEnrolledStudents(int sectionId, String instructorId) throws SQLException, ServiceException {
        accessManager.requireInstructorForSection(instructorId, sectionId);
        return enrollmentDao.getEnrolledStudents(sectionId);
    }

    //assessment list
    public List<Assessment> getAssessments(int sectionId, String instructorId) throws SQLException, ServiceException {
        accessManager.requireInstructorForSection(instructorId, sectionId);
        return assessmentDao.getBySection(sectionId);
    }

    public void addAssessment(Assessment a, String instructorId) throws SQLException, ServiceException {
        maintenanceService.requireWriteAllowed();
        accessManager.requireInstructorForSection(instructorId, a.getSectionId());
        assessmentDao.insert(a);
    }

    public void updateAssessment(Assessment a, String instructorId) throws SQLException, ServiceException {
        maintenanceService.requireWriteAllowed();
        accessManager.requireInstructorForSection(instructorId, a.getSectionId());
        assessmentDao.update(a);
    }

    public void deleteAssessment(Assessment a, String instructorId) throws SQLException,  ServiceException {
        maintenanceService.requireWriteAllowed();
        accessManager.requireInstructorForSection(instructorId, a.getSectionId());
        scoreDao.deleteByAssessment(a.getId());
        assessmentDao.delete(a.getId());
    }

    //get scores for assessment
    public List<Score> getScoresForAssessment(int assessmentId, int sectionId, String instructorId) throws SQLException, ServiceException {
        accessManager.requireInstructorForSection(instructorId, sectionId);
        return scoreDao.getScoresByAssessment(assessmentId);
    }

    public void saveScores(List<Score> scores, int sectionId, String instructorId) throws SQLException, ServiceException {
        maintenanceService.requireWriteAllowed();
        accessManager.requireInstructorForSection(instructorId,sectionId);
        scoreDao.upsertScores(scores);
    }

    //compute final grades
    public List<FinalGrade> computeFinalsPreview(int sectionId, String instructorId) throws SQLException, ServiceException {
        accessManager.requireInstructorForSection(instructorId,sectionId);
        return finalGradeDao.computeFinals(sectionId);
    }

    //compute + store in final_grades table
    public void computeAndStoreFinals(int sectionId, String instructorId) throws SQLException, ServiceException {
        maintenanceService.requireWriteAllowed();
        accessManager.requireInstructorForSection(instructorId,sectionId);
        finalGradeDao.computeAndStoreFinals(sectionId);
    }

    //final grade for a student
    public FinalGrade getFinalGrade(int sectionId, String studentId) throws SQLException, ServiceException {
        accessManager.requireStudent(studentId);
        // return stored final grade if exists
        FinalGrade fg = finalGradeDao.getFinalGrade(sectionId, studentId);
        if (fg != null) return fg;

        // or compute fresh if not saved yet
        return finalGradeDao.computeForStudent(sectionId, studentId);
    }

    //final grades for a section
    public SectionGradeSummary getSectionGradeSummary(int sectionId, String instructorId) throws SQLException,  ServiceException {
        accessManager.requireInstructorForSection(instructorId,sectionId);
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

    //grades for a section for all components
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

    //grade summary for student
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
