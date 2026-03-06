package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.exceptions.AlreadyTakenException;
import service.exceptions.BadRequestException;
import service.exceptions.UnauthorizedException;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTests {

    private GameService gameService;
    private MemoryGameDAO gameDAO;
    private MemoryAuthDAO authDAO;
    private static final String VALID_TOKEN = "valid-auth-token";
    private static final String USERNAME = "player1";

    @BeforeEach
    void setUp() throws DataAccessException {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        authDAO.createAuth(new AuthData(VALID_TOKEN, USERNAME));
        gameService = new GameService(gameDAO, authDAO);
    }

    // ---------- listGames ----------

    @Test
    @DisplayName("List games success")
    void listGamesSuccess() throws DataAccessException, UnauthorizedException {
        var result = gameService.listGames(VALID_TOKEN);
        assertNotNull(result);
        assertNotNull(result.games());
        assertEquals(0, result.games().length);
    }

    @Test
    @DisplayName("List games unauthorized - invalid token")
    void listGamesUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> gameService.listGames("bad-token"));
    }

    @Test
    @DisplayName("List games unauthorized - null token")
    void listGamesUnauthorizedNullToken() {
        assertThrows(UnauthorizedException.class, () -> gameService.listGames(null));
    }

    // ---------- createGame ----------

    @Test
    @DisplayName("Create game success")
    void createGameSuccess() throws DataAccessException, UnauthorizedException, BadRequestException {
        CreateGameRequest request = new CreateGameRequest("My Game");
        var result = gameService.createGame(VALID_TOKEN, request);

        assertNotNull(result);
        assertNotNull(result.gameID());
        assertTrue(result.gameID() > 0);
        assertNotNull(gameDAO.getGame(result.gameID()));
        assertEquals("My Game", gameDAO.getGame(result.gameID()).gameName());
    }

    @Test
    @DisplayName("Create game bad request - null game name")
    void createGameBadRequest() {
        CreateGameRequest request = new CreateGameRequest(null);
        assertThrows(BadRequestException.class, () -> gameService.createGame(VALID_TOKEN, request));
    }

    @Test
    @DisplayName("Create game bad request - empty game name")
    void createGameBadRequestEmptyName() {
        CreateGameRequest request = new CreateGameRequest("");
        assertThrows(BadRequestException.class, () -> gameService.createGame(VALID_TOKEN, request));
    }

    @Test
    @DisplayName("Create game unauthorized")
    void createGameUnauthorized() {
        CreateGameRequest request = new CreateGameRequest("Game");
        assertThrows(UnauthorizedException.class, () -> gameService.createGame("invalid", request));
    }

    // ---------- joinGame ----------

    @Test
    @DisplayName("Join game success as white")
    void joinGameSuccessWhite() throws DataAccessException, UnauthorizedException, BadRequestException, AlreadyTakenException {
        var createResult = gameService.createGame(VALID_TOKEN, new CreateGameRequest("Test"));
        gameService.joinGame(VALID_TOKEN, new JoinGameRequest(ChessGame.TeamColor.WHITE, createResult.gameID()));

        GameData game = gameDAO.getGame(createResult.gameID());
        assertNotNull(game);
        assertEquals(USERNAME, game.whiteUsername());
        assertNull(game.blackUsername());
    }

    @Test
    @DisplayName("Join game success as black")
    void joinGameSuccessBlack() throws DataAccessException, UnauthorizedException, BadRequestException, AlreadyTakenException {
        var createResult = gameService.createGame(VALID_TOKEN, new CreateGameRequest("Test"));
        gameService.joinGame(VALID_TOKEN, new JoinGameRequest(ChessGame.TeamColor.BLACK, createResult.gameID()));

        GameData game = gameDAO.getGame(createResult.gameID());
        assertNotNull(game);
        assertNull(game.whiteUsername());
        assertEquals(USERNAME, game.blackUsername());
    }

    @Test
    @DisplayName("Join game bad request - null game ID")
    void joinGameBadRequestNullGameId() {
        assertThrows(BadRequestException.class, () ->
                gameService.joinGame(VALID_TOKEN, new JoinGameRequest(ChessGame.TeamColor.WHITE, null)));
    }

    @Test
    @DisplayName("Join game bad request - null player color")
    void joinGameBadRequestNullColor() throws DataAccessException, UnauthorizedException, BadRequestException {
        var createResult = gameService.createGame(VALID_TOKEN, new CreateGameRequest("Game"));
        assertThrows(BadRequestException.class, () ->
                gameService.joinGame(VALID_TOKEN, new JoinGameRequest(null, createResult.gameID())));
    }

    @Test
    @DisplayName("Join game already taken")
    void joinGameAlreadyTaken() throws DataAccessException, UnauthorizedException, BadRequestException, AlreadyTakenException {
        var createResult = gameService.createGame(VALID_TOKEN, new CreateGameRequest("Game"));
        gameService.joinGame(VALID_TOKEN, new JoinGameRequest(ChessGame.TeamColor.WHITE, createResult.gameID()));

        assertThrows(AlreadyTakenException.class, () ->
                gameService.joinGame(VALID_TOKEN, new JoinGameRequest(ChessGame.TeamColor.WHITE, createResult.gameID())));
    }

    @Test
    @DisplayName("Join game unauthorized")
    void joinGameUnauthorized() throws DataAccessException, UnauthorizedException, BadRequestException {
        var createResult = gameService.createGame(VALID_TOKEN, new CreateGameRequest("Game"));
        assertThrows(UnauthorizedException.class, () ->
                gameService.joinGame("bad-token", new JoinGameRequest(ChessGame.TeamColor.WHITE, createResult.gameID())));
    }

    // ---------- clear ----------

    @Test
    @DisplayName("Clear success")
    void clearSuccess() throws DataAccessException, UnauthorizedException, BadRequestException {
        gameService.createGame(VALID_TOKEN, new CreateGameRequest("Game1"));
        gameService.clear();
        assertTrue(gameDAO.getAllGames().isEmpty());
    }
}
