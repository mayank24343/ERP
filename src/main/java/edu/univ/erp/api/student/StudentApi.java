package edu.univ.erp.api.student;

import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.ServiceException;
import edu.univ.erp.service.StudentService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class StudentApi {

    private final StudentService service;

    public StudentApi(StudentService service) {
        this.service = service;
    }

    // Course list
    public ApiResult<List<Course>> listCourses() {
        try {
            return ApiResult.ok(service.listCourses());
        } catch (SQLException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    // Section list
    public ApiResult<List<Section>> listSections(int courseId) {
        try {
            return ApiResult.ok(service.listSections(courseId));
        } catch (SQLException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    // Register
    public ApiResult<Void> register(String studentId, int sectionId) {
        try {
            service.register(studentId, sectionId);
            return ApiResult.okMessage("Registered.");
        } catch (SQLException | ServiceException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    // Drop enrollment
    public ApiResult<Void> drop(String studentId, int enrollmentId) {
        try {
            service.drop(studentId, enrollmentId);
            return ApiResult.okMessage("Dropped.");
        } catch (SQLException | ServiceException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    // Get grades for one enrollment
    public ApiResult<List<Grade>> getGrades(int enrollmentId) {
        try {
            return ApiResult.ok(service.getGrades(enrollmentId));
        } catch (SQLException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    // List all enrollments (with section info)
    public ApiResult<List<Map<String,Object>>> listMyEnrollments(String studentId) {
        try {
            return ApiResult.ok(service.listMyEnrollments(studentId));
        } catch (SQLException e) {
            return ApiResult.error(e.getMessage());
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    // Transcript CSV data
    public ApiResult<List<Map<String,Object>>> exportTranscript(String studentId) {
        try {
            return ApiResult.ok(service.exportTranscript(studentId));
        } catch (SQLException e) {
            return ApiResult.error(e.getMessage());
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }
}
