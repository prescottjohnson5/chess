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
import model.AuthData;
import model.GameData;
import org.glassfish.tyrus.client.ClientManager;
import ui.BoardRenderer;

import java.net.URI;
import java.util.Scanner;

public class ChessClient {

    private final ServerFacade facade;
    private final String wsHost;
    private final int wsPort;
    private final Scanner scanner;
    private final Gson gson = new Gson();

    private AuthData auth;
    private GameData[] lastListedGames;

    public ChessClient(ServerFacade facade, String host, int port) {
        this.facade = facade;
        this.wsHost = host;
        this.wsPort = port;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        auth = null;
        lastListedGames = null;
        preloginUi();
    }

    private void preloginUi() {
        System.out.println("Welcome to 240 Chess Client.");
        printPreloginHelp();

        while (true) {
            System.out.print("> ");
            String command = readCommand();
            if (command == null) {
                continue;
            }

            if (isExitCommand(command)) {
                System.out.println("Goodbye.");
                return;
            }

            switch (command) {
                case "help" -> printPreloginHelp();
                case "login" -> {
                    auth = attemptLogin();
                    if (auth != null && postloginAndExitIfStillAuthed()) {
                        return;
                    }
                }
                case "register" -> {
                    auth = attemptRegister();
                    if (auth != null && postloginAndExitIfStillAuthed()) {
                        return;
                    }
                }
                default -> System.out.println("Unknown command! Type `help` for commands");
            }
        }
    }

    /**
     * Runs postlogin UI; returns true if the user quit the client from postlogin while still logged in.
     */
    private boolean postloginAndExitIfStillAuthed() {
        postloginUi();
        return auth != null;
    }

    private void postloginUi() {
        printPostloginHelp();
        lastListedGames = null;

        while (true) {
            System.out.print("> ");
            String command = readCommand();
            if (command == null) {
                continue;
            }

            if (isExitCommand(command)) {
                System.out.println("Thank you for playing!");
                return;
            }

            switch (command) {
                case "help" -> printPostloginHelp();
                case "logout" -> {
                    if (attemptLogout()) {
                        auth = null;
                        return;
                    }
                }
                case "create" -> createGameUi();
                case "list" -> listGamesUi();
                case "play" -> playGameUi();
                case "observe" -> observeGameUi();
                default -> System.out.println("Unknown command! Type `help` for commands");
            }
        }
    }

    private void printPreloginHelp() {
        System.out.println("Prelogin commands: ");
        System.out.println("  help                 Shows all commands");
        System.out.println("  login                Log in to an existing account");
        System.out.println("  register             Register a new account");
        System.out.println("  quit                 Exit the client");
        System.out.println("  exit                 Exit the client");
    }

    private void printPostloginHelp() {
        System.out.println("Postlogin commands: ");
        System.out.println("  help                 Show commands");
        System.out.println("  logout               Log out");
        System.out.println("  create               Create a new game");
        System.out.println("  list                 List existing games");
        System.out.println("  play                 Join an exisiting game");
        System.out.println("  observe              Observe an existing game");
        System.out.println("  quit                 Exit the client");
        System.out.println("  exit                 Exit the client");
    }

    private boolean isExitCommand(String command) {
        return command != null && (command.equals("quit") || command.equals("exit"));
    }

    private String readCommand() {
        if (!scanner.hasNextLine()) {
            return null;
        }
        String line = scanner.nextLine();
        if (line == null) {
            return null;
        }
        String trimmed = line.trim().toLowerCase();
        if (trimmed.isEmpty()) {
            return null;
        }
        String[] parts = trimmed.split("\\s+");
        return parts.length == 0 ? null : parts[0];
    }

