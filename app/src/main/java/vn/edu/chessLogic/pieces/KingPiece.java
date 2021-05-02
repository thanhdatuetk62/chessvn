package vn.edu.chessLogic.pieces;

import androidx.core.util.Pair;

import java.util.ArrayList;

import vn.edu.chessLogic.Coordination;
import vn.edu.chessLogic.GameState;
import vn.edu.Constants;

public class KingPiece extends ChessPiece {
    public KingPiece(char color) {
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

    public boolean isThreaten(int x, int y, GameState state) {
        ArrayList<Coordination> enemies = state.getPieceLocations(getOpponentColor());
        for (Coordination coo : enemies) {
            Pair<Integer, Integer> p = coo.getCoordination();
            ChessPiece enemy = state.getPieceAt(coo);
            if (enemy.canMove(p.first, p.second, x, y, state))
                return true;
        }
        return false;
    }
}
