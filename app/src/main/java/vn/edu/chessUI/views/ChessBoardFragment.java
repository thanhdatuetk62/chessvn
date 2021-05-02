package vn.edu.chessUI.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.AlertDialog;
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

import vn.edu.chessLogic.ChessMovement;
import vn.edu.chessLogic.Coordination;
import vn.edu.chessLogic.PairCells;
import vn.edu.chessUI.viewmodels.ChessResponse;
import vn.edu.chessUI.viewmodels.ChessViewModel;
import vn.edu.chessUI.R;

import vn.edu.Constants;

public class ChessBoardFragment extends Fragment {
    private final ChessCellView[][] mCells = new ChessCellView[Constants.SIZE][Constants.SIZE];
    private ViewGroup mBoard;
    private ChessViewModel model;
    private AlertDialog mPromoteOptions;
    private ViewGroup mPromoteLayout;

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
        // Inflate promotion dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialog = inflater.inflate(R.layout.dialog_promote_options, null);
        mPromoteOptions = new AlertDialog.Builder(requireActivity())
                .setView(dialog)
                .create();
        mPromoteOptions.setCanceledOnTouchOutside(false);
        // Link to this view
        mPromoteLayout = (ViewGroup) dialog;
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

    private void setPieces(String[][] locations) {
        for (int r = 0; r < Constants.SIZE; r++) {
            for (int c = 0; c < Constants.SIZE; c++) {
                String newCode = locations[r][c];
                ChessPieceView piece = getPiece(r, c);
                if (newCode == null) {
                    // Remove pieces in this location
                    mCells[r][c].removeView(piece);
                } else if (piece == null) {
                    // No piece resides on this cell, so I create a new one and add it into the cell
                    piece = new ChessPieceView(requireActivity());
                    piece.setPieceType(newCode);
                    mCells[r][c].addView(piece);
                } else if (!piece.getPieceCode().equals(newCode)) {
                    // Change pieces image :)
                    piece.setPieceType(newCode);
                }
            }
        }
    }

    private void setPieces(ArrayList<Pair<String, Coordination>> locations) {
        String[][] pieceCodes = new String[Constants.SIZE][Constants.SIZE];
        for (Pair<String, Coordination> obj : locations) {
            String pieceCode = obj.first;
            Pair<Integer, Integer> coordination = obj.second.getTrueCoordination(model.getUserColor());
            pieceCodes[coordination.first][coordination.second] = pieceCode;
        }
        setPieces(pieceCodes);
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
                // Override
                srcPiece.setTranslationX(0);
                srcPiece.setTranslationY(0);
                mCells[x2][y2].removeView(trgPiece);
                mCells[x2][y2].addView(srcPiece);
            }
        });
        return animator;
    }

    private void showPromoteDialog(ChessMovement movement) {
        char[] codes = {'Q', 'R', 'N', 'B'};
        // Get layout
        for (int i = 0; i < mPromoteLayout.getChildCount(); i++) {
            ChessPieceView piece = (ChessPieceView) mPromoteLayout.getChildAt(i);
            piece.setPieceType("" + movement.getPromoteSide() + codes[i]);
            piece.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    movement.setPromotion(piece.getPieceCode());
                    model.confirmMove(movement);
                    mPromoteOptions.dismiss();
                }
            });
        }
        mPromoteOptions.show();
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
                    // Handle promotion before send confirmation to the model
                    char promoteSide = movements.getPromoteSide();
                    if (promoteSide == Constants.WHITE_COLOR || promoteSide == Constants.BLACK_COLOR) {
                        // Wait user to choose promotion piece before sending confirmation to model
                        showPromoteDialog(movements);
                    } else {
                        // Send confirm movement back to viewModel when animations ended
                        if (movements.isOnAir()) {
                            if (pieceLocations != null)
                                setPieces(pieceLocations);
                        } else
                            model.confirmMove(movements);
                    }
                    // Enable tapping again
                    setCellInteraction(true);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    // Handle promotion before send confirmation to the model
                    char promoteSide = movements.getPromoteSide();
                    if (promoteSide == Constants.WHITE_COLOR || promoteSide == Constants.BLACK_COLOR) {
                        showPromoteDialog(movements);
                    } else {
                        // Send confirm movement back to viewModel when animations ended
                        if (movements.isOnAir()) {
                            if (pieceLocations != null)
                                setPieces(pieceLocations);
                        } else
                            model.confirmMove(movements);
                    }
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