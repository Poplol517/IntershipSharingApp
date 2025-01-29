package mdad.localdata.intershipsharingapp;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

public class ViewPieDetailActivity extends AppCompatActivity {
    private static final String urlViewAllUser = StaffMainActivity.ipBaseAddress + "/get_all_user.php";
    private LinearLayout lv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pie_detail);
        lv = findViewById(R.id.list);

        Toolbar toolbar = findViewById(R.id.top_toolbar);
        TextView title = findViewById(R.id.title);
        setSupportActionBar(toolbar);

        // Enable the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Details"); // Set title
        }

        // Retrieve the intent data
        String category = getIntent().getStringExtra("category");
        title.setText("Users registered as "+ category);
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

                    // Retrieve the communityId from the arguments
                    String category = getIntent().getStringExtra("category");
                    Log.d("Category", "Category: " + category);

                    String[] users = response.split(":");

                    for (String user : users) {
                        if (!user.isEmpty()) {
                            String[] details = user.split(";");
                            if (details.length >= 5) {
                                // Extract the communityId and ownerId from the response
                                String role = details[9];
                                // Check if the communityId matches the target communityId
                                if (role.equals(category)) {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("userId", details[0]);
                                    map.put("name", details[1]);
                                    map.put("course", details[5]);
                                    map.put("role", role);
                                    map.put("user_photo", details[10]);
                                    addUserToLayout(map);

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

    private void addUserToLayout(final HashMap<String, String> item) {
        Log.d("UserDetails", "Item: " + item);

        // Inflate the custom layout for displaying user info
        View postView = LayoutInflater.from(this).inflate(R.layout.user_item, lv, false);

        // Set data for user info
        ImageView profilePhoto = postView.findViewById(R.id.profile_icon);
        TextView user_Name = postView.findViewById(R.id.post_user_name);
        TextView user_Role = postView.findViewById(R.id.post_user_role);
        TextView roleTag = postView.findViewById(R.id.role_tag);

        // Populate the fields with dynamic data
        user_Name.setText(item.get("name"));
        user_Role.setText(item.get("course"));

        // Handle the image data for profile photo
        String photoData = item.get("user_photo");
        if (photoData != null && !photoData.isEmpty()) {
            saveBase64ToFile(photoData, file -> {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) {
                    profilePhoto.setImageBitmap(bitmap);
                } else {
                    profilePhoto.setImageResource(R.drawable.account);
                }
            });
        } else {
            profilePhoto.setImageResource(R.drawable.no_image);
        }
        roleTag.setVisibility(View.GONE);

        // Add long press listener to show Bottom Sheet
        postView.setOnLongClickListener(v -> {
            showBottomSheetDialog(item);
            return true; // Indicate that the long press was handled
        });

        postView.setOnClickListener(v -> {
            openUserDetailActivity(item);
        });

        // Add the postView to the parent layout (list of users)
        lv.addView(postView);
    }

    private void showBottomSheetDialog(HashMap<String, String> user) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_user, null);

        Button editUser = sheetView.findViewById(R.id.btn_edit_user);
        Button deleteUser = sheetView.findViewById(R.id.btn_delete_user);

        editUser.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Toast.makeText(this, "Edit Message clicked for " + user.get("name"), Toast.LENGTH_SHORT).show();
            // Implement your edit message logic here
        });

        // Handle Delete Message button click
        deleteUser.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Toast.makeText(this, "Delete Message clicked for " + user.get("name"), Toast.LENGTH_SHORT).show();
            // Implement your delete message logic here
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    private void openUserDetailActivity(HashMap<String, String> user) {

        // Create an intent to navigate to the ViewSelectedAccountActivity
        Intent intent = new Intent(this, ViewSelectedAccountActivity.class);

        // Pass the user data as extras
        intent.putExtra("userId", user.get("userId"));
        intent.putExtra("name", user.get("name"));
        intent.putExtra("course", user.get("course"));
        intent.putExtra("role", user.get("role"));

        // Start the activity
        startActivity(intent);
    }


    private void saveBase64ToFile(String base64Data, ViewAccountFragment.OnFileSavedListener listener) {
        try {
            // Decode the base64 data into a byte array
            byte[] decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
            Log.d("Base64Decode", "Decoded bytes length: " + decodedBytes.length);

            // Create the PNG file in the cache directory
            File cacheDir = this.getCacheDir();
            File imageFile = new File(cacheDir, "community_image.png");  // PNG extension

            // Save the decoded bytes to the file
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                fos.write(decodedBytes);
                fos.flush();  // Ensure all data is written
                listener.onFileSaved(imageFile);  // Notify when file is saved
            }

        } catch (Exception e) {
            Log.e("FileSaveError", "Error saving Base64 to file: " + e.getMessage());
        }
    }
}