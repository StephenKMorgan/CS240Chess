package ui;

import chess.ChessGame;

public interface GameHandler {
    void updateGame(ChessGame game);
    void printMessage(String message);
}