    private AuthData attemptLogin() {
        System.out.print("Username: ");
        String username = readLineRequired();
        if (username == null) {
            return null;
        }

        System.out.print("Password: ");
        String password = readLineRequired();
        if (password == null) {
            return null;
        }

        try {
            return facade.login(username, password);
        } catch (ServerFacadeException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private AuthData attemptRegister() {
        System.out.print("Username: ");
        String username = readLineRequired();
        if (username == null) {
            return null;
        }

        System.out.print("Password: ");
        String password = readLineRequired();
        if (password == null) {
            return null;
        }

        System.out.print("Email: ");
        String email = readLineRequired();
        if (email == null) {
            return null;
        }

        try {
            return facade.register(username, password, email);
        } catch (ServerFacadeException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private boolean attemptLogout() {
        try {
            facade.logout(auth.authToken());
            System.out.println("Logged out.");
            return true;
        } catch (ServerFacadeException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private void createGameUi() {
        System.out.print("Game name: ");
        String gameName = readLineRequired();
        if (gameName == null) {
            return;
        }
        try {
            facade.createGame(auth.authToken(), gameName);
            System.out.println("Created the game.");
        } catch (ServerFacadeException e) {
            System.out.println(e.getMessage());
        }
        lastListedGames = null;
    }

    private void listGamesUi() {
        try {
            lastListedGames = facade.listGames(auth.authToken());
            if (lastListedGames.length == 0) {
                System.out.println("No games currently on the server.");
                return;
            }

            for (int i = 0; i < lastListedGames.length; i++) {
                GameData g = lastListedGames[i];
                String white = g.whiteUsername() == null ? "-" : g.whiteUsername();
                String black = g.blackUsername() == null ? "-" : g.blackUsername();
                System.out.println((i + 1) + ") " + g.gameName() + "  [White: " + white + "] [Black: " + black + "]");
            }
        } catch (ServerFacadeException e) {
            System.out.println(e.getMessage());
        }
    }

    private void playGameUi() {
        if (lastListedGames == null || lastListedGames.length == 0) {
            System.out.println("List games first using `list`.");
            return;
        }
        int gameIndex = readGameNumber(lastListedGames.length);
        if (gameIndex < 1) {
            return;
        }
        GameData selected = lastListedGames[gameIndex - 1];
        System.out.print("Play as (white/black): ");
        String color = readCommand();
        ChessGame.TeamColor team = parseTeamColor(color);
        if (team == null) {
            System.out.println("Invalid color. Type `play` again.");
            return;
        }

        try {
            facade.joinGame(auth.authToken(), team, selected.gameID());
            System.out.println("Joined game successfully!");
            gameplayUi(selected.gameID(), team);
        } catch (ServerFacadeException e) {
            System.out.println(e.getMessage());
        }
    }

    private void observeGameUi() {
        if (lastListedGames == null || lastListedGames.length == 0) {
            System.out.println("List games first using `list`.");
            return;
        }
        int gameIndex = readGameNumber(lastListedGames.length);
        if (gameIndex < 1) {
            return;
        }
        GameData selected = lastListedGames[gameIndex - 1];
        System.out.println("Observing game (WebSocket)…");
        gameplayUi(selected.gameID(), ChessGame.TeamColor.WHITE);
    }

    private int readGameNumber(int max) {
        System.out.print("Game number (1-" + max + "): ");
        String line = readLineRequired();
        if (line == null) {
            return -1;
        }
        try {
            int n = Integer.parseInt(line.trim());
            if (n < 1 || n > max) {
                System.out.println("That game number is out of range.");
                return -1;
            }
            return n;
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            return -1;
        }
    }

    private String readLineRequired() {
        if (!scanner.hasNextLine()) {
            return null;
        }
        String line = scanner.nextLine();
        if (line == null) {
            return null;
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            System.out.println("Input cannot be empty.");
            return null;
        }
        return trimmed;
    }

    private ChessGame.TeamColor parseTeamColor(String color) {
        if (color == null) {
            return null;
        }
        if (color.equals("white") || color.equals("w")) {
            return ChessGame.TeamColor.WHITE;
        }
        if (color.equals("black") || color.equals("b")) {
            return ChessGame.TeamColor.BLACK;
        }
        return null;
    }

    private void gameplayUi(int gameId, ChessGame.TeamColor perspective) {
        Session[] sock = { null };
        ChessGame[] board = { null };
        String url = "ws://" + wsHost + ":" + wsPort + "/ws";
        try {
            ClientManager.createClient().connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session s, EndpointConfig c) {
                    sock[0] = s;
                    s.addMessageHandler(String.class, j -> onWsMessage(j, board, perspective));
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
                case "redraw" -> {
                    if (board[0] == null) {
                        System.out.println("No board yet.");
                    } else {
                        BoardRenderer.drawInitialBoard(board[0], perspective);
                    }
                }
                case "leave" -> {
                    wsSend(sock[0], "LEAVE", gameId, null);
                    done = true;
                }
                case "move" -> tryMove(sock[0], gameId, p);
                case "quit", "exit" -> System.out.println("Use `leave` first, then `quit` from the main menu.");
                default -> System.out.println("Unknown command. Type `help`.");
            }
        }
        closeQuietly(sock[0]);
        System.out.println("Left the game.");
    }

    private void onWsMessage(String json, ChessGame[] board, ChessGame.TeamColor perspective) {
        try {
            JsonObject o = JsonParser.parseString(json).getAsJsonObject();
            switch (o.get("serverMessageType").getAsString()) {
                case "LOAD_GAME" -> {
                    board[0] = gson.fromJson(o.get("game"), ChessGame.class);
                    BoardRenderer.drawInitialBoard(board[0], perspective);
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

    private String wsJson(String commandType, int gameId, ChessMove move) {
        JsonObject o = new JsonObject();
        o.addProperty("commandType", commandType);
        o.addProperty("authToken", auth.authToken());
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
        System.out.println("Gameplay: help | redraw | move sr sc er ec | leave");
    }
}

