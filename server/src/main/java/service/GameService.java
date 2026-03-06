package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import service.exceptions.AlreadyTakenException;
import service.exceptions.BadRequestException;
import service.exceptions.UnauthorizedException;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.results.CreateGameResult;
import service.results.ListGamesResult;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public ListGamesResult listGames(String authToken) throws UnauthorizedException, DataAccessException {
        requireAuth(authToken);
        GameData[] games = gameDAO.getAllGames().toArray(new GameData[0]);
        return new ListGamesResult(games);
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest request)
            throws UnauthorizedException, BadRequestException, DataAccessException {
        requireAuth(authToken);
        if (!isValidCreateGameRequest(request)) {
            throw new BadRequestException("bad request");
        }

        GameData newGame = new GameData(0, null, null, request.gameName(), new ChessGame());
        int gameId = gameDAO.createGame(newGame);
        return new CreateGameResult(gameId);
    }

    public void joinGame(String authToken, JoinGameRequest request)
            throws UnauthorizedException, BadRequestException, AlreadyTakenException, DataAccessException {
        AuthData auth = requireAuth(authToken);
        if (!isValidJoinGameRequest(request)) {
            throw new BadRequestException("bad request");
        }

        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new BadRequestException("bad request");
        }

        String username = auth.username();
        if (request.playerColor() == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null) {
                throw new AlreadyTakenException("already taken");
            }
            gameDAO.updateGame(new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game()));
        } else {
            if (game.blackUsername() != null) {
                throw new AlreadyTakenException("already taken");
            }
            gameDAO.updateGame(new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game()));
        }
    }

    public void clear() throws DataAccessException {
        gameDAO.clear();
    }

    private AuthData requireAuth(String authToken) throws UnauthorizedException, DataAccessException {
        if (authToken == null || authToken.isEmpty()) {
            throw new UnauthorizedException("unauthorized");
        }
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }
        return auth;
    }

    private static boolean isValidCreateGameRequest(CreateGameRequest request) {
        return request != null
                && request.gameName() != null
                && !request.gameName().isEmpty();
    }

    private static boolean isValidJoinGameRequest(JoinGameRequest request) {
        return request != null
                && request.playerColor() != null
                && request.gameID() != null;
    }
}