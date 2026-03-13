package com.furniturevision.backend;

import com.furniturevision.model.Design;
import com.furniturevision.model.FurnitureItem;
import com.furniturevision.model.Room;

import java.awt.Color;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object for Design operations.
 * Handles saving, loading, updating, and deleting room designs
 * along with their associated rooms and furniture items.
 * 
 * All operations are transactional — furniture items and room data
 * are saved/loaded atomically with the design.
 */
public class DesignDAO {

    private final DatabaseManager dbManager;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public DesignDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    // =========================================================================
    //  SAVE OPERATIONS
    // =========================================================================

    /**
     * Saves a complete design (room + furniture items) to the database.
     * Uses a transaction to ensure atomicity.
     * 
     * If the design already exists (by ID), it is updated.
     * Otherwise, a new design is inserted.
     * 
     * @param design      the design to save
     * @param designerId  the database ID of the designer who owns this design
     * @return true if saved successfully
     */
    public boolean saveDesign(Design design, String designerId) {
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false);  // BEGIN TRANSACTION

            // 1. Save or update the Room
            int roomId = saveRoom(conn, design.getRoom());
            if (roomId == -1) {
                conn.rollback();
                return false;
            }

            // 2. Check if design already exists
            boolean exists = designExists(conn, design.getId());

            if (exists) {
                // UPDATE existing design
                String sql = "UPDATE designs SET designer_id = ?, room_id = ?, customer_name = ?, " +
                             "notes = ?, modified_at = NOW() WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, designerId);
                    pstmt.setInt(2, roomId);
                    pstmt.setString(3, design.getCustomerName());
                    pstmt.setString(4, design.getNotes());
                    pstmt.setString(5, design.getId());
                    pstmt.executeUpdate();
                }

