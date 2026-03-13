package com.furniturevision.ui;

import com.furniturevision.model.Design;
import com.furniturevision.model.Room;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Panel for entering room specifications to create a new design.
 * Allows setting room dimensions, shape, and colour scheme.
 */
public class RoomSpecPanel extends JPanel {

    private DashboardFrame dashboard;

    private JTextField roomNameField;
    private JTextField customerNameField;
    private JSpinner widthSpinner;
    private JSpinner depthSpinner;
    private JSpinner heightSpinner;
    private JComboBox<Room.Shape> shapeCombo;
    private JButton wallColorBtn;
    private JButton floorColorBtn;
    private JTextArea notesArea;

    private Color selectedWallColor = new Color(230, 225, 215);
    private Color selectedFloorColor = new Color(180, 150, 120);

    public RoomSpecPanel(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        setBackground(ThemeManager.bgPrimary());
        setLayout(new GridBagLayout());
        initComponents();
    }

    private void initComponents() {
        // Main scrollable container
        JPanel formContainer = new JPanel();
        formContainer.setOpaque(false);
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBorder(new EmptyBorder(30, 40, 30, 40));
        formContainer.setMaximumSize(new Dimension(700, Integer.MAX_VALUE));

        // Header section
        JPanel headerCard = createCard();
        headerCard.setLayout(new BoxLayout(headerCard, BoxLayout.Y_AXIS));
        headerCard.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = ModernUI.createLabel("Create New Room Design", ModernUI.FONT_TITLE, ThemeManager.textPrimary());
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel descLabel = ModernUI.createLabel("Enter the room specifications to begin your design", ModernUI.FONT_BODY, ThemeManager.textSecondary());
        descLabel.setAlignmentX(LEFT_ALIGNMENT);

        headerCard.add(titleLabel);
        headerCard.add(Box.createVerticalStrut(8));
        headerCard.add(descLabel);

        // Customer info section
        JPanel customerCard = createCard();
        customerCard.setLayout(new BoxLayout(customerCard, BoxLayout.Y_AXIS));
        customerCard.setBorder(new EmptyBorder(24, 30, 24, 30));

        JLabel custSection = ModernUI.createLabel("Client Information", ModernUI.FONT_HEADING, ModernUI.PRIMARY_LIGHT);
        custSection.setAlignmentX(LEFT_ALIGNMENT);

        customerNameField = ModernUI.createTextField("Enter customer name");
        customerNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        customerNameField.setAlignmentX(LEFT_ALIGNMENT);

        customerCard.add(custSection);
        customerCard.add(Box.createVerticalStrut(12));
        customerCard.add(createFieldLabel("Customer Name"));
        customerCard.add(Box.createVerticalStrut(6));
        customerCard.add(customerNameField);

        // Room details section
        JPanel roomCard = createCard();
        roomCard.setLayout(new BoxLayout(roomCard, BoxLayout.Y_AXIS));
        roomCard.setBorder(new EmptyBorder(24, 30, 24, 30));

        JLabel roomSection = ModernUI.createLabel("Room Details", ModernUI.FONT_HEADING, ModernUI.PRIMARY_LIGHT);
        roomSection.setAlignmentX(LEFT_ALIGNMENT);

        roomNameField = ModernUI.createTextField("e.g., Living Room, Master Bedroom");
        roomNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        roomNameField.setAlignmentX(LEFT_ALIGNMENT);

        shapeCombo = ModernUI.createComboBox(Room.Shape.values());
        shapeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        shapeCombo.setAlignmentX(LEFT_ALIGNMENT);

        roomCard.add(roomSection);
        roomCard.add(Box.createVerticalStrut(12));
        roomCard.add(createFieldLabel("Room Name"));
        roomCard.add(Box.createVerticalStrut(6));
        roomCard.add(roomNameField);
        roomCard.add(Box.createVerticalStrut(16));
        roomCard.add(createFieldLabel("Room Shape"));
        roomCard.add(Box.createVerticalStrut(6));
        roomCard.add(shapeCombo);

        // Dimensions section
        JPanel dimCard = createCard();
        dimCard.setLayout(new BoxLayout(dimCard, BoxLayout.Y_AXIS));
        dimCard.setBorder(new EmptyBorder(24, 30, 24, 30));

        JLabel dimSection = ModernUI.createLabel("Dimensions (metres)", ModernUI.FONT_HEADING, ModernUI.PRIMARY_LIGHT);
        dimSection.setAlignmentX(LEFT_ALIGNMENT);

        // Spinners in a row
        JPanel spinnerRow = new JPanel(new GridLayout(1, 3, 15, 0));
        spinnerRow.setOpaque(false);
        spinnerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        spinnerRow.setAlignmentX(LEFT_ALIGNMENT);

        JPanel widthPanel = createDimensionPanel("Width", 5.0);
        JPanel depthPanel = createDimensionPanel("Depth", 4.0);
        JPanel heightPanel = createDimensionPanel("Height", 2.8);

        widthSpinner = (JSpinner) widthPanel.getClientProperty("spinner");
        depthSpinner = (JSpinner) depthPanel.getClientProperty("spinner");
        heightSpinner = (JSpinner) heightPanel.getClientProperty("spinner");

        spinnerRow.add(widthPanel);
        spinnerRow.add(depthPanel);
        spinnerRow.add(heightPanel);

        dimCard.add(dimSection);
        dimCard.add(Box.createVerticalStrut(12));
        dimCard.add(spinnerRow);

        // Colour scheme section
        JPanel colorCard = createCard();
        colorCard.setLayout(new BoxLayout(colorCard, BoxLayout.Y_AXIS));
        colorCard.setBorder(new EmptyBorder(24, 30, 24, 30));

        JLabel colorSection = ModernUI.createLabel("Colour Scheme", ModernUI.FONT_HEADING, ModernUI.PRIMARY_LIGHT);
        colorSection.setAlignmentX(LEFT_ALIGNMENT);

        JPanel colorRow = new JPanel(new GridLayout(1, 2, 15, 0));
        colorRow.setOpaque(false);
        colorRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        colorRow.setAlignmentX(LEFT_ALIGNMENT);

        wallColorBtn = createColorPickerButton("Wall Colour", selectedWallColor);
        floorColorBtn = createColorPickerButton("Floor Colour", selectedFloorColor);

        wallColorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose Wall Colour", selectedWallColor);
            if (c != null) {
                selectedWallColor = c;
                wallColorBtn.setBackground(c);
                wallColorBtn.repaint();
            }
        });

        floorColorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose Floor Colour", selectedFloorColor);
            if (c != null) {
                selectedFloorColor = c;
                floorColorBtn.setBackground(c);
                floorColorBtn.repaint();
            }
        });

        colorRow.add(wallColorBtn);
        colorRow.add(floorColorBtn);

        colorCard.add(colorSection);
        colorCard.add(Box.createVerticalStrut(12));
        colorCard.add(colorRow);

        // Notes section
        JPanel notesCard = createCard();
        notesCard.setLayout(new BoxLayout(notesCard, BoxLayout.Y_AXIS));
        notesCard.setBorder(new EmptyBorder(24, 30, 24, 30));

        JLabel notesSection = ModernUI.createLabel("Design Notes", ModernUI.FONT_HEADING, ModernUI.PRIMARY_LIGHT);
        notesSection.setAlignmentX(LEFT_ALIGNMENT);

        notesArea = new JTextArea(3, 20);
        notesArea.setFont(ModernUI.FONT_BODY);
        notesArea.setBackground(ThemeManager.bgTertiary());
        notesArea.setForeground(ThemeManager.textPrimary());
        notesArea.setCaretColor(ModernUI.PRIMARY);
        notesArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = ModernUI.createScrollPane(notesArea);
        notesScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        notesScroll.setAlignmentX(LEFT_ALIGNMENT);

        notesCard.add(notesSection);
        notesCard.add(Box.createVerticalStrut(12));
        notesCard.add(notesScroll);

        // Create button
        JButton createBtn = ModernUI.createButton("Create Design  →", ModernUI.PRIMARY);
        createBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        createBtn.setAlignmentX(LEFT_ALIGNMENT);
        createBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        createBtn.addActionListener(e -> createDesign());

        // Assemble all cards
        formContainer.add(headerCard);
        formContainer.add(Box.createVerticalStrut(20));
        formContainer.add(customerCard);
        formContainer.add(Box.createVerticalStrut(16));
        formContainer.add(roomCard);
        formContainer.add(Box.createVerticalStrut(16));
        formContainer.add(dimCard);
        formContainer.add(Box.createVerticalStrut(16));
        formContainer.add(colorCard);
        formContainer.add(Box.createVerticalStrut(16));
        formContainer.add(notesCard);
        formContainer.add(Box.createVerticalStrut(24));
        formContainer.add(createBtn);
        formContainer.add(Box.createVerticalStrut(30));

        JScrollPane scrollPane = ModernUI.createScrollPane(formContainer);
        scrollPane.getViewport().setBackground(ThemeManager.bgPrimary());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Center the form
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                g2.setColor(ThemeManager.bgSecondary());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(ThemeManager.border());
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = ModernUI.createLabel(text, ModernUI.FONT_SMALL, ThemeManager.textSecondary());
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JPanel createDimensionPanel(String label, double defaultValue) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lbl = ModernUI.createLabel(label, ModernUI.FONT_SMALL, ThemeManager.textSecondary());
        lbl.setAlignmentX(LEFT_ALIGNMENT);

        JSpinner spinner = ModernUI.createSpinner(defaultValue, 1.0, 30.0, 0.1);
        spinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        spinner.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(lbl);
        panel.add(Box.createVerticalStrut(6));
        panel.add(spinner);

        panel.putClientProperty("spinner", spinner);
        return panel;
    }

    private JButton createColorPickerButton(String label, Color initialColor) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);

                // Background
                g2.setColor(ThemeManager.bgTertiary());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

                // Color swatch
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(12, 12, 40, 40, 8, 8));

                // Border for swatch
                g2.setColor(ThemeManager.borderLight());
                g2.draw(new RoundRectangle2D.Float(12, 12, 40, 40, 8, 8));

                // Label
                g2.setColor(ThemeManager.textSecondary());
                g2.setFont(ModernUI.FONT_SMALL);
                g2.drawString(label, 64, 28);

                // Color hex
                Color bg = getBackground();
                String hex = String.format("#%02X%02X%02X", bg.getRed(), bg.getGreen(), bg.getBlue());
                g2.setColor(ThemeManager.textPrimary());
                g2.setFont(ModernUI.FONT_BODY);
                g2.drawString(hex, 64, 48);

                g2.dispose();
            }
        };
        btn.setBackground(initialColor);
        btn.setPreferredSize(new Dimension(200, 64));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Validates input and creates a new design.
     */
    private void createDesign() {
        String roomName = roomNameField.getText().trim();
        String customerName = customerNameField.getText().trim();

        if (roomName.isEmpty()) {
            ModernUI.showToast(this, "⚠ Please enter a room name", ModernUI.WARNING);
            roomNameField.requestFocusInWindow();
            return;
        }

        if (customerName.isEmpty()) {
            ModernUI.showToast(this, "⚠ Please enter a customer name", ModernUI.WARNING);
            customerNameField.requestFocusInWindow();
            return;
        }

        double width = (Double) widthSpinner.getValue();
        double depth = (Double) depthSpinner.getValue();
        double height = (Double) heightSpinner.getValue();
        Room.Shape shape = (Room.Shape) shapeCombo.getSelectedItem();

        Room room = new Room(roomName, width, depth, height, shape, selectedWallColor, selectedFloorColor);
        Design design = new Design(dashboard.getCurrentDesigner().getUsername(), customerName, room);
        design.setNotes(notesArea.getText().trim());

        ModernUI.showToast(this, "✓ Design created successfully!", ModernUI.SUCCESS);
        dashboard.createNewDesign(design);

        // Reset form
        roomNameField.setText("");
        customerNameField.setText("");
        notesArea.setText("");
    }
}
