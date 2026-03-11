package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class MySqlUserDAOTests {

    private MySqlUserDAO userDAO;
    private MySqlAuthDAO authDAO;
    private MySqlGameDAO gameDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        userDAO = new MySqlUserDAO();
        authDAO = new MySqlAuthDAO();
        gameDAO = new MySqlGameDAO();
        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();
    }

    @Test
    @DisplayName("Create user success")
    void createUserSuccess() throws DataAccessException {
        UserData user = new UserData("alice", "password", "a@test.com");
        userDAO.createUser(user);

        UserData stored = userDAO.getUser("alice");
        assertNotNull(stored);
        assertEquals("alice", stored.username());
        assertEquals("a@test.com", stored.email());
        assertNotNull(stored.password());
        assertNotEquals("password", stored.password(), "Password should not be stored in clear text");
    }

    @Test
    @DisplayName("Create user null fails")
    void createUserNullFails() {
        assertThrows(DataAccessException.class, () -> userDAO.createUser(null));
    }

    @Test
    @DisplayName("Create user duplicate fails")
    void createUserDuplicateFails() throws DataAccessException {
        UserData user = new UserData("alice", "password", "a@test.com");
        userDAO.createUser(user);
        assertThrows(DataAccessException.class, () -> userDAO.createUser(user));
    }

    @Test
    @DisplayName("Get user success")
    void getUserSuccess() throws DataAccessException {
        userDAO.createUser(new UserData("bob", "pw", "b@test.com"));
        UserData stored = userDAO.getUser("bob");
        assertNotNull(stored);
        assertEquals("bob", stored.username());
    }

    @Test
    @DisplayName("Get user missing returns null")
    void getUserMissingReturnsNull() throws DataAccessException {
        UserData result = userDAO.getUser("missing");
        assertNull(result);
    }

    @Test
    @DisplayName("Get user null username returns null")
    void getUserNullReturnsNull() throws DataAccessException {
        assertNull(userDAO.getUser(null));
    }

    @Test
    @DisplayName("Clear users success")
    void clearUsersSuccess() throws DataAccessException {
        userDAO.createUser(new UserData("c", "pw", "c@test.com"));
        userDAO.clear();
        assertNull(userDAO.getUser("c"));
    }

    @Test
    @DisplayName("Clear when empty does not throw")
    void clearWhenEmptySucceeds() throws DataAccessException {
        userDAO.clear();
        assertDoesNotThrow(() -> userDAO.clear());
        userDAO.createUser(new UserData("after", "pw", "e@test.com"));
        assertNotNull(userDAO.getUser("after"));
    }
}