                // Delete old furniture items and re-insert
                deleteFurnitureItems(conn, design.getId());

            } else {
                // INSERT new design
                String sql = "INSERT INTO designs (id, designer_id, room_id, customer_name, notes) " +
                             "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, design.getId());
                    pstmt.setString(2, designerId);
                    pstmt.setInt(3, roomId);
                    pstmt.setString(4, design.getCustomerName());
                    pstmt.setString(5, design.getNotes());
                    pstmt.executeUpdate();
                }
            }

            // 3. Save all furniture items
            saveFurnitureItems(conn, design.getId(), design.getFurnitureItems());

            conn.commit();  // COMMIT TRANSACTION
            logAudit(conn, designerId, exists ? "DESIGN_UPDATED" : "DESIGN_CREATED",
                     "Design '" + design.getRoom().getName() + "' " + (exists ? "updated" : "created"));

            System.out.println("[DesignDAO] Design saved: " + design.getId());
            return true;

        } catch (SQLException e) {
            System.err.println("[DesignDAO] saveDesign error: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignore */ }
            }
        }
    }

    /**
     * Saves or updates a room in the database.
     * @return the room's auto-generated ID, or -1 on failure
     */
    private int saveRoom(Connection conn, Room room) throws SQLException {
        // Check if a room with the same name already exists for reuse
        String checkSql = "SELECT id FROM rooms WHERE name = ? AND width = ? AND depth = ? AND height = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, room.getName());
            pstmt.setDouble(2, room.getWidth());
            pstmt.setDouble(3, room.getDepth());
            pstmt.setDouble(4, room.getHeight());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int existingId = rs.getInt("id");
                // Update existing room's colors and shape
                updateRoom(conn, existingId, room);
                return existingId;
            }
        }

        // Insert new room
        String sql = "INSERT INTO rooms (name, width, depth, height, shape, wall_color, floor_color, ceiling_color) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, room.getName());
            pstmt.setDouble(2, room.getWidth());
            pstmt.setDouble(3, room.getDepth());
            pstmt.setDouble(4, room.getHeight());
            pstmt.setString(5, room.getShape().name());
            pstmt.setString(6, colorToHex(room.getWallColor()));
            pstmt.setString(7, colorToHex(room.getFloorColor()));
            pstmt.setString(8, colorToHex(room.getCeilingColor()));
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Updates an existing room's properties.
     */
    private void updateRoom(Connection conn, int roomId, Room room) throws SQLException {
        String sql = "UPDATE rooms SET shape = ?, wall_color = ?, floor_color = ?, ceiling_color = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, room.getShape().name());
            pstmt.setString(2, colorToHex(room.getWallColor()));
            pstmt.setString(3, colorToHex(room.getFloorColor()));
            pstmt.setString(4, colorToHex(room.getCeilingColor()));
            pstmt.setInt(5, roomId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Saves all furniture items for a design.
     */
    private void saveFurnitureItems(Connection conn, String designId, List<FurnitureItem> items)
            throws SQLException {
        String sql = "INSERT INTO placed_furniture (id, design_id, name, category, width, depth, height, " +
                     "x, y, rotation, scale_val, color) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (FurnitureItem item : items) {
                pstmt.setString(1, item.getId());
                pstmt.setString(2, designId);
                pstmt.setString(3, item.getName());
                pstmt.setString(4, item.getCategory().name());
                pstmt.setDouble(5, item.getWidth());
                pstmt.setDouble(6, item.getDepth());
                pstmt.setDouble(7, item.getHeight());
                pstmt.setDouble(8, item.getX());
                pstmt.setDouble(9, item.getY());
                pstmt.setDouble(10, item.getRotation());
                pstmt.setDouble(11, item.getScale());
                pstmt.setString(12, colorToHex(item.getColor()));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    // =========================================================================
    //  LOAD OPERATIONS
    // =========================================================================

    /**
     * Loads a complete design by its ID, including room and furniture items.
     */
    public Design loadDesign(String designId) {
        String sql = "SELECT d.*, r.name AS room_name, r.width AS room_width, r.depth AS room_depth, " +
                     "r.height AS room_height, r.shape, r.wall_color, r.floor_color, r.ceiling_color " +
                     "FROM designs d JOIN rooms r ON d.room_id = r.id WHERE d.id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, designId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Design design = mapResultSetToDesign(rs);
                design.setFurnitureItems(loadFurnitureItems(designId));
                return design;
            }
        } catch (SQLException e) {
            System.err.println("[DesignDAO] loadDesign error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Loads all designs belonging to a specific designer.
     * Uses the designer's database ID (not username) for the query.
     */
    public List<Design> loadAllDesigns(String designerId) {
        List<Design> designs = new ArrayList<>();
        String sql = "SELECT d.*, r.name AS room_name, r.width AS room_width, r.depth AS room_depth, " +
                     "r.height AS room_height, r.shape, r.wall_color, r.floor_color, r.ceiling_color " +
                     "FROM designs d JOIN rooms r ON d.room_id = r.id WHERE d.designer_id = ? " +
                     "ORDER BY d.modified_at DESC";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, designerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Design design = mapResultSetToDesign(rs);
                design.setFurnitureItems(loadFurnitureItems(design.getId()));
                designs.add(design);
            }
        } catch (SQLException e) {
            System.err.println("[DesignDAO] loadAllDesigns error: " + e.getMessage());
        }
        return designs;
    }

    /**
     * Loads all designs by the designer's username (for backward compatibility).
     */
    public List<Design> loadAllDesignsByUsername(String username) {
        List<Design> designs = new ArrayList<>();
        String sql = "SELECT d.*, r.name AS room_name, r.width AS room_width, r.depth AS room_depth, " +
                     "r.height AS room_height, r.shape, r.wall_color, r.floor_color, r.ceiling_color " +
                     "FROM designs d JOIN rooms r ON d.room_id = r.id " +
                     "JOIN users ds ON d.designer_id = ds.id " +
                     "WHERE ds.username = ? ORDER BY d.modified_at DESC";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Design design = mapResultSetToDesign(rs);
                design.setFurnitureItems(loadFurnitureItems(design.getId()));
                designs.add(design);
            }
        } catch (SQLException e) {
            System.err.println("[DesignDAO] loadAllDesignsByUsername error: " + e.getMessage());
        }
        return designs;
    }

    /**
     * Loads all furniture items for a given design.
     */
    private List<FurnitureItem> loadFurnitureItems(String designId) throws SQLException {
        List<FurnitureItem> items = new ArrayList<>();
        String sql = "SELECT * FROM placed_furniture WHERE design_id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, designId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                FurnitureItem item = new FurnitureItem(
                    rs.getString("name"),
                    FurnitureItem.Category.valueOf(rs.getString("category")),
                    rs.getDouble("width"),
                    rs.getDouble("depth"),
                    rs.getDouble("height"),
                    hexToColor(rs.getString("color"))
                );
                item.setId(rs.getString("id"));
                item.setX(rs.getDouble("x"));
                item.setY(rs.getDouble("y"));
                item.setRotation(rs.getDouble("rotation"));
                item.setScale(rs.getDouble("scale_val"));
                items.add(item);
            }
        }
        return items;
    }

    // =========================================================================
    //  DELETE OPERATIONS
    // =========================================================================

    /**
     * Deletes a design and all its associated furniture items.
     */
    public boolean deleteDesign(String designId) {
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false);

            // Delete furniture items first
            deleteFurnitureItems(conn, designId);

            // Delete the design
            String sql = "DELETE FROM designs WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, designId);
                pstmt.executeUpdate();
            }

            conn.commit();
            logAudit(conn, null, "DESIGN_DELETED", "Design deleted: " + designId);
            System.out.println("[DesignDAO] Design deleted: " + designId);
            return true;

        } catch (SQLException e) {
            System.err.println("[DesignDAO] deleteDesign error: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignore */ }
            }
        }
    }

    /**
     * Deletes all furniture items for a design.
     */
    private void deleteFurnitureItems(Connection conn, String designId) throws SQLException {
        String sql = "DELETE FROM placed_furniture WHERE design_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, designId);
            pstmt.executeUpdate();
        }
    }

    // =========================================================================
    //  STATISTICS
    // =========================================================================

    /**
     * Gets the total design count for a designer.
     */
    public int getDesignCount(String designerId) {
        String sql = "SELECT COUNT(*) FROM designs WHERE designer_id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, designerId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Gets the total furniture item count across all designs for a designer.
     */
    public int getTotalFurnitureCount(String designerId) {
        String sql = "SELECT COUNT(*) FROM placed_furniture fi " +
                     "JOIN designs d ON fi.design_id = d.id WHERE d.designer_id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, designerId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    // =========================================================================
    //  HELPER METHODS
    // =========================================================================

    /**
     * Checks if a design ID already exists in the database.
     */
    private boolean designExists(Connection conn, String designId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM designs WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, designId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /**
     * Maps a ResultSet row (with joined room data) to a Design object.
     */
    private Design mapResultSetToDesign(ResultSet rs) throws SQLException {
        Room room = new Room(
            rs.getString("room_name"),
            rs.getDouble("room_width"),
            rs.getDouble("room_depth"),
            rs.getDouble("room_height"),
            Room.Shape.valueOf(rs.getString("shape")),
            hexToColor(rs.getString("wall_color")),
            hexToColor(rs.getString("floor_color"))
        );
        room.setCeilingColor(hexToColor(rs.getString("ceiling_color")));

        Design design = new Design(rs.getString("designer_id"), rs.getString("customer_name"), room);
        design.setId(rs.getString("id"));
        design.setNotes(rs.getString("notes"));

        // Parse dates
        try {
            String created = rs.getString("created_at");
            String modified = rs.getString("modified_at");
            if (created != null) design.setCreatedDate(DATE_FORMAT.parse(created));
            if (modified != null) design.setModifiedDate(DATE_FORMAT.parse(modified));
        } catch (ParseException e) {
            // Use current date as fallback
            design.setCreatedDate(new Date());
            design.setModifiedDate(new Date());
        }

        return design;
    }

    /**
     * Converts a java.awt.Color to hex string (e.g., "#FF8800").
     */
    private String colorToHex(Color color) {
        if (color == null) return "#808080";
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Converts a hex string (e.g., "#FF8800") to a java.awt.Color.
     */
    private Color hexToColor(String hex) {
        if (hex == null || hex.isEmpty()) return Color.GRAY;
        try {
            return Color.decode(hex);
        } catch (NumberFormatException e) {
            return Color.GRAY;
        }
    }

    /**
     * Writes an audit log entry.
     */
    private void logAudit(Connection conn, String designerId, String action, String details) {
        String sql = "INSERT INTO audit_log (designer_id, action, details) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (designerId != null) {
                pstmt.setString(1, designerId);
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setString(2, action);
            pstmt.setString(3, details);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Audit] Error logging: " + e.getMessage());
        }
    }
}
