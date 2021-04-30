package chessLogic;

public class KnightPiece extends ChessPiece {
    public KnightPiece(char color) {
        mColor = color;
        mPieceName = Constants.KNIGHT;
    }

    @Override
    public boolean canMove(int x1, int y1, int x2, int y2, ChessModel model) {
        if (!super.canMove(x1, y1, x2, y2, model))
            return false;
        int absX = Math.abs(x2 - x1);
        int absY = Math.abs(y2 - y1);
        if (absX * absY != 2) {
            return false;
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
