package com.allocator.service;

import com.allocator.config.AppConfig;
import com.allocator.graph.ConflictGraph;
import com.allocator.graph.GreedyColoring;
import com.allocator.graph.WelshPowellColoring;
import com.allocator.model.Allocation;
import com.allocator.model.ConflictReport;
import com.allocator.model.Resource;
import com.allocator.model.Student;
import com.allocator.model.Task;

import java.util.*;

/**
 * Resource & Time Slot Allocation Engine.
 * Coordinates Graph Construction, Graph Coloring (Standard Greedy or Welsh-Powell),
 * and Capacity-Aware Physical Resource Assignment within each Color Group (Time Slot).
 */
public class AllocationEngine {

    public enum AlgorithmMode {
        WELSH_POWELL("Welsh-Powell (Degree-Ordered)"),
        STANDARD_GREEDY("Standard Greedy Coloring");

        private final String displayName;

        AlgorithmMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static class AllocationResult {
        private final ConflictGraph graph;
        private final List<ConflictReport> conflicts;
        private final List<Allocation> allocations;
        private final List<String> algorithmTrace;
        private final int chromaticNumber;
        private final List<String> warnings;

        public AllocationResult(ConflictGraph graph, List<ConflictReport> conflicts,
                                List<Allocation> allocations, List<String> algorithmTrace,
                                int chromaticNumber, List<String> warnings) {
            this.graph = graph;
            this.conflicts = conflicts;
            this.allocations = allocations;
            this.algorithmTrace = algorithmTrace;
            this.chromaticNumber = chromaticNumber;
            this.warnings = warnings;
        }

        public ConflictGraph getGraph() {
            return graph;
        }

        public List<ConflictReport> getConflicts() {
            return conflicts;
        }

        public List<Allocation> getAllocations() {
            return allocations;
        }

        public List<String> getAlgorithmTrace() {
            return algorithmTrace;
        }

        public int getChromaticNumber() {
            return chromaticNumber;
        }

        public List<String> getWarnings() {
            return warnings;
        }
    }

    private final ConflictDetector conflictDetector = new ConflictDetector();
    private final GreedyColoring greedyColoring = new GreedyColoring();
    private final WelshPowellColoring welshPowellColoring = new WelshPowellColoring();

    /**
     * Runs the complete conflict detection, graph coloring, and room assignment pipeline.
     */
    public AllocationResult generateSchedule(List<Task> tasks,
                                             List<Student> students,
                                             List<Resource> availableResources,
                                             AlgorithmMode mode) {
        List<ConflictReport> conflicts = conflictDetector.detectAllConflicts(tasks, students);
        ConflictGraph graph = conflictDetector.buildConflictGraph(tasks, conflicts);

        Map<Integer, Integer> colorMap;
        List<String> trace;
        int chromaticNumber;

        if (mode == AlgorithmMode.STANDARD_GREEDY) {
            colorMap = greedyColoring.colorGraph(graph);
            trace = greedyColoring.getExecutionTrace();
            chromaticNumber = greedyColoring.countDistinctColors(colorMap);
        } else {
            colorMap = welshPowellColoring.colorGraph(graph);
            trace = welshPowellColoring.getExecutionTrace();
            chromaticNumber = welshPowellColoring.countDistinctColors(colorMap);
        }

        // Group tasks by their assigned color (Time Slot)
        Map<Integer, List<Task>> slotToTasks = new TreeMap<>();
        Map<Integer, Task> taskLookup = new HashMap<>();
        for (Task t : tasks) {
            taskLookup.put(t.getId(), t);
            int color = colorMap.getOrDefault(t.getId(), 0);
            slotToTasks.computeIfAbsent(color, k -> new ArrayList<>()).add(t);
        }

        // Filter to active resources and sort by capacity ascending (Best-Fit Room Allocation)
        List<Resource> activeRooms = new ArrayList<>();
        for (Resource r : availableResources) {
            if (r.isAvailable()) {
                activeRooms.add(r);
            }
        }
        activeRooms.sort(Comparator.comparingInt(Resource::getCapacity));

        List<Allocation> finalAllocations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Within each time slot (color), assign distinct physical rooms to concurrent tasks
        for (Map.Entry<Integer, List<Task>> entry : slotToTasks.entrySet()) {
            int currentSlotColor = entry.getKey();
            List<Task> concurrentTasks = entry.getValue();

            // Sort concurrent tasks by enrollment count descending so larger exams get priority on big rooms
            concurrentTasks.sort((a, b) -> Integer.compare(b.getEnrollmentCount(), a.getEnrollmentCount()));

            Set<Integer> usedResourceIdsInSlot = new HashSet<>();

            for (Task task : concurrentTasks) {
                Resource assignedRoom = selectBestRoom(task, activeRooms, usedResourceIdsInSlot);

                if (assignedRoom == null && !activeRooms.isEmpty()) {
                    // If all rooms in this time slot are occupied, spill over to a new slot color
                    currentSlotColor = findNextFreeSlotForRoom(task, activeRooms, finalAllocations, currentSlotColor + 1);
                    assignedRoom = activeRooms.get(activeRooms.size() - 1);
                    warnings.add("Task " + task.getCode() + " shifted to Slot " + (currentSlotColor + 1)
                        + " due to physical room exhaustion.");
                }

                if (assignedRoom != null) {
                    usedResourceIdsInSlot.add(assignedRoom.getId());
                    String slotLabel = AppConfig.getTimeSlotLabel(currentSlotColor);

                    finalAllocations.add(new Allocation(
                        0,
                        task.getId(),
                        task.getCode(),
                        task.getTitle(),
                        task.getInstructor(),
                        task.getEnrollmentCount(),
                        currentSlotColor,
                        slotLabel,
                        assignedRoom.getId(),
                        assignedRoom.getCode(),
                        assignedRoom.getName(),
                        assignedRoom.getCapacity(),
                        mode.name(),
                        false
                    ));
                } else {
                    warnings.add("No available resources found in database to allocate task " + task.getCode());
                }
            }
        }

        return new AllocationResult(
            graph,
            conflicts,
            finalAllocations,
            trace,
            chromaticNumber,
            warnings
        );
    }

