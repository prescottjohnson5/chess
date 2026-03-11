package server;

import dataaccess.*;
import io.javalin.Javalin;
import server.handlers.ClearHandler;
import server.handlers.GameHandler;
import server.handlers.UserHandler;
import service.GameService;
import service.UserService;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        UserDAO userDAO;
        GameDAO gameDAO;
        AuthDAO authDAO;
        try {
            userDAO = new MySqlUserDAO();
            gameDAO = new MySqlGameDAO();
            authDAO = new MySqlAuthDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException("Unable to initialize database", e);
        }

        // Services
        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);

        // Handlers
        UserHandler userHandler = new UserHandler(userService);
        GameHandler gameHandler = new GameHandler(gameService);
        ClearHandler clearHandler = new ClearHandler(userService, gameService);

        // Routes
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
}

