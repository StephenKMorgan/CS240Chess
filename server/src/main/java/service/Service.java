package service;

import java.util.Collection;

import dataAccess.DataAccess;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

public class Service {
    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData userData) throws ResponseException {
        return dataAccess.register(userData);
    }

    public AuthData login(UserData userData) throws ResponseException {
        return dataAccess.login(userData);
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
