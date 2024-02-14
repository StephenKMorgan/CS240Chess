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

    @Override
    public UserData login(UserData userData) throws ResponseException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }

    @Override
    public void logout(String authToken) throws ResponseException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logout'");
    }

    @Override
    public Collection<GameData> listGames(String authToken) throws ResponseException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listGames'");
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
