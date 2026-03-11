package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class MySqlGameDAO implements GameDAO {
    private static final String COL_GAME_JSON = "game";
    private final Gson gson = new Gson();

    public MySqlGameDAO() throws DataAccessException {
        SqlSchema.createDatabaseAndTables();
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("game cannot be null");
        }

        String gameJson = serializeGame(game.game());
        String sql = "INSERT INTO game (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, gameJson);
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
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return rowToGameData(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("failed to get game", e);
        }
    }

    @Override
    public Collection<GameData> getAllGames() throws DataAccessException {
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game";
        Collection<GameData> games = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                games.add(rowToGameData(rs));
            }
            return games;
        } catch (SQLException e) {
            throw new DataAccessException("failed to list games", e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("game cannot be null");
        }

        String gameJson = serializeGame(game.game());
        String sql = "UPDATE game SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, gameJson);
            ps.setInt(5, game.gameID());
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0) {
                throw new DataAccessException("game does not exist");
            }
        } catch (SQLException e) {
            throw new DataAccessException("failed to update game", e);
        }
    }

    private GameData rowToGameData(ResultSet rs) throws SQLException {
        String gameJson = rs.getString(COL_GAME_JSON);
        ChessGame chessGame = gson.fromJson(gameJson, ChessGame.class);
        return new GameData(
                rs.getInt("gameID"),
                rs.getString("whiteUsername"),
                rs.getString("blackUsername"),
                rs.getString("gameName"),
                chessGame
        );
    }

    private String serializeGame(ChessGame chessGame) {
        return gson.toJson(chessGame);
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "TRUNCATE TABLE game";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("failed to clear games", e);
        }
    }
}

