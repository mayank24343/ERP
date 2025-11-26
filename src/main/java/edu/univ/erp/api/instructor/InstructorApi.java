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

    //students enrolled in section
    public ApiResult<List<Student>> getEnrolledStudents(int sectionId) {
        try {
            return ApiResult.ok(service.getEnrolledStudents(sectionId));
        } catch (Exception e) {
            return ApiResult.error("Failed To Load Enrolled Students: " + e.getMessage());
        }
    }

    //assessment list
    public ApiResult<List<Assessment>> listAssessments(int sectionId) {
        try {
            return ApiResult.ok(service.getAssessments(sectionId));
        } catch (Exception e) {
            return ApiResult.error("Failed To Load Assessments: " + e.getMessage());
        }
    }

    //add assessment
    public ApiResult<Void> addAssessment(Assessment a) {
        try {
            service.addAssessment(a);
            return ApiResult.okMessage("Assessment Added.");
        } catch (Exception e) {
            return ApiResult.error("Failed To Add Assessment: " + e.getMessage());
        }
    }

    //update assessment
    public ApiResult<Void> updateAssessment(Assessment a) {
        try {
            service.updateAssessment(a);
            return ApiResult.okMessage("Assessment Updated.");
        } catch (Exception e) {
            return ApiResult.error("Failed To Update Assessment: " + e.getMessage());
        }
    }

    //delete assessment
    public ApiResult<Void> deleteAssessment(int assessmentId) {
        try {
            service.deleteAssessment(assessmentId);
            return ApiResult.okMessage("Assessment Deleted.");
        } catch (Exception e) {
            return ApiResult.error("Failed To Delete Assessment: " + e.getMessage());
        }
    }

    public ApiResult<List<Score>> getScores(int assessmentId) {
        try {
            return ApiResult.ok(service.getScoresForAssessment(assessmentId));
        } catch (Exception e) {
            return ApiResult.error("Failed to load scores: " + e.getMessage());
        }
    }

    //save scores to db
    public ApiResult<String> saveScores(List<Score> scores) {
        try {
            service.saveScores(scores);
            return ApiResult.okMessage("Scores Saved.");
        } catch (Exception e) {
            return ApiResult.error("Failed To Save Scores: " + e.getMessage());
        }
    }

   //see preview of final grades
    public ApiResult<List<FinalGrade>> previewFinalGrades(int sectionId) {
        try {
            return ApiResult.ok(service.computeFinalsPreview(sectionId));
        } catch (Exception e) {
            return ApiResult.error("Failed To Compute Final Grades: " + e.getMessage());
        }
    }

    //save final grades
    public ApiResult<String> finalizeGrades(int sectionId) {
        try {
            service.computeAndStoreFinals(sectionId);
            return ApiResult.okMessage("Final grades computed and saved.");
        } catch (Exception e) {
            return ApiResult.error("Failed to finalize grades: " + e.getMessage());
        }
    }

    //get component wise & student wise grades for section
    public ApiResult<InstructorService.SectionGradeSummary> getGradebook(int sectionId) {
        try {
            return ApiResult.ok(service.getSectionGradeSummary(sectionId));
        } catch (Exception e) {
            return ApiResult.error("Failed To Load Grades: " + e.getMessage());
        }
    }
}
