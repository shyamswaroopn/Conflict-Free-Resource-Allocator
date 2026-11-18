package com.allocator.ui.views;

import com.allocator.dao.StudentDAO;
import com.allocator.dao.TaskDAO;
import com.allocator.model.Resource;
import com.allocator.model.Student;
import com.allocator.model.Task;
import com.allocator.ui.components.ModernTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Exam & Task Management View.
 * Allows creating/updating Tasks (Graph Vertices) and assigning enrolled Students
 * (which automatically induce Conflict Edges when two tasks share students).
 */
public class TaskView extends JPanel {
    private final TaskDAO taskDAO = new TaskDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    private final DefaultTableModel tableModel;
    private final ModernTable table;
    private final DefaultListModel<Student> studentListModel = new DefaultListModel<>();
    private final JList<Student> studentMultiList = new JList<>(studentListModel);

    private List<Task> currentTasks = new ArrayList<>();
    private Integer selectedTaskId = null;

    private final JTextField codeField = new JTextField(10);
    private final JTextField titleField = new JTextField(18);
    private final JComboBox<String> deptBox = new JComboBox<>(new String[]{
        "Computer Science", "Electronics", "Information Tech", "AI & Data Science", "Mathematics", "Humanities"
    });
    private final JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(180, 60, 300, 30));
    private final JComboBox<Resource.ResourceType> reqTypeBox = new JComboBox<>(Resource.ResourceType.values());
    private final JTextField instructorField = new JTextField(15);

    public TaskView() {
        setLayout(new BorderLayout(0, 18));
        setBackground(new Color(15, 23, 42));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Exam & Task Management (Graph Vertices & Student Enrollments)");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        // Top Split: Left Form, Right Multi-select Student List
        JPanel topSplit = new JPanel(new BorderLayout(16, 0));
        topSplit.setBackground(new Color(30, 41, 59));
        topSplit.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setBackground(new Color(30, 41, 59));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(formGrid, gbc, 0, "Course/Task Code:", codeField);
        addField(formGrid, gbc, 1, "Task / Exam Title:", titleField);
        addField(formGrid, gbc, 2, "Department:", deptBox);
        addField(formGrid, gbc, 3, "Duration (Mins):", durationSpinner);
        addField(formGrid, gbc, 4, "Required Room Type:", reqTypeBox);
        addField(formGrid, gbc, 5, "Instructor / Proctor:", instructorField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(new Color(30, 41, 59));
        JButton addBtn = createBtn("Add Task Vertex", new Color(37, 99, 235));
        JButton updateBtn = createBtn("Update Task", new Color(5, 150, 105));
        JButton deleteBtn = createBtn("Delete Task", new Color(220, 38, 38));
        JButton clearBtn = createBtn("Clear", new Color(71, 85, 105));

        addBtn.addActionListener(e -> handleAdd());
        updateBtn.addActionListener(e -> handleUpdate());
        deleteBtn.addActionListener(e -> handleDelete());
        clearBtn.addActionListener(e -> clearForm());

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        formGrid.add(btnPanel, gbc);

        topSplit.add(formGrid, BorderLayout.CENTER);

        // Student Multi-Select Panel
        JPanel enrollmentBox = new JPanel(new BorderLayout(0, 6));
        enrollmentBox.setBackground(new Color(30, 41, 59));
        enrollmentBox.setPreferredSize(new Dimension(320, 170));
        JLabel enrollLbl = new JLabel("Enrolled Students (Hold Ctrl/Cmd to multi-select):");
        enrollLbl.setForeground(new Color(203, 213, 225));
        enrollLbl.setFont(new Font("SansSerif", Font.BOLD, 12));

        studentMultiList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        studentMultiList.setBackground(new Color(15, 23, 42));
        studentMultiList.setForeground(Color.WHITE);
        JScrollPane listScroll = new JScrollPane(studentMultiList);
        listScroll.setBorder(BorderFactory.createLineBorder(new Color(71, 85, 105)));

        enrollmentBox.add(enrollLbl, BorderLayout.NORTH);
        enrollmentBox.add(listScroll, BorderLayout.CENTER);
        topSplit.add(enrollmentBox, BorderLayout.EAST);

        // Table
        String[] cols = {"ID", "Code", "Title", "Department", "Duration", "Room Type", "Instructor", "Enrolled Students"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new ModernTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                Task t = currentTasks.get(table.getSelectedRow());
                selectedTaskId = t.getId();
                codeField.setText(t.getCode());
                titleField.setText(t.getTitle());
                deptBox.setSelectedItem(t.getDepartment());
                durationSpinner.setValue(t.getDurationMins());
                reqTypeBox.setSelectedItem(t.getRequiredResourceType());
                instructorField.setText(t.getInstructor());

                // Highlight enrolled students in JList
                List<Integer> indices = new ArrayList<>();
                for (int i = 0; i < studentListModel.size(); i++) {
                    if (t.getEnrolledStudentIds().contains(studentListModel.getElementAt(i).getId())) {
                        indices.add(i);
                    }
                }
                int[] idxArr = indices.stream().mapToInt(Integer::intValue).toArray();
                studentMultiList.setSelectedIndices(idxArr);
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.getViewport().setBackground(new Color(15, 23, 42));
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(new Color(15, 23, 42));
        body.add(topSplit, BorderLayout.NORTH);
        body.add(tableScroll, BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int idx, String label, JComponent comp) {
        gbc.gridx = (idx % 2) * 2;
        gbc.gridy = idx / 2;
        gbc.gridwidth = 1;
        JLabel l = new JLabel(label);
        l.setForeground(new Color(203, 213, 225));
        panel.add(l, gbc);
        gbc.gridx = (idx % 2) * 2 + 1;
        panel.add(comp, gbc);
    }

    private JButton createBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        return b;
    }

    public void loadTasksAndStudents() {
        try {
            List<Student> students = studentDAO.findAll();
            studentListModel.clear();
            for (Student s : students) {
                studentListModel.addElement(s);
            }

            currentTasks = taskDAO.findAllWithEnrollments();
            tableModel.setRowCount(0);
            for (Task t : currentTasks) {
                tableModel.addRow(new Object[]{
                    t.getId(), t.getCode(), t.getTitle(), t.getDepartment(),
                    t.getDurationMins() + " min", t.getRequiredResourceType().name(),
                    t.getInstructor(), t.getEnrollmentCount() + " enrolled"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateForm() {
        if (codeField.getText().trim().isEmpty() || titleField.getText().trim().isEmpty() || instructorField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Task Code, Title, and Instructor are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void handleAdd() {
        if (!validateForm()) return;
        try {
            Task t = new Task(
                codeField.getText().trim(),
                titleField.getText().trim(),
                String.valueOf(deptBox.getSelectedItem()),
                (Integer) durationSpinner.getValue(),
                (Resource.ResourceType) reqTypeBox.getSelectedItem(),
                instructorField.getText().trim()
            );
            for (Student s : studentMultiList.getSelectedValuesList()) {
                t.addStudentId(s.getId());
            }
            taskDAO.insertWithEnrollments(t);
            clearForm();
            loadTasksAndStudents();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to add task: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (selectedTaskId == null) {
            JOptionPane.showMessageDialog(this, "Select a task from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateForm()) return;
        try {
            Task t = new Task(
                selectedTaskId,
                codeField.getText().trim(),
                titleField.getText().trim(),
                String.valueOf(deptBox.getSelectedItem()),
                (Integer) durationSpinner.getValue(),
                (Resource.ResourceType) reqTypeBox.getSelectedItem(),
                instructorField.getText().trim()
            );
            for (Student s : studentMultiList.getSelectedValuesList()) {
                t.addStudentId(s.getId());
            }
            taskDAO.updateWithEnrollments(t);
            clearForm();
            loadTasksAndStudents();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to update task: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (selectedTaskId == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected Task and its enrollments?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                taskDAO.delete(selectedTaskId);
                clearForm();
                loadTasksAndStudents();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete task: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedTaskId = null;
        codeField.setText("");
        titleField.setText("");
        instructorField.setText("");
        studentMultiList.clearSelection();
        table.clearSelection();
    }
}
