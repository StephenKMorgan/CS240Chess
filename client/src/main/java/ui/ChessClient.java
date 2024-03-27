package ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import chess.ChessPiece;
import chess.ChessPosition;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.GameResponseData;
import server.Server;


public class ChessClient {

    public Status status = Status.LoggedOut;
    public ServerFacade server;
    public Server database;
    public String url;
    public AuthData authData;
    public Boolean isRunning = true;
    public GameData gameData;


    public static void main(String[] args) {
        var client = new ChessClient();

        client.run();
    }

    public ChessClient() {
        server = new ServerFacade("http://localhost:4567");
         this.url = "http://localhost:4567";
        // this.database = new Server();
        // database.run(4567);

    }

    public ChessClient(String url) {
        server = new ServerFacade(url);
         this.url = url;
        // this.database = new Server();
        // var port = Integer.parseInt(url.split(":")[2]);
        // database.run(port);
    }

    public void  run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(EscapeSequences.ERASE_SCREEN);
        System.out.println(EscapeSequences.SET_TEXT_BOLD + "Welcome to Chess!" + EscapeSequences.RESET_TEXT_BOLD_FAINT);
        System.out.println("Type 'help' for a list of commands or 'quit' to exit the program.");
        while (isRunning) {
            System.out.print(EscapeSequences.SET_TEXT_BOLD + this.status + EscapeSequences.RESET_TEXT_BOLD_FAINT + " >>>> ");
            var input = scanner.nextLine();
            var output = inputParser(input);
            System.out.println(output);
        }
        System.exit(0);
    }

    public String inputParser(String input){
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "quit" -> quit();
            case "help" -> help();
            case "register" -> params.length < 3 ? EscapeSequences.SET_TEXT_COLOR_RED +"Missing parameters. Usage: register <username> <password> <email>"+ EscapeSequences.SET_TEXT_COLOR_WHITE : register(params[0], params[1], params[2]);
            case "login" -> params.length < 2 ? EscapeSequences.SET_TEXT_COLOR_RED +"Missing parameters. Usage: login <username> <password>"+ EscapeSequences.SET_TEXT_COLOR_WHITE : login(params[0], params[1]);
            case "logout" -> logout();
            case "list" -> listGames();
            case "create" -> params.length < 1 ? EscapeSequences.SET_TEXT_COLOR_RED +"Missing parameters. Usage: create <game name>"+ EscapeSequences.SET_TEXT_COLOR_WHITE : createGame(String.join(" ", params));
            case "join" -> params.length < 1 ? EscapeSequences.SET_TEXT_COLOR_RED +"Missing parameters. Usage: join <game id> [white|black|<empty>]"+ EscapeSequences.SET_TEXT_COLOR_WHITE : joinGame(params[0], params.length < 2 ? null : params[1]);
            case "observe" -> params.length < 1 ? EscapeSequences.SET_TEXT_COLOR_RED +"Missing parameters. Usage: observe <game id>"+ EscapeSequences.SET_TEXT_COLOR_WHITE : observeGame(params[0]);
            default -> help();
        };
    }

    public String help() {
        if (status == Status.LoggedIn) {
            return(
                """
                Available commands:
                - Create <name> - Create a new game
                - List - List all available games
                - Join <id> [white|black|<empty>]- Join a game
                - Observe <id> - Observe a game
                - Logout - Log out of the current account
                - Quit - Exit the program
                - Help - Display this message
                """
            );
        } else {
            return(
                """
                Available commands:
                - Register <username> <password> <email> - Register a new account
                - Login <username> <password> - Log in to an existing account
                - Quit - Exit the program
                - Help - Display this message                        
                """
            );
        }
    }

    public String register(String username, String password, String email) {
        if(this.status == Status.LoggedIn){
            return EscapeSequences.SET_TEXT_COLOR_RED + "You must be logged out to register a new user."+ EscapeSequences.SET_TEXT_COLOR_WHITE;
        }
        try {
            AuthData user = server.registerUser(username, password, email);
            this.authData = user;
            this.status = Status.LoggedIn;
            return EscapeSequences.SET_TEXT_COLOR_GREEN + "User " + user.username() + " registered successfully!"+ EscapeSequences.SET_TEXT_COLOR_WHITE;
        } catch (ResponseException ex) {
            return errorParsing(Method.register, ex.getMessage());
        }
    }

    public String login(String username, String password) {
        if(this.status == Status.LoggedIn){
            return EscapeSequences.SET_TEXT_COLOR_RED + "You are already logged in."+ EscapeSequences.SET_TEXT_COLOR_WHITE;
        }
        try {
            AuthData user = server.loginUser(username, password);
            this.authData = user;
            this.status = Status.LoggedIn;
            return EscapeSequences.SET_TEXT_COLOR_GREEN + "User " + user.username() + " logged in successfully!"+ EscapeSequences.SET_TEXT_COLOR_WHITE;
        } catch (ResponseException ex) {
            return errorParsing(Method.login, ex.getMessage());
        }
    }

    public String logout() {
        if (this.status == Status.LoggedOut){
            return EscapeSequences.SET_TEXT_COLOR_RED + "You are already logged out."+ EscapeSequences.SET_TEXT_COLOR_WHITE;
        }
        try {
            server.logoutUser(authData.authToken());
            var username = this.authData.username();
            this.authData = null;
            this.status = Status.LoggedOut;
            return EscapeSequences.SET_TEXT_COLOR_GREEN + "The user " + username +" logged out successfully!"+ EscapeSequences.SET_TEXT_COLOR_WHITE;
        } catch (ResponseException ex) {
            return errorParsing(Method.logout, ex.getMessage());
        }
    }

    public String listGames() {
        if(this.status == Status.LoggedOut){
            return EscapeSequences.SET_TEXT_COLOR_RED + "You must be logged in to list games."+ EscapeSequences.SET_TEXT_COLOR_WHITE;
        }
        try {
            var games = server.listGames(authData.authToken());
            List<GameData> sortedGames = new ArrayList<>(games.games());
            sortedGames.sort(Comparator.comparingInt(GameData::gameID));
            String output = "Available games:\n";
            for (GameData game : sortedGames) {
                output += String.format(
                    "%-10s %-30s %-20s %-20s%n",
                    "Game ID: " + game.gameID(),
                    "| Game Name: " + game.gameName(),
                    "| White: " + (game.whiteUsername() == null ? "empty" : game.whiteUsername()),
                    "| Black: " + (game.blackUsername() == null ? "empty" : game.blackUsername())
                );
            }
            return output;
        } catch (ResponseException ex) {
            return errorParsing(Method.listGames, ex.getMessage());
        }
    }

    public String createGame(String gameName) {
        if(this.status == Status.LoggedOut){
            return EscapeSequences.SET_TEXT_COLOR_RED + "You must be logged in to create a game."+ EscapeSequences.SET_TEXT_COLOR_WHITE;
        }
        try {
            var game = server.createGame(authData.authToken(), gameName);
            return EscapeSequences.SET_TEXT_COLOR_GREEN + "Game " + game.gameName() + " created successfully!"+ EscapeSequences.SET_TEXT_COLOR_WHITE;
        } catch (ResponseException ex) {
            return errorParsing(Method.createGame, ex.getMessage());
        }
    }

    public String joinGame(String gameId, String color) {
        int ID = 0;
        if(this.status == Status.LoggedOut){
            return EscapeSequences.SET_TEXT_COLOR_RED + "You must be logged in to join a game."+ EscapeSequences.SET_TEXT_COLOR_WHITE;
        }
        if (color != null && !color.isBlank() && !color.equalsIgnoreCase("white") && !color.equalsIgnoreCase("black")) {
            return EscapeSequences.SET_TEXT_COLOR_RED + "Invalid color. Use 'white', 'black' or leave it empty."+ EscapeSequences.SET_TEXT_COLOR_WHITE;
        }
        try{
            ID = Integer.parseInt(gameId);
        } catch (NumberFormatException ex) {
            return EscapeSequences.SET_TEXT_COLOR_RED + "Invalid game ID."+ EscapeSequences.SET_TEXT_COLOR_WHITE;
        }
        try {
            this.gameData = server.joinGame(authData.authToken(), ID, color);
            new Game(this.gameData, this.url, this.authData, color).startGame();
            return ""; 

        } catch (ResponseException ex) {
            return errorParsing(Method.joinGame, ex.getMessage());
        } catch (Exception ex) {
            return EscapeSequences.SET_TEXT_COLOR_RED + "Failed to join game: " + ex.getMessage()+ EscapeSequences.SET_TEXT_COLOR_WHITE;
        }
    }

    public String observeGame(String gameId) {
        int ID = 0;
        if(this.status == Status.LoggedOut){
            return EscapeSequences.SET_TEXT_COLOR_RED + "You must be logged in to observe a game." + EscapeSequences.SET_TEXT_COLOR_WHITE;
        }
        try{
            ID = Integer.parseInt(gameId);
        } catch (NumberFormatException ex) {
            return EscapeSequences.SET_TEXT_COLOR_RED + "Invalid game ID."+ EscapeSequences.SET_TEXT_COLOR_WHITE;
        }
        try {
            var game = server.joinGame(authData.authToken(), ID, null);
            this.gameData = game;
            new Game(this.gameData, this.url, this.authData, null).startGame();
            return "";
        } catch (ResponseException ex) {
            return errorParsing(Method.observeGame, ex.getMessage());
        }
    }

    public String quit() {
        if (this.status == Status.LoggedIn){
            this.logout();
        }
        this.status = Status.LoggedOut;
        this.isRunning = false;
        // database.stop();
        return "Goodbye!";
    }

    private enum Method {
        register,
        login,
        logout,
        listGames,
        createGame,
        joinGame,
        observeGame
    }

    private String errorParsing(Method method, String message) {
        var code = message.split(": |-")[1];
        switch (code) {
            case "400":
            if (method == Method.register) {
                return EscapeSequences.SET_TEXT_COLOR_RED + "Please retry your registration." + EscapeSequences.SET_TEXT_COLOR_WHITE;
            }
            return EscapeSequences.SET_TEXT_COLOR_RED + "Please retry your entry." + EscapeSequences.SET_TEXT_COLOR_WHITE;
            case "401":
                if (method == Method.login) {
                    return EscapeSequences.SET_TEXT_COLOR_RED + "Invalid username or password." + EscapeSequences.SET_TEXT_COLOR_WHITE;
                }
                return EscapeSequences.SET_TEXT_COLOR_RED + "You are currently unauthorized from preforming this function. \nPlease logout or quit and login again." + EscapeSequences.SET_TEXT_COLOR_WHITE;
            case "403":
                if (method == Method.register) {
                    return EscapeSequences.SET_TEXT_COLOR_RED + "User already exists." + EscapeSequences.SET_TEXT_COLOR_WHITE;
                }
                if (method == Method.joinGame) {
                    return EscapeSequences.SET_TEXT_COLOR_RED + "This color is already taken. Please choose another one." + EscapeSequences.SET_TEXT_COLOR_WHITE;
                }
                return EscapeSequences.SET_TEXT_COLOR_RED + "You are currently unauthorized from preforming this function. \nPlease logout or quit and login again." + EscapeSequences.SET_TEXT_COLOR_WHITE;
            case "500":
                return EscapeSequences.SET_TEXT_COLOR_RED + "Internal Server Error." + EscapeSequences.SET_TEXT_COLOR_WHITE;
            default:
                return EscapeSequences.SET_TEXT_COLOR_RED + "An unknown error has occurred." + EscapeSequences.SET_TEXT_COLOR_WHITE;
        }
    }
}
