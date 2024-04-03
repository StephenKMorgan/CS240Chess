package clientTests;

import org.junit.jupiter.api.*;

import exception.ResponseException;
import model.AuthData;
import ui.ServerFacade;
import server.Server;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        var port = 4567;
        server = new Server();
        server.run(port);
        serverFacade = new ServerFacade("http://localhost:4567");
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterEach
    public void resetDatabase(){
        serverFacade.clearData();
    }

    @AfterAll
    public static void tearDown() {
        serverFacade.clearData();
        server.stop();
    }
    
    @Test
    public void testRegisterUserPass() throws ResponseException {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";

        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
        } catch (ResponseException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testRegisterUserFail() throws ResponseException {
        String username = null;
        String password = "testPassword";
        String email = "test@test.com";

        try {
            serverFacade.registerUser(username, password, email);
            Assertions.fail("Expected exception");
        } catch (ResponseException e) {
            Assertions.assertEquals(400, e.statusCode());
        }
    }

    @Test
    public void testRegisterUserFailInDatabaseAlready() throws ResponseException {
        String username = "testUser2";
        String password = "testPassword2";
        String email = "test@test.com";
    
        try {
            // First registration attempt
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.logoutUser(authData.authToken());
            // Second registration attempt
            serverFacade.registerUser(username, password, email);
        }
        catch (ResponseException e) {
            Assertions.assertEquals(403, e.statusCode());
        }
    }

    @Test
    public void testLoginUserPass() throws ResponseException {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";

        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.logoutUser(authData.authToken());
            AuthData testAuthData = serverFacade.loginUser(username, password);
            Assertions.assertNotNull(testAuthData);
        } catch (ResponseException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testLoginUserFail() throws ResponseException {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";
        String wrongPassword = "wrongPassword";
        
        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.logoutUser(authData.authToken());
            serverFacade.loginUser(username, wrongPassword);
            Assertions.fail("Expected exception");
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.statusCode());
        }
    }

    @Test
    public void testLogoutUserPass() throws ResponseException {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";

        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.logoutUser(authData.authToken());
        } catch (ResponseException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testLogoutUserFail() throws ResponseException {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";

        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.logoutUser("wrongToken");
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.statusCode());
        }
    }

    @Test
    public void testListGamesPass() throws ResponseException {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";
       
        
        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.createGame(authData.authToken(), "testGame1");
            serverFacade.createGame(authData.authToken(), "testGame2");
            serverFacade.createGame(authData.authToken(), "testGame3");
            var games = serverFacade.listGames(authData.authToken());
            Assertions.assertNotNull(games);
        } catch (ResponseException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testListGamesFail() throws ResponseException {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";
       
        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.createGame(authData.authToken(), "testGame1");
            serverFacade.createGame(authData.authToken(), "testGame2");
            serverFacade.createGame(authData.authToken(), "testGame3");
            serverFacade.listGames("wrongToken");
            Assertions.fail("Expected exception");
        }
        catch (ResponseException e) {
            Assertions.assertEquals(401, e.statusCode());
        }
    }

    @Test
    public void testCreateGamePass() throws ResponseException {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";
       
        
        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.createGame(authData.authToken(), "testGame1");
            var games = serverFacade.listGames(authData.authToken());
            Assertions.assertNotNull(games);
        } catch (ResponseException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testCreateGameFail() throws ResponseException {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";
       
        
        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.createGame("Wrong Token", "testGame1");
            Assertions.fail("Expected exception");
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.statusCode());
        }
    }

    @Test
    public void testjoinGamePass() throws ResponseException {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";
       
        
        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.createGame(authData.authToken(), "testGame1");
            var games = serverFacade.listGames(authData.authToken());
            Assertions.assertNotNull(games);
            var result = serverFacade.joinGame(authData.authToken(), games.games().get(0).gameID(), "white");
            Assertions.assertNotNull(result);
        } catch (ResponseException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testjoinGameFail() throws ResponseException {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";
       
        
        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.createGame(authData.authToken(), "testGame1");
            var games = serverFacade.listGames(authData.authToken());
            Assertions.assertNotNull(games);
            serverFacade.joinGame(authData.authToken(), -1, "white");
            Assertions.fail("Expected exception");
        } catch (ResponseException e) {
            Assertions.assertEquals(400, e.statusCode());
        }
    }

}