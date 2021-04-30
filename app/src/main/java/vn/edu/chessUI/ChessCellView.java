package vn.edu.chessUI;

import android.content.Context;
import android.util.AttributeSet;
import androidx.core.util.Pair;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class ChessCellView extends FrameLayout {
    private Pair<Integer, Integer> mLocation;
    public ChessCellView(Context context) {
        super(context);
    }

    public ChessCellView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChessCellView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setLocation(int x, int y) {
        mLocation = new Pair<>(x, y);
    }

    public Pair<Integer, Integer> getLocation() {
        return mLocation;
    };
}