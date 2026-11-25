package com.travelease.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * DBConnection - Database Connection Utility
 * jdbc type 4 driver - pure java driver
 * Manages raw JDBC connections to the local MySQL Server using credentials 
 * loaded from 'db.properties'.
 * 
 * KEY CONCEPTS:
 * 1. DriverManager: Factory class that registers the MySQL JDBC Driver ('com.mysql.cj.jdbc.Driver')
 *    and establishes a TCP socket connection to the database.
 * 2. Connection: Represents an open physical session with MySQL.
 * 3. Connection Pooling / Auto-Close: Always close connections or use try-with-resources
 *    to return sockets back to the OS and prevent memory/connection leaks.
 */
public class DBConnection {

    private static String url = "jdbc:mysql://localhost:3306/travelease?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String user = "root";
    private static String password = "root";
    private static boolean initialized = false;

    static {
        loadProperties();
    }

    private static synchronized void loadProperties() {
        if (initialized) return;

        // Step 1: Ensure MySQL Driver is registered
        ensureDriverRegistered();

        // Step 2: Read connection properties
        Properties props = new Properties();
        boolean loaded = false;

        // Try 1: From classpath resource
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
                loaded = true;
            }
        } catch (Exception ignored) {
        }

        // Try 2: From filesystem if classpath load was not available
        if (!loaded) {
            String[] candidatePaths = {
                "backend/src/main/resources/db.properties",
                "src/main/resources/db.properties",
                "../../../../backend/src/main/resources/db.properties",
                "../../../../../../backend/src/main/resources/db.properties"
            };
            for (String path : candidatePaths) {
                File f = new File(path);
                if (f.exists() && f.isFile()) {
                    try (InputStream fis = new FileInputStream(f)) {
                        props.load(fis);
                        loaded = true;
                        break;
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        if (loaded) {
            if (props.getProperty("db.url") != null) url = props.getProperty("db.url");
            if (props.getProperty("db.user") != null) user = props.getProperty("db.user");
            if (props.getProperty("db.password") != null) password = props.getProperty("db.password");
        }

        initialized = true;
        System.out.println("[DBConnection] Driver loaded. Target URL: " + url);
    }

    private static void ensureDriverRegistered() {
        // Direct attempt via standard classloader
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return;
        } catch (ClassNotFoundException ignored) {
        }

        // Fallback: Locate mysql-connector jar in project folders
        String[] jarPaths = {
            "lib/mysql-connector-j-8.3.0.jar",
            "backend/lib/mysql-connector-j-8.3.0.jar",
            "backend/target/dependency/mysql-connector-j-8.3.0.jar",
            "../lib/mysql-connector-j-8.3.0.jar",
            "../../lib/mysql-connector-j-8.3.0.jar",
            "../../../../lib/mysql-connector-j-8.3.0.jar",
            "../../../../backend/lib/mysql-connector-j-8.3.0.jar",
            "../../../../backend/target/dependency/mysql-connector-j-8.3.0.jar",
            "../../../../../../lib/mysql-connector-j-8.3.0.jar",
            "../../../../../../backend/lib/mysql-connector-j-8.3.0.jar",
            "../../../../../../backend/target/dependency/mysql-connector-j-8.3.0.jar"
        };

        File foundJar = null;
        for (String p : jarPaths) {
            File f = new File(p);
            if (f.exists() && f.isFile()) {
                foundJar = f;
                break;
            }
        }

        if (foundJar != null) {
            try {
                URL[] urls = new URL[]{foundJar.toURI().toURL()};
                URLClassLoader loader = new URLClassLoader(urls, DBConnection.class.getClassLoader());
                Class<?> driverClass = Class.forName("com.mysql.cj.jdbc.Driver", true, loader);
                Driver rawDriver = (Driver) driverClass.getDeclaredConstructor().newInstance();
                DriverManager.registerDriver(new DriverShim(rawDriver));
                return;
            } catch (Exception ex) {
                System.err.println("[DBConnection] Dynamic driver load warning: " + ex.getMessage());
            }
        }

        System.err.println("[DBConnection] WARNING: MySQL JDBC Driver could not be auto-discovered. Please ensure mysql-connector-j-8.3.0.jar is in classpath.");
    }

    /**
     * DriverShim allows DriverManager to use a driver loaded from a custom URLClassLoader.
     */
    private static class DriverShim implements Driver {
        private final Driver driver;

        public DriverShim(Driver d) {
            this.driver = d;
        }

        @Override
        public boolean acceptsURL(String u) throws SQLException {
            return this.driver.acceptsURL(u);
        }

        @Override
        public Connection connect(String u, Properties p) throws SQLException {
            return this.driver.connect(u, p);
        }

        @Override
        public int getMajorVersion() {
            return this.driver.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return this.driver.getMinorVersion();
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
            return this.driver.getPropertyInfo(u, p);
        }

        @Override
        public boolean jdbcCompliant() {
            return this.driver.jdbcCompliant();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return this.driver.getParentLogger();
        }
    }

    /**
     * Obtains a new JDBC Connection to the MySQL database.
     * 
     * @return Connection active SQL connection
     * @throws SQLException if authentication fails or MySQL is not running
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            loadProperties();
        }
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Safely closes one or more JDBC resources (Connection, Statement, ResultSet)
     * without throwing checked exceptions.
     */
    public static void closeQuietly(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Simple test method to verify connectivity from terminal or IDE.
     */
    public static boolean testConnection() {
        System.out.println("[DBConnection] Testing connection to MySQL...");
        try (Connection conn = getConnection()) {
            boolean valid = conn.isValid(3);
            System.out.println("[DBConnection] Connection successful! Database: " + conn.getCatalog());
            return valid;
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connection FAILED: " + e.getMessage());
            System.err.println("-> Please verify: Is MySQL Server running? Are credentials in db.properties correct? Did you execute schema.sql in Workbench?");
            return false;
        }
    }

    public static void main(String[] args) {
        testConnection();
    }
}
