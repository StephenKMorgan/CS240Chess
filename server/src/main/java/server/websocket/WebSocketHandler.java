package server.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import webSocketMessages.serverMessages.LoadGameMessage;
import webSocketMessages.serverMessages.NotificationMessage;
import webSocketMessages.userCommands.JoinObserverCommand;
import webSocketMessages.userCommands.JoinPlayerCommand;
import webSocketMessages.userCommands.LeaveGameCommand;
import webSocketMessages.userCommands.MakeMoveCommand;
import webSocketMessages.userCommands.ResignCommand;
import webSocketMessages.userCommands.UserGameCommand;
import webSocketMessages.serverMessages.ServerMessage;
import service.Service;
import java.io.IOException;

import javax.management.Notification;



@WebSocket
public class WebSocketHandler {

    private WebSocketSessions sessions;
    private Service service;

    
    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        System.out.println("Connected");
        sessions = new WebSocketSessions();
        service = new Service();
    }

    @OnWebSocketClose
    public void onClose(Session session) {
        System.out.println("Closed");
        sessions.removeSession(session);
    }

    @OnWebSocketError
    public void onError(Throwable error) {
        System.out.println("Error");
        error.printStackTrace();
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws ResponseException, IOException {
        System.out.println("Message: " + message);
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case JOIN_PLAYER:
                joinPlayer((JoinPlayerCommand) command, session);
                break;
            case JOIN_OBSERVER:
                joinObserver((JoinObserverCommand) command, session);
                break;
            case MAKE_MOVE:
                makeMove((MakeMoveCommand) command, session);
                break;
            case LEAVE:
                leaveGame((LeaveGameCommand) command, session);
                break;
            case RESIGN:
                resignGame((ResignCommand) command, session);
                break;
            default:
                throw new ResponseException(500, "Invalid command type");
        }
    }

    public void joinPlayer(JoinPlayerCommand command, Session session) throws IOException, ResponseException {
        //Add the session to the game
        sessions.addSessionToGame(command.getGameID(), command.getAuthToken(), session);

        //Join the game
        var gameData = service.joinGame(command.getPlayerColor().toString(), command.getGameID(), command.getAuthToken());

        //Get the game data for notifications
        var game = gameData.game();
        var username = command.getUsername();

        //Send a LoadGameMessage to the player
        var loadMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadMessage.setGame(game);
        sendMessage(command.getGameID(), loadMessage, command.getAuthToken());

        //Send a NotificationMessage to the other players
        var notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(username + " has joined the game as the " + command.getPlayerColor().toString() + " player");
        broadcastMessage(command.getGameID(), notificationMessage, command.getAuthToken());        
    }

    public void joinObserver(JoinObserverCommand command, Session session) throws IOException, ResponseException {
        //Add the session to the game
        sessions.addSessionToGame(command.getGameID(), command.getAuthToken(), session);

        //Join the game
        var gameData = service.joinGame(null, command.getGameID(), command.getAuthToken());

        //Get the game data for notifications
        var game = gameData.game();
        var username = command.getUsername();

        //Send a LoadGameMessage to the player
        var loadMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadMessage.setGame(game);
        sendMessage(command.getGameID(), loadMessage, command.getAuthToken());

        //Send a NotificationMessage to the other players
        var notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(username + " has joined the game as an observer");
    }

    public void makeMove(MakeMoveCommand command, Session session) throws ResponseException, IOException {
        //Verify the move
        

        service.makeMove(command.getGameID(), command.getAuthToken(), command.getMove());
        //sendMessage(command.getGameID(), "Move made", command.getAuthToken());
    }

    public void leaveGame(LeaveGameCommand command, Session session) {
        // sessions.removeSessionFromGame(command.getGameID(), command.getAuthToken(), session);
    }

    public void resignGame(ResignCommand command, Session session) {
        //
    }

    private void sendMessage(Integer gameID, ServerMessage message, String authToken) throws ResponseException, IOException{
        sessions.getSessionsForGame(gameID).get(authToken).getRemote().sendString(new Gson().toJson(message));
    }

    private void broadcastMessage(Integer gameID, ServerMessage message, String exceptThisAuthToken) throws IOException{
        sessions.getSessionsForGame(gameID).forEach((authToken, session) -> {
            if (authToken != exceptThisAuthToken) {
                try {
                    session.getRemote().sendString(new Gson().toJson(message));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}