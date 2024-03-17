package ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.GameResponseData;


public class ChessClient {


    private Status status = Status.LoggedOut;
    private ServerFacade server;
    private String url;
    private AuthData authData;
    private Boolean isRunning = true;
    private GameData gameData;

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
            case "join" -> params.length < 1 ? "Missing parameters. Usage: join <game id>" : joinGame(Integer.parseInt(params[0]), params[1]);
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
        if(this.status == Status.LoggedIn){
            return "You must be logged out to register a new user.";
        }
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
            //if the error is a 401, the user entered the wrong password or username
            if (ex.getMessage().contains("401")) {
                return "Invalid username or password.";
            }
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
            List<GameData> sortedGames = new ArrayList<>(games.games());
            sortedGames.sort(Comparator.comparingInt(GameData::gameID));
            String output = "Available games:\n";
            for (GameData game : sortedGames) {
                output += "Game " + game.gameID() + ": " + game.gameName() + "\n";
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
        if (!color.equalsIgnoreCase("white") && !color.equalsIgnoreCase("black") && !color.equalsIgnoreCase("")) {
            return "Invalid color. Use 'white', 'black' or leave it empty.";
        }
        try {
            var game = server.joinGame(authData.authToken(), gameId, color);
            this.gameData = game;
            return displayBoard(color);

        } catch (ResponseException ex) {
            if (ex.getMessage().contains("403")) {
                return "This color is already taken. Please choose another one.";
            }
            return ex.getMessage();
        } catch (Exception ex) {
            return "Failed to join game: " + ex.getMessage();
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

    private String displayBoard(String color) {
        if (this.gameData == null) {
            return "No game data available.";
        }
        //game contains the game data in this form {"turn": "WHITE", "board": {"board": [[{"type": "ROOK", "checkMoves": [], "pieceColor": "BLACK", "validMoves": []}, {"type": "KNIGHT", "checkMoves": [], "pieceColor": "BLACK", "validMoves": []}, {"type": "BISHOP", "checkMoves": [], "pieceColor": "BLACK", "validMoves": []}, {"type": "QUEEN", "checkMoves": [], "pieceColor": "BLACK", "validMoves": []}, {"type": "KING", "checkMoves": [], "pieceColor": "BLACK", "validMoves": []}, {"type": "BISHOP", "checkMoves": [], "pieceColor": "BLACK", "validMoves": []}, {"type": "KNIGHT", "checkMoves": [], "pieceColor": "BLACK", "validMoves": []}, {"type": "ROOK", "checkMoves": [], "pieceColor": "BLACK", "validMoves": []}], [{"type": "PAWN", "checkMoves": [], "pieceColor": "BLACK", "validMoves": []}, {"type": "PAWN", "checkMoves": [], "pieceColor": "BLACK", "validMoves": []}, {"type": "PAWN", "checkMoves": [], "pieceColor": "BLACK", "validMoves": []}, {"type": "PAWN", "checkMoves": [], "pieceColor": "BLACK", "validMoves": []}, {"type": "PAWN", "checkMoves": [], "pieceColor": "BLACK", "validMoves": []}, {"type": "PAWN", "checkMoves": [], "pieceColor": "BLACK", "validMoves": []}, {"type": "PAWN", "checkMoves": [], "pieceColor": "BLACK", "validMoves": []}, {"type": "PAWN", "checkMoves": [], "pieceColor": "BLACK", "validMoves": []}], [null, null, null, null, null, null, null, null], [null, null, null, null, null, null, null, null], [null, null, null, null, null, null, null, null], [null, null, null, null, null, null, null, null], [{"type": "PAWN", "checkMoves": [], "pieceColor": "WHITE", "validMoves": []}, {"type": "PAWN", "checkMoves": [], "pieceColor": "WHITE", "validMoves": []}, {"type": "PAWN", "checkMoves": [], "pieceColor": "WHITE", "validMoves": []}, {"type": "PAWN", "checkMoves": [], "pieceColor": "WHITE", "validMoves": []}, {"type": "PAWN", "checkMoves": [], "pieceColor": "WHITE", "validMoves": []}, {"type": "PAWN", "checkMoves": [], "pieceColor": "WHITE", "validMoves": []}, {"type": "PAWN", "checkMoves": [], "pieceColor": "WHITE", "validMoves": []}, {"type": "PAWN", "checkMoves": [], "pieceColor": "WHITE", "validMoves": []}], [{"type": "ROOK", "checkMoves": [], "pieceColor": "WHITE", "validMoves": []}, {"type": "KNIGHT", "checkMoves": [], "pieceColor": "WHITE", "validMoves": []}, {"type": "BISHOP", "checkMoves": [], "pieceColor": "WHITE", "validMoves": []}, {"type": "QUEEN", "checkMoves": [], "pieceColor": "WHITE", "validMoves": []}, {"type": "KING", "checkMoves": [], "pieceColor": "WHITE", "validMoves": []}, {"type": "BISHOP", "checkMoves": [], "pieceColor": "WHITE", "validMoves": []}, {"type": "KNIGHT", "checkMoves": [], "pieceColor": "WHITE", "validMoves": []}, {"type": "ROOK", "checkMoves": [], "pieceColor": "WHITE", "validMoves": []}]]}}
        var gameInfo = this.gameData.game();
        //break down the game data into the board and the turn
        var board = gameInfo.getBoard();
        var turn = gameInfo.getTeamTurn();
        var output = "Game " + this.gameData.gameName() + ":\n";
        //display the board
        for (int i = 0; i < board.size(); i++) {
            for (int j = 0; j < board.get(i).size(); j++) {
                var piece = board.get(i).get(j);
                if (piece == null) {
                    output += "  ";
                } else {
                    output += piece.getPieceColor().equalsIgnoreCase("WHITE") ? "W" : "B";
                    output += piece.getType().substring(0, 1);
                }
            }
            output += "\n";
        }
    }
    
}
