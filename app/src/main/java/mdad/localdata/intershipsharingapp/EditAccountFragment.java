package mdad.localdata.intershipsharingapp;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditAccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditAccountFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private List<String> roleNames = new ArrayList<>();
    private List<String> roleIds = new ArrayList<>();
    ArrayList<String> yearsOfStudy = new ArrayList<>();
    private String selectedRoleId = "";


    private static final String urlViewAccount = StaffMainActivity.ipBaseAddress + "/get_all_user.php";
    private static final String urlEditUser = StaffMainActivity.ipBaseAddress + "/edit_user.php";

    private String mParam1;
    private String mParam2;
    private TextView labelStudyYear, labelGraduatedYear;
    private Button editButton;
    private EditText inputName, inputEmail, inputUsername, inputPassword, inputCourse, inputGraduatedYear;
    private Spinner roleSpinner, studyYearSpinner;
    private SharedPreferences sharedPreferences;

    public EditAccountFragment() {
        // Required empty public constructor
    }

    public static EditAccountFragment newInstance(String param1, String param2) {
        EditAccountFragment fragment = new EditAccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_account, container, false);

        // Initialize views
        inputName = view.findViewById(R.id.inputName);
        inputEmail = view.findViewById(R.id.inputEmail);
        inputUsername = view.findViewById(R.id.inputUsername);
        inputPassword = view.findViewById(R.id.inputPassword);
        inputCourse = view.findViewById(R.id.inputCourse);
        studyYearSpinner = view.findViewById(R.id.StudyYear);
        labelStudyYear = view.findViewById(R.id.labelStudyYear);
        labelGraduatedYear = view.findViewById(R.id.labelGraduatedYear);
        inputGraduatedYear = view.findViewById(R.id.inputGrad);
        roleSpinner = view.findViewById(R.id.Role);
        editButton = view.findViewById(R.id.editAccount); // Assuming you have this button in the layout

        initializeYearsOfStudy();

        // Add Eye Toggle for Password
        View togglePasswordView = view.findViewById(R.id.togglePassword);
        togglePasswordView.setOnClickListener(v -> {
            if (inputPassword.getTransformationMethod() == null) {
                // Show Password
                inputPassword.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
                ((ImageView) togglePasswordView).setImageResource(R.drawable.hidden); // Eye closed icon
            } else {
                // Hide Password
                inputPassword.setTransformationMethod(null);
                ((ImageView) togglePasswordView).setImageResource(R.drawable.visible); // Eye open icon
            }
        });

        // Retrieve shared preferences
        sharedPreferences = requireActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
        String loggedInUserId = sharedPreferences.getString("username", "");

        if (!loggedInUserId.isEmpty()) {
            fetchUserDetails(loggedInUserId); // Call method to fetch and display user data
            fetchRoles(); // Fetch roles to populate the spinner

        } else {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
        }


        editButton.setOnClickListener(v -> editUserData());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    // Fetch user details based on logged-in user's ID
    private void fetchUserDetails(String loggedInUserId) {
        String url_view_account = StaffMainActivity.ipBaseAddress + "/get_all_user.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_view_account,
                response -> {
                    Log.d("UserDetails", "Server Response: " + response);
                    if (response.equals("Error")) {
                        Toast.makeText(requireContext(), "Error retrieving database", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Split the response into user entries
                    String[] users = response.split(":");
                    Log.d("UserDetails", "Users: " + Arrays.toString(users));
                    for (String user : users) {
                        if (!user.isEmpty()) {
                            String[] details = user.split(";");
                            Log.d("UserDetails", "Details: " + Arrays.toString(details));
                            if (details.length >= 9) { // Adjust based on your response structure
                                String userId = details[0]; // Assuming userId is the first field

                                // Match the logged-in user's ID
                                if (loggedInUserId.equals(userId)) {
                                    // Display data in the EditText fields
                                    inputName.setText(details[1]); // Name
                                    inputUsername.setText(details[3]); // Username
                                    inputEmail.setText(details[2]); // Email
                                    inputCourse.setText(details[5]); // Course
                                    inputPassword.setText(details[4]); // Placeholder for password

                                    // Set selected role based on role ID
                                    String userRoleId = details[8]; // Assuming role ID is at index 8
                                    Log.d("UserDetails", "User Role ID: " + userRoleId);

                                    // Find the matching role by roleId
                                    for (int i = 0; i < roleIds.size(); i++) {
                                        if (roleIds.get(i).equals(userRoleId)) {
                                            roleSpinner.setSelection(i); // Set the spinner to the matching role
                                            selectedRoleId = userRoleId; // Update the selectedRoleId
                                            break;
                                        }
                                    }

                                    // Set Year of Study Spinner
                                    String yearOfStudy = details[6]; // Assuming yearOfStudy is in the response
                                    int yearPosition = yearsOfStudy.indexOf(yearOfStudy);
                                    if (yearPosition >= 0) {
                                        studyYearSpinner.setSelection(yearPosition);
                                    }

                                    // Set Graduated Year if the role is Alumni
                                    if ("2".equals(userRoleId)) { // Assuming Alumni roleId is "2"
                                        String graduatedYear = details[7]; // Assuming graduatedYear is in the response
                                        Log.d("UserDetails", "Graduated Year: " + graduatedYear);
                                        inputGraduatedYear.setText(graduatedYear);
                                        labelGraduatedYear.setVisibility(View.VISIBLE);
                                        inputGraduatedYear.setVisibility(View.VISIBLE);
                                        labelStudyYear.setVisibility(View.GONE);
                                        studyYearSpinner.setVisibility(View.GONE);
                                    }

                                    Log.d("UserDetails", "Details loaded into fields");
                                    break; // Exit loop after finding the user
                                }
                            }
                        }
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error retrieving user details", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Optional: Add POST parameters if required by your backend
                Map<String, String> params = new HashMap<>();
                params.put("userid", loggedInUserId); // Example parameter
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void initializeYearsOfStudy() {
        yearsOfStudy.add("");
        yearsOfStudy.add("0");
        yearsOfStudy.add("1");
        yearsOfStudy.add("2");
        yearsOfStudy.add("3");
        yearsOfStudy.add("4");
        yearsOfStudy.add("5");

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, yearsOfStudy);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studyYearSpinner.setAdapter(yearAdapter);
    }

    // Fetch roles from the backend and populate the spinner
    // Update fetchRoles() method to ensure spinner selection happens after roles are populated
    private void fetchRoles() {
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        // Make a request to get all roles
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlEditUser,
                response -> {
                    if (response.equals("Error")) {
                        Toast.makeText(requireContext(), "Error retrieving roles", Toast.LENGTH_LONG).show();
                        return;
                    }

                    try {
                        JSONArray rolesArray = new JSONArray(response);
                        roleNames.clear();
                        roleIds.clear();

                        roleNames.add("Select a role");
                        roleIds.add(""); // Placeholder ID for the first option

                        // Retrieve role ID from shared preferences
                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
                        String userRoleId = sharedPreferences.getString("roleId", "");

                        // If role ID is not 3, display only the first two roles
                        if ("3".equals(userRoleId)) {
                            // Display all roles
                            for (int i = 0; i < rolesArray.length(); i++) {
                                JSONObject roleObject = rolesArray.getJSONObject(i);
                                String roleName = roleObject.getString("name");
                                String roleId = roleObject.getString("RoleId"); // Assuming RoleId is in the response
                                roleNames.add(roleName);
                                roleIds.add(roleId);
                            }
                        } else {
                            // Display only the first two roles
                            for (int i = 0; i < 2 && i < rolesArray.length(); i++) {
                                JSONObject roleObject = rolesArray.getJSONObject(i);
                                String roleName = roleObject.getString("name");
                                String roleId = roleObject.getString("RoleId"); // Assuming RoleId is in the response
                                roleNames.add(roleName);
                                roleIds.add(roleId);
                            }
                        }

                        // Convert List to array for the spinner
                        String[] roles = roleNames.toArray(new String[0]);

                        // Set the roles to the spinner
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, roles);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        roleSpinner.setAdapter(adapter);

                        // Once the roles are populated, set the selected role
                        setSelectedRole(userRoleId);

                        // Set the item selected listener after the spinner is populated
                        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedRoleId = roleIds.get(position); // Update selectedRoleId based on the position
                                // Filter inputs based on role
                                if ("Student".equals(roleNames.get(position))) {
                                    labelStudyYear.setVisibility(View.VISIBLE);
                                    studyYearSpinner.setVisibility(View.VISIBLE);
                                    labelGraduatedYear.setVisibility(View.GONE);
                                    inputGraduatedYear.setVisibility(View.GONE);
                                    inputGraduatedYear.setText("");
                                } else if ("Alumni".equals(roleNames.get(position))) {
                                    labelGraduatedYear.setVisibility(View.VISIBLE);
                                    inputGraduatedYear.setVisibility(View.VISIBLE);
                                    labelStudyYear.setVisibility(View.GONE);
                                    studyYearSpinner.setVisibility(View.GONE);
                                    studyYearSpinner.setSelection(0);
                                } else {
                                    labelStudyYear.setVisibility(View.GONE);
                                    studyYearSpinner.setVisibility(View.GONE);
                                    labelGraduatedYear.setVisibility(View.GONE);
                                    inputGraduatedYear.setVisibility(View.GONE);
                                    inputGraduatedYear.setText("");
                                    studyYearSpinner.setSelection(0);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // Handle the case when nothing is selected, if necessary
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error parsing roles", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error retrieving roles", Toast.LENGTH_LONG).show();
                });

        queue.add(stringRequest);
    }

    // Method to set the selected role after roles are fetched
    // Method to set the selected role after roles are fetched
    private void setSelectedRole(String userRoleId) {
        // Find the matching role by userRoleId
        for (int i = 0; i < roleIds.size(); i++) {
            if (roleIds.get(i).equals(userRoleId)) {
                // Make 'i' effectively final by assigning it to a final variable
                final int finalI = i;

                // Ensure the spinner selection happens after the spinner is populated
                roleSpinner.post(() -> roleSpinner.setSelection(finalI)); // Set the spinner selection on the main thread
                selectedRoleId = userRoleId; // Update the selectedRoleId
                break;
            }
        }
    }

    private void editUserData() {
        // Collecting the updated data from the input fields
        String name = inputName.getText().toString();
        String email = inputEmail.getText().toString();
        String username = inputUsername.getText().toString();
        String password = inputPassword.getText().toString();
        String course = inputCourse.getText().toString();
        String studyYear = studyYearSpinner.getSelectedItem().toString();
        String graduatedYear = inputGraduatedYear.getText().toString();

        // Check if all required fields are filled
        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || course.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare parameters to send in the request
        Map<String, String> params = new HashMap<>();
        params.put("userId", sharedPreferences.getString("username", "")); // User ID
        params.put("user_name", name);
        params.put("email", email);
        params.put("username", username);
        params.put("password", password);
        params.put("course", course);
        params.put("studyYear", studyYear); // Send selected study year
        params.put("graduatedYear", graduatedYear); // Send graduated year if applicable
        params.put("roleId", selectedRoleId); // Send the selected role ID
        Log.d("EditUser", "Selected Role ID: " + params);

        // Send a POST request to update the user details
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlEditUser,
                response -> {
                    Log.d("EditUser", "Server Response: " + response);
                    try {
                        // Parse the response as a JSON object
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.optString("status"); // Get the "status" field

                        // Check the status and act accordingly
                        if ("Success".equals(status)) {
                            showCreateSuccessfulAlert();
                            // You can redirect the user or update the UI as needed after successful update
                        } else {
                            showCreateFailedAlert();
                        }

                    } catch (JSONException e) {
                        Log.e("JSONError", "Error parsing response: " + e.getMessage());
                        Toast.makeText(getContext(), "Error parsing server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(getContext(), "Error updating user details", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        queue.add(stringRequest);
    }

    public void showCreateSuccessfulAlert() {
        // Create LottieAnimationView dynamically
        LottieAnimationView lottieSuccess = new LottieAnimationView(requireContext());
        lottieSuccess.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        lottieSuccess.setAnimation(R.raw.success);  // Reference your Lottie JSON animation
        lottieSuccess.playAnimation();

        // Create a TextView for the success message
        TextView messageTextView = new TextView(requireContext());
        messageTextView.setText("Updated Profile Details Successfully!");
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
        LinearLayout dialogLayout = new LinearLayout(requireContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 50, 50, 50);  // Add padding
        dialogLayout.setGravity(Gravity.CENTER);
        dialogLayout.addView(lottieSuccess);
        dialogLayout.addView(messageTextView);



        // Build and show the dialog with the custom view
        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogLayout)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    requireActivity().onBackPressed();
                })
                .show();
    }

    public void showCreateFailedAlert() {
        // Create LottieAnimationView dynamically
        LottieAnimationView lottieSuccess = new LottieAnimationView(requireContext());
        lottieSuccess.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        lottieSuccess.setAnimation(R.raw.denied);  // Reference your Lottie JSON animation
        lottieSuccess.playAnimation();

        // Create a TextView for the success message
        TextView messageTextView = new TextView(requireContext());
        messageTextView.setText("Fail to Update Profile Details. Please try again!");
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
