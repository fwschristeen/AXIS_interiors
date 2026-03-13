package com.furniturevision.backend;

import java.awt.Color;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the furniture catalog.
 * Manages the custom furniture definitions that admins can
 * create, edit, and delete through the Admin Panel.
 * 
 * Each catalog entry defines a furniture template:
 *   - name, category, dimensions (width/depth/height), color
 * 
 * These templates appear in the "Add Furniture" dropdown
 * alongside the built-in furniture types.
 */
public class FurnitureCatalogDAO {

    private final DatabaseManager dbManager;

    public FurnitureCatalogDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    // =========================================================================
    //  MODEL — simple inner class for catalog entries
    // =========================================================================

    /**
     * Represents a single furniture catalog entry.
     */
    public static class CatalogItem {
        private int id;
        private String name;
        private String category;
        private double width;
        private double depth;
        private double height;
        private String color;
        private boolean custom;   // true = admin-created, false = shop default
        private boolean active;
        private String createdAt;
        private String updatedAt;

        public CatalogItem() {}

        /** Creates a shop (non-custom) item */
        public CatalogItem(String name, String category, double width, double depth,
                           double height, String color) {
            this(name, category, width, depth, height, color, false);
        }

        /** Creates an item with explicit custom flag */
        public CatalogItem(String name, String category, double width, double depth,
                           double height, String color, boolean custom) {
            this.name = name;
            this.category = category;
            this.width = width;
            this.depth = depth;
            this.height = height;
            this.color = color;
            this.custom = custom;
            this.active = true;
        }

        // Getters & Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public double getWidth() { return width; }
        public void setWidth(double width) { this.width = width; }
        public double getDepth() { return depth; }
        public void setDepth(double depth) { this.depth = depth; }
        public double getHeight() { return height; }
        public void setHeight(double height) { this.height = height; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public boolean isCustom() { return custom; }
        public void setCustom(boolean custom) { this.custom = custom; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

        /**
         * Converts the color hex string to a java.awt.Color.
         */
        public Color getAwtColor() {
            try {
                return Color.decode(color);
            } catch (Exception e) {
                return Color.GRAY;
            }
        }

        @Override
        public String toString() {
            return name + " (" + category + ")";
        }
    }

    // =========================================================================
    //  CRUD OPERATIONS
    // =========================================================================

    /**
     * Adds a new furniture item to the catalog.
     * @return the generated ID, or -1 on failure
     */
    public int addItem(CatalogItem item) {
        String sql = "INSERT INTO furniture_catalog (name, category, width, depth, height, color, is_custom) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getCategory());
            pstmt.setDouble(3, item.getWidth());
            pstmt.setDouble(4, item.getDepth());
            pstmt.setDouble(5, item.getHeight());
            pstmt.setString(6, item.getColor());
            pstmt.setInt(7, item.isCustom() ? 1 : 0);
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                System.out.println("[CatalogDAO] Added: " + item.getName() + " (ID: " + id + ")");
                return id;
            }
        } catch (SQLException e) {
            System.err.println("[CatalogDAO] addItem error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates an existing catalog item.
     */
    public boolean updateItem(CatalogItem item) {
        String sql = "UPDATE furniture_catalog SET name = ?, category = ?, width = ?, depth = ?, " +
                     "height = ?, color = ? WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getCategory());
            pstmt.setDouble(3, item.getWidth());
            pstmt.setDouble(4, item.getDepth());
            pstmt.setDouble(5, item.getHeight());
            pstmt.setString(6, item.getColor());
            pstmt.setInt(7, item.getId());
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                System.out.println("[CatalogDAO] Updated: " + item.getName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[CatalogDAO] updateItem error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a catalog item by ID.
     */
    public boolean deleteItem(int id) {
        String sql = "DELETE FROM furniture_catalog WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                System.out.println("[CatalogDAO] Deleted ID: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[CatalogDAO] deleteItem error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Toggles the active status of a catalog item.
     */
    public boolean toggleActive(int id, boolean active) {
        String sql = "UPDATE furniture_catalog SET is_active = ? WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, active ? 1 : 0);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CatalogDAO] toggleActive error: " + e.getMessage());
        }
        return false;
    }

    // =========================================================================
    //  QUERIES
    // =========================================================================

    /**
     * Gets all catalog items (including inactive — for admin view).
     */
    public List<CatalogItem> getAllItems() {
        return query("SELECT * FROM furniture_catalog ORDER BY category, name");
    }

    /**
     * Gets only active catalog items (for the Add Furniture menu).
     */
    public List<CatalogItem> getActiveItems() {
        return query("SELECT * FROM furniture_catalog WHERE is_active = 1 ORDER BY category, name");
    }

    /**
     * Gets catalog items filtered by category.
     */
    public List<CatalogItem> getItemsByCategory(String category) {
        List<CatalogItem> items = new ArrayList<>();
        String sql = "SELECT * FROM furniture_catalog WHERE category = ? AND is_active = 1 ORDER BY name";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[CatalogDAO] getItemsByCategory error: " + e.getMessage());
        }
        return items;
    }

    /**
     * Gets a single catalog item by ID.
     */
    public CatalogItem getItem(int id) {
        String sql = "SELECT * FROM furniture_catalog WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[CatalogDAO] getItem error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets the total count of catalog items.
     */
    public int getItemCount() {
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM furniture_catalog")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Returns all distinct categories in the catalog.
     */
    public List<String> getCategories() {
        List<String> cats = new ArrayList<>();
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT category FROM furniture_catalog ORDER BY category")) {
            while (rs.next()) {
                cats.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            System.err.println("[CatalogDAO] getCategories error: " + e.getMessage());
        }
        return cats;
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private List<CatalogItem> query(String sql) {
        List<CatalogItem> items = new ArrayList<>();
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[CatalogDAO] query error: " + e.getMessage());
        }
        return items;
    }

    private CatalogItem mapRow(ResultSet rs) throws SQLException {
        CatalogItem item = new CatalogItem();
        item.setId(rs.getInt("id"));
        item.setName(rs.getString("name"));
        item.setCategory(rs.getString("category"));
        item.setWidth(rs.getDouble("width"));
        item.setDepth(rs.getDouble("depth"));
        item.setHeight(rs.getDouble("height"));
        item.setColor(rs.getString("color"));
        item.setCustom(rs.getInt("is_custom") == 1);
        item.setActive(rs.getInt("is_active") == 1);
        item.setCreatedAt(rs.getString("created_at"));
        item.setUpdatedAt(rs.getString("updated_at"));
        return item;
    }
}
