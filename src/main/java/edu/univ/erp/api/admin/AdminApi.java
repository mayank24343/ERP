package edu.univ.erp.api.admin;

import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.ServiceException;
import edu.univ.erp.service.UserService;

import java.sql.SQLException;
import java.util.List;

public class AdminApi {
    private final AdminService service;
    private final UserService userService;

    //constructor
    public AdminApi(AdminService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    //list of instructors
    public ApiResult<List<Instructor>> listInstructors() {
        try {
            return ApiResult.ok(userService.loadAllInstructors());
        } catch (SQLException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    //list of sections
    public ApiResult<List<Section>> listSections() {
        try {
            return ApiResult.ok(service.listSections());
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    //list of courses
    public ApiResult<List<Course>> listCourses() {
        try {
            return ApiResult.ok(service.listCourses());
        } catch (SQLException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    //add user
    public ApiResult<Void> addUser(String fullName, String username, String password, String role, String rollNo, String program, Integer year, String department, String designation) {
        try {
            service.addUser(fullName, username, password, role, rollNo, program, year, department, designation);
            return ApiResult.okMessage("User Created Successfully.");
        }
        catch (Exception e) {
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

    //add a section
    public ApiResult<Void> addSection(int courseId, String instructorId, String dayTime, String room, int capacity, String semester, int year) {
        try {
            service.addSection(courseId, instructorId, dayTime, room, capacity, semester, year);
            return ApiResult.okMessage("Section created.");
        } catch (SQLException | ServiceException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    //update course
    public ApiResult<Void> updateCourse(String code, String title, int credits) {
        try {
            service.updateCourse(code, title, credits);
            return ApiResult.okMessage("Course Updated.");
        } catch (SQLException | ServiceException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    //update section
    public ApiResult<Void> updateSection(int sectionId, int courseId, String instructorId, String dayTime, String room, int capacity, String semester, int year){
        try {
            service.updateSection(sectionId, courseId, instructorId, dayTime, room, capacity, semester, year);
            return ApiResult.okMessage("Section updated.");
        }
        catch (SQLException | ServiceException e) {
            return ApiResult.error(e.getMessage());
        }

    }

    //delete section
    public ApiResult<Void> deleteSection(int sectionId) {
        try {
            service.deleteSection(sectionId);
            return  ApiResult.okMessage("Section deleted.");
        } catch ( Exception e){
            return ApiResult.error(e.getMessage());
        }
    }

    //delete course
    public ApiResult<Void> deleteCourse(int courseId) {
        try {
            service.deleteCourse(courseId);
            return ApiResult.okMessage("Course deleted.");
        } catch ( Exception e){
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
