package ui;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.google.gson.Gson;

import webSocketMessages.serverMessages.ServerMessage;

import chess.ChessGame;
import exception.ResponseException;

public class WebSocketFacade extends Endpoint {

    private Session session;
    private NotificationHandler notificationHandler;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/connect");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
                    notificationHandler.notify(notification);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

// User Game Commands
// Command	Required Fields	Description
// JOIN_PLAYER	Integer gameID, ChessGame.TeamColor playerColor	Used for a user to request to join a game.
// JOIN_OBSERVER	Integer gameID	Used to request to start observing a game.
// MAKE_MOVE	Integer gameID, ChessMove move	Used to request to make a move in a game.
// LEAVE	Integer gameID	Tells the server you are leaving the game so it will stop sending you notifications.
// RESIGN	Integer gameID	Forfeits the match and ends the game (no more moves can be made).
// Server Messages
// Command	Required Fields	Description
// LOAD_GAME	game (can be any type, just needs to be called game)	Used by the server to send the current game state to a client. When a client receives this message, it will redraw the chess board.
// ERROR	String errorMessage	This message is sent to a client when it sends an invalid command. The message must include the word Error.
// NOTIFICATION	String message	This is a message meant to inform a player when another player made an action.

   


@Override
public void onOpen(Session arg0, EndpointConfig arg1) {
    
}
}
