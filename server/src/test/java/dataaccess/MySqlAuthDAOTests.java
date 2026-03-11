package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class MySqlAuthDAOTests {

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

        userDAO.createUser(new UserData("alice", "pw", "a@test.com"));
    }

    @Test
    @DisplayName("Create auth success")
    void createAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("token-1", "alice");
        authDAO.createAuth(auth);

        AuthData stored = authDAO.getAuth("token-1");
        assertNotNull(stored);
        assertEquals("token-1", stored.authToken());
        assertEquals("alice", stored.username());
    }

    @Test
    @DisplayName("Create auth duplicate fails")
    void createAuthDuplicateFails() throws DataAccessException {
        AuthData auth = new AuthData("token-1", "alice");
        authDAO.createAuth(auth);
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(auth));
    }

    @Test
    @DisplayName("Get auth missing returns null")
    void getAuthMissingReturnsNull() throws DataAccessException {
        assertNull(authDAO.getAuth("missing"));
    }

    @Test
    @DisplayName("Delete auth success")
    void deleteAuthSuccess() throws DataAccessException {
        authDAO.createAuth(new AuthData("token-2", "alice"));
        authDAO.deleteAuth("token-2");
        assertNull(authDAO.getAuth("token-2"));
    }

    @Test
    @DisplayName("Clear auth success")
    void clearAuthSuccess() throws DataAccessException {
        authDAO.createAuth(new AuthData("token-3", "alice"));
        authDAO.clear();
        assertNull(authDAO.getAuth("token-3"));
    }
}

