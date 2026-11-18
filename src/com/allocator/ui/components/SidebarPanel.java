package com.allocator.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Left Sidebar Navigation Panel for switching between views in MainFrame.
 */
public class SidebarPanel extends JPanel {
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final Consumer<String> onNavigate;
    private String activeKey = "DASHBOARD";

    public SidebarPanel(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(250, 0));
        setBackground(new Color(15, 23, 42));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(51, 65, 85)));

        // Top Branding Header
        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        brandPanel.setBackground(new Color(15, 23, 42));
        brandPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("GraphAlloc PBL");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Graph Coloring Engine");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(148, 163, 184));

        brandPanel.add(title);
        brandPanel.add(subtitle);
        add(brandPanel, BorderLayout.NORTH);

        // Navigation Buttons List
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(15, 23, 42));
        menuPanel.setBorder(new EmptyBorder(8, 12, 12, 12));

        addNavButton(menuPanel, "DASHBOARD", "01. Admin Dashboard");
        addNavButton(menuPanel, "STUDENTS", "02. Student Management");
        addNavButton(menuPanel, "RESOURCES", "03. Resource Management");
        addNavButton(menuPanel, "TASKS", "04. Exam & Task Setup");
        addNavButton(menuPanel, "GRAPH", "05. Conflict Graph & Coloring");
        addNavButton(menuPanel, "ALLOCATIONS", "06. Allocation Schedule");

        add(menuPanel, BorderLayout.CENTER);

        // Footer Note
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(15, 23, 42));
        footerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel dbBadge = new JLabel("Engine: Core Java + MySQL JDBC");
        dbBadge.setFont(new Font("Monospaced", Font.PLAIN, 11));
        dbBadge.setForeground(new Color(100, 116, 139));
        footerPanel.add(dbBadge, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        setActiveTab("DASHBOARD");
    }

    private void addNavButton(JPanel container, String key, String label) {
        JButton btn = new JButton(label);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(220, 42));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));

        btn.addActionListener(e -> {
            setActiveTab(key);
            onNavigate.accept(key);
        });

        navButtons.put(key, btn);
        container.add(btn);
        container.add(Box.createRigidArea(new Dimension(0, 6)));
    }

    public void setActiveTab(String key) {
        this.activeKey = key;
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(key)) {
                btn.setBackground(new Color(37, 99, 235));
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(new Color(15, 23, 42));
                btn.setForeground(new Color(148, 163, 184));
            }
        }
    }
}
