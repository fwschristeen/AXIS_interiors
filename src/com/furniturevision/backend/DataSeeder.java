package com.furniturevision.backend;

/**
 * Seeds the database with default designer accounts on first launch.
 * 
 * This ensures the application is usable immediately after installation.
 * The default credentials are:
 *   - admin / admin123
 *   - sarah / sarah123
 * 
 * Only seeds if the designers table is empty (first run).
 */
public class DataSeeder {

    private final DesignerDAO designerDAO;
    private final FurnitureCatalogDAO catalogDAO;

    public DataSeeder() {
        this.designerDAO = new DesignerDAO();
        this.catalogDAO = new FurnitureCatalogDAO();
    }

    /**
     * Seeds default data if tables are empty.
     */
    public void seedIfEmpty() {
        DatabaseManager db = DatabaseManager.getInstance();

        if (db.isTableEmpty("users")) {
            System.out.println("[DataSeeder] Seeding default designer accounts...");
            seedDefaultDesigners();
        } else {
            System.out.println("[DataSeeder] Database already has data, skipping seed.");
        }

        if (db.isTableEmpty("furniture_catalog")) {
            System.out.println("[DataSeeder] Seeding sample furniture catalog...");
            seedFurnitureCatalog();
        }
    }

    /**
     * Creates the default designer accounts.
     */
    private void seedDefaultDesigners() {
        // Admin account
        designerDAO.register(
            "admin",
            "admin123",
            "Admin Designer",
            "admin@axisinteriors.com",
            "admin"
        );

        // Sarah Mitchell - Senior Designer
        designerDAO.register(
            "sarah",
            "sarah123",
            "Sarah Mitchell",
            "sarah@axisinteriors.com",
            "designer"
        );

        System.out.println("[DataSeeder] Default accounts created:");
        System.out.println("  → admin / admin123  (Administrator)");
        System.out.println("  → sarah / sarah123  (Designer)");
    }

    /**
     * Seeds the furniture catalog with all shop defaults + sample custom items.
     * Shop defaults (is_custom = false): the 12 standard furniture types
     * Custom samples (is_custom = true): example admin-created items
     */
    private void seedFurnitureCatalog() {
        // ── Shop Defaults (is_custom = false) ──────────────────────────────
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "Dining Chair", "CHAIR", 0.5, 0.5, 0.9, "#8B5A2B", false));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "Dining Table", "DINING_TABLE", 1.6, 0.9, 0.75, "#A0522D", false));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "Side Table", "SIDE_TABLE", 0.5, 0.5, 0.6, "#D2691E", false));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "3-Seater Sofa", "SOFA", 2.2, 0.9, 0.85, "#708090", false));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "Coffee Table", "COFFEE_TABLE", 1.2, 0.6, 0.45, "#8B4513", false));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "Bookshelf", "BOOKSHELF", 0.8, 0.3, 1.8, "#DEB887", false));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "Double Bed", "BED", 1.6, 2.0, 0.5, "#B0C4DE", false));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "Wardrobe", "WARDROBE", 1.5, 0.6, 2.0, "#696969", false));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "Office Desk", "DESK", 1.4, 0.7, 0.75, "#D2B48C", false));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "Area Rug", "RUG", 2.0, 1.4, 0.02, "#B22222", false));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "Floor Lamp", "LAMP", 0.35, 0.35, 1.6, "#FFD700", false));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "TV Unit", "TV_UNIT", 1.8, 0.45, 0.55, "#2F4F4F", false));

        // ── Custom Samples (is_custom = true) ──────────────────────────────
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "Ottoman", "CHAIR", 0.6, 0.6, 0.4, "#8B6914", true));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "L-Shaped Sofa", "SOFA", 2.8, 1.8, 0.85, "#4A5568", true));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "Nightstand", "SIDE_TABLE", 0.45, 0.4, 0.55, "#A0522D", true));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "Plant Pot", "LAMP", 0.35, 0.35, 0.8, "#228B22", true));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "Display Cabinet", "BOOKSHELF", 1.0, 0.4, 1.5, "#DEB887", true));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "Round Mirror", "MIRROR", 0.6, 0.05, 0.6, "#C0C0C0", true));
        catalogDAO.addItem(new FurnitureCatalogDAO.CatalogItem(
                "Standing Mirror", "MIRROR", 0.5, 0.4, 1.6, "#8B5A2B", true));

        System.out.println("[DataSeeder] Furniture catalog seeded (12 shop + 7 custom items)");
    }
}
