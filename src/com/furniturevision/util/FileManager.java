package com.furniturevision.util;

import com.furniturevision.backend.BackendService;
import com.furniturevision.model.Design;
import com.furniturevision.model.Designer;

import java.io.*;
import java.util.List;

/**
 * FileManager now acts as a bridge between the old file-based code and
 * the new SQLite database backend.
 * 
 * All methods delegate to BackendService for database operations.
 * The old file I/O code has been removed; data is now stored in SQLite.
 * 
 * Existing code that calls FileManager.authenticate(), FileManager.saveDesign(), etc.
 * will continue to work without any modification.
 */
public class FileManager {

    private static final String APP_DIR = System.getProperty("user.home") + File.separator + ".furniturevision";
    private static final String DESIGNS_DIR = APP_DIR + File.separator + "designs";

    static {
        // Keep the directories for any legacy files
        new File(DESIGNS_DIR).mkdirs();
    }

    // ===========================
    // Design Operations
    // ===========================

    /**
     * Saves a design to the database.
     */
    public static boolean saveDesign(Design design) {
        BackendService backend = BackendService.getInstance();
        if (backend.isLoggedIn()) {
            design.touch();
            return backend.saveDesign(design);
        }
        // Fallback: save using designer username from the design itself
        Designer designer = backend.findDesignerByUsername(design.getDesignerUsername());
        if (designer != null) {
            design.touch();
            return backend.saveDesign(design, designer.getId());
        }
        System.err.println("[FileManager] Cannot save design: no authenticated user found.");
        return false;
    }

    /**
     * Loads a design from the database.
     */
    public static Design loadDesign(String designId) {
        return BackendService.getInstance().loadDesign(designId);
    }

    /**
     * Loads all designs for a specific designer (by username).
     */
    public static List<Design> loadAllDesigns(String designerUsername) {
        if (designerUsername == null) {
            return BackendService.getInstance().loadMyDesigns();
        }
        return BackendService.getInstance().loadDesignsByUsername(designerUsername);
    }

    /**
     * Deletes a design from the database.
     */
    public static boolean deleteDesign(String designId) {
        return BackendService.getInstance().deleteDesign(designId);
    }

    // ===========================
    // Designer Operations
    // ===========================

    /**
     * Loads all designers from the database.
     */
    public static List<Designer> loadDesigners() {
        return BackendService.getInstance().getAllDesigners();
    }

    /**
     * Saves all designers — this is now a no-op since the DB handles persistence.
     * Kept for backward compatibility.
     */
    public static boolean saveDesigners(List<Designer> designers) {
        // No-op: designers are persisted automatically in the database
        System.out.println("[FileManager] saveDesigners() called — DB handles this automatically.");
        return true;
    }

    /**
     * Authenticates a designer by username and password.
     * Delegates to BackendService which uses secure SHA-256 hashing.
     */
    public static Designer authenticate(String username, String password) {
        return BackendService.getInstance().login(username, password);
    }
}
