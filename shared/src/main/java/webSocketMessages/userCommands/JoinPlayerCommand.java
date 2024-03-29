package webSocketMessages.userCommands;

import chess.ChessGame;

public class JoinPlayerCommand extends UserGameCommand {

    private Integer gameID;
    private String username;
    private ChessGame.TeamColor playerColor;


    public JoinPlayerCommand(String authToken) {
        super(authToken);
        this.commandType = CommandType.JOIN_PLAYER;
    }

    public Integer getGameID() {
        return gameID;
    }

    public ChessGame.TeamColor getPlayerColor() {
        return playerColor;
    }

    public void setGameID(Integer gameID) {
        this.gameID = gameID;
    }

    public void setPlayerColor(ChessGame.TeamColor playerColor) {
        this.playerColor = playerColor;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

}
