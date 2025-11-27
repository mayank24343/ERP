package edu.univ.erp.api.instructor;

import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.InstructorService;

import java.util.List;

public class InstructorApi {
    private final InstructorService service;
    //constructor
    public InstructorApi(InstructorService service) {
        this.service = service;
    }

    //sections taught by instructor
    public ApiResult<List<Section>> getMySections(String instructorId) {
        try {
            return ApiResult.ok(service.getMySections(instructorId));
        } catch (Exception e) {
            return ApiResult.error("Failed To Load Sections: " + e.getMessage());
        }
    }

    //get component wise & student wise grades for section
    public ApiResult<InstructorService.SectionGradeSummary> getGradebook(int sectionId,  String instructorId) {
        try {
            return ApiResult.ok(service.getSectionGradeSummary(sectionId, instructorId));
        } catch (Exception e) {
            return ApiResult.error("Failed To Load Grades: " + e.getMessage());
        }
    }

    //see preview of final grades
    public ApiResult<List<FinalGrade>> previewFinalGrades(int sectionId,  String instructorId) {
        try {
            return ApiResult.ok(service.computeFinalsPreview(sectionId,  instructorId));
        } catch (Exception e) {
            return ApiResult.error("Failed To Compute Final Grades: " + e.getMessage());
        }
    }

    //save final grades
    public ApiResult<String> finalizeGrades(int sectionId,  String instructorId) {
        try {
            service.computeAndStoreFinals(sectionId,   instructorId);
            return ApiResult.okMessage("Final grades submitted and course marked completed.");
        } catch (Exception e) {
            return ApiResult.error("Failed to finalize grades: " + e.getMessage());
        }
    }

    //save scores to db
    public ApiResult<String> saveScores(List<Score> scores, String instructorId, int sectionId) {
        try {
            service.saveScores(scores, sectionId,  instructorId);
            return ApiResult.okMessage("Scores Saved.");
        } catch (Exception e) {
            return ApiResult.error("Failed To Save Scores: " + e.getMessage());
        }
    }

    //add assessment
    public ApiResult<Void> addAssessment(Assessment a, String instructorId) {
        try {
            service.addAssessment(a,  instructorId);
            return ApiResult.okMessage("Assessment Added.");
        } catch (Exception e) {
            return ApiResult.error("Failed To Add Assessment: " + e.getMessage());
        }
    }

    //update assessment
    public ApiResult<Void> updateAssessment(Assessment a, String instructorId) {
        try {
            service.updateAssessment(a, instructorId);
            return ApiResult.okMessage("Assessment Updated.");
        } catch (Exception e) {
            return ApiResult.error("Failed To Update Assessment: " + e.getMessage());
        }
    }

    //delete assessment
    public ApiResult<Void> deleteAssessment(Assessment a, String instructorId) {
        try {
            service.deleteAssessment(a,  instructorId);
            return ApiResult.okMessage("Assessment Deleted.");
        } catch (Exception e) {
            return ApiResult.error("Failed To Delete Assessment: " + e.getMessage());
        }
    }

    //assessment list
    public ApiResult<List<Assessment>> listAssessments(int sectionId, String instructorId) {
        try {
            return ApiResult.ok(service.getAssessments(sectionId, instructorId));
        } catch (Exception e) {
            return ApiResult.error("Failed To Load Assessments: " + e.getMessage());
        }
    }
}
