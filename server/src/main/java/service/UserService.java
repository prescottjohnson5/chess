package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
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
        if (user == null || !passwordMatches(request.password(), user.password())) {
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

    private static boolean passwordMatches(String providedClearTextPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        boolean looksLikeBcrypt = storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$");
        if (looksLikeBcrypt) {
            return BCrypt.checkpw(providedClearTextPassword, storedPassword);
        }
        return storedPassword.equals(providedClearTextPassword);
    }
}