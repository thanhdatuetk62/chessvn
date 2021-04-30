package vn.edu.chessUI;

import android.util.Log;
import androidx.core.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import chessLogic.ChessModel;
import chessLogic.ChessMovement;
import chessLogic.Coordination;
import chessLogic.PairCells;

public class ChessViewModel extends ViewModel {
    private final ChessModel mModel;
    private ChessResponse mResponse;
    private final ChessControl mControl;
    private final MutableLiveData<ChessResponse> mLiveDataResponse;
    private final MutableLiveData<ChessControl> mLiveDataControl;
    private Pair<Integer, Integer> mLastCell;
    private PairCells mLastActive;
    private int mUserColor = Constants.WHITE_PERSPECTIVE;

    public ChessViewModel() {
        // Initialize chess logic (model)
        mModel = new ChessModel();
        // Initialize data UI binding (serve for live data)
        mResponse = new ChessResponse();
        mControl = new ChessControl();
        mLiveDataResponse = new MutableLiveData<>(mResponse);
        mLiveDataControl = new MutableLiveData<>(mControl);
    }

    public void loadModelCheckpoint(int mode) {
        /*
        Load model checkpoint from disk / string code
        FLOW: This view model gets data from repository and parse the data received to the chess model
        CURRENT: Not implemented yet!
        */
        switch (mode) {
            case Constants.AI_MODE:
                try {
                    // Parse data to model
                    mModel.loadCheckpoint(Constants.pathToDisk);
                    // Get necessary state (get user perspective, add labels, ...)
                    //... Not implemented yet
                    updateUI();
                } catch (Exception e) {
                    Log.d("EXCEPTION", String.valueOf(e));
                    // Create new Game, can choose game level but NOT implemented yet!
                    // Highlight new game button!
                    mControl.setOptionHL(true);
                    updateControl();
                }
                break;
            case Constants.LAN_MODE:
                throw new UnsupportedOperationException("Local Network mode has not been implemented yet!");
        }
    }

    public void newAIGame(int level, int userColor) {
        //TODO: Create new AI agent w.r.t this level
        Log.d("TEST", String.format("Create new game with level %d and side %d", level, userColor));
        // Update UI with new game setup
        mModel.newGame();
        // Get necessary info to update UI
        mUserColor = userColor;
        mResponse = new ChessResponse();
        mResponse.setPieceLocations(mModel.getPieceLocations());
        // Call Update UI method
        updateUI();
        // Turn off all highlights in control panel
        mControl.setOptionHL(false);
        mControl.setRotateHL(false);
        updateControl();
    }

    public void newLANGame() {

    }

    public LiveData<ChessResponse> getResponse() {
        return mLiveDataResponse;
    }

    public LiveData<ChessControl> getControl() {
        return mLiveDataControl;
    }

    private void updateControl() {
        mLiveDataControl.setValue(mControl);
    }

    private void updateUI() {
        mLiveDataResponse.setValue(mResponse);
    }

    public void selectSquare(int x, int y) {
        // Called when a user tap a square on the board
        // This method will decide when to call goMove or goSuggest
        // Need updateUI after process this function
        // Convert to origin coordination system
        x = transform(x);
        y = transform(y);
        // Now need to decide when user when suggestions or movements
        if (mLastCell == null) {
            // It's a suggestion?
            // First need to check if there is a piece resides in this cell
            if (!mModel.hasPiece(x, y))
                // No piece selected! Terminate immediately
                return;
            else {
                // OK!, This is a piece, now have to get suggestions
                userSuggest(x, y);
                mLastCell = new Pair<>(x, y);
            }
        } else {
            // Check if can move from previous selected to the current one
            int prevX = mLastCell.first;
            int prevY = mLastCell.second;
            if (mModel.canMove(prevX, prevY, x, y)) {
                mLastActive = new PairCells(new Coordination(prevX, prevY), new Coordination(x, y));
                userMove(prevX, prevY, x, y);
                mLastCell = null;
                clearMarksExceptHistory();
            } else {
                // It's not a valid move,
                // so need to check if there is a piece on this location so that we can perform suggestion
                if (mModel.hasPiece(x, y)) {
                    userSuggest(x, y);
                    mLastCell = new Pair<>(x, y);
                } else {
                    mLastCell = null;
                    clearMarksExceptHistory();
                }
            }
        }
        updateUI();
    }

    private void clearMarksExceptHistory() {
        // Clear all marks except history marks
        ArrayList<Pair<Integer, Coordination>> marks = new ArrayList<>();
        if (mLastActive != null) {
            marks.add(new Pair<>(Constants.HISTORY_MARK_TAG, mLastActive.src));
            marks.add(new Pair<>(Constants.HISTORY_MARK_TAG, mLastActive.trg));
        }
        mResponse.setMarks(marks);
    }

    private void userMove(int x1, int y1, int x2, int y2) {
        // Called when user perform a move, only used by selectSquare internally
        ChessMovement movements = mModel.preMove(x1, y1, x2, y2);
        // Renew movements on Response object
        mResponse.setMovements(movements);
    }

    private void userSuggest(int x, int y) {
        // Called when user want to get suggestions of moves, only used by selectSquare internally
        // NOTE: Suggestion marks will be on top of history marks
        ArrayList<Pair<Integer, Coordination>> marks = new ArrayList<>();
        // Selection mark
        marks.add(new Pair<>(Constants.SELECT_MARK_TAG, new Coordination(x, y)));
        // History marks
        if (mLastActive != null) {
            marks.add(new Pair<>(Constants.HISTORY_MARK_TAG, mLastActive.src));
            marks.add(new Pair<>(Constants.HISTORY_MARK_TAG, mLastActive.trg));
        }
        // Suggestions marks
        ArrayList<Pair<Integer, Coordination>> suggestionMarks = mModel.getSuggestions(x, y);
        marks.addAll(suggestionMarks);
        // Renew marks on response object
        mResponse.setMarks(marks);
    }

    public void agentMove(int x1, int y1, int x2, int y2) {
        // Called when opponent (agent) perform a move, only used by ChessGameFragment
    }

    private int transform(int x) {
        // Called when to translate from UI location to Logic location or vice versa
        // Must be called each time UI requests data from ViewModel
        if (mUserColor == Constants.BLACK_PERSPECTIVE) {
            return Constants.SIZE - 1 - x;
        } else {
            return x;
        }
    }

    public void userRotate() {
        // Rotate board the the view from opponent
        mControl.rotateHL();
        // Update control panel
        updateControl();
        // Need to update transformer to update board
        // Update UI
        updateUI();
    }

    public void fetchBoard() {
        // Init step, called when enter the game or when config changes such as rotations or device switches
        // Need updateUI after process this function
        updateUI();
    }

    public int getUserColor() {
        return mUserColor;
    }

    public void confirmMove(ChessMovement movements) {
        // TODO: Check if there is a need to promotion
        // Send back to chess model
        ArrayList<Pair<String, Coordination>> locations = mModel.postMove(movements);
        mResponse.setPieceLocations(locations);
        // Update UI
        updateUI();
    }
}
