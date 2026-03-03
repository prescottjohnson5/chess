package dataaccess;

import model.GameData;

import java.util.*;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextId = 1;

    @Override
    public int createGame(GameData game) throws DataAccessException {
        if (game == null) throw new DataAccessException("game cannot be null");
        int id = nextId++;
        GameData stored = new GameData(id, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        games.put(id, stored);
        return id;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> getAllGames() throws DataAccessException {
        return new ArrayList<>(games.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) throw new DataAccessException("game cannot be null");
        if (!games.containsKey(game.gameID())) throw new DataAccessException("game does not exist");
        games.put(game.gameID(), game);
    }

    @Override
    public void clear() throws DataAccessException {
        games.clear();
        nextId = 1;
    }
}