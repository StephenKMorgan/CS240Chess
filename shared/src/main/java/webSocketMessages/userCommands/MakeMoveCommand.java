package webSocketMessages.userCommands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {

    private ChessMove move;

    public MakeMoveCommand(String authToken) {
        super(authToken);
        this.commandType = CommandType.MAKE_MOVE;
    }

    public ChessMove getMove() {
        return move;
    }

    public void setMove(ChessMove move) {
        this.move = move;
    }
}
