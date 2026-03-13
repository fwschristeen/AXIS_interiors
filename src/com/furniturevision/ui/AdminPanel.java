package com.furniturevision.ui;

import com.furniturevision.backend.BackendService;
import com.furniturevision.backend.FurnitureCatalogDAO;
import com.furniturevision.backend.FurnitureCatalogDAO.CatalogItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin Panel for managing the Furniture Catalog.
 * Full theme-aware table with zebra rows, pill badges, and colour swatches.
 */
public class AdminPanel extends JPanel {

    private FurnitureCatalogDAO catalogDAO;

    private JTable table;
    private CatalogTableModel tableModel;
    private JLabel statusLabel;
    private JLabel countLabel;

    // Theme-aware row helpers
    private static Color rowEven()     { return ThemeManager.isDark() ? new Color(30, 32, 42)  : new Color(255, 255, 255); }
    private static Color rowOdd()      { return ThemeManager.isDark() ? new Color(36, 38, 50)  : new Color(245, 246, 250); }
    private static Color rowSelected() { return ThemeManager.isDark() ? new Color(108, 99, 255, 50) : new Color(108, 99, 255, 30); }
    private static Color headerBg()    { return ThemeManager.isDark() ? new Color(24, 26, 34)  : new Color(240, 241, 248); }
    private static Color gridLine()    { return ThemeManager.isDark() ? new Color(45, 48, 62)  : new Color(230, 232, 240); }
    private static Color headerText()  { return ThemeManager.isDark() ? new Color(140, 145, 170) : new Color(100, 105, 130); }
    private static Color idText()      { return ThemeManager.isDark() ? new Color(100, 104, 130) : new Color(160, 165, 180); }
    private static Color hexText()     { return ThemeManager.isDark() ? new Color(160, 165, 190) : new Color(120, 125, 145); }

    // Available categories (matches FurnitureItem.Category enum)
    private static final String[] CATEGORIES = {
        "CHAIR", "DINING_TABLE", "SIDE_TABLE", "SOFA", "COFFEE_TABLE",
        "BOOKSHELF", "BED", "WARDROBE", "DESK", "RUG", "LAMP", "TV_UNIT", "MIRROR"
    };

    private static final String[] CATEGORY_LABELS = {
        "Chair", "Dining Table", "Side Table", "Sofa", "Coffee Table",
        "Bookshelf", "Bed", "Wardrobe", "Desk", "Rug", "Lamp", "TV Unit", "Mirror"
    };

