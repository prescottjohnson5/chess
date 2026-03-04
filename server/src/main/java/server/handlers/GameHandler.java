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

import java.util.Map;

public class GameHandler {
    private final GameService service;
    private final Gson gson = new Gson();

    public GameHandler(GameService service) {
        this.service = service;
    }

    public void listGames(Context ctx) {
        try {
            String token = ctx.header("authorization");
            var res = service.listGames(token);
            ctx.status(200).result(gson.toJson(Map.of("games", res.games())));
        } catch (UnauthorizedException e) {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized")));
        } catch (DataAccessException e) {
            ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    public void createGame(Context ctx) {
        try {
            String token = ctx.header("authorization");
            CreateGameRequest req = gson.fromJson(ctx.body(), CreateGameRequest.class);
            var res = service.createGame(token, req);
            ctx.status(200).result(gson.toJson(res));
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
            String token = ctx.header("authorization");
            JoinGameRequest req = gson.fromJson(ctx.body(), JoinGameRequest.class);
            service.joinGame(token, req);
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