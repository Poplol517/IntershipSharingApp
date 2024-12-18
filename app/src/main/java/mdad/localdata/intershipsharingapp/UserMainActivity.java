package mdad.localdata.intershipsharingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class UserMainActivity extends AppCompatActivity {
    private LinearLayout lv;  // Reference to LinearLayout to dynamically add TextViews
    private SearchView searchView;
    private static String url_all_internship = StaffMainActivity.ipBaseAddress + "/get_all_internship.php";
    private Map<Integer, Fragment> fragmentMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLoginSession();
        setContentView(R.layout.activity_user_main);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        // Initialize the fragment map
        fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.menu_home, new HomeFragment());
        //fragmentMap.put(R.id.menu_account, new AllQuestionFragment());
        fragmentMap.put(R.id.menu_create_post, new CreatePostFragment());
        searchView = findViewById(R.id.search_view);

        // Load the default fragment
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }

        // Handle bottom navigation item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_logout) {
                logoutUser();  // Handle logout directly
                return true;
            }
            Fragment selectedFragment = fragmentMap.get(item.getItemId());
            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
                return true;
            }
            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search query submission
                Toast.makeText(UserMainActivity.this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
                // You can add logic to filter content in fragments or make API calls here
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle live search suggestions or filtering (optional)
                return false;
            }
        });
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void checkLoginSession() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            String roleId = sharedPreferences.getString("roleId", "");

            // Ensure this activity is accessed only by user roles
            if (roleId.equals("1") || roleId.equals("2")) {
                // Allow access for valid user roles
                return;
            } else if (roleId.equals("3")) {
                // Redirect to StaffMainActivity for staff roles
                Intent intent = new Intent(this, StaffMainActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Handle undefined roles (optional)
                Toast.makeText(this, "Undefined role, unable to navigate.", Toast.LENGTH_SHORT).show();
                navigateToLogin();
            }
        } else {
            // Redirect to LoginActivity if not logged in
            navigateToLogin();
        }
    }

    /**
     * Redirects to the LoginActivity and clears the current activity.
     */
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
        editor.clear();  // Clears all saved session data
        editor.apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to LoginActivity
        navigateToLogin();
    }
}

