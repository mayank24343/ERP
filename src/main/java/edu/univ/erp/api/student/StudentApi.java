package edu.univ.erp.api.student;

import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.StudentService;

import java.util.List;

public class StudentApi {
    private final StudentService service;

    //constructor
    public StudentApi(StudentService service) {
        this.service = service;
    }

    //course catalog
    public ApiResult<List<Course>> catalog() {
        try {
            return ApiResult.ok(service.getCatalog());
        } catch (Exception e) {
            return ApiResult.error("Failed To Load Catalog: " + e.getMessage());
        }
    }

    //available sections for a course
    public ApiResult<List<Section>> getAvailableSections(int courseId) {
        try {
            return ApiResult.ok(service.getAvailableSections(courseId));
        } catch (Exception e) {
            return ApiResult.error("Failed To Load Sections: " + e.getMessage());
        }
    }

    //register in section
    public ApiResult<String> register(String studentId, int sectionId) {
        try {
            service.register(studentId, sectionId);
            return ApiResult.okMessage("Registered Successfully.");
        } catch (Exception e) {
            return ApiResult.error("Registration Failed: " + e.getMessage());
        }
    }

    //drop section
    public ApiResult<String> drop(String studentId, int sectionId) {
        try {
            service.drop(studentId, sectionId);
            return ApiResult.okMessage("Section Dropped.");
        } catch (Exception e) {
            return ApiResult.error("Drop Failed: " + e.getMessage());
        }
    }

    //enrolled sections for student
    public ApiResult<List<Section>> mySections(String studentId) {
        try {
            return ApiResult.ok(service.getMySections(studentId));
        } catch (Exception e) {
            return ApiResult.error("Failed To Load Sections: " + e.getMessage());
        }
    }

    //student timetable
    public ApiResult<List<Section>> timetable(String studentId) {
        try {
            return ApiResult.ok(service.getTimetable(studentId));
        } catch (Exception e) {
            return ApiResult.error("Failed To Load Timetable: " + e.getMessage());
        }
    }

    //get component wise grades for student
    public ApiResult<List<StudentService.GradeView>> gradeBreakdown(String studentId) {
        try {
            return ApiResult.ok(service.getGradeBreakdown(studentId));
        } catch (Exception e) {
            return ApiResult.error("Failed To Load Grades: " + e.getMessage());
        }
    }

   //get final grades for student
    public ApiResult<List<FinalGrade>> finalGrades(String studentId) {
        try {
            return ApiResult.ok(service.getFinalGrades(studentId));
        } catch (Exception e) {
            return ApiResult.error("Failed To Load Final Grades: " + e.getMessage());
        }
    }

    //finsihed courses
    public ApiResult<List<Section>> getCompletedSections(String studentId) {
        try {
            return ApiResult.ok(service.getCompletedSections(studentId));
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

}
