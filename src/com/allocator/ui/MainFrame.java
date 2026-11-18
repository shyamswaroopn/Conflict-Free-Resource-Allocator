package com.allocator.ui;

import com.allocator.ui.components.SidebarPanel;
import com.allocator.ui.views.*;

import javax.swing.*;
import java.awt.*;

/**
 * Primary Application Window (JFrame) hosting the Sidebar Navigation and CardLayout Views.
 */
public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea = new JPanel(cardLayout);
    private final SidebarPanel sidebar;

    private final DashboardView dashboardView;
    private final StudentView studentView;
    private final ResourceView resourceView;
    private final TaskView taskView;
    private final GraphVisualizerPanel graphView;
    private final AllocationView allocationView;

    public MainFrame() {
        setTitle("Conflict-Free Resource Allocation System Using Graph Coloring (Java PBL)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 820);
        setMinimumSize(new Dimension(1120, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

       graphView = new GraphVisualizerPanel();

dashboardView = new DashboardView(() -> {
    navigateTo("GRAPH");
    graphView.executeColoringAndPersist(true);
});

studentView = new StudentView();
resourceView = new ResourceView();
taskView = new TaskView();
allocationView = new AllocationView();
        contentArea.add(dashboardView, "DASHBOARD");
        contentArea.add(studentView, "STUDENTS");
        contentArea.add(resourceView, "RESOURCES");
        contentArea.add(taskView, "TASKS");
        contentArea.add(graphView, "GRAPH");
        contentArea.add(allocationView, "ALLOCATIONS");

        sidebar = new SidebarPanel(this::navigateTo);

        add(sidebar, BorderLayout.WEST);
        add(contentArea, BorderLayout.CENTER);

        // Load initial view data
        dashboardView.refreshMetrics();
    }

    public void navigateTo(String viewKey) {
        sidebar.setActiveTab(viewKey);
        cardLayout.show(contentArea, viewKey);

        switch (viewKey) {
            case "DASHBOARD":
                dashboardView.refreshMetrics();
                break;
            case "STUDENTS":
                studentView.loadStudents();
                break;
            case "RESOURCES":
                resourceView.loadResources();
                break;
            case "TASKS":
                taskView.loadTasksAndStudents();
                break;
            case "GRAPH":
                graphView.executeColoringAndPersist(false);
                break;
            case "ALLOCATIONS":
                allocationView.loadAllocations();
                break;
            default:
                break;
        }
    }
}
