package mdad.localdata.intershipsharingapp;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing the "Create Internship" tab content.
 */
public class CreateQuestionFragment extends Fragment {
    private static String url_create_question = StaffMainActivity.ipBaseAddress + "/create_question.php";
    private static String url_questionindustry = StaffMainActivity.ipBaseAddress + "/create_question_industry.php";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ArrayList<String> selectedIndustryIds = new ArrayList<>();
    private Map<String, String> industryMap = new HashMap<>();


    private String mParam1;
    private String mParam2;
    Button createButton;
    private ChipGroup chipGroupIndustries; // Reference to ChipGroup
    private EditText inputTitle,inputCompany,inputRole,inputDescription,startDateInput, endDateInput;
    String title, company, role, description, startDate, endDate, selectedLocationId, userId;
    private Spinner locationSpinner;
    private static final int DESCRIPTION_MAX_LENGTH = 1000;
    private TextView charCountText;
    ArrayList<String> locationNames = new ArrayList<>();
    ArrayList<String> locationIds = new ArrayList<>();

    public CreateQuestionFragment() {
        // Required empty public constructor
    }

    public static CreateQuestionFragment newInstance(String param1, String param2) {
        CreateQuestionFragment fragment = new CreateQuestionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_question, container, false);

        chipGroupIndustries = view.findViewById(R.id.chipGroupIndustries); // Initialize ChipGroup
        inputTitle = view.findViewById(R.id.inputTitle);
        inputDescription = view.findViewById(R.id.inputDescription);
        charCountText = view.findViewById(R.id.charCountText);
        createButton = view.findViewById(R.id.createButton);

        charCountText.setText("0/" + DESCRIPTION_MAX_LENGTH);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);

// Retrieve userId from SharedPreferences
        String userId = sharedPreferences.getString("userId", null);

