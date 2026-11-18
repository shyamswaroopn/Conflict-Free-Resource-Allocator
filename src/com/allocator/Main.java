package com.allocator;

import com.allocator.config.AppConfig;
import com.allocator.config.DatabaseConnection;
import com.allocator.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Entry Point for the "Conflict-Free Resource Allocation System Using Graph Coloring"
 * Java Swing & MySQL JDBC Application.
 */
public class Main {

    public static void main(String[] args) {
        // Set cross-platform sleek Swing Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignoredSys) {
            }
        }

        SwingUtilities.invokeLater(() -> {
            if (!DatabaseConnection.testConnection()) {
                int choice = JOptionPane.showConfirmDialog(
                    null,
                    "Could not connect to MySQL at:\n" + AppConfig.getJdbcUrl() +
                    "\n\nMake sure MySQL Server is running and sql/schema.sql has been executed.\n" +
                    "Would you like to configure custom MySQL credentials now?",
                    "MySQL JDBC Connection Check",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    promptDatabaseConfig();
                }
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    private static void promptDatabaseConfig() {
        JTextField urlField = new JTextField(AppConfig.getJdbcUrl(), 32);
        JTextField userField = new JTextField(AppConfig.getDbUser(), 15);
        JPasswordField passField = new JPasswordField(AppConfig.getDbPassword(), 15);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("JDBC URL:"));
        panel.add(urlField);
        panel.add(Box.createVerticalStrut(8));
        panel.add(new JLabel("MySQL Username:"));
        panel.add(userField);
        panel.add(Box.createVerticalStrut(8));
        panel.add(new JLabel("MySQL Password:"));
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(
            null, panel, "Configure MySQL JDBC Connection",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            AppConfig.updateConnectionCredentials(
                urlField.getText().trim(),
                userField.getText().trim(),
                new String(passField.getPassword())
            );
        }
    }
}
