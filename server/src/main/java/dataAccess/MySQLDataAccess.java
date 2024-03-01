package dataAccess;

import java.sql.*;
import java.util.HashSet;
import java.util.UUID;

import com.google.gson.Gson;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import exception.ResponseException;

public class MySQLDataAccess implements DataAccess {

    public MySQLDataAccess() throws ResponseException, DataAccessException {
        configureDatabase();
    }

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

    
    
    //Helper functions
    private UserData getUser(String username) throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM userdata WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
            } else {
                return null;
            }
        }
    }

    private void createUser(UserData userData) throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO userdata (username, password, email) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userData.username());
            stmt.setString(2, userData.password());
            stmt.setString(3, userData.email());
            stmt.executeUpdate();
        }
    }

    private Boolean validateUser(UserData userData) throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM userdata WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userData.username());
            stmt.setString(2, userData.password());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

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
    
    private AuthData getAuth(String authToken) throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM authdata WHERE authID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, authToken);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new AuthData(rs.getString("authID"), rs.getString("username"));
            } else {
                return null;
            }
        }
    }

    private void removeAuth(String authToken) throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM authdata WHERE authID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, authToken);
            stmt.executeUpdate();
        }
    }

    private boolean validateAuth(String authToken) throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM authdata WHERE authID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, authToken);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    private ChessGame convertJsonToChessGame(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, ChessGame.class);
    }

    private HashSet<GameData> getGames() throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM gamedata";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            HashSet<GameData> games = new HashSet<>();
            while (rs.next()) {
                String gameJson = rs.getString("game");
                ChessGame game = convertJsonToChessGame(gameJson);
                games.add(new GameData(rs.getInt("game_id"), rs.getString("whiteUsername"), rs.getString("blackUsername"), rs.getString("gameName"), game));
            }
            return games;
        }
    }
    
    private void configureDatabase() throws ResponseException, DataAccessException {
        DatabaseManager.createDatabase();
        String[] createStatements = {
            "CREATE TABLE IF NOT EXISTS userdata (id int PRIMARY KEY AUTO_INCREMENT, username varchar(255) UNIQUE NOT NULL, password varchar(255) NOT NULL, email varchar(255) UNIQUE)",
            "CREATE TABLE IF NOT EXISTS authdata (authID varchar(255) PRIMARY KEY NOT NULL, username varchar(255) NOT NULL, timestamp datetime, FOREIGN KEY (username) REFERENCES userdata (username))",
            "CREATE TABLE IF NOT EXISTS gamedata (game_id int PRIMARY KEY NOT NULL, whiteUsername varchar(255), blackUsername varchar(255), gameName varchar(255) NOT NULL, game JSON)",
            "CREATE TABLE IF NOT EXISTS gamelists (game_id int, username varchar(255), FOREIGN KEY (username) REFERENCES userdata (username), FOREIGN KEY (game_id) REFERENCES gamedata (game_id))"
        };
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new ResponseException(500, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}