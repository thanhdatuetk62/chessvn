package chessLogic;

import android.util.Log;

import androidx.core.util.Pair;

import java.util.ArrayList;

import chessLogic.Constants;

public class ChessModel {
    private ChessPiece[][] mState = new ChessPiece[Constants.SIZE][Constants.SIZE];
    protected char curColor = Constants.WHITE_COLOR;

    public ChessModel() {

    }

    public void loadCheckpoint(String checkpoint) {
        // NotImplementedError!
        throw new UnsupportedOperationException("Load checkpoint has not been implemented yet!");
    }

    public void newGame() {
        curColor = Constants.WHITE_COLOR;
        mState = new ChessPiece[][]{
                {
                    // First rank
                    new RookPiece(Constants.BLACK_COLOR), new KnightPiece(Constants.BLACK_COLOR), new BishopPiece(Constants.BLACK_COLOR),
                    new KingPiece(Constants.BLACK_COLOR), new QueenPiece(Constants.BLACK_COLOR), new BishopPiece(Constants.BLACK_COLOR),
                    new KnightPiece(Constants.BLACK_COLOR), new RookPiece(Constants.BLACK_COLOR)
                },
                {
                    // Second rank
                    new PawnPiece(Constants.BLACK_COLOR), new PawnPiece(Constants.BLACK_COLOR), new PawnPiece(Constants.BLACK_COLOR),
                    new PawnPiece(Constants.BLACK_COLOR), new PawnPiece(Constants.BLACK_COLOR), new PawnPiece(Constants.BLACK_COLOR),
                    new PawnPiece(Constants.BLACK_COLOR), new PawnPiece(Constants.BLACK_COLOR)
                },
                // 3th -> 6th rank
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {
                    // Seventh rank
                    new PawnPiece(Constants.WHITE_COLOR), new PawnPiece(Constants.WHITE_COLOR), new PawnPiece(Constants.WHITE_COLOR),
                    new PawnPiece(Constants.WHITE_COLOR), new PawnPiece(Constants.WHITE_COLOR), new PawnPiece(Constants.WHITE_COLOR),
                    new PawnPiece(Constants.WHITE_COLOR), new PawnPiece(Constants.WHITE_COLOR)
                },
                {
                    // Eighth rank
                    new RookPiece(Constants.WHITE_COLOR), new KnightPiece(Constants.WHITE_COLOR), new BishopPiece(Constants.WHITE_COLOR),
                    new KingPiece(Constants.WHITE_COLOR), new QueenPiece(Constants.WHITE_COLOR), new BishopPiece(Constants.WHITE_COLOR),
                    new KnightPiece(Constants.WHITE_COLOR), new RookPiece(Constants.WHITE_COLOR)
                }
        };
    }

    protected ChessPiece getPieceAt(int x, int y) {
        return mState[x][y];
    }

    public boolean hasPiece(int x, int y) {
        return mState[x][y] != null;
    }

    public boolean canMove(int x1, int y1, int x2, int y2) {
        return mState[x1][y1].canMove(x1, y1, x2, y2, this);
    }

    public ChessMovement preMove(int x1, int y1, int x2, int y2) {
        ChessMovement movement = new ChessMovement();
        movement.setActive(new Coordination(x1, y1), new Coordination(x2, y2));
        return movement;
    }

    public ArrayList<Pair<String, Coordination>> postMove(ChessMovement movement) {
        // Override locations state
        // Used to confirm movement from preMove -> update piece location
        PairCells activeMove = movement.getActive();
        Pair<Integer, Integer> srcActive = activeMove.src.getCoordination();
        Pair<Integer, Integer> trgActive = activeMove.trg.getCoordination();
        mState[trgActive.first][trgActive.second] = mState[srcActive.first][srcActive.second];
        mState[srcActive.first][srcActive.second] = null;
        return getPieceLocations();
    }

    public ArrayList<Pair<String, Coordination>> getPieceLocations() {
        ArrayList<Pair<String, Coordination>> pieces = new ArrayList<>();
        for (int r = 0; r < Constants.SIZE; r++) {
            for (int c = 0; c < Constants.SIZE; c++) {
                if (mState[r][c] != null) {
                    pieces.add(new Pair<>(mState[r][c].pieceCode(), new Coordination(r, c)));
                }
            }
        }
        return pieces;
    }

    public ArrayList<Pair<Integer, Coordination>> getSuggestions(int x, int y) {
        ArrayList<Pair<Integer, Coordination>> suggestions = new ArrayList<>();
        if (mState[x][y] == null)
            return suggestions;
        ChessPiece piece = mState[x][y];
        for (int r = 0; r < Constants.SIZE; r++) {
            for (int c = 0; c < Constants.SIZE; c++) {
                if (piece.canMove(x, y, r, c, this)) {
                    int markType = getMarkType(x, y, r, c);
                    suggestions.add(new Pair<>(markType, new Coordination(r, c)));
                }
            }
        }
        Log.d("TEST", String.format("Number of available moves: %d", suggestions.size()));
        return suggestions;
    }

    private int getMarkType(int x1, int y1, int x2, int y2) {
        // Check if it is capture mark
        if (mState[x2][y2] != null)
            return Constants.CAPTURE_MARK_TAG;
        return Constants.OCCUPY_MARK_TAG;
    }
}
