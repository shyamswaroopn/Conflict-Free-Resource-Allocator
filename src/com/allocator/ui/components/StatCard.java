package com.allocator.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Reusable Dashboard Metric Card displaying a title, numerical KPI value, and subtitle.
 */
public class StatCard extends JPanel {
    private final JLabel valueLabel;
    private final JLabel subtitleLabel;
    private final Color accentColor;

    public StatCard(String title, String initialValue, String subtitle, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout(0, 6));
        setBackground(new Color(30, 41, 59)); // Slate 800
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLabel.setForeground(new Color(148, 163, 184));

        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(new Font("Monospaced", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);

        subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        add(titleLabel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
        add(subtitleLabel, BorderLayout.SOUTH);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }
}
