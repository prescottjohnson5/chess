package chess;

import java.util.Objects;
/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {
    @Override
    public boolean equals(Object instance) {
        if (this == instance) {
            return true;
        } else if (instance == null || getClass() != instance.getClass()){
            return false;
        }
        ChessMove that = (ChessMove) instance;
        return Objects.equals(start, that.start) && Objects.equals(end, that.end) && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, pieceType);
    }

    ChessPosition start;
    ChessPosition end;
    ChessPiece.PieceType pieceType;
    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        start = startPosition;
        end = endPosition;
        pieceType = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return start;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {

        return end;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {

        return pieceType;
    }
}
