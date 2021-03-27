package vn.edu.uetchess;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {

    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_chessboard, ChessBoardFragment.class, null)
                    .commit();
        }
    }

    public void testPlacePiece1(View v) {
        ChessBoardFragment chessBoard =
                (ChessBoardFragment) fragmentManager.findFragmentById(R.id.fragment_chessboard);
        assert chessBoard != null;
        chessBoard.placePiece("wQ", "a7");
    }

    public void testPlacePiece2(View v) {
        ChessBoardFragment chessBoard =
                (ChessBoardFragment) fragmentManager.findFragmentById(R.id.fragment_chessboard);
        assert chessBoard != null;
        chessBoard.placePiece("bN", "e6");
    }

    public void testMovePiece(View view) {
        ChessBoardFragment chessBoard =
                (ChessBoardFragment) fragmentManager.findFragmentById(R.id.fragment_chessboard);
        assert chessBoard != null;
        chessBoard.move("a7", "e6");
    }


    public void testRotatePerspective(View view) {
        ChessBoardFragment chessBoard =
                (ChessBoardFragment) fragmentManager.findFragmentById(R.id.fragment_chessboard);
        assert chessBoard != null;
        chessBoard.rotatePerspective();
    }
}