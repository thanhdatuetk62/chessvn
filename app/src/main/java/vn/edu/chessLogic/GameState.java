package vn.edu.chessLogic;

import android.text.method.MovementMethod;
import android.util.Log;

import androidx.core.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;

import vn.edu.chessLogic.pieces.BishopPiece;
import vn.edu.chessLogic.pieces.ChessPiece;
import vn.edu.chessLogic.pieces.KingPiece;
import vn.edu.chessLogic.pieces.KnightPiece;
import vn.edu.chessLogic.pieces.PawnPiece;
import vn.edu.chessLogic.pieces.QueenPiece;
import vn.edu.chessLogic.pieces.RookPiece;

import vn.edu.Constants;

public class GameState implements Serializable {
    private char mColor;
    private int bThreatenCnt;
    private int wThreatenCnt;
    private boolean bThreaten; // Check if black is currently threaten
    private boolean wThreaten; // Check if white is currently threaten
    private Stack<ChessMovement> mHistory;
    private final boolean[] bCastling = new boolean[2]; // Can black castling on King/Queen side
    private final boolean[] wCastling = new boolean[2]; // Can white castling on King/Queen side
    private ChessPiece[][] mState = new ChessPiece[Constants.SIZE][Constants.SIZE];
    private int[] mCornerCnt = new int[4];
    private Coordination wkLocation;
    private Coordination bkLocation;

    public GameState() {
    }

    public GameState(GameState other) {
        mColor = other.mColor;
        bThreatenCnt = other.bThreatenCnt;
        wThreatenCnt = other.wThreatenCnt;
        bThreaten = other.bThreaten;
        wThreaten = other.wThreaten;
        wkLocation = new Coordination(other.wkLocation.mX, other.wkLocation.mY);
        bkLocation = new Coordination(other.bkLocation.mX, other.bkLocation.mY);

        mHistory = new Stack<>(); // Cannot track the history so that can avoid unexpected modification
        // Add the last movement :)) (dirty trick)
        if (!other.mHistory.isEmpty()) {
            final ChessMovement movement = other.mHistory.peek().copy();
            mHistory.push(movement);
        }
        for (int i = 0; i < 2; i++) {
            bCastling[i] = other.bCastling[i];
            wCastling[i] = other.wCastling[i];
        }
        for (int r = 0; r < Constants.SIZE; r++) {
            System.arraycopy(other.mState[r], 0, mState[r], 0, Constants.SIZE);
        }
        for (int i = 0; i < 4; i++)
            mCornerCnt[i] = other.mCornerCnt[i];
    }

    public void newGame() {
        mColor = Constants.WHITE_COLOR;
        bThreatenCnt = wThreatenCnt = 0;
        bThreaten = wThreaten = false;
        bCastling[0] = bCastling[1] = true;
        wCastling[0] = wCastling[1] = true;
        wkLocation = new Coordination(7, 3);
        bkLocation = new Coordination(0, 3);
        mCornerCnt = new int[4];
        mHistory = new Stack<>();
        mState = new ChessPiece[][]{
                {
                        // First rank
                        new RookPiece(Constants.BLACK_COLOR),
                        new KnightPiece(Constants.BLACK_COLOR),
                        new BishopPiece(Constants.BLACK_COLOR),
                        new KingPiece(Constants.BLACK_COLOR),
                        new QueenPiece(Constants.BLACK_COLOR),
                        new BishopPiece(Constants.BLACK_COLOR),
                        new KnightPiece(Constants.BLACK_COLOR),
                        new RookPiece(Constants.BLACK_COLOR)
                },
                {
                        // Second rank
                        new PawnPiece(Constants.BLACK_COLOR),
                        new PawnPiece(Constants.BLACK_COLOR),
                        new PawnPiece(Constants.BLACK_COLOR),
                        new PawnPiece(Constants.BLACK_COLOR),
                        new PawnPiece(Constants.BLACK_COLOR),
                        new PawnPiece(Constants.BLACK_COLOR),
                        new PawnPiece(Constants.BLACK_COLOR),
                        new PawnPiece(Constants.BLACK_COLOR)
                },
                // 3th -> 6th rank
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {
                        // Seventh rank
                        new PawnPiece(Constants.WHITE_COLOR),
                        new PawnPiece(Constants.WHITE_COLOR),
                        new PawnPiece(Constants.WHITE_COLOR),
                        new PawnPiece(Constants.WHITE_COLOR),
                        new PawnPiece(Constants.WHITE_COLOR),
                        new PawnPiece(Constants.WHITE_COLOR),
                        new PawnPiece(Constants.WHITE_COLOR),
                        new PawnPiece(Constants.WHITE_COLOR)
                },
                {
                        // Eighth rank
                        new RookPiece(Constants.WHITE_COLOR),
                        new KnightPiece(Constants.WHITE_COLOR),
                        new BishopPiece(Constants.WHITE_COLOR),
                        new KingPiece(Constants.WHITE_COLOR),
                        new QueenPiece(Constants.WHITE_COLOR),
                        new BishopPiece(Constants.WHITE_COLOR),
                        new KnightPiece(Constants.WHITE_COLOR),
                        new RookPiece(Constants.WHITE_COLOR)
                }
        };
    }

