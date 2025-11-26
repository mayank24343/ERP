package edu.univ.erp.api.student;

import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.StudentService;

import java.util.List;

public class StudentApi {

    private final StudentService service;

    public StudentApi(StudentService service) {
        this.service = service;
    }

    // ---------------------------------------------------------
    // COURSE CATALOG
    // ---------------------------------------------------------
    public ApiResult<List<Course>> catalog() {
        try {
            return ApiResult.ok(service.getCatalog());
        } catch (Exception e) {
            return ApiResult.error("Failed to load catalog: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // AVAILABLE SECTIONS FOR A COURSE
    // ---------------------------------------------------------
    public ApiResult<List<Section>> getAvailableSections(int courseId) {
        try {
            return ApiResult.ok(service.getAvailableSections(courseId));
        } catch (Exception e) {
            return ApiResult.error("Failed to load sections: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // REGISTER
    // ---------------------------------------------------------
    public ApiResult<String> register(String studentId, int sectionId) {
        try {
            service.register(studentId, sectionId);
            return ApiResult.okMessage("Registered successfully.");
        } catch (Exception e) {
            return ApiResult.error("Registration failed: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // DROP
    // ---------------------------------------------------------
    public ApiResult<String> drop(String studentId, int sectionId) {
        try {
            service.drop(studentId, sectionId);
            return ApiResult.okMessage("Section dropped.");
        } catch (Exception e) {
            return ApiResult.error("Drop failed: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // MY SECTIONS
    // ---------------------------------------------------------
    public ApiResult<List<Section>> mySections(String studentId) {
        try {
            return ApiResult.ok(service.getMySections(studentId));
        } catch (Exception e) {
            return ApiResult.error("Failed to load sections: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // TIMETABLE
    // ---------------------------------------------------------
    public ApiResult<List<Section>> timetable(String studentId) {
        try {
            return ApiResult.ok(service.getTimetable(studentId));
        } catch (Exception e) {
            return ApiResult.error("Failed to load timetable: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // GRADE BREAKDOWN (detailed)
    // ---------------------------------------------------------
    public ApiResult<List<StudentService.GradeView>> gradeBreakdown(String studentId) {
        try {
            return ApiResult.ok(service.getGradeBreakdown(studentId));
        } catch (Exception e) {
            return ApiResult.error("Failed to load grades: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // FINAL GRADES (simple)
    // ---------------------------------------------------------
    public ApiResult<List<FinalGrade>> finalGrades(String studentId) {
        try {
            return ApiResult.ok(service.getFinalGrades(studentId));
        } catch (Exception e) {
            return ApiResult.error("Failed to load final grades: " + e.getMessage());
        }
    }
}
