package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import org.glassfish.tyrus.client.ClientManager;
import ui.BoardRenderer;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Supplier;

public final class GameplayUi {

    private final Scanner scanner;
    private final Gson gson;
    private final String wsHost;
    private final int wsPort;
    private final Supplier<String> authToken;

    public GameplayUi(Scanner scanner, Gson gson, String wsHost, int wsPort, Supplier<String> authToken) {
        this.scanner = scanner;
        this.gson = gson;
        this.wsHost = wsHost;
        this.wsPort = wsPort;
        this.authToken = authToken;
    }

    public void run(int gameId, ChessGame.TeamColor perspective) {
        Session[] sock = { null };
        ChessGame[] board = { null };
        HighlightState highlight = new HighlightState();
        String url = "ws://" + wsHost + ":" + wsPort + "/ws";
        try {
            ClientManager.createClient().connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session s, EndpointConfig c) {
                    sock[0] = s;
                    s.addMessageHandler(String.class, j -> onWsMessage(j, board, perspective, highlight));
                    wsSend(s, "CONNECT", gameId, null);
                }
            }, ClientEndpointConfig.Builder.create().build(), URI.create(url));
        } catch (Exception e) {
            System.out.println("WebSocket: " + e.getMessage());
            return;
        }
        printGameplayHelp();
        for (boolean done = false; !done; ) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine();
            if (line == null) {
                break;
            }
            String[] p = line.trim().toLowerCase().split("\\s+");
            if (p.length == 0 || p[0].isEmpty()) {
                continue;
            }
            switch (p[0]) {
                case "help" -> printGameplayHelp();
                case "redraw" -> redrawGameplay(board[0], perspective, highlight);
                case "leave" -> {
                    wsSend(sock[0], "LEAVE", gameId, null);
                    done = true;
                }
                case "move" -> tryMove(sock[0], gameId, p);
                case "highlight" -> tryHighlight(board[0], perspective, p, highlight);
                case "resign" -> tryResign(sock[0], gameId);
                case "quit", "exit" -> System.out.println("Use `leave` first, then `quit` from the main menu.");
                default -> System.out.println("Unknown command. Type `help`.");
            }
        }
        closeQuietly(sock[0]);
        System.out.println("Left the game.");
    }

    private static final class HighlightState {
        ChessPosition from;
        Set<ChessPosition> to = Collections.emptySet();
    }

    private void redrawGameplay(ChessGame game, ChessGame.TeamColor perspective, HighlightState hi) {
        if (game == null) {
            System.out.println("No board yet.");
            return;
        }
        BoardRenderer.drawBoard(game, perspective, hi.from, hi.to);
    }

    private void onWsMessage(String json, ChessGame[] board, ChessGame.TeamColor perspective, HighlightState hi) {
        try {
            JsonObject o = JsonParser.parseString(json).getAsJsonObject();
            switch (o.get("serverMessageType").getAsString()) {
                case "LOAD_GAME" -> {
                    board[0] = gson.fromJson(o.get("game"), ChessGame.class);
                    hi.from = null;
                    hi.to = Collections.emptySet();
                    redrawGameplay(board[0], perspective, hi);
                }
                case "NOTIFICATION" -> System.out.println(o.get("message").getAsString());
                case "ERROR" -> System.out.println(o.get("errorMessage").getAsString());
                default -> {
                }
            }
        } catch (RuntimeException e) {
            System.out.println("Bad server message.");
        }
    }

    private void tryHighlight(ChessGame game, ChessGame.TeamColor perspective, String[] p, HighlightState hi) {
        if (game == null) {
            System.out.println("No board yet.");
            return;
        }
        if (p.length != 3) {
            System.out.println("Usage: highlight <row> <col> (1-8)");
            return;
        }
        try {
            int row = Integer.parseInt(p[1]);
            int col = Integer.parseInt(p[2]);
            ChessPosition at = new ChessPosition(row, col);
            if (game.getBoard().getPiece(at) == null) {
                System.out.println("No piece on that square.");
                return;
            }
            Set<ChessPosition> ends = new HashSet<>();
            for (ChessMove m : game.validMoves(at)) {
                ends.add(m.getEndPosition());
            }
            hi.from = at;
            hi.to = ends;
            BoardRenderer.drawBoard(game, perspective, hi.from, hi.to);
        } catch (NumberFormatException e) {
            System.out.println("highlight needs two integers.");
        }
    }

    private void tryResign(Session s, int gameId) {
        System.out.print("Resign this game? Type 'yes' to confirm: ");
        if (!scanner.hasNextLine()) {
            return;
        }
        String ans = scanner.nextLine().trim().toLowerCase();
        if (ans.equals("yes") || ans.equals("y")) {
            wsSend(s, "RESIGN", gameId, null);
        } else {
            System.out.println("Resign cancelled.");
        }
    }

    private String wsJson(String commandType, int gameId, ChessMove move) {
        JsonObject o = new JsonObject();
        o.addProperty("commandType", commandType);
        o.addProperty("authToken", authToken.get());
        o.addProperty("gameID", gameId);
        if (move != null) {
            o.add("move", gson.toJsonTree(move));
        }
        return gson.toJson(o);
    }

    private void wsSend(Session s, String commandType, int gameId, ChessMove move) {
        if (s == null || !s.isOpen()) {
            System.out.println("Not connected.");
            return;
        }
        try {
            s.getBasicRemote().sendText(wsJson(commandType, gameId, move));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void tryMove(Session s, int gameId, String[] p) {
        if (p.length != 5) {
            System.out.println("Usage: move <sr> <sc> <er> <ec> (rows/cols 1-8)");
            return;
        }
        try {
            int sr = Integer.parseInt(p[1]);
            int sc = Integer.parseInt(p[2]);
            int er = Integer.parseInt(p[3]);
            int ec = Integer.parseInt(p[4]);
            wsSend(s, "MAKE_MOVE", gameId, new ChessMove(new ChessPosition(sr, sc), new ChessPosition(er, ec), null));
        } catch (NumberFormatException e) {
            System.out.println("Move needs four integers.");
        }
    }

    private static void closeQuietly(Session s) {
        try {
            if (s != null && s.isOpen()) {
                s.close();
            }
        } catch (Exception ignored) {
        }
    }

    private void printGameplayHelp() {
        System.out.println("Gameplay commands:");
        System.out.println("  help              List these commands");
        System.out.println("  redraw            Redraw the board (keeps move highlights if any)");
        System.out.println("  move r0 c0 r1 c1  Move from square (row,col) to (row,col), using 1-8");
        System.out.println("  highlight r c     Show legal moves for the piece on that square (local only)");
        System.out.println("  resign            Forfeit; asks for confirmation before sending");
        System.out.println("  leave             Leave the game and return to the main menu");
        System.out.println("  quit / exit       Reminder: leave the game first, then quit from the menu");
    }
}
