package server;

import spark.*;

import java.nio.file.Paths;
import model.UserData;
import service.Service;

import com.google.gson.Gson;

import exception.ResponseException;

public class Server {
    private final Service service;

    public Server(Service service) {
        this.service = service;
    }


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        var webDir = Paths.get(Server.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "web");
        Spark.externalStaticFileLocation(webDir.toString());

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        



        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
    }

    public Object registerUser(Request req, Response res) throws ResponseException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        user = service.register(user);
        return new Gson().toJson(user);
    }

    public Object login(Request req, Response res) throws ResponseException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        user = service.login(user);
        return new Gson().toJson(user);
    }

    public Object logout(Request req, Response res) throws ResponseException {
        service.logout(req.headers("Authorization"));
        return "";
    }
}
