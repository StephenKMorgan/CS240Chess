package server.websocket;

import com.google.gson.Gson;

import chess.ChessGame.TeamColor;
import exception.ResponseException;
import model.GameData;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import webSocketMessages.serverMessages.ErrorMessage;
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
import java.util.Objects;

@WebSocket
public class WebSocketHandler {

    private WebSocketSessions sessions = new WebSocketSessions();;
    private Service service = new Service();

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        System.out.println("Connected");
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("Closed: " + statusCode + " " + reason);
        sessions.removeSession(session);
        session.close();
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.out.println("Error: " + error.getMessage());
        var errorMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR);
        errorMessage.setErrorMessage("Error: " + error.getMessage());
        try {
            System.out.println(new Gson().toJson(errorMessage));
            session.getRemote().sendString(new Gson().toJson(errorMessage));
        } catch (IOException e) {
            System.out.println("Error sending error message: " + e.getMessage());
            e.printStackTrace();
        }
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

    public void joinPlayer(JoinPlayerCommand command, Session session) {
        //If the username field in the command is null, look up the username from the authToken
        if (command.getUsername() == null) {
            try {
                command.setUsername(service.getUsernameFromAuthToken(command.getAuthString()));
            } catch (ResponseException e) {
                onError(session, e);
                return;
            }
        }

        // Get the joined game data
        GameData gameData;
        try {
            gameData = service.getGameData(command.getGameID(), command.getAuthString());
        } catch (ResponseException e) {
            onError(session, e);
            return;
        }

        // Get the game data for notifications
        var game = gameData.game();
        var username = command.getUsername();

        if (game.getTeamTurn() == TeamColor.FINISHED) {
            onError(session, new ResponseException(400, "Game is finished"));
            return;
        }
        if(gameData.whiteUsername() == null && gameData.blackUsername() == null) {
            onError(session, new ResponseException(400, "Game has not been created."));
            return;
        }
        if ((command.getPlayerColor() == TeamColor.WHITE &&  !gameData.whiteUsername().equals(command.getUsername())) || (command.getPlayerColor() == TeamColor.BLACK && !Objects.equals(gameData.blackUsername(), command.getUsername()))){
            onError(session, new ResponseException(400, "Username already taken"));
            return;
        }

        // Add the session to the game
        sessions.addSessionToGame(command.getGameID(), command.getAuthString(), session);

        // Send a LoadGameMessage to the player
        var loadMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadMessage.setGame(game);
        try {
            sendMessage(command.getGameID(), loadMessage, command.getAuthString(), session);
        } catch (ResponseException | IOException e) {
            onError(session, e);
            return;
        }

        // Send a NotificationMessage to the other players
        var notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(
                username + " has joined the game as the " + command.getPlayerColor().toString() + " player");
        try {
            broadcastMessage(command.getGameID(), notificationMessage, command.getAuthString());
        } catch (IOException e) {
            onError(session, e);
            return;
        }
    }

    public void joinObserver(JoinObserverCommand command, Session session) {
        //If the username field in the command is null, look up the username from the authToken
        if (command.getUsername() == null) {
            try {
                command.setUsername(service.getUsernameFromAuthToken(command.getAuthString()));
            } catch (ResponseException e) {
                onError(session, e);
                return;
            }
        }
        
        // Get the joined game data
        GameData gameData;
        try {
            gameData = service.getGameData(command.getGameID(), command.getAuthString());
        } catch (ResponseException e) {
            onError(session, e);
            return;
        }

        // Get the game data for notifications
        var game = gameData.game();
        var username = command.getUsername();

        if (game.getTeamTurn() == TeamColor.FINISHED) {
            onError(session, new ResponseException(400, "Game is finished"));
            return;
        }

        // Add the session to the game
        sessions.addSessionToGame(command.getGameID(), command.getAuthString(), session);

        // Send a LoadGameMessage to the player
        var loadMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadMessage.setGame(game);
        try {
            sendMessage(command.getGameID(), loadMessage, command.getAuthString(), session);
        } catch (ResponseException | IOException e) {
            onError(session, e);
            return;
        }

        // Send a NotificationMessage to the other players
        var notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(username + " has joined the game as an observer");
        try {
            broadcastMessage(command.getGameID(), notificationMessage, command.getAuthString());
        } catch (IOException e) {
            onError(session, e);
            return;
        }
    }

    public void makeMove(MakeMoveCommand command, Session session) {
        // Verify and make the move
        GameData gameData;
        try {
            gameData = service.makeMove(command.getGameID(), command.getAuthToken(), command.getMove());
        } catch (ResponseException e) {
            onError(session, e);
            return;
        }

        // Refresh the game data after the move
        try {
            gameData = service.getGameData(command.getGameID(), command.getAuthToken());
        } catch (ResponseException e) {
            onError(session, e);
            return;
        }

        // Get the game data for notifications
        var game = gameData.game();
        var move = command.getMove().toString();

        // Send a LoadGameMessage to all players
        var loadMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadMessage.setGame(game);
        try {
            sendMessage(command.getGameID(), loadMessage, command.getAuthString(), session);
        } catch (ResponseException | IOException e) {
            onError(session, e);
            return;
        }
        try {
            broadcastMessage(command.getGameID(), loadMessage, command.getAuthString());
        } catch (IOException e) {
            onError(session, e);
            return;
        }

        // Send a NotificationMessage to all players
        var notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(move);
        try {
            broadcastMessage(command.getGameID(), notificationMessage, command.getAuthString());
        } catch (IOException e) {
            onError(session, e);
            return;
        }
    }

    public void leaveGame(LeaveGameCommand command, Session session) {
        // Remove the session from the game
        sessions.removeSessionFromGame(command.getGameID(), command.getAuthString(), session);

        // Leave the game
        String username;
        try {
            username = service.leaveGame(command.getGameID(), command.getAuthString());
        } catch (ResponseException e) {
            onError(session, e);
            return;
        }

        // Get the game data for notifications
        var gameID = command.getGameID();

        // Refresh the game data after the move
        GameData gameData;
        try {
            gameData = service.getGameData(command.getGameID(), command.getAuthString());
        } catch (ResponseException e) {
            onError(session, e);
            return;
        }
        var game = gameData.game();
        // Set the current player to the word "Finished"
        game.setTeamTurn(TeamColor.FINISHED);
        // Send a LoadGameMessage to all players
        var loadMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadMessage.setGame(game);
        try {
            sendMessage(command.getGameID(), loadMessage, command.getAuthString(), session);
        } catch (ResponseException | IOException e) {
            onError(session, e);
            return;
        }
        try {
            broadcastMessage(command.getGameID(), loadMessage, command.getAuthString());
        } catch (IOException e) {
            onError(session, e);
            return;
        }

        // Send a NotificationMessage to the other players
        var notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(username + " has left the game");
        try {
            broadcastMessage(gameID, notificationMessage, command.getAuthString());
        } catch (IOException e) {
            onError(session, e);
            return;
        }
    }

    public void resignGame(ResignCommand command, Session session) {
        // Remove the session from the game
        sessions.removeSessionFromGame(command.getGameID(), command.getAuthString(), session);
        // Resign the game
        String username;
        try {
            username = service.resignGame(command.getGameID(), command.getAuthString());
        } catch (ResponseException e) {
            onError(session, e);
            return;
        }

        // Get the game data for notifications
        var gameID = command.getGameID();

        // Send a NotificationMessage to the other players
        var notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(username + " has resigned the game.");
        try {
            broadcastMessage(gameID, notificationMessage, command.getAuthString());
        } catch (IOException e) {
            onError(session, e);
            return;
        }
    }

    private void sendMessage(Integer gameID, ServerMessage message, String authToken, Session session)
            throws ResponseException, IOException {
        if (session != null) {
            if (session.isOpen()) {
                String jsonMessage = new Gson().toJson(message);
                System.out.println("Sending message to session: " + session + ", message: " + jsonMessage);
                session.getRemote().sendString(jsonMessage);
                session.getRemote().flush();
            } else {
                System.out.println("Cannot send message. Session is closed.");
            }
        } else {
            System.out.println("Cannot send message. Session is null.");
        }
    }

    private void broadcastMessage(Integer gameID, ServerMessage message, String exceptThisAuthToken)
            throws IOException {
        System.out.println("Broadcasting message: " + message);
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