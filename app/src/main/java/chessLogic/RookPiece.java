package chessLogic;

public class RookPiece extends ChessPiece {
    public RookPiece(char color) {
        mColor = color;
        mPieceName = Constants.ROOK;
    }

    @Override
    public boolean canMove(int x1, int y1, int x2, int y2, ChessModel model) {
        if (!super.canMove(x1, y1, x2, y2, model))
            return false;
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
                if (model.getPieceAt(x2, i) != null) {
                    return false;
                }
            }
        }
        if (absY == 0) {
            int sign = (x2 > x1 ? 1 : -1);
            for (int i = x1 + sign; i != x2; i += sign) {
                if (model.getPieceAt(i, y1) != null) {
                    return false;
                }
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
