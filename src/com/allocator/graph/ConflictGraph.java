package com.allocator.graph;

import com.allocator.model.Task;

import java.util.*;

/**
 * Undirected Graph G = (V, E) represented using a Core Java Adjacency List
 * (HashMap<Integer, ArrayList<Integer>>) and Vertex lookup table.
 *
 * Used to model mutual resource and student scheduling conflicts across Tasks.
 */
public class ConflictGraph {
    private final Map<Integer, Vertex> vertices;
    private final Map<Integer, List<Integer>> adjacencyList;
    private final List<Edge> edges;

    public ConflictGraph() {
        this.vertices = new LinkedHashMap<>();
        this.adjacencyList = new LinkedHashMap<>();
        this.edges = new ArrayList<>();
    }

    /**
     * Adds a Task as a Vertex in the conflict graph.
     */
    public void addVertex(Task task) {
        if (!vertices.containsKey(task.getId())) {
            vertices.put(task.getId(), new Vertex(task));
            adjacencyList.put(task.getId(), new ArrayList<>());
        }
    }

    /**
     * Adds an undirected conflict edge between taskA and taskB if not already present.
     */
    public void addEdge(int taskAId, int taskBId, String reason, int weight) {
        if (taskAId == taskBId) return;
        if (!vertices.containsKey(taskAId) || !vertices.containsKey(taskBId)) {
            return;
        }

        List<Integer> neighborsA = adjacencyList.get(taskAId);
        if (!neighborsA.contains(taskBId)) {
            neighborsA.add(taskBId);
            adjacencyList.get(taskBId).add(taskAId);

            vertices.get(taskAId).incrementDegree();
            vertices.get(taskBId).incrementDegree();

            edges.add(new Edge(taskAId, taskBId, reason, weight));
        }
    }

    public boolean areAdjacent(int taskAId, int taskBId) {
        List<Integer> neighbors = adjacencyList.get(taskAId);
        return neighbors != null && neighbors.contains(taskBId);
    }

    public List<Integer> getNeighbors(int taskId) {
        return adjacencyList.getOrDefault(taskId, Collections.emptyList());
    }

    public Vertex getVertex(int taskId) {
        return vertices.get(taskId);
    }

    public Collection<Vertex> getVertices() {
        return vertices.values();
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public Map<Integer, List<Integer>> getAdjacencyList() {
        return Collections.unmodifiableMap(adjacencyList);
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public int getMaxDegree() {
        int max = 0;
        for (Vertex v : vertices.values()) {
            if (v.getDegree() > max) {
                max = v.getDegree();
            }
        }
        return max;
    }

    public void resetColors() {
        for (Vertex v : vertices.values()) {
            v.resetColor();
        }
    }
}
