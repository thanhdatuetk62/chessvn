package vn.edu.chessAgent;

import android.util.Log;

import java.util.concurrent.Executor;

import vn.edu.chessLogic.ChessMovement;
import vn.edu.chessLogic.GameState;

import vn.edu.Constants;

public class AgentConnector {
    /* This class is in charge of handling agent connections include AI and LAN mode */
    private final Executor executor; // Mostly works relate to background tasks, so we need to use Threading
    private int mode; // Use to distinguish between AI mode and LAN mode
    private ChessAI aiAgent; // For AI mode

    public AgentConnector(Executor executor) {
        this.executor = executor;
    }

    public void connectAI(int level, char color) {
        // No need to create listener for AI mode, only cache level variable
        mode = Constants.AI_MODE;
        aiAgent = new ChessAI(level, color);
        Log.d("TEST", "Connect to AI successfully!");
    }

    public Result<ChessMovement> synchronousMove(GameState state) {
        try {
            ChessMovement movement = null;
            switch (mode) {
                case Constants.AI_MODE:
                    movement = aiAgent.move(state);
                    break;
                case Constants.LAN_MODE:
                    throw new UnsupportedOperationException("LAN mode has not been implemented yet!");
            }
            return new Result.Success<>(movement);
        } catch (Exception e) {
            return new Result.Error<>(e);
        }
    }

    public void move(GameState state, AgentCallBack<ChessMovement> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Result<ChessMovement> result = synchronousMove(state);
                    callback.onComplete(result);
                } catch (Exception e) {
                    Result<ChessMovement> errResult = new Result.Error<>(e);
                    callback.onComplete(errResult);
                }
            }
        });
    }
}
