package chessLogic.pieces;

import androidx.core.util.Pair;

import java.util.ArrayList;

import chessLogic.ChessModel;
import chessLogic.Constants;
import chessLogic.Coordination;
import chessLogic.GameState;

public class KingPiece extends ChessPiece {
    public KingPiece(char color) {
        mColor = color;
        mPieceName = Constants.KING;
    }

    @Override
    public boolean canMove(int x1, int y1, int x2, int y2, GameState state) {
        int absX = Math.abs(x2 - x1);
        int absY = Math.abs(y2 - y1);

        if (absX > 1 || absY > 1)
            return false;

        // Check if there is a piece in trg location
        ChessPiece trgPiece = state.getPieceAt(x2, y2);
        if (trgPiece != null) {
            // No traitor please :)
            // TODO: Need to check Castling situation
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
