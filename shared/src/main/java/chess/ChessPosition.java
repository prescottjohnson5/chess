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
        return board_row == that.board_row && board_col == that.board_col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board_row, board_col);
    }

    int board_row;
    int board_col;

    public ChessPosition(int row, int col) {
        board_row = row;
        board_col = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return board_row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return board_col;
    }
}
