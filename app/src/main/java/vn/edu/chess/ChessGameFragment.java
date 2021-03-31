package vn.edu.chess;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class ChessGameFragment extends Fragment {

    private FragmentManager fragmentManager;

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
        fragmentManager = getChildFragmentManager();
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_chessboard, ChessBoardFragment.class, null)
                    .commit();

        }
        // Setup on click event handler for buttons
        View reverseButton = view.findViewById(R.id.button_reverse);
        reverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotatePerspective(v);
            }
        });
    }

    public void rotatePerspective(View view) {
        ChessBoardFragment chessBoard =
                (ChessBoardFragment) fragmentManager.findFragmentById(R.id.fragment_chessboard);
        assert chessBoard != null;
        chessBoard.rotatePerspective();
    }
}