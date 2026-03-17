package com.furniturevision.ui;

import com.furniturevision.model.Design;
import com.furniturevision.model.FurnitureItem;
import com.furniturevision.model.Room;
import com.furniturevision.util.FileManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * 2D layout canvas for arranging furniture items in a room.
 * Features drag & drop, selection, colour changes, scaling, and room grid.
 */
public class DesignCanvas2D extends JPanel {

    private DashboardFrame dashboard;
    private Design design;

    private CanvasPanel canvas;
    private JLabel statusLabel;
    private JLabel zoomLabel;

    // Canvas state
    private double zoom = 1.0;
    private double panX = 60, panY = 60;
    private double pixelsPerMetre = 80.0;
    private boolean showGrid = true;

    // Interaction state
    private FurnitureItem selectedItem = null;
    private boolean dragging = false;
    private double dragOffsetX, dragOffsetY;
    private Point lastMousePos;

    // Undo history
    private List<List<FurnitureItem>> undoHistory = new ArrayList<>();
    private int undoIndex = -1;

    public DesignCanvas2D(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        setBackground(ThemeManager.bgPrimary());
        setLayout(new BorderLayout());
        initComponents();
        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        // Delete
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "delete");
        am.put("delete", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { deleteSelected(); } });

        // Undo (Ctrl+Z / Cmd+Z)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "undo");
        am.put("undo", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { undo(); } });

        // Save (Ctrl+S / Cmd+S)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "save");
        am.put("save", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { saveDesign(); } });

        // Zoom In (+ or =)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "zoomIn");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "zoomIn");
        am.put("zoomIn", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { zoom = Math.min(5.0, zoom + 0.2); updateZoomLabel(); canvas.repaint(); } });

        // Zoom Out (-)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "zoomOut");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "zoomOut");
        am.put("zoomOut", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { zoom = Math.max(0.3, zoom - 0.2); updateZoomLabel(); canvas.repaint(); } });

        // Toggle Grid (G)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "grid");
        am.put("grid", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { showGrid = !showGrid; canvas.repaint(); } });
    }

    private void initComponents() {
        // ===========================
        // TOOLBAR
        // ===========================
        JPanel toolbar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(ThemeManager.bgSecondary());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(ThemeManager.border());
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        toolbar.setPreferredSize(new Dimension(0, ModernUI.TOOLBAR_HEIGHT));
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 8));

        // Add furniture dropdown
        JButton addFurnitureBtn = ModernUI.createButton("+ Add Furniture", ModernUI.PRIMARY);
        addFurnitureBtn.setPreferredSize(new Dimension(140, 34));
        addFurnitureBtn.addActionListener(e -> showFurnitureMenu(addFurnitureBtn));

        // Tool buttons
        JButton gridBtn = createToolButton("Grid", "Toggle grid visibility (Cmd+G)", true);
        JButton zoomInBtn = createToolButton("Zoom +", "Zoom In (Cmd++)", false);
        JButton zoomOutBtn = createToolButton("Zoom -", "Zoom Out (Cmd+-)", false);
        JButton fitBtn = createToolButton("Fit", "Fit design to view", false);
        JButton colorBtn = createToolButton("🎨 Colour", "Change colours", false);
        JButton shadeBtn = createToolButton("🌗 Shade", "Adjust shading", false);
        JButton scaleUpBtn = createToolButton("Scale +", "Increase size (110%)", false);
        JButton scaleDownBtn = createToolButton("Scale -", "Decrease size (90%)", false);
        JButton rotateBtn = createToolButton("Rotate", "Rotate 90 degrees", false);
        JButton deleteBtn = createToolButton("Delete", "Delete selected item (Del)", false);
        JButton undoBtn = createToolButton("Undo", "Undo last action (Cmd+Z)", false);
        JButton saveBtn = createToolButton("Save", "Save design (Cmd+S)", false);
        JButton view3DBtn = ModernUI.createButton("View in Basic 3D →", ModernUI.PRIMARY);
        view3DBtn.setToolTipText("Switch to basic Java 3D visualization");
        view3DBtn.setPreferredSize(new Dimension(140, 34));

        JButton realisticBtn = ModernUI.createButton("✨ Realistic Web 3D", ModernUI.ACCENT);
        realisticBtn.setToolTipText("Launch high-quality web visualization");
        realisticBtn.setPreferredSize(new Dimension(160, 34));

        // Button actions
        gridBtn.addActionListener(e -> { showGrid = !showGrid; canvas.repaint(); });
        zoomInBtn.addActionListener(e -> { zoom = Math.min(5.0, zoom + 0.2); updateZoomLabel(); canvas.repaint(); });
        zoomOutBtn.addActionListener(e -> { zoom = Math.max(0.3, zoom - 0.2); updateZoomLabel(); canvas.repaint(); });
        fitBtn.addActionListener(e -> fitToView());
        colorBtn.addActionListener(e -> showColorMenu(colorBtn));
        shadeBtn.addActionListener(e -> showShadeMenu(shadeBtn));
        scaleUpBtn.addActionListener(e -> scaleSelected(1.1));
        scaleDownBtn.addActionListener(e -> scaleSelected(0.9));
        rotateBtn.addActionListener(e -> rotateSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        undoBtn.addActionListener(e -> undo());
        saveBtn.addActionListener(e -> saveDesign());
        view3DBtn.addActionListener(e -> dashboard.showIn3D());
        
        realisticBtn.addActionListener(e -> {
            if (design != null) {
                if (design.getId() == null || design.getId().isEmpty()) {
                    design.setId(java.util.UUID.randomUUID().toString());
                }
                dashboard.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                new Thread(() -> {
                    boolean success = com.furniturevision.util.FileManager.saveDesign(design);
                    SwingUtilities.invokeLater(() -> {
                        dashboard.setCursor(Cursor.getDefaultCursor());
                        if (success) {
                            try {
                                java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:5173/view/" + design.getId()));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                ModernUI.showToast(this, "⚠ Could not open browser", ModernUI.ERROR);
                            }
                        } else {
                            ModernUI.showToast(this, "⚠ Failed to save design before exporting", ModernUI.ERROR);
                        }
                    });
                }).start();
            } else {
                ModernUI.showToast(this, "⚠ No design available", ModernUI.WARNING);
            }
        });

        toolbar.add(addFurnitureBtn);
        toolbar.add(createSeparator());
        toolbar.add(gridBtn);
        toolbar.add(zoomInBtn);
        toolbar.add(zoomOutBtn);
        toolbar.add(fitBtn);
        toolbar.add(createSeparator());
        toolbar.add(colorBtn);
        toolbar.add(shadeBtn);
        toolbar.add(createSeparator());
        toolbar.add(scaleUpBtn);
        toolbar.add(scaleDownBtn);
        toolbar.add(rotateBtn);
        toolbar.add(deleteBtn);
        toolbar.add(createSeparator());
        toolbar.add(undoBtn);
        toolbar.add(saveBtn);

        // ===========================
        // CANVAS
        // ===========================
        canvas = new CanvasPanel();

        // ===========================
        // STATUS BAR
        // ===========================
        JPanel statusBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(ThemeManager.bgSecondary());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(ThemeManager.border());
                g2.drawLine(0, 0, getWidth(), 0);
                g2.dispose();
            }
        };
        statusBar.setPreferredSize(new Dimension(0, 50));
        statusBar.setLayout(new BorderLayout());
        statusBar.setBorder(new EmptyBorder(0, 12, 0, 12));

        statusLabel = ModernUI.createLabel("Ready", ModernUI.FONT_BODY, ThemeManager.textMuted());
        zoomLabel = ModernUI.createLabel("Zoom: 100%", ModernUI.FONT_BODY, ThemeManager.textMuted());

        JPanel rightStatusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        rightStatusPanel.setOpaque(false);
        rightStatusPanel.add(zoomLabel);
        rightStatusPanel.add(view3DBtn);
        rightStatusPanel.add(realisticBtn);

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(rightStatusPanel, BorderLayout.EAST);

        // Layout
        add(toolbar, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JButton createToolButton(String text, String tooltip, boolean toggle) {
        JButton btn = new JButton(text) {
            private boolean hovering = false;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                Color bg = hovering ? ThemeManager.surfaceHover() : ThemeManager.surface();
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.setColor(ThemeManager.textSecondary());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
                });
            }
        };
        btn.setFont(ModernUI.FONT_SMALL);
        btn.setPreferredSize(new Dimension(70, 34));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);
        return btn;
    }

    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 30));
        sep.setForeground(ThemeManager.border());
        sep.setBackground(ThemeManager.border());
        return sep;
    }

    /**
     * Shows the unified furniture selection popup menu.
     * All items come from the database. Custom items are marked with ★.
     */
    private void showFurnitureMenu(Component anchor) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(ThemeManager.bgSecondary());
        menu.setBorder(BorderFactory.createLineBorder(ThemeManager.border()));

        try {
            var catalogDAO = com.furniturevision.backend.BackendService.getInstance().getCatalogDAO();
            var allItems = catalogDAO.getActiveItems();

            if (allItems.isEmpty()) {
                JMenuItem empty = new JMenuItem("No furniture available");
                empty.setFont(ModernUI.FONT_BODY);
                empty.setForeground(ThemeManager.textMuted());
                empty.setBackground(ThemeManager.bgSecondary());
                empty.setEnabled(false);
                menu.add(empty);
            } else {
                for (var catItem : allItems) {
                    String label = catItem.isCustom()
                            ? "★ " + catItem.getName()
                            : catItem.getName();

                    JMenuItem mi = new JMenuItem(label);
                    mi.setFont(ModernUI.FONT_BODY);
                    mi.setForeground(catItem.isCustom()
                            ? new Color(130, 200, 255)   // light blue for custom
                            : ThemeManager.textPrimary());     // white for shop
                    mi.setBackground(ThemeManager.bgSecondary());
                    mi.setBorder(new EmptyBorder(6, 12, 6, 12));
                    mi.addActionListener(e -> addCatalogFurniture(catItem));
                    menu.add(mi);
                }
            }
        } catch (Exception ex) {
            System.err.println("[DesignCanvas] Error loading catalog: " + ex.getMessage());
            JMenuItem err = new JMenuItem("Error loading furniture");
            err.setForeground(ThemeManager.textMuted());
            err.setBackground(ThemeManager.bgSecondary());
            err.setEnabled(false);
            menu.add(err);
        }

        menu.show(anchor, 0, anchor.getHeight());
    }

    /**
     * Adds a furniture item from the catalog to the design.
     */
    private void addCatalogFurniture(com.furniturevision.backend.FurnitureCatalogDAO.CatalogItem catItem) {
        if (design == null) {
            ModernUI.showToast(this, "⚠ No design loaded. Create a new design first.", ModernUI.WARNING);
            return;
        }

        saveUndoState();

        // Map catalog category string to enum
        FurnitureItem.Category category;
        String customCatName = null;
        try {
            category = FurnitureItem.Category.valueOf(catItem.getCategory());
        } catch (IllegalArgumentException e) {
            category = FurnitureItem.Category.CUSTOM;
            customCatName = catItem.getCategory();
        }

        FurnitureItem item = new FurnitureItem(
                catItem.getName(), category,
                catItem.getWidth(), catItem.getDepth(), catItem.getHeight(),
                catItem.getAwtColor()
        );
        if (customCatName != null) {
            item.setCustomCategoryName(customCatName);
        }

        placeAndSelectItem(item);
    }

    /**
     * Places a furniture item in the center of the room and selects it.
     */
    private void placeAndSelectItem(FurnitureItem item) {
        // Place in center of room
        item.setX(design.getRoom().getWidth() / 2 - item.getWidth() / 2);
        item.setY(design.getRoom().getDepth() / 2 - item.getDepth() / 2);

        design.addFurniture(item);
        selectItem(item);
        canvas.repaint();

        statusLabel.setText("Added: " + item.getName());
        ModernUI.showToast(this, "✓ " + item.getName() + " added", ModernUI.SUCCESS);
    }

    private void selectItem(FurnitureItem item) {
        if (design != null) design.clearSelection();
        selectedItem = item;
        if (item != null) {
            item.setSelected(true);
            statusLabel.setText("Selected: " + item.getName() +
                    " | Size: " + String.format("%.1f×%.1f", item.getScaledWidth(), item.getScaledDepth()) + "m" +
                    " | Scale: " + String.format("%.0f%%", item.getScale() * 100));
        }
        canvas.repaint();
    }

    // ===========================
    // COLOUR & SHADING MENUS
    // ===========================

    /** Popup colour menu — choose to colour selected item or all items */
    private void showColorMenu(Component anchor) {
        if (design == null) {
            ModernUI.showToast(this, "⚠ No design loaded", ModernUI.WARNING);
            return;
        }
        JPopupMenu menu = createStyledPopup();

        JMenuItem selectedOpt = createStyledMenuItem("🎨 Colour Selected Piece");
        selectedOpt.addActionListener(e -> changeSelectedColor());
        menu.add(selectedOpt);

        JMenuItem allOpt = createStyledMenuItem("🎨 Colour All Pieces");
        allOpt.addActionListener(e -> changeAllColors());
        menu.add(allOpt);

        menu.addSeparator();

        JMenuItem floorOpt = createStyledMenuItem("🏠 Change Floor Colour");
        floorOpt.addActionListener(e -> changeFloorColor());
        menu.add(floorOpt);

        JMenuItem wallOpt = createStyledMenuItem("🧱 Change Wall Colour");
        wallOpt.addActionListener(e -> changeWallColor());
        menu.add(wallOpt);

        menu.show(anchor, 0, anchor.getHeight());
    }

    /** Popup shading menu — shade selected or entire design */
    private void showShadeMenu(Component anchor) {
        if (design == null) {
            ModernUI.showToast(this, "⚠ No design loaded", ModernUI.WARNING);
            return;
        }
        JPopupMenu menu = createStyledPopup();

        JMenuItem selDarken  = createStyledMenuItem("🌑 Darken Selected");
        selDarken.addActionListener(e -> applyShadingToSelected(0.15));
        menu.add(selDarken);

        JMenuItem selLighten = createStyledMenuItem("☀ Lighten Selected");
        selLighten.addActionListener(e -> applyShadingToSelected(-0.15));
        menu.add(selLighten);

        JMenuItem selSlider  = createStyledMenuItem("🌗 Shade Selected (Slider)");
        selSlider.addActionListener(e -> showShadingSlider(false));
        menu.add(selSlider);

        menu.addSeparator();

        JMenuItem allDarken  = createStyledMenuItem("🌑 Darken Entire Design");
        allDarken.addActionListener(e -> applyShadingToAll(0.15));
        menu.add(allDarken);

        JMenuItem allLighten = createStyledMenuItem("☀ Lighten Entire Design");
        allLighten.addActionListener(e -> applyShadingToAll(-0.15));
        menu.add(allLighten);

        JMenuItem allSlider  = createStyledMenuItem("🌗 Shade Entire Design (Slider)");
        allSlider.addActionListener(e -> showShadingSlider(true));
        menu.add(allSlider);

        menu.addSeparator();

        JMenuItem reset  = createStyledMenuItem("↺ Reset All Shading");
        reset.addActionListener(e -> resetAllShading());
        menu.add(reset);

        menu.show(anchor, 0, anchor.getHeight());
    }

    // ===========================
    // COLOUR ACTIONS
    // ===========================

    private void changeSelectedColor() {
        if (selectedItem == null) {
            ModernUI.showToast(this, "⚠ Select a furniture item first", ModernUI.WARNING);
            return;
        }
        Color c = JColorChooser.showDialog(this, "Choose Furniture Colour", selectedItem.getColor());
        if (c != null) {
            saveUndoState();
            selectedItem.setColor(c);
            canvas.repaint();
            ModernUI.showToast(this, "✓ Colour updated", ModernUI.SUCCESS);
        }
    }

    private void changeAllColors() {
        if (design == null || design.getFurnitureItems().isEmpty()) {
            ModernUI.showToast(this, "⚠ No furniture in the design", ModernUI.WARNING);
            return;
        }
        Color c = JColorChooser.showDialog(this, "Choose Colour for All Furniture", Color.WHITE);
        if (c != null) {
            saveUndoState();
            for (FurnitureItem item : design.getFurnitureItems()) {
                item.setColor(c);
            }
            canvas.repaint();
            ModernUI.showToast(this, "✓ Colour applied to all " + design.getFurnitureItems().size() + " items", ModernUI.SUCCESS);
        }
    }

    private void changeFloorColor() {
        if (design == null) return;
        Color c = JColorChooser.showDialog(this, "Choose Floor Colour", design.getRoom().getFloorColor());
        if (c != null) {
            design.getRoom().setFloorColor(c);
            canvas.repaint();
            ModernUI.showToast(this, "✓ Floor colour updated", ModernUI.SUCCESS);
        }
    }

    private void changeWallColor() {
        if (design == null) return;
        Color c = JColorChooser.showDialog(this, "Choose Wall Colour", design.getRoom().getWallColor());
        if (c != null) {
            design.getRoom().setWallColor(c);
            canvas.repaint();
            ModernUI.showToast(this, "✓ Wall colour updated", ModernUI.SUCCESS);
        }
    }

    // ===========================
    // SHADING ACTIONS
    // ===========================

    private void applyShadingToSelected(double delta) {
        if (selectedItem == null) {
            ModernUI.showToast(this, "⚠ Select a furniture item first", ModernUI.WARNING);
            return;
        }
        saveUndoState();
        selectedItem.setShading(selectedItem.getShading() + delta);
        canvas.repaint();
        String label = delta > 0 ? "Darkened" : "Lightened";
        statusLabel.setText(label + ": " + selectedItem.getName() +
                " | Shade: " + String.format("%+.0f%%", selectedItem.getShading() * 100));
    }

    private void applyShadingToAll(double delta) {
        if (design == null || design.getFurnitureItems().isEmpty()) {
            ModernUI.showToast(this, "⚠ No furniture in the design", ModernUI.WARNING);
            return;
        }
        saveUndoState();
        for (FurnitureItem item : design.getFurnitureItems()) {
            item.setShading(item.getShading() + delta);
        }
        canvas.repaint();
        String label = delta > 0 ? "Darkened" : "Lightened";
        ModernUI.showToast(this, "✓ " + label + " all " + design.getFurnitureItems().size() + " items", ModernUI.SUCCESS);
    }

    private void resetAllShading() {
        if (design == null || design.getFurnitureItems().isEmpty()) return;
        saveUndoState();
        for (FurnitureItem item : design.getFurnitureItems()) {
            item.setShading(0);
        }
        canvas.repaint();
        ModernUI.showToast(this, "✓ All shading reset", ModernUI.SUCCESS);
    }

    /** Interactive slider dialog for fine-grained shading control */
    private void showShadingSlider(boolean applyToAll) {
        if (!applyToAll && selectedItem == null) {
            ModernUI.showToast(this, "⚠ Select a furniture item first", ModernUI.WARNING);
            return;
        }
        if (design == null) return;

        // Calculate current shading
        double currentShading;
        if (applyToAll && !design.getFurnitureItems().isEmpty()) {
            currentShading = design.getFurnitureItems().get(0).getShading();
        } else if (selectedItem != null) {
            currentShading = selectedItem.getShading();
        } else {
            currentShading = 0;
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(380, 120));

        JLabel titleLbl = new JLabel(applyToAll ? "Shade Entire Design" : "Shade: " + selectedItem.getName());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(titleLbl, BorderLayout.NORTH);

        // Slider: -100 (lighten) to +100 (darken)
        JSlider slider = new JSlider(-100, 100, (int)(currentShading * 100));
        slider.setMajorTickSpacing(25);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        java.util.Hashtable<Integer, JLabel> labels = new java.util.Hashtable<>();
        labels.put(-100, new JLabel("Light"));
        labels.put(0, new JLabel("None"));
        labels.put(100, new JLabel("Dark"));
        slider.setLabelTable(labels);

        JLabel valueLbl = new JLabel("Shading: " + (int)(currentShading * 100) + "%");
        valueLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        slider.addChangeListener(e -> {
            int val = slider.getValue();
            valueLbl.setText("Shading: " + (val >= 0 ? "+" : "") + val + "%");
        });

        panel.add(slider, BorderLayout.CENTER);
        panel.add(valueLbl, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Adjust Shading", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            double newShading = slider.getValue() / 100.0;
            saveUndoState();
            if (applyToAll) {
                for (FurnitureItem item : design.getFurnitureItems()) {
                    item.setShading(newShading);
                }
                ModernUI.showToast(this, "✓ Shading applied to all items", ModernUI.SUCCESS);
            } else {
                selectedItem.setShading(newShading);
                ModernUI.showToast(this, "✓ Shading applied to " + selectedItem.getName(), ModernUI.SUCCESS);
            }
            canvas.repaint();
        }
    }

    // ===========================
    // POPUP HELPERS
    // ===========================

    private JPopupMenu createStyledPopup() {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(ThemeManager.bgSecondary());
        menu.setBorder(BorderFactory.createLineBorder(ThemeManager.border()));
        return menu;
    }

    private JMenuItem createStyledMenuItem(String text) {
        JMenuItem mi = new JMenuItem(text);
        mi.setFont(ModernUI.FONT_BODY);
        mi.setForeground(ThemeManager.textPrimary());
        mi.setBackground(ThemeManager.bgSecondary());
        mi.setBorder(new EmptyBorder(6, 12, 6, 12));
        return mi;
    }

    private void scaleSelected(double factor) {
        if (selectedItem == null) {
            ModernUI.showToast(this, "⚠ Select a furniture item first", ModernUI.WARNING);
            return;
        }
        saveUndoState();
        selectedItem.setScale(selectedItem.getScale() * factor);
        selectItem(selectedItem); // Update status
        canvas.repaint();
    }

    private void rotateSelected() {
        if (selectedItem == null) {
            ModernUI.showToast(this, "⚠ Select a furniture item first", ModernUI.WARNING);
            return;
        }
        saveUndoState();
        selectedItem.setRotation(selectedItem.getRotation() + 90);
        canvas.repaint();
        statusLabel.setText("Rotated: " + selectedItem.getName() + " to " + (int) selectedItem.getRotation() + "°");
    }

    private void deleteSelected() {
        if (selectedItem == null) {
            ModernUI.showToast(this, "⚠ Select a furniture item first", ModernUI.WARNING);
            return;
        }
        saveUndoState();
        String name = selectedItem.getName();
        design.removeFurniture(selectedItem);
        selectedItem = null;
        canvas.repaint();
        statusLabel.setText("Deleted: " + name);
        ModernUI.showToast(this, "✓ " + name + " removed", ModernUI.SUCCESS);
    }

    private void saveDesign() {
        if (design == null) {
            ModernUI.showToast(this, "⚠ No design to save", ModernUI.WARNING);
            return;
        }
        if (FileManager.saveDesign(design)) {
            ModernUI.showToast(this, "✓ Design saved successfully!", ModernUI.SUCCESS);
            statusLabel.setText("Design saved: " + design.getRoom().getName());
        } else {
            ModernUI.showToast(this, "✗ Failed to save design", ModernUI.ERROR);
        }
    }

    private void fitToView() {
        if (design == null || canvas.getWidth() == 0) return;
        double roomW = design.getRoom().getWidth() * pixelsPerMetre;
        double roomD = design.getRoom().getDepth() * pixelsPerMetre;
        double scaleX = (canvas.getWidth() - 120) / roomW;
        double scaleY = (canvas.getHeight() - 120) / roomD;
        zoom = Math.min(scaleX, scaleY);
        panX = (canvas.getWidth() - roomW * zoom) / 2;
        panY = (canvas.getHeight() - roomD * zoom) / 2;
        updateZoomLabel();
        canvas.repaint();
    }

    private void updateZoomLabel() {
        zoomLabel.setText("Zoom: " + (int) (zoom * 100) + "%");
    }

    // --- Undo ---
    private void saveUndoState() {
        if (design == null) return;
        // Trim any redo states
        while (undoHistory.size() > undoIndex + 1) {
            undoHistory.remove(undoHistory.size() - 1);
        }
        List<FurnitureItem> snapshot = new ArrayList<>();
        for (FurnitureItem item : design.getFurnitureItems()) {
            snapshot.add(item.copy());
        }
        undoHistory.add(snapshot);
        undoIndex++;
    }

    private void undo() {
        if (undoIndex < 0 || design == null) {
            ModernUI.showToast(this, "⚠ Nothing to undo", ModernUI.WARNING);
            return;
        }
        List<FurnitureItem> snapshot = undoHistory.get(undoIndex);
        design.setFurnitureItems(new ArrayList<>(snapshot));
        undoIndex--;
        selectedItem = null;
        canvas.repaint();
        statusLabel.setText("Undo performed");
    }

    /**
     * Sets the design to display and edit.
     */
    public void setDesign(Design design) {
        this.design = design;
        this.selectedItem = null;
        undoHistory.clear();
        undoIndex = -1;
        fitToView();

        // Show design info including notes in the status bar
        if (design != null) {
            String info = design.getRoom().getName() + " | " +
                    design.getFurnitureItems().size() + " items";
            String notes = design.getNotes();
            if (notes != null && !notes.isEmpty()) {
                String truncated = notes.replace("\n", " ");
                if (truncated.length() > 60) truncated = truncated.substring(0, 60) + "…";
                info += " | 📝 " + truncated;
            }
            statusLabel.setText(info);
        }

        canvas.repaint();
    }

    // ===========================
    // CANVAS PANEL - Custom painting
    // ===========================
    private class CanvasPanel extends JPanel {

        public CanvasPanel() {
            setBackground(new Color(30, 30, 50));
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

            // Mouse listeners for interaction
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (design == null) return;

                    double mx = screenToRoomX(e.getX());
                    double my = screenToRoomY(e.getY());

                    if (SwingUtilities.isRightMouseButton(e)) {
                        showContextMenu(e);
                        return;
                    }

                    // Check if clicking on a furniture item
                    FurnitureItem clicked = design.getFurnitureAt(mx, my);
                    if (clicked != null) {
                        selectItem(clicked);
                        dragging = true;
                        dragOffsetX = mx - clicked.getX();
                        dragOffsetY = my - clicked.getY();
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    } else {
                        selectItem(null);
                        selectedItem = null;
                        statusLabel.setText("Ready");
                        // Start panning
                        lastMousePos = e.getPoint();
                    }
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (dragging) {
                        dragging = false;
                        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    }
                    lastMousePos = null;
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (design == null) return;

                    if (dragging && selectedItem != null) {
                        double mx = screenToRoomX(e.getX());
                        double my = screenToRoomY(e.getY());

                        double newX = mx - dragOffsetX;
                        double newY = my - dragOffsetY;

                        // Clamp to room bounds
                        newX = Math.max(0, Math.min(design.getRoom().getWidth() - selectedItem.getScaledWidth(), newX));
                        newY = Math.max(0, Math.min(design.getRoom().getDepth() - selectedItem.getScaledDepth(), newY));

                        selectedItem.setX(newX);
                        selectedItem.setY(newY);
                        selectItem(selectedItem);
                        repaint();
                    } else if (lastMousePos != null) {
                        // Panning
                        panX += e.getX() - lastMousePos.x;
                        panY += e.getY() - lastMousePos.y;
                        lastMousePos = e.getPoint();
                        repaint();
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    if (design == null) return;
                    double mx = screenToRoomX(e.getX());
                    double my = screenToRoomY(e.getY());
                    if (mx >= 0 && mx <= design.getRoom().getWidth() && my >= 0 && my <= design.getRoom().getDepth()) {
                        statusLabel.setText(String.format("Position: %.1fm, %.1fm", mx, my) +
                                (selectedItem != null ? " | Selected: " + selectedItem.getName() : ""));
                    }
                }
            });

            addMouseWheelListener(e -> {
                double oldZoom = zoom;
                if (e.getWheelRotation() < 0) {
                    zoom = Math.min(5.0, zoom + 0.1);
                } else {
                    zoom = Math.max(0.3, zoom - 0.1);
                }
                // Zoom towards mouse position
                double factor = zoom / oldZoom;
                panX = e.getX() - (e.getX() - panX) * factor;
                panY = e.getY() - (e.getY() - panY) * factor;
                updateZoomLabel();
                repaint();
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            ModernUI.enableAntiAliasing(g2);

            // Background pattern (subtle dots)
            g2.setColor(new Color(35, 35, 55));
            for (int x = 0; x < getWidth(); x += 20) {
                for (int y = 0; y < getHeight(); y += 20) {
                    g2.fillOval(x, y, 2, 2);
                }
            }

            if (design == null) {
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                g2.setColor(ThemeManager.textMuted());
                g2.setFont(ModernUI.FONT_BODY);
                FontMetrics fm = g2.getFontMetrics();
                String msg = "Use \"New Design\" to create a room, or \"My Designs\" to open one";
                g2.drawString(msg, cx - fm.stringWidth(msg) / 2, cy);

                g2.dispose();
                return;
            }

            // Apply transformations
            AffineTransform original = g2.getTransform();
            g2.translate(panX, panY);
            g2.scale(zoom, zoom);

            Room room = design.getRoom();
            double roomW = room.getWidth() * pixelsPerMetre;
            double roomH = room.getDepth() * pixelsPerMetre;

            // --- Draw room floor ---
            g2.setColor(room.getFloorColor());
            g2.fillRect(0, 0, (int) roomW, (int) roomH);

            // --- Draw grid ---
            if (showGrid) {
                g2.setColor(ModernUI.GRID_COLOR);
                g2.setStroke(new BasicStroke(0.5f));
                double gridSpacing = pixelsPerMetre; // 1 metre grid
                for (double x = 0; x <= roomW; x += gridSpacing) {
                    g2.drawLine((int) x, 0, (int) x, (int) roomH);
                }
                for (double y = 0; y <= roomH; y += gridSpacing) {
                    g2.drawLine(0, (int) y, (int) roomW, (int) y);
                }

                // Grid labels (metres)
                g2.setFont(ModernUI.FONT_TINY);
                g2.setColor(ThemeManager.textMuted());
                for (int m = 0; m <= room.getWidth(); m++) {
                    g2.drawString(m + "m", (int) (m * pixelsPerMetre) + 2, -4);
                }
                for (int m = 0; m <= room.getDepth(); m++) {
                    g2.drawString(m + "m", -28, (int) (m * pixelsPerMetre) + 12);
                }
            }

            // --- Draw wall border ---
            g2.setColor(room.getWallColor().darker());
            g2.setStroke(new BasicStroke(4f));
            g2.drawRect(0, 0, (int) roomW, (int) roomH);

            // Wall thickness illusion
            g2.setColor(room.getWallColor());
            g2.setStroke(new BasicStroke(8f));
            g2.drawRect(-4, -4, (int) roomW + 8, (int) roomH + 8);

            // --- Draw furniture items ---
            for (FurnitureItem item : design.getFurnitureItems()) {
                drawFurnitureItem(g2, item);
            }

            // --- Room dimensions labels ---
            g2.setFont(ModernUI.FONT_SMALL);
            g2.setColor(new Color(80, 80, 100));

            // Width label
            String widthLabel = String.format("%.1fm", room.getWidth());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(widthLabel, (int) (roomW / 2 - fm.stringWidth(widthLabel) / 2), (int) roomH + 24);

            // Depth label
            AffineTransform at = g2.getTransform();
            g2.rotate(-Math.PI / 2, -20, roomH / 2);
            String depthLabel = String.format("%.1fm", room.getDepth());
            g2.drawString(depthLabel, (int) (-20 - fm.stringWidth(depthLabel) / 2), (int) (roomH / 2 + 5));
            g2.setTransform(at);

            g2.setTransform(original);
            g2.dispose();
        }

        /**
         * Draws a single furniture item on the canvas.
         */
        private void drawFurnitureItem(Graphics2D g2, FurnitureItem item) {
            double x = item.getX() * pixelsPerMetre;
            double y = item.getY() * pixelsPerMetre;
            double w = item.getScaledWidth() * pixelsPerMetre;
            double d = item.getScaledDepth() * pixelsPerMetre;

            int ix = (int) x, iy = (int) y, iw = (int) w, id = (int) d;

            // Handle rotation
            AffineTransform old = g2.getTransform();
            if (item.getRotation() != 0) {
                g2.rotate(Math.toRadians(item.getRotation()), ix + iw / 2.0, iy + id / 2.0);
            }

            boolean isBeanbag = item.getName() != null && item.getName().toLowerCase().contains("beanbag");
            boolean isRound = isBeanbag || (item.getName() != null && item.getName().toLowerCase().contains("round"));

            // Shadow
            g2.setColor(new Color(0, 0, 0, 40));
            if (isRound) {
                g2.fillOval(ix + 3, iy + 3, iw, id);
            } else {
                g2.fillRoundRect(ix + 3, iy + 3, iw, id, 6, 6);
            }

            // Main body — use shaded colour
            Color displayColor = item.getShadedColor();
            g2.setColor(displayColor);
            if (isRound) {
                g2.fillOval(ix, iy, iw, id);
            } else {
                g2.fillRoundRect(ix, iy, iw, id, 6, 6);
            }

            // Highlight (top edge gradient) — subtler when darkened
            int highlightAlpha = item.getShading() > 0 ? (int)(50 * (1 - item.getShading())) : 50;
            g2.setColor(new Color(255, 255, 255, Math.max(0, highlightAlpha)));
            if (isRound) {
                g2.fillOval(ix + iw / 4, iy + id / 8, iw / 2, id / 4);
            } else {
                g2.fillRoundRect(ix, iy, iw, id / 3, 6, 6);
            }

            // Draw category-specific details
            if (isBeanbag) {
                // Wrinkles for the beanbag
                g2.setColor(new Color(0, 0, 0, 60));
                g2.drawArc(ix + iw / 4, iy + id / 4, iw / 2, id / 2, 45, 90);
                g2.drawArc(ix + iw / 3, iy + id / 3, iw / 3, id / 3, 180, 90);
            } else {
                drawFurnitureDetails(g2, item, ix, iy, iw, id);
            }

            // Border
            if (item.isSelected()) {
                g2.setColor(ModernUI.PRIMARY);
                g2.setStroke(new BasicStroke(3f));
                if (isRound) {
                    g2.drawOval(ix - 2, iy - 2, iw + 4, id + 4);
                } else {
                    g2.drawRoundRect(ix - 2, iy - 2, iw + 4, id + 4, 8, 8);
                }

                // Selection handles
                g2.setColor(ModernUI.PRIMARY_LIGHT);
                int hs = 6;
                g2.fillRect(ix - hs / 2, iy - hs / 2, hs, hs);
                g2.fillRect(ix + iw - hs / 2, iy - hs / 2, hs, hs);
                g2.fillRect(ix - hs / 2, iy + id - hs / 2, hs, hs);
                g2.fillRect(ix + iw - hs / 2, iy + id - hs / 2, hs, hs);
            } else {
                g2.setColor(displayColor.darker());
                g2.setStroke(new BasicStroke(1.5f));
                if (isRound) {
                    g2.drawOval(ix, iy, iw, id);
                } else {
                    g2.drawRoundRect(ix, iy, iw, id, 6, 6);
                }
            }

            // Shading indicator — subtle overlay pattern when shaded
            if (item.getShading() != 0) {
                int overlayAlpha = (int)(Math.abs(item.getShading()) * 30);
                if (item.getShading() > 0) {
                    // Dark hatching
                    g2.setColor(new Color(0, 0, 0, overlayAlpha));
                } else {
                    // Light hatching
                    g2.setColor(new Color(255, 255, 255, overlayAlpha));
                }
                g2.setStroke(new BasicStroke(0.5f));
                for (int step = -id; step < iw; step += 6) {
                    g2.drawLine(ix + step, iy, ix + step + id, iy + id);
                }
            }

            // Label
            g2.setFont(ModernUI.FONT_TINY);
            g2.setColor(isLightColor(displayColor) ? Color.BLACK : Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            String label = item.getCategory().getDisplayName();
            if (fm.stringWidth(label) < iw - 6) {
                g2.drawString(label, ix + (iw - fm.stringWidth(label)) / 2,
                        iy + (id + fm.getAscent() - fm.getDescent()) / 2);
            }

            g2.setTransform(old);
            g2.setStroke(new BasicStroke(1f));
        }

        private void drawFurnitureDetails(Graphics2D g2, FurnitureItem item, int x, int y, int w, int h) {
            Color detailDark = new Color(0, 0, 0, 60);
            Color detailLight = new Color(255, 255, 255, 60);

            switch (item.getCategory()) {
                case SOFA:
                    // Backrest
                    g2.setColor(detailDark);
                    g2.fillRoundRect(x + 4, y + 4, w - 8, h / 4, 6, 6);
                    // Armrests
                    g2.fillRoundRect(x + 4, y + 4, w / 6, h - 8, 8, 8);
                    g2.fillRoundRect(x + w - (w / 6) - 4, y + 4, w / 6, h - 8, 8, 8);
                    // Seat Cushions (3 cushions)
                    int cushW = (w - (w / 3) - 16) / 3;
                    g2.setColor(detailLight);
                    for (int i = 0; i < 3; i++) {
                        g2.fillRoundRect(x + 8 + (w / 6) + (i * (cushW + 2)), y + 8 + (h / 4), cushW, h - (h / 4) - 16, 8, 8);
                        g2.setColor(detailDark);
                        g2.drawRoundRect(x + 8 + (w / 6) + (i * (cushW + 2)), y + 8 + (h / 4), cushW, h - (h / 4) - 16, 8, 8);
                        g2.setColor(detailLight);
                    }
                    break;

                case BED:
                    // Wooden Headboard
                    g2.setColor(new Color(110, 70, 40));
                    g2.fillRoundRect(x + 2, y + 2, w - 4, h / 8, 4, 4);
                    // Two Pillows
                    g2.setColor(new Color(240, 240, 245));
                    g2.fillRoundRect(x + w / 8, y + h / 6, w / 3, h / 6, 8, 8);
                    g2.fillRoundRect(x + w - (w / 3) - (w / 8), y + h / 6, w / 3, h / 6, 8, 8);
                    g2.setColor(new Color(0, 0, 0, 20)); // Pillow depth
                    g2.drawRoundRect(x + w / 8, y + h / 6, w / 3, h / 6, 8, 8);
                    g2.drawRoundRect(x + w - (w / 3) - (w / 8), y + h / 6, w / 3, h / 6, 8, 8);
                    // Folded Blanket
                    g2.setColor(item.getShadedColor().darker());
                    g2.fillRoundRect(x - 2, y + h / 2, w + 4, h / 2 + 2, 8, 8);
                    g2.setColor(new Color(0, 0, 0, 30)); // Blanket fold line
                    g2.drawLine(x, y + h / 2 + 4, x + w, y + h / 2 + 4);
                    break;

                case CHAIR:
                    // Curved Backrest
                    g2.setColor(detailDark);
                    g2.fillRoundRect(x + 4, y + 4, w - 8, h / 4, 10, 10);
                    // Seat Cushion
                    g2.setColor(detailLight);
                    g2.fillRoundRect(x + 6, y + h / 4 + 4, w - 12, h - (h / 4) - 10, 12, 12);
                    g2.setColor(detailDark);
                    g2.drawRoundRect(x + 6, y + h / 4 + 4, w - 12, h - (h / 4) - 10, 12, 12);
                    break;

                case DINING_TABLE:
                case COFFEE_TABLE:
                case SIDE_TABLE:
                    // Wood Grain / Glass Reflection
                    g2.setColor(detailLight);
                    g2.fillOval(x + w / 8, y + h / 8, w - (w / 4), h - (h / 4));
                    g2.setColor(detailDark);
                    g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, 8, 8);
                    g2.drawLine(x + w / 4, y + h / 4, x + w - (w / 4), y + h - (h / 4)); // Subtle glare
                    break;

                case DESK:
                    // Desk pad / laptop area
                    g2.setColor(new Color(0, 0, 0, 80));
                    g2.fillRoundRect(x + w / 4, y + h / 4, w / 2, h / 2, 4, 4);
                    // Keyboard/laptop glare
                    g2.setColor(detailLight);
                    g2.fillRect(x + w / 4 + 2, y + h / 4 + 2, w / 2 - 4, h / 4);
                    break;

                case WARDROBE:
                case BOOKSHELF:
                case TV_UNIT:
                    // Front lip / doors
                    g2.setColor(detailDark);
                    g2.fillRect(x + 2, y + h - 6, w - 4, 4); 
                    // Split down the middle for doors
                    g2.drawLine(x + w / 2, y + 4, x + w / 2, y + h - 6);
                    // Handles
                    g2.setColor(detailLight);
                    g2.fillRect(x + w / 2 - 6, y + h / 2, 4, h / 6);
                    g2.fillRect(x + w / 2 + 2, y + h / 2, 4, h / 6);
                    break;

                case LAMP:
                    // Circular shade
                    g2.setColor(detailLight);
                    g2.fillOval(x + 2, y + 2, w - 4, h - 4);
                    g2.setColor(detailDark);
                    g2.drawOval(x + 2, y + 2, w - 4, h - 4);
                    // Light bulb core
                    g2.setColor(new Color(255, 255, 150));
                    g2.fillOval(x + w / 2 - 4, y + h / 2 - 4, 8, 8);
                    break;

                case RUG:
                    // Fringes on the ends
                    g2.setColor(detailDark);
                    for(int i = 0; i < w; i += 4) {
                        g2.drawLine(x + i, y, x + i, y + 4);
                        g2.drawLine(x + i, y + h, x + i, y + h - 4);
                    }
                    // Inner pattern rectangle
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.drawRect(x + 8, y + 8, w - 16, h - 16);
                    g2.drawRect(x + 12, y + 12, w - 24, h - 24);
                    break;

                case MIRROR:
                    // Reflective Glass illusion
                    g2.setColor(new Color(220, 240, 255, 200)); 
                    boolean isRound = item.getName() != null && item.getName().toLowerCase().contains("round");
                    if (isRound) {
                        g2.fillOval(x + 4, y + 4, w - 8, h - 8);
                        // Glare slash
                        g2.setColor(new Color(255, 255, 255, 120));
                        g2.drawArc(x + 8, y + 8, w - 16, h - 16, 45, 60);
                    } else {
                        g2.fillRect(x + 4, y + 4, w - 8, h - 8);
                        // Diagonal reflection glare
                        g2.setColor(new Color(255, 255, 255, 150));
                        g2.drawLine(x + 4, y + h - 10, x + w - 10, y + 4);
                        g2.drawLine(x + 4, y + h - 4, x + w - 4, y + 4);    
                    }
                    break;

                default:
                    // Generic inset line
                    g2.setColor(detailDark);
                    g2.drawRect(x + 4, y + 4, w - 8, h - 8);
                    break;
            }
        }

        private void showContextMenu(MouseEvent e) {
            if (selectedItem == null) return;

            JPopupMenu menu = new JPopupMenu();
            menu.setBackground(ThemeManager.bgSecondary());
            menu.setBorder(BorderFactory.createLineBorder(ThemeManager.border()));

            addMenuItem(menu, "🎨 Change Colour", ev -> changeSelectedColor());
            addMenuItem(menu, "🌑 Darken", ev -> applyShadingToSelected(0.15));
            addMenuItem(menu, "☀ Lighten", ev -> applyShadingToSelected(-0.15));
            addMenuItem(menu, "🌗 Shade (Slider)", ev -> showShadingSlider(false));
            menu.addSeparator();
            addMenuItem(menu, "Scale Up (110%)", ev -> scaleSelected(1.1));
            addMenuItem(menu, "Scale Down (90%)", ev -> scaleSelected(0.9));
            addMenuItem(menu, "Rotate 90°", ev -> rotateSelected());
            menu.addSeparator();
            addMenuItem(menu, "🎨 Colour All Pieces", ev -> changeAllColors());
            addMenuItem(menu, "🌗 Shade All (Slider)", ev -> showShadingSlider(true));
            menu.addSeparator();
            addMenuItem(menu, "Duplicate", ev -> duplicateSelected());
            addMenuItem(menu, "Delete", ev -> deleteSelected());

            menu.show(this, e.getX(), e.getY());
        }

        private void addMenuItem(JPopupMenu menu, String text, ActionListener action) {
            JMenuItem mi = new JMenuItem(text);
            mi.setFont(ModernUI.FONT_BODY);
            mi.setForeground(ThemeManager.textPrimary());
            mi.setBackground(ThemeManager.bgSecondary());
            mi.setBorder(new EmptyBorder(6, 12, 6, 12));
            mi.addActionListener(action);
            menu.add(mi);
        }
    }

    private void duplicateSelected() {
        if (selectedItem == null || design == null) return;
        saveUndoState();
        FurnitureItem copy = selectedItem.copy();
        copy.setX(copy.getX() + 0.3);
        copy.setY(copy.getY() + 0.3);
        design.addFurniture(copy);
        selectItem(copy);
        canvas.repaint();
        ModernUI.showToast(this, "✓ Item duplicated", ModernUI.SUCCESS);
    }

    // --- Coordinate conversion ---
    private double screenToRoomX(int sx) {
        return (sx - panX) / (zoom * pixelsPerMetre);
    }

    private double screenToRoomY(int sy) {
        return (sy - panY) / (zoom * pixelsPerMetre);
    }

    private boolean isLightColor(Color c) {
        return (c.getRed() * 0.299 + c.getGreen() * 0.587 + c.getBlue() * 0.114) > 128;
    }
}
