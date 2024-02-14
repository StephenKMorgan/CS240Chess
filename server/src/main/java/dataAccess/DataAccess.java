package dataAccess;

import java.util.Collection;

import exception.ResponseException;
import model.UserData;
import model.AuthData;
import model.GameData;

public interface DataAccess {
    AuthData register(UserData userData) throws ResponseException;
    UserData login(UserData userData) throws ResponseException;
    void logout(String authToken) throws ResponseException;
    Collection<GameData> listGames(String authToken) throws ResponseException;
    GameData createGame(String authToken, String gameName) throws ResponseException;
    void joinGame(String clientColor, int gameID, String authToken) throws ResponseException;
    void clear() throws ResponseException;    
} 