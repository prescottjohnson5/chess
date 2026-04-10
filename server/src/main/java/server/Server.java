package server;

import dataaccess.*;
import io.javalin.Javalin;
import io.javalin.websocket.WsHandlerType;
import server.handlers.ClearHandler;
import server.handlers.GameHandler;
import server.handlers.UserHandler;
import server.websocket.GameWebSocketHandler;
import service.GameService;
import service.UserService;

public class Server {

    private final Javalin javalin;

    public Server() {
        DataAccessLayer data = initDataAccess();

        UserService userService = new UserService(data.userDAO(), data.authDAO());
        GameService gameService = new GameService(data.gameDAO(), data.authDAO());

        UserHandler userHandler = new UserHandler(userService);
        GameHandler gameHandler = new GameHandler(gameService);
        ClearHandler clearHandler = new ClearHandler(userService, gameService);
        GameWebSocketHandler webSocketHandler = new GameWebSocketHandler(data.gameDAO(), data.authDAO());

        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        javalin.addWsHandler(WsHandlerType.WEBSOCKET, "/ws", ws -> {
            ws.onMessage(webSocketHandler::onMessage);
            ws.onClose(webSocketHandler::onClose);
        });

        javalin.delete("/db", clearHandler::clear);

        javalin.post("/user", userHandler::register);

        javalin.post("/session", userHandler::login);
        javalin.delete("/session", userHandler::logout);

        javalin.get("/game", gameHandler::listGames);
        javalin.post("/game", gameHandler::createGame);
        javalin.put("/game", gameHandler::joinGame);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private static DataAccessLayer initDataAccess() {
        try {
            return new DataAccessLayer(
                    new MySqlUserDAO(),
                    new MySqlGameDAO(),
                    new MySqlAuthDAO()
            );
        } catch (DataAccessException e) {
            throw new RuntimeException("Unable to initialize database", e);
        }
    }

    private record DataAccessLayer(UserDAO userDAO, GameDAO gameDAO, AuthDAO authDAO) {
    }
}

