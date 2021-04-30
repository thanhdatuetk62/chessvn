package vn.edu.chessUI.viewmodels;

public class OneNightEvent<T> {
    private boolean received = false;
    private final T mContent;

    public OneNightEvent (T content) {
        mContent = content;
    }

    public T get() {
        if (!received) {
            received = true;
            return mContent;
        }
        return null;
    }
}