    /**
     * Best-Fit Room Selection: prefers exact matching ResourceType and sufficient capacity.
     */
    private Resource selectBestRoom(Task task, List<Resource> sortedRooms, Set<Integer> usedResourceIds) {
        int neededCapacity = Math.max(task.getEnrollmentCount(), 1);

        // Pass 1: Exact ResourceType match + sufficient capacity
        for (Resource r : sortedRooms) {
            if (!usedResourceIds.contains(r.getId())
                && r.getType() == task.getRequiredResourceType()
                && r.getCapacity() >= neededCapacity) {
                return r;
            }
        }

        // Pass 2: Any available room with sufficient capacity
        for (Resource r : sortedRooms) {
            if (!usedResourceIds.contains(r.getId()) && r.getCapacity() >= neededCapacity) {
                return r;
            }
        }

        // Pass 3: Any unused room in this time slot
        for (Resource r : sortedRooms) {
            if (!usedResourceIds.contains(r.getId())) {
                return r;
            }
        }

        return null;
    }

    private int findNextFreeSlotForRoom(Task task, List<Resource> rooms, List<Allocation> existing, int startColor) {
        int candidateColor = startColor;
        while (candidateColor < 50) {
            Set<Integer> usedInCandidate = new HashSet<>();
            for (Allocation a : existing) {
                if (a.getColorIndex() == candidateColor) {
                    usedInCandidate.add(a.getResourceId());
                }
            }
            if (usedInCandidate.size() < rooms.size()) {
                return candidateColor;
            }
            candidateColor++;
        }
        return startColor;
    }

    /**
     * Validates a manual override proposed by the administrator before saving to MySQL.
     * Checks both Graph Edge conflicts (student/instructor clashes) and double-booked rooms.
     *
     * @return null if conflict-free, or a descriptive error message if a violation would occur
     */
    public String validateManualModification(int targetTaskId,
                                             int newColorIndex,
                                             int newResourceId,
                                             List<Allocation> currentAllocations,
                                             ConflictGraph graph) {
        for (Allocation other : currentAllocations) {
            if (other.getTaskId() == targetTaskId) {
                continue;
            }
            if (other.getColorIndex() == newColorIndex) {
                // Check 1: Are targetTask and other.taskId connected by a conflict edge?
                if (graph.areAdjacent(targetTaskId, other.getTaskId())) {
                    return String.format(
                        "GRAPH COLORING VIOLATION: Task conflicts with %s (%s) which is already scheduled in Slot #%d. " +
                        "Adjacent vertices cannot share the same color/time slot!",
                        other.getTaskCode(), other.getTaskTitle(), (newColorIndex + 1)
                    );
                }
                // Check 2: Are both tasks trying to use the exact same physical room in the same slot?
                if (other.getResourceId() == newResourceId) {
                    return String.format(
                        "RESOURCE DOUBLE-BOOKING: Room %s (%s) is already assigned to %s during Slot #%d!",
                        other.getResourceCode(), other.getResourceName(), other.getTaskCode(), (newColorIndex + 1)
                    );
                }
            }
        }
        return null;
    }
}
