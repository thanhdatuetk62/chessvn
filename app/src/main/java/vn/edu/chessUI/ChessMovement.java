package vn.edu.chessUI;

import android.util.Pair;

public class ChessMovement {
    private boolean isForward = true;
    private int mFlag;
    private Pair<String, String> mSrcTrigger;
    private Pair<String, String> mTrgTrigger;
    private Pair<String, String> mSrcCausal;
    private Pair<String, String> mTrgCausal;
    private Pair<String, String> mRemove;
    private Pair<String, String> mAdd;

    public void setFlag(int flag) {
        mFlag = flag;
    }

    public void setSrcTrigger(String location, String piece) {
        mSrcTrigger = new Pair<>(location, piece);
    }

    public void setTrgTrigger(String location, String piece) {
        mTrgTrigger = new Pair<>(location, piece);
    }

    public void setSrcCausal(String location, String piece) {
        mSrcCausal = new Pair<>(location, piece);
    }

    public void setTrgCausal(String location, String piece) {
        mTrgCausal = new Pair<>(location, piece);
    }

    public void setRemove(String location, String piece) {
        mRemove = new Pair<>(location, piece);
    }

    public void setAdd(String location, String piece) {
        mAdd = new Pair<>(location, piece);
    }

    public int getFlag() {
        return mFlag;
    }

    public Pair<String, String> getSrcTrigger() {
        return mSrcTrigger;
    }

    public Pair<String, String> getTrgTrigger() {
        return mTrgTrigger;
    }

    public Pair<String, String> getSrcCausal() {
        return mSrcCausal;
    }

    public Pair<String, String> getTrgCausal() {
        return mTrgCausal;
    }

    public Pair<String, String> getRemove() {
        return mRemove;
    }

    public Pair<String, String> getAdd() {
        return mAdd;
    }

    public Pair<String, String> getTriggerMove() {
        return new Pair<>(mSrcTrigger.first, mTrgTrigger.first);
    }

    public Pair<String, String> getCausalMove() {
        if (mSrcCausal == null || mTrgCausal == null)
            return null;
        return new Pair<>(mSrcCausal.first, mTrgCausal.first);
    }

}
