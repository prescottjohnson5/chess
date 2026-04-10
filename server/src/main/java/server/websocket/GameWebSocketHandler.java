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

import static chess.ChessGame.TeamColor;
import static websocket.messages.ServerMessage.ServerMessageType.ERROR;
import static websocket.messages.ServerMessage.ServerMessageType.LOAD_GAME;
import static websocket.messages.ServerMessage.ServerMessageType.NOTIFICATION;

public class GameWebSocketHandler {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final Gson gson = new Gson();
    private final ConcurrentHashMap<Integer, Set<WsContext>> byGame = new ConcurrentHashMap<>();

    public GameWebSocketHandler(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void onMessage(WsMessageContext ctx) {
        try {
            JsonObject json = JsonParser.parseString(ctx.message()).getAsJsonObject();
            switch (json.get("commandType").getAsString()) {
                case "CONNECT" -> connect(ctx, json);
                case "MAKE_MOVE" -> move(ctx, json);
                case "LEAVE" -> leave(ctx, json);
                case "RESIGN" -> resign(ctx, json);
                default -> err(ctx, "Error: unknown command");
            }
        } catch (DataAccessException e) {
            err(ctx, "Error: " + e.getMessage());
        } catch (RuntimeException e) {
            err(ctx, "Error: bad request");
        }
    }

    public void onClose(WsCloseContext ctx) {
        unregister(ctx);
    }

    private void connect(WsMessageContext ctx, JsonObject json) throws DataAccessException {
        if (Boolean.TRUE.equals(ctx.attribute("joined"))) {
            err(ctx, "Error: already connected to a game");
            return;
        }
        AuthData auth = authDAO.getAuth(str(json, "authToken"));
        if (auth == null) {
            err(ctx, "Error: unauthorized");
            return;
        }
        int gameID = gameId(json.get("gameID"));
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            err(ctx, "Error: game not found");
            return;
        }
        TeamColor side = side(auth.username(), game);
        byGame.computeIfAbsent(gameID, g -> ConcurrentHashMap.newKeySet()).add(ctx);
        ctx.attribute("joined", true);
        ctx.attribute("gameID", gameID);
        ctx.attribute("username", auth.username());
        ctx.attribute("side", side);
        ctx.send(gson.toJson(new ServerMessage(LOAD_GAME, null, null, game.game())));
        String who = auth.username();
        String msg = side == TeamColor.WHITE ? who + " joined as white"
                : side == TeamColor.BLACK ? who + " joined as black"
                : who + " joined as observer";
        notifyOthers(gameID, msg, ctx);
    }

    private void move(WsMessageContext ctx, JsonObject json) throws DataAccessException {
        Conn c = conn(ctx);
        if (c == null) {
            return;
        }
        if (c.side == null) {
            err(ctx, "Error: observers cannot move");
            return;
        }
        if (badAuthOrGame(ctx, json, c)) {
            return;
        }
        if (!json.has("move") || json.get("move").isJsonNull()) {
            err(ctx, "Error: missing move");
            return;
        }
        ChessMove mv = gson.fromJson(json.get("move"), ChessMove.class);
        GameData row = gameDAO.getGame(c.gameID);
        if (row == null) {
            err(ctx, "Error: game not found");
            return;
        }
        ChessGame g = row.game();
        var piece = g.getBoard().getPiece(mv.getStartPosition());
        if (piece == null) {
            err(ctx, "Error: no piece at start");
            return;
        }
        if (piece.getTeamColor() != c.side || g.getTeamTurn() != c.side) {
            err(ctx, "Error: wrong turn");
            return;
        }
        try {
            g.makeMove(mv);
        } catch (InvalidMoveException e) {
            err(ctx, "Error: " + e.getMessage());
            return;
        }
        gameDAO.updateGame(new GameData(row.gameID(), row.whiteUsername(), row.blackUsername(), row.gameName(), g));
        row = gameDAO.getGame(c.gameID);
        ChessGame updated = row.game();
        broadcast(c.gameID, new ServerMessage(LOAD_GAME, null, null, updated), null);
        notifyOthers(c.gameID, c.username + " moved: " + moveStr(mv), ctx);
        TeamColor next = updated.getTeamTurn();
        String nextName = next == TeamColor.WHITE ? row.whiteUsername() : row.blackUsername();
        String nextLabel = nextName != null ? nextName : "player";
        if (updated.isInCheckmate(next)) {
            updated.setGameOver(true);
            gameDAO.updateGame(new GameData(row.gameID(), row.whiteUsername(), row.blackUsername(), row.gameName(), updated));
            broadcast(c.gameID, new ServerMessage(NOTIFICATION, nextLabel + " is in checkmate", null, null), null);
        } else if (updated.isInStalemate(next)) {
            updated.setGameOver(true);
            gameDAO.updateGame(new GameData(row.gameID(), row.whiteUsername(), row.blackUsername(), row.gameName(), updated));
            broadcast(c.gameID, new ServerMessage(NOTIFICATION, nextLabel + " is in stalemate", null, null), null);
        } else if (updated.isInCheck(next)) {
            broadcast(c.gameID, new ServerMessage(NOTIFICATION, nextLabel + " is in check", null, null), null);
        }
    }

