package webSocketMessages.userCommands;

public class LeaveGameCommand extends UserGameCommand{

    public LeaveGameCommand(String authToken) {
        super(authToken);
        this.commandType = CommandType.LEAVE;
    }
    
}
