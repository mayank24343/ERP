package edu.univ.erp.service;

import edu.univ.erp.access.AccessManager;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.*;
import org.mindrot.jbcrypt.BCrypt;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class AdminService {
    private final AccessManager access;

    private final AuthDao authDao;
    private final StudentDao studentDao;
    private final InstructorDao instructorDao;
    private final CourseDao courseDao;
    private final SectionDao sectionDao;
    private final MaintenanceService maintenanceService;

    //constructor
    public AdminService(DataSource authDS, DataSource erpDS, AccessManager access) {
        this.access = access;

        this.authDao = new AuthDao(authDS);
        this.studentDao = new StudentDao(erpDS);
        this.instructorDao = new InstructorDao(erpDS);
        this.courseDao = new CourseDao(erpDS);
        this.sectionDao = new SectionDao(erpDS);
        this.maintenanceService = new MaintenanceService(erpDS);

    }

    //add user
    public void addUser(String fullName, String username, String password, String role, String rollNo, String program, Integer year, String department, String designation) throws SQLException, ServiceException {

        access.requireAdmin();
        maintenanceService.requireWriteAllowed();

        if (username.isEmpty() || password.isEmpty() || role.isEmpty()) throw new ServiceException("Username and password and role are required.");
        role = role.toLowerCase().trim();
        if (!List.of("student", "instructor", "admin").contains(role))
            throw new ServiceException("Invalid role: " + role);
        if (role == "student"){
            if (rollNo.isEmpty() || program.isEmpty()) throw new ServiceException("Roll No and Program are required.");
            if (year <= 0) throw new ServiceException("Year must be greater than 0.");
        }
        if (role == "instructor"){
            if (department.isEmpty()) throw new ServiceException("Department is required.");
            if (designation.isEmpty()) throw new ServiceException("Designation is required.");
        }

        //generate user_id (UUID)
        String userId = UUID.randomUUID().toString();
        //add to auth_db
        authDao.addUser(fullName,username,password,role,userId);

        //insert into erp_db
        switch (role) {
            case "student":
                studentDao.insertStudent(userId, rollNo, program, year);
                break;

            case "instructor":
                instructorDao.insertInstructor(userId, department, designation);
                break;

            case "admin":
                //not in erp table
                break;
        }
    }

    //add course
    public void addCourse(String code, String title, int credits) throws SQLException, ServiceException {

        access.requireAdmin();
        maintenanceService.requireWriteAllowed();

        if (code.isEmpty()) throw new ServiceException("Code cannot be empty.");
        if (title.isEmpty()) throw new ServiceException("Title cannot be empty.");
        if (credits <= 0) throw new ServiceException("Credits must be positive.");

        courseDao.insertCourse(code, title, credits);
    }

    //update course
    public void updateCourse(String code, String title, int credits) throws SQLException, ServiceException {
        access.requireAdmin();
        maintenanceService.requireWriteAllowed();
        if (code.isEmpty()) throw new ServiceException("Code cannot be empty.");
        if (title.isEmpty()) throw new ServiceException("Title cannot be empty.");
        if (credits <= 0) throw new ServiceException("Credits must be positive.");

        courseDao.updateCourse(code, title, credits);
    }

    //list courses
    public List<Course> listCourses() throws SQLException {
        return courseDao.findAllCourses();
    }

    //list all sections
    public List<Section> listSections() throws SQLException {
        return sectionDao.getAllSections();
    }

    //add section
    public void addSection(int courseId, String instructorId, String dayTime, String room, int capacity, String semester, int year) throws SQLException, ServiceException {
        access.requireAdmin();
        maintenanceService.requireWriteAllowed();

        if (capacity <= 0)
            throw new ServiceException("Capacity must be positive.");

        sectionDao.insertSection(courseId, instructorId, dayTime, room, capacity, semester, year);
    }

    public void updateSection(int sectionID, int courseId, String instructorId, String dayTime, String room, int capacity, String semester, int year) throws SQLException, ServiceException {
        access.requireAdmin();
        maintenanceService.requireWriteAllowed();

        if (capacity <= 0)
            throw new ServiceException("Capacity must be positive.");

        sectionDao.updateSection(sectionID, courseId, instructorId, dayTime, room, capacity, semester, year);
    }

    //assign instructor
    public void assignInstructor(int sectionId, String instructorId) throws SQLException, ServiceException {

        access.requireAdmin();
        maintenanceService.requireWriteAllowed();

        sectionDao.updateInstructor(sectionId, instructorId);
    }

    //maintenance mode toggle
    public void setMaintenance(boolean on) throws SQLException, ServiceException {

        access.requireAdmin();
        if (on){
            maintenanceService.turnOn();
        }
        else {
            maintenanceService.turnOff();
        }
    }
}