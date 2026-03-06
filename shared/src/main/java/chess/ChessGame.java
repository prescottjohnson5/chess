package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Manages a chess game and moves on a board.
 * <p>
 * Note: You can add to this class, but you may not alter
 * the signature of the existing methods.
 */
public class ChessGame {

    private static final int BOARD_MIN = 1;
    private static final int BOARD_MAX = 8;

    private ChessBoard currentBoard;
    private TeamColor currentTurn;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(currentBoard, chessGame.currentBoard) &&
            currentTurn == chessGame.currentTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentBoard, currentTurn);
    }

    public ChessGame() {
        currentBoard = new ChessBoard();
        currentBoard.resetBoard();
        currentTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Sets which team's turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
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
        ChessPiece startPiece = currentBoard.getPiece(startPosition);

        if (startPiece == null) {
            return new ArrayList<>();
        }
        
        Collection<ChessMove> pieceMoves = startPiece.pieceMoves(currentBoard, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();
        
        
        for (ChessMove move : pieceMoves) {
            ChessBoard boardAfterMove = copyBoardAndApplyMove(move);
            ChessGame gameAfterMove = new ChessGame();
            gameAfterMove.setBoard(boardAfterMove);
            if (!gameAfterMove.isInCheck(startPiece.getTeamColor())) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    /**
     * Returns a copy of the current board with the given move applied.
     */
    private ChessBoard copyBoardAndApplyMove(ChessMove move) {
        ChessBoard copy = new ChessBoard();
        for (int row = BOARD_MIN; row <= BOARD_MAX; row++) {
            for (int col = BOARD_MIN; col <= BOARD_MAX; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                copy.addPiece(pos, currentBoard.getPiece(pos));
            }
        }
        copy.addPiece(move.getEndPosition(), copy.getPiece(move.getStartPosition()));
        copy.addPiece(move.getStartPosition(), null);
        return copy;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece startPiece = currentBoard.getPiece(move.getStartPosition());
        if (startPiece == null) {
            throw new InvalidMoveException("No piece at start position");
        }
        if (startPiece.getTeamColor() != currentTurn) {
            throw new InvalidMoveException("Not the current team's turn");
        }
        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());

        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException("Move is not valid");
        }
        ChessPiece.PieceType promotionType = move.getPromotionPiece();
        if (promotionType == null) {
            currentBoard.addPiece(move.getEndPosition(), currentBoard.getPiece(move.getStartPosition()));
            currentBoard.addPiece(move.getStartPosition(), null);
        } else {
            currentBoard.addPiece(move.getEndPosition(), new ChessPiece(startPiece.getTeamColor(), promotionType));
            currentBoard.addPiece(move.getStartPosition(), null);
        }

        if (currentTurn == TeamColor.WHITE) {
            currentTurn = TeamColor.BLACK;
        } else {
            currentTurn = TeamColor.WHITE;
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public ChessPosition getKing(TeamColor team) {
        for (int row = BOARD_MIN; row <= BOARD_MAX; row++) {
            for (int col = BOARD_MIN; col <= BOARD_MAX; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = currentBoard.getPiece(position);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == team) {
                    return position;
                }
            }
        }
        return null;
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = getKing(teamColor);
        for (int row = BOARD_MIN; row <= BOARD_MAX; row++) {
            for (int col = BOARD_MIN; col <= BOARD_MAX; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = currentBoard.getPiece(position);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(currentBoard, position);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
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
        if (!this.isInCheck(teamColor)) {
            return false;
        }
        for (int row = BOARD_MIN; row <= BOARD_MAX; row++) {
            for (int col = BOARD_MIN; col <= BOARD_MAX; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = currentBoard.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> legalMoves = validMoves(position);
                    if (!legalMoves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (this.isInCheck(teamColor)) {
            return false;
        }
        for (int row = BOARD_MIN; row <= BOARD_MAX; row++) {
            for (int col = BOARD_MIN; col <= BOARD_MAX; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = currentBoard.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> legalMoves = validMoves(position);
                    if (!legalMoves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        currentBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return currentBoard;
    }
}
