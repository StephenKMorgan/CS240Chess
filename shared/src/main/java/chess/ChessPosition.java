package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private int row;
    private int col;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPosition that=(ChessPosition) o;
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    public ChessPosition() {
        this.row = 0;
        this.col = 0;
    }

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return this.row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return this.col;
    }

    //toString converts the row and column to a string for example row 7 and col 1 would be "a7"
    @Override
    public String toString() {
        return String.valueOf((char) ('a' + col - 1)) + row;
    }
}
