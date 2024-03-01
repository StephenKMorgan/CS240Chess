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

    public AuthData register(UserData userData) throws ResponseException {
        //Check to make sure the user is not already taken
        try {
            if (getUser(userData.username()) != null){
                throw new ResponseException(403, "Error: already taken");
            }
        } catch (SQLException | DataAccessException | ResponseException e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
        //Create the user
        try {
            createUser(userData);
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }          
      
        //Create a new auth token
        AuthData authToken;
        try {
            authToken = createAuth(userData.username());
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
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

    public void joinGame(String clientColor, int gameID, String authToken) throws ResponseException {
        //Check if the auth token is valid
        if (!validateAuth(authToken)){
            throw new ResponseException(401, "Error: Unauthorized");
        }
        //Check if the game is valid
        if (!validateGame(clientColor, gameID, authToken)){
            throw new ResponseException(403, "Error: Already taken");
        }
        //Join the game
        joinValidGame(clientColor, gameID, authToken);
    }

    public void clear() throws ResponseException {
        users.clear();
        games.clear();
        authTokens.clear();
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

    private GameData generateGame(String authToken, String gameName) throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM gamedata";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            int gameID = 0;
            while (rs.next()) {
                gameID = Math.max(gameID, rs.getInt("game_id"));
            }
            gameID++;
            ChessGame game = new ChessGame();
            GameData newGame = new GameData(gameID, null, null, gameName, game);
            String gameJson = new Gson().toJson(game);
            try (Connection conn2 = DatabaseManager.getConnection()) {
                String sql2 = "INSERT INTO gamedata (game_id, gameName, game) VALUES (?, ?, ?)";
                PreparedStatement stmt2 = conn2.prepareStatement(sql2);
                stmt2.setInt(1, gameID);
                stmt2.setString(2, gameName);
                stmt2.setString(3, gameJson);
                stmt2.executeUpdate();
            }
            return newGame;
        }
    }

    private boolean validateGame(String clientColor, int gameID, String authToken) throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM gamedata WHERE game_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, gameID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String whiteUsername = rs.getString("whiteUsername");
                String blackUsername = rs.getString("blackUsername");
                if (clientColor == null) {
                    return true;
                }
                if (clientColor.equals("white")) {
                    if (whiteUsername == null) {
                        return true;
                    }
                } else {
                    if (blackUsername == null) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private void joinValidGame(String clientColor, int gameID, String authToken) throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM gamedata WHERE game_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, gameID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String whiteUsername = rs.getString("whiteUsername");
                String blackUsername = rs.getString("blackUsername");
                if (clientColor == null) {
                    if (whiteUsername == null) {
                        try (Connection conn2 = DatabaseManager.getConnection()) {
                            String sql2 = "UPDATE gamedata SET whiteUsername = ? WHERE game_id = ?";
                            PreparedStatement stmt2 = conn2.prepareStatement(sql2);
                            stmt2.setString(1, authToken);
                            stmt2.setInt(2, gameID);
                            stmt2.executeUpdate();
                        }
                    } else {
                        try (Connection conn2 = DatabaseManager.getConnection()) {
                            String sql2 = "UPDATE gamedata SET blackUsername = ? WHERE game_id = ?";
                            PreparedStatement stmt2 = conn2.prepareStatement(sql2);
                            stmt2.setString(1, authToken);
                            stmt2.setInt(2, gameID);
                            stmt2.executeUpdate();
                        }
                    }
                } else {
                    if (clientColor.equals("WHITE")) {
                        try (Connection conn2 = DatabaseManager.getConnection()) {
                            String sql2 = "UPDATE gamedata SET whiteUsername = ? WHERE game_id = ?";
                            PreparedStatement stmt2 = conn2.prepareStatement(sql2);
                            stmt2.setString(1, authToken);
                            stmt2.setInt(2, gameID);
                            stmt2.executeUpdate();
                        }
                    } else {
                        try (Connection conn2 = DatabaseManager.getConnection()) {
                            String sql2 = "UPDATE gamedata SET blackUsername = ? WHERE game_id = ?";
                            PreparedStatement stmt2 = conn2.prepareStatement(sql2);
                            stmt2.setString(1, authToken);
                            stmt2.setInt(2, gameID);
                            stmt2.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    private void clearAllData() throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM userdata";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM authdata";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM gamedata";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
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