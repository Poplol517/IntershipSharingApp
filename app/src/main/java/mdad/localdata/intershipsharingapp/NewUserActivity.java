package mdad.localdata.intershipsharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
    private static final String urlCreateAccount = MainActivity.ipBaseAddress + "/create_user.php"; // Update as needed
    ArrayList<String> roleNames = new ArrayList<>();
    ArrayList<String> roleIds = new ArrayList<>();

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
        labelGraduatedYear = findViewById(R.id.labelGraduatedYear);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // Disable spinner initially
        roleSpinner.setEnabled(false);

        // Fetch roles from the server
        fetchRoles();

        // Filter inputs based on role
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (roleNames.isEmpty()) {
                    return; // Don't proceed if roles are not yet loaded
                }

                selectedRoleId = roleIds.get(position); // Get the selected role ID

                if ("Student".equals(roleNames.get(position))) {
                    // Show Study Year fields
                    labelStudyYear.setVisibility(View.VISIBLE);
                    studyYearSpinner.setVisibility(View.VISIBLE);
                    // Hide Graduated Year fields
                    labelGraduatedYear.setVisibility(View.GONE);
                    inputGraduatedYear.setVisibility(View.GONE);
                } else if ("Alumni".equals(roleNames.get(position))) {
                    // Show Graduated Year fields
                    labelGraduatedYear.setVisibility(View.VISIBLE);
                    inputGraduatedYear.setVisibility(View.VISIBLE);
                    // Hide Study Year fields
                    labelStudyYear.setVisibility(View.GONE);
                    studyYearSpinner.setVisibility(View.GONE);
                } else {
                    // Hide both fields
                    labelStudyYear.setVisibility(View.GONE);
                    studyYearSpinner.setVisibility(View.GONE);
                    labelGraduatedYear.setVisibility(View.GONE);
                    inputGraduatedYear.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(NewUserActivity.this, "Please select a role.", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle account creation
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user inputs
                name = inputName.getText().toString();
                email = inputEmail.getText().toString();
                username = inputUsername.getText().toString();
                password = inputPassword.getText().toString();
                course = inputCourse.getText().toString();
                studyYear = studyYearSpinner.getSelectedItem() != null ? studyYearSpinner.getSelectedItem().toString().trim() : "";
                graduatedYear = inputGraduatedYear.getText() != null ? inputGraduatedYear.getText().toString().trim() : "";

                // Validate inputs
                if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || course.isEmpty() || selectedRoleId == null) {
                    Toast.makeText(NewUserActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Prepare parameters
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("username", username);
                params.put("password", password);
                params.put("course", course);
                params.put("role", selectedRoleId); // Use role ID
                params.put("studyYear", studyYear);
                params.put("graduatedYear", graduatedYear);

                // Post data to create the account
                postData(urlCreateAccount, params);
            }
        });
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
                            JSONArray rolesArray = new JSONArray(response);
                            for (int i = 0; i < rolesArray.length(); i++) {
                                JSONObject roleObject = rolesArray.getJSONObject(i);
                                roleNames.add(roleObject.getString("name")); // Get role name
                                roleIds.add(roleObject.getString("RoleId")); // Store corresponding role ID
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

    private void postData(String url, Map<String, String> params) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    if ("Success".equals(response)) {
                        Toast.makeText(getApplicationContext(), "Account created successfully", Toast.LENGTH_LONG).show();
                        finish();
                        startActivity(new Intent(getApplicationContext(), AllUserActivity.class));
                    } else {
                        Toast.makeText(getApplicationContext(), "Error: " + response, Toast.LENGTH_LONG).show();
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
