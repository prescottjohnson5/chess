package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;
/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    int num;
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
    ChessBoard currentBoard;
    /**
     * @return Which team's turn it is
     */
    TeamColor currentTurn = ChessGame.TeamColor.WHITE;
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
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
            ChessBoard tempBoard = new ChessBoard();
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    ChessPosition position = new ChessPosition(i, j);
                    tempBoard.addPiece(position, currentBoard.getPiece(position));
                }
            }
            tempBoard.addPiece(move.getEndPosition(), tempBoard.getPiece(move.getStartPosition()));
            tempBoard.addPiece(move.getStartPosition(), null);

            ChessGame tempGame = new ChessGame();
            tempGame.setBoard(tempBoard);
            boolean inCheck = tempGame.isInCheck(startPiece.getTeamColor());

            if (inCheck != true) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
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

        TeamColor turn = this.getTeamTurn();
        if (turn == TeamColor.WHITE) {
            this.setTeamTurn(TeamColor.BLACK);
        } else {
            this.setTeamTurn(TeamColor.WHITE);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public ChessPosition getKing(TeamColor team) {
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition current = new ChessPosition(i, j);
                ChessPiece currPiece = currentBoard.getPiece(current);
                if (currPiece != null && currPiece.getPieceType() == ChessPiece.PieceType.KING && currPiece.getTeamColor() == team) {
                    return current;
                }
            }
        }
        return null;
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = getKing(teamColor);
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition current = new ChessPosition(i, j);
                ChessPiece currentPiece = currentBoard.getPiece(current);

                if (currentPiece != null && currentPiece.getTeamColor() != teamColor) {
                    Collection<ChessMove> pieceMoves = currentPiece.pieceMoves(currentBoard, current);
                    for (ChessMove move : currentPiece.pieceMoves(currentBoard, current)) {
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
        for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    ChessPosition position = new ChessPosition(i, j);
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
        for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    ChessPosition position = new ChessPosition(i, j);
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
