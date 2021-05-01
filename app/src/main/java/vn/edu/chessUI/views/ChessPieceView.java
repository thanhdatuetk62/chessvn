package vn.edu.chessUI.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;

import androidx.appcompat.widget.AppCompatImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class ChessPieceView extends AppCompatImageView {
    private String mPieceCode;

    public ChessPieceView(Context context) {
        super(context);
    }

    public ChessPieceView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChessPieceView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPieceType(String pieceCode) {
        // Get image URI
        String imageName = getContext().getString(getResources().getIdentifier(
                pieceCode, "string", getContext().getPackageName()));
        // Load image
        setImageResource(getResources().getIdentifier(
                imageName, "drawable", getContext().getPackageName()));
        mPieceCode = pieceCode;
    }

    public String getPieceCode() {
        return mPieceCode;
    }
}