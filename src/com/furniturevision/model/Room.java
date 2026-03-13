package com.furniturevision.model;

import java.awt.Color;
import java.io.Serializable;

/**
 * Represents a room with its physical specifications and colour scheme.
 */
public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Shape {
        RECTANGULAR("Rectangular"),
        L_SHAPED("L-Shaped"),
        SQUARE("Square"),
        OPEN_PLAN("Open Plan");

        private final String displayName;
        Shape(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        @Override public String toString() { return displayName; }
    }

    private String name;
    private double width;     // in metres
    private double depth;     // in metres
    private double height;    // in metres (wall height)
    private Shape shape;
    private Color wallColor;
    private Color floorColor;
    private Color ceilingColor;

    public Room() {
        this.name = "Untitled Room";
        this.width = 5.0;
        this.depth = 4.0;
        this.height = 2.8;
        this.shape = Shape.RECTANGULAR;
        this.wallColor = new Color(230, 225, 215);
        this.floorColor = new Color(180, 150, 120);
        this.ceilingColor = Color.WHITE;
    }

    public Room(String name, double width, double depth, double height, Shape shape,
                Color wallColor, Color floorColor) {
        this.name = name;
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.shape = shape;
        this.wallColor = wallColor;
        this.floorColor = floorColor;
        this.ceilingColor = Color.WHITE;
    }

    // --- Getters and Setters ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }

    public double getDepth() { return depth; }
    public void setDepth(double depth) { this.depth = depth; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public Shape getShape() { return shape; }
    public void setShape(Shape shape) { this.shape = shape; }

    public Color getWallColor() { return wallColor; }
    public void setWallColor(Color wallColor) { this.wallColor = wallColor; }

    public Color getFloorColor() { return floorColor; }
    public void setFloorColor(Color floorColor) { this.floorColor = floorColor; }

    public Color getCeilingColor() { return ceilingColor; }
    public void setCeilingColor(Color ceilingColor) { this.ceilingColor = ceilingColor; }

    /**
     * Returns the area of the room in square metres.
     */
    public double getArea() {
        return width * depth;
    }

    @Override
    public String toString() {
        return name + " (" + width + "m × " + depth + "m)";
    }
}
