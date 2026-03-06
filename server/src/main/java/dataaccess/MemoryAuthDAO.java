package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO {
    private final Map<String, AuthData> tokens = new HashMap<>();

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        if (auth == null) {
            throw new DataAccessException("auth cannot be null");
        }
        tokens.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return tokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        tokens.remove(authToken);
    }

    @Override
    public void clear() throws DataAccessException {
        tokens.clear();
    }
}