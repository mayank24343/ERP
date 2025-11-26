package edu.univ.erp.domain;

public class FinalGrade {

    private final String studentId;
    private final int sectionId;
    private final double percentage;
    private final String letter;

    public FinalGrade(String studentId, int sectionId,
                      double percentage, String letter) {
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.percentage = percentage;
        this.letter = letter;
    }

    public String getStudentId() { return studentId; }
    public int getSectionId() { return sectionId; }
    public double getPercentage() { return percentage; }
    public String getLetter() { return letter; }
}

