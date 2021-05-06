package vn.edu.chessLogic;

import java.io.Serializable;

public class PairCells implements Serializable {
    public final Coordination src;
    public final Coordination trg;
    public PairCells(Coordination _src, Coordination _trg) {
        src = _src;
        trg = _trg;
    }
}
