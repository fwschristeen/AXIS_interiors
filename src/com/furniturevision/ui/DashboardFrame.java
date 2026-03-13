package com.furniturevision.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.furniturevision.backend.BackendService;
import com.furniturevision.model.Design;
import com.furniturevision.model.Designer;

/**
 * Main dashboard frame after login.
 * Contains a sidebar for navigation and a content area using CardLayout.
 * Supports light/dark theme via ThemeManager.
 */
public class DashboardFrame extends JFrame {

    private Designer currentDesigner;
    private Design currentDesign;

    private CardLayout cardLayout;
    private JPanel contentPanel;

    // Navigation panels
    private RoomSpecPanel roomSpecPanel;
    private DesignCanvas2D designCanvas2D;
    private View3DPanel view3DPanel;
    private DesignManagerPanel designManagerPanel;
    private AdminPanel adminPanel;

    // Sidebar buttons
    private List<JButton> sidebarButtons = new ArrayList<>();
    private JLabel pageTitleLabel;

    // Panel names
    private static final String PANEL_NEW_DESIGN = "newDesign";
    private static final String PANEL_2D_VIEW = "2dView";
    private static final String PANEL_3D_VIEW = "3dView";
    private static final String PANEL_MY_DESIGNS = "myDesigns";
    private static final String PANEL_ADMIN = "adminPanel";

    public DashboardFrame(Designer designer) {
        this.currentDesigner = designer;
        setTitle("AXIS Interiors - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        initComponents();
        showPanel(PANEL_NEW_DESIGN, "New Design");

        // Repaint entire frame on theme change
        ThemeManager.addChangeListener(() -> {
            SwingUtilities.invokeLater(() -> {
                repaint();
                revalidate();
            });
        });
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.bgPrimary());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // ===========================
        // SIDEBAR
        // ===========================
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                g2.setColor(ThemeManager.sidebarBg());
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Right border line
                g2.setColor(ThemeManager.border());
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(ModernUI.SIDEBAR_WIDTH, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // App logo area
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);

                // Logo icon
                g2.setColor(ThemeManager.accent());
                g2.fillRoundRect(20, 20, 36, 36, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2.drawString("AX", 27, 44);

                // App name — two lines
                g2.setColor(ThemeManager.textPrimary());
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2.drawString("AXIS", 62, 32);

                g2.setColor(ThemeManager.textSecondary());
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.drawString("Interiors", 62, 48);

                g2.setColor(ThemeManager.textMuted());
                g2.setFont(ModernUI.FONT_TINY);
                g2.drawString("Design Studio", 62, 62);

                // Bottom border
                g2.setColor(ThemeManager.border());
                g2.drawLine(15, getHeight() - 1, getWidth() - 15, getHeight() - 1);
                g2.dispose();
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(ModernUI.SIDEBAR_WIDTH, 80));
        logoPanel.setMaximumSize(new Dimension(ModernUI.SIDEBAR_WIDTH, 80));
        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(20));

        // Section label
        JLabel sectionLabel = new JLabel("  NAVIGATION") {
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
        sectionLabel.setFont(ModernUI.FONT_TINY);
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionLabel.setBorder(new EmptyBorder(0, 20, 8, 0));
        sectionLabel.setMaximumSize(new Dimension(ModernUI.SIDEBAR_WIDTH, 25));
        sidebar.add(sectionLabel);

        // Navigation buttons
        addSidebarButton(sidebar, "New Design", "✚", PANEL_NEW_DESIGN);
        addSidebarButton(sidebar, "My Designs", "◫", PANEL_MY_DESIGNS);
        addSidebarButton(sidebar, "2D Layout", "⊞", PANEL_2D_VIEW);
        addSidebarButton(sidebar, "3D View", "◆", PANEL_3D_VIEW);

        // Admin panel - only for admin users
        if (currentDesigner.isAdmin()) {
            sidebar.add(Box.createVerticalStrut(12));
            JLabel adminLabel = new JLabel("  ADMIN") {
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
            adminLabel.setFont(ModernUI.FONT_TINY);
            adminLabel.setMaximumSize(new Dimension(ModernUI.SIDEBAR_WIDTH, 20));
            sidebar.add(adminLabel);
            addSidebarButton(sidebar, "Furniture Catalog", "⚙", PANEL_ADMIN);
        }

        sidebar.add(Box.createVerticalGlue());

        // Designer info at bottom
        JPanel userPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);

                // Top border
                g2.setColor(ThemeManager.border());
                g2.drawLine(15, 0, getWidth() - 15, 0);

                // Avatar circle
                g2.setColor(ThemeManager.accent());
                g2.fillOval(20, 18, 36, 36);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                String initials = currentDesigner.getFullName().substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, 20 + (36 - fm.stringWidth(initials)) / 2,
                        18 + (36 + fm.getAscent() - fm.getDescent()) / 2);

