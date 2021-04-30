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

public class GameState {
    private ChessPiece[][] mState = new ChessPiece[Constants.SIZE][Constants.SIZE];

    public GameState() {
    }

    public GameState(ChessPiece[][] state) {
        int sz = Constants.SIZE;
        for (int r = 0; r < sz; r++) {
            for (int c = 0; c < sz; c++) {
                mState[r][c] = state[r][c];
            }
        }
    }

    public void newGame() {
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
        return new GameState(mState);
    }

    public void move(ChessMovement movement) {
        for (PairCells p : movement.getAllMoves()) {
            Coordination src = p.src;
            Coordination trg = p.trg;
            int x1 = src.mX, y1 = src.mY, x2 = trg.mX, y2 = trg.mY;
            mState[x2][y2] = mState[x1][y1];
            mState[x1][y1] = null;
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
}
