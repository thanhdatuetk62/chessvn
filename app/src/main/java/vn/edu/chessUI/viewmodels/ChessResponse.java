package vn.edu.chessUI.viewmodels;

import androidx.core.util.Pair;

import java.util.ArrayList;

import chessLogic.ChessMovement;
import chessLogic.Coordination;

public class ChessResponse {
    private OneNightEvent<ArrayList<Pair<String, Coordination>>> mPieceLocations;
    private OneNightEvent<ChessMovement> mMovements;
    private ArrayList<Pair<Integer, Coordination>> mMarks;
    private boolean mPromote;

    public ChessResponse() {
        mPromote = false;
    }

    public void setPieceLocations(ArrayList<Pair<String, Coordination>> locations) {
        mPieceLocations = new OneNightEvent<>(locations);
    }

    public void setMovements(ChessMovement movements) {
        mMovements = new OneNightEvent<>(movements);
    }

    public void setMarks(ArrayList<Pair<Integer, Coordination>> marks) {
        mMarks = marks;
    }

    public void setPromote(boolean o) {
        mPromote = o;
    }

    public ChessMovement getMovements() {
        if (mMovements != null)
            return mMovements.get();
        return null;
    }

    public ArrayList<Pair<String, Coordination>> getPieceLocations() {
        if (mPieceLocations != null)
            return mPieceLocations.get();
        return null;
    }

    public ArrayList<Pair<Integer, Coordination>> getMarks() {
        return mMarks;
    }
}
