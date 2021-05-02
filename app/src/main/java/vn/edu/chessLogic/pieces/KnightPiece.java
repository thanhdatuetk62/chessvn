package vn.edu.chessLogic.pieces;

import vn.edu.chessLogic.GameState;
import vn.edu.Constants;

public class KnightPiece extends ChessPiece {
    public KnightPiece(char color) {
        mColor = color;
        mPieceName = Constants.KNIGHT;
    }

    @Override
    public boolean canMove(int x1, int y1, int x2, int y2, GameState state) {
        int absX = Math.abs(x2 - x1);
        int absY = Math.abs(y2 - y1);
        if (absX * absY != 2) {
            return false;
        }
        // Check if there is a piece in trg location
        ChessPiece trgPiece = state.getPieceAt(x2, y2);
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
}
