package vn.edu.chessLogic.pieces;

import android.util.Log;

import androidx.core.util.Pair;

import java.util.ArrayList;

import vn.edu.chessLogic.GameState;
import vn.edu.Constants;

public class KingPiece extends ChessPiece {
    public KingPiece(char color) {
        d = 0;
        t = 4;
        p = 0;
        m = 0;
        mColor = color;
        mPieceName = Constants.KING;
    }

    @Override
    public boolean canMove(int x1, int y1, int x2, int y2, GameState state) {
        // Check if there is a piece in trg location
        ChessPiece trgPiece = state.getPieceAt(x2, y2);

        // Castling situation
        if (x1 == x2 && trgPiece instanceof RookPiece && trgPiece.getColor() == mColor) {
            if (y1 - y2 == 3 && state.canCastling(mColor, 0)) {
                // King side castling
                // Check no obstacle in the route
                for (int y = y2 + 1; y < y1; y++) {
                    if (state.getPieceAt(x1, y) != null)
                        return false;
                }
                return true;
            }
            if (y2 - y1 == 4 && state.canCastling(mColor, 1)) {
                // Queen side castling
                // Check no obstacle in the route
                for (int y = y1 + 1; y < y2; y++) {
                    if (state.getPieceAt(x1, y) != null)
                        return false;
                }
                return true;
            }
        }

        // Normal situation
        int absX = Math.abs(x2 - x1);
        int absY = Math.abs(y2 - y1);
        if (absX > 1 || absY > 1)
            return false;

        if (trgPiece != null) {
            // No traitor please :)
            if (!isEnemy(trgPiece)) {
                return false;
            }
            // OK hook it!
            return true;
        }
        // No enemy insight
        return true;
    }

    @Override
    public ArrayList<Pair<Integer, Integer>> allPossibleMoves(int x, int y, GameState state) {
        ArrayList<Pair<Integer, Integer>> moves = new ArrayList<>();
        // Non-slide type :) no x-ray checking
        int[] dx = {1, 1, -1, -1, 0, 0, 1, -1};
        int[] dy = {1, -1, 1, -1, 1, -1, 0, 0};

        // Check all possible moves
        for (int i = 0; i < 8; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx < 0 || nx >= Constants.SIZE || ny < 0 || ny >= Constants.SIZE)
                continue;
            ChessPiece piece = state.getPieceAt(nx, ny);
            if (piece != null) {
                if (isEnemy(piece))
                    moves.add(new Pair<>(nx, ny));
            } else {
                moves.add(new Pair<>(nx, ny));
            }
        }

        // Check castling
        if (state.canCastling(mColor, 0)) {
            // King side
            boolean canKS = true;
            for (int i = 1; i < y; i++) {
                if (state.getPieceAt(x, i) != null) {
                    canKS = false;
                    break;
                }
            }
            if (canKS)
                moves.add(new Pair<>(x, 0));
        }
        if (state.canCastling(mColor, 1)) {
            // Queen side
            boolean canQS = true;
            for (int i = y + 1; i < Constants.SIZE - 1; i++) {
                if (state.getPieceAt(x, i) != null) {
                    canQS = false;
                    break;
                }
            }
            if (canQS)
                moves.add(new Pair<>(x, Constants.SIZE - 1));
        }

        return moves;
    }

    public boolean isThreaten(int x, int y, GameState state) {
        for (Pair<Integer, Integer> p : onHostage(x, y, state)) {
            int px = p.first, py = p.second;
            ChessPiece piece = state.getPieceAt(px, py);
            if (isEnemy(piece))
                return true;
        }
        return false;
    }
}
