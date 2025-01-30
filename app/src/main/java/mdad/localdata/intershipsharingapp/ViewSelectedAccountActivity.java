package mdad.localdata.intershipsharingapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

public class ViewSelectedAccountActivity extends AppCompatActivity {

    private TextView tvName, tvRole, tvCourse, tvStudyYear;
    private ImageView profileIcon;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private static final String urlViewAllUser = StaffMainActivity.ipBaseAddress + "/get_all_user.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_selected_account);

        // Initialize views
        profileIcon = findViewById(R.id.profileIcon);
        tvName = findViewById(R.id.tvName);
        tvRole = findViewById(R.id.tvRole);
        tvCourse = findViewById(R.id.tvCourse);
        tvStudyYear = findViewById(R.id.tvStudyYear);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Get userId from intent
        String userId = getIntent().getStringExtra("userId");

        // Set up the ViewPager with an adapter
        FragmentAdapter adapter = new FragmentAdapter(this, userId);  // Pass the userId
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

        Toolbar toolbar = findViewById(R.id.top_toolbar);
        setSupportActionBar(toolbar);

        // Enable the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Selected User"); // Set title
        }

        // Fetch user details
        fetchUser();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the back button click
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchUser() {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlViewAllUser,
                response -> {
                    if (response.equals("Error")) {
                        Toast.makeText(this, "Error in retrieving user data", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String[] users = response.split(":");
                    String userId = getIntent().getStringExtra("userId");

                    for (String user : users) {
                        if (!user.isEmpty()) {
                            String[] details = user.split(";");
                            if (details.length >= 5) {
                                String id = details[0];
                                if (id.equals(userId)) {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("userId", userId);
                                    map.put("name", details[1]);
                                    map.put("email", details[2]);
                                    map.put("course", details[5]);
                                    map.put("study_year", details[6]);
                                    map.put("graduated_year", details[7]);
                                    map.put("role", details[9]);
                                    map.put("user_photo", details[10]);
                                    displayUserDetails(map, details[8]);
                                    Log.d("UserDetails", "Fetched Details: " + map);
                                }
                            }
                        }
                    }
                },

                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(this, "Error retrieving user data", Toast.LENGTH_LONG).show();
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

            String photoData = userDetails.get("user_photo");
            Log.d("UserDetails", "Photo Data: " + userDetails.get("user_photo"));
            if (photoData != null && !photoData.isEmpty()) {
                saveBase64ToFile(photoData, file -> {
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

            String photoData = userDetails.get("user_photo");
            if (photoData != null && !photoData.isEmpty()) {
                try {
                    photoData = photoData.replace("\n", "").replace("\r", "");
                    saveBase64ToFile(photoData, file -> {
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
        else if ("3".equals(role)) { // Role 2 - Alumni
            tvName.setText(userDetails.get("name"));
            tvRole.setText(userDetails.get("role"));
            tvCourse.setText(userDetails.get("course"));
            tvStudyYear.setText("Email: " + userDetails.get("email"));

            String photoData = userDetails.get("user_photo");
            if (photoData != null && !photoData.isEmpty()) {
                try {
                    photoData = photoData.replace("\n", "").replace("\r", "");
                    saveBase64ToFile(photoData, file -> {
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

    private void saveBase64ToFile(String base64Data, OnFileSavedListener listener) {
        try {
            byte[] decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
            Log.d("Base64Decode", "Decoded bytes length: " + decodedBytes.length);

            File cacheDir = this.getCacheDir();
            File imageFile = new File(cacheDir, "community_image.png");

            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                fos.write(decodedBytes);
                fos.flush();
                listener.onFileSaved(imageFile);
            }

        } catch (Exception e) {
            Log.e("FileSaveError", "Error saving Base64 to file: " + e.getMessage());
        }
    }

    private static class FragmentAdapter extends FragmentStateAdapter {
        private final String userId;

        // Pass userId to the adapter constructor
        public FragmentAdapter(@NonNull FragmentActivity fragmentActivity, String userId) {
            super(fragmentActivity);
            this.userId = userId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    ViewSelectedAccountInternshipFragment fragment = new ViewSelectedAccountInternshipFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", userId);  // Pass userId from the adapter to the fragment
                    fragment.setArguments(bundle);
                    Log.d("FragmentAdapter", "Created fragment with userId: " + bundle);
                    return fragment;
                case 1:
                    ViewSelectedAccountQuestionFragment questionFragment = new ViewSelectedAccountQuestionFragment();
                    Bundle questionbundle = new Bundle();
                    questionbundle.putString("userId", userId);  // Pass userId from the adapter to the fragment
                    questionFragment.setArguments(questionbundle);
                    Log.d("FragmentAdapter", "Created fragment with userId: " + questionbundle);
                    return questionFragment;
                default:
                    return new ViewAccountFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Adjust based on the number of tabs
        }
    }

    interface OnFileSavedListener {
        void onFileSaved(File file);
    }
}
