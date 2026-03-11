package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class MySqlAuthDAOTests extends MySqlDAOTestBase {

    @BeforeEach
    void setUpAuth() throws DataAccessException {
        createAliceUser();
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
    @DisplayName("Create auth null fails")
    void createAuthNullFails() {
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(null));
    }

    @Test
    @DisplayName("Create auth duplicate fails")
    void createAuthDuplicateFails() throws DataAccessException {
        AuthData auth = new AuthData("token-1", "alice");
        authDAO.createAuth(auth);
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(auth));
    }

    @Test
    @DisplayName("Get auth success")
    void getAuthSuccess() throws DataAccessException {
        authDAO.createAuth(new AuthData("token-get", "alice"));
        AuthData stored = authDAO.getAuth("token-get");
        assertNotNull(stored);
        assertEquals("token-get", stored.authToken());
        assertEquals("alice", stored.username());
    }

    @Test
    @DisplayName("Get auth missing returns null")
    void getAuthMissingReturnsNull() throws DataAccessException {
        AuthData result = authDAO.getAuth("missing");
        assertNull(result);
    }

    @Test
    @DisplayName("Get auth null token returns null")
    void getAuthNullTokenReturnsNull() throws DataAccessException {
        assertNull(authDAO.getAuth(null));
    }

    @Test
    @DisplayName("Delete auth success")
    void deleteAuthSuccess() throws DataAccessException {
        authDAO.createAuth(new AuthData("token-2", "alice"));
        authDAO.deleteAuth("token-2");
        assertNull(authDAO.getAuth("token-2"));
    }

    @Test
    @DisplayName("Delete auth missing token does not throw")
    void deleteAuthMissingTokenSucceeds() throws DataAccessException {
        assertDoesNotThrow(() -> authDAO.deleteAuth("nonexistent-token"));
        assertNull(authDAO.getAuth("nonexistent-token"));
    }

    @Test
    @DisplayName("Clear auth success")
    void clearAuthSuccess() throws DataAccessException {
        authDAO.createAuth(new AuthData("token-3", "alice"));
        authDAO.clear();
        assertNull(authDAO.getAuth("token-3"));
    }

    @Test
    @DisplayName("Clear when empty does not throw")
    void clearWhenEmptySucceeds() throws DataAccessException {
        authDAO.clear();
        assertDoesNotThrow(() -> authDAO.clear());
        authDAO.createAuth(new AuthData("token-after", "alice"));
        assertNotNull(authDAO.getAuth("token-after"));
    }
}

