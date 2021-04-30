package vn.edu.chessUI;

import android.util.Pair;

import java.util.ArrayList;

public class ChessViewModelEvent {
    private boolean mLock;
    private int mPerspective;
    private OneNightEvent<ArrayList<Pair<String, String>>> mMove;
    private ArrayList<Pair<String, Integer>> mMarks;

    public ChessViewModelEvent (int perspective) {
        mLock = true;
        mPerspective = perspective;
        mMarks = new ArrayList<>();
    }

    public void setLock(boolean lock) {
        mLock = lock;
    }

    public boolean getLock() {
        return mLock;
    }

    public void setPerspective(int perspective) {
        mPerspective = perspective;
    }

    public int getPerspective() {
        return mPerspective;
    }

    public void setMove(ArrayList<Pair<String, String>> move) {
        mMove = new OneNightEvent<>(move);
    }

    public ArrayList<Pair<String, String>> getMove() {
        if (mMove == null)
            return null;
        return mMove.get();
    }

    public void setMarks(ArrayList<Pair<String, Integer>> marks) {
        mMarks = marks;
    }

    public ArrayList<Pair<String, Integer>> getMarks() {
        return mMarks;
    }
}
