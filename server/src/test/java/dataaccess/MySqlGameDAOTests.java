package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class MySqlGameDAOTests extends MySqlDAOTestBase {

    @BeforeEach
    void setUpGame() throws DataAccessException {
        createAliceUser();
    }

    @Test
    @DisplayName("Create game success")
    void createGameSuccess() throws DataAccessException {
        GameData game = new GameData(0, null, null, "game1", new ChessGame());
        int id = gameDAO.createGame(game);
        assertTrue(id > 0);

        GameData stored = gameDAO.getGame(id);
        assertNotNull(stored);
        assertEquals(id, stored.gameID());
        assertEquals("game1", stored.gameName());
        assertNotNull(stored.game());
    }

    @Test
    @DisplayName("Create game null fails")
    void createGameNullFails() {
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(null));
    }

    @Test
    @DisplayName("Get game success")
    void getGameSuccess() throws DataAccessException {
        GameData input = new GameData(0, null, null, "fetchMe", new ChessGame());
        int id = gameDAO.createGame(input);
        GameData stored = gameDAO.getGame(id);
        assertNotNull(stored);
        assertEquals(id, stored.gameID());
        assertEquals("fetchMe", stored.gameName());
    }

    @Test
    @DisplayName("Get game missing returns null")
    void getGameMissingReturnsNull() throws DataAccessException {
        GameData result = gameDAO.getGame(999999);
        assertNull(result);
    }

    @Test
    @DisplayName("List games success")
    void listGamesSuccess() throws DataAccessException {
        int id1 = gameDAO.createGame(new GameData(0, null, null, "g1", new ChessGame()));
        int id2 = gameDAO.createGame(new GameData(0, null, null, "g2", new ChessGame()));
        var all = gameDAO.getAllGames();
        assertNotNull(all);
        assertTrue(all.size() >= 2);
        assertTrue(all.stream().anyMatch(g -> g.gameID() == id1));
        assertTrue(all.stream().anyMatch(g -> g.gameID() == id2));
    }

    @Test
    @DisplayName("Get all games empty returns empty list")
    void getAllGamesEmptyReturnsEmptyList() throws DataAccessException {
        gameDAO.clear();
        var all = gameDAO.getAllGames();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    @DisplayName("Update game success")
    void updateGameSuccess() throws DataAccessException {
        int id = gameDAO.createGame(new GameData(0, null, null, "g1", new ChessGame()));
        GameData stored = gameDAO.getGame(id);
        assertNotNull(stored);

        GameData updated = new GameData(id, "alice", null, stored.gameName(), stored.game());
        gameDAO.updateGame(updated);

        GameData after = gameDAO.getGame(id);
        assertNotNull(after);
        assertEquals("alice", after.whiteUsername());
    }

    @Test
    @DisplayName("Update game null fails")
    void updateGameNullFails() {
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(null));
    }

    @Test
    @DisplayName("Update missing game fails")
    void updateMissingGameFails() {
        GameData missing = new GameData(1234567, null, null, "missing", new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(missing));
    }

    @Test
    @DisplayName("Clear games success")
    void clearGamesSuccess() throws DataAccessException {
        gameDAO.createGame(new GameData(0, null, null, "g1", new ChessGame()));
        gameDAO.clear();
        assertTrue(gameDAO.getAllGames().isEmpty());
    }

    @Test
    @DisplayName("Clear when empty does not throw")
    void clearWhenEmptySucceeds() throws DataAccessException {
        gameDAO.clear();
        assertDoesNotThrow(() -> gameDAO.clear());
        int id = gameDAO.createGame(new GameData(0, null, null, "after", new ChessGame()));
        assertNotNull(gameDAO.getGame(id));
    }
}

