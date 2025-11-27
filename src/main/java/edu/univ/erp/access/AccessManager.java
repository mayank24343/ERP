package edu.univ.erp.access;

import edu.univ.erp.data.SectionDao;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.ServiceException;

import javax.sql.DataSource;

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

    //student access
    public void requireStudentAccess(String studentId) throws ServiceException {
        User u = current();//get current user
        if (!"student".equalsIgnoreCase(u.getRole()) || !u.getUserId().equals(studentId)) {
            throw new ServiceException("Access Denied.");
        }
    }

    //instructor access
    public void requireInstructorAccess(String instructorId) throws ServiceException {
        User u = current(); //get current user
        if (!"instructor".equalsIgnoreCase(u.getRole()) || !u.getUserId().equals(instructorId)) {
            throw new ServiceException("Access Denied.");
        }
    }

    //instructor & section access
    public void requireInstructorForSectionAccess(String instructorId, int sectionId) throws Exception {
        User u = current(); //get current user
        if (!"instructor".equalsIgnoreCase(u.getRole()) || !u.getUserId().equals(instructorId)) {
            throw new ServiceException("Access Denied.");
        }
        try {
            var sec = sectionDao.getSection(sectionId);
            //sections should exist and insturctor should match section
            if (sec == null) throw new ServiceException("Access Denied.");
            if (!sec.getInstructor().getUserId().equals(instructorId)) throw new ServiceException("Access Denied.");
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    //admin access
    public void requireAdminAccess() throws ServiceException {
        if (!"admin".equalsIgnoreCase(current().getRole())) {
            throw new ServiceException("Access Denied.");
        }
    }
}
