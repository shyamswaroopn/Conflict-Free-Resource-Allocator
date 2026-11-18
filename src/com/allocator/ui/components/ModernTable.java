package com.allocator.ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * Custom styled JTable with dark slate surface, alternating row backgrounds,
 * high-contrast headers, and clean selection states.
 */
public class ModernTable extends JTable {

    public ModernTable(TableModel model) {
        super(model);
        setRowHeight(36);
        setShowGrid(true);
        setGridColor(new Color(51, 65, 85));
        setBackground(new Color(15, 23, 42));
        setForeground(new Color(226, 232, 240));
        setSelectionBackground(new Color(37, 99, 235));
        setSelectionForeground(Color.WHITE);
        setFont(new Font("SansSerif", Font.PLAIN, 13));
        setFillsViewportHeight(true);

        JTableHeader header = getTableHeader();
        header.setBackground(new Color(30, 41, 59));
        header.setForeground(new Color(148, 163, 184));
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);

        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(15, 23, 42) : new Color(23, 33, 53));
                    c.setForeground(new Color(226, 232, 240));
                }
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                }
                return c;
            }
        });
    }
}
