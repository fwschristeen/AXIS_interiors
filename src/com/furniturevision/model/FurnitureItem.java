package com.furniturevision.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a furniture item that can be placed in a room design.
 * Stores position, dimensions, rotation, colour, and category info.
 */
public class FurnitureItem implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Category {
        CHAIR("Chair", "🪑"),
        DINING_TABLE("Dining Table", "🍽"),
        SIDE_TABLE("Side Table", "☕"),
        SOFA("Sofa", "🛋"),
        COFFEE_TABLE("Coffee Table", "◻"),
        BOOKSHELF("Bookshelf", "📚"),
        BED("Bed", "🛏"),
        WARDROBE("Wardrobe", "🚪"),
        DESK("Desk", "💼"),
        RUG("Rug", "▬"),
        LAMP("Lamp", "💡"),
        TV_UNIT("TV Unit", "📺"),
        MIRROR("Mirror", "🪞"),
        CUSTOM("Custom", "✨");

        private final String displayName;
        private final String icon;

        Category(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        @Override public String toString() { return displayName; }
    }

    private String id;
    private String name;
    private Category category;
    private String customCategoryName; // Used when category == CUSTOM
    private double width;      // metres
    private double depth;      // metres
    private double height;     // metres
    private double x;          // position in room (metres from left)
    private double y;          // position in room (metres from top)
    private double rotation;   // degrees (0, 90, 180, 270)
    private Color color;
    private double scale;
    private double shading;    // -1.0 (lighten) to +1.0 (darken), 0 = none
    private boolean selected;

    public FurnitureItem() {
        this.id = UUID.randomUUID().toString();
        this.scale = 1.0;
        this.shading = 0.0;
        this.rotation = 0;
        this.selected = false;
    }

    public FurnitureItem(String name, Category category, double width, double depth, double height, Color color) {
        this();
        this.name = name;
        this.category = category;
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.color = color;
    }

    /**
     * Creates a deep copy of this furniture item with a new ID.
     */
    public FurnitureItem copy() {
        FurnitureItem copy = new FurnitureItem(name, category, width, depth, height, color);
        copy.x = this.x;
        copy.y = this.y;
        copy.rotation = this.rotation;
        copy.scale = this.scale;
        copy.shading = this.shading;
        return copy;
    }

    // --- Getters and Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getCustomCategoryName() { return customCategoryName; }
    public void setCustomCategoryName(String customCategoryName) { this.customCategoryName = customCategoryName; }

    /**
     * Returns the custom category name if set, otherwise the enum display name.
     */
    public String getCategoryDisplayName() {
        if (category == Category.CUSTOM && customCategoryName != null && !customCategoryName.isEmpty()) {
            return customCategoryName;
        }
        return category.getDisplayName();
    }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }

    public double getDepth() { return depth; }
    public void setDepth(double depth) { this.depth = depth; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getRotation() { return rotation; }
    public void setRotation(double rotation) { this.rotation = rotation % 360; }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public double getScale() { return scale; }
    public void setScale(double scale) { this.scale = Math.max(0.1, Math.min(5.0, scale)); }

    public double getShading() { return shading; }
    public void setShading(double shading) { this.shading = Math.max(-1.0, Math.min(1.0, shading)); }

    /**
     * Returns the actual display color with shading applied.
     * Negative shading lightens the color, positive darkens it.
     */
    public Color getShadedColor() {
        if (shading == 0) return color;
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        if (shading > 0) {
            // Darken: blend toward black
            double f = 1.0 - shading;
            return new Color((int)(r * f), (int)(g * f), (int)(b * f));
        } else {
            // Lighten: blend toward white
            double f = -shading;
            return new Color((int)(r + (255 - r) * f), (int)(g + (255 - g) * f), (int)(b + (255 - b) * f));
        }
    }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    /**
     * Returns the scaled width.
     */
    public double getScaledWidth() { return width * scale; }

    /**
     * Returns the scaled depth.
     */
    public double getScaledDepth() { return depth * scale; }

    /**
     * Returns the scaled height.
     */
    public double getScaledHeight() { return height * scale; }

    /**
     * Checks if a point (in room coordinates) falls within this furniture item.
     */
    public boolean containsPoint(double px, double py) {
        double sw = getScaledWidth();
        double sd = getScaledDepth();
        return px >= x && px <= x + sw && py >= y && py <= y + sd;
    }

    // --- Static Factory Methods for default furniture ---
    public static FurnitureItem createChair() {
        return new FurnitureItem("Dining Chair", Category.CHAIR, 0.5, 0.5, 0.9, new Color(139, 90, 43));
    }

    public static FurnitureItem createDiningTable() {
        return new FurnitureItem("Dining Table", Category.DINING_TABLE, 1.6, 0.9, 0.76, new Color(160, 110, 60));
    }

    public static FurnitureItem createSideTable() {
        return new FurnitureItem("Side Table", Category.SIDE_TABLE, 0.5, 0.5, 0.55, new Color(180, 130, 80));
    }

    public static FurnitureItem createSofa() {
        return new FurnitureItem("3-Seater Sofa", Category.SOFA, 2.0, 0.85, 0.85, new Color(80, 80, 120));
    }

    public static FurnitureItem createCoffeeTable() {
        return new FurnitureItem("Coffee Table", Category.COFFEE_TABLE, 1.2, 0.6, 0.45, new Color(140, 100, 60));
    }

    public static FurnitureItem createBookshelf() {
        return new FurnitureItem("Bookshelf", Category.BOOKSHELF, 1.2, 0.35, 1.8, new Color(120, 80, 40));
    }

    public static FurnitureItem createBed() {
        return new FurnitureItem("Double Bed", Category.BED, 1.6, 2.0, 0.5, new Color(200, 180, 160));
    }

    public static FurnitureItem createWardrobe() {
        return new FurnitureItem("Wardrobe", Category.WARDROBE, 1.5, 0.6, 2.0, new Color(100, 70, 40));
    }

    public static FurnitureItem createDesk() {
        return new FurnitureItem("Office Desk", Category.DESK, 1.4, 0.7, 0.75, new Color(170, 140, 100));
    }

    public static FurnitureItem createRug() {
        return new FurnitureItem("Area Rug", Category.RUG, 2.0, 1.4, 0.02, new Color(150, 50, 50));
    }

    public static FurnitureItem createLamp() {
        return new FurnitureItem("Floor Lamp", Category.LAMP, 0.3, 0.3, 1.5, new Color(220, 200, 120));
    }

    public static FurnitureItem createTVUnit() {
        return new FurnitureItem("TV Unit", Category.TV_UNIT, 1.8, 0.45, 0.5, new Color(60, 60, 60));
    }

    @Override
    public String toString() {
        return name + " (" + category.getDisplayName() + ")";
    }
}
