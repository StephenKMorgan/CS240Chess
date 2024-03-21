package server.websocket;

import com.google.gson.Gson;
import dataAccess.DataAccess;
import exception.ResponseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import webSocketMessages.Action;
import webSocketMessages.Notification;
import webSocketMessages.userCommands.JoinPlayerCommand;
import webSocketMessages.userCommands.UserGameCommand;

import java.io.IOException;
import java.util.Timer;


@WebSocket
public class WebSocketHandler {

    private WebSocketSessions sessions;

    

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        System.out.println("Connected");
        sessions = new WebSocketSessions();
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
    public void onMessage(Session session, String message) throws IOException, ResponseException {
        System.out.println("Message: " + message);
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case JOIN_PLAYER:
                joinPlayer((JoinPlayerCommand) command, session);
                break;
            default:
                throw new ResponseException(500, "Invalid command type");
        }
    }

    public void joinPlayer(JoinPlayerCommand command, Session session) {
        sessions.addSessionToGame(command.getGameID(), command.getAuthToken(), session);
    }

    public void joinObserver() {
        sessions.addSessionToGame(0, "observer", null);
    }

    



}