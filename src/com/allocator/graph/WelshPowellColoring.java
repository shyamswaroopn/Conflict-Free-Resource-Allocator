package com.allocator.graph;

import java.util.*;

/**
 * Implementation of the Welsh-Powell Degree-Ordered Graph Coloring Algorithm.
 *
 * ALGORITHM EXPLANATION:
 * 1. Computes the degree deg(v) for every vertex v in G = (V, E).
 * 2. Sorts all vertices in descending order of their degrees: deg(v_1) >= deg(v_2) >= ... >= deg(v_n).
 *    (Vertices with the highest number of conflicts are prioritized first).
 * 3. Initializes currentColor = 0.
 * 4. While uncolored vertices remain:
 *    a. Picks the first uncolored vertex in the sorted list and assigns it currentColor.
 *    b. Scans down the remaining sorted list: for each uncolored vertex u, if u is NOT adjacent
 *       to ANY vertex already assigned currentColor, assigns currentColor to u as well.
 *    c. Increments currentColor++.
 *
 * COMPLEXITY ANALYSIS:
 * - Time Complexity: O(V log V + V^2 + E)
 *   Sorting vertices by degree takes O(V log V). Each outer pass colors at least one vertex,
 *   and checking adjacency against same-colored vertices across passes takes O(V^2 + E).
 * - Space Complexity: O(V + E) for the sorted vertex list, same-color set, and adjacency list.
 * - Advantage: Significantly reduces the chromatic number (total time slots required) compared
 *   to arbitrary-order Greedy Coloring on high-contention academic timetables.
 */
public class WelshPowellColoring {

    private final List<String> executionTrace = new ArrayList<>();

    /**
     * Colors the given ConflictGraph using the Welsh-Powell degree-ordered heuristic.
     *
     * @param graph the ConflictGraph containing tasks and conflict edges
     * @return Map of taskId -> assigned non-negative colorIndex (0, 1, 2, ...)
     */
    public Map<Integer, Integer> colorGraph(ConflictGraph graph) {
        executionTrace.clear();
        graph.resetColors();

        List<Vertex> sortedVertices = new ArrayList<>(graph.getVertices());
        Map<Integer, Integer> colorAssignments = new LinkedHashMap<>();

        if (sortedVertices.isEmpty()) {
            return colorAssignments;
        }

        // Step 1: Sort vertices in descending order of degree (tie-break by enrollment size, then code)
        sortedVertices.sort((v1, v2) -> {
            int degCompare = Integer.compare(v2.getDegree(), v1.getDegree());
            if (degCompare != 0) return degCompare;
            int enrollCompare = Integer.compare(
                v2.getTask().getEnrollmentCount(),
                v1.getTask().getEnrollmentCount()
            );
            if (enrollCompare != 0) return enrollCompare;
            return v1.getTask().getCode().compareTo(v2.getTask().getCode());
        });

        StringBuilder orderSummary = new StringBuilder("Degree-Sorted Order: ");
        for (int i = 0; i < sortedVertices.size(); i++) {
            Vertex v = sortedVertices.get(i);
            orderSummary.append(v.getTask().getCode()).append("(deg=").append(v.getDegree()).append(")");
            if (i < sortedVertices.size() - 1) orderSummary.append(" > ");
        }
        executionTrace.add(orderSummary.toString());

        int currentColor = 0;
        int coloredCount = 0;
        int n = sortedVertices.size();

        // Step 2: Iteratively assign currentColor to non-adjacent uncolored vertices
        while (coloredCount < n) {
            List<Vertex> coloredInThisPass = new ArrayList<>();
            List<String> passNames = new ArrayList<>();

            for (Vertex candidate : sortedVertices) {
                if (candidate.isColored()) {
                    continue;
                }

                // Check if candidate conflicts with any vertex already given currentColor
                boolean hasConflictWithCurrentColor = false;
                for (Vertex alreadyInColor : coloredInThisPass) {
                    if (graph.areAdjacent(candidate.getId(), alreadyInColor.getId())) {
                        hasConflictWithCurrentColor = true;
                        break;
                    }
                }

                if (!hasConflictWithCurrentColor) {
                    candidate.setColorIndex(currentColor);
                    colorAssignments.put(candidate.getId(), currentColor);
                    coloredInThisPass.add(candidate);
                    passNames.add(candidate.getTask().getCode() + "[deg=" + candidate.getDegree() + "]");
                    coloredCount++;
                }
            }

            executionTrace.add(String.format(
                "Color #%d Pass: Assigned non-conflicting set { %s }",
                currentColor,
                String.join(", ", passNames)
            ));

            currentColor++;
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
