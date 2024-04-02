package webSocketMessages.serverMessages;

public class ErrorMessage extends ServerMessage{

    private String errorMessage;

    public ErrorMessage(ServerMessageType type) {
        super(type);
        this.serverMessageType = ServerMessageType.ERROR;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String message) {
        this.errorMessage = message;
    }
}
