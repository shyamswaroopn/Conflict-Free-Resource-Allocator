package com.allocator.ui.views;

import com.allocator.config.AppConfig;
import com.allocator.dao.AllocationDAO;
import com.allocator.dao.ResourceDAO;
import com.allocator.dao.StudentDAO;
import com.allocator.dao.TaskDAO;
import com.allocator.graph.ConflictGraph;
import com.allocator.model.Allocation;
import com.allocator.model.ConflictReport;
import com.allocator.model.Resource;
import com.allocator.model.Student;
import com.allocator.model.Task;
import com.allocator.service.AllocationEngine;
import com.allocator.service.ConflictDetector;
import com.allocator.ui.components.ModernTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Final Allocation Schedule View & Manual Override Editor.
 * Displays the color-coded time slot and room schedule and validates manual modifications
 * against the Conflict Graph before saving to MySQL.
 */
public class AllocationView extends JPanel {
    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final ConflictDetector conflictDetector = new ConflictDetector();
    private final AllocationEngine allocationEngine = new AllocationEngine();

    private final DefaultTableModel tableModel;
    private final ModernTable table;
    private List<Allocation> currentAllocations = new ArrayList<>();
    private List<Resource> availableResources = new ArrayList<>();

    private final JComboBox<String> slotCombo = new JComboBox<>(AppConfig.STANDARD_TIME_SLOTS);
    private final JComboBox<Resource> resourceCombo = new JComboBox<>();
    private final JLabel selectedTaskLabel = new JLabel("Select a row below to manually modify its slot or room");

    public AllocationView() {
        setLayout(new BorderLayout(0, 18));
        setBackground(new Color(15, 23, 42));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Conflict-Free Resource & Time Slot Allocation Schedule");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        // Manual Modification Toolbar
        JPanel overrideBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        overrideBar.setBackground(new Color(30, 41, 59));
        overrideBar.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));

        selectedTaskLabel.setForeground(new Color(56, 189, 248));
        selectedTaskLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        overrideBar.add(selectedTaskLabel);

        JLabel slotLbl = new JLabel("Time Slot (Color):");
        slotLbl.setForeground(Color.WHITE);
        overrideBar.add(slotLbl);
        overrideBar.add(slotCombo);

        JLabel roomLbl = new JLabel("Physical Room:");
        roomLbl.setForeground(Color.WHITE);
        overrideBar.add(roomLbl);
        overrideBar.add(resourceCombo);

        JButton applyOverrideBtn = new JButton("Validate & Apply Manual Override");
        applyOverrideBtn.setBackground(new Color(5, 150, 105));
        applyOverrideBtn.setForeground(Color.WHITE);
        applyOverrideBtn.setFocusPainted(false);
        applyOverrideBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        applyOverrideBtn.addActionListener(e -> handleManualOverride());
        overrideBar.add(applyOverrideBtn);

        // Schedule Table
        String[] cols = {
            "Color #", "Time Slot Period", "Task Code", "Task / Exam Title",
            "Instructor", "Enrolled", "Assigned Room", "Capacity", "Algorithm / Mode"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new ModernTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                Allocation a = currentAllocations.get(table.getSelectedRow());
                selectedTaskLabel.setText("Editing: " + a.getTaskCode() + " (" + a.getTaskTitle() + ")");
                if (a.getColorIndex() < slotCombo.getItemCount()) {
                    slotCombo.setSelectedIndex(a.getColorIndex());
                }
                for (int i = 0; i < resourceCombo.getItemCount(); i++) {
                    if (resourceCombo.getItemAt(i).getId() == a.getResourceId()) {
                        resourceCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(15, 23, 42));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(new Color(15, 23, 42));
        body.add(overrideBar, BorderLayout.NORTH);
        body.add(scrollPane, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    public void loadAllocations() {
        try {
            availableResources = resourceDAO.findAvailableResources();
            resourceCombo.removeAllItems();
            for (Resource r : availableResources) {
                resourceCombo.addItem(r);
            }

            currentAllocations = allocationDAO.findAllDetailed();
            tableModel.setRowCount(0);
            for (Allocation a : currentAllocations) {
                tableModel.addRow(new Object[]{
                    "Color #" + (a.getColorIndex() + 1),
                    a.getTimeSlotLabel(),
                    a.getTaskCode(),
                    a.getTaskTitle(),
                    a.getInstructor(),
                    a.getStudentCount() + " students",
                    a.getResourceCode() + " - " + a.getResourceName(),
                    a.getResourceCapacity() + " seats",
                    a.isManualOverride() ? "MANUAL OVERRIDE" : a.getAlgorithmUsed()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading schedule: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleManualOverride() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an allocation row from the schedule table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Allocation target = currentAllocations.get(row);
        int newColorIndex = slotCombo.getSelectedIndex();
        String newSlotLabel = AppConfig.getTimeSlotLabel(newColorIndex);
        Resource selectedRoom = (Resource) resourceCombo.getSelectedItem();

        if (selectedRoom == null) return;

        try {
            List<Task> tasks = taskDAO.findAllWithEnrollments();
            List<Student> students = studentDAO.findAll();
            List<ConflictReport> conflicts = conflictDetector.detectAllConflicts(tasks, students);
            ConflictGraph graph = conflictDetector.buildConflictGraph(tasks, conflicts);

            String violation = allocationEngine.validateManualModification(
                target.getTaskId(),
                newColorIndex,
                selectedRoom.getId(),
                currentAllocations,
                graph
            );

            if (violation != null) {
                JOptionPane.showMessageDialog(this, violation, "Conflict Prevention Guard", JOptionPane.ERROR_MESSAGE);
                return;
            }

            allocationDAO.updateManualAllocation(target.getId(), newColorIndex, newSlotLabel, selectedRoom.getId());
            loadAllocations();
            JOptionPane.showMessageDialog(this, "Manual allocation override verified conflict-free and saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to update allocation: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
