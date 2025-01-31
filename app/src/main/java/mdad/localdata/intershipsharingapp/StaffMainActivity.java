package mdad.localdata.intershipsharingapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StaffMainActivity extends AppCompatActivity {
    public static String ipBaseAddress = "http://192.168.0.31/project";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) { // Check to avoid reloading on configuration change
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ViewStatFragment()) // Replace your FrameLayout ID
                    .commit();
        }

        // Set the default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_view_stats);

        // Handle item selection
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_view_stats) {
                    // Navigate to View Items fragment
                    selectedFragment = new ViewStatFragment();

                } else if (item.getItemId() == R.id.nav_new_account) {
                    // Navigate to New Account fragment
                    selectedFragment = new NewAccountFragment();}
                else if (item.getItemId() == R.id.nav_new_post) {
                    // Navigate to New Account fragment
                    Intent intent = new Intent(StaffMainActivity.this, CreatePostActivity.class);
                    startActivity(intent);
                    return true;

                } else if (item.getItemId() == R.id.nav_account) {
                    selectedFragment = new ViewAccountFragment();
                }

                if (selectedFragment != null) {
                    // Replace the fragment in the container
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment) // Replace your FrameLayout ID
                            .commit();
                    return true;
                }

                return false;
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logoutUser() {
        // Clear the login session
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clears all saved session data
        editor.apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to LoginActivity
        navigateToLogin();
    }
}
