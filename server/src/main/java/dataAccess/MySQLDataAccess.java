package dataAccess;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.sql.*;

import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

import com.google.gson.Gson;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySQLDataAccess implements DataAccess{

    public MySQLDataAccess() throws ResponseException, DataAccessException {
        configureDatabase();
    }

    @Override
    public AuthData register(UserData userData) throws ResponseException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'register'");
    }

    @Override
    public AuthData login(UserData userData) throws ResponseException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }

    @Override
    public void logout(String authToken) throws ResponseException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logout'");
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

    private int executeUpdate(String statement, Object... params) throws ResponseException, DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    //else if (param instanceof PetType p) ps.setString(i + 1, p.toString());
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new ResponseException(500, String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  `chess`.`userdata` (
                `ID` INT NOT NULL AUTO_INCREMENT,
                `username` VARCHAR(256) NOT NULL,
                `password` VARCHAR(256) NOT NULL,
                `email` VARCHAR(256) NOT NULL,
                PRIMARY KEY (`ID`),
                UNIQUE INDEX `username_UNIQUE` (`username` ASC) VISIBLE,
                UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE);
            CREATE TABLE IF NOT EXISTS  authData (
              `id` int NOT NULL AUTO_INCREMENT,
              `username` varchar(256) NOT NULL,
              `authID` varchar(256) NOT NULL,
              PRIMARY KEY (`id`),
              UNIQUE KEY `authID` (`authID`),
              INDEX `username_index` (`username`),
              FOREIGN KEY (`username`) REFERENCES `userData` (`username`)
            )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci,
            """,
            """
            CREATE TABLE IF NOT EXISTS  gameData (
              `id` int NOT NULL AUTO_INCREMENT,
              `gameID` varchar(256) NOT NULL,
              `whiteUsername` varchar(256) NOT NULL,
              `blackUsername` varchar(256) NOT NULL,
              `gameName` varchar(256) NOT NULL,
              `game` JSON NOT NULL,
              PRIMARY KEY (`id`),
              UNIQUE KEY `gameID` (`gameID`),
              INDEX `whiteUsername_index` (`whiteUsername`),
              INDEX `blackUsername_index` (`blackUsername`),
              FOREIGN KEY (`whiteUsername`) REFERENCES `userData` (`username`),
              FOREIGN KEY (`blackUsername`) REFERENCES `userData` (`username`)
            )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci,
            """,
            """
            CREATE TABLE IF NOT EXISTS  gameList (
              `id` int NOT NULL AUTO_INCREMENT,
              `gameID` varchar(256) NOT NULL,
              `username` varchar(256) NOT NULL,
              PRIMARY KEY (`id`),
              INDEX `gameID_index` (`gameID`),
              INDEX `username_index` (`username`),
              FOREIGN KEY (`gameID`) REFERENCES `gameData` (`gameID`),
              FOREIGN KEY (`username`) REFERENCES `userData` (`username`)
            )
            """
    };

//     Table: UserData
// ID (int), Username (charvar(50)), Password (charvar(50)), Email (charvar(200))
// ```
// Table: AuthData
// ID (int), Username (charvar(50)), AuthID (charvar(50))
// Username is a foreign key to UserData.Username
// ```
// Table: GameData
// ID (int), gameID (charvar(50)), whiteUsername (charvar(50)), blackUsername (charvar(50)), gameName (charvar(50)), game (JSON)

// ```
// Table: GameList (connections between users and games)
// ID (int), gameID (charvar(50)), Username (charvar(50))
// Username is a foreign key to UserData.Username
// gameID is a foreign key to GameData.gameID
// ```

    private void configureDatabase() throws ResponseException, DataAccessException {
        DatabaseManager.createDatabase();
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
