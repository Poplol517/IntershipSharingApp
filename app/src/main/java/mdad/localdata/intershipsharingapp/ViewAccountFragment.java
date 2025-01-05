package mdad.localdata.intershipsharingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ViewAccountFragment extends Fragment {
    private static String url_view_account = StaffMainActivity.ipBaseAddress + "/get_all_user.php";
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private TextView tvName, tvRole, tvCourse, tvStudyYear;
    private ImageView profileIcon;
    private Button btnEditProfile, btnLogout,btnEditProfilePic,btnRemoveProfilePic;
    private LinearLayout accountSection;

    public ViewAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_account, container, false);

        // Initialize UI components for account section
        profileIcon = view.findViewById(R.id.profileIcon);
        tvName = view.findViewById(R.id.tvName);
        tvRole = view.findViewById(R.id.tvRole);
        tvCourse = view.findViewById(R.id.tvCourse);
        tvStudyYear = view.findViewById(R.id.tvStudyYear);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnEditProfilePic = view.findViewById(R.id.btnEditProfilePic);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnRemoveProfilePic = view.findViewById(R.id.btnRemoveProfilePic);
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

        btnEditProfilePic.setOnClickListener(v -> {
            // Navigate to EditProfileFragment
            UpdateProfilePictureFragment updateProfilePictureFragment = new UpdateProfilePictureFragment();

            // Start the fragment transaction
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, updateProfilePictureFragment) // Replace with your container ID
                    .addToBackStack(null) // Adds the transaction to the back stack
                    .commit();
        });

        btnRemoveProfilePic.setOnClickListener(v -> {
            removeProfilePicture();
        });

        btnEditProfile.setOnClickListener(v -> {
            // Navigate to EditProfileFragment
            EditAccountFragment editProfileFragment = new EditAccountFragment();

            // Start the fragment transaction
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, editProfileFragment) // Replace with your container ID
                    .addToBackStack(null) // Adds the transaction to the back stack
                    .commit();
        });


        btnLogout.setOnClickListener(v -> {
            // Clear SharedPreferences
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", getContext().MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Display a logout confirmation message
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Redirect to the Login Activity (assuming LoginActivity is the name of the login screen)
            navigateToLogin();
        });

        return view;
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
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
                                    userDetails.put("photo", details.length > 10 ? details[10] : "");

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
        Log.d("UserDetails", "Displaying Details: " + userDetails);
        if ("1".equals(role)) { // Role 1 - Student
            tvName.setText(userDetails.get("name"));
            tvRole.setText(userDetails.get("role"));
            tvCourse.setText(userDetails.get("course"));
            tvStudyYear.setText("Year of Study: " + userDetails.get("study_year"));

            String photoData = userDetails.get("photo");
            Log.d("UserDetails", "Photo Data: " + userDetails.get("photo"));
            if (photoData != null && !photoData.isEmpty()) {
                saveBase64ToFile(photoData, file -> {
                    // Once the image is saved, decode the file to Bitmap
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bitmap != null) {
                        profileIcon.setImageBitmap(bitmap);
                    } else {
                        Log.e("ImageError", "Failed to decode bitmap from file.");
                        profileIcon.setImageResource(R.drawable.account); // Default image
                    }
                });
            } else {
                profileIcon.setImageResource(R.drawable.account); // Default image
            }

        } else if ("2".equals(role)) { // Role 2 - Alumni
            tvName.setText(userDetails.get("name"));
            tvRole.setText(userDetails.get("role"));
            tvCourse.setText(userDetails.get("course"));
            tvStudyYear.setText("Graduated Year: " + userDetails.get("graduated_year"));

            String photoData = userDetails.get("photo");
            if (photoData != null && !photoData.isEmpty()) {
                try {
                    photoData = photoData.replace("\n", "").replace("\r", "");
                    Log.d("UserDetails", "Photo Data: " + photoData);
                    saveBase64ToFile(photoData, file -> {
                        // Once the image is saved, decode the file to Bitmap
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        if (bitmap != null) {
                            profileIcon.setImageBitmap(bitmap);
                        } else {
                            Log.e("ImageError", "Failed to decode bitmap from file.");
                            profileIcon.setImageResource(R.drawable.account); // Default image
                        }
                    });
                } catch (IllegalArgumentException e) {
                    Log.e("Base64Error", "Invalid Base64 data: " + e.getMessage());
                    profileIcon.setImageResource(R.drawable.account); // Default image
                }
            } else {
                profileIcon.setImageResource(R.drawable.account); // Default image
            }
        }
    }

    // Method to decode Base64 string and save it to a file
    private void saveBase64ToFile(String base64Data, OnFileSavedListener listener) {
        try {
            byte[] decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);

            // Save the decoded bytes to a file in cache directory
            File cacheDir = requireContext().getCacheDir();
            File imageFile = new File(cacheDir, "profile_image.jpg");

            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(decodedBytes);
            fos.close();

            // Notify that the file has been saved
            listener.onFileSaved(imageFile);
        } catch (Exception e) {
            Log.e("FileSaveError", "Error saving Base64 to file: " + e.getMessage());
        }
    }

    // Callback interface for file saving completion
    public interface OnFileSavedListener {
        void onFileSaved(File file);
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

    private void removeProfilePicture() {
        String urlRemoveProfilePicture = StaffMainActivity.ipBaseAddress + "/delete_profile_picture.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        // Get the logged-in user's ID from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", getContext().MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("username", ""); // Assuming 'username' is used as userID

        // Create a POST request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlRemoveProfilePicture,
                response -> {
                    if (response.equals("Success")) {
                        Toast.makeText(requireContext(), "Profile picture removed successfully", Toast.LENGTH_SHORT).show();
                        profileIcon.setImageResource(R.drawable.account); // Reset to default image
                    } else {
                        Toast.makeText(requireContext(), "Failed to remove profile picture", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error communicating with the server", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Send userID as a POST parameter
                Map<String, String> params = new HashMap<>();
                params.put("userId", currentUserId);
                return params;
            }
        };

        // Add the request to the queue
        queue.add(stringRequest);
    }

}
