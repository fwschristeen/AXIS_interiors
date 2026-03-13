package com.furniturevision.backend;

import com.furniturevision.model.Design;
import com.furniturevision.model.Designer;

import java.util.List;

/**
 * BackendService is the main facade for all backend operations.
 * 
 * All UI code should interact with BackendService rather than calling
 * DAOs directly. This provides a clean separation between the UI layer
 * and the data access layer.
 * 
 * Usage:
 *   BackendService backend = BackendService.getInstance();
 *   backend.initialize();   // call once at startup
 *   Designer d = backend.login("admin", "admin123");
 *   backend.saveDesign(design, d.getId());
 *   backend.shutdown();     // call on app exit
 */
public class BackendService {

    private static BackendService instance;

    private DatabaseManager dbManager;
    private DesignerDAO designerDAO;
    private DesignDAO designDAO;
    private FurnitureCatalogDAO catalogDAO;
    private DataSeeder dataSeeder;

    private Designer currentDesigner;  // Currently logged-in designer

    private BackendService() {}

    /**
     * Returns the singleton BackendService instance.
     */
    public static synchronized BackendService getInstance() {
        if (instance == null) {
            instance = new BackendService();
        }
        return instance;
    }

    // =========================================================================
    //  LIFECYCLE
    // =========================================================================

    /**
     * Initializes the backend: opens DB, creates tables, seeds default data.
     * This MUST be called once before any other backend operations.
     */
    public void initialize() {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║    AXIS Interiors Backend Initializing...    ║");
        System.out.println("╚══════════════════════════════════════════════╝");

        dbManager = DatabaseManager.getInstance();
        designerDAO = new DesignerDAO();
        designDAO = new DesignDAO();
        catalogDAO = new FurnitureCatalogDAO();
        dataSeeder = new DataSeeder();

        try {
            // Open database connection (creates tables if needed)
            dbManager.getConnection();
            System.out.println("[Backend] Database connected: " + dbManager.getDatabasePath());

            // Seed default data on first run
            dataSeeder.seedIfEmpty();

            System.out.println("[Backend] ✓ Initialization complete.");

        } catch (Exception e) {
            System.err.println("[Backend] ✗ Initialization FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Shuts down the backend: closes DB connection.
     * Call this when the application is exiting.
     */
    public void shutdown() {
        System.out.println("[Backend] Shutting down...");
        if (currentDesigner != null) {
            designerDAO.logout(currentDesigner.getId());
        }
        dbManager.close();
        System.out.println("[Backend] ✓ Shutdown complete.");
    }

    // =========================================================================
    //  AUTHENTICATION
    // =========================================================================

    /**
     * Authenticates a user and sets them as the current designer.
     * 
     * @param username the username
     * @param password the plaintext password
     * @return the Designer if login succeeded, null otherwise
     */
    public Designer login(String username, String password) {
        Designer designer = designerDAO.authenticate(username, password);
        if (designer != null) {
            this.currentDesigner = designer;
            System.out.println("[Backend] ✓ Login successful: " + designer.getFullName());
        } else {
            System.out.println("[Backend] ✗ Login failed for: " + username);
        }
        return designer;
    }

    /**
     * Logs out the current designer and ends their session.
     */
    public void logout() {
        if (currentDesigner != null) {
            designerDAO.logout(currentDesigner.getId());
            System.out.println("[Backend] ✓ Logged out: " + currentDesigner.getFullName());
            currentDesigner = null;
        }
    }

    /**
     * Registers a new designer account.
     */
    public Designer register(String username, String password, String fullName, String email) {
        return designerDAO.register(username, password, fullName, email, "designer");
    }

    /**
     * Gets the currently logged-in designer.
     */
    public Designer getCurrentDesigner() {
        return currentDesigner;
    }

    /**
     * Checks if a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return currentDesigner != null;
    }

    // =========================================================================
    //  DESIGNER OPERATIONS
    // =========================================================================

    /**
     * Updates the current designer's profile.
     */
    public boolean updateProfile(String fullName, String email) {
        if (currentDesigner == null) return false;
        boolean success = designerDAO.updateProfile(currentDesigner.getId(), fullName, email);
        if (success) {
            currentDesigner.setFullName(fullName);
            currentDesigner.setEmail(email);
        }
        return success;
    }

    /**
     * Changes the current designer's password.
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (currentDesigner == null) return false;
        return designerDAO.changePassword(currentDesigner.getId(), oldPassword, newPassword);
    }

    /**
     * Gets all designers in the system.
     */
    public List<Designer> getAllDesigners() {
        return designerDAO.findAll();
    }

    /**
     * Finds a designer by their username.
     */
    public Designer findDesignerByUsername(String username) {
        return designerDAO.findByUsername(username);
    }

    // =========================================================================
    //  DESIGN OPERATIONS
    // =========================================================================

    /**
     * Saves a design for the currently logged-in designer.
     */
    public boolean saveDesign(Design design) {
        if (currentDesigner == null) {
            System.err.println("[Backend] Cannot save design: no user logged in.");
            return false;
        }
        return designDAO.saveDesign(design, currentDesigner.getId());
    }

    /**
     * Saves a design with a specified designer ID.
     */
    public boolean saveDesign(Design design, String designerId) {
        return designDAO.saveDesign(design, designerId);
    }

    /**
     * Loads a specific design by its ID.
     */
    public Design loadDesign(String designId) {
        return designDAO.loadDesign(designId);
    }

    /**
     * Loads all designs for the currently logged-in designer.
     */
    public List<Design> loadMyDesigns() {
        if (currentDesigner == null) {
            return List.of();
        }
        return designDAO.loadAllDesigns(currentDesigner.getId());
    }

    /**
     * Loads all designs for a specific designer (by username).
     * Provided for backward compatibility with existing code.
     */
    public List<Design> loadDesignsByUsername(String username) {
        return designDAO.loadAllDesignsByUsername(username);
    }

    /**
     * Deletes a design by its ID.
     */
    public boolean deleteDesign(String designId) {
        return designDAO.deleteDesign(designId);
    }

    // =========================================================================
    //  STATISTICS
    // =========================================================================

    /**
     * Gets the design count for the current designer.
     */
    public int getMyDesignCount() {
        if (currentDesigner == null) return 0;
        return designDAO.getDesignCount(currentDesigner.getId());
    }

    /**
     * Gets the total furniture item count across all designs.
     */
    public int getMyFurnitureCount() {
        if (currentDesigner == null) return 0;
        return designDAO.getTotalFurnitureCount(currentDesigner.getId());
    }

    /**
     * Gets the total number of active designers.
     */
    public int getActiveDesignerCount() {
        return designerDAO.getActiveDesignerCount();
    }

    /**
     * Gets recent audit log entries for the current designer.
     */
    public List<String> getMyRecentActivity(int limit) {
        if (currentDesigner == null) return List.of();
        return designerDAO.getRecentAuditLog(currentDesigner.getId(), limit);
    }

    // =========================================================================
    //  PASSWORD VALIDATION (Static utility)
    // =========================================================================

    /**
     * Validates password strength for UI feedback.
     * Returns null if valid, error message if invalid.
     */
    public static String validatePassword(String password) {
        return PasswordUtils.validatePasswordStrength(password);
    }

    // =========================================================================
    //  FURNITURE CATALOG
    // =========================================================================

    /**
     * Gets the FurnitureCatalogDAO for direct access.
     */
    public FurnitureCatalogDAO getCatalogDAO() {
        return catalogDAO;
    }
}
