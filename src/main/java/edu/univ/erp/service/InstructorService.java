package edu.univ.erp.service;

import edu.univ.erp.access.AccessManager;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class InstructorService {

    private final AccessManager access;
    private final MaintenanceService maintenance;

    private final SectionDao sectionDao;
    private final EnrollmentDao enrollmentDao;
    private final GradeDao gradeDao;

    public InstructorService(DataSource erpDS, AccessManager access) {
        this.access = access;
        this.maintenance = new MaintenanceService(erpDS);

        this.sectionDao = new SectionDao(erpDS);
        this.enrollmentDao = new EnrollmentDao(erpDS);
        this.gradeDao = new GradeDao(erpDS);
    }

    // -------------------
    // My Sections
    // -------------------
    public List<Section> listMySections(String instructorId)
            throws SQLException, ServiceException {

        access.requireInstructor(instructorId);
        return sectionDao.getSectionsByInstructor(instructorId);
    }

    // -------------------
    // Students in section
    // -------------------
    public List<Map<String,Object>> listStudentsInSection(int sectionId)
            throws SQLException, ServiceException {

        String instructorId = access.current().getUserId();
        access.requireInstructorForSection(instructorId, sectionId);

        return enrollmentDao.getStudentsInSection(sectionId);
    }

    // -------------------
    // Add score
    // -------------------
    public void addScore(String instructorId, int sectionId,
                         int enrollmentId, String component, double score)
            throws SQLException, ServiceException {

        access.requireInstructorForSection(instructorId, sectionId);
        maintenance.requireWriteAllowed();

        gradeDao.addComponentScore(enrollmentId, component, score);
    }

    // -------------------
    // Final grade
    // -------------------
    public void assignFinalGrade(String instructorId, int sectionId,
                                 int enrollmentId, String grade)
            throws SQLException, ServiceException {

        access.requireInstructorForSection(instructorId, sectionId);
        maintenance.requireWriteAllowed();

        gradeDao.assignFinal(enrollmentId, grade);
    }

    // -------------------
    // Export CSV data
    // -------------------
    public List<Map<String,Object>> exportSectionGrades(int sectionId)
            throws SQLException, ServiceException {

        access.requireInstructorForSection(access.current().getUserId(), sectionId);
        return gradeDao.getSectionGrades(sectionId);
    }

}