// Print userId to log
        if (userId != null) {
            Log.d("SharedPreferences", "Retrieved userId: " + userId);
        } else {
            Log.d("SharedPreferences", "userId not found in SharedPreferences");
        }

        inputDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Update the character count display
                int charCount = charSequence.length();
                charCountText.setText(charCount + "/" + DESCRIPTION_MAX_LENGTH);

                // Check if the character limit is reached
                if (charCount > DESCRIPTION_MAX_LENGTH) {
                    // Trim the input to the max length if it exceeds the limit
                    inputDescription.setText(charSequence.subSequence(0, DESCRIPTION_MAX_LENGTH));
                    inputDescription.setSelection(DESCRIPTION_MAX_LENGTH); // Move cursor to the end
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Handle create button click
        createButton.setOnClickListener(v -> {
            String description = inputDescription.getText().toString();

            if (description.isEmpty()) {
                Toast.makeText(requireContext(), "Description is required", Toast.LENGTH_SHORT).show();
                inputDescription.requestFocus();
                return;
            }

            // Continue with your existing logic...
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = inputTitle.getText().toString();
                description = inputDescription.getText().toString();

                if (title.isEmpty()) {
                    Toast.makeText(requireContext(), "Name field is required", Toast.LENGTH_SHORT).show();
                    inputTitle.requestFocus();
                    return;
                }

                if (description.isEmpty()) {
                    Toast.makeText(requireContext(), "Email field is required", Toast.LENGTH_SHORT).show();
                    inputDescription.requestFocus();
                    return;
                }

                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);

                // Retrieve userId from SharedPreferences
                String userId = sharedPreferences.getString("username", null);

                // Print userId to log
                if (userId != null) {
                    Log.d("SharedPreferences", "Retrieved userId: " +  userId);
                } else {
                    Log.d("SharedPreferences", "userId not found in SharedPreferences");
                }

                // Collect selected industry IDs from the ChipGroup
                ArrayList<String> selectedIndustries = new ArrayList<>();
                for (int i = 0; i < chipGroupIndustries.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupIndustries.getChildAt(i);
                    if (chip.isChecked() && chip.getTag() != null) {
                        selectedIndustries.add((String) chip.getTag());
                    }
                }

                // Log selected industries to check if they are collected properly
                Log.d("SelectedIndustries", selectedIndustries.toString());

                Map<String, String> params = new HashMap<>();
                params.put("title", title);
                params.put("description", description);
                params.put("userId", userId);

                // Convert the selected industries to a comma-separated string
                String selectedIndustriesStr = TextUtils.join(",", selectedIndustries);
                params.put("industries", selectedIndustriesStr);

                Log.d("Params", params.toString()); // Log the params for debugging")

                // Send data to the create internship endpoint
                postData(url_create_question, params,true);

            }
        });
        fetchIndustries(); // Fetch and display industries

        return view;
    }


    private void postData(String url, Map<String, String> params, boolean isCreateQuestion) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("ServerResponse", "Raw Response: " + response);

                    try {
                        String cleanResponse = response.trim().replaceAll("<[^>]*>", "");

                        if (isCreateQuestion) {
                            JSONObject jsonResponse = new JSONObject(cleanResponse);

                            if (jsonResponse.has("questionId")) {
                                String questionId = jsonResponse.getString("questionId");
                                Log.d("QuestionID", "Retrieved questionId: " + questionId);
                                showCreateSuccessfulAlert();

                                // Now call sendIndustryData with the retrieved internshipId
                                sendIndustryData(questionId);
                            } else {
                                showCreateFailedAlert();
                                Log.e("ResponseError", "No 'internship_id' field in the response");
                                Toast.makeText(requireContext(), "Internship ID not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("JSONParsingError", "Error parsing response: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("VolleyError", "Network Error: " + error.toString());
                    Toast.makeText(requireContext(), "Network Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }



    private void fetchIndustries() {
        String url_all_location = StaffMainActivity.ipBaseAddress + "/get_all_industry.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_all_location,
                response -> {
                    chipGroupIndustries.removeAllViews(); // Clear chips
                    String[] industries = response.split(":");
                    int maxVisibleChips = 4;

                    for (int i = 0; i < industries.length; i++) {
                        String location = industries[i];
                        if (!location.isEmpty()) {
                            String[] details = location.split(";");
                            if (details.length >= 2) {
                                String industryID = details[0];
                                String industryName = details[1];

                                Chip chip = new Chip(requireContext());
                                chip.setText(industryName);
                                chip.setTag(industryID);
                                chip.setCheckable(true);

                                // Check if the industryID is already selected
                                if (selectedIndustryIds.contains(industryID)) {
                                    chip.setChecked(true);
                                }

                                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                    if (isChecked) {
                                        selectedIndustryIds.add(industryID);
                                    } else {
                                        selectedIndustryIds.remove(industryID);
                                    }
                                });

                                chipGroupIndustries.addView(chip);
                            }

                            // Add a "..." Chip after 4 items
                            if (i == maxVisibleChips) {
                                Chip moreChip = new Chip(requireContext());
                                moreChip.setText("...");
                                moreChip.setOnClickListener(v -> showAllIndustriesPopup(industries));
                                chipGroupIndustries.addView(moreChip);
                                break;
                            }
                        }
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error retrieving database", Toast.LENGTH_LONG).show();
                });
        queue.add(stringRequest);
    }


    public void showAllIndustriesPopup(String[] industries) {
        populateIndustryMap(industries);

        TextView titleTextView = new TextView(requireContext());
        titleTextView.setText("Select Relevant Industry");
        titleTextView.setTextSize(18);
        titleTextView.setGravity(Gravity.CENTER);

        EditText searchBar = new EditText(requireContext());
        searchBar.setHint("Search industries...");
        searchBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        searchBar.setPadding(20, 10, 20, 10);
        searchBar.setBackgroundResource(android.R.drawable.edit_text);
        searchBar.setSingleLine(true);

        ChipGroup chipGroup = new ChipGroup(requireContext());

        // Sort industries alphabetically by industry name
        List<Map.Entry<String, String>> sortedIndustries = new ArrayList<>(industryMap.entrySet());
        sortedIndustries.sort(Map.Entry.comparingByValue());

        for (Map.Entry<String, String> entry : sortedIndustries) {
            String industryID = entry.getKey();
            String industryName = entry.getValue();

            Chip chip = new Chip(requireContext());
            chip.setText(industryName);
            chip.setTag(industryID);
            chip.setCheckable(true);

            if (selectedIndustryIds.contains(industryID)) {
                chip.setChecked(true);
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedIndustryIds.add(industryID);
                } else {
                    selectedIndustryIds.remove(industryID);
                }
                refreshMainChipGroup();
            });

            chipGroup.addView(chip);
        }

        // Search functionality to dynamically filter chips
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChips(s.toString(), chipGroup, sortedIndustries);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        LinearLayout dialogLayout = new LinearLayout(requireContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.addView(titleTextView);
        dialogLayout.addView(searchBar);
        dialogLayout.addView(chipGroup);

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogLayout)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    refreshMainChipGroup();
                })
                .show();
    }


    private void refreshMainChipGroup() {
        chipGroupIndustries.removeAllViews();

        for (String industryID : selectedIndustryIds) {
            String industryName = industryMap.get(industryID);

            Chip chip = new Chip(requireContext());
            chip.setText(industryName != null ? industryName : "Unknown Industry");
            chip.setTag(industryID);
            chip.setCheckable(true);
            chip.setChecked(true);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isChecked) {
                    selectedIndustryIds.remove(industryID);
                    refreshMainChipGroup();  // Sync UI if chip is unselected
                }
            });

            chipGroupIndustries.addView(chip);
        }

        Chip moreChip = new Chip(requireContext());
        moreChip.setText("...");
        moreChip.setOnClickListener(v -> showAllIndustriesPopup(getIndustriesArray()));
        chipGroupIndustries.addView(moreChip);
    }

    private void sendIndustryData(String questionId) {
        ArrayList<String> selectedIndustries = new ArrayList<>();

        // Collect selected industries from the chip group
        for (int i = 0; i < chipGroupIndustries.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupIndustries.getChildAt(i);
            if (chip.isChecked() && chip.getTag() != null) {
                selectedIndustries.add((String) chip.getTag());
            }
        }

        // Send a separate request for each industry
        for (String industryId : selectedIndustries) {
            Map<String, String> params = new HashMap<>();
            params.put("questionId", questionId);
            params.put("industryId", industryId);
            Log.d("IndustryParams", params.toString());

            // Send each industry association separately
            postData(url_questionindustry, params, false);
        }
        refreshMainChipGroup();
    }

    // Method to handle server response and call sendIndustryData()
    private void handleServerResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);

            if (jsonResponse.has("questionId")) {
                String questionId = jsonResponse.getString("questionId");
                Log.d("Received Question ID", "Question ID: " + questionId);

                // Call sendIndustryData() with the retrieved internshipId
                sendIndustryData(questionId);
            } else {
                Log.e("Response Error", "No question ID found in response.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void populateIndustryMap(String[] industries) {
        for (String industry : industries) {
            if (!industry.isEmpty()) {
                String[] details = industry.split(";");
                if (details.length >= 2) {
                    String industryID = details[0];
                    String industryName = details[1];
                    industryMap.put(industryID, industryName);
                }
            }
        }
    }
    private String[] getIndustriesArray() {
        String[] industries = new String[industryMap.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : industryMap.entrySet()) {
            industries[i++] = entry.getKey() + ";" + entry.getValue();
        }
        return industries;
    }

    private void filterChips(String query, ChipGroup chipGroup, List<Map.Entry<String, String>> industries) {
        chipGroup.removeAllViews();

        for (Map.Entry<String, String> entry : industries) {
            String industryID = entry.getKey();
            String industryName = entry.getValue();

            if (industryName.toLowerCase().contains(query.toLowerCase())) {
                Chip chip = new Chip(requireContext());
                chip.setText(industryName);
                chip.setTag(industryID);
                chip.setCheckable(true);

                if (selectedIndustryIds.contains(industryID)) {
                    chip.setChecked(true);
                }

                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedIndustryIds.add(industryID);
                    } else {
                        selectedIndustryIds.remove(industryID);
                    }
                    refreshMainChipGroup();
                });

                chipGroup.addView(chip);
            }
        }
    }
    public void showCreateSuccessfulAlert() {
        // Create LottieAnimationView dynamically
        LottieAnimationView lottieSuccess = new LottieAnimationView(requireContext());
        lottieSuccess.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        lottieSuccess.setAnimation(R.raw.success);  // Reference your Lottie JSON animation
        lottieSuccess.playAnimation();

        // Create a TextView for the success message
        TextView messageTextView = new TextView(requireContext());
        messageTextView.setText("Created Post Successfully!");
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
        messageTextView.setText("Fail to Post Question. Please try again!");
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