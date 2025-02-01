package mdad.localdata.intershipsharingapp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewUserActivity extends AppCompatActivity {
    Spinner roleSpinner, studyYearSpinner;
    EditText inputName, inputEmail, inputUsername, inputPassword, inputCourse, inputGraduatedYear;
    Button btnCreateAccount;
    TextView labelStudyYear, labelGraduatedYear;
    String name, email, username, password, course, selectedRoleId, studyYear, graduatedYear;
    private static final String urlCreateAccount = StaffMainActivity.ipBaseAddress + "/create_user.php"; // Update as needed
    ArrayList<String> roleNames = new ArrayList<>();
    ArrayList<String> roleIds = new ArrayList<>();
    ArrayList<String> yearsOfStudy = new ArrayList<>();
    boolean isPasswordVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        // Initialize views
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputUsername = findViewById(R.id.inputUsername);
        inputPassword = findViewById(R.id.inputPassword);
        inputCourse = findViewById(R.id.inputCourse);
        inputGraduatedYear = findViewById(R.id.inputGrad);
        roleSpinner = findViewById(R.id.Role);
        studyYearSpinner = findViewById(R.id.StudyYear);
        labelStudyYear = findViewById(R.id.labelStudyYear);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        ImageView eyeIcon = findViewById(R.id.togglePassword);


        // Initialize years of study locally
        initializeYearsOfStudy();

        // Fetch roles
        fetchRoles();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Set role change behavior
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRoleId = roleIds.get(position); // Update selectedRoleId based on the position
                // Filter inputs based on role
                if ("Student".equals(roleNames.get(position))) {
                    labelStudyYear.setVisibility(View.VISIBLE);
                    studyYearSpinner.setVisibility(View.VISIBLE);
                    inputGraduatedYear.setVisibility(View.GONE);
                    inputGraduatedYear.setText("");

                } else if ("Alumni".equals(roleNames.get(position))) {
                    inputGraduatedYear.setVisibility(View.VISIBLE);
                    labelStudyYear.setVisibility(View.GONE);
                    studyYearSpinner.setVisibility(View.GONE);
                    studyYearSpinner.setSelection(0);
                } else {
                    labelStudyYear.setVisibility(View.GONE);
                    studyYearSpinner.setVisibility(View.GONE);
                    inputGraduatedYear.setVisibility(View.GONE);
                    inputGraduatedYear.setText("");
                    studyYearSpinner.setSelection(0);
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Handle account creation
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = inputName.getText().toString();
                email = inputEmail.getText().toString();
                username = inputUsername.getText().toString();
                password = inputPassword.getText().toString();
                course = inputCourse.getText().toString();
                studyYear = studyYearSpinner.getSelectedItem() != null ? studyYearSpinner.getSelectedItem().toString().trim() : "";
                graduatedYear = inputGraduatedYear.getText() != null ? inputGraduatedYear.getText().toString().trim() : "";

                studyYear = studyYear.isEmpty() ? "null" : studyYear;
                graduatedYear = graduatedYear.isEmpty() ? "null" : graduatedYear;

                if (name.isEmpty()) {
                    Toast.makeText(NewUserActivity.this, "Name field is required", Toast.LENGTH_SHORT).show();
                    inputName.requestFocus();
                    return;
                }

                if (email.isEmpty()) {
                    Toast.makeText(NewUserActivity.this, "Email field is required", Toast.LENGTH_SHORT).show();
                    inputEmail.requestFocus();
                    return;
                }

                if (username.isEmpty()) {
                    Toast.makeText(NewUserActivity.this, "Username field is required", Toast.LENGTH_SHORT).show();
                    inputUsername.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    Toast.makeText(NewUserActivity.this, "Password field is required", Toast.LENGTH_SHORT).show();
                    inputPassword.requestFocus();
                    return;
                }

                if (course.isEmpty()) {
                    Toast.makeText(NewUserActivity.this, "Course field is required", Toast.LENGTH_SHORT).show();
                    inputCourse.requestFocus();
                    return;
                }

                if (selectedRoleId == null || selectedRoleId.isEmpty()) {
                    Toast.makeText(NewUserActivity.this, "Role selection is required", Toast.LENGTH_SHORT).show();
                    roleSpinner.requestFocus();
                    return;
                }
                if (roleSpinner.getSelectedItemPosition() == 0) { // Placeholder is at index 0
                    Toast.makeText(NewUserActivity.this, "Please select a valid role", Toast.LENGTH_SHORT).show();
                    roleSpinner.requestFocus();
                    return;
                }

                // Use selectedRoleId as the value to send
                selectedRoleId = roleIds.get(roleSpinner.getSelectedItemPosition());


                Map<String, String> params = new HashMap<>();
                params.put("user_name", name);
                params.put("email", email);
                params.put("username", username);
                params.put("password", password);
                params.put("course", course);
                params.put("role", selectedRoleId);
                params.put("studyYear", studyYear);
                params.put("graduatedYear", graduatedYear);

                postData(urlCreateAccount, params);
            }
        });
        // Initialize the visibility toggle for password
        eyeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toggle password visibility
                if (isPasswordVisible) {
                    // Hide password
                    inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    eyeIcon.setImageResource(R.drawable.hidden); // Use the hidden icon
                } else {
                    // Show password
                    inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
                    eyeIcon.setImageResource(R.drawable.visible); // Use the visible icon
                }

                // Move the cursor to the end of the password after toggling visibility
                inputPassword.setSelection(inputPassword.getText().length());

                // Update the state of the toggle
                isPasswordVisible = !isPasswordVisible;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the back button press
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // This will call the back stack and finish the activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Ensure the default back press behavior occurs
        // This will navigate back to the previous activity in the stack
    }

    private void initializeYearsOfStudy() {
        yearsOfStudy.add("");
        yearsOfStudy.add("0");
        yearsOfStudy.add("1");
        yearsOfStudy.add("2");
        yearsOfStudy.add("3");
        yearsOfStudy.add("4");
        yearsOfStudy.add("5");

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yearsOfStudy);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studyYearSpinner.setAdapter(yearAdapter);
    }

    //for fetch roleid as a fk
    private void fetchRoles() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlCreateAccount,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("FetchRoles", "Server Response: " + response);
                        try {
                            roleNames.clear();
                            roleIds.clear();

                            // Add a placeholder at the start
                            roleNames.add("Select a Role");
                            roleIds.add(""); // Add an empty string as a placeholder ID
                            JSONArray rolesArray = new JSONArray(response);
                            for (int i = 0; i < rolesArray.length(); i++) {
                                JSONObject roleObject = rolesArray.getJSONObject(i);
                                String roleName = roleObject.getString("name");
                                String roleId = roleObject.getString("RoleId");

                                if ("Staff".equals(roleName)) {
                                    SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                                    String currentUserRoleId = sharedPreferences.getString("roleId", "");

                                    if ("3".equals(currentUserRoleId)) {
                                        roleNames.add(roleName);
                                        roleIds.add(roleId);
                                    }
                                } else {
                                    roleNames.add(roleName);
                                    roleIds.add(roleId);
                                }
                            }

                            // Populate the Spinner
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(NewUserActivity.this, android.R.layout.simple_spinner_item, roleNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            roleSpinner.setAdapter(adapter);

                            // Enable the spinner after the roles are loaded
                            roleSpinner.setEnabled(true);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(NewUserActivity.this, "Error parsing roles", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(NewUserActivity.this, "Error fetching roles: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        requestQueue.add(stringRequest);
    }
    public void showCreatedSuccessfulAlert() {
        // Create LottieAnimationView dynamically
        LottieAnimationView lottieSuccess = new LottieAnimationView(this);
        lottieSuccess.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        lottieSuccess.setAnimation(R.raw.success);  // Reference your Lottie JSON animation
        lottieSuccess.playAnimation();

        // Create a TextView for the success message
        TextView messageTextView = new TextView(this);
        messageTextView.setText("Account Created Successfully!");
        messageTextView.setTypeface(null, Typeface.BOLD);
        messageTextView.setTextSize(18);
        messageTextView.setTextColor(Color.parseColor("#90EE90"));
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
                .setTitle("Success")
                .setView(dialogLayout)
                .setPositiveButton("OK", (dialog, which) -> {dialog.dismiss();
                    Intent intent = new Intent(NewUserActivity.this, LoginActivity.class);
                    startActivity(intent);})
                .show();
    }

    public void showCreatedFailedAlert(String cleanresponese) {
        // Create LottieAnimationView dynamically
        LottieAnimationView lottieSuccess = new LottieAnimationView(this);
        lottieSuccess.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        lottieSuccess.setAnimation(R.raw.denied);  // Reference your Lottie JSON animation
        lottieSuccess.playAnimation();

        // Create a TextView for the success message
        TextView messageTextView = new TextView(this);
        messageTextView.setText("Failed to  create account!\n"+"Responese: "+cleanresponese);
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

    private void postData(String url, Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            Log.d("PostData", "Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("RawResponse", "Raw Response: '" + response + "'");
                    // Clean the response to remove any HTML tags
                    String cleanResponse = response.trim().replaceAll("<[^>]*>", ""); // Remove HTML tags

                    // Log the cleaned response for debugging purposes
                    Log.d("CleanedResponse", "Cleaned Response: '" + cleanResponse + "'");
                    if ("Successful".equals(response.trim())) {
                        showCreatedSuccessfulAlert();
                    } else {
                        showCreatedFailedAlert(cleanResponse);
                    }
                },
                error -> Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
}