    public char getCurrentColor() {
        return mColor;
    }

    public ChessPiece getPieceAt(int r, int c) {
        return mState[r][c];
    }

    public ChessPiece getPieceAt(Coordination coo) {
        return mState[coo.mX][coo.mY];
    }

    public GameState copy() {
        return new GameState(this);
    }

    public boolean canMove(int x1, int y1, int x2, int y2) {
        if (x1 == x2 && y1 == y2) {
            // Cannot suicide
            return false;
        }
        ChessPiece piece = getPieceAt(x1, y1);
        if (piece == null) {
            // Cannot move a non-existence piece
            return false;
        }
        if (piece.getColor() != mColor) {
            // Cannot move opponent pieces
            return false;
        }
        // Non-context checking (Only logic)
        if (!piece.canMove(x1, y1, x2, y2, this))
            return false;

        // Init return var
        boolean res = true;

        // Prevent king threaten checking
        ChessMovement movement = createMovement(x1, y1, x2, y2);
        // Emulate the move, will backtrack to preserve origin state
        move(movement);

        // Get it's king
        if ((piece.getColor() == Constants.WHITE_COLOR && wThreaten)
                || (piece.getColor() == Constants.BLACK_COLOR && bThreaten)) {
            res = false;
        }
        // Back
        undo();
        return res;
    }

