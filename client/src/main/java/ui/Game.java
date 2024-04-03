package ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import exception.ResponseException;
import model.AuthData;
import model.GameData;

/**
 * ChessGameREPL
 */
public class Game implements GameHandler {
    private GameData gameData;
    private String url;
    private AuthData authData;
    private String color;
    private Boolean isObserver = false; 
    private WebSocketFacade webSocketFacade;
    private boolean isRunning = true;
    private ChessPosition highlightPosition = null;
    private Collection<ChessPosition> highlightMoves = null;
    private Scanner scanner = new Scanner(System.in);

    public Game(GameData gameData, String url, AuthData authData, String givenColor) throws ResponseException {
        this.gameData = gameData;
        this.url = url;
        this.authData = authData;
        if (givenColor != null) {
            this.color = givenColor;
            this.isObserver = false;
        } else {
            this.color = "white";
            this.isObserver = true;
        }
        this.webSocketFacade = new WebSocketFacade(url, this);
    }

    public void startGame() {
      
        if(isObserver) { webSocketFacade.joinObserver(this.authData.authToken(), this.gameData.gameID(), this.authData.username());}
        else { webSocketFacade.joinPlayer(this.authData.authToken(), this.gameData.gameID(), this.authData.username(), this.convertTeamColor()); }
        System.out.println(EscapeSequences.SET_TEXT_BOLD + "Welcome to the game " + gameData.gameName() + "!" + EscapeSequences.RESET_TEXT_BOLD_FAINT + " (Type 'help' for a list of commands or 'quit' to exit the program.)");
        while (isRunning) {
            var color = isObserver ? "Observer" : this.color;
            System.out.print(EscapeSequences.SET_TEXT_BOLD + color + " >>>> " + EscapeSequences.RESET_TEXT_BOLD_FAINT);
            String input = scanner.nextLine();
            String output = inputParser(input);
            System.out.println(output);
        }
   
    }

