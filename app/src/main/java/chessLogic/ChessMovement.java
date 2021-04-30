package chessLogic;

import androidx.core.util.Pair;

import java.util.ArrayList;

public class ChessMovement {
    // This class contains info about a single move in chess board.
    // It can be only created by the ChessModel
    private PairCells mActiveMove;
    private final ArrayList<PairCells> mCausalMoves;
    private String promotePiece;
    private boolean hasPromote;

    public ChessMovement() {
        mCausalMoves = new ArrayList<>();
    }

    public void setActive(int x1, int y1, int x2, int y2) {
        Coordination src = new Coordination(x1, y1);
        Coordination trg = new Coordination(x2, y2);
        mActiveMove = new PairCells(src, trg);
    }

    public void addMove(int x1, int y1, int x2, int y2) {
        Coordination src = new Coordination(x1, y1);
        Coordination trg = new Coordination(x2, y2);
        mCausalMoves.add(new PairCells(src, trg));
    }

    public PairCells getActive() {return mActiveMove;}

    public ArrayList<PairCells> getAllMoves() {
        return mCausalMoves;
    }

    public void setPromotion(String pieceCode) {
        promotePiece = pieceCode;
    }

    public String getPromotion() {
        return promotePiece;
    }
}
