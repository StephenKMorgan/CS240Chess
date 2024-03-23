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

import webSocketMessages.serverMessages.ErrorMessage;
import webSocketMessages.serverMessages.LoadGameMessage;
import webSocketMessages.serverMessages.NotificationMessage;
import webSocketMessages.userCommands.JoinObserverCommand;
import webSocketMessages.userCommands.JoinPlayerCommand;
import webSocketMessages.userCommands.LeaveGameCommand;
import webSocketMessages.userCommands.MakeMoveCommand;
import chess.ChessGame;
import chess.ChessMove;
import exception.ResponseException;

public class WebSocketFacade extends Endpoint implements MessageHandler.Whole<String>{

    private Session session;
    private GameHandler gameHandler;

    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
    }

    public void onClose(){}

    public void onError(){}

    public WebSocketFacade(String url) throws ResponseException {
        try {
            //convert the http link to a ws link and add /connect to the end
            url = url.replace("http://", "ws://") + "/connect";
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            var newSession = container.connectToServer(this, new URI(url));
            onOpen(newSession, null);
        } catch (DeploymentException | IOException | URISyntaxException e) {
            throw new ResponseException(500, "Failed: 500 Failed to connect to the server");
        }
    }


    //Outgoing messages
    public void joinPlayer(String authToken, Integer gameID, String username, ChessGame.TeamColor playerColor) {
        //Create the join player message as a join player command
        var joinPlayerCommand = new JoinPlayerCommand(authToken);
        joinPlayerCommand.setGameID(gameID);
        joinPlayerCommand.setUsername(username);
        joinPlayerCommand.setPlayerColor(playerColor);

        //Send the message
        sendMessage(joinPlayerCommand);
    }

    public void joinObserver(String authToken, Integer gameID, String username) {
        //Create the join observer message as a join observer command
        var joinObserverCommand = new JoinObserverCommand(authToken);
        joinObserverCommand.setGameID(gameID);
        joinObserverCommand.setUsername(username);

        //Send the message
        sendMessage(joinObserverCommand);
    }

    public void makeMove(String authToken, Integer gameID, ChessMove move) {
        //Create the make move message as a make move command
        var makeMoveCommand = new MakeMoveCommand(authToken);
        makeMoveCommand.setGameID(gameID);
        makeMoveCommand.setMove(move);

        //Send the message
        sendMessage(makeMoveCommand);
    }

    public void leaveGame(String authToken, Integer gameID) {
        //Create the leave game message as a leave game command
        var leaveGameCommand = new LeaveGameCommand(authToken);
        leaveGameCommand.setGameID(gameID);

        //Send the message
        sendMessage(leaveGameCommand);
    }

    public void resignGame(String authToken, Integer gameID) {
        //Create the resign game message as a resign game command
        var resignGameCommand = new LeaveGameCommand(authToken);
        resignGameCommand.setGameID(gameID);

        //Send the message
        sendMessage(resignGameCommand);
    }
    
    @Override
    public void onMessage(String message) {
       //Deserialize the message
        var gson = new Gson();
        var messageObject = gson.fromJson(message, Object.class);
        System.out.println("Message received: " + messageObject);
        //Call gameHandler to process the message
        if (messageObject instanceof LoadGameMessage) {
            //cast the message to a LoadGameMessage and call the updateGame method
            System.out.println("LoadGameMessage received");
            gameHandler.updateGame(((LoadGameMessage) messageObject).getGame());
        } else if (messageObject instanceof NotificationMessage) {
            System.out.println("NotificationMessage received");
            gameHandler.printMessage(((NotificationMessage) messageObject).getMessage());
        } else if (messageObject instanceof ErrorMessage) {
            System.out.println("ErrorMessage received");
            gameHandler.printMessage(((ErrorMessage) messageObject).getError());
        }
    }

    private void sendMessage(Object message) {
        if (this.session != null && this.session.isOpen()) {
            try {
                this.session.getBasicRemote().sendText(new Gson().toJson(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Cannot send message. Session is either null or closed.");
        }
    }
}
