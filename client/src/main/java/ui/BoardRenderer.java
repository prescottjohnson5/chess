package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public final class BoardRenderer {

    private BoardRenderer() {
    }

    public static void drawInitialBoard(ChessGame game, ChessGame.TeamColor perspective) {
        if (game == null || perspective == null) {
            return;
        }
        drawBoard(game.getBoard(), perspective);
    }

    private static void drawBoard(ChessBoard board, ChessGame.TeamColor perspective) {
        if (board == null) {
            return;
        }

        int[] colOrder = perspective == ChessGame.TeamColor.WHITE
                ? new int[]{1, 2, 3, 4, 5, 6, 7, 8}
                : new int[]{8, 7, 6, 5, 4, 3, 2, 1};

        int[] rowOrder = perspective == ChessGame.TeamColor.WHITE
                ? new int[]{8, 7, 6, 5, 4, 3, 2, 1}
                : new int[]{1, 2, 3, 4, 5, 6, 7, 8};

        char[] letters = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'};
        char[] lettersToPrint = perspective == ChessGame.TeamColor.WHITE
                ? letters
                : new char[]{'H', 'G', 'F', 'E', 'D', 'C', 'B', 'A'};

        System.out.print("   ");
        for (char letter : lettersToPrint) {
            System.out.print(" " + letter + " ");
        }
        System.out.println();

        for (int displayRow : rowOrder) {
            System.out.print(displayRow + " ");
            for (int displayCol : colOrder) {
                ChessPosition pos = new ChessPosition(displayRow, displayCol);
                ChessPiece piece = board.getPiece(pos);

                boolean lightSquare = (displayRow + displayCol) % 2 != 0;
                String bg = lightSquare
                        ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY
                        : EscapeSequences.SET_BG_COLOR_DARK_GREY;

                String textColor;
                if (piece == null) {
                    textColor = EscapeSequences.RESET_TEXT_COLOR;
                    System.out.print(bg + textColor + EscapeSequences.EMPTY + EscapeSequences.RESET_BG_COLOR);
                } else if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                    textColor = EscapeSequences.SET_TEXT_COLOR_RED;
                    System.out.print(bg + textColor + renderPiece(piece) + EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
                } else {
                    textColor = EscapeSequences.SET_TEXT_COLOR_BLUE;
                    System.out.print(bg + textColor + renderPiece(piece) + EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
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
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
        };
    }
}

