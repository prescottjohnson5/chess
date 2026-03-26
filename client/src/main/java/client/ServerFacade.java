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
        RegisterResponse parsed = postFor("/user", body, null, RegisterResponse.class);
        return new AuthData(parsed.authToken(), parsed.username());
    }

    public AuthData login(String username, String password) {
        LoginBody body = new LoginBody(username, password);
        LoginResponse parsed = postFor("/session", body, null, LoginResponse.class);
        return new AuthData(parsed.authToken(), parsed.username());
    }

    public void logout(String authToken) {
        exchangeNoBody("DELETE", "/session", null, authToken);
    }

    public GameData[] listGames(String authToken) {
        ListGamesEnvelope envelope = exchangeFor("/game", authToken, ListGamesEnvelope.class);
        return envelope.games();
    }

    public int createGame(String authToken, String gameName) {
        CreateGameBody body = new CreateGameBody(gameName);
        CreateGameResponse parsed = postFor("/game", body, authToken, CreateGameResponse.class);
        if (parsed.gameID() == null) {
            throw new ServerFacadeException("Server did not return a game ID");
        }
        return parsed.gameID();
    }

    public void joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) {
        JoinGameBody body = new JoinGameBody(playerColor, gameID);
        exchangeNoBody("PUT", "/game", body, authToken);
    }

    private <T> T exchangeFor(String path, String authToken, Class<T> responseClass) {
        HttpResponse<String> response = exchange(path, authToken, "GET", null);
        ensureOk(response);
        return gson.fromJson(response.body(), responseClass);
    }

    private <T> T postFor(String path, Object body, String authToken, Class<T> responseClass) {
        HttpResponse<String> response = exchange(path, authToken, "POST", body);
        ensureOk(response);
        return gson.fromJson(response.body(), responseClass);
    }

    private void exchangeNoBody(String method, String path, Object body, String authToken) {
        HttpResponse<String> response = exchange(path, authToken, method, body);
        ensureOk(response);
    }

    private HttpResponse<String> exchange(String path, String authToken, String method, Object body) {
        URI uri = URI.create(baseUrl + path);
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri).method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(gson.toJson(body)));
        builder.header("Content-Type", "application/json");
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

