package vn.edu.chessUI;

import android.app.Application;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChessApplication extends Application {
    public ExecutorService executorService = Executors.newFixedThreadPool(4);
}
