package serviceTests;

import dataAccess.DataAccess;
import dataAccess.MemoryDataAccess;
import exception.ResponseException;
import model.AuthData;
import model.UserData;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.ClassOrderer.DisplayName;

import service.Service;


import java.util.HashSet;

public class serviceMemoryTest {
    private Service service;

    @BeforeEach
    public void setUp() {
        DataAccess dataAccess = new MemoryDataAccess();
        service = new Service(dataAccess);
    }

    @Test
    public void testRegisterPass() {
        UserData userData = new UserData("username", "password", "test@test.com");
        try {
            AuthData authData = service.register(userData);
            Assertions.assertNotNull(authData);
        } catch (ResponseException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testRegisterFail() {
        UserData userData = new UserData(null, "password", "test@test.com");
        try {
            service.register(userData);
            Assertions.fail("Expected exception");
        } catch (ResponseException e) {
            Assertions.assertEquals(400, e.statusCode());
        }
    }

    @Test
    public void testRegisterFail2() {
        UserData userData = new UserData("username", "", "test@test.com");
        try {
            service.register(userData);
            Assertions.fail("Expected exception");
        } catch (ResponseException e) {
            Assertions.assertEquals(400, e.statusCode());
        }
    }

    @Test
    public void testLoginPass() throws ResponseException {
        UserData userData = new UserData("username", "password", "test@test.com");
        // Register the user first
        service.register(userData);
        try {
            AuthData authData = service.login(userData);
            Assertions.assertNotNull(authData);
            // Add more assertions if needed
        } catch (ResponseException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testLoginFail() throws ResponseException {
        UserData userData = new UserData(null, "password", "test@test.com");
        try {
            service.register(userData);
            Assertions.fail("Expected exception");
        } catch (ResponseException e) {
            Assertions.assertEquals(400, e.statusCode());
        }
    }

    @Test
    public void testLoginFail2() throws ResponseException {
        UserData userData = new UserData("Username", "", "test@test.com");
        try {
            service.register(userData);
            Assertions.fail("Expected exception");
        } catch (ResponseException e) {
            Assertions.assertEquals(400, e.statusCode());
        }
    }

    @Test
    public void testLogoutPass() throws ResponseException {
        UserData userData = new UserData("username", "password", "test@test.com");
        AuthData authData = service.register(userData);
        String authToken = authData.authToken();
        try {
            service.logout(authToken);
        } catch (ResponseException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testLogoutFail() throws ResponseException {
        String authToken = "invalidToken";
        try {
            service.logout(authToken);
            Assertions.fail("Expected exception");
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.statusCode());
        }
    }

    @Test
    public void testListGamesPass() throws ResponseException {
        UserData userData = new UserData("username", "password", "test@test.com");
        AuthData authData = service.register(userData);
        String authToken = authData.authToken();
        service.createGame(authToken, "gameName");
        try {
            HashSet<GameData> games = service.listGames(authToken);
            Assertions.assertNotNull(games);
        } catch (ResponseException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testListGamesFail() throws ResponseException {
        UserData userData = new UserData("username", "password", "test@test.com");
        AuthData authData = service.register(userData);
        String authToken = authData.authToken();
        service.createGame(authToken, "gameName");
        try {
            HashSet<GameData> games = service.listGames(null);
            Assertions.fail("Expected exception");
        } catch (ResponseException e) {
            Assertions.assertEquals(400, e.statusCode());
        }
    }

    @Test
    public void testCreateGamePass() throws ResponseException {
        UserData userData = new UserData("username", "password", "test@test.com");
        AuthData authData = service.register(userData);
        String authToken = authData.authToken();
        try {
            GameData gameData = service.createGame(authToken, "gameName");
            Assertions.assertNotNull(gameData);
        } catch (ResponseException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testCreateGameFail() throws ResponseException {
        UserData userData = new UserData("username", "password", "test@test.com");
        AuthData authData = service.register(userData);
        String authToken = authData.authToken();
        try {
            GameData gameData = service.createGame(authToken, null);
            Assertions.fail("Expected exception");
        } catch (ResponseException e) {
            Assertions.assertEquals(400, e.statusCode());
        }
    }

    @Test
    public void testCreateGameFail2() throws ResponseException {
        UserData userData = new UserData("username", "password", "test@test.com");
        AuthData authData = service.register(userData);
        String authToken = authData.authToken();
        try {
            GameData gameData = service.createGame(null, "gameName");
            Assertions.fail("Expected exception");
        } catch (ResponseException e) {
            Assertions.assertEquals(400, e.statusCode());
        }
    }

    @Test
    public void testJoinGamePass() throws ResponseException {
        UserData userData = new UserData("username", "password", "test@test.com");
        AuthData authData = service.register(userData);
        String authToken = authData.authToken();
        service.createGame(authToken, "gameName");
        String clientColor = "WHITE";
        int gameID = 1;
        try {
            service.joinGame(clientColor, gameID, authToken);
        } catch (ResponseException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testJoinGameWatcherPass() throws ResponseException {
        UserData userData = new UserData("username", "password", "test@test.com");
        AuthData authData = service.register(userData);
        String authToken = authData.authToken();
        service.createGame(authToken, "gameName");
        String clientColor = null;
        int gameID = 1;
        try {
            service.joinGame(clientColor, gameID, authToken);
        } catch (ResponseException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testJoinGameWatchersPass() throws ResponseException {
        UserData userData = new UserData("username", "password", "test@test.com");
        AuthData authData = service.register(userData);
        String authToken = authData.authToken();
        UserData userData2 = new UserData("username2", "password", "test@test.com");
        AuthData authData2 = service.register(userData2);
        String authToken2 = authData2.authToken();
        UserData userData3 = new UserData("username3", "password", "test@test.com");
        AuthData authData3 = service.register(userData3);
        String authToken3 = authData3.authToken();
        service.createGame(authToken, "gameName");
        String clientColor = null;
        int gameID = 1;
        try {
            service.joinGame(clientColor, gameID, authToken);
            service.joinGame(clientColor, gameID, authToken2);
            service.joinGame(clientColor, gameID, authToken3);
        } catch (ResponseException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testJoinGameFail1() throws ResponseException {
        UserData userData = new UserData("username", "password", "test@test.com");
        AuthData authData = service.register(userData);
        String authToken = authData.authToken();
        service.createGame(authToken, "gameName");
        String clientColor = "WHITE";
        int gameID = 0;
        try {
            service.joinGame(clientColor, gameID, authToken);
            Assertions.fail("Expected exception");
        } catch (ResponseException e) {
            Assertions.assertEquals(400, e.statusCode());
        }
    }

    @Test
    public void testJoinGameFail2() throws ResponseException {
        String authToken = "invalidToken";
        String clientColor = "WHITE";
        int gameID = 5;
        try {
            service.joinGame(clientColor, gameID, authToken);
            Assertions.fail("Expected exception");
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.statusCode());
        }
    }

    @Test
    public void testClearAllEmpty() {
        try {
            service.clearAll();
        } catch (ResponseException e) {
            Assertions.fail("Unexpected exception: " + e.getMessage());
        }
    }
}