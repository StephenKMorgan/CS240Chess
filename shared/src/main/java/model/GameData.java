package model;
import chess.ChessGame;

/**
 * Represents game data containing gameID, whiteUsername, blackUsername, gameName, and game.
 */
public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
}
