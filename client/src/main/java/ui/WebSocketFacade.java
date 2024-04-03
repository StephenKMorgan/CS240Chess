package ui;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import webSocketMessages.serverMessages.ErrorMessage;
import webSocketMessages.serverMessages.LoadGameMessage;
import webSocketMessages.serverMessages.NotificationMessage;
import webSocketMessages.userCommands.*;
import chess.ChessGame;
import chess.ChessMove;
import exception.ResponseException;

public class WebSocketFacade extends Endpoint {

    private Session session;
    private GameHandler gameHandler;
    private Game game;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
    }

    @OnClose
    public void onClose(){
    }

    @OnError
    public void onError(){
    }

    public WebSocketFacade(String url, Game game) throws ResponseException {
        try {
            this.game = game;
            //convert the http link to a ws link and add /connect to the end
            url = url.replace("http://", "ws://") + "/connect";
            WebSocketContainer container=ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, new URI(url));
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @OnMessage
                public void onMessage(String message) {                    
                receivedMessage(message);
            }
             });
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
        var resignGameCommand = new ResignCommand(authToken);
        resignGameCommand.setGameID(gameID);

        //Send the message
        sendMessage(resignGameCommand);
    }
    
    public void receivedMessage(String message) {
         // Parse the JSON into a JsonObject
         var gson = new Gson();
         var jsonElement = gson.fromJson(message, JsonElement.class);
         var jsonObject = jsonElement.getAsJsonObject();

         // Extract the type of the message
         var messageType = jsonObject.get("serverMessageType").getAsString();

         // Deserialize into the specific message type based on the type information
         switch (messageType) {
             case "LOAD_GAME":
                 var loadGameMessage = gson.fromJson(jsonObject, LoadGameMessage.class);
                 game.updateGame(loadGameMessage.getGame(), loadGameMessage.getWhiteUsername(), loadGameMessage.getBlackUsername());
                 break;
             case "NOTIFICATION":
                 var notificationMessage = gson.fromJson(jsonObject, NotificationMessage.class);
                 game.printMessage(notificationMessage.getMessage());
                 break;
             case "ERROR":
                 var errorMessage = gson.fromJson(jsonObject, ErrorMessage.class);
                 game.printMessage(errorMessage.getErrorMessage());
                 break;
             default:
                 System.out.println("Unknown message type: " + messageType);
                 break;
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
