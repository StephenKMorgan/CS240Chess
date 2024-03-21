package webSocketMessages.serverMessages;

import chess.ChessGame;

public class LoadGameMessage extends ServerMessage{

    private ChessGame game;

    public LoadGameMessage(ServerMessageType type) {
        super(type);
        this.serverMessageType = ServerMessageType.LOAD_GAME;
    }

    public ChessGame getGame() {
        return game;
    }

    public void setGame(ChessGame game) {
        this.game = game;
    }
}
