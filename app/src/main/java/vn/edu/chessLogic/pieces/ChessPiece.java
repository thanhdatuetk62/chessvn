package vn.edu.chessLogic.pieces;

import vn.edu.chessLogic.GameState;
import vn.edu.Constants;

public abstract class ChessPiece {
    protected char mColor;
    protected char mPieceName;

    public String pieceCode() {
        return "" + mColor + mPieceName;
    }

    public char getOpponentColor() {
        return (mColor == Constants.WHITE_COLOR ? Constants.BLACK_COLOR : Constants.WHITE_COLOR);
    }

    public boolean isEnemy(ChessPiece other) {
        return mColor != other.mColor;
    }

    public char getColor() {
        return mColor;
    }

    public abstract boolean canMove(int x1, int y1, int x2, int y2, GameState state);
}
