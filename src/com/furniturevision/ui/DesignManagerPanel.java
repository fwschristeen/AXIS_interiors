package com.furniturevision.ui;

import com.furniturevision.model.Design;
import com.furniturevision.util.FileManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Panel for managing saved designs.
 * Displays designs in a responsive card grid with open, edit, and delete options.
 */
public class DesignManagerPanel extends JPanel {

    private DashboardFrame dashboard;
    private JPanel cardsContainer;

    public DesignManagerPanel(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        setBackground(ThemeManager.bgPrimary());
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(24, 30, 16, 30));

        JLabel titleLabel = ModernUI.createLabel("My Designs", ModernUI.FONT_TITLE, ThemeManager.textPrimary());
        JLabel subtitleLabel = ModernUI.createLabel("Manage your saved room designs", ModernUI.FONT_BODY, ThemeManager.textSecondary());

        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.add(titleLabel);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(subtitleLabel);

        JButton refreshBtn = ModernUI.createButton("↻ Refresh", ThemeManager.surface());
        refreshBtn.setPreferredSize(new Dimension(100, 36));
        refreshBtn.addActionListener(e -> refreshDesigns());

        header.add(headerText, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);

        // Cards grid container
        cardsContainer = new JPanel();
        cardsContainer.setOpaque(false);
        cardsContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        cardsContainer.setBorder(new EmptyBorder(10, 30, 30, 30));

        JScrollPane scrollPane = ModernUI.createScrollPane(cardsContainer);
        scrollPane.getViewport().setBackground(ThemeManager.bgPrimary());

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Refreshes the list of saved designs.
     */
    public void refreshDesigns() {
        cardsContainer.removeAll();

        List<Design> designs = FileManager.loadAllDesigns(dashboard.getCurrentDesigner().getUsername());

        if (designs.isEmpty()) {
            // Empty state
            JPanel emptyState = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    ModernUI.enableAntiAliasing(g2);
                    g2.setColor(ThemeManager.bgSecondary());
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

                    g2.setColor(ThemeManager.textMuted());
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 48));
                    FontMetrics fm = g2.getFontMetrics();
                    String icon = "◫";
                    g2.drawString(icon, (getWidth() - fm.stringWidth(icon)) / 2, getHeight() / 2 - 20);

