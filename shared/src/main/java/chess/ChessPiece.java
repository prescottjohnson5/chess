package chess;

import java.util.Collection;
import java.util.ArrayList;
/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
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
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new Collection<>();
        if (this.getPieceType() == ChessPiece.PieceType.PAWN) {
            if (this.getTeamColor() == ChessGame.TeamColor.WHITE) {
                ChessPosition oneForward = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
                ChessPosition twoForward = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn());
                ChessPosition rightDiagonal = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1);
                ChessPosition leftDiagonal = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1);

                if (myPosition.getRow() == 2) {
                    if (board.getPiece(oneForward) == null && board.getPiece(twoForward) == null) {
                        moves.add(new ChessMove(myPosition, twoForward, null));
                    }
                }
                if (board.getPiece(oneForward) == null) {
                    if (oneForward.getRow() == 8) {
                        moves.add(new ChessMove(myPosition, oneForward, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, oneForward, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, oneForward, PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, oneForward, PieceType.KNIGHT));
                    } else {
                        moves.add(new ChessMove(myPosition, oneForward, null));
                    }
                }
                if (myPosition.getColumn() < 8) {
                    if (board.getPiece(rightDiagonal) != null) {
                        ChessPiece rightPiece = board.getPiece(rightDiagonal);
                        if (rightPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                            if (rightDiagonal.getRow() == 8) {
                                moves.add(new ChessMove(myPosition, rightDiagonal, PieceType.QUEEN));
                                moves.add(new ChessMove(myPosition, rightDiagonal, PieceType.ROOK));
                                moves.add(new ChessMove(myPosition, rightDiagonal, PieceType.BISHOP));
                                moves.add(new ChessMove(myPosition, rightDiagonal, PieceType.KNIGHT));
                            } else {
                                moves.add(new ChessMove(myPosition, rightDiagonal, null));
                            }
                        }
                    }
                }

                if (myPosition.getColumn() > 1) {
                    if (board.getPiece(leftDiagonal) != null) {
                        ChessPiece rightPiece = board.getPiece(leftDiagonal);
                        if (rightPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                            if (leftDiagonal.getRow() == 8) {
                                moves.add(new ChessMove(myPosition, leftDiagonal, PieceType.QUEEN));
                                moves.add(new ChessMove(myPosition, leftDiagonal, PieceType.ROOK));
                                moves.add(new ChessMove(myPosition, leftDiagonal, PieceType.BISHOP));
                                moves.add(new ChessMove(myPosition, leftDiagonal, PieceType.KNIGHT));
                            } else {
                                moves.add(new ChessMove(myPosition, leftDiagonal, null));
                            }
                        }
                    }
                }
            }

            if (this.getTeamColor() == ChessGame.TeamColor.BLACK) {
                ChessPosition oneDown = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
                ChessPosition twoDown = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn());
                ChessPosition bottomRightDiagonal = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1);
                ChessPosition bottomLeftDiagonal = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1);

                if (myPosition.getRow() == 7) {
                    if (board.getPiece(oneDown) == null && board.getPiece(twoDown) == null) {
                        moves.add(new ChessMove(myPosition, twoDown, null));
                    }
                }
                if (board.getPiece(oneDown) == null) {
                    if (oneDown.getRow() == 1) {
                        moves.add(new ChessMove(myPosition, oneDown, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, oneDown, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, oneDown, PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, oneDown, PieceType.KNIGHT));
                    } else {
                        moves.add(new ChessMove(myPosition, oneDown, null));
                    }
                }
                if (myPosition.getColumn() < 8) {
                    if (board.getPiece(bottomRightDiagonal) != null) {
                        ChessPiece rightPiece = board.getPiece(bottomRightDiagonal);
                        if (rightPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                            if (bottomRightDiagonal.getRow() == 1) {
                                moves.add(new ChessMove(myPosition, bottomRightDiagonal, PieceType.QUEEN));
                                moves.add(new ChessMove(myPosition, bottomRightDiagonal, PieceType.ROOK));
                                moves.add(new ChessMove(myPosition, bottomRightDiagonal, PieceType.BISHOP));
                                moves.add(new ChessMove(myPosition, bottomRightDiagonal, PieceType.KNIGHT));
                            } else {
                                moves.add(new ChessMove(myPosition, bottomRightDiagonal, null));
                            }
                        }
                    }
                }
                if (myPosition.getColumn() > 1) {
                    if (board.getPiece(bottomLeftDiagonal) != null) {
                        ChessPiece rightPiece = board.getPiece(bottomLeftDiagonal);
                        if (rightPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                            if (bottomLeftDiagonal.getRow() == 1) {
                                moves.add(new ChessMove(myPosition, bottomLeftDiagonal, PieceType.QUEEN));
                                moves.add(new ChessMove(myPosition, bottomLeftDiagonal, PieceType.ROOK));
                                moves.add(new ChessMove(myPosition, bottomLeftDiagonal, PieceType.BISHOP));
                                moves.add(new ChessMove(myPosition, bottomLeftDiagonal, PieceType.KNIGHT));
                            } else {
                                moves.add(new ChessMove(myPosition, bottomLeftDiagonal, null));
                            }
                        }
                    }
                }
            }

        }

    return moves;

    }
}

