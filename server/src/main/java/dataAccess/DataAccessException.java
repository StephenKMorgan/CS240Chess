package dataAccess;

/**
 * Indicates there was an error connecting to the database and thows a ResponseException
 */
public class DataAccessException extends Exception{
    public DataAccessException(String message) {
        super(message);
    }
}
