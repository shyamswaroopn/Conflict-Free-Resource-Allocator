package com.allocator.ui.views;

import com.allocator.dao.AllocationDAO;
import com.allocator.dao.ResourceDAO;
import com.allocator.dao.StudentDAO;
import com.allocator.dao.TaskDAO;
import com.allocator.graph.ConflictGraph;
import com.allocator.graph.Edge;
import com.allocator.graph.Vertex;
import com.allocator.model.Resource;
import com.allocator.model.Student;
import com.allocator.model.Task;
import com.allocator.service.AllocationEngine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

/**
 * Interactive 2D Graph Visualization & Coloring Engine Panel.
 * Renders Vertices (Tasks), Conflict Edges, Adjacency List, and Chromatic Color Assignments.
 */
public class GraphVisualizerPanel extends JPanel {
    public static final Color[] PALETTE = {
        new Color(59, 130, 246),  // Color 0: Blue
        new Color(16, 185, 129),  // Color 1: Emerald
        new Color(245, 158, 11),  // Color 2: Amber
        new Color(236, 72, 153),  // Color 3: Pink
        new Color(139, 92, 246),  // Color 4: Violet
        new Color(20, 184, 166),  // Color 5: Teal
        new Color(249, 115, 22),  // Color 6: Orange
        new Color(239, 68, 68)    // Color 7: Red
    };

    private final StudentDAO studentDAO = new StudentDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final AllocationEngine allocationEngine = new AllocationEngine();

    private ConflictGraph currentGraph = new ConflictGraph();
    private final JComboBox<AllocationEngine.AlgorithmMode> algoCombo =
        new JComboBox<>(AllocationEngine.AlgorithmMode.values());
    private final JTextArea traceArea = new JTextArea();
    private final JTextArea adjListArea = new JTextArea();
    private final JLabel statusBanner = new JLabel("Ready to construct Conflict Graph and execute Graph Coloring.");
    private final GraphCanvas canvas = new GraphCanvas();

    public GraphVisualizerPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(new Color(15, 23, 42));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top Control Bar
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setBackground(new Color(15, 23, 42));

        JLabel title = new JLabel("Conflict Graph G=(V,E) & Chromatic Coloring Visualizer");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        topBar.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setBackground(new Color(15, 23, 42));

        JLabel algoLbl = new JLabel("Coloring Algorithm:");
        algoLbl.setForeground(new Color(203, 213, 225));
        controls.add(algoLbl);
        controls.add(algoCombo);

        JButton runBtn = new JButton("Execute Coloring & Save Schedule");
        runBtn.setBackground(new Color(37, 99, 235));
        runBtn.setForeground(Color.WHITE);
        runBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        runBtn.setFocusPainted(false);
        runBtn.addActionListener(e -> executeColoringAndPersist(true));
        controls.add(runBtn);

