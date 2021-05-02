package vn.edu.chessLogic.pieces;

import vn.edu.chessLogic.GameState;
import vn.edu.Constants;

public class QueenPiece extends ChessPiece {
    public QueenPiece(char color) {
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
}
