package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.GameService;
import service.UserService;

import java.util.Map;

public class ClearHandler {
    private final UserService userService;
    private final GameService gameService;
    private final Gson gson = new Gson();

    public ClearHandler(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    public void clear(Context ctx) {
        try {
            userService.clear();
            gameService.clear();
            ctx.status(200).result(gson.toJson(Map.of()));
        } catch (DataAccessException e) {
            ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}