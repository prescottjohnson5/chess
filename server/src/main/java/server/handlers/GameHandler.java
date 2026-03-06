package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.GameService;
import service.exceptions.AlreadyTakenException;
import service.exceptions.BadRequestException;
import service.exceptions.UnauthorizedException;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.results.CreateGameResult;
import service.results.ListGamesResult;

import java.util.Map;

public class GameHandler {
    private final GameService service;
    private final Gson gson = new Gson();

    public GameHandler(GameService service) {
        this.service = service;
    }

    public void listGames(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            ListGamesResult result = service.listGames(authToken);
            ctx.status(200).result(gson.toJson(Map.of("games", result.games())));
        } catch (UnauthorizedException e) {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized")));
        } catch (DataAccessException e) {
            ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    public void createGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
            CreateGameResult result = service.createGame(authToken, request);
            ctx.status(200).result(gson.toJson(result));
        } catch (BadRequestException e) {
            ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request")));
        } catch (UnauthorizedException e) {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized")));
        } catch (DataAccessException e) {
            ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    public void joinGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
            service.joinGame(authToken, request);
            ctx.status(200);
        } catch (BadRequestException e) {
            ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request")));
        } catch (UnauthorizedException e) {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized")));
        } catch (AlreadyTakenException e) {
            ctx.status(403).result(gson.toJson(Map.of("message", "Error: already taken")));
        } catch (DataAccessException e) {
            ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}