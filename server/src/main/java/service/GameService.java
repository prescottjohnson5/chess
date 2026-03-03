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

    public CreateGameResult createGame(String authToken, CreateGameRequest r)
            throws UnauthorizedException, BadRequestException, DataAccessException {

        requireAuth(authToken);

        if (r == null || r.gameName() == null || r.gameName().isEmpty()) {
            throw new BadRequestException("bad request");
        }

        GameData toCreate = new GameData(0, null, null, r.gameName(), new ChessGame());
        int id = gameDAO.createGame(toCreate);
        return new CreateGameResult(id);
    }

    public void joinGame(String authToken, JoinGameRequest r)
            throws UnauthorizedException, BadRequestException, AlreadyTakenException, DataAccessException {

        AuthData auth = requireAuth(authToken);
        if (r == null) throw new BadRequestException("bad request");
        if (r.playerColor() == null || r.gameID() == null) throw new BadRequestException("bad request");

        GameData game = gameDAO.getGame(r.gameID());
        if (game == null) throw new BadRequestException("bad request");

        String username = auth.username();

        if (r.playerColor() == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null) throw new AlreadyTakenException("already taken");
            gameDAO.updateGame(new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game()));
        } else {
            if (game.blackUsername() != null) throw new AlreadyTakenException("already taken");
            gameDAO.updateGame(new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game()));
        }
    }

    public void clear() throws DataAccessException {
        gameDAO.clear();
    }

    private AuthData requireAuth(String authToken) throws UnauthorizedException, DataAccessException {
        if (authToken == null || authToken.isEmpty()) throw new UnauthorizedException("unauthorized");
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) throw new UnauthorizedException("unauthorized");
        return auth;
    }
}