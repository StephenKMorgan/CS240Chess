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
        var serverFacade = new Server().run(port);
        serverFacade = new ServerFacade("http://localhost:4567");
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    public static void tearDown() {
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
            Assertions.assertEquals(500, e.StatusCode());
        }
    }

    @Test
    public void testRegisterUserFailInDatabaseAlready() throws ResponseException {
        String username = "testUser2";
        String password = "testPassword2";
        String email = "test@test.com";
    
        try {
            // First registration attempt
            AuthData authData = server.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
    
            // Second registration attempt
            Assertions.assertThrows(ResponseException.class, () -> {
                serverFacade.registerUser(username, password, email);
            }, "Expected exception");
        }
        catch (ResponseException e) {
            Assertions.assertEquals(500, e.StatusCode());
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
            System.out.println(testAuthData.authToken());
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
            Assertions.assertEquals(500, e.StatusCode());
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
            Assertions.assertEquals(500, e.StatusCode());
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
            Assertions.assertEquals(500, e.StatusCode());
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
            AuthData authData = server.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            server.createGame("Wrong Token", "testGame1");
            Assertions.fail("Expected exception");
        } catch (ResponseException e) {
            Assertions.assertEquals(500, e.StatusCode());
        }
    }

    @Test
    public void testjoinGamePass() throws ResponseException {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";
       
        
        try {
            AuthData authData = server.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            server.createGame(authData.authToken(), "testGame1");
            var games = server.listGames(authData.authToken());
            Assertions.assertNotNull(games);
            var result = server.joinGame(authData.authToken(), games.games().get(0).gameID(), "white");
            Assertions.assertNotNull(result);
        } catch (ResponseException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testjoinGameFail() throws ResponseException {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";
       
        
        try {
            AuthData authData = server.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            server.createGame(authData.authToken(), "testGame1");
            var games = server.listGames(authData.authToken());
            Assertions.assertNotNull(games);
            server.joinGame(authData.authToken(), -1, "white");
            Assertions.fail("Expected exception");
        } catch (ResponseException e) {
            Assertions.assertEquals(500, e.StatusCode());
        }
    }

}