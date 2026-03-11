package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SqlSchema {
    private SqlSchema() {
    }

    private static final String[] TABLE_DEFINITIONS = {
            """
            CREATE TABLE IF NOT EXISTS user (
                username VARCHAR(256) NOT NULL,
                password VARCHAR(256) NOT NULL,
                email VARCHAR(256) NOT NULL,
                PRIMARY KEY (username)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS game (
                gameID INT NOT NULL AUTO_INCREMENT,
                whiteUsername VARCHAR(256),
                blackUsername VARCHAR(256),
                gameName VARCHAR(256) NOT NULL,
                game TEXT NOT NULL,
                PRIMARY KEY (gameID)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(512) NOT NULL,
                username VARCHAR(256) NOT NULL,
                PRIMARY KEY (authToken)
            )
            """
    };

    public static void createDatabaseAndTables() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String ddl : TABLE_DEFINITIONS) {
                stmt.executeUpdate(ddl);
            }
        } catch (SQLException e) {
            throw new DataAccessException("failed to initialize tables", e);
        }
    }
}

