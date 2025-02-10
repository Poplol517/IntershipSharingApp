package mdad.localdata.intershipsharingapp;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class ViewIndustryFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout listLayout;
    private static final String url_get_industry = StaffMainActivity.ipBaseAddress + "/get_all_industry.php";
    private static final String url_delete_industry = StaffMainActivity.ipBaseAddress + "/delete_industry.php";
    private static final String url_edit_industry = StaffMainActivity.ipBaseAddress + "/edit_industry.php";
    private static final String url_create_industry = StaffMainActivity.ipBaseAddress + "/create_industry.php";

    public ViewIndustryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_industry, container, false);

        // Initialize views
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        listLayout = view.findViewById(R.id.list);

        // Set toolbar title dynamically (Optional)
        TextView title = view.findViewById(R.id.title);
        title.setText("View Industries");

        // Add Plus button click listener
        ImageButton btnAddIndustry = view.findViewById(R.id.btn_add_industry);
        btnAddIndustry.setOnClickListener(v -> {
            // Show the BottomSheet for adding a new industry
            showAddIndustryBottomSheet();
        });

        // Fetch industry data initially
        fetchIndustryData();

        // Refresh industry list on swipe down
        swipeRefreshLayout.setOnRefreshListener(this::fetchIndustryData);

        return view;
    }

    private void fetchIndustryData() {
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_get_industry,
                response -> {
                    if (response.equals("Error")) {
                        Toast.makeText(requireContext(), "Error in retrieving industry data", Toast.LENGTH_LONG).show();
                        swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation in case of error
                        return;
                    }

                    listLayout.removeAllViews(); // Clear existing views before adding new ones

                    String[] industries = response.split(":"); // Assuming data is separated by ":"

                    for (String industry : industries) {
                        if (!industry.isEmpty()) {
                            String[] details = industry.split(";"); // Assuming details are separated by ";"
                            if (details.length >= 2) { // Adjust based on actual API response
                                String industryId = details[0]; // ID of industry
                                String industryName = details[1]; // Name of industry

                                // Create a clickable LinearLayout for each industry
                                LinearLayout itemLayout = new LinearLayout(requireContext());
                                itemLayout.setOrientation(LinearLayout.VERTICAL);
                                itemLayout.setPadding(20, 20, 20, 20);
                                itemLayout.setBackgroundResource(android.R.color.darker_gray);

                                // Create a new TextView for industry name
                                TextView textView = new TextView(requireContext());
                                textView.setText(industryName);
                                textView.setTextSize(18);
                                textView.setPadding(20, 20, 20, 20);
                                textView.setTextColor(getResources().getColor(android.R.color.white));

                                // Add TextView to LinearLayout
                                itemLayout.addView(textView);

                                // Set click listener to open BottomSheet
                                itemLayout.setOnClickListener(v -> showBottomSheet(industryId, industryName));

                                // Add to the list layout
                                listLayout.addView(itemLayout);
                            }
                        }
                    }

                    swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation after data is loaded
                },

                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error retrieving industry data", Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation in case of error
                });

        queue.add(stringRequest);
    }

    private void showBottomSheet(String industryId, String industryName) {
        // Create BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_industry, null);
        bottomSheetDialog.setContentView(sheetView);

        // Get button references
        Button btnEditIndustry = sheetView.findViewById(R.id.btn_edit_industry);
        Button btnDelete = sheetView.findViewById(R.id.btn_delete);
        LinearLayout editLayout = sheetView.findViewById(R.id.edit_layout);
        EditText editIndustryName = sheetView.findViewById(R.id.edit_industry_name);
        Button btnSaveIndustry = sheetView.findViewById(R.id.btn_save_industry);

        // Set text dynamically
        TextView title = sheetView.findViewById(R.id.txt_industry_title);
        title.setText(industryName);

        // Handle Edit Industry Button
        btnEditIndustry.setOnClickListener(v -> {
            // Show the Edit Text and Save button, hide the other buttons
            editLayout.setVisibility(View.VISIBLE);
            btnEditIndustry.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        });

        // Handle Save Industry Button
        btnSaveIndustry.setOnClickListener(v -> {
            String updatedName = editIndustryName.getText().toString();
            if (!updatedName.isEmpty()) {
                // Code to update industry (e.g., make an API call)
                editIndustry(industryId, updatedName, bottomSheetDialog);
            } else {
                Toast.makeText(requireContext(), "Please enter a valid name", Toast.LENGTH_SHORT).show();
            }
        });


        // Handle Delete Industry Button
        btnDelete.setOnClickListener(v -> {
            // Call the delete industry API
            deleteIndustry(industryId, bottomSheetDialog);
        });

        bottomSheetDialog.show();
    }

    private void editIndustry(String industryId, String updatedName, BottomSheetDialog bottomSheetDialog) {
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_edit_industry,
                response -> {
                    if (response.equals("Success")) {
                        Toast.makeText(requireContext(), "Industry updated successfully", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        fetchIndustryData(); // Refresh the list after editing
                    } else {
                        Toast.makeText(requireContext(), "Error updating industry", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error updating industry", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("industryId", industryId); // Send the industryId to edit
                params.put("name", updatedName); // Send the updated industry name
                return params;
            }
        };

        queue.add(stringRequest);
    }


    private void deleteIndustry(String industryId, BottomSheetDialog bottomSheetDialog) {
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_delete_industry,
                response -> {
                    if (response.equals("Success")) {
                        Toast.makeText(requireContext(), "Industry deleted successfully", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        fetchIndustryData(); // Refresh the list after deletion
                    } else {
                        Toast.makeText(requireContext(), "Error deleting industry", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error deleting industry", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("industryId", industryId); // Send the industryId to delete
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void showAddIndustryBottomSheet() {
        // Create BottomSheetDialog for adding industry
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_industry, null);
        bottomSheetDialog.setContentView(sheetView);

        // Get button references
        Button btnEditIndustry = sheetView.findViewById(R.id.btn_edit_industry);
        Button btnDelete = sheetView.findViewById(R.id.btn_delete);
        LinearLayout editLayout = sheetView.findViewById(R.id.edit_layout);
        EditText editIndustryName = sheetView.findViewById(R.id.edit_industry_name);
        Button btnSaveIndustry = sheetView.findViewById(R.id.btn_save_industry);

        // Set text dynamically
        TextView title = sheetView.findViewById(R.id.txt_industry_title);
        title.setText("Add New Industry"); // Change title for Add Industry
        btnSaveIndustry.setText("Add New Industry");

        // Hide unnecessary buttons for adding industry
        btnEditIndustry.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);

        // Show the Edit Text and Save button
        editLayout.setVisibility(View.VISIBLE);

        // Handle Save Industry Button (for adding new industry)
        btnSaveIndustry.setOnClickListener(v -> {
            String newIndustryName = editIndustryName.getText().toString();
            if (!newIndustryName.isEmpty()) {
                // Call the API to add the industry
                addIndustry(newIndustryName, bottomSheetDialog);
            } else {
                Toast.makeText(requireContext(), "Please enter a valid name", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.show();
    }

    private void addIndustry(String newIndustryName, BottomSheetDialog bottomSheetDialog) {
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_create_industry,
                response -> {
                    if (response.equals("Success")) {
                        Toast.makeText(requireContext(), "Industry added successfully", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        fetchIndustryData(); // Refresh the list after adding
                    } else {
                        Toast.makeText(requireContext(), "Error adding industry", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error adding industry", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", newIndustryName); // Send the new industry name
                return params;
            }
        };

        queue.add(stringRequest);
    }
}
