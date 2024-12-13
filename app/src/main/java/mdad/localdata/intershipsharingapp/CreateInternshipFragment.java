package mdad.localdata.intershipsharingapp;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.HashMap;

/**
 * A fragment representing the "Create Internship" tab content.
 */
public class CreateInternshipFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ChipGroup chipGroupLocations; // Reference to ChipGroup

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

        chipGroupLocations = view.findViewById(R.id.chipGroupLocations); // Initialize ChipGroup

        fetchLocations(); // Fetch and display locations

        return view;
    }

    private void fetchLocations() {
        String url_all_location = StaffMainActivity.ipBaseAddress + "/get_all_industry.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_all_location,
                response -> {
                    if (response.equals("Error")) {
                        Toast.makeText(requireContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String[] locations = response.split(":");
                    int maxVisibleChips = 4; // Number of chips to display before "..."

                    for (int i = 0; i < locations.length; i++) {
                        String location = locations[i];
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
                                    chipGroupLocations.addView(chip);
                                } else if (i == maxVisibleChips) {
                                    // Create the "..." chip
                                    Chip moreChip = new Chip(requireContext());
                                    moreChip.setText("...");
                                    moreChip.setCheckable(false); // Not selectable
                                    moreChip.setOnClickListener(v -> showAllIndustriesPopup(locations));
                                    chipGroupLocations.addView(moreChip);
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

    public void showAllIndustriesPopup(String[] locations) {
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
        for (String location : locations) {
            if (!location.isEmpty()) {
                String[] details = location.split(";");
                if (details.length >= 2) {
                    String locationID = details[0];
                    String locationName = details[1];

                    // Create a Chip
                    Chip chip = new Chip(requireContext());
                    chip.setText(locationName);
                    chip.setTag(locationID);
                    chip.setCheckable(true); // Allow selection
                    chip.setPadding(20, 10, 20, 10);

                    // Store chip in the map for filtering
                    chipMap.put(locationName.toLowerCase(), chip);

                    // Add a listener to track selections
                    chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            // Add to selected chips
                            selectedChips.put(locationName.toLowerCase(), chip);
                        } else {
                            // Remove from selected chips
                            selectedChips.remove(locationName.toLowerCase());
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
}