package com.furniturevision.backend;

import com.furniturevision.model.Designer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for Designer (user) operations.
 * Handles all CRUD operations, authentication, and session management
 * for designers in the MySQL database.
 */
public class DesignerDAO {

    private final DatabaseManager dbManager;

    public DesignerDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    // =========================================================================
    //  AUTHENTICATION
    // =========================================================================

    /**
     * Authenticates a designer by username and password.
     * Logs the login attempt in the audit log.
     * 
     * @param username  the username
     * @param password  the plaintext password
     * @return the Designer object if authentication succeeds, null otherwise
     */
    public Designer authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = 1";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String storedSalt = rs.getString("password_salt");

                if (PasswordUtils.verifyPassword(password, storedHash, storedSalt)) {
                    Designer designer = mapResultSetToDesigner(rs);
                    
                    // Log successful login
                    logAudit(designer.getId(), "LOGIN_SUCCESS", "User logged in successfully");
                    
                    // Create a new session
                    createSession(designer.getId());
                    
                    return designer;
                }
            }

            // Log failed login attempt
            logAudit(null, "LOGIN_FAILED", "Failed login attempt for username: " + username);
            return null;

        } catch (SQLException e) {
            System.err.println("[DesignerDAO] Authentication error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Logs the designer out by ending their active session.
     */
    public void logout(String designerId) {
        String sql = "UPDATE sessions SET is_active = 0, logout_time = NOW() " +
                     "WHERE designer_id = ? AND is_active = 1";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, designerId);
            pstmt.executeUpdate();
            logAudit(designerId, "LOGOUT", "User logged out");
        } catch (SQLException e) {
            System.err.println("[DesignerDAO] Logout error: " + e.getMessage());
        }
    }

    // =========================================================================
    //  REGISTRATION
    // =========================================================================

    /**
     * Registers a new designer account.
     * 
     * @param username  desired username (must be unique)
     * @param password  plaintext password (will be hashed)
     * @param fullName  display name
     * @param email     email address (optional)
     * @param role      role string ("admin" or "designer")
     * @return the created Designer, or null if username already exists
     */
    public Designer register(String username, String password, String fullName, String email, String role) {
        // Check if username already taken
        if (findByUsername(username) != null) {
            System.err.println("[DesignerDAO] Username already exists: " + username);
            return null;
        }

        // Validate password strength
        String validationError = PasswordUtils.validatePasswordStrength(password);
        if (validationError != null) {
            System.err.println("[DesignerDAO] Weak password: " + validationError);
            return null;
        }

        // Hash the password with a fresh salt
        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hashPassword(password, salt);

        String sql = "INSERT INTO users (username, password_hash, password_salt, full_name, email, role) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hash);
            pstmt.setString(3, salt);
            pstmt.setString(4, fullName);
            pstmt.setString(5, email);
            pstmt.setString(6, role != null ? role : "designer");

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    String id = String.valueOf(keys.getInt(1));
                    Designer designer = new Designer(id, username, hash, fullName, email, role != null ? role : "designer");
                    logAudit(id, "REGISTER", "New designer registered: " + username);
                    System.out.println("[DesignerDAO] Registered new designer: " + username);
                    return designer;
                }
            }
        } catch (SQLException e) {
            System.err.println("[DesignerDAO] Registration error: " + e.getMessage());
        }
        return null;
    }

    // =========================================================================
    //  CRUD OPERATIONS
    // =========================================================================

    /**
     * Finds a designer by their username.
     */
    public Designer findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToDesigner(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DesignerDAO] findByUsername error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds a designer by their ID.
     */
    public Designer findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToDesigner(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DesignerDAO] findById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns all designers in the system.
     */
    public List<Designer> findAll() {
        List<Designer> designers = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY full_name";

        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                designers.add(mapResultSetToDesigner(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DesignerDAO] findAll error: " + e.getMessage());
        }
        return designers;
    }

    /**
     * Updates a designer's profile information.
     */
    public boolean updateProfile(String id, String fullName, String email) {
        String sql = "UPDATE users SET full_name = ?, email = ?, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, id);
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                logAudit(id, "PROFILE_UPDATE", "Profile updated for designer ID: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DesignerDAO] updateProfile error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Changes a designer's password.
     */
    public boolean changePassword(String id, String oldPassword, String newPassword) {
        // Verify old password first
        String sql = "SELECT password_hash, password_salt FROM users WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String storedSalt = rs.getString("password_salt");

                if (!PasswordUtils.verifyPassword(oldPassword, storedHash, storedSalt)) {
                    System.err.println("[DesignerDAO] Old password doesn't match.");
                    return false;
                }

                // Validate new password
                String validationError = PasswordUtils.validatePasswordStrength(newPassword);
                if (validationError != null) {
                    System.err.println("[DesignerDAO] " + validationError);
                    return false;
                }

                // Generate new salt and hash
                String newSalt = PasswordUtils.generateSalt();
                String newHash = PasswordUtils.hashPassword(newPassword, newSalt);

                String updateSql = "UPDATE users SET password_hash = ?, password_salt = ?, " +
                                   "updated_at = NOW() WHERE id = ?";
                try (PreparedStatement updateStmt = dbManager.getConnection().prepareStatement(updateSql)) {
                    updateStmt.setString(1, newHash);
                    updateStmt.setString(2, newSalt);
                    updateStmt.setString(3, id);
                    updateStmt.executeUpdate();
                    logAudit(id, "PASSWORD_CHANGE", "Password changed for designer ID: " + id);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[DesignerDAO] changePassword error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deactivates a designer account (soft delete).
     */
    public boolean deactivate(String id) {
        String sql = "UPDATE users SET is_active = 0, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, id);
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                logAudit(id, "ACCOUNT_DEACTIVATED", "Account deactivated for designer ID: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DesignerDAO] deactivate error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Reactivates a deactivated designer account.
     */
    public boolean reactivate(String id) {
        String sql = "UPDATE users SET is_active = 1, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, id);
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                logAudit(id, "ACCOUNT_REACTIVATED", "Account reactivated for designer ID: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DesignerDAO] reactivate error: " + e.getMessage());
        }
        return false;
    }

    // =========================================================================
    //  SESSION MANAGEMENT
    // =========================================================================

    /**
     * Creates a new login session for a designer.
     */
    private String createSession(String designerId) {
        String token = UUID.randomUUID().toString();
        String sql = "INSERT INTO sessions (designer_id, session_token) VALUES (?, ?)";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, designerId);
            pstmt.setString(2, token);
            pstmt.executeUpdate();
            return token;
        } catch (SQLException e) {
            System.err.println("[DesignerDAO] createSession error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validates if a session token is currently active.
     */
    public boolean isSessionActive(String token) {
        String sql = "SELECT COUNT(*) FROM sessions WHERE session_token = ? AND is_active = 1";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Gets the total number of active designers.
     */
    public int getActiveDesignerCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE is_active = 1";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    // =========================================================================
    //  AUDIT LOGGING
    // =========================================================================

    /**
     * Writes an entry to the audit log.
     */
    private void logAudit(String designerId, String action, String details) {
        String sql = "INSERT INTO audit_log (designer_id, action, details) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
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

    /**
     * Gets the recent audit log entries for a designer.
     */
    public List<String> getRecentAuditLog(String designerId, int limit) {
        List<String> logs = new ArrayList<>();
        String sql = "SELECT action, details, created_at FROM audit_log WHERE designer_id = ? " +
                     "ORDER BY created_at DESC LIMIT ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, designerId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                logs.add("[" + rs.getString("created_at") + "] " + rs.getString("action") + ": " + rs.getString("details"));
            }
        } catch (SQLException e) {
            System.err.println("[DesignerDAO] getRecentAuditLog error: " + e.getMessage());
        }
        return logs;
    }

    // =========================================================================
    //  HELPER METHODS
    // =========================================================================

    /**
     * Maps a database ResultSet row to a Designer model object.
     */
    private Designer mapResultSetToDesigner(ResultSet rs) throws SQLException {
        return new Designer(
            String.valueOf(rs.getInt("id")),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("role")
        );
    }
}
