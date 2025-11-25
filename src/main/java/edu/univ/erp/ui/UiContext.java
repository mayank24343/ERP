package edu.univ.erp.ui;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import edu.univ.erp.access.AccessManager;
import edu.univ.erp.service.*;

import javax.sql.DataSource;
import edu.univ.erp.util.*;

public class UiContext {

    private static UiContext instance;
    private final DataSource authDS;
    private final DataSource erpDS;

    // Services
    private final AuthService authService;
    private final UserService userService;
    private final StudentService studentService;
    private final InstructorService instructorService;
    private final AdminService adminService;

    // Access Manager (shared)
    private final AccessManager accessManager;

    public UiContext() {
        this.authDS = DataSourceProvider.getAuthDataSource();
        this.erpDS = DataSourceProvider.getERPDataSource();

        // Access manager uses ERP datasource
        this.accessManager = new AccessManager(erpDS);

        // Services
        this.authService = new AuthService(authDS);
        this.userService = new UserService(authDS, erpDS);
        this.studentService = new StudentService(erpDS, accessManager);
        this.instructorService = new InstructorService(erpDS, accessManager);
        this.adminService = new AdminService(authDS, erpDS, accessManager);
    }

    // -----------------------------
    // SINGLETON INSTANCE
    // -----------------------------
    public static synchronized UiContext get() {
        if (instance == null) {
            instance = new UiContext();
        }
        return instance;
    }

    // -----------------------------
    // GETTERS
    // -----------------------------
    public AuthService auth() { return authService; }
    public UserService users() { return userService; }
    public StudentService students() { return studentService; }
    public InstructorService instructors() { return instructorService; }
    public AdminService admin() { return adminService; }

    public AccessManager access() { return accessManager; }

    // -----------------------------
    // HIKARI DATASOURCE CREATORS

}