        topBar.add(controls, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Main Split: Left 2D Graph Canvas, Right Adjacency List + Execution Trace
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.62);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);
        splitPane.setBackground(new Color(15, 23, 42));

        JPanel canvasWrapper = new JPanel(new BorderLayout());
        canvasWrapper.setBackground(new Color(15, 23, 42));
        canvasWrapper.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));
        statusBanner.setForeground(new Color(56, 189, 248));
        statusBanner.setFont(new Font("Monospaced", Font.BOLD, 12));
        statusBanner.setBorder(new EmptyBorder(10, 14, 10, 14));
        canvasWrapper.add(statusBanner, BorderLayout.NORTH);
        canvasWrapper.add(canvas, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 12));
        rightPanel.setBackground(new Color(15, 23, 42));

        adjListArea.setEditable(false);
        adjListArea.setBackground(new Color(30, 41, 59));
        adjListArea.setForeground(new Color(226, 232, 240));
        adjListArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        adjListArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane adjScroll = new JScrollPane(adjListArea);
        adjScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            "Adjacency List Representation Adj[u]",
            0, 0, new Font("SansSerif", Font.BOLD, 12), Color.WHITE
        ));
        adjScroll.setBackground(new Color(30, 41, 59));

        traceArea.setEditable(false);
        traceArea.setBackground(new Color(30, 41, 59));
        traceArea.setForeground(new Color(167, 243, 208));
        traceArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        traceArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane traceScroll = new JScrollPane(traceArea);
        traceScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            "Step-by-Step Graph Coloring Trace & Complexity",
            0, 0, new Font("SansSerif", Font.BOLD, 12), Color.WHITE
        ));
        traceScroll.setBackground(new Color(30, 41, 59));

        rightPanel.add(adjScroll);
        rightPanel.add(traceScroll);

        splitPane.setLeftComponent(canvasWrapper);
        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);
    }

    public void executeColoringAndPersist(boolean saveToDatabase) {
        try {
            List<Student> students = studentDAO.findAll();
            List<Resource> resources = resourceDAO.findAll();
            List<Task> tasks = taskDAO.findAllWithEnrollments();

            AllocationEngine.AlgorithmMode selectedMode =
                (AllocationEngine.AlgorithmMode) algoCombo.getSelectedItem();

            AllocationEngine.AllocationResult result =
                allocationEngine.generateSchedule(tasks, students, resources, selectedMode);

            this.currentGraph = result.getGraph();

            if (saveToDatabase) {
                allocationDAO.saveBatchAllocations(result.getAllocations());
            }

            // Populate Adjacency List Text
            StringBuilder adjSb = new StringBuilder();
            for (Vertex v : currentGraph.getVertices()) {
                adjSb.append(String.format("%-7s (deg=%d) -> ", v.getTask().getCode(), v.getDegree()));
                List<Integer> neighbors = currentGraph.getNeighbors(v.getId());
                if (neighbors.isEmpty()) {
                    adjSb.append("[] (Isolated Vertex)");
                } else {
                    List<String> codes = new ArrayList<>();
                    for (Integer nId : neighbors) {
                        Vertex nv = currentGraph.getVertex(nId);
                        if (nv != null) codes.add(nv.getTask().getCode());
                    }
                    adjSb.append(codes);
                }
                adjSb.append("\n");
            }
            adjListArea.setText(adjSb.toString());

            // Populate Step Trace
            StringBuilder traceSb = new StringBuilder();
            traceSb.append("Algorithm: ").append(selectedMode.getDisplayName()).append("\n");
            traceSb.append("Vertices |V| = ").append(currentGraph.getVertexCount())
                   .append(" | Conflict Edges |E| = ").append(currentGraph.getEdgeCount())
                   .append(" | Max Degree Δ(G) = ").append(currentGraph.getMaxDegree()).append("\n");
            traceSb.append("Chromatic Number Used χ(G) = ").append(result.getChromaticNumber()).append(" colors (time slots)\n");
            traceSb.append("----------------------------------------------------------\n");
            for (String line : result.getAlgorithmTrace()) {
                traceSb.append(line).append("\n");
            }
            if (!result.getWarnings().isEmpty()) {
                traceSb.append("\nWarnings:\n");
                for (String w : result.getWarnings()) {
                    traceSb.append("! ").append(w).append("\n");
                }
            }
            traceArea.setText(traceSb.toString());

            statusBanner.setText(String.format(
                "Graph Colored (%s): |V|=%d, |E|=%d, Colors Used X(G)=%d | All adjacent vertices verified conflict-free.",
                selectedMode.name(), currentGraph.getVertexCount(), currentGraph.getEdgeCount(), result.getChromaticNumber()
            ));

            canvas.repaint();

            if (saveToDatabase) {
                JOptionPane.showMessageDialog(
                    this,
                    "Conflict-Free Schedule generated and persisted to MySQL!\n" +
                    "Total Time Slots (Colors) Required: " + result.getChromaticNumber(),
                    "Graph Coloring Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Custom 2D Swing Canvas rendering Graph Vertices in a radial layout with anti-aliased conflict edges.
     */
    private class GraphCanvas extends JPanel {
        public GraphCanvas() {
            setBackground(new Color(15, 23, 42));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Collection<Vertex> vertices = currentGraph.getVertices();
            int n = vertices.size();
            if (n == 0) {
                g2.setColor(new Color(148, 163, 184));
                g2.drawString("No task vertices loaded. Add tasks or connect to MySQL.", 30, 40);
                g2.dispose();
                return;
            }

            int w = getWidth();
            int h = getHeight();
            int centerX = w / 2;
            int centerY = h / 2;
            int radius = Math.max(80, Math.min(w, h) / 2 - 75);

            Map<Integer, Point> positions = new HashMap<>();
            int idx = 0;
            for (Vertex v : vertices) {
                double angle = (2 * Math.PI * idx) / n - (Math.PI / 2);
                int vx = centerX + (int) (radius * Math.cos(angle));
                int vy = centerY + (int) (radius * Math.sin(angle));
                positions.put(v.getId(), new Point(vx, vy));
                idx++;
            }

            // Draw Conflict Edges
            for (Edge edge : currentGraph.getEdges()) {
                Point p1 = positions.get(edge.getSourceTaskId());
                Point p2 = positions.get(edge.getTargetTaskId());
                if (p1 != null && p2 != null) {
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.setColor(new Color(244, 63, 94, 150)); // Rose conflict edge
                    g2.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));

                    // Edge weight midpoint badge
                    int mx = (p1.x + p2.x) / 2;
                    int my = (p1.y + p2.y) / 2;
                    g2.setColor(new Color(30, 41, 59));
                    g2.fillRoundRect(mx - 12, my - 9, 24, 18, 6, 6);
                    g2.setColor(new Color(251, 113, 133));
                    g2.setFont(new Font("Monospaced", Font.BOLD, 10));
                    g2.drawString("w=" + edge.getWeight(), mx - 10, my + 4);
                }
            }

            // Draw Colored Task Vertices
            int nodeRadius = 30;
            for (Vertex v : vertices) {
                Point p = positions.get(v.getId());
                if (p == null) continue;

                int cIdx = Math.max(0, v.getColorIndex());
                Color fill = v.isColored() ? PALETTE[cIdx % PALETTE.length] : new Color(71, 85, 105);

                Shape circle = new Ellipse2D.Double(p.x - nodeRadius, p.y - nodeRadius, nodeRadius * 2, nodeRadius * 2);
                g2.setColor(fill);
                g2.fill(circle);

                g2.setStroke(new BasicStroke(2.5f));
                g2.setColor(Color.WHITE);
                g2.draw(circle);

                // Task Code
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                String code = v.getTask().getCode();
                g2.drawString(code, p.x - fm.stringWidth(code) / 2, p.y - 2);

                // Color Slot & Degree label
                g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
                String sub = "C#" + (cIdx + 1) + " d=" + v.getDegree();
                int subW = g2.getFontMetrics().stringWidth(sub);
                g2.drawString(sub, p.x - subW / 2, p.y + 13);
            }

            g2.dispose();
        }
    }
}
