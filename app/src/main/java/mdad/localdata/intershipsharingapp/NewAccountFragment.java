package mdad.localdata.intershipsharingapp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class NewAccountFragment extends Fragment {

    private Spinner roleSpinner, studyYearSpinner;
    private EditText inputName, inputEmail, inputUsername, inputPassword, inputCourse, inputGraduatedYear;
    private Button btnCreateAccount;
    private TextView labelStudyYear, labelGraduatedYear;
    private String name, email, username, password, course, selectedRoleId, studyYear, graduatedYear;

    private static final String urlCreateAccount = StaffMainActivity.ipBaseAddress + "/create_user.php";

    private ArrayList<String> roleNames = new ArrayList<>();
    private ArrayList<String> roleIds = new ArrayList<>();
    private ArrayList<String> yearsOfStudy = new ArrayList<>();

    public NewAccountFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_account, container, false);

        // Initialize views
        inputName = view.findViewById(R.id.inputName);
        inputEmail = view.findViewById(R.id.inputEmail);
        inputUsername = view.findViewById(R.id.inputUsername);
        inputPassword = view.findViewById(R.id.inputPassword);
        inputCourse = view.findViewById(R.id.inputCourse);
        inputGraduatedYear = view.findViewById(R.id.inputGrad);
        roleSpinner = view.findViewById(R.id.Role);
        studyYearSpinner = view.findViewById(R.id.StudyYear);
        labelStudyYear = view.findViewById(R.id.labelStudyYear);
        btnCreateAccount = view.findViewById(R.id.btnCreateAccount);

        // Initialize years of study locally
        initializeYearsOfStudy();

        // Fetch roles
        fetchRoles();

        // Set role change behavior
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRoleId = roleIds.get(position);
                adjustInputsBasedOnRole(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Handle account creation
        btnCreateAccount.setOnClickListener(view1 -> createAccount());

        return view;
    }

    private void initializeYearsOfStudy() {
        yearsOfStudy.add("");
        yearsOfStudy.add("0");
        yearsOfStudy.add("1");
        yearsOfStudy.add("2");
        yearsOfStudy.add("3");
        yearsOfStudy.add("4");
        yearsOfStudy.add("5");

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, yearsOfStudy);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studyYearSpinner.setAdapter(yearAdapter);
    }

    private void fetchRoles() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlCreateAccount,
                response -> {
                    try {
                        roleNames.clear();
                        roleIds.clear();

                        roleNames.add("Select a Role");
                        roleIds.add("");
                        JSONArray rolesArray = new JSONArray(response);
                        for (int i = 0; i < rolesArray.length(); i++) {
                            JSONObject roleObject = rolesArray.getJSONObject(i);
                            String roleName = roleObject.getString("name");
                            String roleId = roleObject.getString("RoleId");
                            roleNames.add(roleName);
                            roleIds.add(roleId);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, roleNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        roleSpinner.setAdapter(adapter);

                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Error parsing roles", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error fetching roles: " + error.getMessage(), Toast.LENGTH_LONG).show());

        requestQueue.add(stringRequest);
    }

    private void adjustInputsBasedOnRole(int position) {
        String selectedRole = roleNames.get(position);

        if ("Student".equals(selectedRole)) {
            labelStudyYear.setVisibility(View.VISIBLE);
            studyYearSpinner.setVisibility(View.VISIBLE);
            inputGraduatedYear.setVisibility(View.GONE);
            inputGraduatedYear.setText("");

        } else if ("Alumni".equals(selectedRole)) {
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

    private void createAccount() {
        name = inputName.getText().toString();
        email = inputEmail.getText().toString();
        username = inputUsername.getText().toString();
        password = inputPassword.getText().toString();
        course = inputCourse.getText().toString();
        studyYear = studyYearSpinner.getSelectedItem() != null ? studyYearSpinner.getSelectedItem().toString().trim() : "";
        graduatedYear = inputGraduatedYear.getText() != null ? inputGraduatedYear.getText().toString().trim() : "";

        if (validateInputs()) {
            Map<String, String> params = new HashMap<>();
            params.put("user_name", name);
            params.put("email", email);
            params.put("username", username);
            params.put("password", password);
            params.put("course", course);
            params.put("role", selectedRoleId);
            params.put("studyYear", studyYear.isEmpty() ? "null" : studyYear);
            params.put("graduatedYear", graduatedYear.isEmpty() ? "null" : graduatedYear);

            postData(urlCreateAccount, params);
        }
    }

    private boolean validateInputs() {
        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || course.isEmpty() || selectedRoleId.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void postData(String url, Map<String, String> params) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    if ("Successful".equals(response.trim())) {
                        showCreatedSuccessfulAlert();
                    } else {
                        showCreatedFailedAlert(response.trim());
                    }
                },
                error -> Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    public void showCreatedSuccessfulAlert() {
        // Create LottieAnimationView dynamically
        LottieAnimationView lottieSuccess = new LottieAnimationView(requireContext());
        lottieSuccess.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        lottieSuccess.setAnimation(R.raw.success);  // Reference your Lottie JSON animation
        lottieSuccess.playAnimation();

        // Create a TextView for the success message
        TextView messageTextView = new TextView(requireContext());
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
        LinearLayout dialogLayout = new LinearLayout(requireContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 50, 50, 50);  // Add padding
        dialogLayout.setGravity(Gravity.CENTER);
        dialogLayout.addView(lottieSuccess);
        dialogLayout.addView(messageTextView);

        // Build and show the dialog with the custom view
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Success")
                .setView(dialogLayout)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void showCreatedFailedAlert(String cleanresponese) {
        // Create LottieAnimationView dynamically
        LottieAnimationView lottieSuccess = new LottieAnimationView(requireContext());
        lottieSuccess.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        lottieSuccess.setAnimation(R.raw.denied);  // Reference your Lottie JSON animation
        lottieSuccess.playAnimation();

        // Create a TextView for the success message
        TextView messageTextView = new TextView(requireContext());
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
        LinearLayout dialogLayout = new LinearLayout(requireContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 50, 50, 50);  // Add padding
        dialogLayout.setGravity(Gravity.CENTER);
        dialogLayout.addView(lottieSuccess);
        dialogLayout.addView(messageTextView);

        // Build and show the dialog with the custom view
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Denied!!")
                .setView(dialogLayout)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