    public AdminPanel(DashboardFrame dashboard) {
        this.catalogDAO = BackendService.getInstance().getCatalogDAO();
        setBackground(ThemeManager.bgPrimary());
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // ===========================
        // HEADER
        // ===========================
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                g2.setColor(ThemeManager.bgSecondary());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(ThemeManager.border());
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 70));
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(14, 24, 14, 24));

        // Title
        JLabel titleLabel = new JLabel("🪑  Furniture Catalog") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                g2.setFont(getFont()); g2.setColor(ThemeManager.textPrimary());
                g2.drawString(getText(), 0, g2.getFontMetrics().getAscent());
                g2.dispose();
            }
        };
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        countLabel = new JLabel("Loading...") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                g2.setFont(getFont()); g2.setColor(ThemeManager.textMuted());
                g2.drawString(getText(), 0, g2.getFontMetrics().getAscent());
                g2.dispose();
            }
        };
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setBorder(new EmptyBorder(4, 0, 0, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(countLabel);
        header.add(titlePanel, BorderLayout.WEST);

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionPanel.setOpaque(false);

        JButton refreshBtn = createIconButton("↻", "Refresh", new Color(100, 110, 140));
        refreshBtn.addActionListener(e -> refreshTable());

        JButton addBtn = createPrimaryButton("+ Add Furniture");
        addBtn.addActionListener(e -> showAddDialog());

        actionPanel.add(refreshBtn);
        actionPanel.add(addBtn);
        header.add(actionPanel, BorderLayout.EAST);

        // ===========================
        // TABLE
        // ===========================
        tableModel = new CatalogTableModel();
        table = new JTable(tableModel);
        configureTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(rowEven());
        scrollPane.setBackground(rowEven());

        // ===========================
        // BOTTOM BAR
        // ===========================
        JPanel bottomBar = new JPanel() {
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
        bottomBar.setPreferredSize(new Dimension(0, 52));
        bottomBar.setLayout(new BorderLayout());
        bottomBar.setBorder(new EmptyBorder(0, 24, 0, 24));

        statusLabel = new JLabel("Ready") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                g2.setFont(getFont()); g2.setColor(ThemeManager.textMuted());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 0, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bottomBar.add(statusLabel, BorderLayout.WEST);

        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bottomActions.setOpaque(false);

        JButton editBtn = createIconButton("✎", "Edit", new Color(108, 99, 255));
        JButton toggleBtn = createIconButton("⏻", "Toggle", new Color(80, 180, 130));
        JButton deleteBtn = createIconButton("✗", "Delete", new Color(220, 80, 80));

        editBtn.addActionListener(e -> editSelected());
        toggleBtn.addActionListener(e -> toggleSelected());
        deleteBtn.addActionListener(e -> deleteSelected());

        bottomActions.add(editBtn);
        bottomActions.add(toggleBtn);
        bottomActions.add(deleteBtn);
        bottomBar.add(bottomActions, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        // Load data
        refreshTable();
    }

    // ===========================
    // TABLE CONFIGURATION
    // ===========================

    private void configureTable() {
        table.setBackground(rowEven());
        table.setForeground(ThemeManager.textPrimary());
        table.setGridColor(gridLine());
        table.setSelectionBackground(rowSelected());
        table.setSelectionForeground(ThemeManager.textPrimary());
        table.setRowHeight(48);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom header
        JTableHeader th = table.getTableHeader();
        th.setDefaultRenderer(new HeaderRenderer());
        th.setPreferredSize(new Dimension(0, 42));
        th.setReorderingAllowed(false);

        // Column widths
        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(50);   // ID
        cm.getColumn(0).setMaxWidth(60);
        cm.getColumn(1).setPreferredWidth(180);  // Name
        cm.getColumn(2).setPreferredWidth(120);  // Category
        cm.getColumn(3).setPreferredWidth(80);   // Width
        cm.getColumn(4).setPreferredWidth(80);   // Depth
        cm.getColumn(5).setPreferredWidth(80);   // Height
        cm.getColumn(6).setPreferredWidth(110);  // Color
        cm.getColumn(7).setPreferredWidth(90);   // Type
        cm.getColumn(8).setPreferredWidth(90);   // Status

        // Apply base striped renderer to all columns
        for (int i = 0; i < cm.getColumnCount(); i++) {
            cm.getColumn(i).setCellRenderer(new StripedCellRenderer(
                    i == 0 || (i >= 3 && i <= 5) ? SwingConstants.CENTER : SwingConstants.LEFT));
        }

        // Override specific column renderers
        cm.getColumn(1).setCellRenderer(new NameCellRenderer());
        cm.getColumn(6).setCellRenderer(new ColorCellRenderer());
        cm.getColumn(7).setCellRenderer(new BadgeCellRenderer(true));   // Type
        cm.getColumn(8).setCellRenderer(new BadgeCellRenderer(false));  // Status

        // Double-click to edit
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    editSelected();
                }
            }
        });
    }

    // ===========================
    // ACTIONS
    // ===========================

    private void refreshTable() {
        List<CatalogItem> items = catalogDAO.getAllItems();
        tableModel.setItems(items);

        long shopCount = items.stream().filter(i -> !i.isCustom()).count();
        long customCount = items.stream().filter(CatalogItem::isCustom).count();
        countLabel.setText(items.size() + " items  •  " + shopCount + " shop  •  " + customCount + " custom");
        statusLabel.setText("✓ Catalog loaded — " + items.size() + " items");
    }

    private void showAddDialog() {
        CatalogItem newItem = showItemDialog(null);
        if (newItem != null) {
            int id = catalogDAO.addItem(newItem);
            if (id > 0) {
                refreshTable();
                statusLabel.setText("✓ Added: " + newItem.getName());
                ModernUI.showToast(this, "✓ " + newItem.getName() + " added to catalog", ModernUI.SUCCESS);
            } else {
                ModernUI.showToast(this, "✗ Failed to add item", ModernUI.ERROR);
            }
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            ModernUI.showToast(this, "⚠ Select an item to edit", ModernUI.WARNING);
            return;
        }
        CatalogItem existing = tableModel.getItemAt(row);
        CatalogItem updated = showItemDialog(existing);
        if (updated != null) {
            updated.setId(existing.getId());
            if (catalogDAO.updateItem(updated)) {
                refreshTable();
                statusLabel.setText("✓ Updated: " + updated.getName());
                ModernUI.showToast(this, "✓ " + updated.getName() + " updated", ModernUI.SUCCESS);
            } else {
                ModernUI.showToast(this, "✗ Failed to update item", ModernUI.ERROR);
            }
        }
    }

    private void toggleSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            ModernUI.showToast(this, "⚠ Select an item to toggle", ModernUI.WARNING);
            return;
        }
        CatalogItem item = tableModel.getItemAt(row);
        boolean newStatus = !item.isActive();
        if (catalogDAO.toggleActive(item.getId(), newStatus)) {
            refreshTable();
            statusLabel.setText(item.getName() + " is now " + (newStatus ? "active" : "inactive"));
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            ModernUI.showToast(this, "⚠ Select an item to delete", ModernUI.WARNING);
            return;
        }
        CatalogItem item = tableModel.getItemAt(row);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + item.getName() + "\" from the catalog?\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (catalogDAO.deleteItem(item.getId())) {
                refreshTable();
                statusLabel.setText("✓ Deleted: " + item.getName());
                ModernUI.showToast(this, "✓ " + item.getName() + " deleted", ModernUI.SUCCESS);
            }
        }
    }

    // ===========================
    // ADD / EDIT DIALOG
    // ===========================

    private CatalogItem showItemDialog(CatalogItem existing) {
        boolean isEdit = existing != null;

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(420, 380));
        panel.setBackground(ThemeManager.bgSecondary());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Name
        JTextField nameField = createField(isEdit ? existing.getName() : "");
        addRow(panel, gbc, 0, "Name:", nameField);

        // Category
        JComboBox<String> categoryCombo = new JComboBox<>(CATEGORY_LABELS);
        categoryCombo.setFont(ModernUI.FONT_BODY);
        categoryCombo.setEditable(true);
        if (isEdit) {
            boolean found = false;
            for (int i = 0; i < CATEGORIES.length; i++) {
                if (CATEGORIES[i].equals(existing.getCategory())) {
                    categoryCombo.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                categoryCombo.setSelectedItem(existing.getCategory());
            }
        }
        addRow(panel, gbc, 1, "Category:", categoryCombo);

        // Width
        JTextField widthField = createField(isEdit ? String.valueOf(existing.getWidth()) : "1.0");
        addRow(panel, gbc, 2, "Width (m):", widthField);

        // Depth
        JTextField depthField = createField(isEdit ? String.valueOf(existing.getDepth()) : "1.0");
        addRow(panel, gbc, 3, "Depth (m):", depthField);

        // Height
        JTextField heightField = createField(isEdit ? String.valueOf(existing.getHeight()) : "1.0");
        addRow(panel, gbc, 4, "Height (m):", heightField);

        // Color
        Color initialColor = isEdit ? existing.getAwtColor() : Color.GRAY;
        JButton colorBtn = new JButton("   ");
        colorBtn.setBackground(initialColor);
        colorBtn.setPreferredSize(new Dimension(60, 30));
        colorBtn.setOpaque(true);
        colorBtn.setBorderPainted(true);
        final Color[] selectedColor = { initialColor };
        colorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(panel, "Choose Furniture Colour", selectedColor[0]);
            if (c != null) {
                selectedColor[0] = c;
                colorBtn.setBackground(c);
            }
        });

        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        colorPanel.setOpaque(false);
        colorPanel.add(colorBtn);
        JLabel colorHex = new JLabel("  Click to choose");
        colorHex.setFont(ModernUI.FONT_SMALL);
        colorHex.setForeground(ThemeManager.textMuted());
        colorPanel.add(colorHex);
        addRow(panel, gbc, 5, "Colour:", colorPanel);

        // Custom checkbox
        JCheckBox customCheckbox = new JCheckBox("Custom Made ★");
        customCheckbox.setFont(ModernUI.FONT_BODY);
        customCheckbox.setForeground(ThemeManager.textPrimary());
        customCheckbox.setOpaque(false);
        customCheckbox.setSelected(isEdit ? existing.isCustom() : false);
        customCheckbox.setToolTipText("Check for custom-made furniture, uncheck for shop items");
        addRow(panel, gbc, 6, "Type:", customCheckbox);

        // Preview info
        JLabel previewLabel = new JLabel(isEdit ?
                "Editing: " + existing.getName() + " (ID: " + existing.getId() + ")" :
                "Uncheck 'Custom Made' for shop items, check for custom furniture");
        previewLabel.setFont(ModernUI.FONT_TINY);
        previewLabel.setForeground(ThemeManager.textMuted());
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        panel.add(previewLabel, gbc);

        String title = isEdit ? "Edit Furniture Item" : "Add New Furniture Item";
        int result = JOptionPane.showConfirmDialog(this, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                ModernUI.showToast(this, "⚠ Name is required", ModernUI.WARNING);
                return null;
            }

            double width, depth, height;
            try {
                width = Double.parseDouble(widthField.getText().trim());
                depth = Double.parseDouble(depthField.getText().trim());
                height = Double.parseDouble(heightField.getText().trim());
                if (width <= 0 || depth <= 0 || height <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                ModernUI.showToast(this, "⚠ Dimensions must be positive numbers", ModernUI.WARNING);
                return null;
            }

            int catIdx = categoryCombo.getSelectedIndex();
            String category;
            if (catIdx >= 0 && catIdx < CATEGORIES.length) {
                category = CATEGORIES[catIdx];
            } else {
                // If it's a custom-typed category, grab the string text
                Object selected = categoryCombo.getSelectedItem();
                category = selected != null ? selected.toString().trim() : "CUSTOM";
                if (category.isEmpty()) category = "CUSTOM";
            }
            
            String colorHexStr = String.format("#%02X%02X%02X",
                    selectedColor[0].getRed(), selectedColor[0].getGreen(), selectedColor[0].getBlue());

            return new CatalogItem(name, category, width, depth, height, colorHexStr, customCheckbox.isSelected());
        }
        return null;
    }

    // ===========================
    // HELPER UI BUILDERS
    // ===========================

    private JTextField createField(String text) {
        JTextField field = new JTextField(text, 20);
        field.setFont(ModernUI.FONT_BODY);
        return field;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(ModernUI.FONT_BODY);
        lbl.setForeground(ThemeManager.textSecondary());
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    /** Primary action button with gradient */
    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hovering = false;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                Color c1 = hovering ? new Color(128, 120, 255) : new Color(108, 99, 255);
                Color c2 = hovering ? new Color(100, 80, 240) : new Color(80, 60, 220);
                GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(Color.WHITE);
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(155, 36));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Compact icon+label button */
    private JButton createIconButton(String icon, String label, Color accent) {
        JButton btn = new JButton(icon + " " + label) {
            private boolean hovering = false;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                Color bg = hovering
                        ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30)
                        : ThemeManager.isDark() ? new Color(50, 52, 65) : new Color(235, 237, 245);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                // Border
                g2.setColor(ThemeManager.isDark() ? new Color(70, 72, 88) : new Color(210, 212, 224));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.setColor(hovering ? accent : ThemeManager.textSecondary());
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
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setPreferredSize(new Dimension(100, 34));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ===========================
    // TABLE MODEL
    // ===========================

    private static class CatalogTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {
            "#", "Name", "Category", "Width", "Depth", "Height", "Colour", "Type", "Status"
        };
        private List<CatalogItem> items = new ArrayList<>();

        public void setItems(List<CatalogItem> items) {
            this.items = items;
            fireTableDataChanged();
        }

        public CatalogItem getItemAt(int row) { return items.get(row); }

        @Override public int getRowCount() { return items.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int col) { return COLUMNS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            CatalogItem item = items.get(row);
            switch (col) {
                case 0: return row + 1; // Show sequential row number instead of DB ID
                case 1: return item.getName();
                case 2: return formatCategory(item.getCategory());
                case 3: return String.format("%.2f m", item.getWidth());
                case 4: return String.format("%.2f m", item.getDepth());
                case 5: return String.format("%.2f m", item.getHeight());
                case 6: return item.getColor();
                case 7: return item.isCustom() ? "★ Custom" : "Shop";
                case 8: return item.isActive() ? "Active" : "Inactive";
                default: return "";
            }
        }

        private String formatCategory(String cat) {
            String[] parts = cat.toLowerCase().split("_");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1));
            }
            return sb.toString();
        }
    }

    // ===========================
    // CUSTOM CELL RENDERERS
    // ===========================

    /** Header — uppercase, theme-aware bg, subtle purple accent underline */
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, selected, focused, row, col);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(headerText());
            label.setBackground(headerBg());
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(108, 99, 255, 60)),
                    new EmptyBorder(0, 12, 0, 12)
            ));
            label.setOpaque(true);
            label.setHorizontalAlignment(
                    col == 0 || (col >= 3 && col <= 5) ? SwingConstants.CENTER : SwingConstants.LEFT);
            label.setText(value != null ? value.toString().toUpperCase() : "");
            return label;
        }
    }

    /** Base renderer with zebra-striping and consistent padding */
    private static class StripedCellRenderer extends DefaultTableCellRenderer {
        private final int alignment;

        public StripedCellRenderer(int alignment) {
            this.alignment = alignment;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, selected, focused, row, col);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setHorizontalAlignment(alignment);
            label.setBorder(new EmptyBorder(0, 12, 0, 12));
            label.setForeground(col == 0 ? idText() : ThemeManager.textPrimary());
            label.setBackground(selected ? rowSelected() : (row % 2 == 0 ? rowEven() : rowOdd()));
            label.setOpaque(true);
            return label;
        }
    }

    /** Name column — bold text */
    private static class NameCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, selected, focused, row, col);
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            label.setForeground(ThemeManager.textPrimary());
            label.setBorder(new EmptyBorder(0, 14, 0, 12));
            label.setBackground(selected ? rowSelected() : (row % 2 == 0 ? rowEven() : rowOdd()));
            label.setOpaque(true);
            return label;
        }
    }

    /** Colour column — rounded swatch + hex text */
    private static class ColorCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int col) {
            String hex = value != null ? value.toString() : "#808080";

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)) {
                @Override public Dimension getPreferredSize() {
                    return new Dimension(super.getPreferredSize().width, table.getRowHeight());
                }
            };
            panel.setBackground(selected ? rowSelected() : (row % 2 == 0 ? rowEven() : rowOdd()));
            panel.setBorder(new EmptyBorder(0, 8, 0, 8));

            // Rounded swatch
            JPanel swatch = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    ModernUI.enableAntiAliasing(g2);
                    try { g2.setColor(Color.decode(hex)); } catch (Exception e) { g2.setColor(Color.GRAY); }
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                    // Subtle border around swatch
                    g2.setColor(ThemeManager.isDark() ? new Color(255,255,255,30) : new Color(0,0,0,20));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 6, 6));
                    g2.dispose();
                }
            };
            swatch.setPreferredSize(new Dimension(24, 24));
            swatch.setOpaque(false);

            JLabel hexLabel = new JLabel(hex);
            hexLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
            hexLabel.setForeground(hexText());

            panel.add(swatch);
            panel.add(hexLabel);
            return panel;
        }
    }

    /** Pill-shaped badge for Type and Status columns — theme-aware colors */
    private static class BadgeCellRenderer extends DefaultTableCellRenderer {
        private final boolean isTypeColumn;

        public BadgeCellRenderer(boolean isTypeColumn) {
            this.isTypeColumn = isTypeColumn;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int col) {
            String text = value != null ? value.toString() : "";
            boolean dark = ThemeManager.isDark();

            // Pick pill colours — adjusted for both light and dark mode
            Color pillBg, pillFg;
            if (isTypeColumn) {
                if (text.contains("Custom")) {
                    pillBg = dark ? new Color(255, 200, 60, 30)  : new Color(255, 180, 0, 25);
                    pillFg = dark ? new Color(255, 210, 80)      : new Color(180, 120, 0);
                } else {
                    pillBg = dark ? new Color(100, 140, 200, 30) : new Color(80, 120, 200, 20);
                    pillFg = dark ? new Color(140, 170, 220)     : new Color(60, 100, 180);
                }
            } else {
                if ("Active".equals(text)) {
                    pillBg = dark ? new Color(80, 200, 120, 30)  : new Color(60, 180, 100, 20);
                    pillFg = dark ? new Color(80, 220, 120)      : new Color(30, 140, 60);
                } else {
                    pillBg = dark ? new Color(220, 80, 80, 30)   : new Color(220, 60, 60, 20);
                    pillFg = dark ? new Color(220, 100, 100)     : new Color(180, 50, 50);
                }
            }

            Color rowBg = selected ? rowSelected() : (row % 2 == 0 ? rowEven() : rowOdd());

            JPanel panel = new JPanel(new GridBagLayout()) {
                @Override public Dimension getPreferredSize() {
                    return new Dimension(super.getPreferredSize().width, table.getRowHeight());
                }
            };
            panel.setBackground(rowBg);

            JLabel pill = new JLabel(text) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    ModernUI.enableAntiAliasing(g2);
                    g2.setColor(pillBg);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                    // Subtle pill border
                    g2.setColor(new Color(pillFg.getRed(), pillFg.getGreen(), pillFg.getBlue(), 40));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pill.setFont(new Font("Segoe UI", Font.BOLD, 11));
            pill.setForeground(pillFg);
            pill.setHorizontalAlignment(SwingConstants.CENTER);
            pill.setOpaque(false);
            pill.setBorder(new EmptyBorder(4, 10, 4, 10));

            panel.add(pill);
            return panel;
        }
    }
}
