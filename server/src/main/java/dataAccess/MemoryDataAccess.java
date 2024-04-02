package dataAccess;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

public class MemoryDataAccess implements DataAccess{

    private HashMap<String, UserData> users = new HashMap<>();
    private HashMap<Integer, GameData> games = new HashMap<>();
    private HashMap<String, AuthData> authTokens = new HashMap<>();

    public AuthData register(UserData userData) throws ResponseException {
        //Check to make sure the user is not already taken
        if (getUser(userData.username()) != null){
            throw new ResponseException(403, "Error: already taken");
        }
        //Create the user
        createUser(userData);
        //Create a new auth token
        var authToken = createAuth(userData.username());
        return authToken;
    }

    public AuthData login(UserData userData) throws ResponseException {
        //Get the user from the database
        if(getUser(userData.username()) == null){
            throw new ResponseException(401, "Error: Unauthorized");
        }
        //Check if the user is valid
        if (!validateUser(userData)){
            throw new ResponseException(401, "Error: Unauthorized");
        }
        //Create a new auth token
        var authToken = createAuth(userData.username());
        return authToken;
    }

    public void logout(String authToken) throws ResponseException {
        //Check if the auth token is valid
        if (getAuth(authToken) == null){
            throw new ResponseException(401, "Error: Unauthorized");
        }
        //Remove the auth token
        removeAuth(authToken);
    }

    public HashSet<GameData> listGames(String authToken) throws ResponseException {
        //Check if the auth token is valid
        if (!validateAuth(authToken)){
            throw new ResponseException(401, "Error: Unauthorized");
        }
        //Return the list of games
        return getGames();
    }

    public GameData createGame(String authToken, String gameName) throws ResponseException {
        //Check if the auth token is valid
        if (!validateAuth(authToken)){
            throw new ResponseException(401, "Error: Unauthorized");
        }
        //Create a new game
        return generateGame(authToken, gameName);
    }

    public GameData joinGame(String clientColor, int gameID, String authToken) throws ResponseException {
        //Check if the auth token is valid
        if (!validateAuth(authToken)){
            throw new ResponseException(401, "Error: Unauthorized");
        }
        //Check if the game is valid
        if (!validateGame(clientColor, gameID, authToken)){
            throw new ResponseException(403, "Error: Already taken");
        }
        //Join the game
        return joinValidGame(clientColor, gameID, authToken);
    }

    public void clear() throws ResponseException {
        users.clear();
        games.clear();
        authTokens.clear();
    }

    //helper functions
    private UserData getUser(String username) {
        if (users.containsKey(username)) {
            return users.get(username);
        }
        return null;
    }

    private void createUser(UserData userData) {
        users.put(userData.username(), userData);
    }

    private Boolean validateUser(UserData userData) {
        var user = getUser(userData.username());
        if (user == null || !user.password().equals(userData.password())) {
            return false;
        }
        return true;
    }

    private AuthData createAuth(String username) {
        var authToken = new AuthData(UUID.randomUUID().toString(), username);
        authTokens.put(authToken.authToken(), authToken);
        return authToken;
    }

    private AuthData getAuth(String authToken) {
        if (authTokens.containsKey(authToken)) {
            return authTokens.get(authToken);
        }
        return null;
    }

    private void removeAuth(String authToken) {
        authTokens.remove(authToken);
    }

    private boolean validateAuth(String authToken) {
        if (authTokens.containsKey(authToken)) {
            return true;
        }
        return false;
    }

    private HashSet<GameData> getGames() {
        return new HashSet<GameData>(games.values());
    }

    private GameData generateGame(String authToken, String gameName) {
        int gameID = games.size() + 1;
        var game = new GameData(gameID, null, null, gameName, new ChessGame());
        games.put(gameID, game);
        return game;
    }

    private boolean validateGame(String clientColor, int gameID, String authToken) {
        if (games.containsKey(gameID)) {
            var game = games.get(gameID);
            if (clientColor == null) {
                return true;
            }
            if (clientColor.equals("white")) {
                if (game.whiteUsername() == null) {
                    return true;
                }
            } else {
                if (game.blackUsername() == null) {
                    return true;
                }
            }            
        }
        return false;
    }

    private GameData joinValidGame(String clientColor, int gameID, String authToken) {
        var game = games.get(gameID);
        if (clientColor == null) {
            game = new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
            games.put(gameID, game);
            return game;
        } else {
            if (clientColor.equals("WHITE")) {
                game = new GameData(game.gameID(), authTokens.get(authToken).username(), game.blackUsername(), game.gameName(), game.game());
                games.put(gameID, game);
            } else {
                game = new GameData(game.gameID(), game.whiteUsername(), authTokens.get(authToken).username(), game.gameName(), game.game());
                games.put(gameID, game);
            }
            return game;
        }
    }

    public GameData makeMove(int gameID, String authToken, ChessMove move) throws ResponseException {
        //get the game
        var game = games.get(gameID);
        //make the move
        try {
            game.game().makeMove(move);
        } catch (InvalidMoveException e) {
            throw new ResponseException(400, "Error: Bad Request");
        }
        //return the game
        return game;
    }

    public String leaveGame(int gameID, String authToken) {
        //get the game
        var game = games.get(gameID);
        //leave the game
        if (game.whiteUsername().equals(authTokens.get(authToken).username())) {
            game = new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game());
            games.put(gameID, game);
            return game.blackUsername();
        } else {
            game = new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game());
            games.put(gameID, game);
            return game.whiteUsername();
        }
    }

    public String resignGame(int gameID, String authToken) throws ResponseException {
        //get the game
        var game = games.get(gameID);
        //resign the game
        if (game.whiteUsername().equals(authTokens.get(authToken).username())) {
            game = new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game());
            games.put(gameID, game);
            return game.blackUsername();
        } else {
            game = new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game());
            games.put(gameID, game);
            return game.whiteUsername();
        }
    }

    public GameData getGameData(int gameID, String authToken) {
        return games.get(gameID);
    }

    public String getUsernameFromAuthToken(String authToken) {
        return authTokens.get(authToken).username();
    }
}