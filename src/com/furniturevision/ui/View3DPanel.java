package com.furniturevision.ui;

import com.furniturevision.model.Design;
import com.furniturevision.model.FurnitureItem;
import com.furniturevision.model.Room;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.util.Comparator;
import java.util.List;

/**
 * 3D Visualization panel using isometric projection.
 * Renders the room with realistic furniture shapes:
 *   - Sofas with cushions and armrests
 *   - Beds with headboards, mattress, and pillows
 *   - Tables with individual legs
 *   - Wardrobes/Bookshelves with doors/shelves
 *   - Lamps with stand and shade
 *   - etc.
 */
public class View3DPanel extends JPanel {

    private DashboardFrame dashboard;
    private Design design;

    // View parameters
    private double viewAngle = 30;
    private double rotationAngle = 45;
    private double scale3D = 60;
    private boolean showShading = true;
    private boolean showWireframe = false;
    private double ambientLight = 0.4;
    private double directionalLight = 0.6;

    // Pan offset
    private int offsetX = 0, offsetY = 0;
    private Point dragStart = null;
    private int dragOffsetX, dragOffsetY;

    public View3DPanel(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        setBackground(ThemeManager.bgPrimary());
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Controls toolbar
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

        JButton rotateLeftBtn = createToolBtn("◀ Rotate");
        JButton rotateRightBtn = createToolBtn("Rotate ▶");
        JButton tiltUpBtn = createToolBtn("Tilt Up");
        JButton tiltDownBtn = createToolBtn("Tilt Down");
        JButton zoomInBtn = createToolBtn("Zoom +");
        JButton zoomOutBtn = createToolBtn("Zoom -");
        JButton shadingBtn = createToolBtn("Shading");
        JButton wireframeBtn = createToolBtn("Wireframe");
        JButton resetBtn = createToolBtn("Reset View");

        JButton backTo2DBtn = ModernUI.createButton("← Back to 2D", ModernUI.PRIMARY);
        backTo2DBtn.setPreferredSize(new Dimension(120, 34));

        JButton realisticBtn = ModernUI.createButton("✨ Realistic Web 3D", ModernUI.ACCENT);
        realisticBtn.setPreferredSize(new Dimension(160, 34));


        rotateLeftBtn.addActionListener(e -> { rotationAngle -= 15; repaint(); });
        rotateRightBtn.addActionListener(e -> { rotationAngle += 15; repaint(); });
        tiltUpBtn.addActionListener(e -> { viewAngle = Math.min(60, viewAngle + 5); repaint(); });
        tiltDownBtn.addActionListener(e -> { viewAngle = Math.max(10, viewAngle - 5); repaint(); });
        zoomInBtn.addActionListener(e -> { scale3D = Math.min(120, scale3D + 10); repaint(); });
        zoomOutBtn.addActionListener(e -> { scale3D = Math.max(20, scale3D - 10); repaint(); });
        shadingBtn.addActionListener(e -> { showShading = !showShading; repaint(); });
        wireframeBtn.addActionListener(e -> { showWireframe = !showWireframe; repaint(); });
        resetBtn.addActionListener(e -> { rotationAngle = 45; viewAngle = 30; scale3D = 60; offsetX = 0; offsetY = 0; repaint(); });
        backTo2DBtn.addActionListener(e -> dashboard.showPanel("2dView", "2D Layout"));

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
                                ModernUI.showToast(View3DPanel.this, "Error opening browser.", ModernUI.ERROR);
                            }
                        } else {
                            ModernUI.showToast(View3DPanel.this, "Error saving design to MySQL.", ModernUI.ERROR);
                        }
                    });
                }).start();
            }
        });


        toolbar.add(rotateLeftBtn);
        toolbar.add(rotateRightBtn);
        toolbar.add(tiltUpBtn);
        toolbar.add(tiltDownBtn);
        toolbar.add(createSep());
        toolbar.add(zoomInBtn);
        toolbar.add(zoomOutBtn);
        toolbar.add(createSep());
        toolbar.add(shadingBtn);
        toolbar.add(wireframeBtn);
        toolbar.add(resetBtn);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(realisticBtn);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(backTo2DBtn);

        // 3D Canvas
        JPanel canvas3D = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                render3D(g2, getWidth(), getHeight());
                g2.dispose();
            }
        };
        canvas3D.setBackground(new Color(25, 25, 42));

        // Mouse interaction for panning
        canvas3D.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
                dragOffsetX = offsetX;
                dragOffsetY = offsetY;
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                dragStart = null;
            }
        });
        canvas3D.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    offsetX = dragOffsetX + (e.getX() - dragStart.x);
                    offsetY = dragOffsetY + (e.getY() - dragStart.y);
                    canvas3D.repaint();
                }
            }
        });
        canvas3D.addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) {
                scale3D = Math.min(120, scale3D + 5);
            } else {
                scale3D = Math.max(20, scale3D - 5);
            }
            canvas3D.repaint();
        });

        // Info panel at bottom
        JPanel infoPanel = new JPanel() {
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
        infoPanel.setPreferredSize(new Dimension(0, 30));
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setBorder(new EmptyBorder(0, 12, 0, 12));

        JLabel infoLabel = ModernUI.createLabel("Drag to pan | Scroll to zoom | Use toolbar to rotate", ModernUI.FONT_TINY, ThemeManager.textMuted());
        infoPanel.add(infoLabel, BorderLayout.WEST);

        add(toolbar, BorderLayout.NORTH);
        add(canvas3D, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }

    private JButton createToolBtn(String text) {
        JButton btn = new JButton(text) {
            private boolean hovering = false;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.enableAntiAliasing(g2);
                g2.setColor(hovering ? ThemeManager.surfaceHover() : ThemeManager.surface());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.setColor(ThemeManager.textSecondary());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
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
        btn.setFont(ModernUI.FONT_SMALL);
        btn.setPreferredSize(new Dimension(80, 34));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JSeparator createSep() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 30));
        sep.setForeground(ThemeManager.border());
        return sep;
    }

    public void setDesign(Design design) {
        this.design = design;
        repaint();
    }

    // ===========================
    // 3D RENDERING ENGINE
    // ===========================

    private void render3D(Graphics2D g2, int canvasW, int canvasH) {
        if (design == null) {
            // Friendly placeholder
            g2.setColor(ThemeManager.textMuted());
            g2.setFont(ModernUI.FONT_SUBTITLE);
            String msg = "Select a design to visualize in 3D";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (canvasW - fm.stringWidth(msg)) / 2, canvasH / 2);
            return;
        }

        // Background gradient
        GradientPaint bgGradient = new GradientPaint(0, 0, new Color(20, 20, 40),
                0, canvasH, new Color(35, 35, 60));
        g2.setPaint(bgGradient);
        g2.fillRect(0, 0, canvasW, canvasH);

        Room room = design.getRoom();
        double rW = room.getWidth();
        double rD = room.getDepth();
        double rH = room.getHeight();

        double cx = canvasW / 2.0 + offsetX;
        double cy = canvasH / 2.0 + offsetY + 50;

        double angleRad = Math.toRadians(rotationAngle);
        double tiltRad = Math.toRadians(viewAngle);

        double cosA = Math.cos(angleRad);
        double sinA = Math.sin(angleRad);
        double cosT = Math.cos(tiltRad);
        double sinT = Math.sin(tiltRad);

        // ---- Draw Floor ----
        int[][] floorPts = new int[4][2];
        projectPoint(0, 0, 0, cx, cy, cosA, sinA, cosT, sinT, floorPts[0]);
        projectPoint(rW, 0, 0, cx, cy, cosA, sinA, cosT, sinT, floorPts[1]);
        projectPoint(rW, 0, rD, cx, cy, cosA, sinA, cosT, sinT, floorPts[2]);
        projectPoint(0, 0, rD, cx, cy, cosA, sinA, cosT, sinT, floorPts[3]);

        GeneralPath floorPath = createPath(floorPts);
        g2.setColor(applyLighting(room.getFloorColor(), 0.8));
        g2.fill(floorPath);
        g2.setColor(room.getFloorColor().darker());
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(floorPath);

        // Floor grid
        g2.setColor(new Color(0, 0, 0, 30));
        g2.setStroke(new BasicStroke(0.5f));
        for (int i = 0; i <= (int) rW; i++) {
            int[] p1 = new int[2], p2 = new int[2];
            projectPoint(i, 0, 0, cx, cy, cosA, sinA, cosT, sinT, p1);
            projectPoint(i, 0, rD, cx, cy, cosA, sinA, cosT, sinT, p2);
            g2.drawLine(p1[0], p1[1], p2[0], p2[1]);
        }
        for (int i = 0; i <= (int) rD; i++) {
            int[] p1 = new int[2], p2 = new int[2];
            projectPoint(0, 0, i, cx, cy, cosA, sinA, cosT, sinT, p1);
            projectPoint(rW, 0, i, cx, cy, cosA, sinA, cosT, sinT, p2);
            g2.drawLine(p1[0], p1[1], p2[0], p2[1]);
        }

        // ---- Draw Back Walls ----
        Color wallCol = room.getWallColor();

        // Wall along X axis (at z=0)
        int[][] wallBack = new int[4][2];
        projectPoint(0, 0, 0, cx, cy, cosA, sinA, cosT, sinT, wallBack[0]);
        projectPoint(rW, 0, 0, cx, cy, cosA, sinA, cosT, sinT, wallBack[1]);
        projectPoint(rW, rH, 0, cx, cy, cosA, sinA, cosT, sinT, wallBack[2]);
        projectPoint(0, rH, 0, cx, cy, cosA, sinA, cosT, sinT, wallBack[3]);

        GeneralPath wallBackPath = createPath(wallBack);
        g2.setColor(applyLighting(wallCol, 0.7));
        g2.fill(wallBackPath);
        if (showWireframe) { g2.setColor(Color.DARK_GRAY); g2.setStroke(new BasicStroke(1f)); g2.draw(wallBackPath); }

        // Wall along Z axis (at x=0)
        int[][] wallLeft = new int[4][2];
        projectPoint(0, 0, 0, cx, cy, cosA, sinA, cosT, sinT, wallLeft[0]);
        projectPoint(0, 0, rD, cx, cy, cosA, sinA, cosT, sinT, wallLeft[1]);
        projectPoint(0, rH, rD, cx, cy, cosA, sinA, cosT, sinT, wallLeft[2]);
        projectPoint(0, rH, 0, cx, cy, cosA, sinA, cosT, sinT, wallLeft[3]);

        GeneralPath wallLeftPath = createPath(wallLeft);
        g2.setColor(applyLighting(wallCol, 0.55));
        g2.fill(wallLeftPath);
        if (showWireframe) { g2.setColor(Color.DARK_GRAY); g2.setStroke(new BasicStroke(1f)); g2.draw(wallLeftPath); }

        // ---- Draw Furniture (sorted by depth for painter's algorithm) ----
        // Items farther from camera are drawn first (back-to-front)
        List<FurnitureItem> items = design.getFurnitureItems();
        items.sort(Comparator.comparingDouble(item -> {
            // Calculate center point of item
            double centerX = item.getX() + item.getScaledWidth() / 2.0;
            double centerZ = item.getY() + item.getScaledDepth() / 2.0;
            // Project onto camera depth axis (rz = x*sinA + z*cosA)
            // Lower rz = farther from camera = draw first
            return centerX * sinA + centerZ * cosA;
        }));

        for (FurnitureItem item : items) {
            draw3DFurniture(g2, item, cx, cy, cosA, sinA, cosT, sinT);
        }

        // Vignette effect
        RadialGradientPaint vignette = new RadialGradientPaint(
                new Point((int) cx, (int) cy), Math.max(canvasW, canvasH) * 0.6f,
                new float[]{0f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 80)}
        );
        g2.setPaint(vignette);
        g2.fillRect(0, 0, canvasW, canvasH);

        // Info overlay
        drawInfoBox(g2, room, items.size(), canvasH);
    }

    /**
     * Draws the info overlay at the bottom-left.
     */
    private void drawInfoBox(Graphics2D g2, Room room, int itemCount, int canvasH) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(10, canvasH - 65, 220, 55, 8, 8);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(new Color(240, 240, 255));
        g2.drawString("Room: " + room.getName(), 18, canvasH - 46);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.setColor(new Color(200, 200, 220));
        g2.drawString(String.format("Dimensions: %.0fm x %.0fm x %.0fm",
                room.getWidth(), room.getDepth(), room.getHeight()), 18, canvasH - 30);
        g2.drawString("Items: " + itemCount, 18, canvasH - 16);
    }

    // ===========================
    // REALISTIC FURNITURE DRAWING
    // ===========================

    /**
     * Dispatches to the appropriate drawing method based on furniture category.
     */
    private void draw3DFurniture(Graphics2D g2, FurnitureItem item,
                                  double cx, double cy,
                                  double cosA, double sinA, double cosT, double sinT) {
        double fx = item.getX();
        double fy = item.getY();
        double fw = item.getScaledWidth();
        double fd = item.getScaledDepth();
        double fh = item.getScaledHeight();
        Color col = item.getShadedColor();

        switch (item.getCategory()) {
            case SOFA:
                drawSofa(g2, fx, fy, fw, fd, fh, col, cx, cy, cosA, sinA, cosT, sinT);
                break;
            case BED:
                drawBed(g2, fx, fy, fw, fd, fh, col, cx, cy, cosA, sinA, cosT, sinT);
                break;
            case CHAIR:
                drawChair(g2, fx, fy, fw, fd, fh, col, cx, cy, cosA, sinA, cosT, sinT);
                break;
            case DINING_TABLE:
            case COFFEE_TABLE:
            case SIDE_TABLE:
                drawTable(g2, fx, fy, fw, fd, fh, col, cx, cy, cosA, sinA, cosT, sinT, item.getCategory());
                break;
            case WARDROBE:
                drawWardrobe(g2, fx, fy, fw, fd, fh, col, cx, cy, cosA, sinA, cosT, sinT);
                break;
            case BOOKSHELF:
                drawBookshelf(g2, fx, fy, fw, fd, fh, col, cx, cy, cosA, sinA, cosT, sinT);
                break;
            case DESK:
                drawDesk(g2, fx, fy, fw, fd, fh, col, cx, cy, cosA, sinA, cosT, sinT);
                break;
            case LAMP:
                drawLamp(g2, fx, fy, fw, fd, fh, col, cx, cy, cosA, sinA, cosT, sinT);
                break;
            case TV_UNIT:
                drawTVUnit(g2, fx, fy, fw, fd, fh, col, cx, cy, cosA, sinA, cosT, sinT);
                break;
            case RUG:
                drawRug(g2, fx, fy, fw, fd, col, cx, cy, cosA, sinA, cosT, sinT);
                break;
            default:
                drawBox(g2, fx, fy, 0, fw, fd, fh, col, cx, cy, cosA, sinA, cosT, sinT);
                break;
        }

        // Selection indicator
        if (item.isSelected()) {
            drawSelectionGlow(g2, fx, fy, fw, fd, fh, cx, cy, cosA, sinA, cosT, sinT);
        }
    }

    // ---- SOFA ----
    private void drawSofa(Graphics2D g2, double fx, double fy, double fw, double fd, double fh,
                           Color col, double cx, double cy, double cosA, double sinA, double cosT, double sinT) {
        double seatH = fh * 0.4;   // seat height
        double backH = fh;          // full height (backrest)
        double armW = fw * 0.12;    // armrest width
        double backD = fd * 0.2;    // backrest depth
        double cushionGap = fw * 0.02;

        // 1. Base/seat  (full width, seat height)
        drawBox(g2, fx, fy, 0, fw, fd, seatH, col, cx, cy, cosA, sinA, cosT, sinT);

        // 2. Backrest (full width, behind the seat)
        Color backColor = darken(col, 0.85);
        drawBox(g2, fx, fy, seatH, fw, backD, backH - seatH, backColor, cx, cy, cosA, sinA, cosT, sinT);

        // 3. Left armrest
        Color armColor = darken(col, 0.9);
        drawBox(g2, fx, fy, seatH, armW, fd, fh * 0.3, armColor, cx, cy, cosA, sinA, cosT, sinT);

        // 4. Right armrest
        drawBox(g2, fx + fw - armW, fy, seatH, armW, fd, fh * 0.3, armColor, cx, cy, cosA, sinA, cosT, sinT);

        // 5. Seat cushions (2-3 cushions)
        int cushionCount = fw > 1.5 ? 3 : 2;
        double cushionW = (fw - armW * 2 - cushionGap * (cushionCount + 1)) / cushionCount;
        double cushionD = fd - backD - cushionGap * 2;
        double cushionH = fh * 0.08;
        Color cushionColor = brighten(col, 1.1);

        for (int i = 0; i < cushionCount; i++) {
            double cx0 = fx + armW + cushionGap + i * (cushionW + cushionGap);
            drawBox(g2, cx0, fy + backD + cushionGap, seatH,
                    cushionW, cushionD, cushionH, cushionColor, cx, cy, cosA, sinA, cosT, sinT);
        }

        // 6. Back cushions / pillows
        double pillowW = cushionW * 0.85;
        double pillowD = backD * 0.7;
        double pillowH = (backH - seatH) * 0.6;
        Color pillowColor = brighten(col, 1.15);
        for (int i = 0; i < cushionCount; i++) {
            double px = fx + armW + cushionGap + i * (cushionW + cushionGap) + (cushionW - pillowW) / 2;
            drawBox(g2, px, fy + cushionGap, seatH + cushionH,
                    pillowW, pillowD, pillowH, pillowColor, cx, cy, cosA, sinA, cosT, sinT);
        }
    }

    // ---- BED ----
    private void drawBed(Graphics2D g2, double fx, double fy, double fw, double fd, double fh,
                          Color col, double cx, double cy, double cosA, double sinA, double cosT, double sinT) {
        double frameH = fh * 0.3;
        double mattressH = fh * 0.25;
        double headboardH = fh;
        double headboardD = fd * 0.06;
        double footboardH = fh * 0.45;

        // 1. Bed frame
        Color frameColor = new Color(139, 90, 43); // wood brown
        drawBox(g2, fx, fy, 0, fw, fd, frameH, frameColor, cx, cy, cosA, sinA, cosT, sinT);

        // 2. Mattress
        Color mattressColor = new Color(240, 235, 220);
        drawBox(g2, fx + fw * 0.03, fy + headboardD + 0.05, frameH,
                fw * 0.94, fd * 0.88, mattressH, mattressColor, cx, cy, cosA, sinA, cosT, sinT);

        // 3. Headboard
        drawBox(g2, fx, fy, 0, fw, headboardD, headboardH, darken(frameColor, 0.8), cx, cy, cosA, sinA, cosT, sinT);

        // 4. Footboard
        drawBox(g2, fx, fy + fd - headboardD, 0, fw, headboardD, footboardH, darken(frameColor, 0.85), cx, cy, cosA, sinA, cosT, sinT);

        // 5. Pillow(s)
        Color pillowColor = col;
        double pillowW = fw * 0.4;
        double pillowD = fd * 0.15;
        double pillowH = mattressH * 0.5;
        double bedTop = frameH + mattressH;
        // Left pillow
        drawBox(g2, fx + fw * 0.06, fy + headboardD + 0.08, bedTop,
                pillowW, pillowD, pillowH, pillowColor, cx, cy, cosA, sinA, cosT, sinT);
        // Right pillow
        drawBox(g2, fx + fw * 0.54, fy + headboardD + 0.08, bedTop,
                pillowW, pillowD, pillowH, pillowColor, cx, cy, cosA, sinA, cosT, sinT);

        // 6. Blanket / sheet
        Color sheetColor = brighten(col, 1.05);
        drawBox(g2, fx + fw * 0.05, fy + headboardD + pillowD + 0.15, bedTop,
                fw * 0.9, fd * 0.55, fh * 0.04, sheetColor, cx, cy, cosA, sinA, cosT, sinT);
    }

    // ---- CHAIR ----
    private void drawChair(Graphics2D g2, double fx, double fy, double fw, double fd, double fh,
                            Color col, double cx, double cy, double cosA, double sinA, double cosT, double sinT) {
        double seatH = fh * 0.45;
        double legW = fw * 0.08;
        double legD = fd * 0.08;
        Color legColor = darken(col, 0.7);

        // 4 legs
        drawBox(g2, fx, fy, 0, legW, legD, seatH, legColor, cx, cy, cosA, sinA, cosT, sinT);
        drawBox(g2, fx + fw - legW, fy, 0, legW, legD, seatH, legColor, cx, cy, cosA, sinA, cosT, sinT);
        drawBox(g2, fx, fy + fd - legD, 0, legW, legD, seatH, legColor, cx, cy, cosA, sinA, cosT, sinT);
        drawBox(g2, fx + fw - legW, fy + fd - legD, 0, legW, legD, seatH, legColor, cx, cy, cosA, sinA, cosT, sinT);

        // Seat
        drawBox(g2, fx, fy, seatH, fw, fd, fh * 0.08, col, cx, cy, cosA, sinA, cosT, sinT);

        // Backrest
        Color backColor = darken(col, 0.9);
        drawBox(g2, fx, fy, seatH + fh * 0.08, fw, fd * 0.1, fh * 0.47, backColor, cx, cy, cosA, sinA, cosT, sinT);

        // Seat cushion
        Color cushionColor = brighten(col, 1.1);
        drawBox(g2, fx + fw * 0.08, fy + fd * 0.12, seatH + fh * 0.08,
                fw * 0.84, fd * 0.8, fh * 0.06, cushionColor, cx, cy, cosA, sinA, cosT, sinT);
    }

    // ---- TABLE ----
    private void drawTable(Graphics2D g2, double fx, double fy, double fw, double fd, double fh,
                            Color col, double cx, double cy, double cosA, double sinA, double cosT, double sinT,
                            FurnitureItem.Category type) {
        double topH = fh * 0.06;
        double legH = fh - topH;
        double legW, legD;

        Color legColor = darken(col, 0.7);

        if (type == FurnitureItem.Category.SIDE_TABLE) {
            // Round-ish side table with central pedestal
            double pedestalW = fw * 0.15;
            double pedestalD = fd * 0.15;
            // Central pedestal
            drawBox(g2, fx + fw * 0.42, fy + fd * 0.42, 0,
                    pedestalW, pedestalD, legH, legColor, cx, cy, cosA, sinA, cosT, sinT);
            // Base
            drawBox(g2, fx + fw * 0.2, fy + fd * 0.2, 0,
                    fw * 0.6, fd * 0.6, fh * 0.04, legColor, cx, cy, cosA, sinA, cosT, sinT);
        } else {
            // Regular 4-leg table
            legW = fw * 0.06;
            legD = fd * 0.06;
            drawBox(g2, fx + fw * 0.04, fy + fd * 0.04, 0, legW, legD, legH, legColor, cx, cy, cosA, sinA, cosT, sinT);
            drawBox(g2, fx + fw * 0.9, fy + fd * 0.04, 0, legW, legD, legH, legColor, cx, cy, cosA, sinA, cosT, sinT);
            drawBox(g2, fx + fw * 0.04, fy + fd * 0.9, 0, legW, legD, legH, legColor, cx, cy, cosA, sinA, cosT, sinT);
            drawBox(g2, fx + fw * 0.9, fy + fd * 0.9, 0, legW, legD, legH, legColor, cx, cy, cosA, sinA, cosT, sinT);
        }

        // Table top surface
        drawBox(g2, fx, fy, legH, fw, fd, topH, col, cx, cy, cosA, sinA, cosT, sinT);
    }

    // ---- WARDROBE ----
    private void drawWardrobe(Graphics2D g2, double fx, double fy, double fw, double fd, double fh,
                               Color col, double cx, double cy, double cosA, double sinA, double cosT, double sinT) {
        // Main body
        drawBox(g2, fx, fy, 0, fw, fd, fh, col, cx, cy, cosA, sinA, cosT, sinT);

        // Door panels (front face detail) - two doors
        double doorGap = fw * 0.02;
        double doorW = (fw - doorGap * 3) / 2;
        double doorH = fh * 0.88;
        double doorInset = fd * 0.02;
        Color doorColor = darken(col, 0.88);

        // Left door panel
        drawBox(g2, fx + doorGap, fy + fd - doorInset, fh * 0.06,
                doorW, doorInset * 1.5, doorH, doorColor, cx, cy, cosA, sinA, cosT, sinT);
        // Right door panel
        drawBox(g2, fx + doorGap * 2 + doorW, fy + fd - doorInset, fh * 0.06,
                doorW, doorInset * 1.5, doorH, doorColor, cx, cy, cosA, sinA, cosT, sinT);

        // Door handles (small knobs)
        Color handleColor = new Color(180, 160, 120);
        double handleH = fh * 0.03;
        double handleW = fw * 0.02;
        drawBox(g2, fx + doorGap + doorW - handleW * 2, fy + fd, fh * 0.48,
                handleW, fd * 0.03, handleH, handleColor, cx, cy, cosA, sinA, cosT, sinT);
        drawBox(g2, fx + doorGap * 2 + doorW + handleW, fy + fd, fh * 0.48,
                handleW, fd * 0.03, handleH, handleColor, cx, cy, cosA, sinA, cosT, sinT);

        // Top trim
        Color trimColor = darken(col, 0.75);
        drawBox(g2, fx - fw * 0.01, fy - fd * 0.01, fh,
                fw * 1.02, fd * 1.02, fh * 0.03, trimColor, cx, cy, cosA, sinA, cosT, sinT);

        // Base
        drawBox(g2, fx - fw * 0.005, fy - fd * 0.005, 0,
                fw * 1.01, fd * 1.01, fh * 0.04, trimColor, cx, cy, cosA, sinA, cosT, sinT);
    }

    // ---- BOOKSHELF ----
    private void drawBookshelf(Graphics2D g2, double fx, double fy, double fw, double fd, double fh,
                                Color col, double cx, double cy, double cosA, double sinA, double cosT, double sinT) {
        Color woodColor = col;
        double sideW = fw * 0.06;
        double shelfH = fh * 0.02;

        // Left side panel
        drawBox(g2, fx, fy, 0, sideW, fd, fh, woodColor, cx, cy, cosA, sinA, cosT, sinT);
        // Right side panel
        drawBox(g2, fx + fw - sideW, fy, 0, sideW, fd, fh, woodColor, cx, cy, cosA, sinA, cosT, sinT);
        // Back panel
        drawBox(g2, fx + sideW, fy, 0, fw - sideW * 2, fd * 0.08, fh, darken(woodColor, 0.8), cx, cy, cosA, sinA, cosT, sinT);

        // Shelves (4 shelves including top and bottom)
        int shelfCount = 5;
        double innerW = fw - sideW * 2;
        for (int i = 0; i < shelfCount; i++) {
            double shelvY = (fh / (shelfCount - 1)) * i;
            drawBox(g2, fx + sideW, fy, shelvY,
                    innerW, fd, shelfH, woodColor, cx, cy, cosA, sinA, cosT, sinT);
        }

        // Books on shelves (colorful rectangles)
        Color[] bookColors = {
            new Color(180, 40, 40), new Color(40, 100, 170), new Color(50, 140, 60),
            new Color(170, 120, 30), new Color(130, 50, 130), new Color(200, 80, 50)
        };
        double bookD = fd * 0.75;
        for (int shelf = 0; shelf < shelfCount - 1; shelf++) {
            double baseH = (fh / (shelfCount - 1)) * shelf + shelfH;
            double slotH = (fh / (shelfCount - 1)) - shelfH;
            double bx = fx + sideW + innerW * 0.04;
            int bookCount = 4 + (shelf % 2);
            double bookW = (innerW * 0.85) / bookCount;
            for (int b = 0; b < bookCount; b++) {
                double bHeight = slotH * (0.7 + Math.random() * 0.25);
                Color bCol = bookColors[(shelf * 3 + b) % bookColors.length];
                drawBox(g2, bx, fy + fd * 0.1, baseH,
                        bookW * 0.85, bookD, bHeight, bCol, cx, cy, cosA, sinA, cosT, sinT);
                bx += bookW;
            }
        }
    }

    // ---- DESK ----
    private void drawDesk(Graphics2D g2, double fx, double fy, double fw, double fd, double fh,
                           Color col, double cx, double cy, double cosA, double sinA, double cosT, double sinT) {
        double topH = fh * 0.05;
        double legH = fh - topH;
        double legW = fw * 0.06;
        double legD = fd * 0.06;
        Color legColor = darken(col, 0.7);

        // 4 legs
        drawBox(g2, fx + fw * 0.02, fy + fd * 0.02, 0, legW, legD, legH, legColor, cx, cy, cosA, sinA, cosT, sinT);
        drawBox(g2, fx + fw * 0.92, fy + fd * 0.02, 0, legW, legD, legH, legColor, cx, cy, cosA, sinA, cosT, sinT);
        drawBox(g2, fx + fw * 0.02, fy + fd * 0.92, 0, legW, legD, legH, legColor, cx, cy, cosA, sinA, cosT, sinT);
        drawBox(g2, fx + fw * 0.92, fy + fd * 0.92, 0, legW, legD, legH, legColor, cx, cy, cosA, sinA, cosT, sinT);

        // Desktop surface
        drawBox(g2, fx, fy, legH, fw, fd, topH, col, cx, cy, cosA, sinA, cosT, sinT);

        // Drawer unit on right side
        Color drawerColor = darken(col, 0.9);
        double drawerW = fw * 0.35;
        double drawerD = fd * 0.85;
        double drawerH = legH * 0.55;
        drawBox(g2, fx + fw * 0.6, fy + fd * 0.07, 0,
                drawerW, drawerD, drawerH, drawerColor, cx, cy, cosA, sinA, cosT, sinT);

        // Drawer lines (3 drawers)
        Color handleColor = new Color(180, 160, 130);
        for (int i = 0; i < 3; i++) {
            double dh = drawerH / 3;
            drawBox(g2, fx + fw * 0.72, fy + fd * 0.92, dh * i + dh * 0.4,
                    fw * 0.02, fd * 0.04, dh * 0.15, handleColor, cx, cy, cosA, sinA, cosT, sinT);
        }
    }

    // ---- LAMP ----
    private void drawLamp(Graphics2D g2, double fx, double fy, double fw, double fd, double fh,
                           Color col, double cx, double cy, double cosA, double sinA, double cosT, double sinT) {
        double centerX = fx + fw * 0.35;
        double centerZ = fy + fd * 0.35;

        // Base (circular platform)
        Color baseColor = new Color(80, 80, 90);
        double baseSize = fw * 0.5;
        drawBox(g2, centerX - baseSize * 0.1, centerZ - baseSize * 0.1, 0,
                baseSize, baseSize, fh * 0.03, baseColor, cx, cy, cosA, sinA, cosT, sinT);

        // Pole/Stand
        Color poleColor = new Color(160, 155, 145);
        double poleW = fw * 0.06;
        double poleH = fh * 0.65;
        drawBox(g2, centerX + baseSize * 0.18, centerZ + baseSize * 0.18, fh * 0.03,
                poleW, poleW, poleH, poleColor, cx, cy, cosA, sinA, cosT, sinT);

        // Lamp shade (wider truncated cone approximated as a box)
        double shadeW = fw * 0.7;
        double shadeH = fh * 0.3;
        double shadeBottom = fh * 0.03 + poleH;
        Color shadeColor = col;
        drawBox(g2, fx + fw * 0.1, fy + fd * 0.1, shadeBottom,
                shadeW, shadeW, shadeH, shadeColor, cx, cy, cosA, sinA, cosT, sinT);

        // Light glow effect
        int[] bulbPos = new int[2];
        projectPoint(centerX + fw * 0.2, shadeBottom + shadeH * 0.5, centerZ + fd * 0.2,
                cx, cy, cosA, sinA, cosT, sinT, bulbPos);
        RadialGradientPaint glow = new RadialGradientPaint(
                new Point(bulbPos[0], bulbPos[1]), (float)(scale3D * 0.5),
                new float[]{0f, 1f},
                new Color[]{new Color(255, 240, 180, 40), new Color(255, 240, 180, 0)}
        );
        g2.setPaint(glow);
        g2.fillOval(bulbPos[0] - (int)(scale3D * 0.5), bulbPos[1] - (int)(scale3D * 0.5),
                (int)(scale3D), (int)(scale3D));
    }

    // ---- TV UNIT ----
    private void drawTVUnit(Graphics2D g2, double fx, double fy, double fw, double fd, double fh,
                             Color col, double cx, double cy, double cosA, double sinA, double cosT, double sinT) {
        // Cabinet base
        drawBox(g2, fx, fy, 0, fw, fd, fh * 0.55, col, cx, cy, cosA, sinA, cosT, sinT);

        // Cabinet doors (2 sections)
        Color doorColor = darken(col, 0.88);
        double doorW = fw * 0.45;
        drawBox(g2, fx + fw * 0.03, fy + fd - fd * 0.03, fh * 0.05,
                doorW, fd * 0.04, fh * 0.45, doorColor, cx, cy, cosA, sinA, cosT, sinT);
        drawBox(g2, fx + fw * 0.52, fy + fd - fd * 0.03, fh * 0.05,
                doorW, fd * 0.04, fh * 0.45, doorColor, cx, cy, cosA, sinA, cosT, sinT);

        // TV screen (thin panel on top)
        double tvW = fw * 0.85;
        double tvH = fh * 0.45;
        double tvD = fd * 0.06;
        Color tvColor = new Color(30, 30, 35);
        drawBox(g2, fx + (fw - tvW) / 2, fy + fd * 0.3, fh * 0.55,
                tvW, tvD, tvH, tvColor, cx, cy, cosA, sinA, cosT, sinT);

        // Screen surface (slightly brighter)
        Color screenColor = new Color(50, 60, 70);
        drawBox(g2, fx + (fw - tvW) / 2 + tvW * 0.03, fy + fd * 0.3 + tvD, fh * 0.58,
                tvW * 0.94, tvD * 0.2, tvH * 0.9, screenColor, cx, cy, cosA, sinA, cosT, sinT);
    }

    // ---- RUG (flat on floor) ----
    private void drawRug(Graphics2D g2, double fx, double fy, double fw, double fd,
                          Color col, double cx, double cy, double cosA, double sinA, double cosT, double sinT) {
        // Very thin box on the floor
        drawBox(g2, fx, fy, 0, fw, fd, 0.02, col, cx, cy, cosA, sinA, cosT, sinT);

        // Border pattern
        Color borderColor = darken(col, 0.7);
        double border = 0.08;
        drawBox(g2, fx, fy, 0.02, fw, border, 0.005, borderColor, cx, cy, cosA, sinA, cosT, sinT);
        drawBox(g2, fx, fy + fd - border, 0.02, fw, border, 0.005, borderColor, cx, cy, cosA, sinA, cosT, sinT);
        drawBox(g2, fx, fy, 0.02, border, fd, 0.005, borderColor, cx, cy, cosA, sinA, cosT, sinT);
        drawBox(g2, fx + fw - border, fy, 0.02, border, fd, 0.005, borderColor, cx, cy, cosA, sinA, cosT, sinT);

        // Inner pattern
        Color innerColor = brighten(col, 1.15);
        drawBox(g2, fx + fw * 0.25, fy + fd * 0.25, 0.025,
                fw * 0.5, fd * 0.5, 0.005, innerColor, cx, cy, cosA, sinA, cosT, sinT);
    }

    // ===========================
    // GENERIC BOX HELPER
    // ===========================

    /**
     * Draws a 3D box at any position. This is the core building block.
     * @param baseY is the Y (height) start coordinate in 3D space
     */
    private void drawBox(Graphics2D g2, double fx, double fz, double baseY,
                          double fw, double fd, double fh, Color col,
                          double cx, double cy,
                          double cosA, double sinA, double cosT, double sinT) {
        int[][] c = new int[8][2];
        projectPoint(fx, baseY, fz, cx, cy, cosA, sinA, cosT, sinT, c[0]);
        projectPoint(fx + fw, baseY, fz, cx, cy, cosA, sinA, cosT, sinT, c[1]);
        projectPoint(fx + fw, baseY, fz + fd, cx, cy, cosA, sinA, cosT, sinT, c[2]);
        projectPoint(fx, baseY, fz + fd, cx, cy, cosA, sinA, cosT, sinT, c[3]);
        projectPoint(fx, baseY + fh, fz, cx, cy, cosA, sinA, cosT, sinT, c[4]);
        projectPoint(fx + fw, baseY + fh, fz, cx, cy, cosA, sinA, cosT, sinT, c[5]);
        projectPoint(fx + fw, baseY + fh, fz + fd, cx, cy, cosA, sinA, cosT, sinT, c[6]);
        projectPoint(fx, baseY + fh, fz + fd, cx, cy, cosA, sinA, cosT, sinT, c[7]);

        // Only draw faces visible to the camera (proper face culling)
        // Camera looks from +rz direction. A face is visible when its
        // outward normal has a positive projection onto the camera axis.
        //
        // Face normals projected onto rz (= x*sinA + z*cosA):
        //   Back  (+Z normal) : cosA   -> visible when cosA > 0
        //   Front (-Z normal) : -cosA  -> visible when cosA < 0
        //   Right (+X normal) : sinA   -> visible when sinA > 0
        //   Left  (-X normal) : -sinA  -> visible when sinA < 0
        //   Top   (+Y normal) : always visible from above

        // Draw the two far faces first (they're behind), then top last
        // Z-axis pair: only one is visible
        if (cosA > 0) {
            // Back face visible (facing camera)
            drawFace(g2, c, new int[]{3, 2, 6, 7}, applyLighting(col, 0.55));
        } else {
            // Front face visible
            drawFace(g2, c, new int[]{0, 1, 5, 4}, applyLighting(col, 0.7));
        }

        // X-axis pair: only one is visible
        if (sinA > 0) {
            // Right face visible
            drawFace(g2, c, new int[]{1, 2, 6, 5}, applyLighting(col, 0.65));
        } else {
            // Left face visible
            drawFace(g2, c, new int[]{0, 3, 7, 4}, applyLighting(col, 0.6));
        }

        // Top face — always visible from above
        drawFace(g2, c, new int[]{4, 5, 6, 7}, applyLighting(col, 0.9));
    }

    /**
     * Draws a selection glow.
     */
    private void drawSelectionGlow(Graphics2D g2, double fx, double fy, double fw, double fd, double fh,
                                    double cx, double cy, double cosA, double sinA, double cosT, double sinT) {
        int[][] c = new int[4][2];
        projectPoint(fx, fh, fy, cx, cy, cosA, sinA, cosT, sinT, c[0]);
        projectPoint(fx + fw, fh, fy, cx, cy, cosA, sinA, cosT, sinT, c[1]);
        projectPoint(fx + fw, fh, fy + fd, cx, cy, cosA, sinA, cosT, sinT, c[2]);
        projectPoint(fx, fh, fy + fd, cx, cy, cosA, sinA, cosT, sinT, c[3]);

        g2.setColor(new Color(108, 99, 255, 100));
        g2.setStroke(new BasicStroke(3f));
        GeneralPath outline = createPath(c);
        g2.draw(outline);
        g2.setStroke(new BasicStroke(1f));
    }

    // ===========================
    // PROJECTION & HELPERS
    // ===========================

    private void projectPoint(double x, double y, double z,
                               double cx, double cy,
                               double cosA, double sinA, double cosT, double sinT,
                               int[] result) {
        double rx = x * cosA - z * sinA;
        double rz = x * sinA + z * cosA;
        double sx = rx * scale3D;
        double sy = -y * scale3D * cosT + rz * scale3D * sinT;
        result[0] = (int) (cx + sx);
        result[1] = (int) (cy + sy);
    }

    private void drawFace(Graphics2D g2, int[][] corners, int[] indices, Color color) {
        GeneralPath path = new GeneralPath();
        path.moveTo(corners[indices[0]][0], corners[indices[0]][1]);
        for (int i = 1; i < indices.length; i++) {
            path.lineTo(corners[indices[i]][0], corners[indices[i]][1]);
        }
        path.closePath();

        g2.setColor(color);
        g2.fill(path);

        // Subtle edge
        g2.setColor(new Color(0, 0, 0, 30));
        g2.setStroke(new BasicStroke(0.5f));
        g2.draw(path);
    }

    private GeneralPath createPath(int[][] points) {
        GeneralPath path = new GeneralPath();
        path.moveTo(points[0][0], points[0][1]);
        for (int i = 1; i < points.length; i++) {
            path.lineTo(points[i][0], points[i][1]);
        }
        path.closePath();
        return path;
    }

    private Color applyLighting(Color c, double brightness) {
        if (!showShading) return c;
        double b = ambientLight + directionalLight * brightness;
        b = Math.max(0, Math.min(1, b));
        return new Color(
                (int) Math.min(255, c.getRed() * b),
                (int) Math.min(255, c.getGreen() * b),
                (int) Math.min(255, c.getBlue() * b)
        );
    }

    private Color darken(Color c, double factor) {
        return new Color(
                Math.max(0, (int)(c.getRed() * factor)),
                Math.max(0, (int)(c.getGreen() * factor)),
                Math.max(0, (int)(c.getBlue() * factor))
        );
    }

    private Color brighten(Color c, double factor) {
        return new Color(
                Math.min(255, (int)(c.getRed() * factor)),
                Math.min(255, (int)(c.getGreen() * factor)),
                Math.min(255, (int)(c.getBlue() * factor))
        );
    }
}
