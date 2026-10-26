package com.smartcampus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/** Optional PostgreSQL access. Enabled only when DB_URL is set. */
final class Database {
    private Database() {}

    static boolean configured() {
        return !Config.get("DB_URL", "").isEmpty();
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                Config.get("DB_URL", ""),
                Config.get("DB_USER", "smartcampus"),
                Config.get("DB_PASSWORD", ""));
    }

    /** Creates the table, retrying while the database container is starting. */
    static void init() {
        if (!configured()) {
            System.out.println("Database not configured (DB_URL is empty): running without PostgreSQL");
            return;
        }
        DriverManager.setLoginTimeout(3);
        for (int attempt = 1; attempt <= 10; attempt++) {
            try (Connection c = connect(); Statement s = c.createStatement()) {
                s.execute("CREATE TABLE IF NOT EXISTS security_events ("
                        + "id SERIAL PRIMARY KEY, "
                        + "created_at TIMESTAMP DEFAULT now(), "
                        + "message TEXT NOT NULL)");
                System.out.println("Database ready");
                return;
            } catch (SQLException e) {
                System.err.println("Database not ready (attempt " + attempt + "/10): " + e.getMessage());
                try { Thread.sleep(3000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
            }
        }
        System.err.println("Database unreachable: continuing without it");
    }

    /** true when no DB is configured, or when the configured DB answers. */
    static boolean isUp() {
        if (!configured()) return true;
        try (Connection c = connect()) {
            return c.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    static void insertEvent(String message) {
        if (!configured()) return;
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("INSERT INTO security_events(message) VALUES (?)")) {
            ps.setString(1, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Could not store security event in DB: " + e.getMessage());
        }
    }
}
