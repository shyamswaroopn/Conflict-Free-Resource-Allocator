package com.allocator.ui.views;

import com.allocator.dao.StudentDAO;
import com.allocator.model.Student;
import com.allocator.ui.components.ModernTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Student Management View supporting Add, Edit, Delete, and View operations backed by MySQL JDBC.
 */
public class StudentView extends JPanel {
    private final StudentDAO studentDAO = new StudentDAO();
    private final DefaultTableModel tableModel;
    private final ModernTable table;
    private List<Student> currentStudents = new ArrayList<>();

    private final JTextField rollField = new JTextField(12);
    private final JTextField nameField = new JTextField(16);
    private final JTextField emailField = new JTextField(18);
    private final JComboBox<String> deptBox = new JComboBox<>(new String[]{
        "Computer Science", "Electronics", "Information Tech", "AI & Data Science", "Mathematics", "Humanities"
    });
    private final JSpinner semesterSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 8, 1));
    private Integer selectedStudentId = null;

    public StudentView() {
        setLayout(new BorderLayout(0, 18));
        setBackground(new Color(15, 23, 42));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JLabel title = new JLabel("Student Management (Add, Edit, Delete & View)");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(30, 41, 59));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            new EmptyBorder(16, 16, 16, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFormField(formPanel, gbc, 0, "Roll Number:", rollField);
        addFormField(formPanel, gbc, 1, "Full Name:", nameField);
        addFormField(formPanel, gbc, 2, "Email Address:", emailField);
        addFormField(formPanel, gbc, 3, "Department:", deptBox);
        addFormField(formPanel, gbc, 4, "Semester (1-8):", semesterSpinner);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setBackground(new Color(30, 41, 59));

        JButton addBtn = createButton("Add Student", new Color(37, 99, 235));
        JButton updateBtn = createButton("Update Selected", new Color(5, 150, 105));
        JButton deleteBtn = createButton("Delete Selected", new Color(220, 38, 38));
        JButton clearBtn = createButton("Clear Form", new Color(71, 85, 105));

        addBtn.addActionListener(e -> handleAdd());
        updateBtn.addActionListener(e -> handleUpdate());
        deleteBtn.addActionListener(e -> handleDelete());
        clearBtn.addActionListener(e -> clearForm());

        btnRow.add(addBtn);
        btnRow.add(updateBtn);
        btnRow.add(deleteBtn);
        btnRow.add(clearBtn);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 6;
        formPanel.add(btnRow, gbc);

        // Table
        String[] cols = {"ID", "Roll Number", "Student Name", "Email", "Department", "Semester"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new ModernTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                Student s = currentStudents.get(table.getSelectedRow());
                selectedStudentId = s.getId();
                rollField.setText(s.getRollNo());
                nameField.setText(s.getName());
                emailField.setText(s.getEmail());
                deptBox.setSelectedItem(s.getDepartment());
                semesterSpinner.setValue(s.getSemester());
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(15, 23, 42));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(new Color(15, 23, 42));
        body.add(formPanel, BorderLayout.NORTH);
        body.add(scrollPane, BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int index, String labelText, JComponent comp) {
        int row = index / 3;
        int col = (index % 3) * 2;
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(new Color(203, 213, 225));
        panel.add(lbl, gbc);

        gbc.gridx = col + 1;
        panel.add(comp, gbc);
    }

    private JButton createButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        return b;
    }

    public void loadStudents() {
        try {
            currentStudents = studentDAO.findAll();
            tableModel.setRowCount(0);
            for (Student s : currentStudents) {
                tableModel.addRow(new Object[]{
                    s.getId(), s.getRollNo(), s.getName(), s.getEmail(), s.getDepartment(), s.getSemester()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateInputs() {
        if (rollField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Roll Number, Full Name, and Email are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!emailField.getText().contains("@")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void handleAdd() {
        if (!validateInputs()) return;
        try {
            Student s = new Student(
                rollField.getText().trim(),
                nameField.getText().trim(),
                emailField.getText().trim(),
                String.valueOf(deptBox.getSelectedItem()),
                (Integer) semesterSpinner.getValue()
            );
            studentDAO.insert(s);
            clearForm();
            loadStudents();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to add student: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (selectedStudentId == null) {
            JOptionPane.showMessageDialog(this, "Select a student from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateInputs()) return;
        try {
            Student s = new Student(
                selectedStudentId,
                rollField.getText().trim(),
                nameField.getText().trim(),
                emailField.getText().trim(),
                String.valueOf(deptBox.getSelectedItem()),
                (Integer) semesterSpinner.getValue()
            );
            studentDAO.update(s);
            clearForm();
            loadStudents();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to update student: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (selectedStudentId == null) {
            JOptionPane.showMessageDialog(this, "Select a student to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected student and remove their exam enrollments?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                studentDAO.delete(selectedStudentId);
                clearForm();
                loadStudents();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete student: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedStudentId = null;
        rollField.setText("");
        nameField.setText("");
        emailField.setText("");
        semesterSpinner.setValue(5);
        table.clearSelection();
    }
}
