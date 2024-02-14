package dataAccess;

import java.util.Collection;

import exception.ResponseException;
import model.UserData;
import model.GameData;

public interface DataAccess {
    UserData register(UserData userData) throws ResponseException;
    UserData login(UserData userData) throws ResponseException;
    void logout(String authToken) throws ResponseException;
    Collection<GameData> listGames(String authToken) throws ResponseException;
    GameData createGame(String authToken, String gameName) throws ResponseException;
    void joinGame(String clientColor, int gameID, String authToken) throws ResponseException;
    void clear() throws ResponseException;
      
} 

// DataAccess {
//     public UserData register(UserData userData) {
//         return null;
//     }
//     void login(String username, String password) {}
//     void logout(String authToken) {}
//     void listGames(String authToken) {}
//     void createGame(String authToken, String gameName) {}
//     void joinGame(String clientColor, int gameID, String authToken) {}
//     void clear() {}
// }
