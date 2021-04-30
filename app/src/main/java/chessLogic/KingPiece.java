package chessLogic;

public class KingPiece extends ChessPiece {
    public KingPiece(char color) {
        mColor = color;
        mPieceName = Constants.KING;
    }

    @Override
    public boolean canMove(int x1, int y1, int x2, int y2, ChessModel model) {
        if (!super.canMove(x1, y1, x2, y2, model))
            return false;
        int absX = Math.abs(x2 - x1);
        int absY = Math.abs(y2 - y1);

        if (absX > 1 || absY > 1)
            return false;

        // Check if there is a piece in trg location
        ChessPiece trgPiece = model.getPieceAt(x2, y2);
        if (trgPiece != null) {
            // No traitor please :)
            // TODO: Need to check Castling situation
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
