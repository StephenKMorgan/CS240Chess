package chess;

import java.util.Collection;
import java.util.Objects;

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
        this.board = new ChessBoard();
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
        //Get the piece at the start position
        ChessPiece piece = this.board.getPiece(startPosition);
        //If there is no piece at the start position return null
        if (piece == null) {
            return null;
        }
        //If there is a piece at the start position return the valid moves for that piece
        else {
            //For each valid move check if it puts the king in check if so remove it from the collection
            Collection<ChessMove> validMoves = piece.pieceMoves(this.board, startPosition);
            //If there are no valid moves return null
            if (validMoves.size() == 0) {
                return null;
            }
            //If there are valid moves return them
            else {
                //check if the king is in check given each move and remove the move if it puts the king in check
                for (ChessMove move : validMoves) {
                    //Make the move
                    this.board.addPiece(move.getEndPosition(), piece);
                    this.board.removePiece(move.getStartPosition());
                    //Check if the king is in check
                    if (this.isInCheck(piece.getTeamColor())) {
                        validMoves.remove(move);
                    }
                    //Undo the move
                    this.board.addPiece(move.getStartPosition(), piece);
                    this.board.removePiece(move.getEndPosition());
                }
                return validMoves;
            }
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        //Get the piece at the start position
        ChessPiece piece = this.board.getPiece(move.getStartPosition());
        //If there is no piece at the start position throw an exception
        if (piece == null) {
            throw new InvalidMoveException("There is no piece at the start position");
        }
        //If there is a piece at the start position check if the move is valid
        else {
            //Get the valid moves for the piece
            Collection<ChessMove> validMoves = piece.pieceMoves(this.board, move.getStartPosition());
            //If the move is not valid throw an exception
            if (!validMoves.contains(move)) {
                throw new InvalidMoveException("The move is not valid");
            }
            //Check if it is the correct teams turn
            else if (piece.getTeamColor() != this.turn) {
                throw new InvalidMoveException("It is not this teams turn");
            }
            //If the move is valid make the move
            else {
                //If the move is a pawn promotion promote the pawn
                if (move.getPromotionPiece() != null) {
                    this.board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
                    this.board.removePiece(move.getStartPosition());
                }
                else{
                    //Make the move
                    this.board.addPiece(move.getEndPosition(), piece);
                    this.board.removePiece(move.getStartPosition());
                }                
                //Check if the king is in check
                if (this.isInCheck(piece.getTeamColor())) {
                    //Undo the move
                    this.board.addPiece(move.getStartPosition(), piece);
                    this.board.removePiece(move.getEndPosition());
                    throw new InvalidMoveException("The move is not valid");
                }
                //If the king is not in check change the turn
                else {
                    if (this.turn == TeamColor.WHITE) {
                        this.turn = TeamColor.BLACK;
                    }
                    else {
                        this.turn = TeamColor.WHITE;
                    }
                }
            }
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        for(int i = 1; i <= 8; i++){
            for(int j = 1; j <= 8; j++){
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = this.board.getPiece(position);
                if(piece != null && piece.getTeamColor() != teamColor){
                    piece.pieceMoves(this.board, position);
                    Collection<ChessMove> checkedMoves = piece.getCheckMoves();
                    if (checkedMoves != null && checkedMoves.size() > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
                 
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        //First check if the team is in check
        if(this.isInCheck(teamColor)){
            //If the team is in check check if there are any valid moves
            for(int i = 1; i <= 8; i++){
                for(int j = 1; j <= 8; j++){
                    ChessPosition position = new ChessPosition(i, j);
                    ChessPiece piece = this.board.getPiece(position);
                    if(piece != null && piece.getTeamColor() == teamColor){
                        //Get the valid moves for the piece
                        Collection<ChessMove> validMoves = piece.pieceMoves(this.board, position);
                        //Check each move to see if it get the king out of check
                        for(ChessMove move : validMoves){
                            //Make the move
                            this.board.addPiece(move.getEndPosition(), piece);
                            this.board.removePiece(move.getStartPosition());
                            //Check if the king is in check
                            if (!this.isInCheck(teamColor)) {
                                //Undo the move
                                this.board.addPiece(move.getStartPosition(), piece);
                                this.board.removePiece(move.getEndPosition());
                                return false;
                            }
                            //Undo the move
                            this.board.addPiece(move.getStartPosition(), piece);
                            this.board.removePiece(move.getEndPosition());
                        }
                    }
                }
            }
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        //check if this team is in stalemate by checking if the team is in check and has no valid moves
        if(!this.isInCheck(teamColor)){
            for(int i = 1; i <= 8; i++){
                for(int j = 1; j <= 8; j++){
                    ChessPosition position = new ChessPosition(i, j);
                    ChessPiece piece = this.board.getPiece(position);
                    if(piece != null && piece.getTeamColor() == teamColor){
                        //Get the valid moves for the piece
                        Collection<ChessMove> validMoves = piece.pieceMoves(this.board, position);
                        //Check each move to see if it get the king out of check
                        for(ChessMove move : validMoves){
                            //Make the move
                            this.board.addPiece(move.getEndPosition(), piece);
                            this.board.removePiece(move.getStartPosition());
                            //Check if the king is in check
                            if (!this.isInCheck(teamColor)) {
                                //Undo the move
                                this.board.addPiece(move.getStartPosition(), piece);
                                this.board.removePiece(move.getEndPosition());
                                return false;
                            }
                            //Undo the move
                            this.board.addPiece(move.getStartPosition(), piece);
                            this.board.removePiece(move.getEndPosition());
                        }
                    }
                }
            }
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        //Reset the board just in case
        if (this.board != null) {
            this.board.resetBoard();
        }
        //Set the board
        this.board = board;
        
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    @Override
public boolean equals(Object obj) {
    if (this == obj) {
        return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
        return false;
    }
    ChessBoard that = (ChessBoard) obj;
    // compare the fields of this and that
    // return true if they are all equal, false otherwise
    return this.board.equals(that);
}

    @Override
    public int hashCode() {
        return Objects.hash(board, turn);
    }
}
