package server;

import spark.*;

import java.nio.file.Paths;
import model.UserData;
import server.websocket.WebSocketHandler;
import service.Service;

import com.google.gson.Gson;

import exception.ResponseException;

public class Server {
    private final Service service;
    private final WebSocketHandler webSocketHandler;

    public Server() {
        this.service = new Service();
        this.webSocketHandler = new WebSocketHandler();
    }

    public Server(Service service) {
        this.service = service;
        this.webSocketHandler = new WebSocketHandler();
    }


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");
//        var webDir = Paths.get(Server.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "web");
//        Spark.externalStaticFileLocation(webDir.toString());
        Spark.webSocket("/connect", webSocketHandler);

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearDB);
        Spark.exception(ResponseException.class, this::handleResponseException);

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
        } catch (Exception e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
    }

    public Object login(Request req, Response res) throws ResponseException {
        try {
            var user = new Gson().fromJson(req.body(), UserData.class);
            return new Gson().toJson(service.login(user));
        } catch (Exception e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
    }

    public Object logout(Request req, Response res) throws ResponseException {
        try {
            service.logout(req.headers("Authorization"));
            res.status(204);
        } catch (Exception e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
        return "";
    }

    public Object listGames(Request req, Response res) throws ResponseException {
        try {
            return new Gson().toJson(service.listGames(req.headers("Authorization")));
        } catch (Exception e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
    }

    public Object createGame(Request req, Response res) throws ResponseException {
        try {
        var game = new Gson().fromJson(req.body(), model.GameData.class);
        game = service.createGame(req.headers("Authorization"), game.gameName());
        return new Gson().toJson(game);
        } catch (Exception e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
    }

    public Object joinGame(Request req, Response res) throws ResponseException {
        try {
        var gameID = Integer.parseInt(req.params("gameID"));
        service.joinGame(req.queryParams("clientColor"), gameID, req.headers("Authorization"));
        res.status(204);
        } catch (Exception e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
        return "";
    }

    public Object clearDB(Request req, Response res) throws ResponseException{
        try {
            service.clearAll();
        } catch (Exception e) {
            throw new ResponseException(500, "Error: Internal Server Error");
        }
        return "";
    }

    public void handleResponseException(ResponseException e, Request req, Response res) {
        res.status(e.StatusCode());
        res.body(e.getMessage());
    }

}
