package com.allocator.ui.views;

import com.allocator.dao.AllocationDAO;
import com.allocator.dao.ResourceDAO;
import com.allocator.dao.StudentDAO;
import com.allocator.dao.TaskDAO;
import com.allocator.model.Allocation;
import com.allocator.model.ConflictReport;
import com.allocator.model.Resource;
import com.allocator.model.Student;
import com.allocator.model.Task;
import com.allocator.service.ConflictDetector;
import com.allocator.ui.components.ModernTable;
import com.allocator.ui.components.StatCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Admin Dashboard View displaying live statistics, conflict reports, and allocation summaries.
 */
public class DashboardView extends JPanel {
    private final StudentDAO studentDAO = new StudentDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final ConflictDetector conflictDetector = new ConflictDetector();

    private final StatCard studentsCard;
    private final StatCard resourcesCard;
    private final StatCard tasksCard;
    private final StatCard conflictsCard;
    private final StatCard chromaticCard;
    private final DefaultTableModel conflictTableModel;

    public DashboardView(Runnable onRunAllocatorAction) {
        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(15, 23, 42));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(15, 23, 42));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setBackground(new Color(15, 23, 42));
        JLabel heading = new JLabel("Admin Control Dashboard");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Real-time overview of students, resources, conflict edges, and chromatic slot allocation");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(new Color(148, 163, 184));
        titleBox.add(heading);
        titleBox.add(sub);

        JPanel actionBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBox.setBackground(new Color(15, 23, 42));

        JButton refreshBtn = new JButton("Refresh Data");
        refreshBtn.setBackground(new Color(30, 41, 59));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> refreshMetrics());

        JButton allocateBtn = new JButton("Run Graph Coloring Allocator");
        allocateBtn.setBackground(new Color(37, 99, 235));
        allocateBtn.setForeground(Color.WHITE);
        allocateBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        allocateBtn.setFocusPainted(false);
        allocateBtn.addActionListener(e -> onRunAllocatorAction.run());

        actionBox.add(refreshBtn);
        actionBox.add(allocateBtn);

        topBar.add(titleBox, BorderLayout.WEST);
        topBar.add(actionBox, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center KPI Grid + Conflict Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setBackground(new Color(15, 23, 42));

        JPanel kpiGrid = new JPanel(new GridLayout(1, 5, 14, 0));
        kpiGrid.setBackground(new Color(15, 23, 42));

        studentsCard = new StatCard("ENROLLED STUDENTS", "0", "Active university records", new Color(56, 189, 248));
        resourcesCard = new StatCard("PHYSICAL RESOURCES", "0", "Classrooms, Labs, Halls", new Color(52, 211, 153));
        tasksCard = new StatCard("TASKS / EXAMS (V)", "0", "Vertices in Conflict Graph", new Color(167, 139, 250));
        conflictsCard = new StatCard("CONFLICT EDGES (E)", "0", "Mutual scheduling clashes", new Color(251, 113, 133));
        chromaticCard = new StatCard("CHROMATIC SLOTS X(G)", "0", "Distinct time slots used", new Color(250, 204, 21));

        kpiGrid.add(studentsCard);
        kpiGrid.add(resourcesCard);
        kpiGrid.add(tasksCard);
        kpiGrid.add(conflictsCard);
        kpiGrid.add(chromaticCard);
        centerPanel.add(kpiGrid, BorderLayout.NORTH);

        // Automatic Conflict Detection Table
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(new Color(15, 23, 42));

        JLabel tableHeading = new JLabel("Detected Pairwise Conflict Edges E = (u, v)");
        tableHeading.setFont(new Font("SansSerif", Font.BOLD, 15));
        tableHeading.setForeground(Color.WHITE);
        tableContainer.add(tableHeading, BorderLayout.NORTH);

        String[] cols = {"Vertex U (Task 1)", "Vertex V (Task 2)", "Conflict Type", "Edge Weight", "Detailed Causality"};
        conflictTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ModernTable conflictTable = new ModernTable(conflictTableModel);
        JScrollPane scrollPane = new JScrollPane(conflictTable);
        scrollPane.getViewport().setBackground(new Color(15, 23, 42));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tableContainer, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshMetrics() {
        try {
            List<Student> students = studentDAO.findAll();
            List<Resource> resources = resourceDAO.findAll();
            List<Task> tasks = taskDAO.findAllWithEnrollments();
            List<Allocation> allocations = allocationDAO.findAllDetailed();
            List<ConflictReport> conflicts = conflictDetector.detectAllConflicts(tasks, students);

            studentsCard.setValue(String.valueOf(students.size()));
            resourcesCard.setValue(String.valueOf(resources.size()));
            tasksCard.setValue(String.valueOf(tasks.size()));
            conflictsCard.setValue(String.valueOf(conflicts.size()));

            Set<Integer> distinctColors = new HashSet<>();
            for (Allocation a : allocations) {
                distinctColors.add(a.getColorIndex());
            }
            chromaticCard.setValue(String.valueOf(distinctColors.size()));

            conflictTableModel.setRowCount(0);
            for (ConflictReport c : conflicts) {
                conflictTableModel.addRow(new Object[]{
                    c.getTaskACode() + " - " + c.getTaskATitle(),
                    c.getTaskBCode() + " - " + c.getTaskBTitle(),
                    c.getConflictType().name(),
                    c.getConflictWeight(),
                    c.getDescription()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "MySQL Database Connection Error:\n" + ex.getMessage() +
                "\n\nPlease verify MySQL is running and sql/schema.sql has been executed.",
                "Database Connectivity Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
