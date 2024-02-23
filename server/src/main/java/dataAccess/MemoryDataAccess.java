package dataAccess;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import chess.ChessGame;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

public class MemoryDataAccess implements DataAccess{

    private Collection<UserData> users;
    //private HashMap<Integer, UserData> users = new HashMap<>();
    private Collection<GameData> games;
    //private HashMap<Integer, GameData> games = new HashMap<>();
    private Collection<AuthData> authTokens;
    //private HashMap<Integer, AuthData> authTokens = new HashMap<>();

    // public MemoryDataAccess(HashMap<Integer, UserData> users, HashMap<Integer, GameData> games, HashMap<Integer, AuthData> authTokens) {
    //     this.users = users;
    //     this.games = games;
    //     this.authTokens = authTokens;
    // }

    public MemoryDataAccess(Collection<UserData> users, Collection<GameData> games, Collection<AuthData> authTokens) {
        this.users = users;
        this.games = games;
        this.authTokens = authTokens;
    }
    

    public AuthData register(UserData userData) throws ResponseException {
        //Errors to throw
        //400 - Bad Request
        //403 - Already taken
        //500 - Internal Server Error
        if (userData == null || userData.username() == null || userData.username().isEmpty() || userData.password() == null || userData.password().isEmpty() ) {
            throw new ResponseException(400, "Error: Bad Request");
        }
        if (users == null || users.contains(userData)) {
            throw new ResponseException(403, "Error: already taken");
        }
        users.add(userData);
        var authToken = new AuthData(UUID.randomUUID().toString(), userData.username());
        authTokens.add(authToken);
        return authToken;
    }

    public AuthData login(UserData userData) throws ResponseException {
        //Errors to throw
        //400 - Bad Request 
        //401 - Unauthorized
        //500 - Internal Server Error
        if (userData == null || userData.username() == null || userData.username().isEmpty() || userData.password() == null || userData.password().isEmpty() ) {
            throw new ResponseException(400, "Error: Bad Request");
        }
        //Checks the user data against the users in the database and checks if the password is correct
        if (!users.contains(userData) || users.stream().filter(u -> u.username().equals(userData.username())).findFirst().get().password() != userData.password()) {
            throw new ResponseException(401, "Error: Unauthorized");
        }
        var authToken = new AuthData(UUID.randomUUID().toString(), userData.username());
        authTokens.add(authToken);
        return authToken;
    }

    public void logout(String authToken) throws ResponseException {
        //Errors to throw
        //401 - Unauthorized
        //500 - Internal Server Error
        if (authToken == null || authToken.isEmpty() || !authTokens.contains(authTokens.stream().filter(a -> a.authToken().equals(authToken)).findFirst().get())) {
            throw new ResponseException(401, "Error: Unauthorized");
        }
        authTokens.remove(authTokens.stream().filter(a -> a.authToken().equals(authToken)).findFirst().get());
    }

    public Collection<GameData> listGames(String authToken) throws ResponseException {
        //Errors to throw
        //401 - Unauthorized
        //500 - Internal Server Error
        if (authToken == null || authToken.isEmpty() || !authTokens.contains(authTokens.stream().filter(a -> a.authToken().equals(authToken)).findFirst().get())) {
            throw new ResponseException(401, "Error: Unauthorized");
        }
        return games;
    }

    @Override
    public GameData createGame(String authToken, String gameName) throws ResponseException {
        //Errors to throw
        //400 - Bad Request
        //401 - Unauthorized
        //500 - Internal Server Error
        if (authToken == null || authToken.isEmpty() || !authTokens.contains(authTokens.stream().filter(a -> a.authToken().equals(authToken)).findFirst().get())) {
            throw new ResponseException(401, "Error: Unauthorized");
        }
        if (gameName == null || gameName.isEmpty()) {
            throw new ResponseException(400, "Error: Bad Request");
        }
        int gameID = games.size();
        var whiteUsername = authTokens.stream().filter(a -> a.authToken().equals(authToken)).findFirst().get().username();
        var game = new GameData(gameID, whiteUsername, null, gameName, new ChessGame());
        games.add(game);
        return game;
    }

    @Override
    public void joinGame(String clientColor, int gameID, String authToken) throws ResponseException {
        // Errors to throw
        // 400 - Bad Request
        // 401 - Unauthorized
        // 403 - Already taken
        // 500 - Internal Server Error
        if (authToken == null || authToken.isEmpty() || !authTokens.contains(authTokens.stream().filter(a -> a.authToken().equals(authToken)).findFirst().get())) {
            throw new ResponseException(401, "Error: Unauthorized");
        }
        if (clientColor == null || clientColor.isEmpty() || gameID < 0) {
            throw new ResponseException(400, "Error: Bad Request");
        }
        var game = games.stream().filter(g -> g.gameID() == gameID).findFirst().get();
        if (clientColor.equals("white")) {
            if (game.whiteUsername() != null) {
                throw new ResponseException(403, "Error: Already taken");
            }
            game = new GameData(game.gameID(), game.whiteUsername(), authTokens.stream().filter(a -> a.authToken().equals(authToken)).findFirst().get().username(), game.gameName(), game.game());
            games.remove(game);
            games.add(game);
        } else if (clientColor.equals("black")) {
            if (game.blackUsername() != null) {
                throw new ResponseException(403, "Error: Already taken");
            }
            game = new GameData(game.gameID(), authTokens.stream().filter(a -> a.authToken().equals(authToken)).findFirst().get().username(), game.blackUsername(), game.gameName(), game.game());
            games.remove(game);
            games.add(game);
        }

    }

    @Override
    public void clear() throws ResponseException {
        // Errors to throw
        // 500 - Internal Server Error
        if (users != null) {
            users.clear();
        }
        if (games != null) {
            games.clear();
        }
        if (authTokens != null) {
            authTokens.clear();
        }
    }
    
}
