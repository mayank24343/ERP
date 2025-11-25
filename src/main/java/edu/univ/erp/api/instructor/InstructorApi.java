package edu.univ.erp.api.instructor;

import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.ServiceException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class InstructorApi {

    private final InstructorService service;

    public InstructorApi(InstructorService service) {
        this.service = service;
    }

    // List my sections
    public ApiResult<List<Section>> listMySections(String instructorId) {
        try {
            return ApiResult.ok(service.listMySections(instructorId));
        } catch (SQLException e) {
            return ApiResult.error(e.getMessage());
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    // Students in my section
    public ApiResult<List<Map<String,Object>>> listStudentsInSection(int sectionId) {
        try {
            return ApiResult.ok(service.listStudentsInSection(sectionId));
        } catch (SQLException | ServiceException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    // Add component score
    public ApiResult<Void> addScore(String instructorId, int sectionId,
                                    int enrollmentId, String component, double score) {
        try {
            service.addScore(instructorId, sectionId, enrollmentId, component, score);
            return ApiResult.okMessage("Score saved.");
        } catch (SQLException | ServiceException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    // Assign final grade
    public ApiResult<Void> assignFinalGrade(String instructorId, int sectionId,
                                            int enrollmentId, String grade) {
        try {
            service.assignFinalGrade(instructorId, sectionId, enrollmentId, grade);
            return ApiResult.okMessage("Final grade assigned.");
        } catch (SQLException | ServiceException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    // Export grades CSV
    public ApiResult<List<Map<String,Object>>> exportSectionGrades(int sectionId) {
        try {
            return ApiResult.ok(service.exportSectionGrades(sectionId));
        } catch (SQLException | ServiceException e) {
            return ApiResult.error(e.getMessage());
        }
    }
}
