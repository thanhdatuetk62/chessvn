package chessLogic;

public class BishopPiece extends ChessPiece {
    public BishopPiece(char color) {
        mColor = color;
        mPieceName = Constants.BISHOP;
    }

    @Override
    public boolean canMove(int x1, int y1, int x2, int y2, ChessModel model) {
        if (!super.canMove(x1, y1, x2, y2, model))
            return false;
        int absX = Math.abs(x2 - x1);
        int absY = Math.abs(y2 - y1);
        if (absX != absY) {
            // Must form a perfect cross
            return false;
        }
        int xSign = (x2 > x1 ? 1 : -1);
        int ySign = (y2 > y1 ? 1 : -1);
        for (int x = x1 + xSign, y = y1 + ySign; x != x2; x += xSign, y += ySign) {
            if (model.getPieceAt(x, y) != null) {
                return false;
            }
        }
        // Check if there is a piece in trg location
        ChessPiece trgPiece = model.getPieceAt(x2, y2);
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
