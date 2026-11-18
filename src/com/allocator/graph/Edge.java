package com.allocator.graph;

import java.util.Objects;

/**
 * Represents an undirected Edge E = (u, v) in the Conflict Graph G = (V, E).
 * An edge exists between two tasks iff they share at least one enrolled student
 * or are taught/proctored by the same instructor.
 */
public class Edge {
    private final int sourceTaskId;
    private final int targetTaskId;
    private final String conflictReason;
    private final int weight;

    public Edge(int sourceTaskId, int targetTaskId, String conflictReason, int weight) {
        this.sourceTaskId = Math.min(sourceTaskId, targetTaskId);
        this.targetTaskId = Math.max(sourceTaskId, targetTaskId);
        this.conflictReason = conflictReason;
        this.weight = weight;
    }

    public int getSourceTaskId() {
        return sourceTaskId;
    }

    public int getTargetTaskId() {
        return targetTaskId;
    }

    public String getConflictReason() {
        return conflictReason;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge edge = (Edge) o;
        return sourceTaskId == edge.sourceTaskId && targetTaskId == edge.targetTaskId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceTaskId, targetTaskId);
    }

    @Override
    public String toString() {
        return String.format("Edge(%d <-> %d : %s)", sourceTaskId, targetTaskId, conflictReason);
    }
}
