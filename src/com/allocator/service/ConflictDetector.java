package com.allocator.service;

import com.allocator.graph.ConflictGraph;
import com.allocator.model.ConflictReport;
import com.allocator.model.Student;
import com.allocator.model.Task;

import java.util.*;

/**
 * Automatic Conflict Detector Service.
 * Scans all Tasks and Student Enrollments to identify mutual conflicts:
 * 1. Shared Student Enrollments (a student cannot sit for two exams simultaneously).
 * 2. Shared Instructor / Proctor (an instructor cannot supervise two simultaneous tasks).
 *
 * Constructs both human-readable ConflictReports and the undirected ConflictGraph G = (V, E).
 */
public class ConflictDetector {

    /**
     * Analyzes all tasks and students to detect every pairwise scheduling conflict.
     */
    public List<ConflictReport> detectAllConflicts(List<Task> tasks, List<Student> students) {
        List<ConflictReport> reports = new ArrayList<>();
        Map<Integer, Student> studentLookup = new HashMap<>();
        for (Student s : students) {
            studentLookup.put(s.getId(), s);
        }

        int n = tasks.size();
        for (int i = 0; i < n; i++) {
            Task taskA = tasks.get(i);
            Set<Integer> enrolledA = new HashSet<>(taskA.getEnrolledStudentIds());

            for (int j = i + 1; j < n; j++) {
                Task taskB = tasks.get(j);
                List<String> sharedRollNos = new ArrayList<>();

                for (Integer studentId : taskB.getEnrolledStudentIds()) {
                    if (enrolledA.contains(studentId)) {
                        Student s = studentLookup.get(studentId);
                        sharedRollNos.add(s != null ? s.getRollNo() : ("ID#" + studentId));
                    }
                }

                boolean sameInstructor = taskA.getInstructor() != null
                    && !taskA.getInstructor().trim().isEmpty()
                    && taskA.getInstructor().trim().equalsIgnoreCase(taskB.getInstructor().trim());

                if (!sharedRollNos.isEmpty() || sameInstructor) {
                    ConflictReport.ConflictType type;
                    StringBuilder desc = new StringBuilder();

                    if (!sharedRollNos.isEmpty() && sameInstructor) {
                        type = ConflictReport.ConflictType.COMBINED_CONFLICT;
                        desc.append(sharedRollNos.size()).append(" shared student(s) [")
                            .append(String.join(", ", sharedRollNos))
                            .append("] + Same Instructor (").append(taskA.getInstructor()).append(")");
                    } else if (!sharedRollNos.isEmpty()) {
                        type = ConflictReport.ConflictType.SHARED_STUDENTS;
                        desc.append(sharedRollNos.size()).append(" shared student(s): ")
                            .append(String.join(", ", sharedRollNos));
                    } else {
                        type = ConflictReport.ConflictType.SAME_INSTRUCTOR;
                        desc.append("Shared Instructor: ").append(taskA.getInstructor());
                    }

                    reports.add(new ConflictReport(
                        taskA.getId(), taskA.getCode(), taskA.getTitle(),
                        taskB.getId(), taskB.getCode(), taskB.getTitle(),
                        type, sharedRollNos,
                        sameInstructor ? taskA.getInstructor() : null,
                        desc.toString()
                    ));
                }
            }
        }
        return reports;
    }

    /**
     * Builds a complete ConflictGraph G = (V, E) from the tasks and detected conflicts.
     */
    public ConflictGraph buildConflictGraph(List<Task> tasks, List<ConflictReport> conflictReports) {
        ConflictGraph graph = new ConflictGraph();
        for (Task task : tasks) {
            graph.addVertex(task);
        }
        for (ConflictReport report : conflictReports) {
            graph.addEdge(
                report.getTaskAId(),
                report.getTaskBId(),
                report.getDescription(),
                report.getConflictWeight()
            );
        }
        return graph;
    }
}
