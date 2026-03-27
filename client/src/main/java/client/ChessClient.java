package client;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import ui.BoardRenderer;

import java.util.Scanner;

public class ChessClient {

    private final ServerFacade facade;
    private final Scanner scanner;

    private AuthData auth;
    private GameData[] lastListedGames;

    public ChessClient(ServerFacade facade) {
        this.facade = facade;
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
                    if (auth != null) {
                        postloginUi();
                        if (auth != null) {
                            return;
                        }
                    }
                }
                case "register" -> {
                    auth = attemptRegister();
                    if (auth != null) {
                        postloginUi();
                        if (auth != null) {
                            return;
                        }
                    }
                }
                default -> System.out.println("Unknown command! Type `help` for commands");
            }
        }
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
            BoardRenderer.drawInitialBoard(selected.game(), team);
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
        System.out.println("Joined game successfully!");
        BoardRenderer.drawInitialBoard(selected.game(), ChessGame.TeamColor.WHITE);
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
}

