package edu.univ.erp.domain;

public class Section {
    private final int sectionId;
    private final Course course;
    private final Instructor instructor;
    private final String dayTime;
    private final String room;
    private final int capacity;
    private final String semester;
    private final int year;

    public Section(int sectionId, Course course, Instructor instructor, String dayTime, String room, int capacity, String semester, int year) {
        this.sectionId = sectionId;
        this.course = course;
        this.instructor = instructor;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
    }

    public int getSectionId() { return sectionId; }
    public Course getCourse() { return course; }
    public Instructor getInstructor() { return instructor; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }
    public String getSemester() { return semester; }
    public int getYear() { return year; }
}

