package mdad.localdata.intershipsharingapp;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;

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

public class EditInternshipActivity extends AppCompatActivity {
    private ChipGroup chipGroupIndustries;
    private static final String url_edit_internship = StaffMainActivity.ipBaseAddress + "/edit_internship.php";

    private static final String url_internship_industry = StaffMainActivity.ipBaseAddress + "/edit_internship_industry.php";
    private static final String url_all_industry = StaffMainActivity.ipBaseAddress + "/get_all_industry.php";
    private Map<String, String> industryMap = new HashMap<>();
    private List<String> selectedIndustryIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_internship);

        // Retrieve data from the intent
        Intent intent = getIntent();
        String internshipId = intent.getStringExtra("InternshipID");
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String company = intent.getStringExtra("company");
        String startDate = intent.getStringExtra("start_date");
        String endDate = intent.getStringExtra("end_date");
        String dateShared = intent.getStringExtra("date_shared");
        String locationName = intent.getStringExtra("location_name");
        String role = intent.getStringExtra("role");

        // Initialize the location spinner
        Spinner locationSpinner = findViewById(R.id.location);

        // Fetch locations from the backend
        fetchLocations(locationSpinner, locationName);

        // Populate other fields with retrieved data
        EditText titleField = findViewById(R.id.inputTitle);
        EditText descriptionField = findViewById(R.id.inputDescription);
        EditText companyField = findViewById(R.id.inputCompany);
        EditText startDateField = findViewById(R.id.startDateInput);
        EditText endDateField = findViewById(R.id.endDateInput);
        EditText roleField = findViewById(R.id.inputRole);

        titleField.setText(title);
        descriptionField.setText(description);
        companyField.setText(company);
        startDateField.setText(startDate);
        endDateField.setText(endDate);
        roleField.setText(role);

        // Fetch industries associated with this internship
        fetchIndustries();

        // Set up the update button click listener
        findViewById(R.id.updateButton).setOnClickListener(v -> {
            String updatedTitle = titleField.getText().toString();
            String updatedDescription = descriptionField.getText().toString();
            String updatedCompany = companyField.getText().toString();
            String updatedStartDate = startDateField.getText().toString();
            String updatedEndDate = endDateField.getText().toString();
            String updatedLocation = locationSpinner.getSelectedItem().toString();
            String updatedRole = roleField.getText().toString();

            updateInternship(internshipId, updatedTitle, updatedDescription, updatedCompany, updatedStartDate, updatedEndDate, updatedLocation, updatedRole);
        });
    }

    private void updateInternship(String internshipId, String title, String description, String company, String startDate, String endDate, String location, String role) {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_edit_internship,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(EditInternshipActivity.this, "Internship updated successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity after the update
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(EditInternshipActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("internshipId", internshipId);
                params.put("title", title);
                params.put("description", description);
                params.put("company", company);
                params.put("start_date", startDate);
                params.put("end_date", endDate);
                params.put("locationId", location); // Assuming locationId is a string
                params.put("role", role);
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void fetchLocations(Spinner locationSpinner, String locationName) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url_edit_internship,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray locationsArray = new JSONArray(response);
                            ArrayList<String> locationNames = new ArrayList<>();
                            ArrayList<String> locationIds = new ArrayList<>();

                            locationNames.add("Select a Location");
                            locationIds.add("");

                            for (int i = 0; i < locationsArray.length(); i++) {
                                JSONObject locationObject = locationsArray.getJSONObject(i);
                                String locationName = locationObject.getString("location_name");
                                String locationId = locationObject.getString("LocationID");

                                locationNames.add(locationName);
                                locationIds.add(locationId);
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(EditInternshipActivity.this, android.R.layout.simple_spinner_item, locationNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            locationSpinner.setAdapter(adapter);

                            if (locationNames.contains(locationName)) {
                                locationSpinner.setSelection(locationNames.indexOf(locationName));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(EditInternshipActivity.this, "Error parsing locations", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                error -> {
                    Toast.makeText(EditInternshipActivity.this, "Error fetching locations: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );

        requestQueue.add(stringRequest);
    }

    private void fetchIndustries() {
        RequestQueue queue = Volley.newRequestQueue(this);

        // First, fetch the list of all industries from the URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url_all_industry,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Split the response string into industry items
                            String[] industries = response.split(":");
                            Log.d("Industries", Arrays.toString(industries));
                            industryMap.clear(); // Clear the map to avoid duplicates

                            // Parse each industry
                            for (String industry : industries) {
                                if (!industry.isEmpty()) {
                                    String[] parts = industry.split(";");
                                    Log.d("Industry Parts", Arrays.toString(parts));
                                    if (parts.length >= 2) {
                                        String industryName = parts[1];
                                        String industryID = parts[0];
                                        industryMap.put(industryID, industryName);
                                        Log.d("Industry Map", industryMap.toString());
                                    }
                                }
                            }

                            // Fetch the industries associated with the current internship
                            fetchSelectedIndustries();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(EditInternshipActivity.this, "Error parsing industries", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(EditInternshipActivity.this, "Error fetching industries: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );

        queue.add(stringRequest);
    }

    private void fetchSelectedIndustries() {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Fetch selected industries from the internship
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url_internship_industry,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Log the response to check its format
                            Log.d("Response", "Selected Industries Response: " + response);

                            // Parse the response as a JSON array
                            JSONArray selectedIndustriesArray = new JSONArray(response);
                            selectedIndustryIds.clear(); // Clear the existing selection

                            // Retrieve the internship ID from the intent or your data source
                            Intent intent = getIntent();
                            String internshipId = intent.getStringExtra("InternshipID");

                            // Loop through the response and check if the internship ID matches
                            for (int i = 0; i < selectedIndustriesArray.length(); i++) {
                                JSONObject industryObject = selectedIndustriesArray.getJSONObject(i);
                                String currentInternshipId = industryObject.getString("InternshipID");
                                String industryID = industryObject.getString("IndustryID");

                                // If the InternshipID matches the selected internship, add the IndustryID
                                if (currentInternshipId.equals(internshipId)) {
                                    selectedIndustryIds.add(industryID);
                                    Log.d("SelectedIndustry", "IndustryID: " + industryID);
                                }
                            }

                            // Now refresh the chip group with the selected industries
                            refreshMainChipGroup();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(EditInternshipActivity.this, "Error fetching selected industries", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(EditInternshipActivity.this, "Error fetching selected industries: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );

        queue.add(stringRequest);
    }

    private void refreshMainChipGroup() {
        chipGroupIndustries = findViewById(R.id.chipGroupIndustries);
        chipGroupIndustries.removeAllViews();

        Log.d("Selected Industries before UI update", selectedIndustryIds.toString());

        // Display selected industries as chips
        for (Map.Entry<String, String> entry : industryMap.entrySet()) {
            String industryID = entry.getKey();  // Original ID from response
            String industryName = entry.getValue();

            Log.d("Industry Details", "IndustryID: " + industryID + " | IndustryName: " + industryName);

            // Create a chip for each selected industry
            if (selectedIndustryIds.contains(industryID)) {
                Chip chip = new Chip(this);
                chip.setText(industryName);

                // Mark as checked
                chip.setChecked(true);

                // Set chip tag as industryID
                chip.setTag(industryID);

                chip.setCheckable(false);  // Chips in the main display should not be checkable

                // Add the chip to the chip group
                chipGroupIndustries.addView(chip);
            }
        }

        // Add "..." chip to show the popup
        Chip moreChip = new Chip(this);
        moreChip.setText("...");
        moreChip.setCheckable(false);
        moreChip.setOnClickListener(v -> showAllIndustriesPopup());  // Open the popup when clicked
        chipGroupIndustries.addView(moreChip);

        chipGroupIndustries.invalidate();  // Refresh the chip group to apply changes
    }

    private void showAllIndustriesPopup() {
        TextView titleTextView = new TextView(this);
        titleTextView.setText("Select Relevant Industry");
        titleTextView.setTextSize(18);
        titleTextView.setGravity(Gravity.CENTER);

        EditText searchBar = new EditText(this);
        searchBar.setHint("Search industries...");
        searchBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        searchBar.setPadding(20, 10, 20, 10);
        searchBar.setBackgroundResource(android.R.drawable.edit_text);
        searchBar.setSingleLine(true);

        ChipGroup chipGroup = new ChipGroup(this);
        populateChips(chipGroup, industryMap, searchBar);

        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.addView(titleTextView);
        dialogLayout.addView(searchBar);
        dialogLayout.addView(chipGroup);

        new MaterialAlertDialogBuilder(this)
                .setView(dialogLayout)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    refreshMainChipGroup();  // Refresh chips after selecting industries
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void populateChips(ChipGroup chipGroup, Map<String, String> industries, EditText searchBar) {
        chipGroup.removeAllViews();

        // Sort the industries alphabetically by name
        List<Map.Entry<String, String>> sortedIndustries = new ArrayList<>(industries.entrySet());
        sortedIndustries.sort(Map.Entry.comparingByValue());
        Log.d("Sorted Industries", sortedIndustries.toString());

        for (Map.Entry<String, String> entry : sortedIndustries) {
            String industryID = entry.getKey();
            String industryName = entry.getValue();

            Chip chip = new Chip(this);
            chip.setText(industryName);
            chip.setTag(industryID);
            chip.setCheckable(true);

            // Mark the chip as checked if it exists in selectedIndustryIds
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

            chipGroup.addView(chip);
        }

        // Add search functionality to dynamically filter chips
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
    }
    private void postData(String url, Map<String, String> params, boolean isUpdateInternshipIndustry) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("ServerResponse", "Raw Response: " + response);

                    try {
                        String cleanResponse = response.trim().replaceAll("<[^>]*>", "");

                        if (isUpdateInternshipIndustry) {
                            JSONObject jsonResponse = new JSONObject(cleanResponse);

                            if (jsonResponse.has("success")) {
                                String successMessage = jsonResponse.getString("success");
                                Log.d("IndustryUpdate", "Industry Update Success: " + successMessage);

                                // Optionally handle the success response or refresh UI
                                Toast.makeText(this, "Internship industry updated successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("ResponseError", "No success message in the response");
                                Toast.makeText(this, "Error updating internship industries", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("JSONParsingError", "Error parsing response: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("VolleyError", "Network Error: " + error.toString());
                    Toast.makeText(this, "Network Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void sendIndustryData(String internshipId) {
        ArrayList<String> selectedIndustries = new ArrayList<>();

        // Collect selected industries from the chip group
        for (int i = 0; i < chipGroupIndustries.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupIndustries.getChildAt(i);
            if (chip.isChecked() && chip.getTag() != null) {
                selectedIndustries.add((String) chip.getTag());
            }
        }

        // Now update the internship with the new industries
        Map<String, String> params = new HashMap<>();
        params.put("internshipId", internshipId);

        // Clear the existing industries and add the selected ones
        for (String industryId : selectedIndustries) {
            params.put("industryId", industryId);
            Log.d("IndustryParams", "Updating industry: " + industryId);

            // Send each industry association to update the internship
            postData(url_edit_internship, params, true);
        }

        refreshMainChipGroup(); // Optionally refresh the UI with updated industries
    }


    private void filterChips(String query, ChipGroup chipGroup, List<Map.Entry<String, String>> sortedIndustries) {
        chipGroup.removeAllViews();

        for (Map.Entry<String, String> entry : sortedIndustries) {
            String industryName = entry.getValue();

            if (industryName.toLowerCase().contains(query.toLowerCase())) {
                String industryID = entry.getKey();

                Chip chip = new Chip(this);
                chip.setText(industryName);
                chip.setTag(industryID);
                chip.setCheckable(true);

                // Mark the chip as checked if it exists in selectedIndustryIds
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

                chipGroup.addView(chip);
            }
        }
    }
}
