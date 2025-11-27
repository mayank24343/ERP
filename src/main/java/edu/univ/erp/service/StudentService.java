package edu.univ.erp.service;

import edu.univ.erp.access.AccessManager;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.*;
import edu.univ.erp.ui.UiContext;

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
    private final MaintenanceService maintenanceService;

    //constructor
    public StudentService(DataSource ds, AccessManager a) {

        this.courseDao = new CourseDao(ds);
        this.sectionDao = new SectionDao(ds);
        this.enrollmentDao = new EnrollmentDao(ds);
        this.assessmentDao = new AssessmentDao(ds);
        this.scoreDao = new ScoreDao(ds);
        this.finalGradeDao = new FinalGradeDao(ds, assessmentDao, scoreDao, enrollmentDao);
        this.maintenanceService = new MaintenanceService(ds);
    }

    //course catalog
    public List<Course> getCatalog() throws SQLException {
        return courseDao.findAllCourses();
    }

    //sections for registration
    public List<Section> getAvailableSections(int courseId) throws SQLException {
        return sectionDao.findSectionsForRegistration(courseId);
    }

    //register student
    public void register(String studentId, int sectionId) throws Exception {
        //throw error if after drop deadline or not enrolled in course
        if (LocalDate.now().isAfter(UiContext.get().adddrop().getDeadline().getData())) {
            throw new Exception("Registration deadline has passed.");
        }

        //maintenance should be off
        maintenanceService.requireWriteAllowed();

        //access management
        UiContext.get().access().requireStudentAccess(studentId);

        //throw error if already enrolled or section full
        if (enrollmentDao.isAlreadyEnrolled(studentId, sectionId)) {
            throw new Exception("You are already registered in this section.");
        }

        if (!sectionDao.hasSeat(sectionId)) {
            throw new Exception("Section is full.");
        }

        //register student
        enrollmentDao.register(studentId, sectionId);
    }

    //drop course
    public void drop(String studentId, int sectionId) throws Exception {
        //maintenance should be off
        maintenanceService.requireWriteAllowed();

        //access management
        UiContext.get().access().requireStudentAccess(studentId);

        //throw error if after drop deadline or not enrolled in course
        if (LocalDate.now().isAfter(UiContext.get().adddrop().getDeadline().getData())) {
            throw new Exception("Drop deadline has passed.");
        }

        if (!enrollmentDao.isAlreadyEnrolled(studentId, sectionId)) {
            throw new Exception("You are not enrolled in this section.");
        }

        //drop course
        enrollmentDao.drop(studentId, sectionId);
    }

    //enrolled sections
    public List<Section> getMySections(String studentId) throws Exception {
        //access management
        UiContext.get().access().requireStudentAccess(studentId);
        return sectionDao.getSectionsForStudent(studentId);
    }

    //student timetable
    public List<Section> getTimetable(String studentId) throws Exception {
        //access management
        UiContext.get().access().requireStudentAccess(studentId);
        return sectionDao.getSectionsForStudent(studentId);
    }

    //finished courses
    public List<Section> getCompletedSections(String studentId) throws SQLException {
        return enrollmentDao.getCompletedSections(studentId);
    }

    //component wise grades
    public List<GradeView> getGradeBreakdown(String studentId) throws Exception {
        //access management
        UiContext.get().access().requireStudentAccess(studentId);
        List<GradeView> result = new ArrayList<>();

        //enrolled sections
        List<Section> sections = sectionDao.getSectionsForStudent(studentId);

        //for each section, get component scores and final grade
        for (Section s : sections) {
            List<Assessment> assessments = assessmentDao.getBySection(s.getSectionId());

            List<Score> scores = new ArrayList<>();
            for (Assessment a : assessments) {
                Score sc = scoreDao.getScore(a.getId(), studentId);
                if (sc != null) scores.add(sc);
            }

            FinalGrade finalGrade = finalGradeDao.computeForStudent(s.getSectionId(), studentId);
            result.add(new GradeView(s, assessments, scores, finalGrade));
        }

        return result;
    }

    //final grades for student
    public List<FinalGrade> getFinalGrades(String studentId) throws Exception {
        //access management
        UiContext.get().access().requireStudentAccess(studentId);
        return finalGradeDao.getFinalGradesForStudent(studentId);
    }

    //component wise grades for each enrolled section
    public static class GradeView {
        private final Section section;
        private final List<Assessment> assessments;
        private final List<Score> scores;
        private final FinalGrade finalGrade;

        public GradeView(Section section, List<Assessment> assessments, List<Score> scores, FinalGrade finalGrade) {
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
