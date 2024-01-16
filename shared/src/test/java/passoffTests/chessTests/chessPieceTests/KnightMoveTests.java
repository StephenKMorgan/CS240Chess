package passoffTests.chessTests.chessPieceTests;

import org.junit.jupiter.api.Test;

import chess.InvalidMoveException;

import static passoffTests.TestFactory.*;

public class KnightMoveTests {

    @Test
    public void knightMiddleOfBoardWhite() throws InvalidMoveException{
        validateMoves("""
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | |N| | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        """,
                startPosition(5, 5),
                endPositions(new int[][]{
                        {7, 6}, {6, 7}, {4, 7}, {3, 6}, {3, 4}, {4, 3}, {6, 3}, {7, 4},
                })
        );
    }

    @Test
    public void knightMiddleOfBoardBlack() throws InvalidMoveException{
        validateMoves("""
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | |n| | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        """,
                startPosition(5, 5),
                endPositions(new int[][]{
                        {7, 6}, {6, 7}, {4, 7}, {3, 6}, {3, 4}, {4, 3}, {6, 3}, {7, 4},
                })
        );
    }


    @Test
    public void knightEdgeOfBoardLeft() throws InvalidMoveException{
        validateMoves("""
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        |n| | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        """,
                startPosition(4, 1),
                endPositions(new int[][]{{6, 2}, {5, 3}, {3, 3}, {2, 2}})
        );
    }

    @Test
    public void knightEdgeOfBoardRight() throws InvalidMoveException{
        validateMoves("""
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | |n|
                        | | | | | | | | |
                        | | | | | | | | |
                        """,
                startPosition(3, 8),
                endPositions(new int[][]{{1, 7}, {2, 6}, {4, 6}, {5, 7}})
        );
    }

    @Test
    public void knightEdgeOfBoardBottom() throws InvalidMoveException{
        validateMoves("""
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | |N| | |
                        """,
                startPosition(1, 6),
                endPositions(new int[][]{{2, 4}, {3, 5}, {3, 7}, {2, 8}})
        );
    }

    @Test
    public void knightEdgeOfBoardTop() throws InvalidMoveException{
        validateMoves("""
                        | | |N| | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        """,
                startPosition(8, 3),
                endPositions(new int[][]{{7, 5}, {6, 4}, {6, 2}, {7, 1}})
        );
    }


    @Test
    public void knightCornerOfBoardBottomRight() throws InvalidMoveException{
        validateMoves("""
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | |N|
                        """,
                startPosition(1, 8),
                endPositions(new int[][]{{2, 6}, {3, 7}})
        );
    }

    @Test
    public void knightCornerOfBoardTopRight() throws InvalidMoveException{
        validateMoves("""
                        | | | | | | | |N|
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        """,
                startPosition(8, 8),
                endPositions(new int[][]{{6, 7}, {7, 6}})
        );
    }

    @Test
    public void knightCornerOfBoardTopLeft() throws InvalidMoveException{
        validateMoves("""
                        |n| | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        """,
                startPosition(8, 1),
                endPositions(new int[][]{{7, 3}, {6, 2}})
        );
    }

    @Test
    public void knightCornerOfBoardBottomLeft() throws InvalidMoveException{
        validateMoves("""
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        |n| | | | | | | |
                        """,
                startPosition(1, 1),
                endPositions(new int[][]{{2, 3}, {3, 2}})
        );
    }


    @Test
    public void knightBlocked() throws InvalidMoveException{
        validateMoves("""
                        | | | | | | | | |
                        | | | |R| | | | |
                        | | | | | | |P| |
                        | | | | |N| | | |
                        | | |N| | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        """,
                startPosition(5, 5),
                endPositions(new int[][]{{3, 4}, {3, 6}, {4, 7}, {7, 6}, {6, 3}})
        );
    }


    @Test
    public void knightCaptureEnemy() throws InvalidMoveException{
        validateMoves("""
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | | | | | |
                        | | | | |n| | | |
                        | | |N| | | | | |
                        | | | |P| |R| | |
                        | | | | | | | | |
                        | | | | | | | | |
                        """,
                startPosition(5, 5),
                endPositions(new int[][]{{7, 6}, {6, 7}, {4, 7}, {3, 6}, {3, 4}, {4, 3}, {6, 3}, {7, 4}})
        );
    }
}
