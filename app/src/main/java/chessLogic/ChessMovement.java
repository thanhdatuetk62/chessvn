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

    public void setActive(Coordination src, Coordination trg) {
        mActiveMove = new PairCells(src, trg);
    }

    public PairCells getActive() {return mActiveMove;}

    public ArrayList<PairCells> getCausal() {return mCausalMoves;}

    public ArrayList<PairCells> getAllMoves() {
        ArrayList<PairCells> all = new ArrayList<>(mCausalMoves);
        all.add(mActiveMove);
        return all;
    }

    public void setPromotion(String pieceCode) {
        promotePiece = pieceCode;
    }

    public String getPromotion() {
        return promotePiece;
    }
}
