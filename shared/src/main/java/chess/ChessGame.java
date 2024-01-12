package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor turn;
    

    public ChessGame() {
        //Implement the chess board
        ChessBoard board = new ChessBoard();
        turn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        //Reset the board just in case
        this.board.resetBoard();
        //Adding all the black pieces to the board
        this.board.addPiece(new ChessPosition(1, 1), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        this.board.addPiece(new ChessPosition(1, 2), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        this.board.addPiece(new ChessPosition(1, 3), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        this.board.addPiece(new ChessPosition(1, 4), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.KING));
        this.board.addPiece(new ChessPosition(1, 5), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
        this.board.addPiece(new ChessPosition(1, 6), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        this.board.addPiece(new ChessPosition(1, 7), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        this.board.addPiece(new ChessPosition(1, 8), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        for (int i = 1; i <= 8; i++) {
            this.board.addPiece(new ChessPosition(2, i), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.PAWN));
        }

        //Adding all the white pieces to the board
        this.board.addPiece(new ChessPosition(8, 1), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        this.board.addPiece(new ChessPosition(8, 2), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        this.board.addPiece(new ChessPosition(8, 3), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        this.board.addPiece(new ChessPosition(8, 4), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        this.board.addPiece(new ChessPosition(8, 5), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.KING));
        this.board.addPiece(new ChessPosition(8, 6), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        this.board.addPiece(new ChessPosition(8, 7), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        this.board.addPiece(new ChessPosition(8, 8), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        for (int i = 1; i <= 8; i++) {
            this.board.addPiece(new ChessPosition(7, i), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.PAWN));
        }
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
