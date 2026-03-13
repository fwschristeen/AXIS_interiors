package com.furniturevision.ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * ModernUI - Centralized design system for the AXIS Interiors application.
 * Provides consistent colors, fonts, and custom-styled Swing components
 * for a premium dark-themed interface.
 */
public class ModernUI {

    // ===========================
    // COLOUR PALETTE
    // ===========================
    public static final Color BG_DARK       = new Color(15, 15, 26);
    public static final Color BG_MEDIUM     = new Color(26, 26, 46);
    public static final Color BG_LIGHT      = new Color(37, 37, 64);
    public static final Color SURFACE       = new Color(42, 42, 74);
    public static final Color SURFACE_HOVER = new Color(52, 52, 90);
    public static final Color PRIMARY       = new Color(108, 99, 255);  // Purple-blue
    public static final Color PRIMARY_LIGHT = new Color(139, 131, 255);
    public static final Color PRIMARY_DARK  = new Color(80, 72, 200);
    public static final Color ACCENT        = new Color(0, 212, 170);   // Teal
    public static final Color ACCENT_LIGHT  = new Color(50, 230, 200);
    public static final Color TEXT_PRIMARY  = new Color(240, 240, 255);
    public static final Color TEXT_SECONDARY= new Color(160, 160, 200);
    public static final Color TEXT_MUTED    = new Color(100, 100, 140);
    public static final Color BORDER        = new Color(58, 58, 92);
    public static final Color BORDER_LIGHT  = new Color(70, 70, 110);
    public static final Color SUCCESS       = new Color(76, 175, 80);
    public static final Color WARNING       = new Color(255, 152, 0);
    public static final Color ERROR         = new Color(244, 67, 54);
    public static final Color CANVAS_BG     = new Color(245, 242, 235);
    public static final Color GRID_COLOR    = new Color(220, 215, 205);
    public static final Color SELECTION     = new Color(108, 99, 255, 100);

    // ===========================
    // FONTS
    // ===========================
    public static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_SUBTITLE  = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADING   = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY      = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_TINY      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON    = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_ICON      = new Font("Segoe UI", Font.PLAIN, 20);

    // ===========================
    // DIMENSIONS
    // ===========================
    public static final int SIDEBAR_WIDTH   = 250;
    public static final int TOPBAR_HEIGHT   = 60;
    public static final int TOOLBAR_HEIGHT  = 50;
    public static final int BORDER_RADIUS   = 12;
    public static final int BUTTON_RADIUS   = 8;
    public static final int FIELD_RADIUS    = 8;

    // ===========================
    // COMPONENT FACTORY METHODS
    // ===========================

