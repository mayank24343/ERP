package edu.univ.erp.domain;

public class Assessment {

    private final int id;
    private final int sectionId;
    private final String name;
    private final double maxMarks;
    private final double weight; // weight in percentage (0–100)

    public Assessment(int id, int sectionId, String name,
                      double maxMarks, double weight) {
        this.id = id;
        this.sectionId = sectionId;
        this.name = name;
        this.maxMarks = maxMarks;
        this.weight = weight;
    }

    public int getId() { return id; }
    public int getSectionId() { return sectionId; }
    public String getName() { return name; }
    public double getMaxMarks() { return maxMarks; }
    public double getWeight() { return weight; }

    @Override
    public String toString() {
        return name + " (" + weight + "%, max=" + maxMarks + ")";
    }
}
