package com.furniturevision;

import com.furniturevision.backend.BackendService;
import com.furniturevision.ui.LoginFrame;

import javax.swing.*;
import java.awt.*;

/**
 * AXIS Interiors - Main Application Entry Point
 * 
 * A desktop room design visualization application for furniture retailers.
 * Allows designers to create 2D layouts and 3D visualizations of customer rooms
 * with furniture arrangements.
 * 
 * Backend: SQLite database with secure SHA-256 authentication.
 * 
 * @author AXIS Interiors Development Team
 * @version 2.0
 */
public class Main {

    public static void main(String[] args) {
        // Set system properties for better rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.opengl", "true");

        // ===========================
        // INITIALIZE BACKEND
        // ===========================
        BackendService backend = BackendService.getInstance();
        backend.initialize();

        // Register shutdown hook to close DB gracefully on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            backend.shutdown();
        }));

        // Run on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Set cross-platform look and feel
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

                // Override default UI settings for modern appearance
                UIManager.put("Panel.background", new Color(15, 15, 26));
                UIManager.put("OptionPane.background", new Color(26, 26, 46));
                UIManager.put("OptionPane.messageForeground", new Color(240, 240, 255));
                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
                UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 13));
                UIManager.put("Button.background", new Color(108, 99, 255));
                UIManager.put("Button.foreground", Color.WHITE);
                UIManager.put("TextField.background", new Color(37, 37, 64));
                UIManager.put("TextField.foreground", new Color(240, 240, 255));
                UIManager.put("TextField.caretForeground", new Color(108, 99, 255));
                UIManager.put("ComboBox.background", new Color(37, 37, 64));
                UIManager.put("ComboBox.foreground", new Color(240, 240, 255));
                UIManager.put("List.background", new Color(26, 26, 46));
                UIManager.put("List.foreground", new Color(240, 240, 255));
                UIManager.put("List.selectionBackground", new Color(108, 99, 255));
                UIManager.put("ToolTip.background", new Color(42, 42, 74));
                UIManager.put("ToolTip.foreground", new Color(240, 240, 255));
                UIManager.put("ToolTip.font", new Font("Segoe UI", Font.PLAIN, 12));
                UIManager.put("PopupMenu.background", new Color(26, 26, 46));
                UIManager.put("MenuItem.background", new Color(26, 26, 46));
                UIManager.put("MenuItem.foreground", new Color(240, 240, 255));
                UIManager.put("MenuItem.selectionBackground", new Color(108, 99, 255));
                UIManager.put("MenuItem.selectionForeground", Color.WHITE);
                UIManager.put("Separator.foreground", new Color(58, 58, 92));
                UIManager.put("ScrollBar.thumb", new Color(70, 70, 110));
                UIManager.put("ScrollBar.track", new Color(15, 15, 26));

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Launch login screen
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
