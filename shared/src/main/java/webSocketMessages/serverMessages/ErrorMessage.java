package webSocketMessages.serverMessages;

public class ErrorMessage extends ServerMessage{

    private String error;

    public ErrorMessage(ServerMessageType type) {
        super(type);
        this.serverMessageType = ServerMessageType.ERROR;
    }

    public String getError() {
        return error;
    }

    public void setError(String message) {
        this.error = message;
    }
}
