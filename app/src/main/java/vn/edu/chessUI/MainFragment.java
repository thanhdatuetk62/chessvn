package vn.edu.chessUI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import vn.edu.Constants;

public class MainFragment extends Fragment {

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View aiButton = view.findViewById(R.id.button_gameplay_ai);
        aiButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("mode", Constants.AI_MODE);
            Navigation.findNavController(view)
                    .navigate(R.id.action_mainFragment_to_chessGameFragment, bundle);
        });
    }
}