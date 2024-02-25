package service;

import java.util.HashSet;

import dataAccess.DataAccess;
import dataAccess.MemoryDataAccess;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;


public class Service {
    private DataAccess dataAccess = new MemoryDataAccess();
    
    public Service() {
    }
    
    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    //User
    public AuthData register(UserData userData) throws ResponseException {
        if (userData == null || userData.username() == null || userData.username().isEmpty() || userData.password() == null || userData.password().isEmpty() ) {
            throw new ResponseException(400, "Error: Bad Request");
        }
        return dataAccess.register(userData);
    }

    //Session
    public AuthData login(UserData userData) throws ResponseException {
        if (userData == null || userData.username() == null || userData.username().isEmpty() || userData.password() == null || userData.password().isEmpty() ) {
            throw new ResponseException(400, "Error: Bad Request");
        }
        return dataAccess.login(userData);
    }

    public void logout(String authToken) throws ResponseException {
        if (authToken == null || authToken.isEmpty()) {
            throw new ResponseException(400, "Error: Bad Request");
        }
        dataAccess.logout(authToken);
    }

    //game
    public HashSet<GameData> listGames(String authToken) throws ResponseException {
        if (authToken == null || authToken.isEmpty()) {
            throw new ResponseException(400, "Error: Bad Request");
        }
        return dataAccess.listGames(authToken);
    }

    public GameData createGame(String authToken, String gameName) throws ResponseException {
        if (authToken == null || authToken.isEmpty() || gameName == null || gameName.isEmpty()) {
            throw new ResponseException(400, "Error: Bad Request");
        }
        return dataAccess.createGame(authToken, gameName);
    }

    public void joinGame(String clientColor, int gameID, String authToken) throws ResponseException {
        if ( gameID <= 0 || authToken == null || authToken.isEmpty()) {
            throw new ResponseException(400, "Error: Bad Request");
        }
        dataAccess.joinGame(clientColor, gameID, authToken);
    }

    //db
    public void clearAll() throws ResponseException {
        dataAccess.clear();
    }
}
