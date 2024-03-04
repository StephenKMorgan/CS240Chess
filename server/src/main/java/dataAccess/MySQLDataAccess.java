package dataAccess;

import java.sql.*;
import java.util.HashSet;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.google.gson.Gson;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import exception.ResponseException;

public class MySQLDataAccess implements DataAccess {

    public MySQLDataAccess() {
        try {
            configureDatabase();
        } catch (ResponseException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthData register(UserData userData) throws ResponseException {
        //Check to make sure the user is not already taken
        try {
            if (getUser(userData.username()) != null){
                throw new ResponseException(403, "Error: already taken");
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
        //Create the user
        try {
            createUser(userData);
        } catch (SQLException | DataAccessException e) {
            System.out.println("Error in create user");
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
        try {
            if(getUser(userData.username()) == null){
                throw new ResponseException(401, "Error: Unauthorized");
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
        //Check if the user is valid
        try {
            if (!validateUser(userData)){
                throw new ResponseException(401, "Error: Unauthorized");
            }
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

    public void logout(String authToken) throws ResponseException {
        //Check if the auth token is valid
        try {
            if (getAuth(authToken) == null){
                throw new ResponseException(401, "Error: Unauthorized");
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
        //Remove the auth token
        try {
            removeAuth(authToken);
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
    }

    public HashSet<GameData> listGames(String authToken) throws ResponseException {
        //Check if the auth token is valid
        try {
            if (!validateAuth(authToken)){
                throw new ResponseException(401, "Error: Unauthorized");
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
        //Return the list of games
        try {
            return getGames();
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
    }

    public GameData createGame(String authToken, String gameName) throws ResponseException {
        //Check if the auth token is valid
        try {
            if (!validateAuth(authToken)){
                throw new ResponseException(401, "Error: Unauthorized");
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
        //Create a new game
        try {
            return generateGame(authToken, gameName);
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
    }

    public void joinGame(String clientColor, int gameID, String authToken) throws ResponseException {
        //Check if the auth token is valid
        try {
            if (!validateAuth(authToken)){
                throw new ResponseException(401, "Error: Unauthorized");
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
        //Check if the game is valid
        try {
            if (!validateGame(clientColor, gameID, authToken)){
                throw new ResponseException(403, "Error: Already taken");
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
        //Join the game
        try {
            joinValidGame(clientColor, gameID, authToken);
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
    }

    public void clear() throws ResponseException {
        try {
            clearAllData();
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
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
            stmt.setString(2, encryptPassword(userData.password()));
            stmt.setString(3, userData.email());
            stmt.executeUpdate();
        }
    }

    private String encryptPassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(password);
        return hashedPassword;
    }

    private Boolean validateUser(UserData userData) throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT password FROM userdata WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userData.username());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                return encoder.matches(userData.password(), storedHashedPassword);
            } else {
                return false;
            }
        }
        catch (SQLException | DataAccessException e) {
            return false;
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
        //use the gamelists table to join the game
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO gamelists (game_id, username) VALUES (?, ?)";
             PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, gameID);
            stmt.setString(2, getAuth(authToken).username());
            stmt.executeUpdate();
        }
        //update the gamedata table to reflect the new player if they have selected a color to join
        if (clientColor == null) {
            return;
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM gamedata WHERE game_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, gameID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (clientColor.equals("WHITE")) {
                    String sql2 = "UPDATE gamedata SET whiteUsername = ? WHERE game_id = ?";
                    PreparedStatement stmt2 = conn.prepareStatement(sql2);
                    stmt2.setString(1, getAuth(authToken).username());
                    stmt2.setInt(2, gameID);
                    stmt2.executeUpdate();
                } else {
                    String sql2 = "UPDATE gamedata SET blackUsername = ? WHERE game_id = ?";
                    PreparedStatement stmt2 = conn.prepareStatement(sql2);
                    stmt2.setString(1, getAuth(authToken).username());
                    stmt2.setInt(2, gameID);
                    stmt2.executeUpdate();
                }
            }
        }
    }

    private void clearAllData() throws SQLException, DataAccessException {
        try(Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM gamelists";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM authdata";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM userdata";
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
            "CREATE TABLE IF NOT EXISTS userdata (id int PRIMARY KEY AUTO_INCREMENT, username varchar(255) UNIQUE NOT NULL, password varchar(255) NOT NULL, email varchar(255))",
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