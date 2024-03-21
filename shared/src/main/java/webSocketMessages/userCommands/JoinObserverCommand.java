package webSocketMessages.userCommands;

import chess.ChessGame;


public class JoinObserverCommand extends UserGameCommand{

    private Integer gameID;
    private String username;
    private ChessGame.TeamColor playerColor;

    public JoinObserverCommand(String authToken) {
        super(authToken);
        this.commandType = CommandType.JOIN_OBSERVER;
    }

    public Integer getGameID() {
        return gameID;
    }

    public void setGameID(Integer gameID) {
        this.gameID = gameID;
    }

    public ChessGame.TeamColor getPlayerColor() {
        return playerColor;
    }

    public void setPlayerColor(ChessGame.TeamColor playerColor) {
        this.playerColor = playerColor;
    }

    public String getAuthToken() {
        return getAuthToken();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
