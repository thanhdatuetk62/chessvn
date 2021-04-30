package chessLogic;

import androidx.core.util.Pair;

public class Coordination {
    private final int mX;
    private final int mY;

    public Coordination(int x, int y) {
        mX = x;
        mY = y;
    }

    private int transform(int a, int perspective) {
        if (perspective == Constants.WHITE_PERSPECTIVE)
            return a;
        else
            return Constants.SIZE - 1 - a;
    }

    public Pair<Integer, Integer> getTrueCoordination(int perspective) {
        return new Pair<>(transform(mX, perspective), transform(mY, perspective));
    }

    public Pair<Integer, Integer> getCoordination() {
        return new Pair<>(mX, mY);
    }
}
