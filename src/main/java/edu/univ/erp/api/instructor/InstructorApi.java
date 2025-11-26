package edu.univ.erp.api;

import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.InstructorService;

import java.util.List;

public class InstructorApi {

    private final InstructorService service;

    public InstructorApi(InstructorService service) {
        this.service = service;
    }

    // ---------------------------------------------------------
    // SECTIONS TAUGHT BY INSTRUCTOR
    // ---------------------------------------------------------
    public ApiResult<List<Section>> getMySections(String instructorId) {
        try {
            return ApiResult.ok(service.getMySections(instructorId));
        } catch (Exception e) {
            return ApiResult.error("Failed to load sections: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // STUDENT ROSTER
    // ---------------------------------------------------------
    public ApiResult<List<Student>> getEnrolledStudents(int sectionId) {
        try {
            return ApiResult.ok(service.getEnrolledStudents(sectionId));
        } catch (Exception e) {
            return ApiResult.error("Failed to load enrolled students: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // ASSESSMENT OPERATIONS
    // ---------------------------------------------------------
    public ApiResult<List<Assessment>> listAssessments(int sectionId) {
        try {
            return ApiResult.ok(service.getAssessments(sectionId));
        } catch (Exception e) {
            return ApiResult.error("Failed to load assessments: " + e.getMessage());
        }
    }

    public ApiResult<String> addAssessment(Assessment a) {
        try {
            service.addAssessment(a);
            return ApiResult.okMessage("Assessment added.");
        } catch (Exception e) {
            return ApiResult.error("Failed to add assessment: " + e.getMessage());
        }
    }

    public ApiResult<String> updateAssessment(Assessment a) {
        try {
            service.updateAssessment(a);
            return ApiResult.okMessage("Assessment updated.");
        } catch (Exception e) {
            return ApiResult.error("Failed to update assessment: " + e.getMessage());
        }
    }

    public ApiResult<String> deleteAssessment(int assessmentId) {
        try {
            service.deleteAssessment(assessmentId);
            return ApiResult.okMessage("Assessment deleted.");
        } catch (Exception e) {
            return ApiResult.error("Failed to delete assessment: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // SCORES / GRADEBOOK
    // ---------------------------------------------------------
    public ApiResult<List<Score>> getScores(int assessmentId) {
        try {
            return ApiResult.ok(service.getScoresForAssessment(assessmentId));
        } catch (Exception e) {
            return ApiResult.error("Failed to load scores: " + e.getMessage());
        }
    }

    public ApiResult<String> saveScore(Score s) {
        try {
            service.saveScore(s);
            return ApiResult.okMessage("Score saved.");
        } catch (Exception e) {
            return ApiResult.error("Failed to save score: " + e.getMessage());
        }
    }

    public ApiResult<String> saveScores(List<Score> scores) {
        try {
            service.saveScores(scores);
            return ApiResult.okMessage("Scores saved.");
        } catch (Exception e) {
            return ApiResult.error("Failed to save scores: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // FINAL GRADES
    // ---------------------------------------------------------
    public ApiResult<List<FinalGrade>> previewFinalGrades(int sectionId) {
        try {
            return ApiResult.ok(service.computeFinalsPreview(sectionId));
        } catch (Exception e) {
            return ApiResult.error("Failed to compute final grades: " + e.getMessage());
        }
    }

    public ApiResult<String> finalizeGrades(int sectionId) {
        try {
            service.computeAndStoreFinals(sectionId);
            return ApiResult.okMessage("Final grades computed and saved.");
        } catch (Exception e) {
            return ApiResult.error("Failed to finalize grades: " + e.getMessage());
        }
    }

    public ApiResult<FinalGrade> getFinalGrade(int sectionId, String studentId) {
        try {
            return ApiResult.ok(service.getFinalGrade(sectionId, studentId));
        } catch (Exception e) {
            return ApiResult.error("Failed to get final grade: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // FULL GRADEBOOK SUMMARY (ENTIRE SECTION)
    // ---------------------------------------------------------
    public ApiResult<InstructorService.SectionGradeSummary> getGradebook(int sectionId) {
        try {
            return ApiResult.ok(service.getSectionGradeSummary(sectionId));
        } catch (Exception e) {
            return ApiResult.error("Failed to load gradebook: " + e.getMessage());
        }
    }
}
