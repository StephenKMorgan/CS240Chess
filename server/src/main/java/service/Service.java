package service;

import java.util.Collection;

import dataAccess.DataAccess;
import exception.ResponseException;
import model.GameData;
import model.UserData;

public class Service {
    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public UserData register(UserData userData) throws ResponseException {
        dataAccess.register(userData);
        return userData;
    }

    public UserData login(UserData userData) throws ResponseException {
        dataAccess.login(userData);
        return userData;
    }

    public void logout(String authToken) throws ResponseException {
        dataAccess.logout(authToken);
    }

    public Collection<GameData> listGames(String authToken) throws ResponseException {
        return dataAccess.listGames(authToken);
    }

    public GameData createGame(String authToken, String gameName) throws ResponseException {
        return dataAccess.createGame(authToken, gameName);
    }

    public void joinGame(String clientColor, int gameID, String authToken) throws ResponseException {
        dataAccess.joinGame(clientColor, gameID, authToken);
    }

    public void clearAll() throws ResponseException {
        dataAccess.clear();
    }
}
