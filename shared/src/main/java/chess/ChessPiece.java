package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessPiece that)) return false;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type, validMoves);
    }

    @Override
    public String toString() {
        //if the color is white then return the pieces first letter in uppercase else return the piece first letter type in lowercase except for knight which is n;
        return this.pieceColor == ChessGame.TeamColor.WHITE ? this.type.toString().substring(0, 1) : this.type == PieceType.KNIGHT ? "n" : this.type.toString().substring(0, 1).toLowerCase();
    }

    private ChessGame.TeamColor pieceColor;
    private PieceType type;
    private Collection<ChessMove> validMoves;
    private Collection<ChessMove> checkMoves;

    public ChessPiece() {
        this.pieceColor = ChessGame.TeamColor.WHITE;
        this.type = PieceType.PAWN;
        this.validMoves = new ArrayList<ChessMove>();
        this.checkMoves = new ArrayList<ChessMove>();
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
        this.validMoves = new ArrayList<ChessMove>();
        this.checkMoves = new ArrayList<ChessMove>();
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN,
        INVALID
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    public Collection<ChessMove> getCheckMoves() {
        return this.checkMoves;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        //Get the row and column of the position of the piece that I am getting from the board
        int row = myPosition.getRow(); 
        int col = myPosition.getColumn();

        this.validMoves.clear();
        this.checkMoves.clear();

        //Switch on the piece type
        switch (this.type){
            //Check if the piece is a king
            case KING:
                for (int i = -1; i < 2; i++){
                    for (int j = -1; j < 2; j++){
                        checkAround(board, myPosition, row, i, col, j);
                    }
                }    
                break;
            //Check if the piece is a queen
            case QUEEN:
                addValidDiagonalOrVerticalMoves(1, 0, row, col, board, validMoves, myPosition);  // up
                addValidDiagonalOrVerticalMoves(1, 1, row, col, board, validMoves, myPosition);  // up-right
                addValidDiagonalOrVerticalMoves(1, -1, row, col, board, validMoves, myPosition); // up-left
                addValidDiagonalOrVerticalMoves(0, 1, row, col, board, validMoves, myPosition); // right
                addValidDiagonalOrVerticalMoves(-1, 0, row, col, board, validMoves, myPosition); // down
                addValidDiagonalOrVerticalMoves(-1, 1, row, col, board, validMoves, myPosition); // down-right
                addValidDiagonalOrVerticalMoves(-1, -1, row, col, board, validMoves, myPosition); // down-left
                addValidDiagonalOrVerticalMoves(0, -1, row, col, board, validMoves, myPosition); // left
                break;
            //Check if the piece is a bishop
            case BISHOP:
                addValidDiagonalOrVerticalMoves(1, 1, row, col, board, validMoves, myPosition);  // up-right
                addValidDiagonalOrVerticalMoves(-1, 1, row, col, board, validMoves, myPosition); // down-right
                addValidDiagonalOrVerticalMoves(-1, -1, row, col, board, validMoves, myPosition); // down-left
                addValidDiagonalOrVerticalMoves(1, -1, row, col, board, validMoves, myPosition); // up-left
                break;
            //Check if the piece is a knight
            case KNIGHT:
                for (int i = -2; i < 3; i++){
                    for (int j = -2; j < 3; j++){
                        if (Math.abs(i) != Math.abs(j) && i != 0 && j != 0){
                            checkAround(board, myPosition, row, i, col, j);
                        }
                    }
                }
                break;
            //Check if the piece is a rook
            case ROOK:
                addValidDiagonalOrVerticalMoves(1, 0, row, col, board, validMoves, myPosition);  // up
                addValidDiagonalOrVerticalMoves(-1, 0, row, col, board, validMoves, myPosition); // down
                addValidDiagonalOrVerticalMoves(0, 1, row, col, board, validMoves, myPosition); // right
                addValidDiagonalOrVerticalMoves(0, -1, row, col, board, validMoves, myPosition); // left
                break;
            //Check if the piece is a pawn
            case PAWN:         
                //Check if the pawn is in the starting position
                pawnCheckForFirstMove(row, col, board, myPosition);
                //Check for capture
                pawnCheckForCapture(row, col, board, myPosition);
                //Check for promotion
                pawnCheckForPromotion(row, col, board, myPosition);
                //Check for normal move
                pawnCheckForValidMove(row, col, board, myPosition);
                break;
            default:
                break;
        }
        return this.validMoves;
    }

    private void checkAround(ChessBoard board, ChessPosition myPosition, int row, int i, int col, int j) {
        ChessPiece move = board.getPiece(new ChessPosition(row + i, col + j));
        if (move == null || move.getTeamColor() != this.pieceColor && move.type != PieceType.INVALID)
        {
            ChessMove chessMove = new ChessMove(myPosition, new ChessPosition(row + i, col + j), null);
            this.validMoves.add(chessMove);
            if (move != null && move.getTeamColor() != this.pieceColor && move.type == PieceType.KING){
                this.checkMoves.add(chessMove);
            }
        }
    }

    private void addValidDiagonalOrVerticalMoves(int rowIncrement, int colIncrement, int row, int col, ChessBoard board, Collection<ChessMove> validMoves, ChessPosition myPosition) {
        for (int i = 1; i < 8; i++){
                ChessPiece move = board.getPiece(new ChessPosition(row + rowIncrement * i, col + colIncrement * i ));
                if (move == null)
                {
                    ChessMove chessMove = new ChessMove(myPosition, new ChessPosition(row + rowIncrement * i, col + colIncrement * i), null);
                    this.validMoves.add(chessMove);
                }
                else if (move.type != PieceType.INVALID && move.getTeamColor() != this.pieceColor){
                    ChessMove chessMove = new ChessMove(myPosition, new ChessPosition(row + rowIncrement * i, col + colIncrement * i), null);
                    this.validMoves.add(chessMove);
                    if (move != null && move.getTeamColor() != this.pieceColor && move.type == PieceType.KING){
                        this.checkMoves.add(chessMove);
                    }
                    i = 8;
                }
                else{
                    i = 8;
                }
        }
    }

    private void pawnCheckForFirstMove(int row , int col, ChessBoard board, ChessPosition myPosition){
        if ((this.pieceColor ==  ChessGame.TeamColor.WHITE && row == 2) || (this.pieceColor ==  ChessGame.TeamColor.BLACK && row == 7)){
                for (int i = 1; i < 3; i++){
                    ChessPiece move = board.getPiece(new ChessPosition(row == 2 ? row + i : row - i, col));
                    if (move == null)
                    {
                        ChessMove chessMove = new ChessMove(myPosition, new ChessPosition(row == 2 ? row + i : row - i, col), null);
                        this.validMoves.add(chessMove);
                    }
                    else{
                        i = 3;
                    }
                } 
        }
    }

    private void pawnCheckForCapture(int row, int col, ChessBoard board, ChessPosition myPosition){
            for (int i = -1; i < 2; i += 2){
                ChessPiece move = board.getPiece(new ChessPosition(row + (this.pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1), col + i));
                if (move != null && move.getTeamColor() != this.pieceColor && move.type != PieceType.INVALID)
                {
                    if(pawnCheckForPromotion(row + (this.pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1), col + i, board, myPosition)){
                        if (move != null && move.getTeamColor() != this.pieceColor && move.type == PieceType.KING){
                            this.checkMoves.add(new ChessMove(myPosition, new ChessPosition(row + (this.pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1), col + i), null));
                        }
                        return;
                    }
                    else{
                    ChessMove chessMove = new ChessMove(myPosition, new ChessPosition(row + (this.pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1), col + i), null);
                    this.validMoves.add(chessMove);
                    if (move != null && move.getTeamColor() != this.pieceColor && move.type == PieceType.KING){
                        this.checkMoves.add(chessMove);
                    }
                    }
                }
            }
    }

    private boolean pawnCheckForPromotion(int row, int col, ChessBoard board, ChessPosition myPosition){
        if ((this.pieceColor ==  ChessGame.TeamColor.WHITE && row == 8) || (this.pieceColor ==  ChessGame.TeamColor.BLACK && row == 1)){
            //loop the user to choose what piece they want to promote to
            for (int i = 1; i < 5; i++){
                //Create a new chess move
                ChessMove chessMove = new ChessMove(myPosition, new ChessPosition(row, col), PieceType.values()[i]);
                //Add the move to the collection of valid moves
                this.validMoves.add(chessMove);
            }
            return true;
        }
        return false;
    }

    private void pawnCheckForValidMove(int row, int col, ChessBoard board, ChessPosition myPosition){
            ChessPiece move = board.getPiece(new ChessPosition(row + (this.pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1), col));
            if (move == null)
            {
                if(pawnCheckForPromotion(row + (this.pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1), col, board, myPosition)){
                    return;
                }
                else{
                    ChessMove chessMove = new ChessMove(myPosition, new ChessPosition(row + (this.pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1), col), null);
                    this.validMoves.add(chessMove);
                }
            }
    }
}

