package mdad.localdata.intershipsharingapp;

import static android.content.Context.MODE_PRIVATE;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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
import java.util.Map;

/**
 * A fragment representing the "Create Internship" tab content.
 */
public class CreateInternshipFragment extends Fragment {
    private static String url_create_internship = StaffMainActivity.ipBaseAddress + "/create_internship.php";
    private static String url_internshipindustry = StaffMainActivity.ipBaseAddress + "/create_internship_industry.php";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    Button createButton;
    private ChipGroup chipGroupIndustries; // Reference to ChipGroup
    private EditText inputTitle,inputCompany,inputRole,inputDescription,startDateInput, endDateInput;
    String title, company, role, description, startDate, endDate, selectedLocationId, userId;
    private Spinner locationSpinner;
    ArrayList<String> locationNames = new ArrayList<>();
    ArrayList<String> locationIds = new ArrayList<>();

    public CreateInternshipFragment() {
        // Required empty public constructor
    }

    public static CreateInternshipFragment newInstance(String param1, String param2) {
        CreateInternshipFragment fragment = new CreateInternshipFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_internship, container, false);

        chipGroupIndustries = view.findViewById(R.id.chipGroupIndustries); // Initialize ChipGroup
        inputTitle = view.findViewById(R.id.inputTitle);
        inputCompany = view.findViewById(R.id.inputCompany);
        inputRole = view.findViewById(R.id.inputRole);
        inputDescription = view.findViewById(R.id.inputDescription);
        startDateInput = view.findViewById(R.id.startDateInput);
        endDateInput = view.findViewById(R.id.endDateInput);
        createButton = view.findViewById(R.id.createButton);
        locationSpinner = view.findViewById(R.id.location);


        // Set up DatePickerDialog for Start Date
        startDateInput.setOnClickListener(v -> showDatePickerDialog(startDateInput));

        // Set up DatePickerDialog for End Date
        endDateInput.setOnClickListener(v -> showDatePickerDialog(endDateInput));
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);

// Retrieve userId from SharedPreferences
        String userId = sharedPreferences.getString("userId", null);

