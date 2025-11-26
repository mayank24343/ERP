package edu.univ.erp.domain;

public class FinalGrade {

    private final Student student;
    private final Section section;
    private final double percentage;
    private final String letter;

    public FinalGrade(Student student, Section section, double percentage, String letter) {
        this.student = student;
        this.section = section;
        this.percentage = percentage;
        this.letter = letter;
    }

    public Student getStudent() { return student; }
    public Section getSection() { return section; }
    public double getPercentage() { return percentage; }
    public String getLetter() { return letter; }
}

