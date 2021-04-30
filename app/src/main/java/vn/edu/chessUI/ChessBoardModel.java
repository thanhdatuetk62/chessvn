package vn.edu.chessUI;

import android.os.Build;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChessBoardModel { // This class deals with game logic!
    private static final int SIZE = 8;

    private static final int INVALID_FLAG = -1;
    private static final int CAPTURE_FLAG = 1;
    private static final int NORMAL_FLAG = 2;
    private static final int PROMOTE_FLAG = 4;
    private static final int CHECK_FLAG = 8;
    private static final int CASTLING_FLAG = 16;

    private static final int SELECT_MARK_TAG = 1;
    private static final int HISTORY_MARK_TAG = 2;
    private static final int CAPTURE_MARK_TAG = 3;
    private static final int OCCUPY_MARK_TAG = 4;
    private static final int PROMOTE_MARK_TAG = 5;

    private static final int WHITE_PERSPECTIVE = 0;
    private static final int BLACK_PERSPECTIVE = 1;

    private static final char BLACK_COLOR = 'b';
    private static final char WHITE_COLOR = 'w';
    private static final char[] COLORS = {WHITE_COLOR, BLACK_COLOR};

    private static final char PAWN = 'P';
    private static final char ROOK = 'R';
    private static final char KNIGHT = 'N';
    private static final char BISHOP = 'B';
    private static final char QUEEN = 'Q';
    private static final char KING = 'K';

    private static final String[] COL_LABELS = {"a", "b", "c", "d", "e", "f", "g", "h"};
    private static final String[] ROW_LABELS = {"8", "7", "6", "5", "4", "3", "2", "1"};

    private char mAlly;
    private int mTurn;
    private int mWhiteChecks, mBlackChecks;
    private Map<String, Pair<Integer, Integer>> mIndexTable;
    private String[][] mState = new String[SIZE][SIZE];

    public ChessBoardModel() {
        init();
        startFromScratch();
    }

    private void init() {
        // Create index table
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mIndexTable = new ArrayMap<>();
        } else {
            mIndexTable = new HashMap<>();
        }
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                mIndexTable.put(COL_LABELS[c] + ROW_LABELS[r], new Pair<>(r, c));
            }
        }
    }

    private void startFromScratch() {
        // Create new state / Start from scratch
        mAlly = COLORS[WHITE_PERSPECTIVE];
        mTurn = WHITE_PERSPECTIVE;
        mWhiteChecks = mBlackChecks = 0;
        mState = new String[][]{
                {"bR", "bN", "bB", "bK", "bQ", "bB", "bN", "bR"},
                {"bP", "bP", "bP", "bP", "bP", "bP", "bP", "bP"},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {"wP", "wP", "wP", "wP", "wP", "wP", "wP", "wP"},
                {"wR", "wN", "wB", "wK", "wQ", "wB", "wN", "wR"},
        };
    }

    public char getAlly() {
        return mAlly;
    }

    @Nullable
    public String getPieceAt(String location) {
        Pair<Integer, Integer> indexLocation = mIndexTable.get(location);
        assert indexLocation != null;
        int x = indexLocation.first;
        int y = indexLocation.second;
        return mState[x][y];
    }

    private char getColor(int x, int y) {
        assert mState[x][y] != null;
        return mState[x][y].charAt(0);
    }

    private char getPieceType(int x, int y) {
        assert mState[x][y] != null;
        return mState[x][y].charAt(1);
    }

    public ArrayList<Pair<String, String>> getPieceLocations() {
        ArrayList<Pair<String, String>> pieces = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (mState[r][c] != null) {
                    pieces.add(new Pair<>(COL_LABELS[c] + ROW_LABELS[r], mState[r][c]));
                }
            }
        }
        return pieces;
    }

    private int isValidMoveForPawn(int srcX, int srcY, int trgX, int trgY) {
        int vecX = trgX - srcX;
        int vecY = trgY - srcY;
        int absX = Math.abs(vecX);
        int absY = Math.abs(vecY);
        char color = getColor(srcX, srcY);
        int direct = (color == WHITE_COLOR ? -1 : 1);

        if (vecX * direct < 0) {
            // Pawns cannot move backward until promoted
            return INVALID_FLAG;
        }
        if (absX == 1 && absY == 1) {
            // Cross move / Capture enemy
            if (mState[trgX][trgY] == null)
                // No enemy inside
                return INVALID_FLAG;
            if (color == getColor(trgX, trgY))
                // No traitor
                return INVALID_FLAG;
            // OK HOOK IT!
            return CAPTURE_FLAG;
        }
        if (vecY != 0) {
            // Cannot cross move in other cases
            return INVALID_FLAG;
        }
        if (absX == 1 && mState[trgX][trgY] == null) {
            // Move normally
            return NORMAL_FLAG;
        }
        if (absX == 2) {
            // Jump from scratch -> OK
            if (color == WHITE_COLOR && srcX != SIZE-2) {
                // Not from scratch
                return INVALID_FLAG;
            }
            if (color == BLACK_COLOR && srcX != 1) {
                // Not from scratch
                return INVALID_FLAG;
            }
            // Check obstacle
            if (mState[srcX + direct][srcY] != null || mState[trgX][trgY] != null)
                return INVALID_FLAG;
            // OK move it
            return NORMAL_FLAG;
        }
        return INVALID_FLAG;
    }

    private int isValidMoveForRook(int srcX, int srcY, int trgX, int trgY) {
        int absX = Math.abs(trgX - srcX);
        int absY = Math.abs(trgY - srcY);
        if (absX * absY > 0) {
            // is not reachable
            return INVALID_FLAG;
        }
        // Either srcX = trgX or srcY = trgY
        if (absX == 0) {
            int sign = (trgY > srcY ? 1 : -1);
            for(int i=srcY+sign; i!=trgY; i+=sign) {
                if (mState[trgX][i] != null) {
                    return INVALID_FLAG;
                }
            }
        }
        if (absY == 0) {
            int sign = (trgX > srcX ? 1 : -1);
            for(int i=srcX+sign; i!=trgX; i+=sign) {
                if (mState[i][srcY] != null) {
                    return INVALID_FLAG;
                }
            }
        }
        // Check if there is a piece in trg location
        if (mState[trgX][trgY] != null) {
            // No traitor please :)
            if (getColor(srcX, srcY) == getColor(trgX, trgY)) {
                Log.d("TEST", "No traitor?");
                return INVALID_FLAG;
            }
            // OK hook it!
            return CAPTURE_FLAG;
        }
        // No enemy insight
        return NORMAL_FLAG;
    }

    private int isValidMoveForKnight(int srcX, int srcY, int trgX, int trgY) {
        int absX = Math.abs(trgX - srcX);
        int absY = Math.abs(trgY - srcY);
        if (absX * absY != 2) {
            return INVALID_FLAG;
        }
        // Check if there is a piece in trg location
        if (mState[trgX][trgY] != null) {
            // No traitor please :)
            if (getColor(srcX, srcY) == getColor(trgX, trgY)) {
                return INVALID_FLAG;
            }
            // OK hook it!
            return CAPTURE_FLAG;
        }
        // No enemy insight
        return NORMAL_FLAG;
    }

    private int isValidMoveForBishop(int srcX, int srcY, int trgX, int trgY) {
        int absX = Math.abs(trgX - srcX);
        int absY = Math.abs(trgY - srcY);
        if(absX != absY) {
            return INVALID_FLAG;
        }
        int xSign = (trgX > srcX ? 1 : -1);
        int ySign = (trgY > srcY ? 1 : -1);
        for(int x=srcX+xSign, y=srcY+ySign; x!=trgX; x+=xSign, y+=ySign) {
            if(mState[x][y] != null) {
                return INVALID_FLAG;
            }
        }
        // Check if there is a piece in trg location
        if (mState[trgX][trgY] != null) {
            // No traitor please :)
            if (getColor(srcX, srcY) == getColor(trgX, trgY)) {
                return INVALID_FLAG;
            }
            // OK hook it!
            return CAPTURE_FLAG;
        }
        // No enemy insight
        return NORMAL_FLAG;
    }

    private int isValidMoveForQueen(int srcX, int srcY, int trgX, int trgY) {
        int rookValid = isValidMoveForRook(srcX, srcY, trgX, trgY);
        if (rookValid != INVALID_FLAG)
            return rookValid;
        return isValidMoveForBishop(srcX, srcY, trgX, trgY);
    }

    private int isValidMoveForKing(int srcX, int srcY, int trgX, int trgY) {
        int absX = Math.abs(trgX - srcX);
        int absY = Math.abs(trgY - srcY);

        if (absX > 1 || absY > 1)
            return INVALID_FLAG;

        // Check if there is a piece in trg location
        if (mState[trgX][trgY] != null) {
            // No traitor please :)
            if (getColor(srcX, srcY) == getColor(trgX, trgY)) {
                return INVALID_FLAG;
            }
            // OK hook it!
            return CAPTURE_FLAG;
        }
        // No enemy insight
        return NORMAL_FLAG;
    }

    public int isValidMove(String src, String trg) {
        // No suicide here brothers!
        if (src.equals(trg))
            return INVALID_FLAG;
        String srcPiece = getPieceAt(src);
        // No piece at this location, move cancelled
        if (srcPiece == null)
            return INVALID_FLAG;
        // Invalid format type!
        if (srcPiece.length() != 2)
            return INVALID_FLAG;
        char srcColor = srcPiece.charAt(0);
        // This guy is not our ally
        if (srcColor != mAlly)
            return INVALID_FLAG;
        // Now it's time to examine all guys one-by-one
        Pair<Integer, Integer> srcIndex = mIndexTable.get(src);
        Pair<Integer, Integer> trgIndex = mIndexTable.get(trg);
        assert srcIndex != null && trgIndex != null;
        int srcX = srcIndex.first;
        int srcY = srcIndex.second;
        int trgX = trgIndex.first;
        int trgY = trgIndex.second;
        char srcPieceType = srcPiece.charAt(1);
        switch (srcPieceType) {
            case QUEEN:
                return isValidMoveForQueen(srcX, srcY, trgX, trgY);
            case KING:
                return isValidMoveForKing(srcX, srcY, trgX, trgY);
            case PAWN:
                return isValidMoveForPawn(srcX, srcY, trgX, trgY);
            case ROOK:
                return isValidMoveForRook(srcX, srcY, trgX, trgY);
            case KNIGHT:
                return isValidMoveForKnight(srcX, srcY, trgX, trgY);
            case BISHOP:
                return isValidMoveForBishop(srcX, srcY, trgX, trgY);
        }
        // Alien detected !!!
        return INVALID_FLAG;
    }

    public ChessMovement move(String src, String trg, int flag) {
        ChessMovement movement = new ChessMovement();
        movement.setSrcTrigger(src, getPieceAt(src));
        movement.setTrgTrigger(trg, getPieceAt(trg));
        movement.setFlag(flag);
        // Update state table for trigger move
        Pair<Integer, Integer> srcIndex = mIndexTable.get(src);
        Pair<Integer, Integer> trgIndex = mIndexTable.get(trg);
        assert (srcIndex != null) && (trgIndex != null);
        int srcX = srcIndex.first;
        int srcY = srcIndex.second;
        int trgX = trgIndex.first;
        int trgY = trgIndex.second;
        // Move piece logically
        mState[trgX][trgY] = mState[srcX][srcY];
        mState[srcX][srcY] = null;
        // Switch side
        mTurn ^= 1;
        // Used for Over the board :)
        mAlly = COLORS[mTurn];
        // Return move
        return movement;
    }

    public ArrayList<Pair<String, Integer>> getMoveSuggestions(String location) {
        ArrayList<Pair<String, Integer>> marks = new ArrayList<>();
        for(int r=0; r<SIZE; r++) {
            for(int c=0; c<SIZE; c++) {
                int flag = isValidMove(location, COL_LABELS[c] + ROW_LABELS[r]);
                if (flag == INVALID_FLAG)
                    continue;
                if ((flag & CAPTURE_FLAG) > 0)
                    marks.add(new Pair<>(COL_LABELS[c] + ROW_LABELS[r], CAPTURE_MARK_TAG));
                if ((flag & NORMAL_FLAG) > 0)
                    marks.add(new Pair<>(COL_LABELS[c] + ROW_LABELS[r], OCCUPY_MARK_TAG));
            }
        }
        return marks;
    }
}
