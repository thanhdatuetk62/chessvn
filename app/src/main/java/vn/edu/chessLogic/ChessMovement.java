package vn.edu.chessLogic;

import androidx.core.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;

import vn.edu.chessLogic.pieces.ChessPiece;

public class ChessMovement implements Serializable {
    // This class contains info about a single move in chess board.
    // It should be only created by the ChessLogic
    private boolean onAir;
    private PairCells mActiveMove;
    private ArrayList<PairCells> mCausalMoves;
    private ChessPiece mCapturedPiece;
    private Coordination mCapturedCoordination;
    private Coordination mPromoteLocation;
    private String mPromotePiece;
    private char mPromoteSide;

    public ChessMovement() {
        onAir = false;
        mCausalMoves = new ArrayList<>();
    }

    public ChessMovement(ChessMovement other) {
        onAir = other.onAir;
        mCausalMoves = new ArrayList<>(other.mCausalMoves);

        if (other.mActiveMove != null)
            mActiveMove = new PairCells(other.mActiveMove.src, other.mActiveMove.trg);

        if (other.mCapturedPiece != null)
            mCapturedPiece = other.mCapturedPiece;

        if (other.mCapturedCoordination != null)
            mCapturedCoordination = other.mCapturedCoordination;

        if (other.mPromoteLocation != null)
            mPromoteLocation = new Coordination(other.mPromoteLocation.mX, other.mPromoteLocation.mY);

        mPromotePiece = other.mPromotePiece;
        mPromoteSide = other.mPromoteSide;
    }

    public ChessMovement copy() {
        return new ChessMovement(this);
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

    public void revert() {
        mActiveMove = new PairCells(mActiveMove.trg, mActiveMove.src);
        ArrayList<PairCells> causalMoves = new ArrayList<>();
        for (PairCells p : mCausalMoves) {
            causalMoves.add(new PairCells(p.trg, p.src));
        }
        mCausalMoves = causalMoves;
    }

    public PairCells getActive() {
        return mActiveMove;
    }

    public ArrayList<PairCells> getAllMoves() {
        return mCausalMoves;
    }

    public void notifyPromotion(char color, Coordination coo) {
        mPromoteSide = color;
        mPromoteLocation = coo;
    }

    public char getPromoteSide() {
        return mPromoteSide;
    }

    public void setPromotion(String pieceCode) {
        mPromotePiece = pieceCode;
    }

    public void setCapturedPiece(ChessPiece piece, int x, int y) {
        mCapturedPiece = piece;
        mCapturedCoordination = new Coordination(x, y);
    }

    public ChessPiece getCapturedPiece() {
        return mCapturedPiece;
    }

    public Coordination getCapturedCoordination() {return mCapturedCoordination;}

    public String getPromotion() {
        return mPromotePiece;
    }

    public Coordination getPromoteLocation() {
        return mPromoteLocation;
    }

    public void setOnAir() {
        onAir = true;
    }

    public boolean isOnAir() {
        return onAir;
    }
}
