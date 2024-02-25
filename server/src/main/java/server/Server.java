package server;

import spark.*;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import model.GameData;
import model.UserData;
import server.websocket.WebSocketHandler;
import service.Service;

import com.google.gson.Gson;

import exception.ResponseException;

public class Server {
    private final Service service;
    //private final WebSocketHandler webSocketHandler;

    public Server() {
        this.service = new Service();
        //this.webSocketHandler = new WebSocketHandler();
    }

    public Server(Service service) {
        this.service = service;
        //this.webSocketHandler = new WebSocketHandler();
    }


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");
//        var webDir = Paths.get(Server.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "web");
//        Spark.externalStaticFileLocation(webDir.toString());
        //Spark.webSocket("/connect", webSocketHandler);

        // Register your endpoints and handle exceptions here.
        Spark.exception(ResponseException.class, this::handleResponseException);
        Spark.get("/", (req, res) -> "CS 240 Chess Server Web API");
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearDB);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
    }

    public Object registerUser(Request req, Response res) throws ResponseException {
        try {
            var user = new Gson().fromJson(req.body(), UserData.class);
            return new Gson().toJson(service.register(user));
        } catch (ResponseException e) {
            System.out.println("ResponseException occurred during register: " + e);
            res.status(e.StatusCode());
            return new Gson().toJson(Collections.singletonMap("message", e.getMessage()));
        } catch (Exception e) {
            System.out.println("ResponseException occurred during register: " + e);
            res.status(500);
            return new Gson().toJson(Collections.singletonMap("message", "Error: Internal Server Error"));
        }
    }

    public Object login(Request req, Response res) {
        try {
            var user = new Gson().fromJson(req.body(), UserData.class);
            return new Gson().toJson(service.login(user));
        } catch (ResponseException e) {
            System.out.println("ResponseException occurred during login: " + e);
            res.status(e.StatusCode());
            return new Gson().toJson(Collections.singletonMap("message", e.getMessage()));
        } catch (Exception e) {
            System.out.println("ResponseException occurred during login: " + e);
            res.status(500);
            return new Gson().toJson(Collections.singletonMap("message", "Error: Internal Server Error"));
        }
    }

    public Object logout(Request req, Response res) throws ResponseException {
        try {
            service.logout(req.headers("Authorization"));
            res.status(200);
        } catch (ResponseException e) {
            System.out.println("ResponseException occurred during logout: " + e);
            res.status(e.StatusCode());
            return new Gson().toJson(Collections.singletonMap("message", e.getMessage()));
        } catch (Exception e) {
            System.out.println("ResponseException occurred during logout: " + e);
            res.status(500);
            return new Gson().toJson(Collections.singletonMap("message", "Error: Internal Server Error"));
        }
        return "";
    }

    public Object listGames(Request req, Response res) throws ResponseException {
        try {
            var games = service.listGames(req.headers("Authorization"));
            //Convert the hashmap of games to a JSON string and return it in the format: (add the "games" key to the JSON object)
            //for example: { "games": [{"gameID": 1234, "whiteUsername":"", "blackUsername":"", "gameName:""} ]}
            // Create a map with a single entry where the key is "games" and the value is the list of games
            Map<String, Object> response = new HashMap<>();
            response.put("games", games);
            // Convert the map to a JSON string
            String jsonResponse = new Gson().toJson(response);
            System.out.println(jsonResponse);
            return jsonResponse;
        } catch (ResponseException e) {
            System.out.println("ResponseException occurred during listGames: " + e);
            res.status(e.StatusCode());
            return new Gson().toJson(Collections.singletonMap("message", e.getMessage()));
        } catch (Exception e) {
            System.out.println("ResponseException occurred during listGames: " + e);
            res.status(500);
            return new Gson().toJson(Collections.singletonMap("message", "Error: Internal Server Error"));
        }
    }

    public Object createGame(Request req, Response res) throws ResponseException {
        try {
        var game = new Gson().fromJson(req.body(), model.GameData.class);
        game = service.createGame(req.headers("Authorization"), game.gameName());
        return new Gson().toJson(game);
        } catch (ResponseException e) {
            System.out.println("ResponseException occurred during createGame: " + e);
            res.status(e.StatusCode());
            return new Gson().toJson(Collections.singletonMap("message", e.getMessage()));
        } catch (Exception e) {
            System.out.println("ResponseException occurred during createGame: " + e);
            res.status(500);
            return new Gson().toJson(Collections.singletonMap("message", "Error: Internal Server Error"));
        }
    }

    public Object joinGame(Request req, Response res) throws ResponseException {
        try {
        var joinData = new Gson().fromJson(req.body(), model.JoinData.class);
        int gameID = joinData.gameID();
        var playerColor = joinData.playerColor();
        service.joinGame(playerColor, gameID, req.headers("Authorization"));
        res.status(200);
        } catch (ResponseException e) {
            System.out.println("ResponseException occurred during joinGame: " + e);
            res.status(e.StatusCode());
            return new Gson().toJson(Collections.singletonMap("message", e.getMessage()));
        } catch (Exception e) {
            System.out.println("ResponseException occurred during joinGame: " + e);
            res.status(500);
            return new Gson().toJson(Collections.singletonMap("message", "Error: Internal Server Error"));
        }
        return new Gson().toJson(Collections.singletonMap("message", "Success"));
    }

    public Object clearDB(Request req, Response res) throws ResponseException{
        try {
            service.clearAll();
        } catch (ResponseException e) {
            System.out.println("ResponseException occurred during clearAll: " + e);
            res.status(e.StatusCode());
            return new Gson().toJson(Collections.singletonMap("message", e.getMessage()));
        } catch (Exception e) {
            System.out.println("ResponseException occurred during clearAll: " + e);
            res.status(500);
            return new Gson().toJson(Collections.singletonMap("message", "Error: Internal Server Error"));
        }
        return new Gson().toJson(Collections.singletonMap("message", "Success"));
    }

    public void handleResponseException(ResponseException e, Request req, Response res) {
        res.status(e.StatusCode());
        res.body(e.getMessage());
    }

}
