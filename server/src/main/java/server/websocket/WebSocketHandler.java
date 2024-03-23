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
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("Connection closed with status: " + statusCode + " and reason: " + reason);
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
    Gson gson = new Gson();
    switch (command.getCommandType()) {
        case JOIN_PLAYER:
            JoinPlayerCommand joinPlayerCommand = gson.fromJson(message, JoinPlayerCommand.class);
            joinPlayer(joinPlayerCommand, session);
            break;
        case JOIN_OBSERVER:
            JoinObserverCommand joinObserverCommand = gson.fromJson(message, JoinObserverCommand.class);
            joinObserver(joinObserverCommand, session);
            break;
        case MAKE_MOVE:
            MakeMoveCommand makeMoveCommand = gson.fromJson(message, MakeMoveCommand.class);
            makeMove(makeMoveCommand, session);
            break;
        case LEAVE:
            LeaveGameCommand leaveGameCommand = gson.fromJson(message, LeaveGameCommand.class);
            leaveGame(leaveGameCommand, session);
            break;
        case RESIGN:
            ResignCommand resignCommand = gson.fromJson(message, ResignCommand.class);
            resignGame(resignCommand, session);
            break;
        default:
            throw new ResponseException(500, "Invalid command type");
    }
}

    public void joinPlayer(JoinPlayerCommand command, Session session) throws IOException, ResponseException {
        //Add the session to the game
        sessions.addSessionToGame(command.getGameID(), command.getAuthString(), session);

        //Get the joined game data
        var gameData = service.getGameData(command.getGameID(), command.getAuthString());

        //Get the game data for notifications
        var game = gameData.game();
        var username = command.getUsername();

        //Send a LoadGameMessage to the player
        var loadMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadMessage.setGame(game);
        sendMessage(command.getGameID(), loadMessage, command.getAuthString(), session);

        //Send a NotificationMessage to the other players
        var notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(username + " has joined the game as the " + command.getPlayerColor().toString() + " player");
        broadcastMessage(command.getGameID(), notificationMessage, command.getAuthString());
    }

    public void joinObserver(JoinObserverCommand command, Session session) throws IOException, ResponseException {
        //Add the session to the game
        sessions.addSessionToGame(command.getGameID(), command.getAuthString(), session);

        //Get the joined game data
        var gameData = service.getGameData(command.getGameID(), command.getAuthString());

        //Get the game data for notifications
        var game = gameData.game();
        var username = command.getUsername();

        //Send a LoadGameMessage to the player
        var loadMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadMessage.setGame(game);
        sendMessage(command.getGameID(), loadMessage, command.getAuthString(), session);

        //Send a NotificationMessage to the other players
        var notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(username + " has joined the game as an observer");
    }

    public void makeMove(MakeMoveCommand command, Session session) throws ResponseException, IOException {
        //Verify and make the move
        var gameData = service.makeMove(command.getGameID(), command.getAuthToken(), command.getMove());
        
        //Get the game data for notifications
        var game = gameData.game();
        var move = command.getMove().toString();

        //Send a LoadGameMessage to all players
        var loadMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadMessage.setGame(game);
        broadcastMessage(command.getGameID(), loadMessage,"");

        //Send a NotificationMessage to all players
        var notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(move);
    }

    public void leaveGame(LeaveGameCommand command, Session session) throws IOException, ResponseException {
        //Remove the session from the game
        sessions.removeSessionFromGame(command.getGameID(), command.getAuthString(), session);

        //Leave the game
        var username = service.leaveGame(command.getGameID(), command.getAuthString());

        //Get the game data for notifications
        var gameID = command.getGameID(); 

        //Send a NotificationMessage to the other players
        var notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(username + " has left the game");
        broadcastMessage(gameID, notificationMessage, command.getAuthString());
    }

    public void resignGame(ResignCommand command, Session session) throws ResponseException, IOException {
        //Remove the session from the game
        sessions.removeSessionFromGame(command.getGameID(), command.getAuthString(), session);
        //Resign the game
        var username = service.resignGame(command.getGameID(), command.getAuthString());

        //Get the game data for notifications
        var gameID = command.getGameID();

        //Send a NotificationMessage to the other players
        var notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(username + " has resigned the game");
        broadcastMessage(gameID, notificationMessage, command.getAuthString());
    }

    private void sendMessage(Integer gameID, ServerMessage message, String authToken, Session session) throws ResponseException, IOException{
        if (session != null) {
            if (session.isOpen()) {
                String jsonMessage = new Gson().toJson(message);
                System.out.println("Sending message: " + jsonMessage);
                session.getRemote().sendString(jsonMessage);
            } else {
                System.out.println("Cannot send message. Session is closed.");
            }
        } else {
            System.out.println("Cannot send message. Session is null.");
        }
    }
    
    private void broadcastMessage(Integer gameID, ServerMessage message, String exceptThisAuthToken) throws IOException{
        sessions.getSessionsForGame(gameID).forEach((authToken, session) -> {
            if (authToken != exceptThisAuthToken) {
                try {
                    String jsonMessage = new Gson().toJson(message);
                    System.out.println("Broadcasting message: " + jsonMessage);
                    session.getRemote().sendString(jsonMessage);
                } catch (IOException e) {
                    System.out.println("Error broadcasting message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}