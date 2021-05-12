package vn.edu.chessUI.views;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import vn.edu.chessUI.MultiPlayerBroadcastReceiver;
import vn.edu.chessUI.OnPeerSelected;
import vn.edu.chessUI.viewmodels.ChessViewModel;
import vn.edu.chessUI.R;

import vn.edu.Constants;

public class ChessGameFragment extends Fragment {
    private int mode;
    private int userColor;
    private int userTurn;
    private ChessViewModel model;

    private View mReverseButton;
    private View mUndoButton;
    private View mOptionsButton;
    private View mUserStatusBar;
    private View mAgentStatusBar;
    private View mLoadingBar;
    private ImageView mUserFlag;
    private ImageView mAgentFlag;

    private AlertDialog mOptionsDialog;
    private FragmentManager fragmentManager;
    private View mOptionsDialogView;

    // for AI mode
    private int mAILevel = Constants.NOVICE_LEVEL;
    private int mAISide = Constants.WHITE_PERSPECTIVE;

    // for LAN mode
    private ArrayList<String> mPeerNameList;
    private RecyclerView mPeerListRecyclerView;
    private PeerListAdapter mPeerListAdapter;
    private IntentFilter intentFilter;
    private WifiP2pManager.Channel channel;
    private WifiP2pManager manager;
    private MultiPlayerBroadcastReceiver receiver;
    private ArrayList<WifiP2pDevice> peers;
    private WifiP2pManager.PeerListListener peerListListener;
    private WifiP2pManager.ConnectionInfoListener connectionInfoListener;

    public ChessGameFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chess_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get config type for this game
        Bundle args = getArguments();
        assert args != null;
        mode = args.getInt("mode");

        // Setup on click event handler for buttons
        // Setup for reverse function
//        mReverseButton = view.findViewById(R.id.button_reverse);
//        mReverseButton.setOnClickListener(v -> onClickRotate());
        // Setup for undo function
        // Setup for create game function
        mOptionsButton = view.findViewById(R.id.button_more_actions);
        mOptionsButton.setOnClickListener(v -> onClickOptions());
        // other views setup
        mUserStatusBar = view.findViewById(R.id.user_status_bar);
        mAgentStatusBar = view.findViewById(R.id.agent_status_bar);
        mUserFlag = view.findViewById(R.id.user_flag_color);
        mAgentFlag = view.findViewById(R.id.agent_flag_color);
        // loading view
        mLoadingBar = view.findViewById(R.id.loading_panel);

        // Initiate ViewModel which is shared between game play and game board
        model = new ViewModelProvider(this).get(ChessViewModel.class);

        // Setup observer for this fragment
        model.getControl().observe(getViewLifecycleOwner(), event -> {
            // This event object is instance of ChessControl class
            handleOptionHL(event.isOptionHL());
            handleRotateHL(event.isRotateHL());
            handleUserColor(event.getUserColor());
            handleUserTurn(event.getUserTurn());
            handleLoading(event.isLoading());
        });

