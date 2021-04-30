package chessLogic;

public class ChessPiece {
    protected char mColor;
    protected char mPieceName;
    public String pieceCode() {
        return "" + mColor + mPieceName;
    }
    public boolean isEnemy(ChessPiece other) {return mColor != other.mColor;}
    public boolean canMove(int x1, int y1, int x2, int y2, ChessModel model) {
        if (x1 == x2 && y1 == y2) {
            // Cannot suicide
            return false;
        }
        if (model.getPieceAt(x1, y1) == null) {
            // Cannot move a non-existence piece
            return false;
        }
        if (mColor != model.curColor) {
            // Cannot move opponent pieces
            return false;
        }
        // Generally true for other case
        return true;
    }
}
