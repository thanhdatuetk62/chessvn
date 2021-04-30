package vn.edu.chessUI.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.util.Log;

import androidx.core.util.Pair;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Locale;

import chessLogic.ChessMovement;
import chessLogic.Coordination;
import chessLogic.PairCells;
import vn.edu.chessUI.viewmodels.ChessResponse;
import vn.edu.chessUI.viewmodels.ChessViewModel;
import vn.edu.chessUI.Constants;
import vn.edu.chessUI.R;

public class ChessBoardFragment extends Fragment {
    private final ChessCellView[][] mCells = new ChessCellView[Constants.SIZE][Constants.SIZE];
    private ViewGroup mBoard;
    private ChessViewModel model;

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
        assert getParentFragment() != null;
        // Share viewModel with ChessGameFragment
        model = new ViewModelProvider(getParentFragment()).get(ChessViewModel.class);
        // Get reference and assign click listeners
        for (int r = 0; r < Constants.SIZE; r++) {
            ViewGroup rowBoard = (ViewGroup) mBoard.getChildAt(r);
            for (int c = 0; c < Constants.SIZE; c++) {
                ChessCellView cellBoard = (ChessCellView) rowBoard.getChildAt(c);
                cellBoard.setLocation(r, c);
                // Link to views which are specified in XML file
                mCells[r][c] = cellBoard;
            }
        }
        setCellInteraction(true);
        // Create observer
        model.getResponse().observe(getViewLifecycleOwner(), this::handleResponse);
    }

    private void setCellInteraction(boolean enabled) {
        for (int r = 0; r < Constants.SIZE; r++) {
            for (int c = 0; c < Constants.SIZE; c++) {
                // Set onClick listener for chessCell
                mCells[r][c].setOnClickListener((enabled ? onSelectCell() : null));
            }
        }
    }

    private View.OnClickListener onSelectCell() {
        return v -> {
            // Cast view to chessCell manually
            Pair<Integer, Integer> location = ((ChessCellView) v).getLocation();
            // Call interaction to the model
            model.selectSquare(location.first, location.second);
        };
    }

    private ChessPieceView getPiece(int x, int y) {
        /*
        Get the very first piece resides on the cell (x, y).
        If cannot find any piece, return null instead.
        */
        ChessCellView cell = mCells[x][y];
        for (int i = 0; i < cell.getChildCount(); i++) {
            View v = cell.getChildAt(i);
            if (v instanceof ChessPieceView) {
                return (ChessPieceView) v;
            }
        }
        return null;
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

    private ChessPieceView placePiece(String pieceCode, int x, int y) {
        ChessPieceView piece = new ChessPieceView(requireActivity());
        // Set piece type
        piece.setPieceType(pieceCode);
        // Place image into frame at specified location
        mCells[x][y].addView(piece);
        return piece;
    }

    private void removePiece(int x, int y) {
        ChessPieceView piece = getPiece(x, y);
        if (piece == null) {
            String message = String.format(Locale.ENGLISH,
                    "Cannot removePiece because the cell at (%d, %d) is empty!", x, y);
            Log.d("TEST", message);
            return;
        }
        mCells[x][y].removeView(piece);
    }

    private ChessMarkView addMark(int x, int y, int type) {
        ChessMarkView mark = new ChessMarkView(requireActivity());
        // Set mark type
        mark.setMarkType(type);
        // Add right behind piece if exist
        ViewGroup cell = mCells[x][y];
        cell.addView(mark, 0);
        return mark;
    }

    private ArrayList<ChessMarkView> getMarks(int x, int y) {
        ArrayList<ChessMarkView> marks = new ArrayList<>();
        ChessCellView cell = mCells[x][y];
        for (int i = 0; i < cell.getChildCount(); i++) {
            View v = cell.getChildAt(i);
            if (v instanceof ChessMarkView) {
                marks.add((ChessMarkView) v);
            }
        }
        return marks;
    }

    private void clearMarks() {
        for (int r = 0; r < Constants.SIZE; r++) {
            for (int c = 0; c < Constants.SIZE; c++) {
                for (ChessMarkView mark : getMarks(r, c)) {
                    mCells[r][c].removeView(mark);
                }
            }
        }
    }

    private void clearPieces() {
        for (int r = 0; r < Constants.SIZE; r++) {
            for (int c = 0; c < Constants.SIZE; c++) {
                ChessPieceView piece = getPiece(r, c);
                while (piece != null) {
                    mCells[r][c].removeView(piece);
                    piece = getPiece(r, c);
                }
            }
        }
    }

    private void setPieces(ArrayList<Pair<String, Coordination>> locations) {
        clearPieces();
        for (Pair<String, Coordination> obj : locations) {
            String pieceCode = obj.first;
            Pair<Integer, Integer> coordination = obj.second.getTrueCoordination(model.getUserColor());
            placePiece(pieceCode, coordination.first, coordination.second);
        }
    }

    private void setMarks(ArrayList<Pair<Integer, Coordination>> marks) {
        clearMarks();
        for (Pair<Integer, Coordination> obj : marks) {
            int typeMark = obj.first;
            Pair<Integer, Integer> coordination = obj.second.getTrueCoordination(model.getUserColor());
            addMark(coordination.first, coordination.second, typeMark);
        }
    }

    private Animator createMoveAnimation(int x1, int y1, int x2, int y2) {
        ChessCellView srcCell = mCells[x1][y1];
        ChessCellView trgCell = mCells[x2][y2];
        ChessPieceView srcPiece = getPiece(x1, y1);
        ChessPieceView trgPiece = getPiece(x2, y2);
        // Check if there is a piece inside the source cell.
        // If cannot find any piece in source cell, cancel operation immediately
        if (srcPiece == null) {
            Log.d("TEST", "[move] The source cell did not contain any piece!");
            return null;
        }
        // Calculate vector translation between Ending and Starting position
        float vecX = getX(trgCell) - getX(srcCell);
        float vecY = getY(trgCell) - getY(srcCell);
        // Move piece to the overlay view for animation purpose
        PropertyValuesHolder translateX = PropertyValuesHolder.ofFloat("translationX", vecX);
        PropertyValuesHolder translateY = PropertyValuesHolder.ofFloat("translationY", vecY);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(srcPiece, translateX, translateY);
        animator.setDuration(128);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mBoard.getOverlay().add(srcPiece);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Remove source piece from overlay view
                mBoard.getOverlay().remove(srcPiece);
            }
        });
        return animator;
    }

    private void handleResponse(ChessResponse response) {
        ArrayList<Pair<String, Coordination>> pieceLocations = response.getPieceLocations();
        ChessMovement movements = response.getMovements();
        if (movements == null) {
            // No animations needed, only set piece locations on the board
            if (pieceLocations != null)
                setPieces(pieceLocations);
        } else {
            // Oh yes that's it. Now have to handle animations!!
            ArrayList<Animator> animators = new ArrayList<>();
            for (PairCells p : movements.getAllMoves()) {
                Pair<Integer, Integer> src = p.src.getTrueCoordination(model.getUserColor());
                Pair<Integer, Integer> trg = p.trg.getTrueCoordination(model.getUserColor());
                Log.d("TEST", String.format("Move from (%d, %d) to (%d, %d)", src.first, src.second, trg.first, trg.second));
                Animator animator = createMoveAnimation(src.first, src.second, trg.first, trg.second);
                if (animator != null)
                    animators.add(animator);
            }
            // Initiate animatorSet and run animation
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animators);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // Disable user tap for avoid spamming
                    setCellInteraction(false);
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    // Send confirm movement back to viewModel when animations ended
                    model.confirmMove(movements);
                    // Enable tapping again
                    setCellInteraction(true);
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                    // Send confirm movement back to viewModel when animations cancelled
                    model.confirmMove(movements);
                    // Enable tapping again
                    setCellInteraction(true);
                }
            });
            animatorSet.start();
        }
        // Set marks
        ArrayList<Pair<Integer, Coordination>> marks = response.getMarks();
        if (marks != null) {
            setMarks(marks);
        }
    }
}