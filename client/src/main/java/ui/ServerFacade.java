package ui;

import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.JoinData;
import model.UserData;

import java.io.*;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    //Spark Endpoints:
    // Spark.post("/user", this::registerUser);
    // Spark.post("/session", this::login);
    // Spark.delete("/session", this::logout);
    // Spark.get("/game", this::listGames);
    // Spark.post("/game", this::createGame);
    // Spark.put("/game", this::joinGame);

    public AuthData registerUser(String username, String password, String email) throws ResponseException {
        var path = "/user/register";
        var request = new UserData(username, password, email);
        return this.makeRequest("POST", path, request, AuthData.class, null);
    }

    public AuthData loginUser(String username, String password) throws ResponseException {
        var path = "/session/login";
        var request = new UserData(username, password, null);
        return this.makeRequest("POST", path, request, AuthData.class, null);
    }

    public void logoutUser(String token) throws ResponseException {
        var path = "/session/logout";
        this.makeRequest("DELETE", path, null, null, token);
    }

    public GameData[] listGames(String token) throws ResponseException {
        var path = "/game";
        return this.makeRequest("GET", path, null, GameData[].class, token);
    }

    public GameData createGame(String token, String gameName) throws ResponseException {
        var path = "/game";
        var request = new GameData(0, null, null, gameName, null);
        return this.makeRequest("POST", path, request, GameData.class, token);
    }

    public GameData joinGame(String token, int gameId, String color) throws ResponseException {
        var path = "/game";
        var request = new JoinData(color, gameId);
        return this.makeRequest("PUT", path, request, GameData.class, token);
    }

private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String token) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            if (token != null) {
                http.setRequestProperty("Authorization", "Bearer " + token);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}


