package vn.edu.chessUI.viewmodels;

import vn.edu.Constants;

public class ChessControl {
    private boolean mOptionHL;
    private boolean mRotateHL;
    private int userColor;
    private int userTurn;
    private boolean isLoading;
    private OneNightEvent<Integer> mGameStatus;

    public ChessControl() {
        mOptionHL = false;
        mRotateHL = false;
        isLoading = false;
        userColor = -1;
        userTurn = -1;
    }

    public boolean isOptionHL() {
        return mOptionHL;
    }

    public boolean isRotateHL() {
        return mRotateHL;
    }

    public void setOptionHL(boolean o) {
        mOptionHL = o;
    }

    public void setRotateHL(boolean o) {
        mRotateHL = o;
    }

    public void rotateHL() {
        mRotateHL = !mRotateHL;
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

    public void setUserColor(int color) {
        userColor = color;
    }

    public void setUserTurn(int turn) {
        userTurn = turn;
    }

    public int getUserColor() {
        return userColor;
    }

    public int getUserTurn() {
        return userTurn;
    }

    public void setLoading(boolean o) {
        isLoading = o;
    }

    public boolean isLoading() {
        return isLoading;
    }
}
