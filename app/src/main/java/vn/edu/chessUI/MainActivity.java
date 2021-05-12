package vn.edu.chessUI;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
//    private DrawerLayout mDrawer;
    private FragmentManager fragmentManager;
    private NavHostFragment navHostFragment;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Requesting Location Permission")
                        .setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // Add fragment chess board
        fragmentManager = getSupportFragmentManager();

        // Setup navigation system
        setUpNavigationController();

        checkLocationPermission();
    }

    public void setUpNavigationController() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        navHostFragment = (NavHostFragment) fragmentManager.findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        // drawer
//        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        AppBarConfiguration appBarConfiguration =
//                new AppBarConfiguration.Builder(R.id.mainFragment, R.id.chessGameFragment)
//                        .setOpenableLayout(mDrawer)
//                        .build();
//        AppBarConfiguration appBarConfiguration =
//                new AppBarConfiguration.Builder(R.id.mainFragment, R.id.chessGameFragment).build();
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();
        // Link navigation to app bar and drawer
//        NavigationView navView = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

//        NavigationUI.setupWithNavController(navView, navController);
        // set drawer's content
//        navView.setNavigationItemSelectedListener(
//                new NavigationView.OnNavigationItemSelectedListener() {
//                    @Override
//                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                        switch (item.getItemId()) {
//                            case R.id.nav_home:
//                                navController.navigate(R.id.action_global_mainFragment);
//                                mDrawer.closeDrawers();
//                                break;
//                        }
//                        return true;
//                    }
//                });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        switch (item.getItemId()) {
//            case R.id.menu_bar:
//                Toast.makeText(this, "Hello Brat!", Toast.LENGTH_SHORT).show();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

//    @Override
//    public void onConfigurationChanged(@NonNull Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        View decorView = getWindow().getDecorView();
//        ActionBar actionBar = getActionBar();
//        // TODO: Do something to hide system bar and change toolbar's position
//    }
}