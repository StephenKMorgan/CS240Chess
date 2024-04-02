package dataAccess;

import java.util.Collection;
import java.util.HashSet;

import chess.ChessMove;
import exception.ResponseException;
import model.UserData;
import model.AuthData;
import model.GameData;

public interface DataAccess {
    //Http
    AuthData register(UserData userData) throws ResponseException;
    AuthData login(UserData userData) throws ResponseException;
    void logout(String authToken) throws ResponseException;
    HashSet<GameData> listGames(String authToken) throws ResponseException;
    GameData createGame(String authToken, String gameName) throws ResponseException;
    GameData joinGame(String clientColor, int gameID, String authToken) throws ResponseException;
    //Admin
    void clear() throws ResponseException;
    //WebSocket
    GameData makeMove(int gameID, String authToken, ChessMove move) throws ResponseException;
    String leaveGame(int gameID, String authToken) throws ResponseException;
    String resignGame(int gameID, String authToken) throws ResponseException;
    GameData getGameData(int gameID, String authToken);
    String getUsernameFromAuthToken(String authToken);
} 