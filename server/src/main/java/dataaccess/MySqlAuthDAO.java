package dataaccess;

import model.AuthData;

import java.sql.SQLException;

public class MySqlAuthDAO implements AuthDAO {

    public MySqlAuthDAO() throws DataAccessException {
        SqlSchema.createDatabaseAndTables();
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        if (auth == null) {
            throw new DataAccessException("auth cannot be null");
        }
        runUpdate("INSERT INTO auth (authToken, username) VALUES (?, ?)", auth.authToken(), auth.username());
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        if (authToken == null) {
            return null;
        }
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("SELECT authToken, username FROM auth WHERE authToken = ?")) {
            ps.setString(1, authToken);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? new AuthData(rs.getString(1), rs.getString(2)) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("failed to get auth", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        runUpdate("DELETE FROM auth WHERE authToken = ?", authToken);
    }

    @Override
    public void clear() throws DataAccessException {
        runUpdate("TRUNCATE TABLE auth");
    }

    private static void runUpdate(String sql, String... args) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                ps.setString(i + 1, args[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("failed to update auth", e);
        }
    }
}

