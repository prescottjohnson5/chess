package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import websocket.messages.ServerMessage;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GameWebSocketHandler {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final Gson gson = new Gson();

    private final ConcurrentHashMap<Integer, Set<WsContext>> sessionsByGame = new ConcurrentHashMap<>();

    public GameWebSocketHandler(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void onMessage(WsMessageContext ctx) {
        try {
            JsonObject json = JsonParser.parseString(ctx.message()).getAsJsonObject();
            String cmd = json.get("commandType").getAsString();
            switch (cmd) {
                case "CONNECT" -> handleConnect(ctx, json);
                case "MAKE_MOVE" -> handleMakeMove(ctx, json);
                case "LEAVE" -> handleLeave(ctx, json);
                case "RESIGN" -> handleResign(ctx, json);
                default -> sendError(ctx, "Error: unknown command");
            }
        } catch (DataAccessException e) {
            sendError(ctx, "Error: " + e.getMessage());
        } catch (RuntimeException e) {
            sendError(ctx, "Error: bad request");
        }
    }

    public void onClose(WsCloseContext ctx) {
        removeSession(ctx);
    }

    private void handleConnect(WsMessageContext ctx, JsonObject json) throws DataAccessException {
        if (Boolean.TRUE.equals(ctx.attribute("joined"))) {
            sendError(ctx, "Error: already connected to a game");
            return;
        }
        String authToken = getString(json, "authToken");
        int gameID = parseGameId(json.get("gameID"));
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            sendError(ctx, "Error: game not found");
            return;
        }
        PlayerRole role = resolveRole(auth.username(), game);
        registerSession(gameID, ctx);
        ctx.attribute("joined", true);
        ctx.attribute("gameID", gameID);
        ctx.attribute("username", auth.username());
        ctx.attribute("role", role);
        sendLoadGame(ctx, game.game());
        String note = switch (role) {
            case WHITE -> auth.username() + " joined as white";
            case BLACK -> auth.username() + " joined as black";
            case OBSERVER -> auth.username() + " joined as observer";
        };
        broadcastNotification(gameID, note, ctx);
    }

    private void handleMakeMove(WsMessageContext ctx, JsonObject json) throws DataAccessException {
        SessionInfo info = requireSession(ctx);
        if (info == null) {
            return;
        }
        if (info.role == PlayerRole.OBSERVER) {
            sendError(ctx, "Error: observers cannot move");
            return;
        }
        String authToken = getString(json, "authToken");
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null || !auth.username().equals(info.username)) {
            sendError(ctx, "Error: unauthorized");
            return;
        }
        int gameID = parseGameId(json.get("gameID"));
        if (gameID != info.gameID) {
            sendError(ctx, "Error: wrong game");
            return;
        }
        if (!json.has("move") || json.get("move").isJsonNull()) {
            sendError(ctx, "Error: missing move");
            return;
        }
        ChessMove move = gson.fromJson(json.get("move"), ChessMove.class);
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            sendError(ctx, "Error: game not found");
            return;
        }
        ChessGame chess = game.game();
        var piece = chess.getBoard().getPiece(move.getStartPosition());
        if (piece == null) {
            sendError(ctx, "Error: no piece at start");
            return;
        }
        ChessGame.TeamColor playerColor = info.role == PlayerRole.WHITE ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        if (piece.getTeamColor() != playerColor) {
            sendError(ctx, "Error: cannot move opponent's piece");
            return;
        }
        if (chess.getTeamTurn() != playerColor) {
            sendError(ctx, "Error: wrong turn");
            return;
        }
        try {
            chess.makeMove(move);
        } catch (InvalidMoveException e) {
            sendError(ctx, "Error: " + e.getMessage());
            return;
        }
        gameDAO.updateGame(new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), chess));
        game = gameDAO.getGame(gameID);
        broadcastLoadGame(gameID, game.game());
        String moveText = formatMove(move);
        broadcastNotification(gameID, info.username + " moved: " + moveText, ctx);
        ChessGame.TeamColor toMove = game.game().getTeamTurn();
        if (game.game().isInCheckmate(toMove)) {
            String victim = toMove == ChessGame.TeamColor.WHITE ? game.whiteUsername() : game.blackUsername();
            String name = victim != null ? victim : "player";
            broadcastNotification(gameID, name + " is in checkmate", null);
        }
    }

    private void handleLeave(WsMessageContext ctx, JsonObject json) throws DataAccessException {
        SessionInfo info = requireSession(ctx);
        if (info == null) {
            return;
        }
        String authToken = getString(json, "authToken");
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null || !auth.username().equals(info.username)) {
            sendError(ctx, "Error: unauthorized");
            return;
        }
        int gameID = parseGameId(json.get("gameID"));
        if (gameID != info.gameID) {
            sendError(ctx, "Error: wrong game");
            return;
        }
        GameData game = gameDAO.getGame(gameID);
        if (game != null && info.role == PlayerRole.WHITE) {
            gameDAO.updateGame(new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game()));
        } else if (game != null && info.role == PlayerRole.BLACK) {
            gameDAO.updateGame(new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game()));
        }
        removeSession(ctx);
        ctx.attribute("joined", false);
        broadcastNotification(gameID, info.username + " left the game", ctx);
    }

    private void handleResign(WsMessageContext ctx, JsonObject json) throws DataAccessException {
        SessionInfo info = requireSession(ctx);
        if (info == null) {
            return;
        }
        if (info.role == PlayerRole.OBSERVER) {
            sendError(ctx, "Error: observers cannot resign");
            return;
        }
        String authToken = getString(json, "authToken");
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null || !auth.username().equals(info.username)) {
            sendError(ctx, "Error: unauthorized");
            return;
        }
        int gameID = parseGameId(json.get("gameID"));
        if (gameID != info.gameID) {
            sendError(ctx, "Error: wrong game");
            return;
        }
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            sendError(ctx, "Error: game not found");
            return;
        }
        ChessGame chess = game.game();
        if (chess.isGameOver()) {
            sendError(ctx, "Error: game is already over");
            return;
        }
        chess.setGameOver(true);
        gameDAO.updateGame(new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), chess));
        broadcastNotification(gameID, info.username + " resigned", null);
    }

    private SessionInfo requireSession(WsContext ctx) {
        Integer gameID = ctx.attribute("gameID");
        String username = ctx.attribute("username");
        PlayerRole role = ctx.attribute("role");
        if (!Boolean.TRUE.equals(ctx.attribute("joined")) || gameID == null || username == null || role == null) {
            sendError(ctx, "Error: not connected to a game");
            return null;
        }
        return new SessionInfo(gameID, username, role);
    }

    private void registerSession(int gameID, WsContext ctx) {
        sessionsByGame.computeIfAbsent(gameID, g -> ConcurrentHashMap.newKeySet()).add(ctx);
    }

    private void removeSession(WsContext ctx) {
        Integer gameID = ctx.attribute("gameID");
        if (gameID == null) {
            return;
        }
        Set<WsContext> set = sessionsByGame.get(gameID);
        if (set != null) {
            set.remove(ctx);
            if (set.isEmpty()) {
                sessionsByGame.remove(gameID, set);
            }
        }
    }

    private void broadcastLoadGame(int gameID, ChessGame game) {
        String payload = gson.toJson(loadGameMessage(game));
        for (WsContext s : copySessions(gameID)) {
            safeSend(s, payload);
        }
    }

    private void broadcastNotification(int gameID, String text, WsContext except) {
        String payload = gson.toJson(notificationMessage(text));
        for (WsContext s : copySessions(gameID)) {
            if (except != null && s.sessionId().equals(except.sessionId())) {
                continue;
            }
            safeSend(s, payload);
        }
    }

    private Set<WsContext> copySessions(int gameID) {
        Set<WsContext> set = sessionsByGame.get(gameID);
        if (set == null) {
            return Set.of();
        }
        return Set.copyOf(set);
    }

    private static void safeSend(WsContext s, String json) {
        try {
            s.send(json);
        } catch (Exception ignored) {
            // Session may be closed; drop message
        }
    }

    private void sendLoadGame(WsContext ctx, ChessGame game) {
        ctx.send(gson.toJson(loadGameMessage(game)));
    }

    private static ServerMessage loadGameMessage(ChessGame game) {
        return new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, null, null, game);
    }

    private static ServerMessage notificationMessage(String text) {
        return new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, text, null, null);
    }

    private void sendError(WsContext ctx, String message) {
        if (!message.toLowerCase(Locale.ROOT).contains("error")) {
            message = "Error: " + message;
        }
        ctx.send(gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, message, null)));
    }

    private static String getString(JsonObject json, String key) {
        JsonElement e = json.get(key);
        if (e == null || e.isJsonNull()) {
            return null;
        }
        return e.getAsString();
    }

    private static int parseGameId(JsonElement e) {
        if (e == null || e.isJsonNull()) {
            throw new IllegalArgumentException("missing gameID");
        }
        if (e.isJsonPrimitive()) {
            JsonPrimitive p = e.getAsJsonPrimitive();
            if (p.isNumber()) {
                return p.getAsInt();
            }
            if (p.isString()) {
                return Integer.parseInt(p.getAsString());
            }
        }
        throw new IllegalArgumentException("bad gameID");
    }

    private static PlayerRole resolveRole(String username, GameData game) {
        if (username.equals(game.whiteUsername())) {
            return PlayerRole.WHITE;
        }
        if (username.equals(game.blackUsername())) {
            return PlayerRole.BLACK;
        }
        return PlayerRole.OBSERVER;
    }

    private static String formatMove(ChessMove move) {
        if (move == null) {
            return "";
        }
        var a = move.getStartPosition();
        var b = move.getEndPosition();
        return a.getRow() + "," + a.getColumn() + " -> " + b.getRow() + "," + b.getColumn();
    }

    private enum PlayerRole {
        WHITE,
        BLACK,
        OBSERVER
    }

    private record SessionInfo(int gameID, String username, PlayerRole role) {
    }
}
