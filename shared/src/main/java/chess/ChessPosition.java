package chess;

import java.util.Objects;

import org.junit.jupiter.api.ClassOrderer;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    @Override
    public boolean equals(Object instance) {
        if (this == instance) {
            return true;
        } else if (instance == null || getClass() != instance.getClass()){
            return false;
        }
        ChessPosition that = (ChessPosition) instance;
        return boardRow == that.boardRow && boardCol == that.boardCol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(boardRow, boardCol);
    }

    private final int boardRow;
    private final int boardCol;

    public ChessPosition(int row, int col) {
        this.boardRow = row;
        this.boardCol = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return boardRow;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return boardCol;
    }
}
