package edu.univ.erp.domain;

public class Section {
    private final int sectionId;
    private final int courseId;
    private final String instructorId;
    private final String dayTime;
    private final String room;
    private final int capacity;
    private final String semester;
    private final int year;

    public Section(int sectionId, int courseId, String instructorId,
                   String dayTime, String room, int capacity,
                   String semester, int year) {
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.instructorId = instructorId;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
    }

    public int getSectionId() { return sectionId; }
    public int getCourseId() { return courseId; }
    public String getInstructorId() { return instructorId; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }
    public String getSemester() { return semester; }
    public int getYear() { return year; }
}

