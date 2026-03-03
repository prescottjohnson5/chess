package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import service.exceptions.AlreadyTakenException;
import service.exceptions.BadRequestException;
import service.exceptions.UnauthorizedException;
import service.requests.LoginRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest r)
            throws BadRequestException, AlreadyTakenException, DataAccessException {

        if (r == null ||
                r.username() == null || r.password() == null || r.email() == null ||
                r.username().isEmpty() || r.password().isEmpty() || r.email().isEmpty()) {
            throw new BadRequestException("bad request");
        }

        if (userDAO.getUser(r.username()) != null) {
            throw new AlreadyTakenException("already taken");
        }

        userDAO.createUser(new UserData(r.username(), r.password(), r.email()));

        String token = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token, r.username()));

        return new RegisterResult(r.username(), token);
    }

    public LoginResult login(LoginRequest r)
            throws BadRequestException, UnauthorizedException, DataAccessException {

        if (r == null ||
                r.username() == null || r.password() == null ||
                r.username().isEmpty() || r.password().isEmpty()) {
            throw new BadRequestException("bad request");
        }

        UserData user = userDAO.getUser(r.username());
        if (user == null || !user.password().equals(r.password())) {
            throw new UnauthorizedException("unauthorized");
        }

        String token = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token, r.username()));

        return new LoginResult(r.username(), token);
    }

    public void logout(String authToken) throws UnauthorizedException, DataAccessException {
        if (authToken == null || authToken.isEmpty()) throw new UnauthorizedException("unauthorized");

        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) throw new UnauthorizedException("unauthorized");

        authDAO.deleteAuth(authToken);
    }

    public void clear() throws DataAccessException {
        authDAO.clear();
        userDAO.clear();
    }
}