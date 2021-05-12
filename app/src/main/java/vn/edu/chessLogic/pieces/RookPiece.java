package vn.edu.chessLogic.pieces;

import androidx.core.util.Pair;

import java.util.ArrayList;

import vn.edu.chessLogic.GameState;
import vn.edu.Constants;

public class RookPiece extends ChessPiece {
    public RookPiece(char color) {
        d = 4;
        t = 2;
        p = 0;
        m = 0;
        mColor = color;
        mPieceName = Constants.ROOK;
    }

    @Override
    public boolean canMove(int x1, int y1, int x2, int y2, GameState state) {
        int absX = Math.abs(x2 - x1);
        int absY = Math.abs(y2 - y1);
        if (absX * absY > 0) {
            // is not reachable
            return false;
        }
        // Either x1 = x2 or y1 = y2
        if (absX == 0) {
            int sign = (y2 > y1 ? 1 : -1);
            for (int i = y1 + sign; i != y2; i += sign) {
                if (state.getPieceAt(x2, i) != null) {
                    return false;
                }
            }
        }
        if (absY == 0) {
            int sign = (x2 > x1 ? 1 : -1);
            for (int i = x1 + sign; i != x2; i += sign) {
                if (state.getPieceAt(i, y1) != null) {
                    return false;
                }
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
        int[] dx = {0, 0, -1, 1};
        int[] dy = {1, -1, 0, 0};
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
