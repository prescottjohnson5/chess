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
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
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
                        ChessPiece leftPiece = board.getPiece(leftDiagonal);
                        if (leftPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
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

        ChessPosition curr = new ChessPosition(myPosition.getRow(), myPosition.getColumn());
        if (this.pieceType == ChessPiece.PieceType.BISHOP) {

            while (curr.getColumn() > 1 && curr.getRow() < 8) {
                ChessPosition upLeft = new ChessPosition(curr.getRow() + 1, curr.getColumn() - 1);
                ChessPiece upLeftPiece = board.getPiece(upLeft);
                if (upLeftPiece == null) {
                    moves.add(new ChessMove(myPosition, upLeft, null));
                } else {
                    if (upLeftPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, upLeft, null));
                        break;
                    } else {
                        break;
                    }
                }
                curr = upLeft;
            }
            curr = myPosition;

            while (curr.getColumn() < 8 && curr.getRow() < 8) {
                ChessPosition upRight = new ChessPosition(curr.getRow() + 1, curr.getColumn() + 1);
                ChessPiece upRightPiece = board.getPiece(upRight);
                if (upRightPiece == null) {
                    moves.add(new ChessMove(myPosition, upRight, null));
                } else {
                    if (upRightPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, upRight, null));
                        break;
                    } else {
                        break;
                    }
                }
                curr = upRight;
            }
            curr = myPosition;

            while (curr.getColumn() > 1 && curr.getRow() > 1) {
                ChessPosition downLeft = new ChessPosition(curr.getRow() - 1, curr.getColumn() - 1);
                ChessPiece downLeftPiece = board.getPiece(downLeft);
                if (downLeftPiece == null) {
                    moves.add(new ChessMove(myPosition, downLeft, null));
                } else {
                    if (downLeftPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, downLeft, null));
                        break;
                    } else {
                        break;
                    }
                }
                curr = downLeft;
            }
            curr = myPosition;

            while (curr.getColumn() < 8 && curr.getRow() > 1) {
                ChessPosition downRight = new ChessPosition(curr.getRow() - 1, curr.getColumn() + 1);
                ChessPiece downRightPiece = board.getPiece(downRight);
                if (downRightPiece == null) {
                    moves.add(new ChessMove(myPosition, downRight, null));
                } else {
                    if (downRightPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, downRight, null));
                        break;
                    } else {
                        break;
                    }
                }
                curr = downRight;
            }
            curr = myPosition;
        }


        if (this.pieceType == ChessPiece.PieceType.ROOK) {

            while (curr.getColumn() > 1) {
                ChessPosition left = new ChessPosition(curr.getRow(), curr.getColumn() - 1);
                ChessPiece leftPiece = board.getPiece(left);
                if (leftPiece == null) {
                    moves.add(new ChessMove(myPosition, left, null));
                } else {
                    if (leftPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, left, null));
                        break;
                    } else {
                        break;
                    }
                }
                curr = left;
            }
            curr = myPosition;

            while (curr.getColumn() < 8) {
                ChessPosition right = new ChessPosition(curr.getRow(), curr.getColumn() + 1);
                ChessPiece rightPiece = board.getPiece(right);
                if (rightPiece == null) {
                    moves.add(new ChessMove(myPosition, right, null));
                } else {
                    if (rightPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, right, null));
                        break;
                    } else {
                        break;
                    }
                }
                curr = right;
            }
            curr = myPosition;

            while (curr.getRow() > 1) {
                ChessPosition down = new ChessPosition(curr.getRow() - 1, curr.getColumn());
                ChessPiece downPiece = board.getPiece(down);
                if (downPiece == null) {
                    moves.add(new ChessMove(myPosition, down, null));
                } else {
                    if (downPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, down, null));
                        break;
                    } else {
                        break;
                    }
                }
                curr = down;
            }
            curr = myPosition;

            while (curr.getRow() < 8) {
                ChessPosition up = new ChessPosition(curr.getRow() + 1, curr.getColumn());
                ChessPiece upPiece = board.getPiece(up);
                if (upPiece == null) {
                    moves.add(new ChessMove(myPosition, up, null));
                } else {
                    if (upPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, up, null));
                        break;
                    } else {
                        break;
                    }
                }
                curr = up;
            }
        }
        curr = myPosition;


        if (this.pieceType == ChessPiece.PieceType.KNIGHT) {
            if (myPosition.getRow() + 2 <= 8 && myPosition.getColumn() + 1 <= 8) {
                ChessPosition move1 = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() + 1);
                if (board.getPiece(move1) == null || board.getPiece(move1).getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, move1, null));
                }
            }
            if (myPosition.getRow() + 2 <= 8 && myPosition.getColumn() - 1 >= 1 ) {
                ChessPosition move2 = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() - 1);
                if (board.getPiece(move2) == null || board.getPiece(move2).getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, move2, null));
                }
            }
            if (myPosition.getRow() + 1 <= 8 && myPosition.getColumn() + 2 <= 8) {
                ChessPosition move3 = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 2);
                if (board.getPiece(move3) == null || board.getPiece(move3).getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, move3, null));
                }
            }
            if (myPosition.getRow() + 1 <= 8 && myPosition.getColumn() - 2 >=1) {
                ChessPosition move4 = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 2);
                if (board.getPiece(move4) == null || board.getPiece(move4).getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, move4, null));
                }
            }
            if (myPosition.getRow() - 1 >= 1 && myPosition.getColumn() + 2 <= 8) {
                ChessPosition move5 = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 2);
                if (board.getPiece(move5) == null || board.getPiece(move5).getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, move5, null));
                }
            }
            if (myPosition.getRow() - 1 >= 1 && myPosition.getColumn() - 2 >= 1) {
                ChessPosition move6 = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 2);
                if (board.getPiece(move6) == null || board.getPiece(move6).getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, move6, null));
                }
            }
            if (myPosition.getRow() - 2 >= 1 && myPosition.getColumn() + 1 <= 8) {
                ChessPosition move7 = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() + 1);
                if (board.getPiece(move7) == null || board.getPiece(move7).getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, move7, null));
                }
            }
            if (myPosition.getRow() - 2 >= 1 && myPosition.getColumn() - 1 >= 1) {
                ChessPosition move8 = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() - 1);
                if (board.getPiece(move8) == null || board.getPiece(move8).getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, move8, null));
                }
            }
        }


        if (this.pieceType == ChessPiece.PieceType.QUEEN) {
            while (curr.getColumn() > 1 && curr.getRow() < 8) {
                ChessPosition upLeft = new ChessPosition(curr.getRow() + 1, curr.getColumn() - 1);
                ChessPiece upLeftPiece = board.getPiece(upLeft);
                if (upLeftPiece == null) {
                    moves.add(new ChessMove(myPosition, upLeft, null));
                } else {
                    if (upLeftPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, upLeft, null));
                        break;
                    } else {
                        break;
                    }
                }
                curr = upLeft;
            }
            curr = myPosition;

            while (curr.getColumn() < 8 && curr.getRow() < 8) {
                ChessPosition upRight = new ChessPosition(curr.getRow() + 1, curr.getColumn() + 1);
                ChessPiece upRightPiece = board.getPiece(upRight);
                if (upRightPiece == null) {
                    moves.add(new ChessMove(myPosition, upRight, null));
                } else {
                    if (upRightPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, upRight, null));
                        break;
                    } else {
                        break;
                    }
                }
                curr = upRight;
            }
            curr = myPosition;

            while (curr.getColumn() > 1 && curr.getRow() > 1) {
                ChessPosition downLeft = new ChessPosition(curr.getRow() - 1, curr.getColumn() - 1);
                ChessPiece downLeftPiece = board.getPiece(downLeft);
                if (downLeftPiece == null) {
                    moves.add(new ChessMove(myPosition, downLeft, null));
                } else {
                    if (downLeftPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, downLeft, null));
                        break;
                    } else {
                        break;
                    }
                }
                curr = downLeft;
            }
            curr = myPosition;

            while (curr.getColumn() < 8 && curr.getRow() > 1) {
                ChessPosition downRight = new ChessPosition(curr.getRow() - 1, curr.getColumn() + 1);
                ChessPiece downRightPiece = board.getPiece(downRight);
                if (downRightPiece == null) {
                    moves.add(new ChessMove(myPosition, downRight, null));
                } else {
                    if (downRightPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, downRight, null));
                        break;
                    } else {
                        break;
                    }
                }
                curr = downRight;
            }
            curr = myPosition;

            while (curr.getColumn() > 1) {
                ChessPosition left = new ChessPosition(curr.getRow(), curr.getColumn() - 1);
                ChessPiece leftPiece = board.getPiece(left);
                if (leftPiece == null) {
                    moves.add(new ChessMove(myPosition, left, null));
                } else {
                    if (leftPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, left, null));
                        break;
                    } else {
                        break;
                    }
                }
                curr = left;
            }
            curr = myPosition;

            while (curr.getColumn() < 8) {
                ChessPosition right = new ChessPosition(curr.getRow(), curr.getColumn() + 1);
                ChessPiece rightPiece = board.getPiece(right);
                if (rightPiece == null) {
                    moves.add(new ChessMove(myPosition, right, null));
                } else {
                    if (rightPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, right, null));
                        break;
                    } else {
                        break;
                    }
                }
                curr = right;
            }
            curr = myPosition;

            while (curr.getRow() > 1) {
                ChessPosition down = new ChessPosition(curr.getRow() - 1, curr.getColumn());
                ChessPiece downPiece = board.getPiece(down);
                if (downPiece == null) {
                    moves.add(new ChessMove(myPosition, down, null));
                } else {
                    if (downPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, down, null));
                        break;
                    } else {
                        break;
                    }
                }
                curr = down;
            }
            curr = myPosition;

            while (curr.getRow() < 8) {
                ChessPosition up = new ChessPosition(curr.getRow() + 1, curr.getColumn());
                ChessPiece upPiece = board.getPiece(up);
                if (upPiece == null) {
                    moves.add(new ChessMove(myPosition, up, null));
                } else {
                    if (upPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, up, null));
                        break;
                    } else {
                        break;
                    }
                }
                curr = up;
            }
        }


        if (this.pieceType == PieceType.KING) {
            if (myPosition.getRow() < 8) {
                ChessPosition up = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
                ChessPiece upPiece = board.getPiece(up);
                if (upPiece == null) {
                    moves.add(new ChessMove(myPosition, up, null));
                } else {
                    if (upPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, up, null));
                    }
                }
            }
            if (myPosition.getRow() > 1) {
                ChessPosition down = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
                ChessPiece downPiece = board.getPiece(down);
                if (downPiece == null) {
                    moves.add(new ChessMove(myPosition, down, null));
                } else {
                    if (downPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, down, null));
                    }
                }
            }
            if (myPosition.getColumn() < 8) {
                ChessPosition right = new ChessPosition(myPosition.getRow(), myPosition.getColumn() + 1);
                ChessPiece rightPiece = board.getPiece(right);
                if (rightPiece == null) {
                    moves.add(new ChessMove(myPosition, right, null));
                } else {
                    if (rightPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, right, null));
                    }
                }
            }
            if (myPosition.getColumn() > 1) {
                ChessPosition left = new ChessPosition(myPosition.getRow(), myPosition.getColumn() - 1);
                ChessPiece leftPiece = board.getPiece(left);
                if (leftPiece == null) {
                    moves.add(new ChessMove(myPosition, left, null));
                } else {
                    if (leftPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, left, null));
                    }
                }
            }

            if (myPosition.getRow() < 8 && myPosition.getColumn() < 8) {
                ChessPosition upRight = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1);
                ChessPiece upRightPiece = board.getPiece(upRight);
                if (upRightPiece == null) {
                    moves.add(new ChessMove(myPosition, upRight, null));
                } else {
                    if (upRightPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, upRight, null));
                    }
                }
            }
            if (myPosition.getRow() < 8 && myPosition.getColumn() > 1) {
                ChessPosition upLeft = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1);
                ChessPiece upLeftPiece = board.getPiece(upLeft);
                if (upLeftPiece == null) {
                    moves.add(new ChessMove(myPosition, upLeft, null));
                } else {
                    if (upLeftPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, upLeft, null));
                    }
                }
            }
            if (myPosition.getRow() > 1 && myPosition.getColumn() < 8) {
                ChessPosition downRight = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1);
                ChessPiece downRightPiece = board.getPiece(downRight);
                if (downRightPiece == null) {
                    moves.add(new ChessMove(myPosition, downRight, null));
                } else {
                    if (downRightPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, downRight, null));
                    }
                }
            }
            if (myPosition.getRow() > 1 && myPosition.getColumn() > 1) {
                ChessPosition downLeft = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1);
                ChessPiece downLeftPiece = board.getPiece(downLeft);
                if (downLeftPiece == null) {
                    moves.add(new ChessMove(myPosition, downLeft, null));
                } else {
                    if (downLeftPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, downLeft, null));
                    }
                }
            }
        }
    return moves;

    }
}

