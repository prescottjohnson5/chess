package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;

abstract class MySqlDAOTestBase {

    protected MySqlUserDAO userDAO;
    protected MySqlAuthDAO authDAO;
    protected MySqlGameDAO gameDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        userDAO = new MySqlUserDAO();
        authDAO = new MySqlAuthDAO();
        gameDAO = new MySqlGameDAO();
        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();
    }

    protected void createAliceUser() throws DataAccessException {
        userDAO.createUser(new UserData("alice", "pw", "a@test.com"));
    }
}
