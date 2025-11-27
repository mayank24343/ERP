package edu.univ.erp.access;

import edu.univ.erp.data.SectionDao;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.ServiceException;

import javax.sql.DataSource;
import java.sql.SQLException;

public class AccessManager {
    private final SectionDao sectionDao;

    //constructor
    public AccessManager(DataSource erpDS) {
        this.sectionDao = new SectionDao(erpDS);
    }

    //current user
    public User current() throws ServiceException {
        User u = CurrentSession.get();
        if (u == null)
            throw new ServiceException("Not logged in.");
        return u;
    }

    //admin access
    public void requireAdminAccess() throws ServiceException {
        if (!"admin".equalsIgnoreCase(current().getRole())) {
            throw new ServiceException("Access denied: Admin only.");
        }
    }

    //student access
    public void requireStudentAccess(String studentId) throws ServiceException {
        User u = current();
        if (!"student".equalsIgnoreCase(u.getRole()) || !u.getUserId().equals(studentId)) {
            throw new ServiceException("Not allowed: Student access only.");
        }
    }

    //instructor access
    public void requireInstructorAccess(String instructorId) throws ServiceException {
        User u = current();
        if (!"instructor".equalsIgnoreCase(u.getRole()) || !u.getUserId().equals(instructorId)) {
            throw new ServiceException("Not allowed: Instructor access only.");
        }
    }

    //instructor & section access
    public void requireInstructorForSectionAccess(String instructorId, int sectionId) throws ServiceException {
        User u = current();
        if (!"instructor".equalsIgnoreCase(u.getRole()) ||
                !u.getUserId().equals(instructorId)) {
            throw new ServiceException("Not allowed: Instructor access only.");
        }

        try {
            var sec = sectionDao.getSection(sectionId);
            System.out.println("DEBUG CHECK:");
            System.out.println("section.instructor = " + sec.getInstructor());
            System.out.println("instructorUser = " + current().getUserId());
            if (sec == null)
                throw new ServiceException("Section not found.");

            if (!sec.getInstructor().getUserId().equals(instructorId))
                throw new ServiceException("Not your section.");

        } catch (SQLException e) {
            throw new ServiceException(e.getMessage());
        }
    }
}
