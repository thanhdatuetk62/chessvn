package vn.edu.chessUI.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import androidx.core.util.Pair;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import vn.edu.chessAgent.AgentCallBack;
import vn.edu.chessAgent.AgentConnector;
import vn.edu.chessAgent.Result;
import vn.edu.chessLogic.ChessModel;
import vn.edu.chessLogic.ChessMovement;
import vn.edu.chessLogic.Coordination;
import vn.edu.chessLogic.PairCells;
import vn.edu.chessUI.ChessApplication;

import vn.edu.Constants;

public class ChessViewModel extends AndroidViewModel {
    private final ChessModel mModel;
    private int mode;
    private ChessResponse mResponse;
    private final ChessControl mControl;
    private final MutableLiveData<ChessResponse> mLiveDataResponse;
    private final MutableLiveData<ChessControl> mLiveDataControl;
    private Pair<Integer, Integer> mLastCell;
    private ArrayList<Pair<Integer, Coordination>> mCheckMarks;
    private PairCells mLastActive;
    private char mUserColor = Constants.WHITE_COLOR;
    private char mAgentColor = Constants.BLACK_COLOR;
    private AgentConnector agentConnector;
    private Executor executor;

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public ChessViewModel(Application application) {
        super(application);

        // Initialize chess logic (model)
        mModel = new ChessModel();

        // Initialize data UI binding (serve for live data)
        mResponse = new ChessResponse();
        mControl = new ChessControl();
        mLiveDataResponse = new MutableLiveData<>(mResponse);
        mLiveDataControl = new MutableLiveData<>(mControl);

        // Initialize agent connection
        executor = ((ChessApplication) application).executorService;
        agentConnector = new AgentConnector(executor);
    }

