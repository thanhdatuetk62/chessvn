package vn.edu.chessUI.viewmodels;

import androidx.core.util.Pair;

import java.util.ArrayList;

import vn.edu.Constants;
import vn.edu.chessLogic.ChessMovement;
import vn.edu.chessLogic.Coordination;

public class ChessResponse {
    private OneNightEvent<ArrayList<Pair<String, Coordination>>> mPieceLocations;
    private OneNightEvent<ChessMovement> mMovements;
    private OneNightEvent<Integer> mGameStatus;
    private ArrayList<Pair<Integer, Coordination>> mMarks;

    public void setPieceLocations(ArrayList<Pair<String, Coordination>> locations) {
        mPieceLocations = new OneNightEvent<>(locations);
    }

    public void setMovements(ChessMovement movements) {
        mMovements = new OneNightEvent<>(movements);
    }

    public void setMarks(ArrayList<Pair<Integer, Coordination>> marks) {
        mMarks = marks;
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

    public void setGameStatus(int status) {
        mGameStatus = new OneNightEvent<>(status);
    }

    public int getGameStatus() {
        if (mGameStatus != null) {
            Integer status = mGameStatus.get();
            return (status == null ? Constants.NOT_FINISH : status);
        }
        return Constants.NOT_FINISH;
    }
}
