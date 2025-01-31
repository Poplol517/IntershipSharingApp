package mdad.localdata.intershipsharingapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewBarDetailActivity extends AppCompatActivity {
    private static final String urlViewAllUser = StaffMainActivity.ipBaseAddress + "/get_all_user.php";
    private static final String url_delete_user = StaffMainActivity.ipBaseAddress + "/delete_user.php";

    private LinearLayout lv;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<HashMap<String, String>> userList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bar_detail);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::fetchUser);
        lv = findViewById(R.id.list);

        Toolbar toolbar = findViewById(R.id.top_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Details");
        }

        String category = getIntent().getStringExtra("category");
        TextView title = findViewById(R.id.title);
        title.setText("Users registered on " + category);

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return false;
            }
        });

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
        swipeRefreshLayout.setRefreshing(true); // Show refresh indicator
        lv.removeAllViews(); // Clear the list before adding new items

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlViewAllUser,
                response -> {
                    swipeRefreshLayout.setRefreshing(false); // Hide refresh indicator

                    if (response.equals("Error")) {
                        Toast.makeText(this, "Error in retrieving user data", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Retrieve the category from intent
                    String category = getIntent().getStringExtra("category");
                    Log.d("Category", "Category: " + category);

                    String[] users = response.split(":");

                    for (String user : users) {
                        if (!user.isEmpty()) {
                            String[] details = user.split(";");
                            if (details.length >= 6) {
                                String date_joined = details[11];

                                // Check if the category matches before adding
                                if (date_joined.equals(category)) {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("userId", details[0]);
                                    map.put("name", details[1]);
                                    map.put("course", details[5]);
                                    map.put("roleId", details[8]);
                                    map.put("role", details[9]);
                                    map.put("user_photo", details[10]);
                                    userList.add(map); // Store all users
                                }
                            }
                        }
                    }
                    updateUserList(userList); // Display all users initially
                },

                error -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(this, "Error retrieving user data", Toast.LENGTH_LONG).show();
                });

        queue.add(stringRequest);
    }


    private void addUserToLayout(final HashMap<String, String> item) {
        Log.d("UserDetails", "Item: " + item);

        // Add the new item to the memberList for later use in the dialog

        // Inflate the custom layout for displaying user chat info
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
                    profilePhoto.setImageResource(R.drawable.no_image);
                }
            });
        } else {
            profilePhoto.setImageResource(R.drawable.no_image);
        }
        roleTag.setVisibility(View.GONE);

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

    private void filterUsers(String query) {
        ArrayList<HashMap<String, String>> filteredList = new ArrayList<>();

        for (HashMap<String, String> user : userList) {
            String name = user.get("name").toLowerCase();
            String course = user.get("course").toLowerCase();

            if (name.contains(query.toLowerCase()) || course.contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }

        updateUserList(filteredList);
    }
    private void updateUserList(ArrayList<HashMap<String, String>> list) {
        lv.removeAllViews(); // Clear the UI before adding new views
        for (HashMap<String, String> user : list) {
            addUserToLayout(user);
        }
    }

    private void showBottomSheetDialog(HashMap<String, String> user) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_user, null);

        Button editUser = sheetView.findViewById(R.id.btn_edit_user);
        Button deleteUser = sheetView.findViewById(R.id.btn_delete_user);

        editUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditSelectedAccountActivity.class);

            // Pass the user data as extras
            intent.putExtra("userId", user.get("userId"));
            intent.putExtra("name", user.get("name"));
            intent.putExtra("course", user.get("course"));
            intent.putExtra("role", user.get("role"));
            intent.putExtra("roleId", user.get("roleId"));

            // Start the activity
            startActivity(intent);
            bottomSheetDialog.dismiss();
        });

        // Handle Delete Message button click
        deleteUser.setOnClickListener(v -> {
            String userId = user.get("userId");
            deleteUser(userId);
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

    private void deleteUser(String userId) {
        if (this != null) {
            // API endpoint for creating a message

            // Use Volley to make the POST request
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url_delete_user,
                    response -> {
                        Log.d("CreateUserchatResponse", response);

                        if (response.trim().equals("Error")) {
                            Toast.makeText(this, "Error deleting user", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "You have succesfully deleted a user", Toast.LENGTH_SHORT).show();


                        }
                    },
                    error -> {
                        Log.e("VolleyError", "Error leaving community: " + error.getMessage());
                        Toast.makeText(this, "Error leaving coummnity", Toast.LENGTH_LONG).show();
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    // Add POST parameters to the request
                    Map<String, String> params = new HashMap<>();
                    params.put("userId", userId);
                    return params;
                }
            };

            // Add the request to the Volley request queue
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(stringRequest);
        } else {
            Log.w("FragmentError", "Fragment is not attached to a context, skipping createMessage call.");
        }
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