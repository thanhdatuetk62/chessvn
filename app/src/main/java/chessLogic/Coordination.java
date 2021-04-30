package chessLogic;

import androidx.core.util.Pair;

public class Coordination {
    protected final int mX;
    protected final int mY;

    public Coordination(int x, int y) {
        mX = x;
        mY = y;
    }

    private int transform(int a, char color) {
        if (color == Constants.WHITE_COLOR)
            return a;
        else
            return Constants.SIZE - 1 - a;
    }

    public Pair<Integer, Integer> getTrueCoordination(char color) {
        return new Pair<>(transform(mX, color), transform(mY, color));
    }

    public Pair<Integer, Integer> getCoordination() {
        return new Pair<>(mX, mY);
    }
}
