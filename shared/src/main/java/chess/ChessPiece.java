package chess;

import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
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
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
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

        //Get the piece at the position
        ChessPiece chessPiece = board.getPiece(myPosition);

        //Get the piece type of the piece that I am adding to the board
        ChessPiece.PieceType pieceType = chessPiece.getPieceType();

        //Get the team color of the piece that I am adding to the board
        ChessGame.TeamColor teamColor = chessPiece.getTeamColor();

        //Create a collection of valid moves
        Collection<ChessMove> validMoves = new Collection<ChessMove>();

        //Switch on the piece type
        switch (ChessPiece.PieceType){
            //Check if the piece is a king
            case KING:
                for (int i = -1; i < 2; i++){
                    for (int j = -1; j < 2; j++){
                        try { 
                            ChessPiece move = board.getPiece(new ChessPosition(row + i, col + j));
                            if (move == null || move.getTeamColor() != teamColor)
                            {
                                //Create a new chess move
                                ChessMove chessMove = new ChessMove(myPosition, new ChessPosition(row + i, col + j), null);
                                //Add the move to the collection of valid moves
                                validMoves.add(chessMove);
                            }
                        }
                        catch (InvalidMoveException e) { }
                    }
                }
                
                break;
            //Check if the piece is a queen
            case QUEEN:
            //Check the diagonal up and to the right
                // for (int i = -8; i < 8; i++){
                //     for (int j = -8; j < 8; j++){
                //         try { 
                //             ChessPiece move = board.getPiece(new ChessPosition(row + i, col + j));
                //             if (move == null || move.getTeamColor() != teamColor)
                //             {
                //                 //Create a new chess move
                //                 ChessMove chessMove = new ChessMove(myPosition, new ChessPosition(row + i, col + j), null);
                //                 //Add the move to the collection of valid moves
                //                 validMoves.add(chessMove);
                //             }
                //         }
                //         catch (InvalidMoveException e) { }
                //     }
                // }
                break;
            //Check if the piece is a bishop
            case BISHOP:
                addValidDiagonalOrVerticalMoves(1, 1, row, col);  // up-right
                addValidDiagonalOrVerticalMoves(1, -1, row, col); // up-left
                addValidDiagonalOrVerticalMoves(-1, 1, row, col); // down-right
                addValidDiagonalOrVerticalMoves(-1, -1, row, col); // down-left
                break;
            //Check if the piece is a knight
            case KNIGHT:
                break;
            //Check if the piece is a rook
            case ROOK:
                addValidDiagonalOrVerticalMoves(1, 0, row, col);  // up
                addValidDiagonalOrVerticalMoves(-1, 0, row, col); // down
                addValidDiagonalOrVerticalMoves(0, 1, row, col); // right
                addValidDiagonalOrVerticalMoves(0, -1, row, col); // left
                break;
            //Check if the piece is a pawn
            case PAWN:
                break;
        }
    }

    private void addValidDiagonalOrVerticalMoves(int rowIncrement, int colIncrement, row, col) {
        for (int i = 1; i < 8; i++){
            try { 
                ChessPiece move = board.getPiece(new ChessPosition(row + rowIncrement * i, col + colIncrement * i));
                if (move == null || move.getTeamColor() != teamColor)
                {
                    //Create a new chess move
                    ChessMove chessMove = new ChessMove(myPosition, new ChessPosition(row + rowIncrement * i, col + colIncrement * i), null);
                    //Add the move to the collection of valid moves
                    validMoves.add(chessMove);
                }
            }
            catch (InvalidMoveException e) { }
        }
    }
}

