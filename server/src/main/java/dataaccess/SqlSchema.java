package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SqlSchema {
    private SqlSchema() {
    }

    public static void createDatabaseAndTables() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            createUserTable(conn);
            createGameTable(conn);
            createAuthTable(conn);
        } catch (SQLException e) {
            throw new DataAccessException("failed to initialize tables", e);
        }
    }

    private static void createUserTable(Connection conn) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS user (
                    username VARCHAR(256) NOT NULL,
                    password VARCHAR(256) NOT NULL,
                    email VARCHAR(256) NOT NULL,
                    PRIMARY KEY (username)
                )
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private static void createGameTable(Connection conn) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS game (
                    gameID INT NOT NULL AUTO_INCREMENT,
                    whiteUsername VARCHAR(256),
                    blackUsername VARCHAR(256),
                    gameName VARCHAR(256) NOT NULL,
                    game TEXT NOT NULL,
                    PRIMARY KEY (gameID)
                )
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private static void createAuthTable(Connection conn) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS auth (
                    authToken VARCHAR(512) NOT NULL,
                    username VARCHAR(256) NOT NULL,
                    PRIMARY KEY (authToken)
                )
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
}

