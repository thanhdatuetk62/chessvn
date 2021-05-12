package vn.edu.chessLogic.pieces;

import androidx.core.util.Pair;

import java.util.ArrayList;

import vn.edu.chessLogic.GameState;
import vn.edu.Constants;

public class BishopPiece extends ChessPiece {
    public BishopPiece(char color) {
        d = 2;
        t = 1;
        p = 1;
        m = 0;
        mColor = color;
        mPieceName = Constants.BISHOP;
    }

    @Override
    public boolean canMove(int x1, int y1, int x2, int y2, GameState state) {
        int absX = Math.abs(x2 - x1);
        int absY = Math.abs(y2 - y1);
        if (absX != absY) {
            // Must form a perfect cross
            return false;
        }
        int xSign = (x2 > x1 ? 1 : -1);
        int ySign = (y2 > y1 ? 1 : -1);
        for (int x = x1 + xSign, y = y1 + ySign; x != x2; x += xSign, y += ySign) {
            if (state.getPieceAt(x, y) != null) {
                return false;
            }
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
        int[] dx = {1, 1, -1, -1};
        int[] dy = {1, -1, 1, -1};
        // X-ray moves
        for (int i = 0; i < 4; i++) {
            // Check move for each ray
            int nx = x + dx[i];
            int ny = y + dy[i];

            // X-ray testing
            for (; nx >= 0 & nx < Constants.SIZE && ny >= 0 && ny < Constants.SIZE; nx += dx[i], ny += dy[i]) {
                ChessPiece piece = state.getPieceAt(nx, ny);
                if (piece != null) {
                    // Has obstacle
                    if (isEnemy(piece)) {
                        // Enemy, allow capture here (not checkmate detection yet)
                        moves.add(new Pair<>(nx, ny));
                    }
                    break;
                }
                // Add this empty cell and continue testing X-ray
                moves.add(new Pair<>(nx, ny));
            }
        }
        return moves;
    }
}
