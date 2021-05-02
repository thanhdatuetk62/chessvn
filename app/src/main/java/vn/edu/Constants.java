package vn.edu;

public class Constants {
    public static final int SIZE = 8;
    public static final int AI_MODE = 0;
    public static final int LAN_MODE = 1;
    public static final int WHITE_PERSPECTIVE = 0;
    public static final int BLACK_PERSPECTIVE = 1;
    public static final String pathToDisk = "-1";
    public static final int NOVICE_LEVEL = 0;
    public static final int EXPERT_LEVEL = 1;
    public static final int MASTER_LEVEL = 2;
    public static final char BLACK_COLOR = 'b';
    public static final char WHITE_COLOR = 'w';
    public static final char[] COLORS = {WHITE_COLOR, BLACK_COLOR};
    public static final char PAWN = 'P';
    public static final char ROOK = 'R';
    public static final char KNIGHT = 'N';
    public static final char BISHOP = 'B';
    public static final char QUEEN = 'Q';
    public static final char KING = 'K';
    public static final String[] COL_LABELS = {"a", "b", "c", "d", "e", "f", "g", "h"};
    public static final String[] ROW_LABELS = {"8", "7", "6", "5", "4", "3", "2", "1"};
    public static final int SELECT_MARK_TAG = 0;
    public static final int HISTORY_MARK_TAG = 1;
    public static final int CAPTURE_MARK_TAG = 2;
    public static final int OCCUPY_MARK_TAG = 3;
    public static final int PROMOTE_MARK_TAG = 4;
    public static final int CHECK_MARK_TAG = 5;
    public static final int[] ROOK_X = {0, 0, SIZE-1, SIZE-1};
    public static final int[] ROOK_Y = {0, SIZE-1, 0, SIZE-1};
}
