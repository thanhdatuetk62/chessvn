package vn.edu.uetchess;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

public class ChessLayout extends FrameLayout {

    public ChessLayout(Context context) {
        super(context);
    }

    public ChessLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChessLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int desiredSize = Math.min(widthSize, heightSize);
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(desiredSize, widthMode),
                MeasureSpec.makeMeasureSpec(desiredSize, heightMode));
    }
}
