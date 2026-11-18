package com.allocator.test;

import com.allocator.graph.ConflictGraph;
import com.allocator.graph.GreedyColoring;
import com.allocator.graph.WelshPowellColoring;
import com.allocator.model.Allocation;
import com.allocator.model.ConflictReport;
import com.allocator.model.Resource;
import com.allocator.model.Student;
import com.allocator.model.Task;
import com.allocator.service.AllocationEngine;
import com.allocator.service.ConflictDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Automated Verification & Sample Test Cases Suite for the Graph Coloring Resource Allocator.
 * Can be executed directly via: java -cp out com.allocator.test.GraphColoringTestSuite
 */
public class GraphColoringTestSuite {

    public static void main(String[] args) {
        System.out.println("=======================================================================");
        System.out.println("  GRAPH COLORING RESOURCE ALLOCATION - AUTOMATED TEST SUITE");
        System.out.println("=======================================================================\n");

        runTestCase1_CompleteGraphK4();
        runTestCase2_BipartiteCycleC4();
        runTestCase3_UniversityExamTimetableAndRoomAllocation();
        runTestCase4_ManualModificationGuard();

        System.out.println("\n=======================================================================");
        System.out.println("  ALL 4 SAMPLE TEST SUITES PASSED WITH ZERO ADJACENCY VIOLATIONS!");
        System.out.println("=======================================================================");
    }

    /**
     * Test Case 1: Complete Graph K4 (4 tasks all sharing Student #1).
     * Expected Chromatic Number X(K4) = 4.
     */
    private static void runTestCase1_CompleteGraphK4() {
        System.out.println("[TEST CASE 1] Complete Graph K4 (4 mutually conflicting exams)");
        ConflictGraph graph = new ConflictGraph();
        for (int i = 1; i <= 4; i++) {
            Task t = new Task(i, "CS10" + i, "Course " + i, "CS", 180, Resource.ResourceType.CLASSROOM, "Dr. A");
            graph.addVertex(t);
        }
        // Connect every pair
        for (int i = 1; i <= 4; i++) {
            for (int j = i + 1; j <= 4; j++) {
                graph.addEdge(i, j, "Shared Student S1", 1);
            }
        }

        WelshPowellColoring wp = new WelshPowellColoring();
        Map<Integer, Integer> colors = wp.colorGraph(graph);
        int distinct = wp.countDistinctColors(colors);

        assertValidColoring(graph, colors);
        if (distinct != 4) {
            throw new AssertionError("Expected 4 colors for K4, got " + distinct);
        }
        System.out.println("  -> Passed: |V|=4, |E|=6, Chromatic Number X(K4) = " + distinct + "\n");
    }

    /**
     * Test Case 2: Even Cycle Graph C4 (1-2-3-4-1).
     * Expected Chromatic Number X(C4) = 2 (Bipartite Graph).
     */
    private static void runTestCase2_BipartiteCycleC4() {
        System.out.println("[TEST CASE 2] Even Cycle Graph C4 (Bipartite 4-Cycle)");
        ConflictGraph graph = new ConflictGraph();
        for (int i = 1; i <= 4; i++) {
            Task t = new Task(i, "EC20" + i, "Module " + i, "EC", 120, Resource.ResourceType.LAB, "Prof. B");
            graph.addVertex(t);
        }
        graph.addEdge(1, 2, "Shared Student", 1);
        graph.addEdge(2, 3, "Shared Student", 1);
        graph.addEdge(3, 4, "Shared Student", 1);
        graph.addEdge(4, 1, "Shared Student", 1);

        GreedyColoring greedy = new GreedyColoring();
        Map<Integer, Integer> colors = greedy.colorGraph(graph);
        int distinct = greedy.countDistinctColors(colors);

        assertValidColoring(graph, colors);
        if (distinct != 2) {
            throw new AssertionError("Expected 2 colors for C4, got " + distinct);
        }
        System.out.println("  -> Passed: |V|=4, |E|=4, Chromatic Number X(C4) = " + distinct + "\n");
    }

