package mdad.localdata.intershipsharingapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

                    for (String location : locations) {
                        if (!location.isEmpty()) {
                            String[] details = location.split(";");
                            if (details.length >= 2) { // Ensure there are enough details
                                String locationID = details[0];
                                String locationName = details[1];
                                Log.d("DetailsArray", "Size: " + details.length + ", Content: " + Arrays.toString(details));

                                // Create a chip for each location
                                Chip chip = new Chip(requireContext());
                                chip.setText(locationName); // Display the location name
                                chip.setTag(locationID); // Optionally store the location ID
                                chip.setCheckable(true); // Allow the chip to be selected

                                // Add the chip to the ChipGroup
                                chipGroupLocations.addView(chip);
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
}
