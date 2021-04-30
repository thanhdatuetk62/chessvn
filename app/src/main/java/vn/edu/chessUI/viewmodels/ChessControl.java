package vn.edu.chessUI.viewmodels;

public class ChessControl {
    private boolean mOptionHL;
    private boolean mRotateHL;
    public ChessControl() {
        mOptionHL = false;
        mRotateHL = false;
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
    public void setRotateHL(boolean o) { mRotateHL = o; }
    public void rotateHL() {
        mRotateHL = !mRotateHL;
    }
}