    /**
     * Test Case 3: End-to-End Conflict Detection + Welsh-Powell + Room Capacity Allocation.
     */
    private static void runTestCase3_UniversityExamTimetableAndRoomAllocation() {
        System.out.println("[TEST CASE 3] End-to-End University Conflict Detection & Room Allocation");
        List<Student> students = Arrays.asList(
            new Student(1, "CS001", "Aarav", "a@u.edu", "CS", 5),
            new Student(2, "CS002", "Diya", "d@u.edu", "CS", 5),
            new Student(3, "EC003", "Kabir", "k@u.edu", "EC", 5)
        );

        Task t1 = new Task(1, "CS301", "Algorithms", "CS", 180, Resource.ResourceType.CLASSROOM, "Dr. Ramanujan");
        t1.setEnrolledStudentIds(Arrays.asList(1, 2));

        Task t2 = new Task(2, "CS302", "Operating Systems", "CS", 180, Resource.ResourceType.LAB, "Prof. Subramanian");
        t2.setEnrolledStudentIds(Arrays.asList(1, 2));

        Task t3 = new Task(3, "EC305", "Embedded Systems", "EC", 180, Resource.ResourceType.LAB, "Prof. Gopalan");
        t3.setEnrolledStudentIds(Arrays.asList(3));

        List<Task> tasks = Arrays.asList(t1, t2, t3);

        List<Resource> resources = Arrays.asList(
            new Resource(1, "RM-101", "Lecture Hall 101", Resource.ResourceType.CLASSROOM, 60, "Block A", true),
            new Resource(2, "LAB-301", "Computing Lab 1", Resource.ResourceType.LAB, 40, "Tech Tower", true)
        );

        AllocationEngine engine = new AllocationEngine();
        AllocationEngine.AllocationResult result = engine.generateSchedule(
            tasks, students, resources, AllocationEngine.AlgorithmMode.WELSH_POWELL
        );

        if (result.getConflicts().size() != 1) {
            throw new AssertionError("Expected 1 conflict edge (CS301 <-> CS302), got " + result.getConflicts().size());
        }
        if (result.getChromaticNumber() != 2) {
            throw new AssertionError("Expected 2 time slots, got " + result.getChromaticNumber());
        }
        System.out.println("  -> Passed: Detected " + result.getConflicts().size()
            + " conflict edge(s), allocated in " + result.getChromaticNumber() + " time slots.\n");
    }

    /**
     * Test Case 4: Manual Override Clash Prevention Guard.
     */
    private static void runTestCase4_ManualModificationGuard() {
        System.out.println("[TEST CASE 4] Manual Modification Conflict Guard Verification");
        ConflictDetector detector = new ConflictDetector();
        AllocationEngine engine = new AllocationEngine();

        Student s1 = new Student(1, "CS001", "Aarav", "a@u.edu", "CS", 5);
        Task t1 = new Task(1, "CS301", "Algorithms", "CS", 180, Resource.ResourceType.CLASSROOM, "Dr. A");
        t1.addStudentId(1);
        Task t2 = new Task(2, "CS302", "DBMS", "CS", 180, Resource.ResourceType.CLASSROOM, "Dr. B");
        t2.addStudentId(1);

        List<Task> tasks = Arrays.asList(t1, t2);
        List<ConflictReport> conflicts = detector.detectAllConflicts(tasks, Arrays.asList(s1));
        ConflictGraph graph = detector.buildConflictGraph(tasks, conflicts);

        List<Allocation> current = new ArrayList<>();
        current.add(new Allocation(1, 1, "CS301", "Algorithms", "Dr. A", 1, 0, "Slot 1", 1, "RM-101", "Hall 1", 60, "WELSH_POWELL", false));
        current.add(new Allocation(2, 2, "CS302", "DBMS", "Dr. B", 1, 1, "Slot 2", 2, "RM-102", "Hall 2", 60, "WELSH_POWELL", false));

        // Attempt to move CS302 (taskId=2) into Slot 0 (colorIndex=0) where conflicting CS301 sits
        String error = engine.validateManualModification(2, 0, 2, current, graph);
        if (error == null) {
            throw new AssertionError("Expected Conflict Guard to block moving conflicting task into same color slot!");
        }
        System.out.println("  -> Passed: Guard blocked illegal manual override with message:\n     \"" + error + "\"");
    }

    private static void assertValidColoring(ConflictGraph graph, Map<Integer, Integer> colorMap) {
        for (com.allocator.graph.Edge edge : graph.getEdges()) {
            int c1 = colorMap.getOrDefault(edge.getSourceTaskId(), -1);
            int c2 = colorMap.getOrDefault(edge.getTargetTaskId(), -2);
            if (c1 == c2 || c1 < 0 || c2 < 0) {
                throw new AssertionError("Adjacent vertices " + edge.getSourceTaskId()
                    + " and " + edge.getTargetTaskId() + " received identical color " + c1);
            }
        }
    }
}
