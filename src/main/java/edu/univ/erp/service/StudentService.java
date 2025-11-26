package edu.univ.erp.service;

import edu.univ.erp.access.AccessManager;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudentService {

    private final CourseDao courseDao;
    private final SectionDao sectionDao;
    private final EnrollmentDao enrollmentDao;
    private final AssessmentDao assessmentDao;
    private final ScoreDao scoreDao;
    private final FinalGradeDao finalGradeDao;

    // Set drop deadline here (change as needed)
    private final LocalDate DROP_DEADLINE = LocalDate.of(2025, 1, 31);

    public StudentService(DataSource ds, AccessManager a) {

        this.courseDao = new CourseDao(ds);
        this.sectionDao = new SectionDao(ds);
        this.enrollmentDao = new EnrollmentDao(ds);
        this.assessmentDao = new AssessmentDao(ds);
        this.scoreDao = new ScoreDao(ds);
        this.finalGradeDao = new FinalGradeDao(ds, assessmentDao, scoreDao, enrollmentDao);
    }

    // ---------------------------------------------------------
    // COURSE CATALOG
    // ---------------------------------------------------------
    public List<Course> getCatalog() throws SQLException {
        return courseDao.findAllCourses();
    }

    // ---------------------------------------------------------
    // SECTIONS AVAILABLE FOR REGISTRATION
    // ---------------------------------------------------------
    public List<Section> getAvailableSections(int courseId) throws SQLException {
        return sectionDao.findSectionsForRegistration(courseId);
    }

    // ---------------------------------------------------------
    // REGISTER STUDENT
    // ---------------------------------------------------------
    public void register(String studentId, int sectionId) throws Exception {

        // Check duplicate
        if (enrollmentDao.isAlreadyEnrolled(studentId, sectionId)) {
            throw new Exception("You are already registered in this section.");
        }

        // Check capacity
        if (!sectionDao.hasSeat(sectionId)) {
            throw new Exception("Section is full.");
        }

        // Register
        enrollmentDao.register(studentId, sectionId);
    }

    // ---------------------------------------------------------
    // DROP SECTION
    // ---------------------------------------------------------
    public void drop(String studentId, int sectionId) throws Exception {

        // Check drop deadline
        if (LocalDate.now().isAfter(DROP_DEADLINE)) {
            throw new Exception("Drop deadline has passed.");
        }

        // Must be enrolled
        if (!enrollmentDao.isAlreadyEnrolled(studentId, sectionId)) {
            throw new Exception("You are not enrolled in this section.");
        }

        enrollmentDao.drop(studentId, sectionId);
    }

    // ---------------------------------------------------------
    // LIST MY SECTIONS (REGISTERED)
    // ---------------------------------------------------------
    public List<Section> getMySections(String studentId) throws SQLException {
        return sectionDao.getSectionsForStudent(studentId);
    }

    // ---------------------------------------------------------
    // TIMETABLE (same as sections)
    // ---------------------------------------------------------
    public List<Section> getTimetable(String studentId) throws SQLException {
        return sectionDao.getSectionsForStudent(studentId);
    }

    // ---------------------------------------------------------
    // GET ALL GRADES FOR THIS STUDENT
    // Final GradeDao returns FINAL grades (one per section)
    // Scores are shown assessment-by-assessment
    // ---------------------------------------------------------
    public List<GradeView> getGradeBreakdown(String studentId) throws SQLException {

        List<GradeView> result = new ArrayList<>();

        // sections the student is enrolled in
        List<Section> sections = sectionDao.getSectionsForStudent(studentId);

        for (Section s : sections) {

            List<Assessment> assessments = assessmentDao.getBySection(s.getSectionId());

            List<Score> scores = new ArrayList<>();
            for (Assessment a : assessments) {
                Score sc = scoreDao.getScore(a.getId(), studentId);
                if (sc != null) scores.add(sc);
            }

            FinalGrade finalGrade = finalGradeDao.computeForStudent(s.getSectionId(), studentId);

            result.add(new GradeView(
                    s,
                    assessments,
                    scores,
                    finalGrade
            ));
        }

        return result;
    }

    // ---------------------------------------------------------
    // Convenience: Student only sees final grades (no breakdown)
    // ---------------------------------------------------------
    public List<FinalGrade> getFinalGrades(String studentId) throws SQLException {
        return finalGradeDao.getFinalGradesForStudent(studentId);
    }

    // ---------------------------------------------------------
    // Wrapper DTO for Student Dashboard grade screen
    // (One object per section)
    // ---------------------------------------------------------
    public static class GradeView {
        private final Section section;
        private final List<Assessment> assessments;
        private final List<Score> scores;
        private final FinalGrade finalGrade;

        public GradeView(Section section,
                         List<Assessment> assessments,
                         List<Score> scores,
                         FinalGrade finalGrade) {
            this.section = section;
            this.assessments = assessments;
            this.scores = scores;
            this.finalGrade = finalGrade;
        }

        public Section getSection() { return section; }
        public List<Assessment> getAssessments() { return assessments; }
        public List<Score> getScores() { return scores; }
        public FinalGrade getFinalGrade() { return finalGrade; }
    }
}
