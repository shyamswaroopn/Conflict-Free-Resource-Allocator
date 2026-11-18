package com.allocator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an Examination or Academic Task to be scheduled.
 * In Graph Coloring, each Task instance corresponds to a Vertex V in G = (V, E).
 */
public class Task {
    private int id;
    private String code;
    private String title;
    private String department;
    private int durationMins;
    private Resource.ResourceType requiredResourceType;
    private String instructor;
    private List<Integer> enrolledStudentIds;

    public Task() {
        this.enrolledStudentIds = new ArrayList<>();
    }

    public Task(int id, String code, String title, String department, int durationMins,
                Resource.ResourceType requiredResourceType, String instructor) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.department = department;
        this.durationMins = durationMins;
        this.requiredResourceType = requiredResourceType;
        this.instructor = instructor;
        this.enrolledStudentIds = new ArrayList<>();
    }

    public Task(String code, String title, String department, int durationMins,
                Resource.ResourceType requiredResourceType, String instructor) {
        this(0, code, title, department, durationMins, requiredResourceType, instructor);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getDurationMins() {
        return durationMins;
    }

    public void setDurationMins(int durationMins) {
        this.durationMins = durationMins;
    }

    public Resource.ResourceType getRequiredResourceType() {
        return requiredResourceType;
    }

    public void setRequiredResourceType(Resource.ResourceType requiredResourceType) {
        this.requiredResourceType = requiredResourceType;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public List<Integer> getEnrolledStudentIds() {
        return enrolledStudentIds;
    }

    public void setEnrolledStudentIds(List<Integer> enrolledStudentIds) {
        this.enrolledStudentIds = enrolledStudentIds != null ? new ArrayList<>(enrolledStudentIds) : new ArrayList<>();
    }

    public void addStudentId(int studentId) {
        if (!enrolledStudentIds.contains(studentId)) {
            enrolledStudentIds.add(studentId);
        }
    }

    public int getEnrollmentCount() {
        return enrolledStudentIds.size();
    }

    @Override
    public String toString() {
        return code + " - " + title + " (" + getEnrollmentCount() + " students)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(code, task.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }
}