                // Name and role
                g2.setColor(ThemeManager.textPrimary());
                g2.setFont(ModernUI.FONT_BODY);
                g2.drawString(currentDesigner.getFullName(), 66, 32);

                g2.setColor(ThemeManager.textMuted());
                g2.setFont(ModernUI.FONT_TINY);
                g2.drawString(currentDesigner.isAdmin() ? "Administrator" : "Interior Designer", 66, 48);

                g2.dispose();
            }
        };
        userPanel.setOpaque(false);
        userPanel.setPreferredSize(new Dimension(ModernUI.SIDEBAR_WIDTH, 72));
        userPanel.setMaximumSize(new Dimension(ModernUI.SIDEBAR_WIDTH, 72));

        // Logout button
        JButton logoutBtn = ModernUI.createButton("Logout", ModernUI.ERROR);
        logoutBtn.setMaximumSize(new Dimension(ModernUI.SIDEBAR_WIDTH - 30, 36));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.addActionListener(e -> logout());

        sidebar.add(userPanel);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(15));

        // ===========================
        // TOP BAR
        // ===========================
        JPanel topBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                g2.setColor(ThemeManager.bgPrimary());
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Bottom border
                g2.setColor(ThemeManager.border());
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        topBar.setPreferredSize(new Dimension(0, ModernUI.TOPBAR_HEIGHT));
        topBar.setLayout(new BorderLayout());
        topBar.setBorder(new EmptyBorder(0, 24, 0, 24));

        pageTitleLabel = new JLabel("New Design") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                g2.setFont(getFont());
                g2.setColor(ThemeManager.textPrimary());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 0, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        pageTitleLabel.setFont(ModernUI.FONT_SUBTITLE);
        topBar.add(pageTitleLabel, BorderLayout.WEST);

        // Right side: theme toggle + design info
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        topRight.setOpaque(false);

        // Theme toggle button in top bar
        JButton themeToggle = createThemeToggle();
        topRight.add(themeToggle);


        topBar.add(topRight, BorderLayout.EAST);

        // ===========================
        // CONTENT AREA
        // ===========================
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.bgPrimary());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Create panels
        roomSpecPanel = new RoomSpecPanel(this);
        designCanvas2D = new DesignCanvas2D(this);
        view3DPanel = new View3DPanel(this);
        designManagerPanel = new DesignManagerPanel(this);

        contentPanel.add(roomSpecPanel, PANEL_NEW_DESIGN);
        contentPanel.add(designCanvas2D, PANEL_2D_VIEW);
        contentPanel.add(view3DPanel, PANEL_3D_VIEW);
        contentPanel.add(designManagerPanel, PANEL_MY_DESIGNS);

        if (currentDesigner.isAdmin()) {
            adminPanel = new AdminPanel(this);
            contentPanel.add(adminPanel, PANEL_ADMIN);
        }

        // ===========================
        // ASSEMBLE MAIN LAYOUT
        // ===========================
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(topBar, BorderLayout.NORTH);
        centerPanel.add(contentPanel, BorderLayout.CENTER);

        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void addSidebarButton(JPanel sidebar, String text, String icon, String panelName) {
        // Custom sidebar button that reads from ThemeManager
        JButton btn = new JButton() {
            private boolean hovering = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovering = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                boolean active = Boolean.TRUE.equals(getClientProperty("active"));

                if (active || hovering) {
                    g2.setColor(active ? ThemeManager.sidebarActive() : ThemeManager.sidebarHover());
                    g2.fill(new RoundRectangle2D.Float(8, 2, getWidth() - 16, getHeight() - 4, 10, 10));
                }

                if (active) {
                    g2.setColor(ThemeManager.accent());
                    g2.fillRoundRect(0, (getHeight() - 24) / 2, 4, 24, 4, 4);
                }

                // Icon
                g2.setFont(ModernUI.FONT_ICON);
                g2.setColor(active ? ThemeManager.accent() : (hovering ? ThemeManager.textPrimary() : ThemeManager.textSecondary()));
                g2.drawString(icon, 24, (getHeight() + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2);

                // Text
                g2.setFont(ModernUI.FONT_BODY);
                g2.setColor(active ? ThemeManager.textPrimary() : (hovering ? ThemeManager.textPrimary() : ThemeManager.textSecondary()));
                g2.drawString(text, 56, (getHeight() + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2);

                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(ModernUI.SIDEBAR_WIDTH, 48));
        btn.setMaximumSize(new Dimension(ModernUI.SIDEBAR_WIDTH, 48));
        btn.setMinimumSize(new Dimension(ModernUI.SIDEBAR_WIDTH, 48));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("panelName", panelName);
        btn.addActionListener(e -> showPanel(panelName, text));
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebarButtons.add(btn);
    }

    /** Sun / Moon theme toggle */
    private JButton createThemeToggle() {
        JButton btn = new JButton() {
            private boolean hovering = false;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                g2.setColor(hovering ? ThemeManager.surfaceHover() : ThemeManager.bgTertiary());
                g2.fillOval(0, 0, getWidth(), getHeight());
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
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Toggle Light / Dark Mode");
        btn.addActionListener(e -> {
            ThemeManager.toggle();
            SwingUtilities.invokeLater(() -> { repaint(); revalidate(); });
        });
        return btn;
    }

    /**
     * Switches to a different panel in the content area.
     */
    public void showPanel(String panelName, String title) {
        cardLayout.show(contentPanel, panelName);
        pageTitleLabel.setText(title);

        // Update active sidebar button
        for (JButton btn : sidebarButtons) {
            String btnPanel = (String) btn.getClientProperty("panelName");
            boolean isActive = panelName.equals(btnPanel);
            btn.putClientProperty("active", isActive);
            btn.repaint();
        }

        // Refresh panels when shown
        if (panelName.equals(PANEL_MY_DESIGNS)) {
            designManagerPanel.refreshDesigns();
        }
        if (panelName.equals(PANEL_2D_VIEW) && currentDesign != null) {
            designCanvas2D.setDesign(currentDesign);
        }
        if (panelName.equals(PANEL_3D_VIEW) && currentDesign != null) {
            view3DPanel.setDesign(currentDesign);
        }
    }

    /**
     * Sets the current active design and switches to 2D view.
     */
    public void openDesignIn2D(Design design) {
        this.currentDesign = design;
        designCanvas2D.setDesign(design);
        showPanel(PANEL_2D_VIEW, "2D Layout - " + design.getRoom().getName());
    }

    /**
     * Shows the 3D view for the current design.
     */
    public void showIn3D() {
        if (currentDesign != null) {
            view3DPanel.setDesign(currentDesign);
            showPanel(PANEL_3D_VIEW, "3D View - " + currentDesign.getRoom().getName());
        }
    }

    /**
     * Creates a new design from room specifications and opens in 2D view.
     */
    public void createNewDesign(Design design) {
        design.setDesignerUsername(currentDesigner.getUsername());
        this.currentDesign = design;
        openDesignIn2D(design);
    }

    public Designer getCurrentDesigner() { return currentDesigner; }
    public Design getCurrentDesign() { return currentDesign; }

    private void logout() {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            BackendService.getInstance().logout();
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });
        }
    }
}
