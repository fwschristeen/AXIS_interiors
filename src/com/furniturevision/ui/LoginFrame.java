package com.furniturevision.ui;

import com.furniturevision.model.Designer;
import com.furniturevision.util.FileManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Modern login screen for AXIS Interiors.
 * Features a split-panel design — form on the left, furniture image on the right.
 * Supports light / dark mode via a toggle in the top-right corner.
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    // Cached furniture image
    private BufferedImage furnitureImage;

    public LoginFrame() {
        setTitle("AXIS Interiors - Designer Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 600);
        setMinimumSize(new Dimension(820, 520));
        setLocationRelativeTo(null);
        setUndecorated(false);

        loadFurnitureImage();
        initComponents();

        // Listen for theme changes to repaint
        ThemeManager.addChangeListener(() -> {
            getContentPane().repaint();
            SwingUtilities.updateComponentTreeUI(this);
            repaint();
        });
    }

    private void loadFurnitureImage() {
        try {
            furnitureImage = ImageIO.read(
                    getClass().getResourceAsStream("/com/furniturevision/resources/login_bg.png"));
        } catch (Exception e) {
            System.err.println("[Login] Could not load furniture image: " + e.getMessage());
        }
    }

    private void initComponents() {
        // Main split container
        JPanel mainPanel = new JPanel(new GridLayout(1, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.bgPrimary());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setOpaque(false);

        // ===========================
        // LEFT PANEL — Login Form
        // ===========================
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                g2.setColor(ThemeManager.cardBg());
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Right-side subtle border
                g2.setColor(ThemeManager.cardBorder());
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        leftPanel.setLayout(new GridBagLayout());
        leftPanel.setOpaque(false);

        // Theme toggle button — top-right of left panel
        JButton themeToggle = createThemeToggle();

        // Form container
        JPanel formCard = new JPanel();
        formCard.setOpaque(false);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new EmptyBorder(0, 50, 0, 50));
        formCard.setPreferredSize(new Dimension(360, 430));

        // "WELCOME" heading
        JLabel welcomeLabel = new JLabel("WELCOME") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                g2.setFont(getFont());
                g2.setColor(ThemeManager.textPrimary());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 0, fm.getAscent());
                g2.dispose();
            }
        };
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(ThemeManager.textPrimary());
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Sign in to your designer account") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                g2.setFont(getFont());
                g2.setColor(ThemeManager.textSecondary());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 0, fm.getAscent());
                g2.dispose();
            }
        };
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ─── Or ─── divider
        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                int y = getHeight() / 2;
                g2.setColor(ThemeManager.border());
                g2.drawLine(0, y, getWidth() / 2 - 20, y);
                g2.drawLine(getWidth() / 2 + 20, y, getWidth(), y);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.setColor(ThemeManager.textMuted());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("Or", (getWidth() - fm.stringWidth("Or")) / 2, y + fm.getAscent() / 2 - 1);
                g2.dispose();
            }
        };
        divider.setOpaque(false);
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        divider.setPreferredSize(new Dimension(300, 30));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Username field
        usernameField = createLoginField("Username");
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        // Password field
        passwordField = createLoginPasswordField("Password");
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(ThemeManager.error());
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login button
        JButton loginButton = createLoginButton("Login");
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        loginButton.addActionListener(e -> performLogin());

        // Demo credentials hint
        JLabel hintLabel = new JLabel("Demo: admin / admin123") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                g2.setFont(getFont());
                g2.setColor(ThemeManager.textMuted());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 0, fm.getAscent());
                g2.dispose();
            }
        };
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Assemble form
        formCard.add(welcomeLabel);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(subtitleLabel);
        formCard.add(Box.createVerticalStrut(28));
        formCard.add(divider);
        formCard.add(Box.createVerticalStrut(20));
        formCard.add(usernameField);
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(passwordField);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(errorLabel);
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(loginButton);
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(hintLabel);

        // Layout for left panel — form in center, theme toggle top-right
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = new Insets(16, 0, 0, 16); gbc.weightx = 1;
        leftPanel.add(themeToggle, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0); gbc.weighty = 1;
        leftPanel.add(formCard, gbc);

        // ===========================
        // RIGHT PANEL — Furniture Image
        // ===========================
        JPanel rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);

                if (furnitureImage != null) {
                    // Scale to fill while maintaining aspect ratio
                    int pw = getWidth(), ph = getHeight();
                    double imgW = furnitureImage.getWidth(), imgH = furnitureImage.getHeight();
                    double scale = Math.max((double) pw / imgW, (double) ph / imgH);
                    int drawW = (int)(imgW * scale), drawH = (int)(imgH * scale);
                    int x = (pw - drawW) / 2, y = (ph - drawH) / 2;

                    // Draw with rounded corners using clip
                    g2.setClip(new RoundRectangle2D.Float(8, 8, pw - 16, ph - 16, 20, 20));
                    g2.drawImage(furnitureImage, x, y, drawW, drawH, null);
                    g2.setClip(null);

                    // Subtle overlay for text readability
                    g2.setColor(new Color(0, 0, 0, 20));
                    g2.fill(new RoundRectangle2D.Float(8, 8, pw - 16, ph - 16, 20, 20));

                    // Border around image
                    g2.setColor(ThemeManager.cardBorder());
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(8, 8, pw - 16, ph - 16, 20, 20));
                } else {
                    // Fallback gradient
                    GradientPaint gp = new GradientPaint(0, 0, new Color(108, 99, 255),
                            getWidth(), getHeight(), new Color(0, 150, 136));
                    g2.setPaint(gp);
                    g2.fill(new RoundRectangle2D.Float(8, 8, getWidth() - 16, getHeight() - 16, 20, 20));

                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
                    g2.drawString("AXIS", 50, getHeight() / 2 - 20);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 28));
                    g2.drawString("Interiors", 50, getHeight() / 2 + 20);
                }

                // Branding overlay on image
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2.setColor(new Color(255, 255, 255, 200));
                g2.drawString("AXIS Interiors", 28, getHeight() - 40);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.setColor(new Color(255, 255, 255, 140));
                g2.drawString("Visualize. Design. Transform.", 28, getHeight() - 22);

                g2.dispose();
            }
        };
        rightPanel.setOpaque(false);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        setContentPane(mainPanel);

        // Enter key to login
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        });

        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) passwordField.requestFocusInWindow();
            }
        });

        // Focus username field on open
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                usernameField.requestFocusInWindow();
            }
        });
    }

    // ===========================
    // LOGIN LOGIC (unchanged)
    // ===========================

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        Designer designer = FileManager.authenticate(username, password);
        if (designer != null) {
            errorLabel.setText(" ");
            dispose();
            SwingUtilities.invokeLater(() -> {
                DashboardFrame dashboard = new DashboardFrame(designer);
                dashboard.setVisible(true);
            });
        } else {
            showError("Invalid username or password. Please try again.");
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setForeground(ThemeManager.error());

        Point original = getLocation();
        Timer timer = new Timer(30, null);
        final int[] shakeCount = {0};
        timer.addActionListener(e -> {
            shakeCount[0]++;
            if (shakeCount[0] > 10) {
                setLocation(original);
                timer.stop();
            } else {
                int dx = (shakeCount[0] % 2 == 0) ? 5 : -5;
                setLocation(original.x + dx, original.y);
            }
        });
        timer.start();
    }

    // ===========================
    // CUSTOM COMPONENTS
    // ===========================

    /** Theme-aware text field with clean border */
    private JTextField createLoginField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                // Background
                g2.setColor(ThemeManager.fieldBg());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
                // Placeholder
                if (getText().isEmpty() && !hasFocus()) {
                    g2.setColor(ThemeManager.textPlaceholder());
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, getInsets().left + 4,
                            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.dispose();
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setOpaque(false);
        field.setForeground(ThemeManager.textPrimary());
        field.setCaretColor(ThemeManager.accent());
        field.setBorder(BorderFactory.createCompoundBorder(
                new ModernUI.RoundedBorder(10, ThemeManager.fieldBorder()),
                new EmptyBorder(10, 14, 10, 14)
        ));
        field.setPreferredSize(new Dimension(300, 48));
        return field;
    }

    /** Theme-aware password field with clean border */
    private JPasswordField createLoginPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                g2.setColor(ThemeManager.fieldBg());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
                if (getPassword().length == 0 && !hasFocus()) {
                    g2.setColor(ThemeManager.textPlaceholder());
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, getInsets().left + 4,
                            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.dispose();
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setOpaque(false);
        field.setForeground(ThemeManager.textPrimary());
        field.setCaretColor(ThemeManager.accent());
        field.setBorder(BorderFactory.createCompoundBorder(
                new ModernUI.RoundedBorder(10, ThemeManager.fieldBorder()),
                new EmptyBorder(10, 14, 10, 14)
        ));
        field.setPreferredSize(new Dimension(300, 48));
        return field;
    }

    /** Charcoal dark login button */
    private JButton createLoginButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hovering = false;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                Color bg = hovering ? ThemeManager.accent() : ThemeManager.buttonPrimary();
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(ThemeManager.buttonText());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
                });
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(300, 48));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Sun / Moon theme toggle button */
    private JButton createThemeToggle() {
        JButton btn = new JButton() {
            private boolean hovering = false;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                // Circle background
                g2.setColor(hovering ? ThemeManager.surfaceHover() : ThemeManager.bgTertiary());
                g2.fillOval(0, 0, getWidth(), getHeight());
                // Icon
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 18));
                String icon = ThemeManager.isDark() ? "☀" : "🌙";
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(ThemeManager.textPrimary());
                g2.drawString(icon,
                        (getWidth() - fm.stringWidth(icon)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
                });
            }
        };
        btn.setPreferredSize(new Dimension(38, 38));
        btn.setMinimumSize(new Dimension(38, 38));
        btn.setMaximumSize(new Dimension(38, 38));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Toggle Light / Dark Mode");
        btn.addActionListener(e -> {
            ThemeManager.toggle();
            // Repaint entire frame
            SwingUtilities.invokeLater(() -> {
                repaint();
                revalidate();
            });
        });
        return btn;
    }
}
