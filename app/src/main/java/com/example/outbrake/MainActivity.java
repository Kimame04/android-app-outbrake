package com.example.outbrake;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.outbrake.fragments.MapsFragment;
import com.example.outbrake.fragments.NewsFragment;
import com.example.outbrake.fragments.ThermometerFragment;
import com.example.outbrake.fragments.TravelFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private static FirebaseDatabase firebaseDatabase;
    private static FirebaseAuth firebaseAuth;
    private static FirebaseUser firebaseUser;
    private static String displayName = "";
    private static FrameLayout frameLayout;
    private FirebaseAuth.AuthStateListener authStateListener;
    private Context context;
    private RelativeLayout relativeLayout;
    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.temperature:
                            selectedFragment = new ThermometerFragment();
                            break;
                        case R.id.news:
                            selectedFragment = new NewsFragment();
                            break;
                        case R.id.travel:
                            selectedFragment = new TravelFragment();
                            break;
                        case R.id.cluster:
                            selectedFragment = new MapsFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commitAllowingStateLoss();
                    return true;
                }
            };

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static void showNoConnectionSnackBar() {
        final Snackbar snackbar = Snackbar.make(frameLayout, "No Internet Connection", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Ok", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    public static FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }


    public static FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public static String getDisplayName() {
        return displayName;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        frameLayout = findViewById(R.id.fragment_container);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    firebaseUser = firebaseAuth.getCurrentUser();
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    switch (sharedPreferences.getString("settings_select_default", "")) {
                        case "News":
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NewsFragment()).commitAllowingStateLoss();
                            bottomNavigationView.setSelectedItemId(R.id.news);
                            break;
                        case "Declare Travel":
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TravelFragment()).commitAllowingStateLoss();
                            bottomNavigationView.setSelectedItemId(R.id.travel);
                            break;
                        case "Log Temperature":
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ThermometerFragment()).commitAllowingStateLoss();
                            bottomNavigationView.setSelectedItemId(R.id.temperature);
                            break;
                        case "Clusters":
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapsFragment()).commitAllowingStateLoss();
                            bottomNavigationView.setSelectedItemId(R.id.cluster);
                            break;
                    }

                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder().setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
        firebaseAuth.addAuthStateListener(authStateListener);

        relativeLayout = findViewById(R.id.main_relative_layout);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(context, "Successfully signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(context, "Sign in cancelled.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                AuthUI.getInstance().signOut(context);
                return true;
            case R.id.settings:
                startActivity(new Intent(context, SettingsActivity.class));
                return true;
            case R.id.feedback:
                String[] TO = {"h1710095@nushigh.edu.sg"};
                String[] CC = {};
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL,TO);
                emailIntent.putExtra(Intent.EXTRA_CC,CC);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Put a meaningful subject name here");
                emailIntent.putExtra(Intent.EXTRA_TEXT,"Insert your feedback here. Provide feedback constructively!");
                try{
                    startActivity(Intent.createChooser(emailIntent, "Send mail"));
                } catch(android.content.ActivityNotFoundException e){
                    Snackbar.make(relativeLayout,"You have no email client installed.", BaseTransientBottomBar.LENGTH_SHORT).show(); }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

