package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.exceptions.AlreadyTakenException;
import service.exceptions.BadRequestException;
import service.exceptions.UnauthorizedException;
import service.requests.LoginRequest;
import service.requests.RegisterRequest;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTests {

    private UserService userService;
    private MemoryUserDAO userDAO;
    private MemoryAuthDAO authDAO;

    @BeforeEach
    void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    // ---------- register ----------

    @Test
    @DisplayName("Register success")
    void registerSuccess() throws DataAccessException, BadRequestException, AlreadyTakenException {
        RegisterRequest request = new RegisterRequest("alice", "pass123", "alice@test.com");
        var result = userService.register(request);

        assertNotNull(result);
        assertEquals("alice", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isEmpty());
        assertNotNull(userDAO.getUser("alice"));
        assertNotNull(authDAO.getAuth(result.authToken()));
    }

    @Test
    @DisplayName("Register bad request - null username")
    void registerBadRequestNullUsername() {
        RegisterRequest request = new RegisterRequest(null, "pass", "a@b.com");
        assertThrows(BadRequestException.class, () -> userService.register(request));
    }

    @Test
    @DisplayName("Register bad request - empty password")
    void registerBadRequestEmptyPassword() {
        RegisterRequest request = new RegisterRequest("alice", "", "alice@test.com");
        assertThrows(BadRequestException.class, () -> userService.register(request));
    }

    @Test
    @DisplayName("Register already taken")
    void registerAlreadyTaken() throws DataAccessException, BadRequestException, AlreadyTakenException {
        userService.register(new RegisterRequest("alice", "pass", "alice@test.com"));
        RegisterRequest duplicate = new RegisterRequest("alice", "other", "other@test.com");
        assertThrows(AlreadyTakenException.class, () -> userService.register(duplicate));
    }

    // ---------- login ----------

    @Test
    @DisplayName("Login success")
    void loginSuccess() throws DataAccessException, BadRequestException, AlreadyTakenException, UnauthorizedException {
        userService.register(new RegisterRequest("bob", "secret", "bob@test.com"));
        LoginRequest request = new LoginRequest("bob", "secret");
        var result = userService.login(request);

        assertNotNull(result);
        assertEquals("bob", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isEmpty());
    }

    @Test
    @DisplayName("Login bad request - null password")
    void loginBadRequest() {
        LoginRequest request = new LoginRequest("bob", null);
        assertThrows(BadRequestException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("Login unauthorized - wrong password")
    void loginUnauthorizedWrongPassword() throws DataAccessException, BadRequestException, AlreadyTakenException {
        userService.register(new RegisterRequest("bob", "secret", "bob@test.com"));
        LoginRequest request = new LoginRequest("bob", "wrong");
        assertThrows(UnauthorizedException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("Login unauthorized - unknown user")
    void loginUnauthorizedUnknownUser() {
        LoginRequest request = new LoginRequest("nobody", "pass");
        assertThrows(UnauthorizedException.class, () -> userService.login(request));
    }

    // ---------- logout ----------

    @Test
    @DisplayName("Logout success")
    void logoutSuccess() throws DataAccessException, BadRequestException, AlreadyTakenException, UnauthorizedException {
        var reg = userService.register(new RegisterRequest("alice", "pass", "a@b.com"));
        userService.logout(reg.authToken());
        assertNull(authDAO.getAuth(reg.authToken()));
    }

    @Test
    @DisplayName("Logout unauthorized - invalid token")
    void logoutUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> userService.logout("invalid-token"));
    }

    @Test
    @DisplayName("Logout unauthorized - null token")
    void logoutUnauthorizedNullToken() {
        assertThrows(UnauthorizedException.class, () -> userService.logout(null));
    }

    // ---------- clear ----------

    @Test
    @DisplayName("Clear success")
    void clearSuccess() throws DataAccessException, BadRequestException, AlreadyTakenException {
        userService.register(new RegisterRequest("alice", "pass", "a@b.com"));
        userService.clear();
        assertNull(userDAO.getUser("alice"));
    }
}