                    g2.setFont(ModernUI.FONT_SUBTITLE);
                    fm = g2.getFontMetrics();
                    String msg = "No saved designs yet";
                    g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2 + 20);

                    g2.setFont(ModernUI.FONT_BODY);
                    g2.setColor(ThemeManager.textMuted());
                    fm = g2.getFontMetrics();
                    msg = "Create a new design to get started";
                    g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2 + 50);

                    g2.dispose();
                }
            };
            emptyState.setOpaque(false);
            emptyState.setPreferredSize(new Dimension(500, 300));
            cardsContainer.setLayout(new GridBagLayout());
            cardsContainer.add(emptyState);
        } else {
            cardsContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));

            for (Design design : designs) {
                cardsContainer.add(createDesignCard(design));
            }
        }

        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    /**
     * Creates a design card widget.
     */
    private JPanel createDesignCard(Design design) {
        JPanel card = new JPanel() {
            private boolean hovering = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);

                // Card background
                Color bg = hovering ? ThemeManager.bgTertiary() : ThemeManager.bgSecondary();
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));

                // Border
                g2.setColor(hovering ? ModernUI.PRIMARY : ThemeManager.border());
                g2.setStroke(new BasicStroke(hovering ? 2f : 1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));

                // Room preview area (top section)
                g2.setColor(new Color(30, 30, 50));
                g2.fill(new RoundRectangle2D.Float(8, 8, getWidth() - 16, 130, 10, 10));

                // Mini room preview
                drawMiniPreview(g2, design, 8, 8, getWidth() - 16, 130);

                // Room name
                g2.setColor(ThemeManager.textPrimary());
                g2.setFont(ModernUI.FONT_HEADING);
                g2.drawString(design.getRoom().getName(), 16, 162);

                // Customer name
                g2.setColor(ModernUI.ACCENT);
                g2.setFont(ModernUI.FONT_SMALL);
                g2.drawString("Client: " + (design.getCustomerName() != null ? design.getCustomerName() : "N/A"), 16, 180);

                // Details
                g2.setColor(ThemeManager.textSecondary());
                g2.setFont(ModernUI.FONT_TINY);
                g2.drawString(String.format("%.1f × %.1f m | %d items",
                        design.getRoom().getWidth(), design.getRoom().getDepth(),
                        design.getFurnitureItems().size()), 16, 200);

                // Date
                g2.setColor(ThemeManager.textMuted());
                g2.drawString("Modified: " + design.getFormattedModifiedDate(), 16, 218);

                // Notes (truncated to fit card)
                String notes = design.getNotes();
                if (notes != null && !notes.isEmpty()) {
                    g2.setColor(ThemeManager.textSecondary());
                    g2.setFont(ModernUI.FONT_TINY);
                    FontMetrics fm = g2.getFontMetrics();
                    String noteText = "📝 " + notes.replace("\n", " ");
                    int maxWidth = getWidth() - 32;
                    if (fm.stringWidth(noteText) > maxWidth) {
                        while (fm.stringWidth(noteText + "…") > maxWidth && noteText.length() > 4) {
                            noteText = noteText.substring(0, noteText.length() - 1);
                        }
                        noteText += "…";
                    }
                    g2.drawString(noteText, 16, 234);
                }

                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setPreferredSize(new Dimension(280, 310));
        card.setLayout(new BorderLayout());
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Buttons panel at bottom
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 4));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(new EmptyBorder(0, 8, 8, 8));

        JButton openBtn = ModernUI.createButton("Open", ModernUI.PRIMARY);
        openBtn.setPreferredSize(new Dimension(65, 30));
        openBtn.setFont(ModernUI.FONT_SMALL);
        openBtn.setToolTipText("Open this design in the 2D editor");

        JButton editBtn = ModernUI.createButton("Edit", ModernUI.WARNING);
        editBtn.setPreferredSize(new Dimension(55, 30));
        editBtn.setFont(ModernUI.FONT_SMALL);
        editBtn.setToolTipText("Edit room name, customer, and notes");

        JButton view3DBtn = ModernUI.createButton("3D", ModernUI.ACCENT);
        view3DBtn.setPreferredSize(new Dimension(40, 30));
        view3DBtn.setFont(ModernUI.FONT_SMALL);
        view3DBtn.setToolTipText("View this design in 3D");

        JButton deleteBtn = ModernUI.createButton("Delete", ModernUI.ERROR);
        deleteBtn.setPreferredSize(new Dimension(60, 30));
        deleteBtn.setFont(ModernUI.FONT_SMALL);
        deleteBtn.setToolTipText("Permanently delete this design");

        openBtn.addActionListener(e -> {
            dashboard.openDesignIn2D(design);
        });

        editBtn.addActionListener(e -> {
            editDesign(design);
        });

        view3DBtn.addActionListener(e -> {
            dashboard.openDesignIn2D(design);
            dashboard.showIn3D();
        });

        deleteBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete '" + design.getRoom().getName() + "'?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                FileManager.deleteDesign(design.getId());
                ModernUI.showToast(this, "✓ Design deleted", ModernUI.SUCCESS);
                refreshDesigns();
            }
        });

        buttonsPanel.add(openBtn);
        buttonsPanel.add(editBtn);
        buttonsPanel.add(view3DBtn);
        buttonsPanel.add(deleteBtn);

        card.add(buttonsPanel, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Opens an edit dialog for a saved design's properties.
     */
    private void editDesign(Design design) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(400, 320));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Room name
        JTextField roomNameField = new JTextField(design.getRoom().getName(), 20);
        roomNameField.setFont(ModernUI.FONT_BODY);
        addEditRow(panel, gbc, 0, "Room Name:", roomNameField);

        // Customer name
        JTextField customerField = new JTextField(
                design.getCustomerName() != null ? design.getCustomerName() : "", 20);
        customerField.setFont(ModernUI.FONT_BODY);
        addEditRow(panel, gbc, 1, "Customer:", customerField);

        // Room dimensions
        JSpinner widthSp = new JSpinner(new SpinnerNumberModel(design.getRoom().getWidth(), 1.0, 30.0, 0.1));
        widthSp.setFont(ModernUI.FONT_BODY);
        addEditRow(panel, gbc, 2, "Width (m):", widthSp);

        JSpinner depthSp = new JSpinner(new SpinnerNumberModel(design.getRoom().getDepth(), 1.0, 30.0, 0.1));
        depthSp.setFont(ModernUI.FONT_BODY);
        addEditRow(panel, gbc, 3, "Depth (m):", depthSp);

        JSpinner heightSp = new JSpinner(new SpinnerNumberModel(design.getRoom().getHeight(), 1.0, 10.0, 0.1));
        heightSp.setFont(ModernUI.FONT_BODY);
        addEditRow(panel, gbc, 4, "Height (m):", heightSp);

        // Notes
        JTextArea notesArea = new JTextArea(design.getNotes(), 3, 20);
        notesArea.setFont(ModernUI.FONT_BODY);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(200, 60));
        addEditRow(panel, gbc, 5, "Notes:", notesScroll);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Edit Design: " + design.getRoom().getName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newName = roomNameField.getText().trim();
            if (newName.isEmpty()) {
                ModernUI.showToast(this, "⚠ Room name cannot be empty", ModernUI.WARNING);
                return;
            }
            design.getRoom().setName(newName);
            design.setCustomerName(customerField.getText().trim());
            design.getRoom().setWidth((Double) widthSp.getValue());
            design.getRoom().setDepth((Double) depthSp.getValue());
            design.getRoom().setHeight((Double) heightSp.getValue());
            design.setNotes(notesArea.getText().trim());
            design.touch();

            // Save changes
            FileManager.saveDesign(design);
            ModernUI.showToast(this, "✓ Design updated", ModernUI.SUCCESS);
            refreshDesigns();
        }
    }

    private void addEditRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(ModernUI.FONT_BODY);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    /**
     * Draws a miniature preview of the room layout.
     */
    private void drawMiniPreview(Graphics2D g2, Design design, int x, int y, int w, int h) {
        double roomW = design.getRoom().getWidth();
        double roomD = design.getRoom().getDepth();

        double scale = Math.min((w - 20.0) / roomW, (h - 20.0) / roomD);
        double offsetX = x + (w - roomW * scale) / 2;
        double offsetY = y + (h - roomD * scale) / 2;

        // Room floor
        g2.setColor(design.getRoom().getFloorColor());
        g2.fillRect((int) offsetX, (int) offsetY, (int) (roomW * scale), (int) (roomD * scale));

        // Room border
        g2.setColor(design.getRoom().getWallColor().darker());
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect((int) offsetX, (int) offsetY, (int) (roomW * scale), (int) (roomD * scale));

        // Furniture — use shaded colours
        for (var item : design.getFurnitureItems()) {
            int fx = (int) (offsetX + item.getX() * scale);
            int fy = (int) (offsetY + item.getY() * scale);
            int fw = (int) (item.getScaledWidth() * scale);
            int fd = (int) (item.getScaledDepth() * scale);

            g2.setColor(item.getShadedColor());
            g2.fillRect(fx, fy, Math.max(2, fw), Math.max(2, fd));
            g2.setColor(item.getShadedColor().darker());
            g2.setStroke(new BasicStroke(0.5f));
            g2.drawRect(fx, fy, Math.max(2, fw), Math.max(2, fd));
        }
    }
}
