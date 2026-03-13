package com.furniturevision.backend;

import java.sql.*;

/**
 * DatabaseManager handles the MySQL database connection, initialization,
 * and schema management for the AXIS Interiors application.
 * 
 * Uses a singleton pattern to ensure a single database connection throughout
 * the application lifecycle.
 * 
 * Database: MySQL  |  Schema: furniturevision
 * 
 * IMPORTANT: MySQL must be running before launching the application.
 * Default connection: root@localhost:3306/furniturevision
 */
public class DatabaseManager {

    // ===== MySQL Connection Settings =====
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "furniturevision";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    // URL without database name (for initial database creation)
    private static final String DB_URL_NO_DB = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static DatabaseManager instance;
    private Connection connection;

    /**
     * Private constructor — use getInstance().
     */
    private DatabaseManager() {}

    /**
     * Returns the singleton instance of DatabaseManager.
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Opens a connection to the MySQL database.
     * Creates the database and tables if they don't exist.
     */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC driver not found. Ensure mysql-connector-j is on the classpath.", e);
            }

            // First, create the database if it doesn't exist
            createDatabaseIfNotExists();

            // Now connect to the actual database
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Initialize tables if first run
            initializeSchema();
        }
        return connection;
    }

    /**
     * Creates the 'furniturevision' database if it doesn't already exist.
     */
    private void createDatabaseIfNotExists() {
        try (Connection conn = DriverManager.getConnection(DB_URL_NO_DB, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            System.out.println("[DB] Database '" + DB_NAME + "' ensured.");
        } catch (SQLException e) {
            System.err.println("[DB] Error creating database: " + e.getMessage());
        }
    }

    /**
     * Creates all necessary database tables if they don't exist.
     */
    private void initializeSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            // ==============================
            // 1. USERS TABLE
            // ==============================
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "    id            INT AUTO_INCREMENT PRIMARY KEY," +
                "    username      VARCHAR(50)  NOT NULL UNIQUE," +
                "    password_hash VARCHAR(255) NOT NULL," +
                "    password_salt VARCHAR(255) NOT NULL," +
                "    full_name     VARCHAR(100) NOT NULL," +
                "    email         VARCHAR(100)," +
                "    role          VARCHAR(20)  NOT NULL DEFAULT 'designer'," +
                "    is_active     TINYINT      NOT NULL DEFAULT 1," +
                "    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            // ==============================
            // 2. ROOMS TABLE
            // ==============================
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS rooms (" +
                "    id            INT AUTO_INCREMENT PRIMARY KEY," +
                "    name          VARCHAR(100) NOT NULL," +
                "    width         DOUBLE       NOT NULL DEFAULT 5.0," +
                "    depth         DOUBLE       NOT NULL DEFAULT 4.0," +
                "    height        DOUBLE       NOT NULL DEFAULT 2.8," +
                "    shape         VARCHAR(30)  NOT NULL DEFAULT 'RECTANGULAR'," +
                "    wall_color    VARCHAR(10)  NOT NULL DEFAULT '#E6E1D7'," +
                "    floor_color   VARCHAR(10)  NOT NULL DEFAULT '#B49678'," +
                "    ceiling_color VARCHAR(10)  NOT NULL DEFAULT '#FFFFFF'," +
                "    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            // ==============================
            // 3. DESIGNS TABLE
            // ==============================
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS designs (" +
                "    id              VARCHAR(50) PRIMARY KEY," +
                "    designer_id     INT          NOT NULL," +
                "    room_id         INT          NOT NULL," +
                "    customer_name   VARCHAR(100)," +
                "    notes           TEXT," +
                "    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    modified_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "    FOREIGN KEY (designer_id) REFERENCES users(id) ON DELETE CASCADE,"  +
                "    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            // ==============================
            // 4. FURNITURE ITEMS TABLE
            // ==============================
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS placed_furniture (" +
                "    id          VARCHAR(50) PRIMARY KEY," +
                "    design_id   VARCHAR(50) NOT NULL," +
                "    name        VARCHAR(100) NOT NULL," +
                "    category    VARCHAR(30)  NOT NULL," +
                "    width       DOUBLE       NOT NULL," +
                "    depth       DOUBLE       NOT NULL," +
                "    height      DOUBLE       NOT NULL," +
                "    x           DOUBLE       NOT NULL DEFAULT 0," +
                "    y           DOUBLE       NOT NULL DEFAULT 0," +
                "    rotation    DOUBLE       NOT NULL DEFAULT 0," +
                "    scale_val   DOUBLE       NOT NULL DEFAULT 1.0," +
                "    color       VARCHAR(10)  NOT NULL DEFAULT '#808080'," +
                "    FOREIGN KEY (design_id) REFERENCES designs(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            // ==============================
            // 5. LOGIN SESSIONS TABLE
            // ==============================
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS sessions (" +
                "    id              INT AUTO_INCREMENT PRIMARY KEY," +
                "    designer_id     INT          NOT NULL," +
                "    session_token   VARCHAR(50)  NOT NULL UNIQUE," +
                "    login_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    logout_time     DATETIME," +
                "    is_active       TINYINT      NOT NULL DEFAULT 1," +
                "    FOREIGN KEY (designer_id) REFERENCES users(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            // ==============================
            // 6. AUDIT LOG TABLE
            // ==============================
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS audit_log (" +
                "    id          INT AUTO_INCREMENT PRIMARY KEY," +
                "    designer_id INT," +
                "    action      VARCHAR(50)  NOT NULL," +
                "    details     TEXT," +
                "    ip_address  VARCHAR(45)," +
                "    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    FOREIGN KEY (designer_id) REFERENCES users(id) ON DELETE SET NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            // ==============================
            // 7. FURNITURE CATALOG TABLE
            // ==============================
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS furniture_catalog (" +
                "    id          INT AUTO_INCREMENT PRIMARY KEY," +
                "    name        VARCHAR(100) NOT NULL," +
                "    category    VARCHAR(30)  NOT NULL," +
                "    width       DOUBLE       NOT NULL DEFAULT 1.0," +
                "    depth       DOUBLE       NOT NULL DEFAULT 1.0," +
                "    height      DOUBLE       NOT NULL DEFAULT 1.0," +
                "    color       VARCHAR(10)  NOT NULL DEFAULT '#808080'," +
                "    is_custom   TINYINT      NOT NULL DEFAULT 0," +
                "    is_active   TINYINT      NOT NULL DEFAULT 1," +
                "    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            // ==============================
            // INDEXES for performance
            // ==============================
            createIndexIfNotExists(stmt, "idx_users_username", "users", "username");
            createIndexIfNotExists(stmt, "idx_designs_designer", "designs", "designer_id");
            createIndexIfNotExists(stmt, "idx_furniture_design", "placed_furniture", "design_id");
            createIndexIfNotExists(stmt, "idx_sessions_designer", "sessions", "designer_id");
            createIndexIfNotExists(stmt, "idx_sessions_token", "sessions", "session_token");
            createIndexIfNotExists(stmt, "idx_audit_designer", "audit_log", "designer_id");
            createIndexIfNotExists(stmt, "idx_audit_timestamp", "audit_log", "created_at");
            createIndexIfNotExists(stmt, "idx_catalog_category", "furniture_catalog", "category");
            // Cleanup deprecated table
            stmt.execute("DROP TABLE IF EXISTS furniture_items");
        }

        System.out.println("[DB] MySQL schema initialized successfully.");
    }

    /**
     * Creates an index if it doesn't already exist (MySQL-safe).
     */
    private void createIndexIfNotExists(Statement stmt, String indexName, String tableName, String columnName) {
        try {
            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) FROM information_schema.statistics " +
                "WHERE table_schema = '" + DB_NAME + "' AND index_name = '" + indexName + "'"
            );
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("CREATE INDEX " + indexName + " ON " + tableName + "(" + columnName + ")");
            }
        } catch (SQLException e) {
            // Index might already exist, ignore
        }
    }

    /**
     * Checks if a table has any rows.
     */
    public boolean isTableEmpty(String tableName) {
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return rs.next() && rs.getInt(1) == 0;
        } catch (SQLException e) {
            return true;
        }
    }

    /**
     * Gracefully closes the database connection.
     */
    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] MySQL connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Returns connection info string (for debugging/display).
     */
    public String getDatabasePath() {
        return "mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
    }
}
