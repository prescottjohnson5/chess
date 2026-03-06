package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;
/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    @Override
    public boolean equals(Object instance) {
        if (this == instance) {
            return true;
        } else if (instance == null || getClass() != instance.getClass()){
            return false;
        }
        ChessPiece that = (ChessPiece) instance;
        return color == that.color && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, pieceType);
    }

    ChessGame.TeamColor color;
    ChessPiece.PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        color = pieceColor;
        pieceType = type;
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
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to.
     * Does not take into account moves that are illegal due to leaving the king in
     * danger.
     *
     * @return collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        switch (pieceType) {
            case PAWN -> addPawnMoves(board, myPosition, moves);
            case BISHOP -> addBishopMoves(board, myPosition, moves);
            case ROOK -> addRookMoves(board, myPosition, moves);
            case QUEEN -> {
                addBishopMoves(board, myPosition, moves);
                addRookMoves(board, myPosition, moves);
            }
            case KNIGHT -> addKnightMoves(board, myPosition, moves);
            case KING -> addKingMoves(board, myPosition, moves);
            default -> {
            }
        }
        return moves;
    }

    private void addPawnMoves(ChessBoard board, ChessPosition from, Collection<ChessMove> moves) {
        if (color == ChessGame.TeamColor.WHITE) {
            addWhitePawnMoves(board, from, moves);
        } else {
            addBlackPawnMoves(board, from, moves);
        }
    }

    private void addWhitePawnMoves(ChessBoard board, ChessPosition from, Collection<ChessMove> moves) {
        int row = from.getRow();
        int col = from.getColumn();

        ChessPosition oneForward = new ChessPosition(row + 1, col);
        if (row == 2) {
            ChessPosition twoForward = new ChessPosition(row + 2, col);
            if (board.getPiece(oneForward) == null && board.getPiece(twoForward) == null) {
                moves.add(new ChessMove(from, twoForward, null));
            }
        }
        if (row + 1 <= 8 && board.getPiece(oneForward) == null) {
            if (oneForward.getRow() == 8) {
                addPromotionMoves(from, oneForward, moves);
            } else {
                moves.add(new ChessMove(from, oneForward, null));
            }
        }

        // captures
        addPawnCapture(board, from, row + 1, col + 1, ChessGame.TeamColor.BLACK, moves);
        addPawnCapture(board, from, row + 1, col - 1, ChessGame.TeamColor.BLACK, moves);
    }

    private void addBlackPawnMoves(ChessBoard board, ChessPosition from, Collection<ChessMove> moves) {
        int row = from.getRow();
        int col = from.getColumn();

        ChessPosition oneDown = new ChessPosition(row - 1, col);
        if (row == 7) {
            ChessPosition twoDown = new ChessPosition(row - 2, col);
            if (board.getPiece(oneDown) == null && board.getPiece(twoDown) == null) {
                moves.add(new ChessMove(from, twoDown, null));
            }
        }
        if (row - 1 >= 1 && board.getPiece(oneDown) == null) {
            if (oneDown.getRow() == 1) {
                addPromotionMoves(from, oneDown, moves);
            } else {
                moves.add(new ChessMove(from, oneDown, null));
            }
        }

        // captures
        addPawnCapture(board, from, row - 1, col + 1, ChessGame.TeamColor.WHITE, moves);
        addPawnCapture(board, from, row - 1, col - 1, ChessGame.TeamColor.WHITE, moves);
    }

    private void addPawnCapture(ChessBoard board,
                                ChessPosition from,
                                int targetRow,
                                int targetCol,
                                ChessGame.TeamColor enemyColor,
                                Collection<ChessMove> moves) {
        if (targetRow < 1 || targetRow > 8 || targetCol < 1 || targetCol > 8) {
            return;
        }
        ChessPosition target = new ChessPosition(targetRow, targetCol);
        ChessPiece targetPiece = board.getPiece(target);
        if (targetPiece != null && targetPiece.getTeamColor() == enemyColor) {
            if (targetRow == 1 || targetRow == 8) {
                addPromotionMoves(from, target, moves);
            } else {
                moves.add(new ChessMove(from, target, null));
            }
        }
    }

    private void addPromotionMoves(ChessPosition from, ChessPosition to, Collection<ChessMove> moves) {
        moves.add(new ChessMove(from, to, PieceType.QUEEN));
        moves.add(new ChessMove(from, to, PieceType.ROOK));
        moves.add(new ChessMove(from, to, PieceType.BISHOP));
        moves.add(new ChessMove(from, to, PieceType.KNIGHT));
    }

    private void addBishopMoves(ChessBoard board, ChessPosition from, Collection<ChessMove> moves) {
        addSlidingMoves(board, from, 1, -1, moves);  // up-left
        addSlidingMoves(board, from, 1, 1, moves);   // up-right
        addSlidingMoves(board, from, -1, -1, moves); // down-left
        addSlidingMoves(board, from, -1, 1, moves);  // down-right
    }

    private void addRookMoves(ChessBoard board, ChessPosition from, Collection<ChessMove> moves) {
        addSlidingMoves(board, from, 0, -1, moves);  // left
        addSlidingMoves(board, from, 0, 1, moves);   // right
        addSlidingMoves(board, from, -1, 0, moves);  // down
        addSlidingMoves(board, from, 1, 0, moves);   // up
    }

    private void addSlidingMoves(ChessBoard board,
                                 ChessPosition from,
                                 int dRow,
                                 int dCol,
                                 Collection<ChessMove> moves) {
        int row = from.getRow() + dRow;
        int col = from.getColumn() + dCol;
        while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
            ChessPosition to = new ChessPosition(row, col);
            ChessPiece target = board.getPiece(to);
            if (target == null) {
                moves.add(new ChessMove(from, to, null));
            } else {
                if (target.getTeamColor() != color) {
                    moves.add(new ChessMove(from, to, null));
                }
                break;
            }
            row += dRow;
            col += dCol;
        }
    }

    private void addKnightMoves(ChessBoard board, ChessPosition from, Collection<ChessMove> moves) {
        int[][] deltas = {
                {2, 1}, {2, -1}, {1, 2}, {1, -2},
                {-1, 2}, {-1, -2}, {-2, 1}, {-2, -1}
        };
        for (int[] delta : deltas) {
            int row = from.getRow() + delta[0];
            int col = from.getColumn() + delta[1];
            if (row < 1 || row > 8 || col < 1 || col > 8) {
                continue;
            }
            ChessPosition to = new ChessPosition(row, col);
            ChessPiece target = board.getPiece(to);
            if (target == null || target.getTeamColor() != color) {
                moves.add(new ChessMove(from, to, null));
            }
        }
    }

    private void addKingMoves(ChessBoard board, ChessPosition from, Collection<ChessMove> moves) {
        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow == 0 && dCol == 0) {
                    continue;
                }
                int row = from.getRow() + dRow;
                int col = from.getColumn() + dCol;
                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    continue;
                }
                ChessPosition to = new ChessPosition(row, col);
                ChessPiece target = board.getPiece(to);
                if (target == null || target.getTeamColor() != color) {
                    moves.add(new ChessMove(from, to, null));
                }
            }
        }
    }
}

