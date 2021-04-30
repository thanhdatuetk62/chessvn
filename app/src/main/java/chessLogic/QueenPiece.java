package chessLogic;

public class QueenPiece extends ChessPiece {
    public QueenPiece(char color) {
        mColor = color;
        mPieceName = Constants.QUEEN;
    }

    private boolean canMoveLikeRook(int x1, int y1, int x2, int y2, ChessModel model) {
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
        return true;
    }

    private boolean canMoveLikeBishop(int x1, int y1, int x2, int y2, ChessModel model) {
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
        return true;
    }

    @Override
    public boolean canMove(int x1, int y1, int x2, int y2, ChessModel model) {
        if (!super.canMove(x1, y1, x2, y2, model))
            return false;
        // Check if there is a piece in trg location
        ChessPiece trgPiece = model.getPieceAt(x2, y2);
        if (trgPiece != null) {
            // No traitor please :)
            if (!isEnemy(trgPiece)) {
                return false;
            }
        }
        return (canMoveLikeBishop(x1, y1, x2, y2, model) || canMoveLikeRook(x1, y1, x2, y2, model));
    }
}
