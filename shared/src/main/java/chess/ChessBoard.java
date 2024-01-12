package chess;

import java.util.Arrays;

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
        return Arrays.equals(board, that.board);
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
    public ChessPiece getPiece(ChessPosition position) throws InvalidMoveException {
        //Get the row and column of the position of the piece that I am getting from the board
        int row = position.getRow(); 
        int col = position.getColumn();

        //Check if the position is valid
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            throw new InvalidMoveException("Invalid position");
        }
        //For some stupid reason the board starts at the bottom left corner with 1,1
        //Get the piece at the position
        ChessPiece chessPiece = this.board[8 - row][col - 1];

        //Check if there is a piece at the position if not return null
        if (chessPiece == null) {
            return null;
        }

        //Return the piece
        return chessPiece;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; i++){
                this.board[i][j] = null;
            }
        }
    }
}
