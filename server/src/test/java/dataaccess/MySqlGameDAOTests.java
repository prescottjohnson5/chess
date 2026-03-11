package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class MySqlGameDAOTests {

    private MySqlUserDAO userDAO;
    private MySqlAuthDAO authDAO;
    private MySqlGameDAO gameDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        userDAO = new MySqlUserDAO();
        authDAO = new MySqlAuthDAO();
        gameDAO = new MySqlGameDAO();
        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();

        userDAO.createUser(new UserData("alice", "pw", "a@test.com"));
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
    @DisplayName("Get game missing returns null")
    void getGameMissingReturnsNull() throws DataAccessException {
        assertNull(gameDAO.getGame(999999));
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
}

