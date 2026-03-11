package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class MySqlGameDAO implements GameDAO {
    private final Gson gson = new Gson();

    public MySqlGameDAO() throws DataAccessException {
        SqlSchema.createDatabaseAndTables();
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("game cannot be null");
        }
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("INSERT INTO game (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
            bindGameRow(ps, game.whiteUsername(), game.blackUsername(), game.gameName(), gson.toJson(game.game()));
            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DataAccessException("failed to create game (no id returned)");
        } catch (SQLException e) {
            throw new DataAccessException("failed to create game", e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game WHERE gameID = ?")) {
            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return toGameData(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("failed to get game", e);
        }
    }

    @Override
    public Collection<GameData> getAllGames() throws DataAccessException {
        Collection<GameData> out = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game");
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(toGameData(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new DataAccessException("failed to list games", e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("game cannot be null");
        }
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("UPDATE game SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?")) {
            bindGameRow(ps, game.whiteUsername(), game.blackUsername(), game.gameName(), gson.toJson(game.game()));
            ps.setInt(5, game.gameID());
            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("game does not exist");
            }
        } catch (SQLException e) {
            throw new DataAccessException("failed to update game", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("TRUNCATE TABLE game")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("failed to clear games", e);
        }
    }

    private static void bindGameRow(java.sql.PreparedStatement ps, String white, String black, String name, String gameJson) throws SQLException {
        ps.setString(1, white);
        ps.setString(2, black);
        ps.setString(3, name);
        ps.setString(4, gameJson);
    }

    private GameData toGameData(ResultSet rs) throws SQLException {
        return new GameData(
                rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                gson.fromJson(rs.getString(5), ChessGame.class)
        );
    }
}

