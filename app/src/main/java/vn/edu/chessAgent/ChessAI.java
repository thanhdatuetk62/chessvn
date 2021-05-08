package vn.edu.chessAgent;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import vn.edu.Constants;
import vn.edu.chessLogic.ChessMovement;
import vn.edu.chessLogic.Coordination;
import vn.edu.chessLogic.GameState;
import vn.edu.chessLogic.PairCells;
import vn.edu.chessLogic.pieces.BishopPiece;
import vn.edu.chessLogic.pieces.ChessPiece;
import vn.edu.chessLogic.pieces.KingPiece;
import vn.edu.chessLogic.pieces.KnightPiece;
import vn.edu.chessLogic.pieces.PawnPiece;
import vn.edu.chessLogic.pieces.QueenPiece;
import vn.edu.chessLogic.pieces.RookPiece;

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

    // Random bot
    public int randInt(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    public ChessMovement move(GameState state) {
        return search(state);
    }

    public static float eval(GameState state, char color) {
        // Give a evaluate float score value for a specified color side
        // For a win state, its value is 1.0
        // For a draw state, its value is 0
        // for a lose state, its value is -1.0
        int sz = Constants.SIZE;
        float score = 0.0f;
        // First need to check status, (whether the game is over or not)
        int gameStatus = state.isGameOver();
        if (gameStatus != Constants.NOT_FINISH) {
            if (gameStatus == Constants.CHECKMATE) {
                // Checkmate, if the current state color is the same with the color then the current
                // color side is lose => Return -1. Otherwise, return +1
                return (state.getCurrentColor() == color
                        ? -1.0f : 1.0f);
            } else {
                // Stalemate (Draw)
                return 0.0f;
            }
        }

        // Define some features
        float weightK = 0.80f; // Weight for King
        float weightQ = 0.50f; // Weight for Queen
        float weightN = 0.10f; // weight for Knight
        float weightR = 0.25f; // weight for Rook
        float weightB = 0.10f; // weight for Bishop
        float weightP = 0.05f; // weight for Pawn

        // 1. Delta between separate non-king type of pieces for each side
        float deltaTotal = 0;
        for (int x=0; x<sz; x++) {
            for (int y=0; y<sz; y++) {
                ChessPiece piece = state.getPieceAt(x, y);
                if (piece == null)
                    continue;
                float weight = 0.0f;
                if (piece instanceof QueenPiece) {
                    weight = weightQ;
                }
                if (piece instanceof KnightPiece) {
                    weight = weightN;
                }
                if (piece instanceof RookPiece) {
                    weight = weightR;
                }
                if (piece instanceof BishopPiece) {
                    weight = weightB;
                }
                if (piece instanceof PawnPiece) {
                    weight = weightP;
                }
                deltaTotal += weight * (piece.getColor() == color ? 1 : -1);
            }
        }

        // 2. Check availability of moves (number of available moves) or to measure who is being more
        // "conquer" than the other. In details:
        // => Counting number of squares which are in controlled by current color subtract by
        //    number of squares which are in controlled by opponent color
        int[][] cnt = new int[sz][sz];

        // Combine features together
        score = deltaTotal;
        // return score
        return score;
    }

    private static class ScoredMove {
        public float score;
        public ChessMovement move;

        ScoredMove(float score, ChessMovement move) {
            this.score = score;
            this.move = move;
        }
    }

    private ScoredMove minimum(GameState state, int d, float alpha, float beta) {
        // Initialize
        ScoredMove minNode = new ScoredMove(Float.POSITIVE_INFINITY, null);
        ArrayList<ChessMovement> possibleMoves = state.getAllPossibleMoves();

        // Check if this node is a leaf
        if (possibleMoves.isEmpty() || d == 0) {
            float score = eval(state, color);
            return new ScoredMove(score, null);
        }

        // Shuffle the list to ensure randomness
        Collections.shuffle(possibleMoves);

        // Iterate through all possible moves
        for (ChessMovement move : possibleMoves) {
            // Stimulate the move
            state.move(move);
            // Evaluate the optimal score for the children
            ScoredMove node = maximum(state, d - 1, alpha, beta);
            // Undo the move
            state.undo();
            // Get the minimum values among these max nodes
            if (node.score < minNode.score) {
                // re-assign the best node
                minNode.score = node.score;
                minNode.move = move;
            }
            // Early stop due to alpha-beta pruning
            if (minNode.score <= alpha)
                return minNode;
            // Else continue update value
            beta = Math.min(beta, minNode.score);
        }
        // return the optimal node
        return minNode;
    }

    private ScoredMove maximum(GameState state, int d, float alpha, float beta) {
        // Initialize
        ScoredMove maxNode = new ScoredMove(Float.NEGATIVE_INFINITY, null);
        ArrayList<ChessMovement> possibleMoves = state.getAllPossibleMoves();

        // Check if this node is a leaf
        if (possibleMoves.isEmpty() || d == 0) {
            float score = eval(state, color);
            return new ScoredMove(score, null);
        }

        // Shuffle the list to ensure randomness
        Collections.shuffle(possibleMoves);

        // Not a leaf node, so iterate through all possible moves
        for (ChessMovement move : possibleMoves) {
            // Stimulate the move
            state.move(move);
            // Evaluate the optimal score for the children
            ScoredMove node = minimum(state, d - 1, alpha, beta);
            // Undo the move
            state.undo();
            // Get the minimum values among these max nodes
            if (node.score > maxNode.score) {
                // re-assign the best node
                maxNode.score = node.score;
                maxNode.move = move;
            }
            // Early stop due to alpha-beta pruning
            if (maxNode.score >= beta)
                return maxNode;
            // Else continue update value
            alpha = Math.max(alpha, maxNode.score);
        }
        // return the optimal node
        return maxNode;
    }

    private ChessMovement search(GameState state) {
        // Extract the depth of search
        int d = -1;
        switch (level) {
            case Constants.NOVICE_LEVEL:
                d = 1;
                break;
            case Constants.EXPERT_LEVEL:
                d = 2;
                break;
            case Constants.MASTER_LEVEL:
                d = 3;
                break;
        }

        Log.d("TEST", String.format("Running with depth %d", d));

        // Get the optimal Node for the root (max node)
        // and initialize alpha to -inf and beta to +inf
        ScoredMove bestNode = maximum(state, d, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);

        Log.d("TEST", String.format("Done!. Best score is %f", bestNode.score));

        if (bestNode.move == null) {
            Log.d("TEST", "... but this move is null?? WTF!!");
        } else {
            int x1, y1, x2, y2;
            PairCells pc = bestNode.move.getActive();
            Coordination src = pc.src;
            Coordination trg = pc.trg;
            x1 = src.getCoordination().first;
            y1 = src.getCoordination().second;
            x2 = trg.getCoordination().first;
            y2 = trg.getCoordination().second;
            Log.d("TEST", String.format("... with location from (%d, %d) to (%d, %d)", x1, y1, x2, y2));
        }
        // Return the optimal movement
        return bestNode.move;
    }
}
