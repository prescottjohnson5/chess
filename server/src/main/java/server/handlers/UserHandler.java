package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.UserService;
import service.exceptions.AlreadyTakenException;
import service.exceptions.BadRequestException;
import service.exceptions.UnauthorizedException;
import service.requests.LoginRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;

import java.util.Map;

public class UserHandler {
    private final UserService service;
    private final Gson gson = new Gson();

    public UserHandler(UserService service) {
        this.service = service;
    }

    public void register(Context ctx) {
        try {
            RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
            RegisterResult result = service.register(request);
            ctx.status(200).result(gson.toJson(result));
        } catch (BadRequestException e) {
            ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request")));
        } catch (AlreadyTakenException e) {
            ctx.status(403).result(gson.toJson(Map.of("message", "Error: already taken")));
        } catch (DataAccessException e) {
            ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    public void login(Context ctx) {
        try {
            LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
            LoginResult result = service.login(request);
            ctx.status(200).result(gson.toJson(result));
        } catch (BadRequestException e) {
            ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request")));
        } catch (UnauthorizedException e) {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized")));
        } catch (DataAccessException e) {
            ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    public void logout(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            service.logout(authToken);
            ctx.status(200);
        } catch (UnauthorizedException e) {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized")));
        } catch (DataAccessException e) {
            ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}