        // Initiate chess board fragment
        fragmentManager = getChildFragmentManager();
        Fragment chessBoardFragment = new ChessBoardFragment();
        if (savedInstanceState == null) {
            // Init steps: Get current game state.
            // Notify the model to update game state on the observing data
            model.loadModelCheckpoint(mode);
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_chessboard, chessBoardFragment)
                    .commit();
        }

        // Init dialog options base on the game mode
        switch (mode) {
            case Constants.AI_MODE:
                initAIDialog();
                break;
            case Constants.LAN_MODE:
                // Initialize
                manager = (WifiP2pManager) requireActivity().getSystemService(Context.WIFI_P2P_SERVICE);
                channel = manager.initialize(requireContext(), requireActivity().getMainLooper(), null);
                peers = new ArrayList<>();
                mPeerNameList = new ArrayList<>();

                // Set peer list listener for updating list of available peers
                peerListListener = peerList -> {
                    peers.clear();
                    peers.addAll(peerList.getDeviceList());
                    // Update peer name list to UI
                    mPeerNameList.clear();
                    for (WifiP2pDevice device : peers) {
                        mPeerNameList.add(device.deviceName);
                    }
                    // Notify change on adapter
                    mPeerListAdapter.notifyDataSetChanged();
                };

                // Setup connection listener for create server & client thread and decide which side to choose
                connectionInfoListener = info -> {
                    // Automatically close the dialog and start new game
                    if (mOptionsDialog.isShowing())
                        mOptionsDialog.dismiss();
                    // Leave the job for the view model
                    model.newLANGame(info);
                };

                // Setup options dialog
                initLANDialog();
                Log.d("TEST", "Network setup completed!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mode == Constants.LAN_MODE) {
            intentFilter = new IntentFilter();
            // Indicates a change in the Wi-Fi P2P status.
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

            // Indicates a change in the list of available peers.
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

            // Indicates the state of Wi-Fi P2P connectivity has changed.
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

            // Indicates this device's details have changed.
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

            // Register
            receiver = new MultiPlayerBroadcastReceiver(manager, channel, requireActivity());
            receiver.setPeerListListener(peerListListener);
            receiver.setConnectionListener(connectionInfoListener);

            requireActivity().registerReceiver(receiver, intentFilter);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mode == Constants.LAN_MODE) {
            requireActivity().unregisterReceiver(receiver);
//            disconnect();
        } else {
            // AI mode
            model.saveModelCheckpoint();
        }
    }

    private void onClickUndo() {
        model.undo();
    }

    private void initLANDialog() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        mOptionsDialogView = inflater.inflate(R.layout.dialog_lan_options, null);
        mOptionsDialog = new AlertDialog.Builder(requireActivity())
                .setView(mOptionsDialogView)
                .setTitle("Choose a peer")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        manager.stopPeerDiscovery(channel, null);
                    }
                })
                .create();

        // Get recycler views
        mPeerListRecyclerView = mOptionsDialogView.findViewById(R.id.peer_list);

        // Create peer list adapter
        mPeerListAdapter = new PeerListAdapter(getContext(), mPeerNameList, new OnPeerSelected() {
            @Override
            public void onSelected(int i) {
                connect(i);
            }
        });

        // Setup adapter on recycler view
        mPeerListRecyclerView.setAdapter(mPeerListAdapter);
        mPeerListRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
    }

    private void initAIDialog() {
        // Get inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        mOptionsDialogView = inflater.inflate(R.layout.dialog_ai_options, null);
        mOptionsDialog = new AlertDialog.Builder(requireActivity())
                .setView(mOptionsDialogView)
                .setTitle("New AI game")
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do something stupid here!
                        model.newAIGame(mAILevel, mAISide);
                    }
                }).create();

        // Fetch data into spinners inside this dialog
        // Fetch data into levels spinner
        Spinner levelsSpinner = (Spinner) mOptionsDialogView.findViewById(R.id.spinner_levels);
        ArrayAdapter<CharSequence> levelsAdapter = ArrayAdapter.createFromResource(requireActivity(),
                R.array.levels, R.layout.support_simple_spinner_dropdown_item);
        levelsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        levelsSpinner.setAdapter(levelsAdapter);
        levelsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAILevel = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // Fetch data into side spinner
        Spinner sideSpinner = mOptionsDialogView.findViewById(R.id.spinner_side);
        ArrayAdapter<CharSequence> sideAdapter = ArrayAdapter.createFromResource(requireActivity(),
                R.array.side, R.layout.support_simple_spinner_dropdown_item);
        sideAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        sideSpinner.setAdapter(sideAdapter);
        sideSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAISide = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void handleOptionHL(boolean o) {
        // Get the parent view (FrameLayout) of this view for change background color
        View pv = (View) mOptionsButton.getParent();
        if (o) {
            // Turn on highlight!
            pv.setBackgroundColor(getResources().getColor(R.color.highlight));
            // Also turn show new Game selection dialog
            onClickOptions();
        } else {
            // Revert back to transparent
            pv.setBackgroundColor(getResources().getColor(R.color.white));
        }
    }

    private void handleRotateHL(boolean o) {
//        View pv = (View) mReverseButton.getParent();
//        if (o) {
//            // Turn on highlight!
//            pv.setBackgroundColor(getResources().getColor(R.color.teal_200));
//        } else {
//            // Revert back to transparent
//            pv.setBackgroundColor(getResources().getColor(R.color.white));
//        }
    }

    private void handleUserColor(int color) {
        if (color == -1 || color == userColor)
            return;
        mUserFlag.setImageResource(getResources().getIdentifier(
                (color == Constants.WHITE_PERSPECTIVE ? "ic_white_flag" : "ic_black_flag"),
                "drawable", requireActivity().getPackageName()));
        mAgentFlag.setImageResource(getResources().getIdentifier(
                (color == Constants.BLACK_PERSPECTIVE ? "ic_white_flag" : "ic_black_flag"),
                "drawable", requireActivity().getPackageName()));
        userColor = color;
    }

    private void handleUserTurn(int turn) {
        if (turn == -1 || turn == userTurn)
            return;
        mUserStatusBar.setBackgroundColor(getResources()
                .getColor((turn == 1 ? R.color.highlight : R.color.white)));
        mAgentStatusBar.setBackgroundColor(getResources()
                .getColor((turn == 0 ? R.color.highlight : R.color.white)));
        userTurn = turn;
    }

    private void handleLoading(boolean o) {
        if (o) {
            mLoadingBar.setVisibility(View.VISIBLE);
        } else {
            mLoadingBar.setVisibility(View.GONE);
        }
    }

    @SuppressLint("MissingPermission")
    public void onClickOptions() {
        switch (mode) {
            case Constants.AI_MODE:
                Log.d("TEST", "Enter AI mode");
                mOptionsDialog.show();
                break;
            case Constants.LAN_MODE:
                Log.d("TEST", "Enter LAN mode");
                mOptionsDialog.show();
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("TEST", "Found!");
                    }

                    @Override
                    public void onFailure(int reason) {
                        if (reason == WifiP2pManager.BUSY) {
                            Log.d("TEST", "Busy");
                        } else {
                            Log.d("TEST", "Error");
                        }
                    }
                });
                break;
        }
    }

    public void onClickRotate() {
        model.userRotate();
    }

    @SuppressLint("MissingPermission")
    private void connect(int peerID) {
        WifiP2pDevice device = peers.get(peerID);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireActivity(), "Connecting to" + mPeerNameList.get(peerID), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(requireActivity(), "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
        if (manager != null && channel != null) {
            manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && manager != null && channel != null) {
                        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d("TEST", "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d("TEST", "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }
            });
        }
    }
}