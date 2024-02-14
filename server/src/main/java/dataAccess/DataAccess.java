package dataAccess;

import exception.ResponseException;
import model.UserData;

public interface DataAccess {
    UserData register(UserData userData) throws ResponseException;
    UserData login(UserData userData) throws ResponseException;
    void logout(String authToken) throws ResponseException;
      
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
