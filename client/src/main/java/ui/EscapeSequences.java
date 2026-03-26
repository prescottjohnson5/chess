package ui;

/**
 * This class contains constants and functions relating to ANSI Escape Sequences that are useful in the Client display
 */
public class EscapeSequences {

    private static final String UNICODE_ESCAPE = "\u001b";

    private EscapeSequences() {
    }

    private static String fg(int color) {
        return UNICODE_ESCAPE + "[38;5;" + color + "m";
    }

    private static String bg(int color) {
        return UNICODE_ESCAPE + "[48;5;" + color + "m";
    }

    public static final String ERASE_SCREEN = UNICODE_ESCAPE + "[H" + UNICODE_ESCAPE + "[2J";
    public static final String ERASE_LINE = UNICODE_ESCAPE + "[2K";

    public static final String SET_TEXT_BOLD = UNICODE_ESCAPE + "[1m";
    public static final String SET_TEXT_FAINT = UNICODE_ESCAPE + "[2m";
    public static final String RESET_TEXT_BOLD_FAINT = UNICODE_ESCAPE + "[22m";
    public static final String SET_TEXT_ITALIC = UNICODE_ESCAPE + "[3m";
    public static final String RESET_TEXT_ITALIC = UNICODE_ESCAPE + "[23m";
    public static final String SET_TEXT_UNDERLINE = UNICODE_ESCAPE + "[4m";
    public static final String RESET_TEXT_UNDERLINE = UNICODE_ESCAPE + "[24m";
    public static final String SET_TEXT_BLINKING = UNICODE_ESCAPE + "[5m";
    public static final String RESET_TEXT_BLINKING = UNICODE_ESCAPE + "[25m";

    public static final String SET_TEXT_COLOR_BLACK = fg(0);
    public static final String SET_TEXT_COLOR_LIGHT_GREY = fg(242);
    public static final String SET_TEXT_COLOR_DARK_GREY = fg(235);
    public static final String SET_TEXT_COLOR_RED = fg(160);
    public static final String SET_TEXT_COLOR_GREEN = fg(46);
    public static final String SET_TEXT_COLOR_YELLOW = fg(226);
    public static final String SET_TEXT_COLOR_BLUE = fg(12);
    public static final String SET_TEXT_COLOR_MAGENTA = fg(5);
    public static final String SET_TEXT_COLOR_WHITE = fg(15);
    public static final String RESET_TEXT_COLOR = UNICODE_ESCAPE + "[39m";

    public static final String SET_BG_COLOR_BLACK = bg(0);
    public static final String SET_BG_COLOR_LIGHT_GREY = bg(242);
    public static final String SET_BG_COLOR_DARK_GREY = bg(235);
    public static final String SET_BG_COLOR_RED = bg(160);
    public static final String SET_BG_COLOR_GREEN = bg(46);
    public static final String SET_BG_COLOR_DARK_GREEN = bg(22);
    public static final String SET_BG_COLOR_YELLOW = bg(226);
    public static final String SET_BG_COLOR_BLUE = bg(12);
    public static final String SET_BG_COLOR_MAGENTA = bg(5);
    public static final String SET_BG_COLOR_WHITE = bg(15);
    public static final String RESET_BG_COLOR = UNICODE_ESCAPE + "[49m";

    public static final String WHITE_KING = " ♔ ";
    public static final String WHITE_QUEEN = " ♕ ";
    public static final String WHITE_BISHOP = " ♗ ";
    public static final String WHITE_KNIGHT = " ♘ ";
    public static final String WHITE_ROOK = " ♖ ";
    public static final String WHITE_PAWN = " ♙ ";
    public static final String BLACK_KING = " ♚ ";
    public static final String BLACK_QUEEN = " ♛ ";
    public static final String BLACK_BISHOP = " ♝ ";
    public static final String BLACK_KNIGHT = " ♞ ";
    public static final String BLACK_ROOK = " ♜ ";
    public static final String BLACK_PAWN = " ♟ ";
    public static final String EMPTY = " \u2003 ";

    public static String moveCursorToLocation(int x, int y) {
        return UNICODE_ESCAPE + "[" + y + ";" + x + "H";
    }
}

