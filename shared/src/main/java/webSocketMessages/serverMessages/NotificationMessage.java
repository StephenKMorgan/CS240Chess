package webSocketMessages.serverMessages;

public class NotificationMessage extends ServerMessage {
    
        private String message;
    
        public NotificationMessage(ServerMessageType type) {
            super(type);
            this.serverMessageType = ServerMessageType.NOTIFICATION;
        }
    
        public String getMessage() {
            return message;
        }
    
        public void setMessage(String message) {
            this.message = message;
        }
    
}
