package com.allocator.graph;

import com.allocator.model.Task;

import java.util.Objects;

/**
 * Represents a Vertex V in the Conflict Graph G = (V, E).
 * Wraps an academic Task/Exam, tracks its vertex degree, and stores its assigned color index.
 */
public class Vertex {
    private final Task task;
    private int degree;
    private int colorIndex; // -1 indicates uncolored

    public Vertex(Task task) {
        this.task = Objects.requireNonNull(task, "Task cannot be null");
        this.degree = 0;
        this.colorIndex = -1;
    }

    public int getId() {
        return task.getId();
    }

    public Task getTask() {
        return task;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public void incrementDegree() {
        this.degree++;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    public boolean isColored() {
        return colorIndex >= 0;
    }

    public void resetColor() {
        this.colorIndex = -1;
    }

    @Override
    public String toString() {
        return String.format("Vertex[%s, deg=%d, color=%d]", task.getCode(), degree, colorIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vertex)) return false;
        Vertex vertex = (Vertex) o;
        return getId() == vertex.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
