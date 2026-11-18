package com.allocator.graph;

import java.util.*;

/**
 * Implementation of the Standard Greedy Graph Coloring Algorithm.
 *
 * ALGORITHM EXPLANATION:
 * 1. Processes vertices sequentially in their insertion order: v_1, v_2, ..., v_n.
 * 2. Assigns Color 0 to the first vertex v_1.
 * 3. For each remaining vertex u:
 *    a. Inspects all adjacent neighbors w in Adj[u].
 *    b. Marks every color currently assigned to any neighbor w as unavailable in a boolean[] array.
 *    c. Finds the smallest non-negative color index c >= 0 that is available and assigns it to u.
 *
 * COMPLEXITY ANALYSIS:
 * - Time Complexity: O(V^2 + E) in the worst case (iterating over V vertices, checking degree(u)
 *   neighbors, and scanning up to V boolean availability flags). With an adjacency list, checking
 *   neighbors across the entire run visits 2E directed edges, yielding O(V + E) work per scan.
 * - Space Complexity: O(V + E) to store the adjacency list, color assignment map, and boolean
 *   availability array of size V.
 */
public class GreedyColoring {

    private final List<String> executionTrace = new ArrayList<>();

    /**
     * Colors the given ConflictGraph using Standard Greedy Coloring.
     *
     * @param graph the ConflictGraph containing tasks and conflict edges
     * @return Map of taskId -> assigned non-negative colorIndex (0, 1, 2, ...)
     */
    public Map<Integer, Integer> colorGraph(ConflictGraph graph) {
        executionTrace.clear();
        graph.resetColors();

        List<Vertex> vertexList = new ArrayList<>(graph.getVertices());
        int n = vertexList.size();
        Map<Integer, Integer> colorAssignments = new LinkedHashMap<>();

        if (n == 0) {
            return colorAssignments;
        }

        // Boolean array to track which colors are already used by adjacent vertices
        boolean[] availableColors = new boolean[n];
        Arrays.fill(availableColors, true);

        // Assign the first color (0) to the first vertex
        Vertex first = vertexList.get(0);
        first.setColorIndex(0);
        colorAssignments.put(first.getId(), 0);
        executionTrace.add(String.format(
            "Step 1: Initialized vertex %s (deg=%d) -> Assigned Color #0",
            first.getTask().getCode(), first.getDegree()
        ));

        // Assign colors to remaining V - 1 vertices
        for (int i = 1; i < n; i++) {
            Vertex current = vertexList.get(i);
            List<Integer> neighbors = graph.getNeighbors(current.getId());

            // Mark colors of adjacent vertices as unavailable
            List<String> blockedBy = new ArrayList<>();
            for (int neighborId : neighbors) {
                Vertex neighbor = graph.getVertex(neighborId);
                if (neighbor != null && neighbor.isColored()) {
                    int usedColor = neighbor.getColorIndex();
                    if (usedColor >= 0 && usedColor < n) {
                        availableColors[usedColor] = false;
                        blockedBy.add(neighbor.getTask().getCode() + "[C" + usedColor + "]");
                    }
                }
            }

            // Find the lowest available color index
            int chosenColor = 0;
            while (chosenColor < n && !availableColors[chosenColor]) {
                chosenColor++;
            }

            current.setColorIndex(chosenColor);
            colorAssignments.put(current.getId(), chosenColor);

            executionTrace.add(String.format(
                "Step %d: Vertex %s (deg=%d) | Blocked by: %s -> Assigned Color #%d",
                (i + 1),
                current.getTask().getCode(),
                current.getDegree(),
                blockedBy.isEmpty() ? "None" : String.join(", ", blockedBy),
                chosenColor
            ));

            // Reset availability flags for the next iteration
            Arrays.fill(availableColors, true);
        }

        return colorAssignments;
    }

    public int countDistinctColors(Map<Integer, Integer> colorMap) {
        return new HashSet<>(colorMap.values()).size();
    }

    public List<String> getExecutionTrace() {
        return Collections.unmodifiableList(executionTrace);
    }
}
