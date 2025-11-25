package edu.univ.erp.service;

import edu.univ.erp.access.AccessManager;
import edu.univ.erp.access.CurrentSession;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

public class StudentService {

    private final AccessManager access;
    private final MaintenanceService maintenance;

    private final CourseDao courseDao;
    private final SectionDao sectionDao;
    private final EnrollmentDao enrollmentDao;
    private final GradeDao gradeDao;

    public StudentService(DataSource erpDS, AccessManager access) {
        this.access = access;
        this.maintenance = new MaintenanceService(erpDS);

        this.courseDao = new CourseDao(erpDS);
        this.sectionDao = new SectionDao(erpDS);
        this.enrollmentDao = new EnrollmentDao(erpDS);
        this.gradeDao = new GradeDao(erpDS);
    }


    public List<Course> listCourses() throws SQLException {
        return courseDao.getAllCourses();
    }

    public List<Section> listSections(int courseId) throws SQLException {
        return sectionDao.getSectionsByCourse(courseId);
    }


    public void register(String studentId, int sectionId)
            throws SQLException, ServiceException {

        access.requireStudent(studentId);
        maintenance.requireWriteAllowed();

        Section sec = sectionDao.getSection(sectionId);
        if (sec == null) throw new ServiceException("Section does not exist.");

        // Check seat availability
        int count = enrollmentDao.countRegistered(sectionId);
        if (count >= sec.getCapacity())
            throw new ServiceException("Section full.");

        // Prevent duplicate
        if (enrollmentDao.isAlreadyRegistered(studentId, sectionId))
            throw new ServiceException("Already registered.");

        enrollmentDao.createEnrollment(studentId, sectionId);
    }

    // -------------------
    // DROP
    // -------------------
    public void drop(String studentId, int enrollmentId)
            throws SQLException, ServiceException {

        access.requireStudent(studentId);
        maintenance.requireWriteAllowed();

        Enrollment e = enrollmentDao.getEnrollment(enrollmentId);
        if (e == null) throw new ServiceException("Enrollment not found.");

        if (!e.getStudentId().equals(studentId))
            throw new ServiceException("Cannot drop another student's course.");

        enrollmentDao.markDropped(enrollmentId);
    }

    // -------------------
    // GRADES
    // -------------------
    public List<Grade> getGrades(int enrollmentId) throws SQLException {
        return gradeDao.getGradesForEnrollment(enrollmentId);
    }

    // -------------------
    // LIST MY ENROLLMENTS
    // -------------------
    public List<Map<String,Object>> listMyEnrollments(String studentId)
            throws SQLException, ServiceException {

        access.requireStudent(studentId);
        return enrollmentDao.getEnrollmentsWithSectionData(studentId);
    }

    // -------------------
    // TRANSCRIPT
    // -------------------
    public List<Map<String,Object>> exportTranscript(String studentId)
            throws SQLException, ServiceException {

        access.requireStudent(studentId);
        return enrollmentDao.getTranscript(studentId);
    }
}