    private void leave(WsMessageContext ctx, JsonObject json) throws DataAccessException {
        Conn c = conn(ctx);
        if (c == null) {
            return;
        }
        if (badAuthOrGame(ctx, json, c)) {
            return;
        }
        GameData row = gameDAO.getGame(c.gameID);
        if (row != null) {
            if (c.side == TeamColor.WHITE) {
                gameDAO.updateGame(new GameData(row.gameID(), null, row.blackUsername(), row.gameName(), row.game()));
            } else if (c.side == TeamColor.BLACK) {
                gameDAO.updateGame(new GameData(row.gameID(), row.whiteUsername(), null, row.gameName(), row.game()));
            }
        }
        unregister(ctx);
        ctx.attribute("joined", false);
        notifyOthers(c.gameID, c.username + " left the game", ctx);
    }

    private void resign(WsMessageContext ctx, JsonObject json) throws DataAccessException {
        Conn c = conn(ctx);
        if (c == null) {
            return;
        }
        if (c.side == null) {
            err(ctx, "Error: observers cannot resign");
            return;
        }
        if (badAuthOrGame(ctx, json, c)) {
            return;
        }
        GameData row = gameDAO.getGame(c.gameID);
        if (row == null) {
            err(ctx, "Error: game not found");
            return;
        }
        ChessGame g = row.game();
        if (g.isGameOver()) {
            err(ctx, "Error: game is already over");
            return;
        }
        g.setGameOver(true);
        gameDAO.updateGame(new GameData(row.gameID(), row.whiteUsername(), row.blackUsername(), row.gameName(), g));
        broadcast(c.gameID, new ServerMessage(NOTIFICATION, c.username + " resigned", null, null), null);
    }

    private boolean badAuthOrGame(WsContext ctx, JsonObject json, Conn c) throws DataAccessException {
        AuthData a = authDAO.getAuth(str(json, "authToken"));
        if (a == null || !a.username().equals(c.username)) {
            err(ctx, "Error: unauthorized");
            return true;
        }
        if (gameId(json.get("gameID")) != c.gameID) {
            err(ctx, "Error: wrong game");
            return true;
        }
        return false;
    }

    private Conn conn(WsContext ctx) {
        if (!Boolean.TRUE.equals(ctx.attribute("joined"))) {
            err(ctx, "Error: not connected to a game");
            return null;
        }
        Integer gid = ctx.attribute("gameID");
        String user = ctx.attribute("username");
        if (gid == null || user == null) {
            err(ctx, "Error: not connected to a game");
            return null;
        }
        return new Conn(gid, user, ctx.attribute("side"));
    }

    private void broadcast(int gameID, ServerMessage msg, WsContext skip) {
        String text = gson.toJson(msg);
        Set<WsContext> set = byGame.get(gameID);
        if (set == null) {
            return;
        }
        for (WsContext s : Set.copyOf(set)) {
            if (skip != null && skip.sessionId().equals(s.sessionId())) {
                continue;
            }
            try {
                s.send(text);
            } catch (Exception ignored) {
            }
        }
    }

    private void notifyOthers(int gameID, String text, WsContext except) {
        broadcast(gameID, new ServerMessage(NOTIFICATION, text, null, null), except);
    }

    private void unregister(WsContext ctx) {
        Integer gid = ctx.attribute("gameID");
        if (gid == null) {
            return;
        }
        Set<WsContext> set = byGame.get(gid);
        if (set != null) {
            set.remove(ctx);
            if (set.isEmpty()) {
                byGame.remove(gid, set);
            }
        }
    }

    private void err(WsContext ctx, String m) {
        if (!m.toLowerCase(Locale.ROOT).contains("error")) {
            m = "Error: " + m;
        }
        ctx.send(gson.toJson(new ServerMessage(ERROR, null, m, null)));
    }

    private static String str(JsonObject json, String key) {
        JsonElement e = json.get(key);
        return e == null || e.isJsonNull() ? null : e.getAsString();
    }

    private static int gameId(JsonElement e) {
        if (e == null || e.isJsonNull()) {
            throw new IllegalArgumentException("gameID");
        }
        JsonPrimitive p = e.getAsJsonPrimitive();
        return p.isNumber() ? p.getAsInt() : Integer.parseInt(p.getAsString());
    }

    private static TeamColor side(String username, GameData game) {
        if (username.equals(game.whiteUsername())) {
            return TeamColor.WHITE;
        }
        if (username.equals(game.blackUsername())) {
            return TeamColor.BLACK;
        }
        return null;
    }

    private static String moveStr(ChessMove m) {
        var a = m.getStartPosition();
        var b = m.getEndPosition();
        return a.getRow() + "," + a.getColumn() + " -> " + b.getRow() + "," + b.getColumn();
    }

    private record Conn(int gameID, String username, TeamColor side) {
    }
}
