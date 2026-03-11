package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class MySqlUserDAO implements UserDAO {

    public MySqlUserDAO() throws DataAccessException {
        SqlSchema.createDatabaseAndTables();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user == null) {
            throw new DataAccessException("user cannot be null");
        }
        UserData toInsert = new UserData(user.username(), hashPassword(user.password()), user.email());
        executeUpdate("INSERT INTO user (username, password, email) VALUES (?, ?, ?)", toInsert.username(), toInsert.password(), toInsert.email());
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (username == null) {
            return null;
        }
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("SELECT username, password, email FROM user WHERE username = ?")) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? userFromRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("failed to get user", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        executeUpdate("TRUNCATE TABLE user");
    }

    private static String hashPassword(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt());
    }

    private static UserData userFromRow(java.sql.ResultSet rs) throws SQLException {
        return new UserData(rs.getString(1), rs.getString(2), rs.getString(3));
    }

    private static void executeUpdate(String sql, String... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            String msg = sql.startsWith("TRUNCATE") ? "failed to clear users" : "failed to create user";
            throw new DataAccessException(msg, e);
        }
    }
}

