package vn.edu.chessLogic.pieces;

import androidx.core.util.Pair;

import java.util.ArrayList;

import vn.edu.chessLogic.GameState;
import vn.edu.Constants;

public class PawnPiece extends ChessPiece {
    public PawnPiece(char color) {
        d = 1;
        t = 0;
        p = 0;
        m = 0;
        mColor = color;
        mPieceName = Constants.PAWN;
    }

    @Override
    public boolean canMove(int x1, int y1, int x2, int y2, GameState state) {
        // Define only pawn logic
        int vecX = x2 - x1;
        int vecY = y2 - y1;
        int absX = Math.abs(vecX);
        int absY = Math.abs(vecY);
        int direct = (mColor == Constants.WHITE_COLOR ? -1 : 1);
        ChessPiece trgPiece = state.getPieceAt(x2, y2);
        if (vecX * direct < 0) {
            // Pawns cannot move backward until promoted
            return false;
        }
        if (absX == 1 && absY == 1) {
            // Cross move / Capture enemy
            if (trgPiece == null)
                // No enemy inside
                return false;
            if (!isEnemy(trgPiece))
                // Cannot assault ally!
                return false;
            // OK HOOK IT!
            return true;
        }
        if (vecY != 0) {
            // Cannot cross move in other cases
            return false;
        }
        if (absX == 1 && trgPiece == null) {
            // Move normally
            return true;
        }
        if (absX == 2) {
            // Jump from scratch -> OK
            if (mColor == Constants.WHITE_COLOR && x1 != Constants.SIZE - 2) {
                // White color is not from scratch
                return false;
            }
            if (mColor == Constants.BLACK_COLOR && x1 != 1) {
                // Black color is not from scratch
                return false;
            }
            // Check obstacle
            if (state.getPieceAt(x1 + direct, y1) != null || trgPiece != null)
                return false;
            // OK move it
            return true;
        }
        return false;
    }

    @Override
    public ArrayList<Pair<Integer, Integer>> allPossibleMoves(int x, int y, GameState state) {
        ArrayList<Pair<Integer, Integer>> moves = new ArrayList<>();
        // Consider white side
        if (mColor == Constants.WHITE_COLOR) {
            if (x - 1 >= 0 && x - 1 < Constants.SIZE) {
                ChessPiece piece = state.getPieceAt(x - 1, y);
                if (piece == null) {
                    moves.add(new Pair<>(x - 1, y));
                    if (x == Constants.SIZE - 2 && state.getPieceAt(x - 2, y) == null)
                        moves.add(new Pair<>(x - 2, y));
                }
                if (y - 1 >= 0 && y - 1 < Constants.SIZE) {
                    piece = state.getPieceAt(x - 1, y - 1);
                    if (piece != null && isEnemy(piece))
                        moves.add(new Pair<>(x - 1, y - 1));
                }
                if (y + 1 >= 0 && y + 1 < Constants.SIZE) {
                    piece = state.getPieceAt(x - 1, y + 1);
                    if (piece != null && isEnemy(piece))
                        moves.add(new Pair<>(x - 1, y + 1));
                }
            }
        }
        // Consider black side
        if (mColor == Constants.BLACK_COLOR) {
            if (x + 1 >= 0 && x + 1 < Constants.SIZE) {
                ChessPiece piece = state.getPieceAt(x + 1, y);
                if (piece == null) {
                    moves.add(new Pair<>(x + 1, y));
                    if (x == 1 && state.getPieceAt(x + 2, y) == null)
                        moves.add(new Pair<>(x + 2, y));
                }
                if (y - 1 >= 0 && y - 1 < Constants.SIZE) {
                    piece = state.getPieceAt(x + 1, y - 1);
                    if (piece != null && isEnemy(piece))
                        moves.add(new Pair<>(x + 1, y - 1));
                }
                if (y + 1 >= 0 && y + 1 < Constants.SIZE) {
                    piece = state.getPieceAt(x + 1, y + 1);
                    if (piece != null && isEnemy(piece))
                        moves.add(new Pair<>(x + 1, y + 1));
                }
            }
        }
        return moves;
    }
}
