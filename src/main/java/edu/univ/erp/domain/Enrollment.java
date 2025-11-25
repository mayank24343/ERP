package edu.univ.erp.domain;

public class Enrollment {

    private final int enrollmentId;
    private final String studentId;
    private final int sectionId;
    private final String status; // registered / dropped / completed

    public Enrollment(int enrollmentId, String studentId, int sectionId, String status) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = status;
    }

    public int getEnrollmentId() { return enrollmentId; }
    public String getStudentId() { return studentId; }
    public int getSectionId() { return sectionId; }
    public String getStatus() { return status; }
}
