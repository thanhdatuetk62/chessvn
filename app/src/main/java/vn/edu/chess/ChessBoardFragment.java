package vn.edu.chess;

import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChessBoardFragment extends Fragment {
    private static final int SIZE = 8;
    private static final int PIECE_TAG = 0;
    private static final int SELECT_MARK_TAG = 1;
    private static final int HISTORY_MARK_TAG = 2;
    private static final int CAPTURE_MARK_TAG = 3;
    private static final int OCCUPY_MARK_TAG = 4;
    private static final int PROMOTE_MARK_TAG = 5;
    private static final int WHITE_PERSPECTIVE = 0;
    private static final int BLACK_PERSPECTIVE = 1;
    private static final String[] COL_LABELS = {"a", "b", "c", "d", "e", "f", "g", "h"};
    private static final String[] ROW_LABELS = {"8", "7", "6", "5", "4", "3", "2", "1"};

    // Lookup tables (include reverse direction)
    private int mPerspective = WHITE_PERSPECTIVE;
    private String[][] mLabelTable;
    private Map<String, Pair<Integer, Integer>> mIndexTable;

    private final ViewGroup[][] mCells = new ViewGroup[SIZE][SIZE];
    private final ArrayList<View> mMarks = new ArrayList<>();
    private ViewGroup mBoard;
    private ChessBoardViewModel model;

    public ChessBoardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chess_board, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBoard = (ViewGroup) view;
        // Get the view model
        model = new ViewModelProvider(requireActivity()).get(ChessBoardViewModel.class);
        // Get reference and assign click listeners
        for(int r=0; r<SIZE; r++) {
            ViewGroup rowBoard = (ViewGroup) mBoard.getChildAt(r);
            for(int c=0; c<SIZE; c++) {
                ViewGroup cellBoard = (ViewGroup) rowBoard.getChildAt(c);
                cellBoard.setClickable(true);
                cellBoard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View cell) {
                        String location = getLocation(cell);
                        model.onSelectLocation(location);
                    }
                });
                // Link to views which are specified in  XML file
                mCells[r][c] = cellBoard;
            }
        }
        // Default white perspective
        annotateLabels(mPerspective);
        // Get game state
        for(Pair<String, String> pair : model.getGameState()) {
            placePiece(pair.second, pair.first);
        }
        // Create observer
        model.getEvent().observe(getViewLifecycleOwner(), event -> {
            // 1. Handling perspective
            changePerspective(event.getPerspective());
            // 2. Handling move
            ArrayList<Pair<String, String>> moves = event.getMove();
            if (moves != null) {
                for(Pair<String, String> pair : moves) {
                    move(pair.first, pair.second);
                }
            }
            // 3. Handling marks
            setMarks(event.getMarks());
            // 4. Handling lock
//            setLock(event.getLock());
        });
    }

    private View getByTag(int x, int y, int tag) {
        ViewGroup cell = mCells[x][y];
        for(int i=0; i<cell.getChildCount(); i++) {
            View view = cell.getChildAt(i);
            if((view.getTag() instanceof Integer) && ((Integer) view.getTag() == tag))
                return view;
        }
        return null;
    }

    private int countByTag(int x, int y, int tag) {
        int result = 0;
        ViewGroup cell = mCells[x][y];
        for(int i=0; i<cell.getChildCount(); i++) {
            View view = cell.getChildAt(i);
            if((view.getTag() instanceof Integer) && ((Integer) view.getTag() == tag))
                result++;
        }
        return result;
    }

    private float getX(View view) {
        if (view.getParent() == mBoard)
            return view.getX();
        return view.getX() + getX((View) view.getParent());
    }

    private float getY(View view) {
        if (view.getParent() == mBoard)
            return view.getY();
        return view.getY() + getY((View) view.getParent());
    }

    private String getLocation(View cell) {
        ViewGroup row = (ViewGroup) cell.getParent();
        ViewGroup board = (ViewGroup) row.getParent();
        int y = row.indexOfChild(cell);
        int x = board.indexOfChild(row);
        return mLabelTable[x][y];
    }

    private View placePiece(String pieceCode, int x, int y) {
        // Check if this cell is valid
        if (countByTag(x, y, PIECE_TAG) > 0) {
            String message = String.format(Locale.ENGLISH,
                    "Cannot placePiece because the cell at (%d, %d) had already been occupied!", x, y);
            Log.d("TEST", message);
            return null;
        }

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        ImageView piece = new ImageView(activity);

        // Get actual image name from pieceCode
        String imageName = getString(getResources().getIdentifier(
                pieceCode, "string", activity.getPackageName()));

        // Load image into imageView piece
        piece.setImageResource(getResources().getIdentifier(
                imageName, "drawable", activity.getPackageName()));

        // Add exclusive tag for chess piece
        piece.setTag(PIECE_TAG);

        // Place image into frame at specified location
        ViewGroup cell = mCells[x][y];
        cell.addView(piece);

        return piece;
    }

    private void removePiece(int x, int y) {
        View piece = getByTag(x, y, PIECE_TAG);
        if (piece == null) {
            String message = String.format(Locale.ENGLISH,
                    "Cannot removePiece because the cell at (%d, %d) is empty!", x, y);
            Log.d("TEST", message);
            return;
        }
        mCells[x][y].removeView(piece);
    }

    private void move(int srcX, int srcY, int trgX, int trgY) {
        ViewGroup srcCell = mCells[srcX][srcY];
        ViewGroup trgCell = mCells[trgX][trgY];
        View srcPiece = getByTag(srcX, srcY, PIECE_TAG);
        View trgPiece = getByTag(trgX, trgY, PIECE_TAG);

        // Check if there is a piece inside the source cell
        if (srcPiece == null) {
            Log.d("TEST", "[move] The source cell did not contain any piece!");
            return;
        }

        // Calculate vector translation between Ending and Starting position
        float vecX = getX(trgCell) - getX(srcCell);
        float vecY = getY(trgCell) - getY(srcCell);

        // Move piece to the overlay view for animation purpose
        mBoard.getOverlay().add(srcPiece);
        srcPiece.animate()
                .translationX(vecX)
                .translationY(vecY)
                .setDuration(256)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // Remove source piece from overlay view
                        mBoard.getOverlay().remove(srcPiece);

                        // Truly put source piece into target cell
                        trgCell.addView(srcPiece);
                        srcPiece.setTranslationX(0);
                        srcPiece.setTranslationY(0);

                        // Check if it is a capture move
                        if (trgPiece != null) {
                            trgCell.removeView(trgPiece);
                        }
                    }
                });
    }

    private boolean hasTag(View view, int tag) {
        if (view.getTag() instanceof Integer) {
            return (Integer) view.getTag() == tag;
        }
        return false;
    }

    private View hasTag(int x, int y, int tag) {
        ViewGroup cell = mCells[x][y];
        for(int i=0; i<cell.getChildCount(); i++) {
            if (hasTag(cell, tag))
                return cell;
        }
        return null;
    }

    private View addMark(int x, int y, int type) {
//        if (!isAdded() || getActivity() == null) {
//            return null;
//        }
        ImageView mark = new ImageView(requireActivity());
        mark.setTag(type);
        int resID = -1;
        switch (type) {
            case SELECT_MARK_TAG:
                resID = R.drawable.mark_select;
                break;
            case HISTORY_MARK_TAG:
                resID = R.drawable.mark_history;
                break;
            case CAPTURE_MARK_TAG:
                resID = R.drawable.mark_capture;
                break;
            case OCCUPY_MARK_TAG:
                resID = R.drawable.mark_occupy;
                break;
            case PROMOTE_MARK_TAG:
                resID = R.drawable.mark_promote;
                break;
            default:
                return null;
        }
        mark.setImageResource(resID);
        int id = -1;
        // Add right behind piece if exist
        ViewGroup cell = mCells[x][y];
        cell.addView(mark, 0);

        return mark;
    }

    public void placePiece(String pieceCode, String labelLocation) {
        if (pieceCode == null)
            return;
        // Convert label to index
        Pair<Integer, Integer> indexLocation = mIndexTable.get(labelLocation);
        assert indexLocation != null;
        int x = indexLocation.first;
        int y = indexLocation.second;

        View piece = placePiece(pieceCode, x, y);
    }

    public void removePiece(String labelLocation) {
        // Convert label to index
        Pair<Integer, Integer> indexLocation = mIndexTable.get(labelLocation);
        assert indexLocation != null;
        int x = indexLocation.first;
        int y = indexLocation.second;

        removePiece(x, y);
    }

    public void move(String srcLabelLocation, String trgLabelLocation) {

        // Convert label to index
        Pair<Integer, Integer> srcIndexLocation = mIndexTable.get(srcLabelLocation);
        assert srcIndexLocation != null;
        int srcX = srcIndexLocation.first;
        int srcY = srcIndexLocation.second;

        Pair<Integer, Integer> trgIndexLocation = mIndexTable.get(trgLabelLocation);
        assert trgIndexLocation != null;
        int trgX = trgIndexLocation.first;
        int trgY = trgIndexLocation.second;

        move(srcX, srcY, trgX, trgY);
    }

    public View addMark(String labelLocation, int tag) {
        // Convert label to index
        Pair<Integer, Integer> indexLocation = mIndexTable.get(labelLocation);
        assert indexLocation != null;
        int x = indexLocation.first;
        int y = indexLocation.second;

        return addMark(x, y, tag);
    }

    public void setMarks(ArrayList<Pair<String, Integer>> marks) {
        // Clear all previous marks
        for(View mark : mMarks) {
            ViewGroup cell = (ViewGroup) mark.getParent();
            cell.removeView(mark);
        }
        mMarks.clear();
        // Set new marks from argument
        for(Pair<String, Integer> pair : marks) {

            View mark = addMark(pair.first, pair.second);
            if (mark != null)
                mMarks.add(mark);
        }
    }
    // OK UNIT TEST
    public void annotateLabels(int perspective) {
        // Initial member vars based on SDK version (for optimizing)
        mPerspective = perspective;
        mLabelTable = new String[SIZE][SIZE];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mIndexTable = new ArrayMap<>();
        } else {
            mIndexTable = new HashMap<>();
        }
        // Assign labels for each board's cells
        for(int r=0; r<SIZE; r++) {
            for(int c=0; c<SIZE; c++) {
                String label;
                if (perspective == WHITE_PERSPECTIVE) {
                    label = COL_LABELS[c] + ROW_LABELS[r];
                } else {
                    label = COL_LABELS[SIZE-1-c] + ROW_LABELS[SIZE-1-r];
                }
                mLabelTable[r][c] = label;
                mIndexTable.put(label, new Pair<>(r, c));
            }
        }
    }

    public void rotatePerspective() {
        model.rotatePerspective();
    }

    // OK UNIT TEST
    public void changePerspective(int perspective) {
        if (mPerspective == perspective) {
            // No change
            return;
        }
        // Change perspective
        mPerspective = perspective;
        annotateLabels(mPerspective);

        // Exchange children views between board's cell
        for(int r=0; r<SIZE/2; r++) {
            for(int c=0; c<SIZE; c++) {
                ViewGroup cell1 = mCells[r][c];
                ViewGroup cell2 = mCells[SIZE-1-r][SIZE-1-c];
                ArrayList<View> children1 = new ArrayList<>();
                ArrayList<View> children2 = new ArrayList<>();
                // Store children of both cells into arrays
                for(int i=0; i<cell1.getChildCount(); i++) {
                    children1.add(cell1.getChildAt(i));
                }
                for(int i=0; i<cell2.getChildCount(); i++) {
                    children2.add(cell2.getChildAt(i));
                }
                cell1.removeAllViews();
                cell2.removeAllViews();
                // Exchange children between cell1 and cell2
                for(View v : children1)
                    cell2.addView(v);
                for(View v : children2)
                    cell1.addView(v);
            }
        }
    }

    public void setLock(boolean lock) {
        // Used to disable/enable player's taps
        // UNTESTED
        for(int r=0; r<SIZE; r++) {
            for(int c=0; c<SIZE; c++) {
                ViewGroup cell = mCells[r][c];
                for(int i=0; i<cell.getChildCount(); i++) {
                    // Do nothing
                }
            }
        }
    }
}