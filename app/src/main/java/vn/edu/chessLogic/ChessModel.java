package vn.edu.chessLogic;

import androidx.core.util.Pair;

import java.util.ArrayList;

import vn.edu.chessLogic.pieces.ChessPiece;
import vn.edu.chessLogic.pieces.KingPiece;
import vn.edu.chessLogic.pieces.PawnPiece;
import vn.edu.chessLogic.pieces.RookPiece;
import vn.edu.Constants;

public class ChessModel {
    private GameState mState;
    protected char curColor;

    public ChessModel() {
        mState = new GameState();
    }

    public void loadCheckpoint(String checkpoint) {
        throw new UnsupportedOperationException("Load checkpoint has not been implemented yet!");
    }

    public char getCurColor() {
        return curColor;
    }

    private ArrayList<Coordination> getPiecesByColor(char color) {
        return mState.getPieceLocations(color);
    }

    public void newGame(char color) {
        curColor = color;
        mState.newGame();
    }

    public GameState getState() {
        return mState;
    }

    public ChessPiece getPieceAt(int x, int y) {
        return mState.getPieceAt(x, y);
    }

    public boolean hasPiece(int x, int y) {
        return mState.getPieceAt(x, y) != null;
    }

    public char getOpponentColor() {
        return (curColor == Constants.WHITE_COLOR ? Constants.BLACK_COLOR : Constants.WHITE_COLOR);
    }

    public boolean canMove(int x1, int y1, int x2, int y2) {
        // Context checking (Include current turn, king checked, ...)
        ChessPiece piece = mState.getPieceAt(x1, y1);
        if (piece != null && piece.getColor() != curColor) {
            // Opponent is moving, pls wait :)
            return false;
        }
        return mState.canMove(x1, y1, x2, y2);
    }

    public ChessMovement preMove(int x1, int y1, int x2, int y2) {
        return mState.createMovement(x1, y1, x2, y2);
    }

    public ArrayList<Pair<String, Coordination>> postMove(ChessMovement movement) {
        // Override locations state
        // Used to confirm movement from preMove -> update piece location
        mState.move(movement);
        // Switch turn WHITE <-> BLACK || Just for testing
        return getPieceLocations();
    }

    public ChessMovement undo() {
        return mState.undo();
    }

    public ArrayList<Pair<String, Coordination>> getPieceLocations() {
        ArrayList<Pair<String, Coordination>> pieceLocations = new ArrayList<>();
        for (Coordination c : mState.getPieceLocations()) {
            ChessPiece piece = mState.getPieceAt(c);
            pieceLocations.add(new Pair<>(piece.pieceCode(), c));
        }
        return pieceLocations;
    }

    public ArrayList<Pair<Integer, Coordination>> getSuggestions(int x, int y) {
        ArrayList<Pair<Integer, Coordination>> suggestions = new ArrayList<>();
        if (mState.getPieceAt(x, y) == null)
            return suggestions;
        for (int r = 0; r < Constants.SIZE; r++) {
            for (int c = 0; c < Constants.SIZE; c++) {
                if (canMove(x, y, r, c)) {
                    int markType = getMarkType(x, y, r, c);
                    suggestions.add(new Pair<>(markType, new Coordination(r, c)));
                }
            }
        }
        return suggestions;
    }

    public PairCells getLastActive() {
        return mState.getLastActive();
    }

    private int getMarkType(int x1, int y1, int x2, int y2) {
        // Check if it is capture mark
        ChessPiece trgPiece = mState.getPieceAt(x2, y2);
        if (trgPiece != null) {
            // Need to check if it is Check move
            if (trgPiece instanceof KingPiece) {
                return Constants.CHECK_MARK_TAG;
            }
            return Constants.CAPTURE_MARK_TAG;
        }
        return Constants.OCCUPY_MARK_TAG;
    }

    public ArrayList<Pair<Integer, Coordination>> checkMarks() {
        return checkMarks(mState);
    }

    public ArrayList<Pair<Integer, Coordination>> checkMarks(GameState state) {
        // Used to check if any king is currently on check!
        ArrayList<Pair<Integer, Coordination>> marks = new ArrayList<>();
        // Get king locations
        Coordination bkLoc = state.getKingLocation(Constants.BLACK_COLOR);
        Coordination wkLoc = state.getKingLocation(Constants.WHITE_COLOR);
        // White king is being threaten
        if (state.isWhiteThreaten()) {
            marks.add(new Pair<>(Constants.CHECK_MARK_TAG, wkLoc));
        }
        // Black king is being threaten
        if (state.isBlackThreaten()) {
            marks.add(new Pair<>(Constants.CHECK_MARK_TAG, bkLoc));
        }
        return marks;
    }
}
