package webSocketMessages.serverMessages;

public class NotificationMessage extends ServerMessage {
    
        private String message;
    
        public NotificationMessage(ServerMessageType type) {
            super(type);
            this.serverMessageType = ServerMessageType.NOTIFICATION;
        }
    
        public String getError() {
            return message;
        }
    
        public void setError(String message) {
            this.message = message;
        }
    
}