    /**
     * Enables anti-aliased rendering for the given Graphics2D context.
     */
    public static void enableAntiAliasing(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /**
     * Creates a modern styled button with hover/press effects.
     */
    public static JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            private boolean hovering = false;
            private boolean pressing = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovering = false; pressing = false; repaint(); }
                    @Override public void mousePressed(MouseEvent e) { pressing = true; repaint(); }
                    @Override public void mouseReleased(MouseEvent e){ pressing = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                enableAntiAliasing(g2);

                Color bg = bgColor;
                if (pressing) bg = bg.darker();
                else if (hovering) bg = brighter(bg, 25);

                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BUTTON_RADIUS, BUTTON_RADIUS));

                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                g2.setColor(TEXT_PRIMARY);
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };

        button.setFont(FONT_BUTTON);
        button.setForeground(TEXT_PRIMARY);
        button.setPreferredSize(new Dimension(160, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Creates a sidebar navigation button.
     */
    public static JButton createSidebarButton(String text, String icon) {
        JButton button = new JButton() {
            private boolean hovering = false;
            private boolean active = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovering = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                enableAntiAliasing(g2);

                if (active || hovering) {
                    g2.setColor(active ? new Color(108, 99, 255, 30) : new Color(255, 255, 255, 8));
                    g2.fill(new RoundRectangle2D.Float(8, 2, getWidth() - 16, getHeight() - 4, 10, 10));
                }

                if (active) {
                    g2.setColor(PRIMARY);
                    g2.fillRoundRect(0, (getHeight() - 24) / 2, 4, 24, 4, 4);
                }

                // Icon
                g2.setFont(FONT_ICON);
                g2.setColor(active ? PRIMARY : (hovering ? TEXT_PRIMARY : TEXT_SECONDARY));
                g2.drawString(icon, 24, (getHeight() + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2);

                // Text
                g2.setFont(FONT_BODY);
                g2.setColor(active ? TEXT_PRIMARY : (hovering ? TEXT_PRIMARY : TEXT_SECONDARY));
                g2.drawString(text, 56, (getHeight() + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2);

                g2.dispose();
            }
        };

        button.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 48));
        button.setMaximumSize(new Dimension(SIDEBAR_WIDTH, 48));
        button.setMinimumSize(new Dimension(SIDEBAR_WIDTH, 48));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.putClientProperty("sidebarButton", true);
        return button;
    }

    /**
     * Sets the active state on a sidebar button.
     */
    public static void setSidebarActive(JButton button, boolean active) {
        // Using reflection-free approach via client property
        button.putClientProperty("active", active);
        button.repaint();
    }

    /**
     * Creates a modern text field.
     */
    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                enableAntiAliasing(g2);

                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), FIELD_RADIUS, FIELD_RADIUS));

                super.paintComponent(g);

                if (getText().isEmpty() && !hasFocus()) {
                    g2.setColor(ThemeManager.textPlaceholder());
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, getInsets().left + 4, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.dispose();
            }
        };

        field.setFont(FONT_BODY);
        field.setBackground(ThemeManager.fieldBg());
        field.setForeground(ThemeManager.textPrimary());
        field.setCaretColor(ThemeManager.accent());
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(FIELD_RADIUS, ThemeManager.fieldBorder()),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(250, 40));
        field.setOpaque(false);
        return field;
    }

    /**
     * Creates a modern password field.
     */
    public static JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                enableAntiAliasing(g2);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), FIELD_RADIUS, FIELD_RADIUS));
                super.paintComponent(g);
                if (getPassword().length == 0 && !hasFocus()) {
                    g2.setColor(ThemeManager.textPlaceholder());
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, getInsets().left + 4, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.dispose();
            }
        };

        field.setFont(FONT_BODY);
        field.setBackground(ThemeManager.fieldBg());
        field.setForeground(ThemeManager.textPrimary());
        field.setCaretColor(ThemeManager.accent());
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(FIELD_RADIUS, ThemeManager.fieldBorder()),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(250, 40));
        field.setOpaque(false);
        return field;
    }

    /**
     * Creates a styled label.
     */
    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /**
     * Creates a panel with gradient background.
     */
    public static JPanel createGradientPanel(Color startColor, Color endColor) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                enableAntiAliasing(g2);
                GradientPaint gp = new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
    }

    /**
     * Creates a modern combo box.
     */
    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(FONT_BODY);
        combo.setBackground(ThemeManager.fieldBg());
        combo.setForeground(ThemeManager.textPrimary());
        combo.setBorder(new RoundedBorder(FIELD_RADIUS, ThemeManager.fieldBorder()));
        combo.setPreferredSize(new Dimension(250, 40));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(FONT_BODY);
                label.setBorder(new EmptyBorder(5, 10, 5, 10));
                if (isSelected) {
                    label.setBackground(PRIMARY);
                    label.setForeground(ThemeManager.textPrimary());
                } else {
                    label.setBackground(ThemeManager.fieldBg());
                    label.setForeground(ThemeManager.textPrimary());
                }
                return label;
            }
        });
        return combo;
    }

    /**
     * Creates a modern spinner.
     */
    public static JSpinner createSpinner(double value, double min, double max, double step) {
        SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(FONT_BODY);
        spinner.setBackground(ThemeManager.fieldBg());
        spinner.setForeground(ThemeManager.textPrimary());
        spinner.setPreferredSize(new Dimension(120, 40));

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setFont(FONT_BODY);
            tf.setBackground(ThemeManager.fieldBg());
            tf.setForeground(ThemeManager.textPrimary());
            tf.setCaretColor(PRIMARY);
        }

        spinner.setBorder(new RoundedBorder(FIELD_RADIUS, ThemeManager.fieldBorder()));
        return spinner;
    }

    /**
     * Creates a modern scroll pane.
     */
    public static JScrollPane createScrollPane(Component view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeManager.bgPrimary());
        scrollPane.setBackground(ThemeManager.bgPrimary());

        // Modern scrollbar
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setBackground(ThemeManager.bgPrimary());
        scrollPane.getHorizontalScrollBar().setBackground(ThemeManager.bgPrimary());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));

        return scrollPane;
    }

    /**
     * Shows a modern styled toast/notification.
     */
    public static void showToast(Component parent, String message, Color bgColor) {
        JWindow toast = new JWindow(SwingUtilities.getWindowAncestor(parent));
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                enableAntiAliasing(g2);
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 24, 12, 24));

        JLabel label = createLabel(message, FONT_BODY, ThemeManager.textPrimary());
        panel.add(label);
        toast.setContentPane(panel);
        toast.pack();

        // Position at bottom center of parent
        Window window = SwingUtilities.getWindowAncestor(parent);
        if (window != null) {
            int x = window.getX() + (window.getWidth() - toast.getWidth()) / 2;
            int y = window.getY() + window.getHeight() - toast.getHeight() - 60;
            toast.setLocation(x, y);
        }

        toast.setVisible(true);
        toast.setAlwaysOnTop(true);

        // Fade out after delay
        Timer timer = new Timer(2000, e -> toast.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    // ===========================
    // UTILITY METHODS
    // ===========================

    /**
     * Brightens a color by a given amount.
     */
    public static Color brighter(Color c, int amount) {
        return new Color(
                Math.min(255, c.getRed() + amount),
                Math.min(255, c.getGreen() + amount),
                Math.min(255, c.getBlue() + amount),
                c.getAlpha()
        );
    }

    /**
     * Darkens a color by a given amount.
     */
    public static Color darker(Color c, int amount) {
        return new Color(
                Math.max(0, c.getRed() - amount),
                Math.max(0, c.getGreen() - amount),
                Math.max(0, c.getBlue() - amount),
                c.getAlpha()
        );
    }

    /**
     * Creates a color with the given alpha.
     */
    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    // ===========================
    // CUSTOM BORDER
    // ===========================

    /**
     * A rounded border implementation.
     */
    public static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);
            g2.setColor(color);
            g2.draw(new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 8, 4, 8);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = 8;
            insets.top = insets.bottom = 4;
            return insets;
        }
    }

    // ===========================
    // MODERN SCROLL BAR UI
    // ===========================

    public static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.trackColor = ThemeManager.bgPrimary();
            this.thumbColor = ThemeManager.border();
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);
            g2.setColor(BORDER_LIGHT);
            g2.fill(new RoundRectangle2D.Float(thumbBounds.x + 1, thumbBounds.y + 1,
                    thumbBounds.width - 2, thumbBounds.height - 2, 6, 6));
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(ThemeManager.bgPrimary());
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }
}
