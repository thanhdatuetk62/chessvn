package vn.edu.chessLogic.pieces;

import androidx.core.util.Pair;

import java.util.ArrayList;

import vn.edu.chessLogic.GameState;
import vn.edu.Constants;

public class KnightPiece extends ChessPiece {
    public KnightPiece(char color) {
        d = 2;
        t = 1;
        p = 1;
        m = 0;
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

    @Override
    public ArrayList<Pair<Integer, Integer>> allPossibleMoves(int x, int y, GameState state) {
        ArrayList<Pair<Integer, Integer>> moves = new ArrayList<>();
        int[] dx = {1, 1, 2, 2, -1, -1, -2, -2};
        int[] dy = {2, -2, 1, -1, 2, -2, 1, -1};

        for (int i = 0; i < 8; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx >= 0 && nx < Constants.SIZE && ny >= 0 && ny < Constants.SIZE) {
                ChessPiece piece = state.getPieceAt(nx, ny);
                if (piece == null || isEnemy(piece))
                    moves.add(new Pair<>(nx, ny));
            }
        }
        return moves;
    }
}
