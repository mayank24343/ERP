package edu.univ.erp.domain;

import java.sql.Timestamp;

public class Student extends User {
    private final String rollNo;
    private final String program;
    private final int year;
    //constructors
    public Student(String fullName, String userId, String username, String role, String passwordHash, String status, int failedAttempts, Timestamp lockedUntil, Timestamp lastLogin, String rollNo, String program, int year) {
        super(fullName,userId, username, role, passwordHash, status, failedAttempts, lockedUntil, lastLogin);
        this.rollNo = rollNo;
        this.program = program;
        this.year = year;
    }
    //getters
    public String getRollNo() {
        return rollNo;
    }
    public String getProgram() {
        return program;
    }

    public int getYear() {
        return year;
    }
}
