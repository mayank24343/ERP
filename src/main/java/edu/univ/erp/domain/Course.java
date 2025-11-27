package edu.univ.erp.domain;

public class Course {
    private final int courseId;
    private final String code;
    private final String title;
    private final int credits;
    //constructor
    public Course(int courseId, String code, String title, int credits) {
        this.courseId = courseId;
        this.code = code;
        this.title = title;
        this.credits = credits;
    }

    //getters
    public int getCourseId() {
        return courseId;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public int getCredits() {
        return credits;
    }
}

