package ui;

import java.util.Arrays;
import java.util.Scanner;

import exception.ResponseException;
import model.AuthData;


public class ChessClient {


    private Status status = Status.LoggedOut;
    private ServerFacade server;
    private String url;
    private AuthData authData;
    private Boolean isRunning = true;

    public static void main(String[] args) {
        var client = new ChessClient();
        client.run();
    }

    public ChessClient() {
        server = new ServerFacade("http://localhost:4567");
        this.url = "http://localhost:4567";
    }

    public ChessClient(String url) {
        server = new ServerFacade(url);
        this.url = url;
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
            case "register" -> params.length < 3 ? "Missing parameters. Usage: register <username> <password> <email>" : register(params[0], params[1], params[2]);
            case "login" -> params.length < 2 ? "Missing parameters. Usage: login <username> <password>" : login(params[0], params[1]);
            case "logout" -> logout();
            case "list" -> listGames();
            case "create" -> params.length < 1 ? "Missing parameters. Usage: create <game name>" : createGame(String.join(" ", params));
            case "join" -> params.length < 2 ? "Missing parameters. Usage: join <game id> <password>" : joinGame(Integer.parseInt(params[0]), params[1]);
            case "observe" -> params.length < 1 ? "Missing parameters. Usage: observe <game id>" : observeGame(Integer.parseInt(params[0]));
            case "devdebug" -> devDebug();
            case "help" -> help();
            default -> help();
        };
    }

    public String devDebug () {

        return """
        Dev Debug:
        - Status: """ + status + """
        - AuthData: """ + authData + """
        - Server: """ + server + """
                """;        
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
        try {
            AuthData user = server.registerUser(username, password, email);
            this.authData = user;
            this.status = Status.LoggedIn;
            return "User " + user.username() + " registered successfully!";
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String login(String username, String password) {
        try {
            AuthData user = server.loginUser(username, password);
            this.authData = user;
            this.status = Status.LoggedIn;
            return "User " + user.username() + " logged in successfully!";
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String logout() {
        try {
            server.logoutUser(authData.authToken());
            this.authData = null;
            this.status = Status.LoggedOut;
            return "User logged out successfully!";
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String listGames() {
        if(this.status == Status.LoggedOut){
            return "You must be logged in to list games.";
        }
        try {
            var games = server.listGames(authData.authToken());
            var output = "Available games:\n";
            for (var game : games) {
                output += game.gameID() + " - " + game.gameName() + "\n";
            }
            return output;
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String createGame(String gameName) {
        if(this.status == Status.LoggedOut){
            return "You must be logged in to create a game.";
        }
        try {
            var game = server.createGame(authData.authToken(), gameName);
            return "Game " + game.gameName() + " created successfully!";
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String joinGame(int gameId, String color) {
        if(this.status == Status.LoggedOut){
            return "You must be logged in to join a game.";
        }
        try {
            var game = server.joinGame(authData.authToken(), gameId, color);
            return "Game " + game.gameName() + " joined successfully!";
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String observeGame(int gameId) {
        if(this.status == Status.LoggedOut){
            return "You must be logged in to observe a game.";
        }
        try {
            var game = server.joinGame(authData.authToken(), gameId, null);
            return "Game " + game.gameName() + " observed successfully!";
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String quit() {
        if (this.status == Status.LoggedIn){
            this.logout();
        }
        this.status = Status.LoggedOut;
        this.isRunning = false;
        return "Goodbye!";
    }


    
}
