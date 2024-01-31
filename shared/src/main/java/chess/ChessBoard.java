package chess;

import java.util.Arrays;

import chess.ChessGame.TeamColor;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] board;

    public ChessBoard() {
        this.board = new ChessPiece[8][8];        
    }

    public ChessBoard(ChessBoard board) {
        this.board = new ChessPiece[8][8];
        //Copy the board
        for (int i = 0; i < 8; i++) {
            this.board[i] = Arrays.copyOf(board.board[i], 8);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        for (int i = 0; i < 8; i++) {
            //display the board with | | between each piece and if the piece is null display a space
            builder.append(Arrays.toString(this.board[i]).replace("null", " ").replace(",", " |").replace("[", "| ").replace("]", " |\n"));
        }
        return builder.toString();
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        //Get the row and column of the position of the piece that I am adding to the board
        int row = position.getRow(); 
        int col = position.getColumn();

        //Add the piece to the board
        this.board[8 - row][col - 1] = piece;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessBoard that=(ChessBoard) o;
        //since the board is a 2d array I need to use the Arrays.equals method twice
        for (int i = 0; i < 8; i++) {
            if (!Arrays.equals(this.board[i], that.board[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(board);
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     * @throws InvalidMoveException 
     */
    public ChessPiece getPiece(ChessPosition position) {
        //Get the row and column of the position of the piece that I am getting from the board
        int row = position.getRow(); 
        int col = position.getColumn();

        //Check if the position is valid
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.INVALID);
        }

        //Get the piece at the position
        ChessPiece chessPiece = this.board[8 - row][col - 1];

        //Check if there is a piece at the position if not return null
        if (chessPiece == null) {
            return null;
        }

        //Return the piece
        return chessPiece;
    }

    public void removePiece(ChessPosition position) {
        //Get the row and column of the position of the piece that I am removing from the board
        int row = position.getRow(); 
        int col = position.getColumn();

        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return ;
        }

        //Remove the piece from the board
        this.board[8 - row][col - 1] = null;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        //Clear all the pieces from the board
        for (int i = 0; i < 8; i++) {
            Arrays.fill(this.board[i], null);
        }

        //Add all the white pieces to the board
        this.board[7][0] = new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        this.board[7][1] = new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        this.board[7][2] = new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        this.board[7][3] = new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        this.board[7][4] = new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.KING);
        this.board[7][5] = new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        this.board[7][6] = new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        this.board[7][7] = new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        for (int i = 0; i < 8; i++) {
            this.board[6][i] = new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        }

        //Add all the black pieces to the board
        this.board[0][0] = new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        this.board[0][1] = new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        this.board[0][2] = new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        this.board[0][3] = new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        this.board[0][4] = new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.KING);
        this.board[0][5] = new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        this.board[0][6] = new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        this.board[0][7] = new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        for (int i = 0; i < 8; i++) {
            this.board[1][i] = new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        }
    }


}
