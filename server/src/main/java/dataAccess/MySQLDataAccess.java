package dataAccess;

import java.sql.*;
import java.util.HashSet;
import java.util.UUID;

import model.AuthData;
import model.GameData;
import model.UserData;
import exception.ResponseException;

public class MySQLDataAccess implements DataAccess {

    public AuthData register(UserData userData) throws ResponseException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO userdata (username, password, email) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userData.username());
            stmt.setString(2, userData.password());
            stmt.setString(3, userData.email());
            stmt.executeUpdate();
            return createAuth(userData.username());
        } catch (SQLException e) {
            throw new ResponseException(403, "Error: already taken");
        }
    }

    public AuthData login(UserData userData) throws ResponseException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM userdata WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userData.username());
            stmt.setString(2, userData.password());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return createAuth(userData.username());
            } else {
                throw new ResponseException(401, "Error: Unauthorized");
            }
        } catch (SQLException e) {
            throw new ResponseException(401, "Error: Unauthorized");
        }
    }

    public void logout(String authToken) throws ResponseException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM authdata WHERE authID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, authToken);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ResponseException(401, "Error: Unauthorized");
        }
    }

    // Implement other methods similarly...

    private AuthData createAuth(String username) throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO authdata (authID, username, timestamp) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            String authToken = UUID.randomUUID().toString();
            stmt.setString(1, authToken);
            stmt.setString(2, username);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
            return new AuthData(authToken, username);
        }
    }

    @Override
    public HashSet<GameData> listGames(String authToken) throws ResponseException {
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