package dataaccess;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;

    /*
     * Load the database information for the db.properties file.
     */
    static {
        loadPropertiesFromResources();
    }

    /**
     * Creates the database if it does not already exist.
     */
    static public void createDatabase() throws DataAccessException {
        var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create database", ex);
        }
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DatabaseManager.getConnection()) {
     * // execute SQL statements.
     * }
     * </code>
     */
    static Connection getConnection() throws DataAccessException {
        try {
            //do not wrap the following line with a try-with-resources
            var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get connection", ex);
        }
    }

    private static void loadPropertiesFromResources() {
        try {
            Properties props = new Properties();
            // 1) dataaccess/db.properties — local / committed defaults
            loadIfPresent(props, DatabaseManager.class.getResourceAsStream("db.properties"));
            // 2) classpath root /db.properties — BYU autograder writes this during grading (overlays above)
            loadIfPresent(props, DatabaseManager.class.getResourceAsStream("/db.properties"));
            applyEnvOverrides(props);
            if (trimOrNull(props.getProperty("db.host")) == null) {
                throw new Exception("Unable to load db.properties");
            }
            loadProperties(props);
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties", ex);
        }
    }

    private static void loadIfPresent(Properties props, InputStream stream) throws java.io.IOException {
        if (stream != null) {
            try (stream) {
                props.load(stream);
            }
        }
    }

    /** Lets CI / autograder override JDBC settings when set. */
    private static void applyEnvOverrides(Properties props) {
        envOverride(props, "db.host", "CHESS_DB_HOST", "MYSQL_HOST");
        envOverride(props, "db.port", "CHESS_DB_PORT", "MYSQL_PORT");
        envOverride(props, "db.name", "CHESS_DB_NAME", "MYSQL_DATABASE");
        envOverride(props, "db.user", "CHESS_DB_USER", "MYSQL_USER");
        envOverride(props, "db.password", "CHESS_DB_PASSWORD", "MYSQL_PASSWORD");
    }

    private static void envOverride(Properties props, String key, String... envNames) {
        for (String env : envNames) {
            String v = System.getenv(env);
            if (v != null) {
                props.setProperty(key, v.trim());
                return;
            }
        }
    }

    private static void loadProperties(Properties props) {
        databaseName = trimOrNull(props.getProperty("db.name"));
        dbUsername = trimOrNull(props.getProperty("db.user"));
        dbPassword = trimOrNull(props.getProperty("db.password"));

        var host = trimOrNull(props.getProperty("db.host"));
        var port = Integer.parseInt(props.getProperty("db.port"));
        connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
    }

    private static String trimOrNull(String s) {
        return s == null ? null : s.trim();
    }
}
