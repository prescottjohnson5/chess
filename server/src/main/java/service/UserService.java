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
import service.results.RegisterResult;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest request)
            throws BadRequestException, AlreadyTakenException, DataAccessException {
        if (!isValidRegisterRequest(request)) {
            throw new BadRequestException("bad request");
        }
        if (userDAO.getUser(request.username()) != null) {
            throw new AlreadyTakenException("already taken");
        }

        userDAO.createUser(new UserData(request.username(), request.password(), request.email()));
        String authToken = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(authToken, request.username()));
        return new RegisterResult(request.username(), authToken);
    }

    public LoginResult login(LoginRequest request)
            throws BadRequestException, UnauthorizedException, DataAccessException {
        if (!isValidLoginRequest(request)) {
            throw new BadRequestException("bad request");
        }
        UserData user = userDAO.getUser(request.username());
        if (user == null || !user.password().equals(request.password())) {
            throw new UnauthorizedException("unauthorized");
        }

        String authToken = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(authToken, request.username()));
        return new LoginResult(request.username(), authToken);
    }

    public void logout(String authToken) throws UnauthorizedException, DataAccessException {
        if (authToken == null || authToken.isEmpty()) {
            throw new UnauthorizedException("unauthorized");
        }
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }
        authDAO.deleteAuth(authToken);
    }

    public void clear() throws DataAccessException {
        authDAO.clear();
        userDAO.clear();
    }

    private static boolean isValidRegisterRequest(RegisterRequest request) {
        return request != null
                && request.username() != null && !request.username().isEmpty()
                && request.password() != null && !request.password().isEmpty()
                && request.email() != null && !request.email().isEmpty();
    }

    private static boolean isValidLoginRequest(LoginRequest request) {
        return request != null
                && request.username() != null && !request.username().isEmpty()
                && request.password() != null && !request.password().isEmpty();
    }
}