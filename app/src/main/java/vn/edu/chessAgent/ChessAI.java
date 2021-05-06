package vn.edu.chessAgent;

import android.util.Log;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import vn.edu.chessLogic.ChessMovement;
import vn.edu.chessLogic.GameState;

public class ChessAI {
    private int level;
    private char color;

    public ChessAI() {
        level = 0;
        color = 'w';
    }

    public ChessAI(int level, char color) {
        this.level = level;
        this.color = color;
    }

    public int randInt(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    public ChessMovement move(GameState state) throws InterruptedException {
        // Random :)))
        ArrayList<ChessMovement> possibleMoves = state.getAllPossibleMoves();
//        Thread.sleep(200);
        Log.d("TEST", String.format("Number of moves: %d", possibleMoves.size()));
        return possibleMoves.get(randInt(0, possibleMoves.size()-1));
    }
}
