package vn.edu.chess;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ChessViewModel extends ViewModel {
    private final ChessModel mModel;
    private final ChessRespond mRespond;
    private final MutableLiveData<ChessRespond> mLiveDataRespond;

    public ChessViewModel() {
        // Initialize chess logic (model)
        mModel = new ChessModel();
        // Initialize data UI binding (serve for live data)
        mRespond = new ChessRespond();
        mLiveDataRespond = new MutableLiveData<>(mRespond);
    }

    public void LoadModelCheckpoint(String checkpoint) {
        /*
        Load model checkpoint from disk / string code
        FLOW: This view model gets data from repository and parse the data received to the chess model
        CURRENT: Not implemented yet!
        */
        try {
            // Parse data to model
            mModel.loadCheckpoint(checkpoint);
            // Get necessary state
            //... Not implemented yet
        } catch (Exception e) {
            Log.d("EXCEPTION", String.valueOf(e));
            // Create new Game, can choose game style but NOT implemented yet!
            mModel.newGame();
        }
    }

    public LiveData<ChessRespond> getRespond() {
        return mLiveDataRespond;
    }

    private void updateUI() {
        mLiveDataRespond.setValue(mRespond);
    }

    public void selectSquare(int x, int y) {
        // Called when a user tap a square on the board
        // This method will decide when to call goMove or goSuggest
        // Need updateUI after process this function
        updateUI();
    }

    private void goMove(int x1, int y1, int x2, int y2) {

    }

    private void goSuggest(int x, int y) {

    }

    public void fetchBoard() {
        // Init step, called when enter the game
        // Need updateUI after process this function
        updateUI();
    }
}
