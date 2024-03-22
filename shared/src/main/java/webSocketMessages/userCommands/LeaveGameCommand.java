package webSocketMessages.userCommands;

public class LeaveGameCommand extends UserGameCommand{

    private Integer gameID;

    public LeaveGameCommand(String authToken) {
        super(authToken);
        this.commandType = CommandType.LEAVE;
    }
    
    public Integer getGameID() {
        return gameID;
    }

    public void setGameID(Integer gameID) {
        this.gameID = gameID;
    }
}
