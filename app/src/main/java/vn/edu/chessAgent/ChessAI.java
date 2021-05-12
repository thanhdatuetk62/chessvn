package vn.edu.chessAgent;

import android.util.Log;

import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import vn.edu.Constants;
import vn.edu.chessLogic.ChessMovement;
import vn.edu.chessLogic.Coordination;
import vn.edu.chessLogic.GameState;
import vn.edu.chessLogic.pieces.ChessPiece;
import vn.edu.chessLogic.pieces.PawnPiece;

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

    public ChessMovement move(GameState state) {
        return search(state);
    }

    Comparator<ChessMovement> sortByScore(GameState state) {
        return (move1, move2) -> {
            state.move(move1);
            Integer eval1 = preEval(state);
            state.undo();
            state.move(move2);
            Integer eval2 = preEval(state);
            state.undo();
            return eval1.compareTo(eval2);
        };
    }

    private int preEval(GameState state) {
        int deltaTotal = 0;

        // threaten, protection, mobility
        for (Coordination coo : state.getPieceLocations(color)) {
            ChessPiece piece = state.getPieceAt(coo);
            // Delta
            deltaTotal += piece.d;
        }

        return deltaTotal;
    }

    public int eval(GameState state) {
        // Give a evaluate float score value for a specified color side
        // For a win state, its value is 1.0
        // For a draw state, its value is 0
        // for a lose state, its value is -1.0
        int sz = Constants.SIZE;
        int score = 0;
        // First need to check status, (whether the game is over or not)
        int gameStatus = state.isGameOver();
        if (gameStatus != Constants.NOT_FINISH) {
            if (gameStatus == Constants.CHECKMATE) {
                // Checkmate, if the current state color is the same with the color then the current
                // color side is lose => Return -1. Otherwise, return +1
                return (state.getCurrentColor() == color
                        ? -Constants.INF : Constants.INF);
            } else {
                // Stalemate (Draw)
                return 0;
            }
        }
        // Define some features
        // 1. Delta
        int deltaTotal = 0;
        // 2. Mobility of the Pieces
        int mobility = 0;
        // 3. Threaten
        int threaten = 0;
        // 4. Protection
        int protection = 0;
        // 5. Pawn advancement
        int pawnAdvance = 0;

        // threaten, protection, mobility
        for (Coordination coo : state.getPieceLocations(color)) {
            int x1 = coo.getCoordination().first, y1 = coo.getCoordination().second;
            ChessPiece piece = state.getPieceAt(coo);

            // Delta
            deltaTotal += piece.d;

            // Pawn advancement
            if (piece instanceof PawnPiece)
                pawnAdvance += (color == Constants.WHITE_COLOR ? sz - 1 - x1 : x1);

            // Protections
            for (Pair<Integer, Integer> p : piece.onHostage(x1, y1, state)) {
                int x2 = p.first, y2 = p.second;
                ChessPiece srcPiece = state.getPieceAt(x2, y2);
                if (!piece.isEnemy(srcPiece)) {
                    // Under protection by ally
                    protection += piece.p;
                }
            }
            // Mobility and threaten
            for (Pair<Integer, Integer> p : piece.allPossibleMoves(x1, y1, state)) {
                int x2 = p.first, y2 = p.second;
                ChessPiece trgPiece = state.getPieceAt(x2, y2);
                if (trgPiece != null && piece.isEnemy(trgPiece)) {
                    // Threaten enemy
                    threaten += trgPiece.t;
                }
                mobility += piece.m;
            }
        }

        // Combine features together
        score = deltaTotal * 4 + mobility + threaten + protection * 4 + pawnAdvance;
        // return score
        return score;
    }

    private static class ScoredMove {
        public int score;
        public ChessMovement move;

        ScoredMove(int score, ChessMovement move) {
            this.score = score;
            this.move = move;
        }
    }

    private ScoredMove minimum(GameState state, int d, int h, float alpha, float beta) {
        // Initialize
        ScoredMove minNode = new ScoredMove(Integer.MAX_VALUE, null);
        ArrayList<ChessMovement> possibleMoves = state.getAllPossibleMoves();

        // Check if this node is a leaf
        if (possibleMoves.isEmpty() || d == 0) {
            return new ScoredMove(eval(state), null);
        }

        // Sort increasing eval score
        Collections.sort(possibleMoves, sortByScore(state));

        // Truncate the number of moves
        if (h > 0)
            possibleMoves = new ArrayList<>(possibleMoves.subList(0, Math.min(possibleMoves.size(), h)));

        // Iterate through all possible moves
        for (ChessMovement move : possibleMoves) {
            // Stimulate the move
            state.move(move);

            // Evaluate the optimal score for the children
            ScoredMove node = maximum(state, d - 1, h, alpha, beta);

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

    private ScoredMove maximum(GameState state, int d, int h, float alpha, float beta) {
        // Initialize
        ScoredMove maxNode = new ScoredMove(Integer.MIN_VALUE, null);
        ArrayList<ChessMovement> possibleMoves = state.getAllPossibleMoves();

        // Check if this node is a leaf
        if (possibleMoves.isEmpty() || d == 0) {
            return new ScoredMove(eval(state), null);
        }

        // Sort increasing eval score
        Collections.sort(possibleMoves, sortByScore(state));

        // Reverse to sort decreasing
        Collections.reverse(possibleMoves);

        // Truncate the number of moves
        if (h > 0)
            possibleMoves = new ArrayList<>(possibleMoves.subList(0, Math.min(possibleMoves.size(), h)));

        // Not a leaf node, so iterate through all possible moves
        for (ChessMovement move : possibleMoves) {
            // Stimulate the move
            state.move(move);
            // Evaluate the optimal score for the children
            ScoredMove node = minimum(state, d - 1, h, alpha, beta);
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
        int d = -1, h = -1;
        switch (level) {
            case Constants.NOVICE_LEVEL:
                d = 2;
                break;
            case Constants.EXPERT_LEVEL:
                d = 3;
                break;
            case Constants.MASTER_LEVEL:
                d = 4;
                break;
        }
        // Get the optimal Node for the root (max node)
        // and initialize alpha to -inf and beta to +inf
        ScoredMove bestNode = maximum(state, d, h, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);

        Log.d("TEST", String.format("Done!. Best score is %d", bestNode.score));
        // Return the optimal movement
        return bestNode.move;
    }
}
