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
    private final UserDao userDao;
    private final StudentDao studentDao;
    private final InstructorDao instructorDao;
    private final CourseDao courseDao;
    private final SectionDao sectionDao;
    private final MaintenanceService maintenanceService;

    public AdminService(DataSource authDS, DataSource erpDS, AccessManager access) {
        this.access = access;

        this.authDao = new AuthDao(authDS);
        this.userDao = new UserDao(authDS, erpDS);
        this.studentDao = new StudentDao(erpDS);
        this.instructorDao = new InstructorDao(erpDS);
        this.courseDao = new CourseDao(erpDS);
        this.sectionDao = new SectionDao(erpDS);
        this.maintenanceService = new MaintenanceService(erpDS);
    }

    //add user
    public void addUser(String username, String password, String role, String rollNo, String program, Integer year, String department, String designation)
            throws SQLException, ServiceException {

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

        //password hash
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));

        //insert into auth DB
        String authSql = "INSERT INTO auth_users (user_id, username, role, password_hash, status, failed_attempts) " + "VALUES (?, ?, ?, ?, 'active', 0)";

        try (var conn = authDao.getConnection();
             var ps = conn.prepareStatement(authSql)) {
            ps.setString(1, userId);
            ps.setString(2, username);
            ps.setString(3, role);
            ps.setString(4, hash);
            ps.executeUpdate();
        }

        //insert into erp DB
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
    public void addCourse(String code, String title, int credits)
            throws SQLException, ServiceException {

        access.requireAdmin();
        maintenanceService.requireWriteAllowed();

        if (code.isEmpty()) throw new ServiceException("Code cannot be empty.");
        if (title.isEmpty()) throw new ServiceException("Title cannot be empty.");
        if (credits <= 0) throw new ServiceException("Credits must be positive.");

        courseDao.insertCourse(code, title, credits);
    }

    public void updateCourse(String code, String title, int credits)
            throws SQLException, ServiceException {
        access.requireAdmin();
        maintenanceService.requireWriteAllowed();
        if (code.isEmpty()) throw new ServiceException("Code cannot be empty.");
        if (title.isEmpty()) throw new ServiceException("Title cannot be empty.");
        if (credits <= 0) throw new ServiceException("Credits must be positive.");

        courseDao.updateCourse(code, title, credits);
    }

    public List<Course> listCourses() throws SQLException {
        return courseDao.getAllCourses();
    }

    //add section
    public void addSection(int courseId, String instructorId, String dayTime,
                           String room, int capacity, String semester, int year)
            throws SQLException, ServiceException {

        access.requireAdmin();
        maintenanceService.requireWriteAllowed();

        if (capacity <= 0)
            throw new ServiceException("Capacity must be positive.");

        sectionDao.insertSection(courseId, instructorId, dayTime, room, capacity, semester, year);
    }

    //assign instructor
    public void assignInstructor(int sectionId, String instructorId)
            throws SQLException, ServiceException {

        access.requireAdmin();
        maintenanceService.requireWriteAllowed();

        sectionDao.updateInstructor(sectionId, instructorId);
    }

    //maintenance mode toggle
    public void setMaintenance(boolean on)
            throws SQLException, ServiceException {

        access.requireAdmin();
        if (on){
            maintenanceService.turnOn();
        }
        else {
            maintenanceService.turnOff();
        }
    }

    //add-drop period toggle


}