    private ArrayList<ChessMovement> replicatePromotion(ChessMovement movement) {
        char[] promoteOptions = {'Q', 'R', 'N', 'B'};
        char promoteSide = movement.getPromoteSide();
        ArrayList<ChessMovement> replicates = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ChessMovement newMovement = new ChessMovement(movement);
            newMovement.setPromotion("" + promoteSide + promoteOptions[i]);
            replicates.add(newMovement);
        }
        return replicates;
    }

    public String debug() {
        StringBuilder res = new StringBuilder();
        res.append("ASCII Board\n");
        for (int i = 0; i < Constants.SIZE; i++) {
            for (int j = 0; j < Constants.SIZE; j++) {
                ChessPiece piece = mState[i][j];
                if (piece == null) {
                    res.append("--");
                } else {
                    res.append(piece.pieceCode());
                }
                res.append(" ");
            }
            res.append("\n");
        }
        return res.toString();
    }

    public ArrayList<Pair<Integer, Integer>> getAllPossibleMoves(int x, int y) {
        ArrayList<Pair<Integer, Integer>> allMoves = new ArrayList<>();
        ChessPiece srcPiece = getPieceAt(x, y);
        if (srcPiece == null)
            return allMoves;
        for (Pair<Integer, Integer> p : srcPiece.allPossibleMoves(x, y, this)) {
            ChessMovement movement = createMovement(x, y, p.first, p.second);
            boolean isKingInDanger = false;
            move(movement);
            // Get it's king
            if ((srcPiece.getColor() == Constants.WHITE_COLOR && wThreaten)
                    || (srcPiece.getColor() == Constants.BLACK_COLOR && bThreaten)) {
                isKingInDanger = true;
            }
            // Back
            undo();
            if (isKingInDanger)
                // Cannot move because it is dangerous
                continue;
            // OK can move now
            allMoves.add(p);
        }
        return allMoves;
    }

    public ArrayList<ChessMovement> getAllPossibleMoves() {
        ArrayList<ChessMovement> allMoves = new ArrayList<>();
        ArrayList<Coordination> pieceLocations = getPieceLocations(mColor);
        String mapBefore = debug();
        for (Coordination coo : pieceLocations) {
            int x1 = coo.mX, y1 = coo.mY;
            ChessPiece piece = getPieceAt(x1, y1);
            if (piece == null) {
                Log.d("TEST", String.format("Null pos: %d, %d", x1, y1));
                Log.d("TEST", "Null map (Before): " + mapBefore);
                Log.d("TEST", "Null map (After): " + debug());
            }
            ArrayList<Pair<Integer, Integer>> moves = piece.allPossibleMoves(x1, y1, this);
            for (Pair<Integer, Integer> p : moves) {
                int x2 = p.first, y2 = p.second;
                ChessMovement movement = createMovement(x1, y1, x2, y2);

                // Prevent checkmate cf
                //Stimulate the move
                boolean isKingInDanger = false;
                move(movement);
                // Get it's king
                if ((piece.getColor() == Constants.WHITE_COLOR && wThreaten)
                        || (piece.getColor() == Constants.BLACK_COLOR && bThreaten)) {
                    isKingInDanger = true;
                }
                // Back
                undo();
                if (isKingInDanger)
                    // Cannot move because it is dangerous
                    continue;

                // Check if there is a promotion,
                // if true, we will replicate this movement obj by 4 to stimulate all possible promotion options
                char promoteSide = movement.getPromoteSide();
                if (promoteSide == Constants.WHITE_COLOR || promoteSide == Constants.BLACK_COLOR) {
                    // Has promotion
                    allMoves.addAll(replicatePromotion(movement));
                } else
                    allMoves.add(movement);
            }
        }
        return allMoves;
    }

    public void move(ChessMovement movement) {
        for (PairCells p : movement.getAllMoves()) {
            Coordination src = p.src;
            Coordination trg = p.trg;
            int x1 = src.mX, y1 = src.mY, x2 = trg.mX, y2 = trg.mY;

            mState[x2][y2] = mState[x1][y1];
            mState[x1][y1] = null;

            if (mState[x2][y2] instanceof KingPiece) {
                if (mState[x2][y2].getColor() == Constants.WHITE_COLOR) {
                    wkLocation = new Coordination(x2, y2);
                    wThreatenCnt++;
                } else {
                    bkLocation = new Coordination(x2, y2);
                    bThreatenCnt++;
                }
            }

            for (int i = 0; i < 4; i++) {
                int x = Constants.ROOK_X[i];
                int y = Constants.ROOK_Y[i];
                if (x == x1 && y == y1) {
                    mCornerCnt[i]++;
                }
                if (x == x2 && y == y2) {
                    mCornerCnt[i]++;
                }
            }
        }
        // Perform promotion - Can only be used in case of user interactions,
        // will be ignored when performing suggestions creating or agent interactions
        Coordination promoteLocation = movement.getPromoteLocation();
        String promotePiece = movement.getPromotion();
        if (promoteLocation != null && promotePiece != null) {
            int x = promoteLocation.mX, y = promoteLocation.mY;
            ChessPiece piece = createPieceFromCode(movement.getPromotion());
            mState[x][y] = piece;
        }
        // Check threatens and Casting availability
        Coordination wkLoc = getKingLocation(Constants.WHITE_COLOR);
        Coordination bkLoc = getKingLocation(Constants.BLACK_COLOR);
        KingPiece wkPiece = (KingPiece) getPieceAt(wkLoc);
        KingPiece bkPiece = (KingPiece) getPieceAt(bkLoc);
        wThreaten = wkPiece.isThreaten(wkLoc.mX, wkLoc.mY, this);
        bThreaten = bkPiece.isThreaten(bkLoc.mX, bkLoc.mY, this);
        // Update cnt
        wThreatenCnt += (wThreaten ? 1 : 0);
        bThreatenCnt += (bThreaten ? 1 : 0);
        // Update castling availability
        wCastling[0] = (wThreatenCnt == 0 && mCornerCnt[2] == 0);
        wCastling[1] = (wThreatenCnt == 0 && mCornerCnt[3] == 0);
        bCastling[0] = (bThreatenCnt == 0 && mCornerCnt[0] == 0);
        bCastling[1] = (bThreatenCnt == 0 && mCornerCnt[1] == 0);
        // Append move into history
        mHistory.push(movement);
        // Switch turn WHITE <-> BLACK
        mColor = (mColor == Constants.WHITE_COLOR ? Constants.BLACK_COLOR : Constants.WHITE_COLOR);
    }

    public ChessMovement createMovement(int x1, int y1, int x2, int y2) {
        ChessMovement movement = new ChessMovement();
        // Active is the pair contain two cells which user tapped recently
        movement.setActive(x1, y1, x2, y2);
        // Now have to add real moves for animation (if need)
        // 1. Need to recognize Casting cases [hard code - I think]
        ChessPiece srcPiece = getPieceAt(x1, y1);
        ChessPiece trgPiece = getPieceAt(x2, y2);
        if (srcPiece instanceof KingPiece && trgPiece instanceof RookPiece && !srcPiece.isEnemy(trgPiece)) {
            // This is castling case
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
        // 4. Set captured piece
        if (trgPiece != null && srcPiece.isEnemy(trgPiece)) {
            movement.setCapturedPiece(trgPiece, x2, y2);
        }
        return movement;
    }

    public ChessMovement undo() {
        // Pop the last movement item from history and revert back to previous state
        if (mHistory.empty()) {
            Log.d("TEST", "Cannot undo because history stack is empty");
            return null;
        }
//        Log.d("TEST", "Before: " + debug());

        ChessMovement movement = mHistory.pop();

        PairCells ac = movement.getActive();
        Coordination sac = ac.src;
        Coordination tac = ac.trg;
//        Log.d("TEST", String.format("Revert move from (%d, %d) -> (%d, %d)", sac.mX, sac.mY, tac.mX, tac.mY));
        // Update cnt
        wThreatenCnt -= (wThreaten ? 1 : 0);
        bThreatenCnt -= (bThreaten ? 1 : 0);

        // Perform REVERT promotion
        // will be ignored when performing suggestions creating or agent interactions
        Coordination promoteLocation = movement.getPromoteLocation();
        String promotePiece = movement.getPromotion();
        if (promoteLocation != null && promotePiece != null) {
            int x = promoteLocation.mX, y = promoteLocation.mY;
            mState[x][y] = new PawnPiece(movement.getPromoteSide());
        }

        // Revert movements
        for (PairCells p : movement.getAllMoves()) {
            Coordination src = p.src;
            Coordination trg = p.trg;
            int x1 = src.mX, y1 = src.mY, x2 = trg.mX, y2 = trg.mY;
            mState[x1][y1] = mState[x2][y2];
            mState[x2][y2] = null;
            if (mState[x1][y1] instanceof KingPiece) {
                if (mState[x1][y1].getColor() == Constants.WHITE_COLOR) {
                    wkLocation = new Coordination(x1, y1);
                    wThreatenCnt--;
                } else {
                    bkLocation = new Coordination(x1, y1);
                    bThreatenCnt--;
                }
            }

            // Revert corner counters
            for (int i = 0; i < 4; i++) {
                int x = Constants.ROOK_X[i];
                int y = Constants.ROOK_Y[i];
                if (x == x1 && y == y1) {
                    mCornerCnt[i]--;
                }
                if (x == x2 && y == y2) {
                    mCornerCnt[i]--;
                }
            }
        }

        // Free captured piece
        ChessPiece capturedPiece = movement.getCapturedPiece();
        Coordination capturedCoo = movement.getCapturedCoordination();
        if (capturedPiece != null && capturedCoo != null) {
            mState[capturedCoo.mX][capturedCoo.mY] = capturedPiece;
        }

        // Revert threaten
        Coordination wkLoc = getKingLocation(Constants.WHITE_COLOR);
        Coordination bkLoc = getKingLocation(Constants.BLACK_COLOR);
        KingPiece wkPiece = (KingPiece) getPieceAt(wkLoc);
        KingPiece bkPiece = (KingPiece) getPieceAt(bkLoc);

        try {
            wThreaten = wkPiece.isThreaten(wkLoc.mX, wkLoc.mY, this);
            bThreaten = bkPiece.isThreaten(bkLoc.mX, bkLoc.mY, this);
        } catch (NullPointerException e) {
            Log.d("TEST", String.valueOf(e), e);
            Log.d("TEST", debug());
        }

        // Update castling availability
        wCastling[0] = (wThreatenCnt == 0 && mCornerCnt[2] == 0);
        wCastling[1] = (wThreatenCnt == 0 && mCornerCnt[3] == 0);
        bCastling[0] = (bThreatenCnt == 0 && mCornerCnt[0] == 0);
        bCastling[1] = (bThreatenCnt == 0 && mCornerCnt[1] == 0);

        // Switch turn WHITE <-> BLACK
        mColor = (mColor == Constants.WHITE_COLOR ? Constants.BLACK_COLOR : Constants.WHITE_COLOR);

//        Log.d("TEST", "After: " + debug());
        return movement;
    }

    private ChessPiece createPieceFromCode(String pieceCode) {
        char color = pieceCode.charAt(0);
        char type = pieceCode.charAt(1);
        switch (type) {
            case Constants.QUEEN:
                return new QueenPiece(color);
            case Constants.BISHOP:
                return new BishopPiece(color);
            case Constants.ROOK:
                return new RookPiece(color);
            default:
                return new KnightPiece(color);
        }
    }

    public ArrayList<Coordination> getPieceLocations() {
        // Get all pieces in the board
        ArrayList<Coordination> pieces = new ArrayList<>();
        for (int r = 0; r < Constants.SIZE; r++) {
            for (int c = 0; c < Constants.SIZE; c++) {
                if (mState[r][c] != null) {
                    pieces.add(new Coordination(r, c));
                }
            }
        }
        return pieces;
    }

    public ArrayList<Coordination> getPieceLocations(char color) {
        // Get pieces in the board by color
        ArrayList<Coordination> pieces = new ArrayList<>();
        for (int r = 0; r < Constants.SIZE; r++) {
            for (int c = 0; c < Constants.SIZE; c++) {
                if (mState[r][c] != null && mState[r][c].getColor() == color) {
                    pieces.add(new Coordination(r, c));
                }
            }
        }
        return pieces;
    }

    protected Coordination getKingLocation(char color) {
        if (color == Constants.WHITE_COLOR)
            return wkLocation;
        else
            return bkLocation;
    }

    public boolean canCastling(char color, int side) {
        return (color == Constants.WHITE_COLOR ? wCastling[side] : bCastling[side]);
    }

    public boolean isBlackThreaten() {
        return bThreaten;
    }

    public boolean isWhiteThreaten() {
        return wThreaten;
    }

    public PairCells getLastActive() {
        if (mHistory.empty())
            return null;
        return mHistory.peek().getActive();
    }

    public final ChessMovement getLastMove() {
        if (mHistory.isEmpty())
            return null;
        return mHistory.peek();
    }

    public int isGameOver() {
        if (getAllPossibleMoves().size() > 0) {
            Log.d("TEST", "WTF?");
            return Constants.NOT_FINISH;
        }
        if ((mColor == Constants.WHITE_COLOR && wThreaten)
                || (mColor == Constants.BLACK_COLOR && bThreaten)) {
            // Checkmate
            return Constants.CHECKMATE;
        } else
            // Stalemate
            return Constants.STALEMATE;
    }

}