// Print userId to log
        if (userId != null) {
            Log.d("SharedPreferences", "Retrieved userId: " + userId);
        } else {
            Log.d("SharedPreferences", "userId not found in SharedPreferences");
        }

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = inputTitle.getText().toString();
                company = inputCompany.getText().toString();
                description = inputDescription.getText().toString();
                role = inputRole.getText().toString();
                startDate = startDateInput.getText().toString();
                endDate = endDateInput.getText().toString();

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

                if (company.isEmpty()) {
                    Toast.makeText(requireContext(), "Username field is required", Toast.LENGTH_SHORT).show();
                    inputCompany.requestFocus();
                    return;
                }

                if (role.isEmpty()) {
                    Toast.makeText(requireContext(), "Password field is required", Toast.LENGTH_SHORT).show();
                    inputRole.requestFocus();
                    return;
                }

                if (startDate.isEmpty()) {
                    Toast.makeText(requireContext(), "Course field is required", Toast.LENGTH_SHORT).show();
                    startDateInput.requestFocus();
                    return;
                }

                if (endDate == null ) {
                    Toast.makeText(requireContext(), "Role selection is required", Toast.LENGTH_SHORT).show();
                    endDateInput.requestFocus();
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
                params.put("company", company);
                params.put("description", description);
                params.put("role", role);
                params.put("start_date", startDate);
                params.put("end_date", endDate);
                params.put("locationId", selectedLocationId);
                params.put("userId", userId);

                // Convert the selected industries to a comma-separated string
                String selectedIndustriesStr = TextUtils.join(",", selectedIndustries);
                params.put("industries", selectedIndustriesStr);

                Log.d("Params", params.toString()); // Log the params for debugging")

                // Send data to the create internship endpoint
                postData(url_create_internship, params);

                // Send data to the industry endpoint
                postData(url_internshipindustry, params);
            }
        });
        fetchLocations();
        fetchIndustries(); // Fetch and display industries
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLocationId = locationIds.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });// Update selectedRoleId based on the position

        return view;
    }
    private void fetchLocations() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url_create_internship,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("FetchLocations", "Server Response: " + response);

                        try {
                            locationNames.clear();
                            locationIds.clear();

                            // Add a placeholder at the start
                            locationNames.add("Select a Location");
                            locationIds.add("");

                            JSONArray locationsArray = new JSONArray(response);

                            for (int i = 0; i < locationsArray.length(); i++) {
                                JSONObject locationObject = locationsArray.getJSONObject(i);
                                String locationName = locationObject.getString("location_name");
                                String locationId = locationObject.getString("LocationID");

                                // Adding location names and their IDs to the lists
                                locationNames.add(locationName);
                                locationIds.add(locationId);
                            }

                            // Populate the Spinner with locations
                            // Populate the Spinner with locations
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, locationNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            locationSpinner.setAdapter(adapter);


                            // Enable the spinner after locations are loaded
                            locationSpinner.setEnabled(true);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "Error parsing locations", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                error -> {
                    Toast.makeText(requireContext(), "Error fetching locations: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );

        requestQueue.add(stringRequest);
    }

    private void postData(String url, Map<String, String> params) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("ServerResponse", "Response: " + response);
                    String cleanResponse = response.trim().replaceAll("<[^>]*>", "");

                    if ("Successful".equals(cleanResponse)) {
                        Toast.makeText(requireContext(), "Data sent successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to send data: " + cleanResponse, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error: " + error.toString());
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
                    if (response.equals("Error")) {
                        Toast.makeText(requireContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String[] industries = response.split(":");
                    int maxVisibleChips = 4; // Number of chips to display before "..."

                    for (int i = 0; i < industries.length; i++) {
                        String location = industries[i];
                        if (!location.isEmpty()) {
                            String[] details = location.split(";");
                            if (details.length >= 2) { // Ensure there are enough details
                                String locationID = details[0];
                                String locationName = details[1];

                                if (i < maxVisibleChips) {
                                    // Create chips for the first 4 industries
                                    Chip chip = new Chip(requireContext());
                                    chip.setText(locationName);
                                    chip.setTag(locationID);
                                    chip.setCheckable(true);
                                    chipGroupIndustries.addView(chip);
                                } else if (i == maxVisibleChips) {
                                    // Create the "..." chip
                                    Chip moreChip = new Chip(requireContext());
                                    moreChip.setText("...");
                                    moreChip.setCheckable(false); // Not selectable
                                    moreChip.setOnClickListener(v -> showAllIndustriesPopup(industries));
                                    chipGroupIndustries.addView(moreChip);
                                    break;
                                }
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
        // Create a TextView for the title or instructions
        TextView titleTextView = new TextView(requireContext());
        titleTextView.setText("Select Relevant Industry");
        titleTextView.setTextSize(18);
        titleTextView.setTextColor(Color.BLACK);
        titleTextView.setPadding(0, 20, 0, 10);
        titleTextView.setGravity(Gravity.CENTER);

        // Create a search bar (EditText)
        EditText searchBar = new EditText(requireContext());
        searchBar.setHint("Search industries...");
        searchBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        searchBar.setPadding(20, 10, 20, 10);
        searchBar.setBackgroundResource(android.R.drawable.edit_text);
        searchBar.setSingleLine(true);

        // Create a ChipGroup dynamically
        ChipGroup chipGroup = new ChipGroup(requireContext());
        chipGroup.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        chipGroup.setPadding(10, 10, 10, 10);
        chipGroup.setSingleSelection(false); // Allow multiple selections if needed

        // List to track selected chips
        HashMap<String, Chip> selectedChips = new HashMap<>();

        // Populate the ChipGroup with chips
        HashMap<String, Chip> chipMap = new HashMap<>();
        for (String industry : industries) {
            if (!industry.isEmpty()) {
                String[] details = industry.split(";");
                if (details.length >= 2) {
                    String industryID = details[0];
                    String industryName = details[1];

                    // Create a Chip
                    Chip chip = new Chip(requireContext());
                    chip.setText(industryName);
                    chip.setTag(industryID);
                    chip.setCheckable(true); // Allow selection
                    chip.setPadding(20, 10, 20, 10);

                    // Store chip in the map for filtering
                    chipMap.put(industryName.toLowerCase(), chip);

                    // Add a listener to track selections
                    chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            // Add to selected chips
                            selectedChips.put(industryName.toLowerCase(), chip);
                        } else {
                            // Remove from selected chips
                            selectedChips.remove(industryName.toLowerCase());
                        }
                        refreshChips(searchBar.getText().toString(), chipGroup, chipMap, selectedChips);
                    });

                    // Add the chip to the ChipGroup initially
                    chipGroup.addView(chip);
                }
            }
        }

        // Add a TextWatcher to the search bar to filter chips dynamically
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshChips(s.toString(), chipGroup, chipMap, selectedChips);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        // Create a vertical layout for the popup
        LinearLayout dialogLayout = new LinearLayout(requireContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 20, 50, 20);
        dialogLayout.setGravity(Gravity.CENTER);
        dialogLayout.addView(titleTextView);
        dialogLayout.addView(searchBar); // Add the search bar below the title
        dialogLayout.addView(chipGroup);

        // Show the dialog with the custom view
        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogLayout)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Handle user selections
                    for (int i = 0; i < chipGroup.getChildCount(); i++) {
                        Chip chip = (Chip) chipGroup.getChildAt(i);
                        if (chip.isChecked()) {
                            String selectedIndustry = chip.getText().toString();
                            // Handle the selected industry (e.g., store or log it)
                            Log.d("SelectedIndustry", "Selected: " + selectedIndustry);
                        }
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Refresh the ChipGroup to show selected chips first
    private void refreshChips(String query, ChipGroup chipGroup, HashMap<String, Chip> chipMap, HashMap<String, Chip> selectedChips) {
        chipGroup.removeAllViews(); // Clear existing chips

        // Add selected chips first
        for (String key : selectedChips.keySet()) {
            if (key.contains(query.toLowerCase())) {
                chipGroup.addView(selectedChips.get(key));
            }
        }

        // Add unselected chips
        for (String key : chipMap.keySet()) {
            if (!selectedChips.containsKey(key) && key.contains(query.toLowerCase())) {
                chipGroup.addView(chipMap.get(key));
            }
        }
    }

    private void showDatePickerDialog(EditText editText) {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create and show the DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format and set the selected date in the EditText
                    String selectedDate =  selectedYear+ "/" + (selectedMonth + 1) + "/" + selectedDay;
                    editText.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

}