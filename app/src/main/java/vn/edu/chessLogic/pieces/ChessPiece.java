package vn.edu.chessLogic.pieces;

import androidx.core.util.Pair;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import vn.edu.chessLogic.Coordination;
import vn.edu.chessLogic.GameState;
import vn.edu.Constants;

public abstract class ChessPiece implements Serializable {
    protected char mColor;
    protected char mPieceName;
    public int d;
    public int t;
    public int p;
    public int m;

    public String pieceCode() {
        return "" + mColor + mPieceName;
    }

    public char getOpponentColor() {
        return (mColor == Constants.WHITE_COLOR ? Constants.BLACK_COLOR : Constants.WHITE_COLOR);
    }

    public boolean isEnemy(ChessPiece other) {
        return mColor != other.mColor;
    }

    public char getColor() {
        return mColor;
    }

    public abstract boolean canMove(int x1, int y1, int x2, int y2, GameState state);

    public abstract ArrayList<Pair<Integer, Integer>> allPossibleMoves(int x, int y, GameState state);

    private void onPawnHostage(int x, int y, GameState state, ArrayList<Pair<Integer, Integer>> store) {
        if (x + 1 < Constants.SIZE && x + 1 >= 0) {
            // White hostage
            if (y - 1 < Constants.SIZE && y - 1 >= 0) {
                ChessPiece piece = state.getPieceAt(x + 1, y - 1);
                if (piece instanceof PawnPiece && piece.getColor() == Constants.WHITE_COLOR)
                    store.add(new Pair<>(x + 1, y - 1));
            }
            if (y + 1 < Constants.SIZE && y + 1 >= 0) {
                ChessPiece piece = state.getPieceAt(x + 1, y + 1);
                if (piece instanceof PawnPiece && piece.getColor() == Constants.WHITE_COLOR)
                    store.add(new Pair<>(x + 1, y + 1));
            }
        }
        if (x - 1 < Constants.SIZE && x - 1 >= 0) {
            // Black hostage
            if (y - 1 < Constants.SIZE && y - 1 >= 0) {
                ChessPiece piece = state.getPieceAt(x - 1, y - 1);
                if (piece instanceof PawnPiece && piece.getColor() == Constants.BLACK_COLOR)
                    store.add(new Pair<>(x - 1, y - 1));
            }
            if (y + 1 < Constants.SIZE && y + 1 >= 0) {
                ChessPiece piece = state.getPieceAt(x - 1, y + 1);
                if (piece instanceof PawnPiece && piece.getColor() == Constants.BLACK_COLOR)
                    store.add(new Pair<>(x - 1, y + 1));
            }
        }
    }

    private void onQueenHostage(int x, int y, GameState state, ArrayList<Pair<Integer, Integer>> store) {
        int[] dx = {-1, 0, 1, 0, 1, 1, -1, -1};
        int[] dy = {0, 1, 0, -1, 1, -1, 1, -1};

        for (int i = 0; i < 8; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            for (; nx >= 0 && nx < Constants.SIZE && ny >= 0 && ny < Constants.SIZE; nx += dx[i], ny += dy[i]) {
                ChessPiece piece = state.getPieceAt(nx, ny);
                if (piece != null) {
                    if (piece instanceof QueenPiece) {
                        store.add(new Pair<>(nx, ny));
                    }
                    break;
                }
            }
        }
    }

    private void onRookHostage(int x, int y, GameState state, ArrayList<Pair<Integer, Integer>> store) {
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};

        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            for (; nx >= 0 && nx < Constants.SIZE && ny >= 0 && ny < Constants.SIZE; nx += dx[i], ny += dy[i]) {
                ChessPiece piece = state.getPieceAt(nx, ny);
                if (piece != null) {
                    if (piece instanceof RookPiece) {
                        store.add(new Pair<>(nx, ny));
                    }
                    break;
                }
            }
        }
    }

    private void onBishopHostage(int x, int y, GameState state, ArrayList<Pair<Integer, Integer>> store) {
        int[] dx = {1, 1, -1, -1};
        int[] dy = {1, -1, 1, -1};

        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            for (; nx >= 0 && nx < Constants.SIZE && ny >= 0 && ny < Constants.SIZE; nx += dx[i], ny += dy[i]) {
                ChessPiece piece = state.getPieceAt(nx, ny);
                if (piece != null) {
                    if (piece instanceof BishopPiece) {
                        store.add(new Pair<>(nx, ny));
                    }
                    break;
                }
            }
        }
    }

    private void onKnightHostage(int x, int y, GameState state, ArrayList<Pair<Integer, Integer>> store) {
        int[] dx = {1, 1, 2, 2, -1, -1, -2, -2};
        int[] dy = {2, -2, 1, -1, 2, -2, 1, -1};

        for (int i = 0; i < 8; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx >= 0 && nx < Constants.SIZE && ny >= 0 && ny < Constants.SIZE) {
                ChessPiece piece = state.getPieceAt(nx, ny);
                if (piece instanceof KnightPiece)
                    store.add(new Pair<>(nx, ny));
            }
        }
    }

    public ArrayList<Pair<Integer, Integer>> onHostage(int x, int y, GameState state) {
        ArrayList<Pair<Integer, Integer>> src = new ArrayList<>();
        // Pawn attacks
        onPawnHostage(x, y, state, src);
        // Queen attacks
        onQueenHostage(x, y, state, src);
        // Rook attacks
        onRookHostage(x, y, state, src);
        // Bishop attacks
        onBishopHostage(x, y, state, src);
        // Knight attacks
        onKnightHostage(x, y, state, src);
        return src;
    }
}
