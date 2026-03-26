package client;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    private static final String HOST = "localhost";
    private static int port;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        facade = new ServerFacade(HOST, port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() {
        HttpClient http = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder(
                            URI.create("http://" + HOST + ":" + port + "/db"))
                    .DELETE()
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> response =
                    http.send(request, HttpResponse.BodyHandlers.ofString());
            Assertions.assertEquals(200, response.statusCode());
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear database for test", e);
        }
    }

    @Test
    void registerSuccess() {
        AuthData auth = facade.register("alice", "password", "a@test.com");
        Assertions.assertNotNull(auth);
        Assertions.assertEquals("alice", auth.username());
        Assertions.assertNotNull(auth.authToken());
        Assertions.assertTrue(auth.authToken().length() > 0);
    }

    @Test
    void registerDuplicateFails() {
        facade.register("alice", "password", "a@test.com");
        Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.register("alice", "password2", "a2@test.com"));
    }

    @Test
    void loginSuccess() {
        facade.register("bob", "pw", "b@test.com");
        AuthData auth = facade.login("bob", "pw");
        Assertions.assertNotNull(auth);
        Assertions.assertEquals("bob", auth.username());
        Assertions.assertNotNull(auth.authToken());
    }

    @Test
    void loginMissingUserFails() {
        Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.login("missing", "pw"));
    }

    @Test
    void logoutSuccess() {
        AuthData auth = facade.register("c", "pw", "c@test.com");
        Assertions.assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    void logoutTwiceFails() {
        AuthData auth = facade.register("d", "pw", "d@test.com");
        facade.logout(auth.authToken());
        Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.logout(auth.authToken()));
    }

    @Test
    void listGamesSuccessInitiallyEmpty() {
        AuthData auth = facade.register("e", "pw", "e@test.com");
        GameData[] games = facade.listGames(auth.authToken());
        Assertions.assertNotNull(games);
        Assertions.assertEquals(0, games.length);
    }

    @Test
    void listGamesInvalidTokenFails() {
        Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.listGames("bad-token"));
    }

    @Test
    void createGameSuccess() {
        AuthData auth = facade.register("f", "pw", "f@test.com");
        int gameID = facade.createGame(auth.authToken(), "mygame");
        Assertions.assertTrue(gameID > 0);
    }

    @Test
    void createGameBadRequestFails() {
        AuthData auth = facade.register("g", "pw", "g@test.com");
        Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.createGame(auth.authToken(), null));
    }

    @Test
    void joinGameSuccessUpdatesList() {
        AuthData auth = facade.register("h", "pw", "h@test.com");
        int gameID = facade.createGame(auth.authToken(), "join-me");

        Assertions.assertDoesNotThrow(() ->
                facade.joinGame(auth.authToken(), ChessGame.TeamColor.WHITE, gameID));

        GameData[] games = facade.listGames(auth.authToken());
        Assertions.assertEquals(1, games.length);
        Assertions.assertEquals(gameID, games[0].gameID());
        Assertions.assertEquals("h", games[0].whiteUsername());
        Assertions.assertNull(games[0].blackUsername());
    }

    @Test
    void joinGameMissingGameFails() {
        AuthData auth = facade.register("i", "pw", "i@test.com");
        Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.joinGame(auth.authToken(), ChessGame.TeamColor.WHITE, 123456));
    }
}

