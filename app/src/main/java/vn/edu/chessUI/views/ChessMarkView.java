package vn.edu.chessUI.views;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import vn.edu.chessUI.R;

import vn.edu.Constants;

public class ChessMarkView extends AppCompatImageView {

    public ChessMarkView(Context context) {
        super(context);
    }

    public ChessMarkView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChessMarkView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMarkType(int type) {
        int resID = -1;
        switch (type) {
            case Constants.SELECT_MARK_TAG:
                resID = R.drawable.mark_select;
                break;
            case Constants.HISTORY_MARK_TAG:
                resID = R.drawable.mark_history;
                break;
            case Constants.CAPTURE_MARK_TAG:
                resID = R.drawable.mark_capture;
                break;
            case Constants.OCCUPY_MARK_TAG:
                resID = R.drawable.mark_occupy;
                break;
            case Constants.PROMOTE_MARK_TAG:
                resID = R.drawable.mark_promote;
                break;
            case Constants.CHECK_MARK_TAG:
                resID = R.drawable.mark_check;
                break;
        }
        setImageResource(resID);
    }
}