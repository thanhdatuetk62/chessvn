package chessLogic;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import java.util.ArrayList;

import chessLogic.pieces.BishopPiece;
import chessLogic.pieces.ChessPiece;
import chessLogic.pieces.KingPiece;
import chessLogic.pieces.KnightPiece;
import chessLogic.pieces.PawnPiece;
import chessLogic.pieces.QueenPiece;
import chessLogic.pieces.RookPiece;

public class GameState {
    private ChessPiece[][] mState = new ChessPiece[Constants.SIZE][Constants.SIZE];
    private boolean bThreaten; // Check if black is currently threaten
    private boolean wThreaten; // Check if white is currently threaten
    private final boolean[] bCastling = new boolean[2]; // Can black castling on King/Queen side
    private final boolean[] wCastling = new boolean[2]; // Can white castling on King/Queen side

    public GameState() {
    }

    public GameState(GameState other) {
        bThreaten = other.bThreaten;
        wThreaten = other.wThreaten;
        for (int i = 0; i < 2; i++) {
            bCastling[i] = other.bCastling[i];
            wCastling[i] = other.wCastling[i];
        }
        for (int r = 0; r < Constants.SIZE; r++) {
            System.arraycopy(other.mState[r], 0, mState[r], 0, Constants.SIZE);
        }
    }

    public void newGame() {
        bThreaten = wThreaten = false;
        bCastling[0] = bCastling[1] = true;
        wCastling[0] = wCastling[1] = true;
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

    public ChessPiece getPieceAt(int r, int c) {
        return mState[r][c];
    }

    public ChessPiece getPieceAt(Coordination coo) {
        return mState[coo.mX][coo.mY];
    }

    public GameState copy() {
        return new GameState(this);
    }

    public void move(ChessMovement movement) {
        for (PairCells p : movement.getAllMoves()) {
            Coordination src = p.src;
            Coordination trg = p.trg;
            int x1 = src.mX, y1 = src.mY, x2 = trg.mX, y2 = trg.mY;
            mState[x2][y2] = mState[x1][y1];
            mState[x1][y1] = null;
        }
        // Perform promotion
        Coordination promoteLocation = movement.getPromoteLocation();
        String promotePiece = movement.getPromotion();
        if (promoteLocation != null && promotePiece != null) {
            int x = promoteLocation.mX, y = promoteLocation.mY;
            ChessPiece piece = createPieceFromCode(movement.getPromotion());
            mState[x][y] = piece;
        }
        // Check threatens and Casting availability
        updateKingStatus(Constants.BLACK_COLOR);
        updateKingStatus(Constants.WHITE_COLOR);
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

    private void updateKingStatus(char color) {
        Coordination kingLoc = getKingLocation(color);
        KingPiece kingPiece = (KingPiece) getPieceAt(kingLoc);
        boolean isThreaten = kingPiece.isThreaten(kingLoc.mX, kingLoc.mY, this);
        // Update side-specific status
        if (color == Constants.WHITE_COLOR) {
            wThreaten = isThreaten;
            if (wThreaten)
                wCastling[0] = wCastling[1] = false;
            else {
                if (wCastling[0]) {
                    // Check rooks status on the corner
                    ChessPiece KSRook = getPieceAt(Constants.SIZE - 1, 0);
                    if (!(KSRook instanceof RookPiece) || !(KSRook.getColor() == color))
                        // KS Castling is not available
                        wCastling[0] = false;
                }
                if (wCastling[1]) {
                    ChessPiece QSRook = getPieceAt(Constants.SIZE - 1, Constants.SIZE - 1);
                    if (!(QSRook instanceof RookPiece) || !(QSRook.getColor() == color))
                        // KS Castling is not available
                        wCastling[1] = false;
                }
            }
        }
        if (color == Constants.BLACK_COLOR) {
            bThreaten = isThreaten;
            if (bThreaten)
                bCastling[0] = bCastling[1] = false;
            else {
                if (bCastling[0]) {
                    // Check rooks status on the corner
                    ChessPiece KSRook = getPieceAt(0, 0);
                    if (!(KSRook instanceof RookPiece) || !(KSRook.getColor() == color))
                        // KS Castling is not available
                        bCastling[0] = false;
                }
                if (bCastling[1]) {
                    ChessPiece QSRook = getPieceAt(0, Constants.SIZE - 1);
                    if (!(QSRook instanceof RookPiece) || !(QSRook.getColor() == color))
                        // KS Castling is not available
                        bCastling[1] = false;
                }
            }
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
        Coordination coo = null;
        for (int r = 0; r < Constants.SIZE; r++) {
            for (int c = 0; c < Constants.SIZE; c++) {
                ChessPiece piece = mState[r][c];
                if (piece != null) {
                    if (piece instanceof KingPiece && piece.getColor() == color) {
                        coo = new Coordination(r, c);
                        break;
                    }
                }
            }
        }
        return coo;
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
}
