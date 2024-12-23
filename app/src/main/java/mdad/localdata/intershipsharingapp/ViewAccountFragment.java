package mdad.localdata.intershipsharingapp;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ViewAccountFragment extends Fragment {
    private static String url_view_account = StaffMainActivity.ipBaseAddress + "/get_all_user.php";
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private TextView tvName, tvRole, tvCourse, tvStudyYear;
    private Button btnEditProfile;
    private LinearLayout accountSection;

    public ViewAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_account, container, false);

        // Initialize UI components for account section
        tvName = view.findViewById(R.id.tvName);
        tvRole = view.findViewById(R.id.tvRole);
        tvCourse = view.findViewById(R.id.tvCourse);
        tvStudyYear = view.findViewById(R.id.tvStudyYear);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        accountSection = view.findViewById(R.id.accountSection);

        // Initialize TabLayout and ViewPager2
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        // Set up the ViewPager with an adapter
        FragmentAdapter adapter = new FragmentAdapter(getActivity());
        viewPager.setAdapter(adapter);

        // Link TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Internship");
                    break;
                case 1:
                    tab.setText("Question");
                    break;
                // Add more tabs if needed
            }
        }).attach();

        // Fetch and display logged-in user account details
        fetchUserDetails();

        return view;
    }

    private void fetchUserDetails() {
        String url_view_account = StaffMainActivity.ipBaseAddress + "/get_all_user.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        // Get the logged-in user's ID from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", getContext().MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("username", ""); // Assuming 'username' is used as userid

        Log.d("CurrentUserId", "Current User ID: " + currentUserId);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_view_account,
                response -> {
                    if (response.equals("Error")) {
                        Toast.makeText(requireContext(), "Error retrieving database", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Split the response into user entries
                    String[] users = response.split(":");
                    for (String user : users) {
                        if (!user.isEmpty()) {
                            String[] details = user.split(";");

                            if (details.length >= 5) { // Adjust based on your response structure
                                String userId = details[0]; // Assuming userId is the first field

                                // Match the logged-in user's ID
                                if (currentUserId.equals(userId)) {
                                    HashMap<String, String> userDetails = new HashMap<>();
                                    userDetails.put("name", details[1]);
                                    userDetails.put("username", details[3]);
                                    userDetails.put("role", details[9]);
                                    userDetails.put("course", details[5]);
                                    userDetails.put("study_year", details[6]);
                                    userDetails.put("graduated_year", details[7]);

                                    Log.d("UserDetails", "Fetched Details: " + userDetails);
                                    displayUserDetails(userDetails, details[8]);
                                    break; // Exit loop after finding the user
                                }
                            }
                        }
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error retrieving user details", Toast.LENGTH_LONG).show();
                });

        queue.add(stringRequest);
    }

    private void displayUserDetails(HashMap<String, String> userDetails, String role) {
        if ("1".equals(role)) { // Role 1 - Student
            tvName.setText(userDetails.get("name"));
            tvRole.setText(userDetails.get("role"));
            tvCourse.setText(userDetails.get("course"));
            tvStudyYear.setText("Year of Study: "+userDetails.get("study_year"));
        } else if ("2".equals(role)) { // Role 2 - Alumni
            tvName.setText(userDetails.get("name"));
            tvRole.setText(userDetails.get("role"));
            tvCourse.setText(userDetails.get("course"));
            tvStudyYear.setText("Graduated Year: "+userDetails.get("graduated_year"));
        }
    }


    private static class FragmentAdapter extends FragmentStateAdapter {
        public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ViewAccountInternshipFragment(); // Your User Info Fragment
                case 1:
                    return new ViewAccountQuestionFragment(); // Your Internship Posts Fragment
                default:
                    return new ViewAccountFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Adjust based on number of tabs
        }
    }
}
