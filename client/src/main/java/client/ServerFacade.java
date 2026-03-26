package client;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class ServerFacade {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private static final String AUTHORIZATION_HEADER = "authorization";

    public ServerFacade(String host, int port) {
        this.baseUrl = "http://" + host + ":" + port;
        this.httpClient = HttpClient.newHttpClient();
    }

    public AuthData register(String username, String password, String email) {
        RegisterBody body = new RegisterBody(username, password, email);
        HttpResponse<String> response = post("/user", body, null);
        ensureOk(response);
        RegisterResponse parsed = gson.fromJson(response.body(), RegisterResponse.class);
        return new AuthData(parsed.authToken(), parsed.username());
    }

    public AuthData login(String username, String password) {
        LoginBody body = new LoginBody(username, password);
        HttpResponse<String> response = post("/session", body, null);
        ensureOk(response);
        LoginResponse parsed = gson.fromJson(response.body(), LoginResponse.class);
        return new AuthData(parsed.authToken(), parsed.username());
    }

    public void logout(String authToken) {
        HttpResponse<String> response = delete("/session", authToken);
        ensureOk(response);
    }

    public GameData[] listGames(String authToken) {
        HttpResponse<String> response = get("/game", authToken);
        ensureOk(response);
        ListGamesEnvelope envelope = gson.fromJson(response.body(), ListGamesEnvelope.class);
        return envelope.games();
    }

    public int createGame(String authToken, String gameName) {
        CreateGameBody body = new CreateGameBody(gameName);
        HttpResponse<String> response = post("/game", body, authToken);
        ensureOk(response);
        CreateGameResponse parsed = gson.fromJson(response.body(), CreateGameResponse.class);
        if (parsed.gameID() == null) {
            throw new ServerFacadeException("Server did not return a game ID");
        }
        return parsed.gameID();
    }

    public void joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) {
        JoinGameBody body = new JoinGameBody(playerColor, gameID);
        HttpResponse<String> response = put("/game", body, authToken);
        ensureOk(response);
    }

    private HttpResponse<String> get(String path, String authToken) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET();
        builder.header("Content-Type", "application/json");
        if (authToken != null && !authToken.isEmpty()) {
            builder.header(AUTHORIZATION_HEADER, authToken);
        }
        return send(builder.build());
    }

    private HttpResponse<String> delete(String path, String authToken) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path)).DELETE();
        builder.header("Content-Type", "application/json");
        if (authToken != null && !authToken.isEmpty()) {
            builder.header(AUTHORIZATION_HEADER, authToken);
        }
        return send(builder.build());
    }

    private HttpResponse<String> post(String path, Object body, String authToken) {
        String jsonBody = gson.toJson(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json");
        if (authToken != null && !authToken.isEmpty()) {
            builder.header(AUTHORIZATION_HEADER, authToken);
        }
        return send(builder.build());
    }

    private HttpResponse<String> put(String path, Object body, String authToken) {
        String jsonBody = gson.toJson(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json");
        if (authToken != null && !authToken.isEmpty()) {
            builder.header(AUTHORIZATION_HEADER, authToken);
        }
        return send(builder.build());
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServerFacadeException("Request failed", e);
        }
    }

    private void ensureOk(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            throw new ServerFacadeException(extractErrorMessage(response));
        }
    }

    private String extractErrorMessage(HttpResponse<String> response) {
        try {
            if (response.body() == null || response.body().isBlank()) {
                return "The server returned an error.";
            }
            Map<?, ?> json = gson.fromJson(response.body(), Map.class);
            Object msg = json.get("message");
            if (msg != null) {
                return msg.toString();
            }
        } catch (RuntimeException ignored) {
        }
        return "The server returned an error.";
    }

    private record RegisterBody(String username, String password, String email) {
    }

    private record LoginBody(String username, String password) {
    }

    private record RegisterResponse(String username, String authToken) {
    }

    private record LoginResponse(String username, String authToken) {
    }

    private record CreateGameBody(String gameName) {
    }

    private record CreateGameResponse(Integer gameID) {
    }

    private record JoinGameBody(ChessGame.TeamColor playerColor, Integer gameID) {
    }

    private record ListGamesEnvelope(GameData[] games) {
    }
}

