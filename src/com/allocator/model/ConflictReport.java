package com.allocator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates a detected Conflict between two Tasks.
 * Used both to construct Graph Edges and to populate the Admin Conflict Report View.
 */
public class ConflictReport {
    public enum ConflictType {
        SHARED_STUDENTS,
        SAME_INSTRUCTOR,
        COMBINED_CONFLICT
    }

    private int taskAId;
    private String taskACode;
    private String taskATitle;
    private int taskBId;
    private String taskBCode;
    private String taskBTitle;
    private ConflictType conflictType;
    private List<String> sharedStudentRollNos;
    private String sharedInstructor;
    private String description;

    public ConflictReport(int taskAId, String taskACode, String taskATitle,
                          int taskBId, String taskBCode, String taskBTitle,
                          ConflictType conflictType, List<String> sharedStudentRollNos,
                          String sharedInstructor, String description) {
        this.taskAId = taskAId;
        this.taskACode = taskACode;
        this.taskATitle = taskATitle;
        this.taskBId = taskBId;
        this.taskBCode = taskBCode;
        this.taskBTitle = taskBTitle;
        this.conflictType = conflictType;
        this.sharedStudentRollNos = sharedStudentRollNos != null ? new ArrayList<>(sharedStudentRollNos) : new ArrayList<>();
        this.sharedInstructor = sharedInstructor;
        this.description = description;
    }

    public int getTaskAId() {
        return taskAId;
    }

    public String getTaskACode() {
        return taskACode;
    }

    public String getTaskATitle() {
        return taskATitle;
    }

    public int getTaskBId() {
        return taskBId;
    }

    public String getTaskBCode() {
        return taskBCode;
    }

    public String getTaskBTitle() {
        return taskBTitle;
    }

    public ConflictType getConflictType() {
        return conflictType;
    }

    public List<String> getSharedStudentRollNos() {
        return sharedStudentRollNos;
    }

    public String getSharedInstructor() {
        return sharedInstructor;
    }

    public String getDescription() {
        return description;
    }

    public int getConflictWeight() {
        int weight = sharedStudentRollNos.size();
        if (sharedInstructor != null && !sharedInstructor.isEmpty()) {
            weight += 5;
        }
        return Math.max(weight, 1);
    }
}
