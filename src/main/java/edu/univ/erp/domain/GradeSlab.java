package edu.univ.erp.domain;

public class GradeSlab {
    private final int id;
    private final int sectionId;
    private final String letter;
    private final double min;
    private final double max;
    //constructor
    public GradeSlab(int id, int sectionId, String letter, double min, double max) {
        this.id = id;
        this.sectionId = sectionId;
        this.letter = letter;
        this.min = min;
        this.max = max;
    }
    //getters
    public int getId() { return id; }
    public int getSectionId() { return sectionId; }
    public String getLetter() { return letter; }
    public double getMin() { return min; }
    public double getMax() { return max; }
}
