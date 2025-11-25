package edu.univ.erp.api.admin;

import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.ServiceException;

import java.sql.SQLException;
import java.util.List;

public class AdminApi {

    private final AdminService service;

    public AdminApi(AdminService service) {
        this.service = service;
    }

    //add user
    public ApiResult<Void> addUser(String username, String password, String role,
                                   String rollNo, String program, Integer year,
                                   String department, String designation) {

        try {
            service.addUser(username, password, role, rollNo, program, year, department, designation);
            return ApiResult.okMessage("User created successfully.");
        }
        catch (SQLException | ServiceException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    //add course
    public ApiResult<Void> addCourse(String code, String title, int credits) {
        try {
            service.addCourse(code, title, credits);
            return ApiResult.okMessage("Course added.");
        } catch (SQLException | ServiceException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    public ApiResult<Void> updateCourse(String code, String title, int credits) {
        try {
            service.updateCourse(code, title, credits);
            return ApiResult.okMessage("Course Updated.");
        } catch (SQLException | ServiceException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    public ApiResult<List<Course>> listCourses() {
        try {
            return ApiResult.ok(service.listCourses());
        } catch (SQLException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    //add a section
    public ApiResult<Void> addSection(int courseId, String instructorId, String dayTime,
                                      String room, int capacity, String semester, int year) {
        try {
            service.addSection(courseId, instructorId, dayTime, room, capacity, semester, year);
            return ApiResult.okMessage("Section created.");
        } catch (SQLException | ServiceException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    //assign instructor to section
    public ApiResult<Void> assignInstructor(int sectionId, String instructorId) {
        try {
            service.assignInstructor(sectionId, instructorId);
            return ApiResult.okMessage("Instructor assigned.");
        } catch (SQLException | ServiceException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    //toggle maintenance mode
    public ApiResult<Void> setMaintenance(boolean on) {
        try {
            service.setMaintenance(on);
            return ApiResult.okMessage(on ? "Maintenance ON." : "Maintenance OFF.");
        } catch (SQLException | ServiceException e) {
            return ApiResult.error(e.getMessage());
        }
    }
}