    public String inputParser(String input){
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "quit" -> quit();
            case "help" -> help();
            case "redraw", "redrawing chess board \n" -> displayGame(this.color);
            case "leave" -> leaveGame();
            case "move" -> params.length != 2 ? "Invalid move command. Usage: move <from> <to>." : makeMove(input);
            case "resign" -> resign();
            case "highlight", "highlight legal moves" -> params.length != 1 ? "Invalid highlight command. Usage: highlight legal moves <position>." : highlightLegalMoves(params[0]);
            default -> help();
        };
    }

    private String help(){
        if (isObserver) {
            return "Available commands:\n" +
                    "help - Display this help message.\n" +
                    "quit - Exit the program.\n" +
                    "Redraw Chess Board or Redraw - Redraw the chess board.\n" +
                    "Leave - Leave the game.\n";
        } else {
        return "Available commands:\n" +
                "help - Display this help message.\n" +
                "quit - Exit the program.\n" +
                "redraw - Redraw the chess board.\n" +
                "leave - Leave the game.\n" +
                "move - Make a move. Usage: move <from> <to>.\n" +
                "resign - Resign the game.\n" +
                "highlight - Highlight all legal moves for a piece. Usage: highlight legal moves <position>.\n";
        }
    }

    private String leaveGame()
    {
        webSocketFacade.leaveGame(this.authData.authToken(), this.gameData.gameID());
        isRunning = false;
        return "You have left the game.";
    }

    private String makeMove(String move){
        //check to make sure that it is the correct players turn
        if (this.gameData.game().getTeamTurn() != convertTeamColor()) {
            return "It is not your turn.";
        }

        ChessPiece.PieceType promotion = null;
        var moveParts = move.split(" ");
        var from = moveParts[1];
        var to = moveParts[2];
        if (!from.matches("[a-h][1-8]") || !to.matches("[a-h][1-8]")) {
            return "Invalid move. Please try again. Please enter a move in the format 'move <from> <to>' where <from> and <to> are positions on the board in the format 'a1' to 'h8'.";
        }

        var fromMove = convertToLegalPosition(from);
        var toMove = convertToLegalPosition(to);
        var validMoves = gameData.game().validMoves(fromMove);
        if (validMoves == null || !validMoves.contains(new ChessMove(fromMove, toMove, null))) {
            
            return "Invalid move. Please try again. \nPlease enter a move in the format 'move <from> <to>' where <from> and <to> are positions on the board in the format 'a1' to 'h8'.";
        }
        //See if the move has the ability to promote
        if (gameData.game().getBoard().getPiece(fromMove).getPieceType() == ChessPiece.PieceType.PAWN && (toMove.getRow() == 0 || toMove.getRow() == 7)) {
            //Ask the user what they want to promote to
            System.out.println("What would you like to promote to? (queen, rook, bishop, knight)");
            try (Scanner scanner = new Scanner(System.in)) {
                var promotionInput = scanner.nextLine();
                switch (promotionInput) {
                    case "queen" -> promotion = ChessPiece.PieceType.QUEEN;
                    case "rook" -> promotion = ChessPiece.PieceType.ROOK;
                    case "bishop" -> promotion = ChessPiece.PieceType.BISHOP;
                    case "knight" -> promotion = ChessPiece.PieceType.KNIGHT;
                    default -> {
                        return "Invalid promotion. Please try again.";
                    }
                }
            }
        }
        var chessMove = new chess.ChessMove(fromMove, toMove, promotion);
        webSocketFacade.makeMove(this.authData.authToken(), this.gameData.gameID(), chessMove); 
        return "You have made the move " + from + " " + to + ".";
    }

    private String resign(){
        if(isObserver) return "Observers cannot resign. Please leave the game instead.";
        //Check to make sure that the other player has not already resigned and the game is still ongoing
        if (this.gameData.game().getTeamTurn() == ChessGame.TeamColor.FINISHED) {
            return "The game has already ended.";
        }
        //see of the other player has resigned
        if ((Objects.equals(this.color, "white") && this.gameData.blackUsername() == null ) || (Objects.equals(this.color, "black") && this.gameData.whiteUsername() == null)){
            return "The other player has already resigned or left the game.";
        }
        System.out.println("Are you sure you want to resign? (yes/no)");
            var input = scanner.nextLine();
            if (!input.equalsIgnoreCase("yes") && !input.equalsIgnoreCase("y")) {
                return "Resignation cancelled.";
            } else {
                resignGame();
            }
        return "You have resigned the game.";
    }

    private void resignGame(){
        isRunning = false;
        webSocketFacade.resignGame(this.authData.authToken(), this.gameData.gameID());
    }

    private String quit(){
        isRunning = false;
        //if the user is in a game leave the game
        if (this.gameData != null) {
            webSocketFacade.leaveGame(this.authData.authToken(), this.gameData.gameID());
        }
        //shutdown the websocket
        webSocketFacade.onClose();
        //Also needs to send a leave game message and quit the program
        return "Goodbye!";
    }

    private ChessPosition convertToLegalPosition(String input){
        if (!input.matches("[a-h][1-8]")) {
            return null;
        }
        var fromRow = 9 - (input.charAt(1) - '0');
        var fromCol = (input.charAt(0) - 'a') + 1;
        return new ChessPosition(fromRow, fromCol);
    }

    private String highlightLegalMoves(String position){
        if (this.gameData.game().getTeamTurn() != convertTeamColor()) {
            return "It is not your turn.";
        }
        var pos = convertToLegalPosition(position);
        if (pos == null) {
            return "Invalid position. Please try again.";
        }
        var validMoves = gameData.game().validMoves(pos);
        if (validMoves == null) {
            return "Invalid position. Please try again.";
        }
        var output = "Legal moves for " + position + ": ";
        this.highlightPosition = pos;
        this.highlightMoves = validMoves.stream().map(move -> move.getEndPosition()).collect(Collectors.toList());
        output = displayBoard();
        this.highlightPosition = null;
        this.highlightMoves = null;
        return output;
    }


    
    public String displayGame(String color) {
        if (this.gameData == null) {
            return "No game data available.";
        }
        var gameInfo = this.gameData.game();
        var turn = gameInfo.getTeamTurn();
        var output = "Game " + this.gameData.gameName() + ":\n";

        if (color.equalsIgnoreCase("white")) {
            output += displayBoard();
            output += "\n--------------------------------\n";
            output += displayBoardInverted();
        } else {
            output += displayBoardInverted();
            output += "\n--------------------------------\n";
            output += displayBoard();
        }

        return output + "\n" + "It is " + turn + "'s turn.\n";
    }

    private String displayBoard(){
        var gameInfo = this.gameData.game();
        var board = gameInfo.getBoard();
        var output = "";

        output += EscapeSequences.SET_TEXT_BOLD + displayAlphabet(false) + EscapeSequences.RESET_TEXT_BOLD_FAINT + "\n";
        for (int i = 0; i < 8; i++) {
            output += EscapeSequences.SET_TEXT_BOLD + (8 - i) + EscapeSequences.RESET_TEXT_BOLD_FAINT + " ";
            for (int j = 0; j < 8; j++) {
                var piece = board.getPiece(new ChessPosition(i + 1, j + 1));
                if (highlightPosition != null && highlightPosition.getRow() == i + 1 && highlightPosition.getColumn() == j + 1) {
                    output += EscapeSequences.SET_BG_COLOR_YELLOW + returnPieceChar(piece) + EscapeSequences.SET_BG_COLOR_DARK_GREY;
                } else if (highlightMoves != null && highlightMoves.contains(new ChessPosition(i + 1, j + 1))) {
                    output += EscapeSequences.SET_BG_COLOR_LIGHT_GREEN + returnPieceChar(piece) + EscapeSequences.SET_BG_COLOR_DARK_GREY;
                } else {
                    output += (i + j) % 2 == 0 ? EscapeSequences.SET_BG_COLOR_LIGHT_BLUE + returnPieceChar(piece) + EscapeSequences.SET_BG_COLOR_DARK_GREY : EscapeSequences.SET_BG_COLOR_BLUE + EscapeSequences.SET_TEXT_COLOR_WHITE + returnPieceChar(piece) + EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.SET_BG_COLOR_DARK_GREY;
                }
            }
            output += EscapeSequences.SET_BG_COLOR_DARK_GREY + EscapeSequences.SET_TEXT_BOLD + (8 - i) + EscapeSequences.RESET_TEXT_BOLD_FAINT + "\n";
        }
        output += EscapeSequences.SET_TEXT_BOLD + displayAlphabet(true) + EscapeSequences.RESET_TEXT_BOLD_FAINT + EscapeSequences.SET_BG_COLOR_DARK_GREY;
        return output;
    }

    private String displayBoardInverted(){
        var gameInfo = this.gameData.game();
        var board = gameInfo.getBoard();
        var output = "";

        output += EscapeSequences.SET_TEXT_BOLD + displayAlphabet(false) + EscapeSequences.RESET_TEXT_BOLD_FAINT + "\n";
        for (int i = 7; i >= 0; i--) {
            output += EscapeSequences.SET_TEXT_BOLD + (8 - i) + EscapeSequences.RESET_TEXT_BOLD_FAINT + " ";
            for (int j = 7; j >= 0; j--) {
                var piece = board.getPiece(new ChessPosition(i + 1, j + 1));
                output += (i + j) % 2 == 0 ? EscapeSequences.SET_BG_COLOR_LIGHT_GREEN + returnPieceChar(piece) + EscapeSequences.SET_BG_COLOR_DARK_GREY : EscapeSequences.SET_BG_COLOR_GREEN + EscapeSequences.SET_TEXT_COLOR_WHITE + returnPieceChar(piece) + EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.SET_BG_COLOR_DARK_GREY;
            }
            output += EscapeSequences.SET_BG_COLOR_DARK_GREY + EscapeSequences.SET_TEXT_BOLD + (8 - i) + EscapeSequences.RESET_TEXT_BOLD_FAINT + "\n";
        }
        output += EscapeSequences.SET_TEXT_BOLD + displayAlphabet(true) + EscapeSequences.RESET_TEXT_BOLD_FAINT + EscapeSequences.SET_BG_COLOR_DARK_GREY;
        return output;
    }

    private String returnPieceChar(ChessPiece piece){
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }
        if (piece.getTeamColor().toString() == "WHITE") {
            switch (piece.getPieceType()) {
                case PAWN:
                    return EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_PAWN + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case ROOK:
                    return EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_ROOK + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case KNIGHT:
                    return EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_KNIGHT + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case BISHOP:
                    return EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_BISHOP + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case QUEEN:
                    return EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_QUEEN + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case KING:
                    return EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_KING + EscapeSequences.SET_TEXT_COLOR_WHITE;        
                default:
                    return EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.EMPTY + EscapeSequences.SET_TEXT_COLOR_WHITE;
            }
        } else {
            switch (piece.getPieceType()) {
                case PAWN:
                    return EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_PAWN  + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case ROOK:
                    return EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_ROOK + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case KNIGHT:
                    return EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_KNIGHT + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case BISHOP:
                    return EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_BISHOP + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case QUEEN:
                    return EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_QUEEN + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case KING:
                    return EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_KING + EscapeSequences.SET_TEXT_COLOR_WHITE;       
                default:
                    return EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.EMPTY + EscapeSequences.SET_TEXT_COLOR_WHITE;
            }
        }
    }

    private String displayAlphabet(Boolean inverted){
        if (inverted) {
            return "  \u2003h\u2003 g\u2003 f\u2003 e\u2003 d\u2003 c\u2003 b\u2003 a";
        }
        return "  \u2003a\u2003 b\u2003 c\u2003 d\u2003 e\u2003 f\u2003 g\u2003 h";
    }

    private ChessGame.TeamColor convertTeamColor(){
        if (this.color == null) {
            return null;
        }
        
        if (this.color.equalsIgnoreCase("white")) {
            return ChessGame.TeamColor.WHITE;
        } else {
            return ChessGame.TeamColor.BLACK;
        }
    }

    @Override
    public void updateGame(ChessGame game, String whiteUsername, String blackUsername) {
        var playerColor = Boolean.TRUE.equals(isObserver) ? "Observer" : this.color;
        this.gameData = new GameData(this.gameData.gameID(), whiteUsername, blackUsername, this.gameData.gameName(), game);
        System.out.println("\nGameUpdate\n" + displayGame(this.color));
        System.out.print(EscapeSequences.SET_TEXT_BOLD + playerColor + " >>>> " + EscapeSequences.RESET_TEXT_BOLD_FAINT);
    }

    @Override
    public void printMessage(String message) {
        var playerColor = isObserver ? "Observer" : this.color;
        System.out.println("\nINCOMING MESSAGE >>>> " + message);
        System.out.print(EscapeSequences.SET_TEXT_BOLD + playerColor + " >>>> " + EscapeSequences.RESET_TEXT_BOLD_FAINT);
    }   
}