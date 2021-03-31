package vn.edu.chess;

import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class ChessBoardViewModel extends ViewModel {
    private static final int SIZE = 8;
    public static final int WHITE_PERSPECTIVE = 0;
    public static final int BLACK_PERSPECTIVE = 1;
    private static final int SELECT_MARK_TAG = 1;
    private static final int HISTORY_MARK_TAG = 2;
    private static final int CAPTURE_MARK_TAG = 3;
    private static final int OCCUPY_MARK_TAG = 4;
    private static final int PROMOTE_MARK_TAG = 5;
    private static final int OVER_THE_BOARD_MODE = 0;

    private static final char BLACK_COLOR = 'b';
    private static final char WHITE_COLOR = 'w';

    private final MutableLiveData<ChessViewModelEvent> mLiveDataEvent;

    private final ChessBoardModel mGameState;
    private final ChessViewModelEvent mEvent;
    private final int mMode;
    private final boolean mAllowRotateForEachMove;
    private String mSelectedLocation;
    private Pair<String, String> mHistoryMarks;

    public ChessBoardViewModel() {
        mGameState = new ChessBoardModel();
        mMode = OVER_THE_BOARD_MODE;
        mAllowRotateForEachMove = false;
        // Create live data
        int perspective = (mGameState.getAlly() == WHITE_COLOR ?
                WHITE_PERSPECTIVE : BLACK_PERSPECTIVE);
        mEvent = new ChessViewModelEvent(perspective);
        mLiveDataEvent = new MutableLiveData<>(mEvent);
    }

    public LiveData<ChessViewModelEvent> getEvent() {
        return mLiveDataEvent;
    }

    public ArrayList<Pair<String, String>> getGameState() {
        return mGameState.getPieceLocations();
    }

    public void onSelectLocation(String location) {
        int flag = (mSelectedLocation != null ? mGameState.isValidMove(mSelectedLocation, location) : -1);
        if (flag >=0) {
            Log.d("TEST", "Move from " + mSelectedLocation + " to " + location);
            handleMoveAction(mSelectedLocation, location, flag);
        } else {
            Log.d("TEST", location + " is selected!");
            handleMoveSuggestions(location);
        }
        // Sync with UI using live data
        mLiveDataEvent.setValue(mEvent);
    }

    public void rotatePerspective() {
        mEvent.setPerspective(mEvent.getPerspective() ^ 1);
        mLiveDataEvent.setValue(mEvent);
    }

    private void handleMoveAction(String srcLocation, String trgLocation, int flag) {
        mSelectedLocation = null;
        // Initialize marks for UX
        ArrayList<Pair<String, Integer>> marks = new ArrayList<>();
        // Add history marks
        mHistoryMarks = new Pair<>(srcLocation, trgLocation);
        marks.add(new Pair<>(srcLocation, HISTORY_MARK_TAG));
        marks.add(new Pair<>(trgLocation, HISTORY_MARK_TAG));
        // Update marks into event
        mEvent.setMarks(marks);
        // Get moves
        ArrayList<Pair<String, String>> moves = new ArrayList<>();
        ChessMovement movement = mGameState.move(srcLocation, trgLocation, flag);
        Pair<String, String> triggerMove = movement.getTriggerMove();
        Pair<String, String> causalMove = movement.getCausalMove();
        if (triggerMove != null)
            moves.add(triggerMove);
        if (causalMove != null)
            moves.add(causalMove);
        // Update moves into event
        mEvent.setMove(moves);
        // For Over The Board mode
        if (mMode == OVER_THE_BOARD_MODE && mAllowRotateForEachMove) {
            int perspective = (mGameState.getAlly() == WHITE_COLOR ?
                    WHITE_PERSPECTIVE : BLACK_PERSPECTIVE);
            // Update perspective into event
            mEvent.setPerspective(perspective);
        }
    }

    private void handleMoveSuggestions(String location) {
        mSelectedLocation = location;
        // Initialize marks for UX
        ArrayList<Pair<String, Integer>> marks;
        // Get all possible moves for this location
        marks = mGameState.getMoveSuggestions(location);
        // Add history marks
        if (mHistoryMarks != null) {
            marks.add(new Pair<>(mHistoryMarks.first, HISTORY_MARK_TAG));
            marks.add(new Pair<>(mHistoryMarks.second, HISTORY_MARK_TAG));
        }
        // Add select mark
        marks.add(new Pair<>(location, SELECT_MARK_TAG));
        // Set value in live data
        mEvent.setMarks(marks);
    }
}