    public void loadModelCheckpoint(int mode) {
        /*
        Load model checkpoint from disk / string code
        */
        this.mode = mode;
        // Loading panel visible
        mControl.setLoading(true);
        // Consider move this method for running on worker thread :)
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Delay for few seconds
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                switch (mode) {
                    case Constants.AI_MODE:
                        try {
                            // Parse data to model
                            mModel.loadCheckpoint(Constants.pathToDisk);
                        } catch (Exception e) {
                            Log.e("EXCEPTION", String.valueOf(e), e);
                            // Create new Game, can choose game level but NOT implemented yet!
                            // Highlight new game button!
                            mControl.setOptionHL(true);
                        }
                        break;
                    case Constants.LAN_MODE:
                        // User need to choose a player to start the game!
                        mControl.setOptionHL(true);
                }
                // Turn off loading
                mControl.setLoading(false);
                updateUI(true);
                updateControl(true);
            }
        });
    }

    public void newAIGame(int level, int userColor) {
        Log.d("TEST", String.format("Create new game with level %d and side %d", level, userColor));
        // Update UI with new game setup
        mUserColor = Constants.COLORS[userColor];
        mAgentColor = Constants.COLORS[userColor ^ 1];
        mModel.newGame(mUserColor);

        // Get necessary info to update UI
        mResponse = new ChessResponse();
        mResponse.setPieceLocations(mModel.getPieceLocations());

        // Init/clear all marks
        mLastActive = null;
        mCheckMarks = null;
        mLastCell = null;
        mResponse.setMarks(new ArrayList<>());

        // Turn off all highlights in control panel
        mControl.setOptionHL(false);
        mControl.setRotateHL(false);
        // Update status
        handleGameStatus();
        // Call Update UI method
        updateUI(false);
        updateControl(false);

        // Connect to AI agent
        agentConnector.connectAI(level, mAgentColor);

        // Let the agent move first if user is in black side
        if (mUserColor == Constants.BLACK_COLOR)
            agentMove();
    }

    public void newLANGame(WifiP2pInfo info) {
        // Initialize connection
        Log.d("TEST", "Connected to the network!");

        // It helps create thread based
        boolean isServer = agentConnector.connectLAN(info);

        // Set white color for client and black color for server :)) because I am lazy OK?
        mUserColor = (isServer ? Constants.BLACK_COLOR : Constants.WHITE_COLOR);
        mAgentColor = (isServer ? Constants.WHITE_COLOR : Constants.BLACK_COLOR);
        mModel.newGame(mUserColor);

        // Get necessary info to update UI
        mResponse = new ChessResponse();
        mResponse.setPieceLocations(mModel.getPieceLocations());

        // Init/clear all marks
        mLastActive = null;
        mCheckMarks = null;
        mLastCell = null;
        mResponse.setMarks(new ArrayList<>());

        // Turn off all highlights in control panel
        mControl.setOptionHL(false);
        mControl.setRotateHL(false);
        // Update status
        handleGameStatus();
        // Call Update UI method
        updateUI(false);
        updateControl(false);

        if (mUserColor == Constants.BLACK_COLOR)
            agentMove();
    }

    public LiveData<ChessResponse> getResponse() {
        return mLiveDataResponse;
    }

    public LiveData<ChessControl> getControl() {
        return mLiveDataControl;
    }

    private void updateControl(boolean async) {
        if (async) {
            mLiveDataControl.postValue(mControl);
        } else {
            mLiveDataControl.setValue(mControl);
        }
    }

    private void updateUI(boolean async) {
        if (async) {
            mLiveDataResponse.postValue(mResponse);
        } else
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

                    // Preserve check marks
                    if (mCheckMarks != null)
                        mResponse.getMarks().addAll(mCheckMarks);
                }
            }
        }
        updateUI(false);
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
        ArrayList<Pair<Integer, Coordination>> marks = mModel.getSuggestions(x, y);

        // Selection mark
        marks.add(new Pair<>(Constants.SELECT_MARK_TAG, new Coordination(x, y)));

        // History marks
        if (mLastActive != null) {
            marks.add(new Pair<>(Constants.HISTORY_MARK_TAG, mLastActive.src));
            marks.add(new Pair<>(Constants.HISTORY_MARK_TAG, mLastActive.trg));
        }

        // Preserve check marks
        if (mCheckMarks != null)
            marks.addAll(mCheckMarks);
        mResponse.setMarks(marks);
    }

    public void agentMove() {
        // Called when opponent (agent) perform a move, only used by ChessGameFragment
        // Send signal to the agent, will copy the state (later) to ensure that the thread is safe!
        agentConnector.move(mModel.getState().copy(), new AgentCallBack<ChessMovement>() {
            @Override
            public void onComplete(Result<ChessMovement> result) {
                if (result instanceof Result.Success) {
                    ChessMovement movement = ((Result.Success<ChessMovement>) result).data;
                    if (movement != null) {
                        movement.setOnAir();
                        mModel.postMove(movement);
                        ArrayList<Pair<String, Coordination>> locations = mModel.getPieceLocations();
                        mResponse.setMovements(movement);
                        mResponse.setPieceLocations(locations);

                        // Clear marks
                        mLastActive = mModel.getLastActive();
                        clearMarksExceptHistory();

                        // and append these mark to current marks
                        mCheckMarks = mModel.checkMarks();
                        mResponse.getMarks().addAll(mCheckMarks);

                        // Handle game status
                        handleGameStatus();

                        // Update UI;
                        updateUI(true);
                        updateControl(true);
                    } else {
                        Log.d("TEST", "Not implemented!");
                    }
                } else {
                    // Show Error in Logcat
                    Exception exception = ((Result.Error<ChessMovement>) result).exception;
                    Log.e("TEST", String.valueOf(exception), exception);
                }
            }
        });
    }

    private int transform(int x) {
        // Called when to translate from UI location to Logic location or vice versa
        // Must be called each time UI requests data from ViewModel
        if (mUserColor == Constants.BLACK_COLOR) {
            return Constants.SIZE - 1 - x;
        } else {
            return x;
        }
    }

    public void userRotate() {
        // Rotate board the the view from opponent
        mControl.rotateHL();

        // Update control panel
        updateControl(false);

        // Need to update transformer to update board
        // Update UI
        updateUI(false);
    }

    public void undo() {
        if (mode == Constants.AI_MODE) {
            ChessMovement movement = mModel.undo();
            if (movement == null)
                // No undo avail
                return;
            // No model confirm required because it has already updated on the air!
            movement.setOnAir();
            movement.revert();
            ArrayList<Pair<String, Coordination>> locations = mModel.getPieceLocations();
            mResponse.setMovements(movement);
            mResponse.setPieceLocations(locations);
            // Clear marks
            mLastActive = mModel.getLastActive();
            clearMarksExceptHistory();
            // Update UI;
            updateUI(false);
        } else {
            Log.d("TEST", "LAN mode did not implement undo function yet!");
        }
    }

    public char getUserColor() {
        return mUserColor;
    }

    private void handleGameStatus() {
        // get status and init status :)
        int status = mModel.getGameStatus();
        mResponse.setGameStatus(status);
        mControl.setGameStatus(status);

        // Set icon and text for each side
        mControl.setUserTurn(mModel.getCurrentTurn() == mUserColor
                ? 1 : 0);
        mControl.setUserColor(mUserColor == Constants.WHITE_COLOR
                ? Constants.WHITE_PERSPECTIVE : Constants.BLACK_PERSPECTIVE);

        if (status != Constants.NOT_FINISH) {
            // Game is over, close all connection
            // Also close p2p connections which in created in activity!
            Log.d("TEST", "Close connection? WTF???");
//            agentConnector.closeConnection();
        }
    }

    public void confirmMove(ChessMovement movements) {
        // NOTE: Can only be used by user, the agent moves will be handled on another method!
        // Send back to chess model
        // Set on Air for this move to avoid promotion choice on other client [running LAN mode]
        movements.setOnAir();
        ArrayList<Pair<String, Coordination>> locations = mModel.postMove(movements);
        mResponse.setPieceLocations(locations);

        // Get list of additional marks after confirmMove
        // and append these mark to current marks
        mCheckMarks = mModel.checkMarks();
        mResponse.getMarks().addAll(mCheckMarks);

        // Will check if game is end or not to display necessary dialogs and close connection!
        handleGameStatus();

        // Update UI
        updateUI(false);
        updateControl(false);

        // Handle the next action of agent move
        agentMove();
    }

    public char getCurrentTurn() {
        return mModel.getCurrentTurn();
    }
}
