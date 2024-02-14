package dataAccess;

import java.util.Collection;
import java.util.UUID;

import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

public class MemoryDataAccess implements DataAccess{

    private Collection<UserData> users;
    private Collection<GameData> games;
    private Collection<AuthData> authTokens;
    

    public AuthData register(UserData userData) throws ResponseException {
        //Errors to throw
        //400 - Bad Request
        //403 - Already taken
        //500 - Internal Server Error
        if (userData == null || userData.username() == null || userData.username().isEmpty() || userData.password() == null || userData.password().isEmpty() ) {
            throw new ResponseException(400, "Error: Bad Request");
        }
        if (users.contains(userData)) {
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createGame'");
    }

    @Override
    public void joinGame(String clientColor, int gameID, String authToken) throws ResponseException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'joinGame'");
    }

    @Override
    public void clear() throws ResponseException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clear'");
    }
    
}
