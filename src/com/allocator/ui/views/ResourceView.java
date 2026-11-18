package com.allocator.ui.views;

import com.allocator.dao.ResourceDAO;
import com.allocator.model.Resource;
import com.allocator.ui.components.ModernTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Physical Resource Management View (Classrooms, Laboratories, Auditoriums).
 */
public class ResourceView extends JPanel {
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final DefaultTableModel tableModel;
    private final ModernTable table;
    private List<Resource> currentResources = new ArrayList<>();

    private final JTextField codeField = new JTextField(10);
    private final JTextField nameField = new JTextField(16);
    private final JComboBox<Resource.ResourceType> typeBox = new JComboBox<>(Resource.ResourceType.values());
    private final JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(60, 10, 500, 5));
    private final JTextField buildingField = new JTextField(14);
    private final JCheckBox availableCheck = new JCheckBox("Available for Scheduling", true);
    private Integer selectedResourceId = null;

    public ResourceView() {
        setLayout(new BorderLayout(0, 18));
        setBackground(new Color(15, 23, 42));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Physical Resource Management (Rooms, Labs & Auditoriums)");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(30, 41, 59));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            new EmptyBorder(16, 16, 16, 16)
        ));

        availableCheck.setBackground(new Color(30, 41, 59));
        availableCheck.setForeground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(formPanel, gbc, 0, "Room Code:", codeField);
        addField(formPanel, gbc, 1, "Room Name:", nameField);
        addField(formPanel, gbc, 2, "Resource Type:", typeBox);
        addField(formPanel, gbc, 3, "Seat Capacity:", capacitySpinner);
        addField(formPanel, gbc, 4, "Building / Wing:", buildingField);
        addField(formPanel, gbc, 5, "Status:", availableCheck);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setBackground(new Color(30, 41, 59));

        JButton addBtn = createButton("Add Resource", new Color(37, 99, 235));
        JButton updateBtn = createButton("Update Resource", new Color(5, 150, 105));
        JButton deleteBtn = createButton("Delete Resource", new Color(220, 38, 38));
        JButton clearBtn = createButton("Clear", new Color(71, 85, 105));

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

        String[] cols = {"ID", "Code", "Resource Name", "Type", "Capacity", "Building", "Available"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new ModernTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                Resource r = currentResources.get(table.getSelectedRow());
                selectedResourceId = r.getId();
                codeField.setText(r.getCode());
                nameField.setText(r.getName());
                typeBox.setSelectedItem(r.getType());
                capacitySpinner.setValue(r.getCapacity());
                buildingField.setText(r.getBuilding());
                availableCheck.setSelected(r.isAvailable());
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

    private void addField(JPanel panel, GridBagConstraints gbc, int idx, String label, JComponent comp) {
        gbc.gridx = (idx % 3) * 2;
        gbc.gridy = idx / 3;
        gbc.gridwidth = 1;
        JLabel l = new JLabel(label);
        l.setForeground(new Color(203, 213, 225));
        panel.add(l, gbc);
        gbc.gridx = (idx % 3) * 2 + 1;
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

    public void loadResources() {
        try {
            currentResources = resourceDAO.findAll();
            tableModel.setRowCount(0);
            for (Resource r : currentResources) {
                tableModel.addRow(new Object[]{
                    r.getId(), r.getCode(), r.getName(), r.getType().name(),
                    r.getCapacity(), r.getBuilding(), r.isAvailable() ? "Yes" : "Offline"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading resources: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateInput() {
        if (codeField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty() || buildingField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Room Code, Name, and Building are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void handleAdd() {
        if (!validateInput()) return;
        try {
            Resource r = new Resource(
                codeField.getText().trim(),
                nameField.getText().trim(),
                (Resource.ResourceType) typeBox.getSelectedItem(),
                (Integer) capacitySpinner.getValue(),
                buildingField.getText().trim(),
                availableCheck.isSelected()
            );
            resourceDAO.insert(r);
            clearForm();
            loadResources();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to add resource: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (selectedResourceId == null) {
            JOptionPane.showMessageDialog(this, "Select a resource to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateInput()) return;
        try {
            Resource r = new Resource(
                selectedResourceId,
                codeField.getText().trim(),
                nameField.getText().trim(),
                (Resource.ResourceType) typeBox.getSelectedItem(),
                (Integer) capacitySpinner.getValue(),
                buildingField.getText().trim(),
                availableCheck.isSelected()
            );
            resourceDAO.update(r);
            clearForm();
            loadResources();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to update resource: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (selectedResourceId == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected physical resource?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                resourceDAO.delete(selectedResourceId);
                clearForm();
                loadResources();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Cannot delete resource (may be referenced by active allocations): " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedResourceId = null;
        codeField.setText("");
        nameField.setText("");
        buildingField.setText("");
        capacitySpinner.setValue(60);
        availableCheck.setSelected(true);
        table.clearSelection();
    }
}
