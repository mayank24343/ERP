package edu.univ.erp.domain;

public class Grade {

    private final int gradeId;
    private final int enrollmentId;
    private final String component;  // "quiz", "midterm", etc.
    private final double score;
    private final String finalGrade; // e.g., "A", "B", "C"

    public Grade(int gradeId, int enrollmentId, String component,
                 double score, String finalGrade) {
        this.gradeId = gradeId;
        this.enrollmentId = enrollmentId;
        this.component = component;
        this.score = score;
        this.finalGrade = finalGrade;
    }

    public int getGradeId() { return gradeId; }
    public int getEnrollmentId() { return enrollmentId; }
    public String getComponent() { return component; }
    public double getScore() { return score; }
    public String getFinalGrade() { return finalGrade; }
}