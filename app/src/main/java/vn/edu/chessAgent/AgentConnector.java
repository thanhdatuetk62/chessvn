package vn.edu.chessAgent;

import android.net.wifi.p2p.WifiP2pInfo;
import android.text.method.MovementMethod;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

import vn.edu.chessLogic.ChessMovement;
import vn.edu.chessLogic.GameState;

import vn.edu.Constants;

public class AgentConnector {
    /* This class is in charge of handling agent connections include AI and LAN mode */
    private final Executor executor; // Mostly works relate to background tasks, so we need to use Threading
    private int mode; // Use to distinguish between AI mode and LAN mode
    private ChessAI aiAgent; // For AI mode
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public AgentConnector(Executor executor) {
        this.executor = executor;
    }

    public void connectAI(int level, char color) {
        // No need to create listener for AI mode, only cache level variable
        mode = Constants.AI_MODE;
        aiAgent = new ChessAI(level, color);
        Log.d("TEST", "Connect to AI successfully!");
    }

    public boolean connectLAN(WifiP2pInfo info) {
        // Decide which thread to create (Server thread vs. Client thread)
        mode = Constants.LAN_MODE;
        String serverAddress = info.groupOwnerAddress.getHostAddress();
        boolean isServer = info.groupFormed && info.isGroupOwner;

        if (isServer) {
            // Run server thread!
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        ServerSocket serverSocket = new ServerSocket(Constants.PORT);
                        socket = serverSocket.accept();
                        // Create tunnel for send/receive data
                        inputStream = socket.getInputStream();
                        outputStream = socket.getOutputStream();
                        Log.d("TEST", "Stream server completed");
                    } catch (IOException e) {
                        Log.d("TEST", "Error", e);
                    }
                }
            });
        } else {
            // Run client thread!
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Log.d("TEST", "This is Client!");
                    try {
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(serverAddress, Constants.PORT));
                        // Create tunnel for send/receive data
                        inputStream = socket.getInputStream();
                        outputStream = socket.getOutputStream();
                        Log.d("TEST", "Stream client completed");
                    } catch (IOException e) {
                        Log.d("TEST", "Error", e);
                    }
                }
            });
        }

        // All is set
        return isServer;
    }

    private byte[] toBytes(ChessMovement movement) {
        byte[] res = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(movement);
            out.flush();
            res = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    private ChessMovement fromBytes(byte[] bytes) {
        ChessMovement res = null;
        ObjectInput in = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            in = new ObjectInputStream(bis);
            res = (ChessMovement) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public Result<ChessMovement> synchronousMove(GameState state) {
        try {
            ChessMovement movement = null;
            switch (mode) {
                case Constants.AI_MODE:
                    // Ensure is thread safe
                    movement = aiAgent.move(state.copy());
                    break;
                case Constants.LAN_MODE:
                    // Here feed inputStream the data of the last move
                    // and wait to get the move from opponent
                    ChessMovement move = state.getLastMove();
                    // block until InputStream and OutputStream available
                    while (inputStream == null || outputStream == null) ;
                    Log.d("TEST", "IO Streams are ready!");

                    if (move != null) {
                        // Convert object to byte array, copy move to ensure thread safe
                        byte[] bytes = toBytes(move);
                        try {
                            // Send data
                            Log.d("TEST", "User sends data");
                            outputStream.write(bytes);
                        } catch (IOException e) {
                            Log.e("TEST", e.getMessage(), e);
                        }
                    } else {
                        Log.d("TEST", "Wtf this move is null pointer?");
                    }
                    // Listen for the move from opponent
                    try {
                        byte[] buffer = new byte[1024];
                        // Receive data
                        int rep = inputStream.read(buffer);
                        if (rep > 0) {
                            Log.d("TEST", "User receives data");
                            // Convert byte back to object
                            movement = fromBytes(buffer);
                        }
                    } catch (IOException e) {
                        Log.e("TEST", "Error", e);
                    }
            }
            return new Result.Success<>(movement);
        } catch (Exception e) {
            return new Result.Error<>(e);
        }
    }

    public void move(GameState state, AgentCallBack<ChessMovement> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Result<ChessMovement> result = synchronousMove(state);
                    callback.onComplete(result);
                } catch (Exception e) {
                    Result<ChessMovement> errResult = new Result.Error<>(e);
                    callback.onComplete(errResult);
                }
            }
        });
    }

    public void closeConnection() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
