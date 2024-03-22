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

import webSocketMessages.userCommands.JoinPlayerCommand;
import webSocketMessages.userCommands.LeaveGameCommand;
import webSocketMessages.userCommands.MakeMoveCommand;
import chess.ChessGame;
import chess.ChessMove;
import exception.ResponseException;

public class WebSocketFacade extends Endpoint implements MessageHandler.Whole<String>{

    private Session session;
    private GameHandler gameHandler;

    public void onOpen(Session session, EndpointConfig config) {}

    public void onClose(){}

    public void onError(){}

    public WebSocketFacade(String url, GameHandler gameHandler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/connect");
            this.gameHandler = gameHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

           
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
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
        var joinObserverCommand = new JoinPlayerCommand(authToken);
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
    
    
    public void onMessage(String message) {
       //Deserialize the message
        var gson = new Gson();
        var messageObject = gson.fromJson(message, Object.class);

        //Call gameHandler to process the message
        if (messageObject instanceof ChessGame) {
            gameHandler.updateGame((ChessGame) messageObject);
        } else if (messageObject instanceof String) {
            gameHandler.printMessage((String) messageObject);
        }
    }
    
   
    private void sendMessage(Object message) {
        try {
            this.session.getBasicRemote().sendText(new Gson().toJson(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
  
}
