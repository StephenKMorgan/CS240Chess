package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    public ChessBoard() {
        //My 2d array of chess pieces that make up my board
        ChessPiece[][] board = new ChessPiece[8][8];
        
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

        //Get the piece type of the piece that I am adding to the board
        ChessPiece.PieceType pieceType = piece.getPieceType();

        //Add the piece to the board
        this.board[row][col] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        //Get the row and column of the position of the piece that I am getting from the board
        int row = position.getRow(); 
        int col = position.getColumn();

        //Check if the position is valid
        if (row < 0 || row > 7 || col < 0 || col > 7) {
            throw new InvalidMoveException("Invalid position");
        }

        //Get the piece at the position
        ChessPiece chessPiece = this.board[row][col];

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
        foreach (ChessPiece piece in board) {
            piece = null;
        }
    }
}
