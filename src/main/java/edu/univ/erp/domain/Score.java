package edu.univ.erp.domain;

public class Score {
    private final int assessmentId;
    private final String studentId;
    private final double marksObtained;

    public Score(int assessmentId, String studentId, double marksObtained) {
        this.assessmentId = assessmentId;
        this.studentId = studentId;
        this.marksObtained = marksObtained;
    }

    public int getAssessmentId() { return assessmentId; }
    public String getStudentId() { return studentId; }
    public double getMarksObtained() { return marksObtained; }
}
