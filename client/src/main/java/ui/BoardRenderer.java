package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class BoardRenderer {

    private BoardRenderer() {
    }

    private static boolean isLightSquare(int row, int col) {
        return (row + col) % 2 != 0;
    }

    public static void drawBoard(ChessGame game, ChessGame.TeamColor perspective,
                                 ChessPosition from, Collection<ChessPosition> legalEnds) {
        if (game == null || perspective == null) {
            return;
        }
        Set<ChessPosition> ends = legalEnds == null ? Collections.emptySet() : new HashSet<>(legalEnds);
        drawCells(game.getBoard(), perspective, from, ends);
    }

    private static void drawCells(ChessBoard board, ChessGame.TeamColor perspective,
                                  ChessPosition from, Set<ChessPosition> legalEnds) {
        if (board == null) {
            return;
        }

        System.out.print("   ");
        for (int colIndex = 0; colIndex < 8; colIndex++) {
            char letter;
            if (perspective == ChessGame.TeamColor.WHITE) {
                letter = (char) ('A' + colIndex);
            } else {
                letter = (char) ('A' + (7 - colIndex));
            }
            System.out.print(" " + letter + " ");
        }
        System.out.println();

        for (int rowIndex = 0; rowIndex < 8; rowIndex++) {
            int row = perspective == ChessGame.TeamColor.WHITE ? (8 - rowIndex) : (1 + rowIndex);
            System.out.print(row + " ");

            for (int colIndex = 0; colIndex < 8; colIndex++) {
                int col = perspective == ChessGame.TeamColor.WHITE ? (1 + colIndex) : (8 - colIndex);
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                String bg;
                if (from != null && pos.equals(from)) {
                    bg = EscapeSequences.SET_BG_COLOR_YELLOW;
                } else if (legalEnds.contains(pos)) {
                    bg = EscapeSequences.SET_BG_COLOR_DARK_GREEN;
                } else {
                    boolean lightSquare = isLightSquare(row, col);
                    bg = lightSquare
                            ? EscapeSequences.SET_BG_COLOR_LIGHT_BLUE
                            : EscapeSequences.SET_BG_COLOR_DARK_BLUE;
                }

                if (piece == null) {
                    System.out.print(bg + EscapeSequences.RESET_TEXT_COLOR + EscapeSequences.EMPTY + EscapeSequences.RESET_BG_COLOR);
                } else {
                    String fg = piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? EscapeSequences.SET_TEXT_COLOR_WHITE
                            : EscapeSequences.SET_TEXT_COLOR_BLACK;
                    System.out.print(bg + fg + renderPiece(piece)
                            + EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
                }
            }
            System.out.println();
        }
        System.out.println(EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
    }

    private static String renderPiece(ChessPiece piece) {
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }

        return switch (piece.getPieceType()) {
            case KING -> EscapeSequences.WHITE_KING;
            case QUEEN -> EscapeSequences.WHITE_QUEEN;
            case BISHOP -> EscapeSequences.WHITE_BISHOP;
            case KNIGHT -> EscapeSequences.WHITE_KNIGHT;
            case ROOK -> EscapeSequences.WHITE_ROOK;
            case PAWN -> EscapeSequences.WHITE_PAWN;
        };
    }
}
