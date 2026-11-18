package com.allocator.model;

/**
 * Represents a finalized Resource & Time Slot Allocation for a Task.
 * Stores the assigned color index (time slot), room resource, and manual override flag.
 */
public class Allocation {
    private int id;
    private int taskId;
    private String taskCode;
    private String taskTitle;
    private String instructor;
    private int studentCount;
    private int colorIndex;
    private String timeSlotLabel;
    private int resourceId;
    private String resourceCode;
    private String resourceName;
    private int resourceCapacity;
    private String algorithmUsed;
    private boolean manualOverride;

    public Allocation() {
    }

    public Allocation(int id, int taskId, String taskCode, String taskTitle, String instructor,
                      int studentCount, int colorIndex, String timeSlotLabel, int resourceId,
                      String resourceCode, String resourceName, int resourceCapacity,
                      String algorithmUsed, boolean manualOverride) {
        this.id = id;
        this.taskId = taskId;
        this.taskCode = taskCode;
        this.taskTitle = taskTitle;
        this.instructor = instructor;
        this.studentCount = studentCount;
        this.colorIndex = colorIndex;
        this.timeSlotLabel = timeSlotLabel;
        this.resourceId = resourceId;
        this.resourceCode = resourceCode;
        this.resourceName = resourceName;
        this.resourceCapacity = resourceCapacity;
        this.algorithmUsed = algorithmUsed;
        this.manualOverride = manualOverride;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    public String getTimeSlotLabel() {
        return timeSlotLabel;
    }

    public void setTimeSlotLabel(String timeSlotLabel) {
        this.timeSlotLabel = timeSlotLabel;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public int getResourceCapacity() {
        return resourceCapacity;
    }

    public void setResourceCapacity(int resourceCapacity) {
        this.resourceCapacity = resourceCapacity;
    }

    public String getAlgorithmUsed() {
        return algorithmUsed;
    }

    public void setAlgorithmUsed(String algorithmUsed) {
        this.algorithmUsed = algorithmUsed;
    }

    public boolean isManualOverride() {
        return manualOverride;
    }

    public void setManualOverride(boolean manualOverride) {
        this.manualOverride = manualOverride;
    }
}
