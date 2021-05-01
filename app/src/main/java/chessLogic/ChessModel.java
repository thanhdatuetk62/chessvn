package chessLogic;

import androidx.core.util.Pair;

import java.util.ArrayList;

import chessLogic.pieces.BishopPiece;
import chessLogic.pieces.ChessPiece;
import chessLogic.pieces.KingPiece;
import chessLogic.pieces.KnightPiece;
import chessLogic.pieces.PawnPiece;
import chessLogic.pieces.QueenPiece;
import chessLogic.pieces.RookPiece;

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

    public void newGame() {
        curColor = Constants.WHITE_COLOR;
        mState.newGame();
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
        if (x1 == x2 && y1 == y2) {
            // Cannot suicide
            return false;
        }
        ChessPiece piece = getPieceAt(x1, y1);
        if (piece == null) {
            // Cannot move a non-existence piece
            return false;
        }
        if (piece.getColor() != curColor) {
            // Cannot move opponent pieces
            return false;
        }
        // Non-context checking (Only logic)
        if (!piece.canMove(x1, y1, x2, y2, mState))
            return false;
        // Prevent king threaten checking
        // Clone new game state with the next move
        ChessMovement movement = preMove(x1, y1, x2, y2);
        GameState pseudoState = mState.copy();
        // Emulate the move
        pseudoState.move(movement);
        // Get it's king
        Coordination kingCoo = pseudoState.getKingLocation(piece.getColor());
        KingPiece kingPiece = (KingPiece) pseudoState.getPieceAt(kingCoo);
        if (kingPiece.isThreaten(kingCoo.mX, kingCoo.mY, pseudoState)) {
            return false;
        }
        // OK it's safe to move
        return true;
    }

    public ChessMovement preMove(int x1, int y1, int x2, int y2) {
        ChessMovement movement = new ChessMovement();
        // Active is the pair contain two cells which user tapped recently
        movement.setActive(x1, y1, x2, y2);
        // Now have to add real moves for animation (if need)
        // 1. Need to recognize Casting cases [hard code - I think]
        ChessPiece srcPiece = getPieceAt(x1, y1);
        ChessPiece trgPiece = getPieceAt(x2, y2);
        if (srcPiece instanceof KingPiece && trgPiece instanceof RookPiece && !srcPiece.isEnemy(trgPiece)) {
            // This is castling case
            if (x1 != x2) {
                // Conflict? WTF?
                throw new RuntimeException(String.format("Conflict Castling Logic in case (%d, %d) -> (%d, %d)", x1, y1, x2, y2));
            }
            if (y1 - y2 == 3) {
                // King side Castling
                // Move king
                movement.addMove(x1, y1, x1, y1 - 2);
                // Move rook
                movement.addMove(x2, y2, x2, y1 - 1);
            } else {
                // Queen side Castling
                // Move king
                movement.addMove(x1, y1, x1, y1 + 2);
                // Move rook
                movement.addMove(x2, y2, x2, y1 + 1);
            }
            return movement;
        }
        // 2. Need to recognize Promotion cases [still hard code]
        if (srcPiece instanceof PawnPiece) {
            if (srcPiece.getColor() == Constants.WHITE_COLOR && x2 == 0) {
                // White side promotion
                movement.notifyPromotion(Constants.WHITE_COLOR, new Coordination(x2, y2));
            }
            if (srcPiece.getColor() == Constants.BLACK_COLOR && x2 == Constants.SIZE - 1) {
                // Black side promotion
                movement.notifyPromotion(Constants.BLACK_COLOR, new Coordination(x2, y2));
            }
        }
        // 3. Else perform normal move (like active move)
        movement.addMove(x1, y1, x2, y2);
        return movement;
    }

    public ArrayList<Pair<String, Coordination>> postMove(ChessMovement movement) {
        // Override locations state
        // Used to confirm movement from preMove -> update piece location
        mState.move(movement);
        // Switch turn WHITE <-> BLACK || Just for testing
        curColor = (curColor == Constants.WHITE_COLOR ? Constants.BLACK_COLOR : Constants.WHITE_COLOR);
        return getPieceLocations();
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
