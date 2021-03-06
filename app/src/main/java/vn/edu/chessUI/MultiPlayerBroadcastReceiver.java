package vn.edu.chessUI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class MultiPlayerBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private Channel channel;
    private WifiP2pManager.PeerListListener peerListListener;
    private WifiP2pManager.ConnectionInfoListener connectionInfoListener;
    private WifiP2pInfo curP2pInfo;

    public MultiPlayerBroadcastReceiver(WifiP2pManager manager, Channel channel, Activity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
    }

    public void setPeerListListener(WifiP2pManager.PeerListListener listener) {
        peerListListener = listener;
    }

    public void setConnectionListener(WifiP2pManager.ConnectionInfoListener listener) {
        connectionInfoListener = listener;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Ignore for now
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // The peer list has changed! We should probably do something about
            // that.
            manager.requestPeers(channel, peerListListener);
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Connection state changed! We should probably do something about
            // that.
            NetworkInfo newNetworkInfo = intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            WifiP2pInfo newP2pInfo = intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);

            // Request connection info for setup a new game
            if (newNetworkInfo.isConnected()) {
                Log.d("TEST", String.format("groupFormed: %s", newP2pInfo.groupFormed));
                Log.d("TEST", String.format("groupOwnerAddress: %s", newP2pInfo.groupOwnerAddress));
                Log.d("TEST", String.format("isGroupOwner: %s", newP2pInfo.isGroupOwner));

                // Check if new info is the same with the currents
                manager.requestConnectionInfo(channel, connectionInfoListener);

                // Assign new info
                curP2pInfo = newP2pInfo;
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

        }
    }
}
