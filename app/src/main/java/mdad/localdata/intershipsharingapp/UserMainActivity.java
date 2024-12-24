package mdad.localdata.intershipsharingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class UserMainActivity extends AppCompatActivity {
    private SearchView searchView;
    private Toolbar toolbar;
    private ImageView accountIcon;
    private static final String url_all_internship = StaffMainActivity.ipBaseAddress + "/get_all_internship.php";
    private static final String url_all_user = StaffMainActivity.ipBaseAddress + "/get_all_user.php";
    private Map<Integer, Fragment> fragmentMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLoginSession();
        setContentView(R.layout.activity_user_main);

        // Initialize toolbar and account icon
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        accountIcon = findViewById(R.id.account_icon);
        accountIcon.setOnClickListener(v -> showAccountDropdown(accountIcon));

        // Initialize bottom navigation and fragments
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.menu_home, new HomeFragment());
        // Add other fragments if necessary
        searchView = findViewById(R.id.search_view);

        // Load the default fragment
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }

        // Handle bottom navigation item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_logout) {
                logoutUser();
                return true;
            } else if (item.getItemId() == R.id.menu_create_post) {
                // Launch CreatePostActivity as an Intent
                Intent intent = new Intent(UserMainActivity.this, CreatePostActivity.class);
                startActivity(intent);
                return true;
            }
            Fragment selectedFragment = fragmentMap.get(item.getItemId());
            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
                return true;
            }
            return false;
        });

        // SearchView query listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search query submission
                Toast.makeText(UserMainActivity.this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
                // Add logic to filter content or make API calls here
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle live search suggestions or filtering (optional)
                return false;
            }
        });
    }

    /**
     * Displays a dropdown menu for account actions when the account icon is clicked.
     */
    private void showAccountDropdown(ImageView accountIcon) {
        // Create and show a PopupMenu
        PopupMenu popupMenu = new PopupMenu(this, accountIcon);
        popupMenu.getMenuInflater().inflate(R.menu.account_menu, popupMenu.getMenu());

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.account_settings) {
                replaceFragment(new ViewAccountFragment());
                // Add logic for account settings
                return true;
            } else if (itemId == R.id.account_logout) {
                logoutUser(); // Logout when clicked
                return true;
            } else {
                return false;
            }
        });

        popupMenu.show(); // Display the dropdown menu
    }

    /**
     * Replaces the current fragment with the specified fragment.
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    /**
     * Checks if the user is logged in and redirects if necessary.
     */
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
                // Handle undefined roles
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

    /**
     * Logs out the user and redirects to the LoginActivity.
     */
    private void logoutUser() {
        // Clear the login session
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }
}

