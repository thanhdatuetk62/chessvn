package vn.edu.chessUI.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import vn.edu.chessUI.viewmodels.ChessViewModel;
import vn.edu.chessUI.Constants;
import vn.edu.chessUI.R;

public class ChessGameFragment extends Fragment {
    private int mode;
    private ChessViewModel model;
    private View mReverseButton;
    private View mOptionsButton;
    private AlertDialog mOptionsDialog;
    private FragmentManager fragmentManager;
    // for AI mode
    private int mAILevel = Constants.NOVICE_LEVEL;
    private int mAISide = Constants.WHITE_PERSPECTIVE;

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
        Log.d("TEST", "Create!");
        // Get config type for this game
        Bundle args = getArguments();
        assert args != null;
        mode = args.getInt("mode");

        // Setup on click event handler for buttons
        // Setup for reverse function
        mReverseButton = view.findViewById(R.id.button_reverse);
        mReverseButton.setOnClickListener(v -> onClickRotate());
        // Setup for create game function
        mOptionsButton = view.findViewById(R.id.button_more_actions);
        mOptionsButton.setOnClickListener(v -> onClickOptions());

        // Initiate ViewModel which is shared between game play and game board
        model = new ViewModelProvider(this).get(ChessViewModel.class);
        // Setup observer for this fragment
        model.getControl().observe(getViewLifecycleOwner(), event -> {
            // This event object is instance of ChessControl class
            handleOptionHL(event.isOptionHL());
            handleRotateHL(event.isRotateHL());
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
                Log.d("TEST", "Cannot initialize LAN Options dialog yet!");
        }
    }

    private void initAIDialog() {
        // Get inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialog = inflater.inflate(R.layout.diaglog_ai_options, null);
        mOptionsDialog = new AlertDialog.Builder(requireActivity())
                .setView(dialog)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do something stupid here!
                        model.newAIGame(mAILevel, mAISide);
                    }
                }).create();

        // Fetch data into spinners inside this dialog
        // Fetch data into levels spinner
        Spinner levelsSpinner = (Spinner) dialog.findViewById(R.id.spinner_levels);
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
        Spinner sideSpinner = (Spinner) dialog.findViewById(R.id.spinner_side);
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
            pv.setBackgroundColor(getResources().getColor(R.color.teal_200));
        } else {
            // Revert back to transparent
            pv.setBackgroundColor(getResources().getColor(R.color.white));
        }
    }

    private void handleRotateHL(boolean o) {
        View pv = (View) mReverseButton.getParent();
        if (o) {
            // Turn on highlight!
            pv.setBackgroundColor(getResources().getColor(R.color.teal_200));
        } else {
            // Revert back to transparent
            pv.setBackgroundColor(getResources().getColor(R.color.white));
        }
    }

    public void onClickOptions() {
        Log.d("TEST", "Not implemented yet!");
        switch (mode) {
            case Constants.AI_MODE:
                Log.d("TEST", "Enter AI mode");
                mOptionsDialog.show();
                break;
            case Constants.LAN_MODE:
                Log.d("TEST", "Enter LAN mode");
        }
    }

    public void onClickRotate() {
        model.userRotate();

    }
}