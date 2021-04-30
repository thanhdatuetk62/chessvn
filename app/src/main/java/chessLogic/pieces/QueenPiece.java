package chessLogic.pieces;

import chessLogic.ChessModel;
import chessLogic.Constants;
import chessLogic.GameState;

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
