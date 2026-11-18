package com.allocator.model;

import java.util.Objects;

/**
 * Represents a Student entity enrolled in university courses/exams.
 * Shared student enrollments across tasks generate conflict edges in the ConflictGraph.
 */
public class Student {
    private int id;
    private String rollNo;
    private String name;
    private String email;
    private String department;
    private int semester;

    public Student() {
    }

    public Student(int id, String rollNo, String name, String email, String department, int semester) {
        this.id = id;
        this.rollNo = rollNo;
        this.name = name;
        this.email = email;
        this.department = department;
        this.semester = semester;
    }

    public Student(String rollNo, String name, String email, String department, int semester) {
        this(0, rollNo, name, email, department, semester);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    @Override
    public String toString() {
        return rollNo + " - " + name + " (" + department + ", Sem " + semester + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        Student student = (Student) o;
        return id == student.id && Objects.equals(rollNo, student.rollNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rollNo);
    }
}
