package mdad.localdata.intershipsharingapp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    //ArrayList to store product list from database
    ArrayList<HashMap<String, String>> userList;
    // url to get all products list via the php file get_all_productsJson.php

    // remeber to change the IP address on different laptop after pulling
    private static String url_all_products = StaffMainActivity.ipBaseAddress+"/get_all_user.php";
    private String roleId, username, userId; // To store the role of the logged-in user

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // ArrayList to store product info in Hashmap for ListView
        userList = new ArrayList<HashMap<String, String>>();
        // re-usable method to use Volley to retrieve products from database
        postData(url_all_products, null );
        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);
        TextView textViewButton = findViewById(R.id.textViewRegister);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString();
                String email = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (userList.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "No users available. Please try again later.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (validateLogin(username, email, password)) {
                    showLoginSuccessfulAlert();
                    // Proceed to the next activity or screen
                } else {
                    showLoginFailedAlert();
                    Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
        textViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create New Product Activity
                Intent i = new Intent(getApplicationContext(), NewUserActivity.class);
                startActivity(i);
            }
        });

    }

    public void saveLoginSession(String username, String roleId) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("username", username); // Save username
        editor.putString("roleId", roleId);     // Save role ID
        editor.putBoolean("isLoggedIn", true);  // Save login status
        editor.apply();
        checkUserSession();
    }


    public void showLoginSuccessfulAlert() {
        if (isFinishing() || isDestroyed()) {
            return;  // Prevent showing dialog if the Activity is finishing or destroyed
        }
        // Create LottieAnimationView dynamically
        LottieAnimationView lottieSuccess = new LottieAnimationView(this);
        lottieSuccess.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        lottieSuccess.setAnimation(R.raw.success);  // Reference your Lottie JSON animation
        lottieSuccess.playAnimation();

        // Create a TextView for the success message
        TextView messageTextView = new TextView(this);
        messageTextView.setText("Login Successful!");
        messageTextView.setTypeface(null, Typeface.BOLD);
        messageTextView.setTextSize(18);
        messageTextView.setTextColor(Color.parseColor("#228B22"));
        messageTextView.setGravity(Gravity.CENTER);

        // Apply a fade-in animation to the TextView
        messageTextView.setAlpha(0f);  // Start fully transparent
        ObjectAnimator animator = ObjectAnimator.ofFloat(messageTextView, "alpha", 0f, 1f);
        animator.setDuration(1000);  // Duration of the fade-in effect (1 second)
        animator.start();

        // Create a vertical LinearLayout to hold Lottie and TextView
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 50, 50, 50);  // Add padding
        dialogLayout.setGravity(Gravity.CENTER);
        dialogLayout.addView(lottieSuccess);
        dialogLayout.addView(messageTextView);



        // Build and show the dialog with the custom view
        new MaterialAlertDialogBuilder(this)
                .setView(dialogLayout)
                .setPositiveButton("OK", (dialog, which) -> {dialog.dismiss();
                    Intent intent;
                    switch (roleId) {
                        case "1": // Example Role ID for Students
                            intent = new Intent(LoginActivity.this, UserMainActivity.class);
                            break;
                        case "2": // Example Role ID for Alumni
                            intent = new Intent(LoginActivity.this, UserMainActivity.class);
                            break;
                        case "3": // Example Role ID for Admin
                            intent = new Intent(LoginActivity.this, StaffMainActivity.class);
                            break;
                        default: // Default case if role is undefined
                            Toast.makeText(this, "Undefined role, unable to navigate.", Toast.LENGTH_SHORT).show();
                            return;
                    }
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    public void showLoginFailedAlert() {
        // Create LottieAnimationView dynamically
        LottieAnimationView lottieSuccess = new LottieAnimationView(this);
        lottieSuccess.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        lottieSuccess.setAnimation(R.raw.denied);  // Reference your Lottie JSON animation
        lottieSuccess.playAnimation();

        // Create a TextView for the success message
        TextView messageTextView = new TextView(this);
        messageTextView.setText("Invalid Credentials. Please try again!");
        messageTextView.setTypeface(null, Typeface.BOLD);
        messageTextView.setTextSize(18);
        messageTextView.setTextColor(Color.parseColor("#800000"));
        messageTextView.setGravity(Gravity.CENTER);

        // Apply a fade-in animation to the TextView
        messageTextView.setAlpha(0f);  // Start fully transparent
        ObjectAnimator animator = ObjectAnimator.ofFloat(messageTextView, "alpha", 0f, 1f);
        animator.setDuration(1000);  // Duration of the fade-in effect (1 second)
        animator.start();

        // Create a vertical LinearLayout to hold Lottie and TextView
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 50, 50, 50);  // Add padding
        dialogLayout.setGravity(Gravity.CENTER);
        dialogLayout.addView(lottieSuccess);
        dialogLayout.addView(messageTextView);

        // Build and show the dialog with the custom view
        new MaterialAlertDialogBuilder(this)
                .setTitle("Denied!!")
                .setView(dialogLayout)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }


    public boolean validateLogin(String username, String email, String password) {
        for (HashMap<String, String> user : userList) {
            // Check if the username/email and password match
            if ((user.containsKey("username") || user.containsKey("email")) && user.containsKey("password")) {
                if ((user.get("username").equals(username) || user.get("email").equals(email))
                        && user.get("password").equals(password)) {

                    // Retrieve the UserId and RoleId of the logged-in user
                    userId = user.get("UserId");
                    username = user.get("username");
                    roleId = user.get("RoleId"); // Get the user's role ID

                    // Save the session data, including the UserId
                    saveLoginSession(userId, roleId);

                    showLoginSuccessfulAlert();
                    checkUserSession();
                    return true;
                }
            }
        }
        return false;
    }


    public void postData(String url, Map<String, String> params) {
        // Create a RequestQueue for Volley
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a StringRequest for Volley (POST method)
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                // Response from the server
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Log the response from the server
                        Log.d("Response", response);  // Add this line to log the response

                        // Check if error code received from server
                        if (response.equals("Error")) {
                            Toast.makeText(getApplicationContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Handle the response data received from server
                        // Assuming the server is returning a JSON array or a formatted string, you can parse it accordingly
                        String[] users = response.split(":");

                        // Check if the response contains data
                        if (users.length == 0) {
                            Toast.makeText(getApplicationContext(), "No users found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // For each user, retrieve the user details
                        for (String user : users) {
                            String[] details = user.split(";");
                            if (details.length >=7) { // Ensure there are 8 fields (safety check)
                                String UserId = details[0];
                                String name = details[1];
                                String email = details[2];
                                String username = details[3];
                                String password = details[4];
                                String course = details[5];
                                String year_of_study = details.length > 6 ? details[6] : ""; // Handle missing year_of_study
                                String graduated_year = details.length > 7 ? details[7] : ""; // Handle missing graduated_year
                                String RoleId = details.length > 8 ? details[8] : "";
                                // Create a new HashMap to store user info
                                HashMap<String, String> map = new HashMap<>();
                                map.put("UserId", UserId);
                                map.put("name", name);
                                map.put("email", email);
                                map.put("username", username);
                                map.put("password", password);
                                map.put("course", course);
                                map.put("year_of_study", year_of_study);
                                map.put("RoleId", RoleId);

                                // Add map to the ArrayList
                                userList.add(map);
                            }
                        }
                        ListAdapter adapter = new SimpleAdapter(
                                LoginActivity.this, userList,
                                R.layout.user_list_item, new String[]{"UserId", "name", "email", "username", "password", "course", "year_of_study", "RoleId"},
                                new int[]{R.id.UserId, R.id.name, R.id.email, R.id.username, R.id.password, R.id.course, R.id.year_of_study, R.id.RoleId});

                    }
                },

                // Error in Volley
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError", error.toString());  // Log the error
                        Toast.makeText(getApplicationContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                    }
                }
        );

        // Add StringRequest to RequestQueue in Volley
        queue.add(stringRequest);
    }

    public void checkUserSession() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            String storedUserId = sharedPreferences.getString("username", null);
            String storedRoleId = sharedPreferences.getString("roleId", null);

            Log.d("SessionCheck", "UserId: " + storedUserId + ", RoleId: " + storedRoleId);
            //Toast.makeText(this, "User logged in as UserId: " + storedUserId + ", RoleId: " + storedRoleId, Toast.LENGTH_LONG).show();
        } else {
            Log.d("SessionCheck", "No active session found.");
            //Toast.makeText(this, "No active session.", Toast.LENGTH_SHORT).show();
        }
    }
}

