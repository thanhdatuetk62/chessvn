package vn.edu.chessLogic.pieces;

import android.util.Log;

import androidx.core.util.Pair;

import java.util.ArrayList;

import vn.edu.chessLogic.GameState;
import vn.edu.Constants;

public class QueenPiece extends ChessPiece {
    public QueenPiece(char color) {
        d = 8;
        t = 5;
        p = 0;
        m = 1;
        mColor = color;
        mPieceName = Constants.QUEEN;
    }

    @Override
    public boolean canMove(int x1, int y1, int x2, int y2, GameState state) {
        // Pseudo rook move and bishop move
        boolean canMoveLikeBishop = (new BishopPiece(mColor)).canMove(x1, y1, x2, y2, state);
        boolean canMoveLikeRook = (new RookPiece(mColor)).canMove(x1, y1, x2, y2, state);
        return canMoveLikeBishop || canMoveLikeRook;
    }

    @Override
    public ArrayList<Pair<Integer, Integer>> allPossibleMoves(int x, int y, GameState state) {
        ArrayList<Pair<Integer, Integer>> moves = new ArrayList<>();
        int[] dx = {-1, 0, 1, 0, 1, 1, -1, -1};
        int[] dy = {0, 1, 0, -1, 1, -1, 1, -1};
        // X-ray moves
        for (int i = 0; i < 8; i++) {
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
