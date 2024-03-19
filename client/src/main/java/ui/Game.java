package ui;

import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;


/**
 * ChessGameREPL
 */
public class Game {
    private GameData gameData;

    public Game(GameData gameData) {
        this.gameData = gameData;   
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

        return output + "\n" + "It is " + turn + "'s turn.";
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
                output += (i + j) % 2 == 0 ? EscapeSequences.SET_BG_COLOR_LIGHT_BLUE + returnPieceChar(piece) + EscapeSequences.SET_BG_COLOR_DARK_GREY : EscapeSequences.SET_BG_COLOR_BLUE + EscapeSequences.SET_TEXT_COLOR_WHITE + returnPieceChar(piece) + EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.SET_BG_COLOR_DARK_GREY;
            }
            output += EscapeSequences.SET_BG_COLOR_DARK_GREY + EscapeSequences.SET_TEXT_BOLD + (8 - i) + EscapeSequences.RESET_TEXT_BOLD_FAINT + "\n";
        }
        output += EscapeSequences.SET_TEXT_BOLD + displayAlphabet(false) + EscapeSequences.RESET_TEXT_BOLD_FAINT + EscapeSequences.SET_BG_COLOR_DARK_GREY;
        return output;
    }

    private String displayBoardInverted(){
        var gameInfo = this.gameData.game();
        var board = gameInfo.getBoard();
        var output = "";

        output += EscapeSequences.SET_TEXT_BOLD + displayAlphabet(true) + EscapeSequences.RESET_TEXT_BOLD_FAINT + "\n";
        for (int i = 7; i >= 0; i--) {
            output += EscapeSequences.SET_TEXT_BOLD + (8 - i) + EscapeSequences.RESET_TEXT_BOLD_FAINT + " ";
            for (int j = 7; j >= 0; j--) {
                var piece = board.getPiece(new ChessPosition(i + 1, j + 1));
                output += (i + j) % 2 == 0 ? EscapeSequences.SET_BG_COLOR_LIGHT_GREEN + returnPieceChar(piece) + EscapeSequences.SET_BG_COLOR_DARK_GREY : EscapeSequences.SET_BG_COLOR_GREEN + EscapeSequences.SET_TEXT_COLOR_WHITE + returnPieceChar(piece) + EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.SET_BG_COLOR_DARK_GREY;
            }
            output += EscapeSequences.SET_BG_COLOR_DARK_GREY + EscapeSequences.SET_TEXT_BOLD + (i + 1) + EscapeSequences.RESET_TEXT_BOLD_FAINT + "\n";
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
}